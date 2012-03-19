/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.HeapCacheTable;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.util.Util;

public class XlinkTextureAssociation implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final HeapCacheTable heapTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTextureParam;
	private PreparedStatement psSelectParts;
	private PreparedStatement psSelectContent;
	
	private int batchCounter;

	public XlinkTextureAssociation(Connection batchConn, HeapCacheTable heapTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.heapTable = heapTable;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psTextureParam = batchConn.prepareStatement("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_DATA_ID) values " +
			"(?, 1, ?, ?, ?)");
		
		psSelectParts = heapTable.getConnection().prepareStatement("select SURFACE_DATA_ID, SURFACE_GEOMETRY_ID from " + heapTable.getTableName() + " where GMLID=?");
		psSelectContent = batchConn.prepareStatement("select WORLD_TO_TEXTURE, TEXTURE_COORDINATES from TEXTUREPARAM where SURFACE_DATA_ID=? " +
				"and SURFACE_GEOMETRY_ID=?");
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
			// one gml:id pointing to a texture association may consist of
			// different parts. so firstly retrieve these parts
			psSelectParts.setString(1, gmlId);
			rs = psSelectParts.executeQuery();

			List<DBXlinkTextureAssociation> texAssList = new ArrayList<DBXlinkTextureAssociation>();
			while (rs.next()) {
				long surfaceDataId = rs.getLong("SURFACE_DATA_ID");
				long surfaceGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");

				texAssList.add(new DBXlinkTextureAssociation(surfaceDataId, surfaceGeometryId, null));
			}

			rs.close();

			for (DBXlinkTextureAssociation texAss : texAssList) {
				psSelectContent.setLong(1, texAss.getSurfaceDataId());
				psSelectContent.setLong(2, texAss.getSurfaceGeometryId());
				rs = psSelectContent.executeQuery();

				if (rs.next()) {
					String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
					String texCoord = rs.getString("TEXTURE_COORDINATES");

					if (worldToTexture != null) {
						GmlIdEntry idEntry = resolverManager.getDBId(xlink.getTargetURI(), CityGMLClass.ABSTRACT_GML_GEOMETRY);

						if (idEntry == null || idEntry.getId() == -1) {
							LOG.error("Failed to resolve XLink reference '" + xlink.getTargetURI() + "'.");
							continue;
						}

						psTextureParam.setLong(1, idEntry.getId());
						psTextureParam.setString(2, worldToTexture);
						psTextureParam.setNull(3, Types.VARCHAR);
						psTextureParam.setLong(4, xlink.getId());

					} else {
						// ok, if we deal with texture coordinates we can ignore the
						// uri attribute of the <target> element. it must be the same as
						// in the referenced <target> element. so we let the new entry point
						// to the same surface_geometry entry...

						psTextureParam.setLong(1, texAss.getSurfaceGeometryId());
						psTextureParam.setNull(2, Types.VARCHAR);
						psTextureParam.setString(3, texCoord);
						psTextureParam.setLong(4, xlink.getId());
					}

					psTextureParam.addBatch();
					if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
						executeBatch();

				} else {
					LOG.warn("Failed to completely resolve XLink reference '" + gmlId + "' to TextureAssociation.");
				}

				rs.close();
			}

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

		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psTextureParam.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTextureParam.close();
		psSelectParts.close();
		psSelectContent.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.XLINK_TEXTUREASSOCIATION;
	}

}
