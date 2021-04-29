/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.citygml.importer.database.xlink.resolver;

import org.citydb.citygml.common.cache.CacheTable;
import org.citydb.citygml.common.cache.IdCacheEntry;
import org.citydb.citygml.common.xlink.DBXlinkTextureAssociation;
import org.citydb.citygml.common.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class XlinkTextureAssociation implements DBXlinkResolver {
	private final Logger log = Logger.getInstance();
	private final DBXlinkResolverManager manager;
	private final PreparedStatement psTextureParam;
	private final PreparedStatement psSelectParts;
	private final PreparedStatement psSelectContent;

	private int batchCounter;

	public XlinkTextureAssociation(Connection connection, CacheTable cacheTable, DBXlinkResolverManager manager) throws SQLException {
		this.manager = manager;
		String schema = manager.getDatabaseAdapter().getConnectionDetails().getSchema();

		psTextureParam = connection.prepareStatement("insert into " + schema + ".TEXTUREPARAM (SURFACE_GEOMETRY_ID, " +
				"IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_DATA_ID) " +
				"values (?, 1, ?, ?, ?)");
		
		psSelectParts = cacheTable.getConnection().prepareStatement("select SURFACE_DATA_ID, SURFACE_GEOMETRY_ID from " +
				cacheTable.getTableName() + " where GMLID=?");

		psSelectContent = connection.prepareStatement("select WORLD_TO_TEXTURE, TEXTURE_COORDINATES from " + schema
				+ ".TEXTUREPARAM where SURFACE_DATA_ID=? and SURFACE_GEOMETRY_ID=?");
	}

	public boolean insert(DBXlinkTextureAssociation xlink) throws SQLException {
		String gmlId = xlink.getGmlId();

		// check whether we deal with a local gml:id
		// remote gml:ids are not supported so far...
		if (Util.isRemoteXlink(gmlId))
			return false;

		// replace leading #
		gmlId = gmlId.replaceAll("^#", "");
		psSelectParts.setString(1, gmlId);

		List<DBXlinkTextureAssociationTarget> texAssList = new ArrayList<>();
		try (ResultSet rs = psSelectParts.executeQuery()) {
			// one gml:id pointing to a texture association may consist of
			// different parts. so firstly retrieve these parts
			while (rs.next()) {
				long surfaceDataId = rs.getLong("SURFACE_DATA_ID");
				long surfaceGeometryId = rs.getLong("SURFACE_GEOMETRY_ID");
				texAssList.add(new DBXlinkTextureAssociationTarget(surfaceDataId, surfaceGeometryId, null));
			}
		}

		for (DBXlinkTextureAssociationTarget texAss : texAssList) {
			psSelectContent.setLong(1, texAss.getSurfaceDataId());
			psSelectContent.setLong(2, texAss.getSurfaceGeometryId());

			try (ResultSet rs = psSelectContent.executeQuery()) {
				if (rs.next()) {
					String worldToTexture = rs.getString("WORLD_TO_TEXTURE");
					if (!rs.wasNull()) {
						IdCacheEntry entry = manager.getGeometryId(xlink.getTargetURI());
						if (entry == null || entry.getId() == -1) {
							log.error("Failed to resolve XLink reference '" + xlink.getTargetURI() + "'.");
							continue;
						}

						psTextureParam.setLong(1, entry.getId());
						psTextureParam.setString(2, worldToTexture);
						psTextureParam.setNull(3, Types.VARCHAR);
					} else {
						// ok, if we deal with texture coordinates we can ignore the
						// uri attribute of the <target> element. it must be the same as
						// in the referenced <target> element. so we let the new entry point
						// to the same surface_geometry entry...

						psTextureParam.setLong(1, texAss.getSurfaceGeometryId());
						psTextureParam.setNull(2, Types.VARCHAR);
						psTextureParam.setObject(3, rs.getObject("TEXTURE_COORDINATES"));
					}

					psTextureParam.setLong(4, xlink.getId());
					psTextureParam.addBatch();

					if (++batchCounter == manager.getDatabaseAdapter().getMaxBatchSize())
						manager.executeBatch(this);
				} else {
					log.warn("Failed to completely resolve XLink reference '" + gmlId + "' to " + CityGMLClass.TEXTURE_ASSOCIATION + ".");
				}
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
