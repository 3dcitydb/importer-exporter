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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.geometry.GeometryObject;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.CacheTable;
import de.tub.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.util.Util;

public class XlinkTexCoordList implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	private final Connection batchConn;
	private final CacheTable cacheTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectRing;
	private PreparedStatement psSelectGeometry;
	private PreparedStatement psTextureParam;

	private int batchCounter;

	public XlinkTexCoordList(Connection batchConn, CacheTable cacheTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.cacheTable = cacheTable;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psSelectRing = cacheTable.getConnection().prepareStatement(new StringBuilder()
		.append("select TEXTURE_COORDINATES, TEXTURE_COORDINATES_ID from ").append(cacheTable.getTableName()).append(" ")
		.append("where GMLID=? and TEXTURE_COORDINATES_ID > 0").toString());

		psSelectGeometry = batchConn.prepareStatement(new StringBuilder()
		.append("select ID, GEOMETRY, IMPLICIT_GEOMETRY, IS_REVERSE from SURFACE_GEOMETRY where ID=? OR PARENT_ID=? order by ID").toString());

		psTextureParam = batchConn.prepareStatement(new StringBuilder()
		.append("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, TEXTURE_COORDINATES, SURFACE_DATA_ID) values ")
		.append("(?, 1, ?, ?)").toString());
	}

	public boolean insert(DBXlinkTextureParam xlink) throws SQLException {		
		// check whether we deal with a local gml:id
		// remote gml:ids are not supported so far...
		if (Util.isRemoteXlink(xlink.getGmlId()))
			return false;

		UIDCacheEntry geometryEntry = resolverManager.getDBId(xlink.getGmlId(), CityGMLClass.ABSTRACT_GML_GEOMETRY, true);
		if (geometryEntry == null || geometryEntry.getId() == -1)
			return false;

		ResultSet rs = null;

		try {
			// first, get all texture coordinates associated with the
			// target from the cache table
			psSelectRing.setString(1, xlink.getGmlId());
			rs = psSelectRing.executeQuery();

			HashMap<Integer, double[]> texCoordsMap = new HashMap<Integer, double[]>();
			texCoordsMap.put(0, xlink.getTextureCoord().getCoordinates(0));

			while (rs.next()) {
				Object texCoords = rs.getObject(1);
				int texCoordsId = rs.getInt(2);

				GeometryObject geomObj = resolverManager.getCacheAdapter().getGeometryConverter().getPolygon(texCoords);
				texCoordsMap.put(texCoordsId, geomObj.getCoordinates(0));
			}

			rs.close();

			// sanity check
			for (int i = 1; i < texCoordsMap.size(); i++) {
				if (texCoordsMap.get(i) == null) {
					LOG.error(new StringBuilder("Failed to rebuild texture coordinates for target '").append(xlink.getGmlId()).append("'. Skipping target.").toString());
					return false;
				}
			}

			// second, retrieve geometries and sub-geometries from target
			psSelectGeometry.setLong(1, geometryEntry.getId());
			psSelectGeometry.setLong(2, geometryEntry.getId());
			rs = psSelectGeometry.executeQuery();

			int nrRings = 0;
			List<SurfaceGeometryTarget> geometryTargets = new ArrayList<SurfaceGeometryTarget>();

			while (rs.next()) {
				Object geometry = rs.getObject(2);
				Object implicitGeometry = rs.getObject(3);
				if (geometry == null && implicitGeometry == null)
					continue;

				long id = rs.getLong(1);
				boolean isReverse = rs.getBoolean(4);
				GeometryObject geomObj = resolverManager.getDatabaseAdapter().getGeometryConverter().getPolygon(geometry != null ? geometry : implicitGeometry);

				geometryTargets.add(new SurfaceGeometryTarget(id, geomObj.getNumElements(), isReverse));
				nrRings += geomObj.getNumElements();
			}

			rs.close();

			// sanity check
			if (texCoordsMap.size() != nrRings) {
				LOG.error(new StringBuilder("Target '").append(xlink.getGmlId()).append("': Not all rings in target geometry receive texture coordinates. Skipping target.").toString());
				return false;
			}

			// finally, assign texture coordinates to surface geometries
			for (int i = 0, ringElement = 0; i < geometryTargets.size(); i++) {
				SurfaceGeometryTarget geometryTarget = geometryTargets.get(i);
				
				// since the cache table only stores texture coordinates for a single linear ring,
				// we possible must combine multiple texture coordinates in case the target
				// surface geometry has more than one linear ring
				double[][] coordinates = new double[geometryTarget.nrOfRings][];
				for (int j = 0; j < geometryTarget.nrOfRings; j++) {
					if (!geometryTarget.isReverse)
						coordinates[j] = texCoordsMap.get(ringElement++);
					else {
						double[] texCoords = texCoordsMap.get(ringElement++);
						coordinates[j] = new double[texCoords.length];
						for (int n = texCoords.length - 2, m = 0; n >= 0; n -= 2) {
							coordinates[j][m++] = texCoords[n];
							coordinates[j][m++] = texCoords[n + 1];
						}
					}
				}

				GeometryObject texCoords = GeometryObject.createPolygon(coordinates, 2, 0);

				psTextureParam.setLong(1, geometryTarget.id);
				psTextureParam.setObject(2, resolverManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(texCoords, batchConn));
				psTextureParam.setLong(3, xlink.getId());

				psTextureParam.addBatch();
				if (++batchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
					executeBatch();

				if (xlink.getTexParamGmlId() != null) {
					// make sure xlinks to the corresponding texture parameterization can be resolved
					resolverManager.propagateXlink(new DBXlinkTextureAssociation(
							xlink.getId(),
							geometryTarget.id,
							xlink.getTexParamGmlId()));
				}
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
		psSelectRing.close();
		psSelectGeometry.close();
		psTextureParam.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXCOORDLIST;
	}
	
	private final class SurfaceGeometryTarget {
		private final long id;
		private final int nrOfRings;
		private final boolean isReverse;
		
		private SurfaceGeometryTarget(long id, int nrOfRings, boolean isReverse) {
			this.id = id;
			this.nrOfRings = nrOfRings;
			this.isReverse = isReverse;
		}
	}

}
