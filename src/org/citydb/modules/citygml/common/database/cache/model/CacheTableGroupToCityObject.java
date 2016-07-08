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
package org.citydb.modules.citygml.common.database.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.citydb.database.adapter.AbstractSQLAdapter;


public class CacheTableGroupToCityObject extends CacheTableModel {
	public static CacheTableGroupToCityObject instance = null;
	
	private CacheTableGroupToCityObject() {		
	}
	
	public synchronized static CacheTableGroupToCityObject getInstance() {
		if (instance == null)
			instance = new CacheTableGroupToCityObject();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();	
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GROUP_ID) " + properties);
			stmt.executeUpdate("create index idx1_" + tableName + " on " + tableName + " (IS_PARENT) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.GROUP_TO_CITYOBJECT;
	}
	
	@Override
	protected String getColumns(AbstractSQLAdapter sqlAdapter) {
		StringBuilder builder = new StringBuilder("(")
		.append("GROUP_ID ").append(sqlAdapter.getInteger()).append(", ")
		.append("GMLID ").append(sqlAdapter.getCharacterVarying(256)).append(", ")
		.append("IS_PARENT ").append(sqlAdapter.getNumeric(1, 0)).append(", ")
		.append("ROLE ").append(sqlAdapter.getCharacterVarying(256))
		.append(")");
		
		return builder.toString();
	}
}
