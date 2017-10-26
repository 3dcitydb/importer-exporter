/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citydb.modules.citygml.importer.util.LocalTextureCoordinatesResolver.SurfaceGeometryTarget;

public class DBTextureParam implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psTextureParam;
	private int batchCounter;

	public DBTextureParam(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
		StringBuilder texCoordListStmt = new StringBuilder()
		.append("insert into TEXTUREPARAM (SURFACE_GEOMETRY_ID, IS_TEXTURE_PARAMETRIZATION, WORLD_TO_TEXTURE, TEXTURE_COORDINATES, SURFACE_DATA_ID) values ")
		.append("(?, ?, ?, ?, ?)");
		psTextureParam = batchConn.prepareStatement(texCoordListStmt.toString());
	}

	public void insert(SurfaceGeometryTarget target, long surfaceDataId) throws SQLException {
		psTextureParam.setLong(1, target.getSurfaceGeometryId());
		psTextureParam.setInt(2, 1);
		psTextureParam.setNull(3, Types.VARCHAR);
		psTextureParam.setObject(4, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getDatabaseObject(target.compileTextureCoordinates(), batchConn));
		psTextureParam.setLong(5, surfaceDataId);

		addBatch();
	}

	public void insert(String worldToTexture, long surfaceDataId, long surfaceGeometryId) throws SQLException {
		psTextureParam.setLong(1, surfaceGeometryId);
		psTextureParam.setInt(2, 1);
		psTextureParam.setString(3, worldToTexture);
		psTextureParam.setNull(4, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
				dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
		psTextureParam.setLong(5, surfaceDataId);

		addBatch();
	}

	public void insert(long surfaceDataId, long surfaceGeometryId) throws SQLException {
		psTextureParam.setLong(1, surfaceGeometryId);
		psTextureParam.setInt(2, 0);
		psTextureParam.setNull(3, Types.VARCHAR);
		psTextureParam.setNull(4, dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryType(),
				dbImporterManager.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName());
		psTextureParam.setLong(5, surfaceDataId);

		addBatch();
	}

	private void addBatch() throws SQLException {
		psTextureParam.addBatch();
		if (++batchCounter == dbImporterManager.getDatabaseAdapter().getMaxBatchSize())
			dbImporterManager.executeBatch(DBImporterEnum.TEXTURE_PARAM);		
	}

	@Override
	public void executeBatch() throws SQLException {
		psTextureParam.executeBatch();
		batchCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		psTextureParam.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.TEXTURE_PARAM;
	}
}
