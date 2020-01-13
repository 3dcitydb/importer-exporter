/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.common.database.cache.model;

import org.citydb.database.adapter.AbstractSQLAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableSurfaceGeometry extends AbstractCacheTableModel {
	public static CacheTableSurfaceGeometry instance = null;
	
	public synchronized static CacheTableSurfaceGeometry getInstance() {
		if (instance == null)
			instance = new CacheTableSurfaceGeometry();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (PARENT_ID) " + properties);
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (ROOT_ID) " + properties);
		}
	}
	
	@Override
	public CacheTableModel getType() {
		return CacheTableModel.SURFACE_GEOMETRY;
	}
	
	@Override
	protected String getColumns(AbstractSQLAdapter sqlAdapter) {
		return "(" +
				"ID " + sqlAdapter.getInteger() + ", " +
				"PARENT_ID " + sqlAdapter.getInteger() + ", " +
				"ROOT_ID " + sqlAdapter.getInteger() + ", " +
				"REVERSE " + sqlAdapter.getNumeric(1, 0) + ", " +
				"GMLID " + sqlAdapter.getCharacterVarying(256) + ", " +
				"CITYOBJECT_ID " + sqlAdapter.getInteger() + ", " +
				"TABLE_NAME " + sqlAdapter.getCharacterVarying(64) + ", " +
				"FROM_COLUMN " + sqlAdapter.getCharacterVarying(64) +
				")";
	}

}
