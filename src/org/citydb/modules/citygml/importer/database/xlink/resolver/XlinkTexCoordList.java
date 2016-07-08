/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.citydb.api.geometry.GeometryObject;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureCoordList;
import org.citydb.util.Util;

public class XlinkTexCoordList implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();
	private final Connection batchConn;
	private final CacheTable texCoords;
	private final CacheTable linearRings;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psSelectTexCoords;
	private PreparedStatement psSelectTexCoordsByGmlId;
	private PreparedStatement psSelectLinearRings;
	private PreparedStatement psTextureParam;

	private int batchCounter;

	public XlinkTexCoordList(Connection batchConn, CacheTable texCoords, CacheTable linearRings, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.texCoords = texCoords;
		this.linearRings = linearRings;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psSelectTexCoords = texCoords.getConnection().prepareStatement(new StringBuilder()
		.append("select GMLID, TEXTURE_COORDINATES from ").append(texCoords.getTableName()).append(" ")
		.append("where TARGET_ID=? and ID=?").toString());

		psSelectTexCoordsByGmlId = texCoords.getConnection().prepareStatement(new StringBuilder()
		.append("select GMLID, TEXTURE_COORDINATES from ").append(texCoords.getTableName()).append(" ")
		.append("where GMLID=?").toString());

		psSelectLinearRings = linearRings.getConnection().prepareStatement(new StringBuilder()
		.append("select GMLID, RING_NO from ").append(linearRings.getTableName()).append(" where PARENT_ID = ?").toString());

		psTextureParam = batchConn.prepareStatement(new StringBuilder()
		.append("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, TEXTURE_COORDINATES, SURFACE_DATA_ID) values ")
		.append("(?, 1, ?, ?)").toString());
	}

	public boolean insert(DBXlinkTextureCoordList xlink) throws SQLException {
		// check whether we deal with a local gml:id
		// remote gml:ids are not supported so far...
		if (Util.isRemoteXlink(xlink.getGmlId()))
			return false;

		ResultSet rs = null;

		try {
			// step 1: get linear rings
			psSelectLinearRings.setLong(1, xlink.getSurfaceGeometryId());
			rs = psSelectLinearRings.executeQuery();

			long surfaceGeometryId = xlink.getSurfaceGeometryId();
			boolean reverse = xlink.isReverse();

			HashMap<String, Integer> ringNos = new HashMap<String, Integer>();
			while (rs.next()) {
				String ringId = rs.getString(1);
				int ringNo = rs.getInt(2);
				ringNos.put(ringId, ringNo);
			}

			rs.close();

			if (surfaceGeometryId == 0)
				return false;

			// step 2: get texture coordinates
			if (ringNos.size() == 1) {
				psSelectTexCoordsByGmlId.setString(1, ringNos.keySet().iterator().next());
				rs = psSelectTexCoordsByGmlId.executeQuery();
			} else {			
				psSelectTexCoords.setLong(1, xlink.getTargetId());
				psSelectTexCoords.setLong(2, xlink.getId());
				rs = psSelectTexCoords.executeQuery();
			}

			double[][] texCoords = new double[ringNos.size()][];
			while (rs.next()) {
				String ringId = rs.getString(1);
				GeometryObject texCoord = resolverManager.getCacheAdapter().getGeometryConverter().getPolygon(rs.getObject(2));
				if (texCoord != null && ringNos.containsKey(ringId))
					texCoords[ringNos.get(ringId)] = texCoord.getCoordinates(0);
			}

			rs.close();

			// step 3: sanity check
			for (int i = 0; i < texCoords.length; i++) {
				if (texCoords[i] == null) {
					for (Entry<String, Integer> entry : ringNos.entrySet()) {
						if (entry.getValue() == i) {
							LOG.warn("Missing texture coordinates for ring '" + entry.getValue() + "'.");
							return false;
						}
					}
				}
			}

			// step 4: reverse texture coordinates if required
			if (reverse) {
				for (int i = 0; i < texCoords.length; i++) {
					double[] tmp = new double[texCoords[i].length];
					for (int j = texCoords[i].length - 2, n = 0; j >= 0; j -=2) {
						tmp[n++] = texCoords[i][j];
						tmp[n++] = texCoords[i][j + 1];
					}

					texCoords[i] = tmp;
				}
			}

			// step 5: update textureparam
			psTextureParam.setLong(1, surfaceGeometryId);
			psTextureParam.setObject(2, resolverManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(GeometryObject.createPolygon(texCoords, 2, 0), batchConn));
			psTextureParam.setLong(3, xlink.getId());

			psTextureParam.addBatch();
			if (++batchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
				executeBatch();

			if (xlink.getTexParamGmlId() != null) {
				// make sure xlinks to the corresponding texture parameterization can be resolved
				resolverManager.propagateXlink(new DBXlinkTextureAssociationTarget(
						xlink.getId(),
						surfaceGeometryId,
						xlink.getTexParamGmlId()));
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
		psSelectTexCoords.close();
		psSelectTexCoordsByGmlId.close();
		psSelectLinearRings.close();
		psTextureParam.close();
	}

	@Override
	public DBXlinkResolverEnum getDBXlinkResolverType() {
		return DBXlinkResolverEnum.TEXCOORDLIST;
	}

}
