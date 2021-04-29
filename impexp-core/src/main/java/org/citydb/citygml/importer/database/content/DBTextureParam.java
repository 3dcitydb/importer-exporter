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
package org.citydb.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.util.LocalAppearanceHandler.SurfaceGeometryTarget;
import org.citydb.config.Config;
import org.citydb.database.schema.TableEnum;

public class DBTextureParam implements DBImporter {
	private final Connection batchConn;
	private final CityGMLImportManager importer;

	private PreparedStatement psTextureParam;
	private int batchCounter;

	public DBTextureParam(Connection batchConn, Config config, CityGMLImportManager importer) throws SQLException {
		this.batchConn = batchConn;
		this.importer = importer;

		String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

		String texCoordListStmt = "insert into " + schema + ".textureparam (surface_geometry_id, is_texture_parametrization, world_to_texture, texture_coordinates, surface_data_id) values " +
				"(?, ?, ?, ?, ?)";
		psTextureParam = batchConn.prepareStatement(texCoordListStmt);
	}

	protected void doImport(SurfaceGeometryTarget target, long surfaceDataId) throws CityGMLImportException, SQLException {
		psTextureParam.setLong(1, target.getSurfaceGeometryId());
		psTextureParam.setInt(2, 1);
		psTextureParam.setNull(3, Types.VARCHAR);
		psTextureParam.setObject(4, importer.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(target.compileTextureCoordinates(), batchConn));
		psTextureParam.setLong(5, surfaceDataId);

		addBatch();
	}

	protected void doImport(String worldToTexture, long surfaceDataId, long surfaceGeometryId) throws CityGMLImportException, SQLException {
		psTextureParam.setLong(1, surfaceGeometryId);
		psTextureParam.setInt(2, 1);
		psTextureParam.setString(3, worldToTexture);
		psTextureParam.setNull(4, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
				importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
		psTextureParam.setLong(5, surfaceDataId);

		addBatch();
	}

	protected void doImport(long surfaceDataId, long surfaceGeometryId) throws CityGMLImportException, SQLException {
		psTextureParam.setLong(1, surfaceGeometryId);
		psTextureParam.setInt(2, 0);
		psTextureParam.setNull(3, Types.VARCHAR);
		psTextureParam.setNull(4, importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
				importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
		psTextureParam.setLong(5, surfaceDataId);

		addBatch();
	}

	private void addBatch() throws CityGMLImportException, SQLException {
		psTextureParam.addBatch();
		if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
			importer.executeBatch(TableEnum.TEXTUREPARAM);		
	}

	@Override
	public void executeBatch() throws CityGMLImportException, SQLException {
		if (batchCounter > 0) {
			psTextureParam.executeBatch();
			batchCounter = 0;
		}
	}

	@Override
	public void close() throws CityGMLImportException, SQLException {
		psTextureParam.close();
	}

}
