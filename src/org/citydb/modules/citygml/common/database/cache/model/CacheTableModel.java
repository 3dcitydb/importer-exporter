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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.citydb.database.adapter.AbstractSQLAdapter;


public abstract class CacheTableModel {

	public void create(Connection conn, String tableName, AbstractSQLAdapter sqlAdapter) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlAdapter.getCreateUnloggedTable(tableName, getColumns(sqlAdapter)));
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	public void createAsSelectFrom(Connection conn, String tableName, String sourceTableName, AbstractSQLAdapter sqlAdapter) throws SQLException {
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlAdapter.getCreateUnloggedTableAsSelectFrom(tableName, sourceTableName));
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	public long size(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		long count = -1;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from " + tableName);

			if (rs.next())
				count = rs.getLong(1);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}

			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}

		return count;
	}

	public void truncate(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("delete from " + tableName);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	public void drop(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("drop table " + tableName);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		// override in subclasses if necessary
	}
	
	public abstract CacheTableModelEnum getType();
	protected abstract String getColumns(AbstractSQLAdapter sqlAdapter);
}
