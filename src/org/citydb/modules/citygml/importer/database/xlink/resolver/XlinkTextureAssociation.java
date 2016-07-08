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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

public class XlinkTextureAssociation implements DBXlinkResolver {
	private final Logger LOG = Logger.getInstance();

	private final Connection batchConn;
	private final CacheTable cacheTable;
	private final DBXlinkResolverManager resolverManager;

	private PreparedStatement psTextureParam;
	private PreparedStatement psSelectParts;
	private PreparedStatement psSelectContent;
	
	private int batchCounter;

	public XlinkTextureAssociation(Connection batchConn, CacheTable cacheTable, DBXlinkResolverManager resolverManager) throws SQLException {
		this.batchConn = batchConn;
		this.cacheTable = cacheTable;
		this.resolverManager = resolverManager;

		init();
	}

	private void init() throws SQLException {
		psTextureParam = batchConn.prepareStatement("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_DATA_ID) values " +
			"(?, 1, ?, ?, ?)");
		
		psSelectParts = cacheTable.getConnection().prepareStatement("select SURFACE_DATA_ID, SURFACE_GEOMETRY_ID from " + cacheTable.getTableName() + " where GMLID=?");
		psSelectContent = batchConn.prepareStatement("select WORLD_TO_TEXTURE, TEXTURE_COORDINATES from TEXTUREPARAM where SURFACE_DATA_ID=? and SURFACE_GEOMETRY_ID=?");
	}

	public boolean insert(DBXlinkTextureAssociation xlink) throws SQLException {
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

			List<DBXlinkTextureAssociationTarget> texAssList = new ArrayList<DBXlinkTextureAssociationTarget>();
			while (rs.next()) {
				long surfaceDataId = rs.getLong("SURFACE_DATA_ID");
				long surfaceGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");

				texAssList.add(new DBXlinkTextureAssociationTarget(surfaceDataId, surfaceGeometryId, null));
			}

			rs.close();

			for (DBXlinkTextureAssociationTarget texAss : texAssList) {
				psSelectContent.setLong(1, texAss.getSurfaceDataId());
				psSelectContent.setLong(2, texAss.getSurfaceGeometryId());
				rs = psSelectContent.executeQuery();

				if (rs.next()) {
					String worldToTexture = rs.getString("WORLD_TO_TEXTURE");

					if (worldToTexture != null) {
						UIDCacheEntry entry = resolverManager.getDBId(xlink.getTargetURI(), CityGMLClass.ABSTRACT_GML_GEOMETRY);

						if (entry == null || entry.getId() == -1) {
							LOG.error("Failed to resolve XLink reference '" + xlink.getTargetURI() + "'.");
							continue;
						}

						psTextureParam.setLong(1, entry.getId());
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
						psTextureParam.setObject(3, rs.getObject("TEXTURE_COORDINATES"));
						psTextureParam.setLong(4, xlink.getId());
					}

					psTextureParam.addBatch();
					if (++batchCounter == resolverManager.getDatabaseAdapter().getMaxBatchSize())
						executeBatch();

				} else {
					LOG.warn("Failed to completely resolve XLink reference '" + gmlId + "' to " + CityGMLClass.TEXTURE_ASSOCIATION + ".");
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
