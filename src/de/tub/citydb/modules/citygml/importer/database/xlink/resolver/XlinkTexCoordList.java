/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.CacheTable;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.util.Util;

public class XlinkTexCoordList implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	
	private final Connection batchConn;
	private final CacheTable textureParamCacheTable;
	private final CacheTable linearRingCacheTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTexCoordList;
	private PreparedStatement psSelectLinearRing;
	private PreparedStatement psSelectInteriorLinearRing;
	private PreparedStatement psSelectTexCoord;
	
	private int batchCounter;

	public XlinkTexCoordList(Connection batchConn, CacheTable textureParamCacheTable, CacheTable linearRingCacheTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.textureParamCacheTable = textureParamCacheTable;
		this.linearRingCacheTable = linearRingCacheTable;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psTexCoordList = batchConn.prepareStatement("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE , TEXTURE_COORDINATES, SURFACE_DATA_ID) values " +
			"(?, 1, null, ?, ?)");

		Connection linearRingConn = linearRingCacheTable.getConnection();
		String linearRingTableName = linearRingCacheTable.getTableName();
		
		psSelectLinearRing = linearRingConn.prepareStatement("select RING_NO, PARENT_ID, REVERSE from " + linearRingTableName + " where GMLID=?");
		psSelectInteriorLinearRing = linearRingConn.prepareStatement("select GMLID, RING_NO from " + linearRingTableName +
				" where PARENT_ID=? and RING_NO<>0");
		psSelectTexCoord = textureParamCacheTable.getConnection().prepareStatement("select GMLID, TEXTURE_COORDINATES from " + textureParamCacheTable.getTableName() +
				" where TEXCOORDLIST_ID=? and not GMLID=?");
	}

	public boolean insert(DBXlinkTextureParam xlink) throws SQLException {
		String gmlId = xlink.getGmlId();
		ResultSet rs = null;

		// check whether we deal with a local gml:id
		// remote gml:ids are not supported so far...
		if (Util.isRemoteXlink(gmlId))
			return false;

		// replace leading #
		gmlId = gmlId.replaceAll("^#", "");
		
		try {
			// step 1: get the exterior linear ring element
			psSelectLinearRing.setString(1, gmlId);
			rs = psSelectLinearRing.executeQuery();

			if (!rs.next())
				return false;

			// if an interior ring is returned we silently discard it
			int exteriorRing = rs.getInt("RING_NO");
			if (exteriorRing != 0)
				return true;
			
			long parentId = rs.getLong("PARENT_ID");
			boolean reverse = rs.getBoolean("REVERSE");
			rs.close();

			// step 3: find all corresponding interior rings
			psSelectInteriorLinearRing.setLong(1, parentId);
			rs = psSelectInteriorLinearRing.executeQuery();

			HashMap<String, Integer> innerRingMap = new HashMap<String, Integer>();
			int maxRingNo = 0;
			while (rs.next()) {
				String innerGmlId = rs.getString("GMLID");
				int ringNo = rs.getInt("RING_NO");

				innerRingMap.put(innerGmlId, ringNo);
				if (ringNo > maxRingNo)
					maxRingNo = ringNo;
			}

			rs.close();

			// step 4: find corresponding texture coordinates
			String[] texCoordList = new String[maxRingNo + 1];
			String textureCoordinates = xlink.getTextureCoord();
			
			// reverse order of texture coordinates if necessary
			if (reverse)
				textureCoordinates = reverseTextureCoordinates(textureCoordinates);
			
			texCoordList[0] = textureCoordinates;
			for (int i = 1; i <= maxRingNo; i++)
				texCoordList[i] = "";

			psSelectTexCoord.setString(1, xlink.getTexCoordListId());
			psSelectTexCoord.setString(2, xlink.getGmlId());
			rs = psSelectTexCoord.executeQuery();

			while (rs.next()) {
				String innerGmlId = rs.getString("GMLID");
				textureCoordinates = rs.getString("TEXTURE_COORDINATES");

				if (Util.isRemoteXlink(innerGmlId))
					continue;
				
				// reverse order of texture coordinates if necessary
				if (reverse)					
					textureCoordinates = reverseTextureCoordinates(textureCoordinates);

				// replace leading #
				innerGmlId = innerGmlId.replaceAll("^#", "");
				if (innerRingMap.containsKey(innerGmlId))
					texCoordList[innerRingMap.get(innerGmlId)] = textureCoordinates;
			}

			// step 5: sanity check
			String texCoord = Util.collection2string(Arrays.asList(texCoordList), ";");
			if (texCoord.length() > 4000) {
				LOG.error("Texture coordinates for ring '" + gmlId + "' (and its interior rings) exceed 4000 characters and will not be imported.");
				return false;
			}
				
			if (texCoord.contains(";;") || texCoord.endsWith(";"))
				LOG.warn("Missing texture coordinates for ring '" + gmlId + "' (or its interior rings).");

			psTexCoordList.setLong(1, parentId);
			psTexCoordList.setString(2, texCoord);
			psTexCoordList.setLong(3, xlink.getId());

			psTexCoordList.addBatch();
			if (++batchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
				executeBatch();

			if (xlink.getTexParamGmlId() != null) {
				// make sure xlinks to the corresponding texture parameterization can be resolved
				resolverManager.propagateXlink(new DBXlinkTextureAssociation(
						xlink.getId(),
						parentId,
						xlink.getTexParamGmlId()));
			}

			return true;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					//
				}

				rs = null;
			}
		}
	}
	
	private String reverseTextureCoordinates(String textureCoordinates) {
		String[] coords = textureCoordinates.split("\\s+");
		
		for (int lower = 0, upper = coords.length - 2; lower < upper; lower += 2, upper -= 2) {
			String x = coords[lower];
			String y = coords[lower + 1];

			coords[lower] = coords[upper];
			coords[lower + 1] = coords[upper + 1];
			
			coords[upper] = x;
			coords[upper + 1] = y;
		}
		
		return Util.collection2string(Arrays.asList(coords), " ");
	}

	@Override
	public void executeBatch() throws SQLException {
		psTexCoordList.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTexCoordList.close();
		psSelectLinearRing.close();
		psSelectInteriorLinearRing.close();
		psSelectTexCoord.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXCOORDLIST;
	}

}
