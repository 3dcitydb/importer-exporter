/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
