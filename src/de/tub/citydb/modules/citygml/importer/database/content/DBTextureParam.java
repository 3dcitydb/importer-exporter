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
package de.tub.citydb.modules.citygml.importer.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import de.tub.citydb.modules.citygml.importer.util.LocalTextureCoordinatesResolver.SurfaceGeometryTarget;

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
