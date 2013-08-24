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
package de.tub.citydb.modules.citygml.common.database.cache.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public abstract class CacheTableModel {

	public void create(Connection conn, String tableName, CacheTableType type) throws SQLException {
		switch (type) {
		case GLOBAL_TEMPORARY_TABLE:
			createGlobalTemporaryTable(conn, tableName);
			break;
		case HEAP_TABLE:
			createHeapTable(conn, tableName);
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type: " + type);
		}
	}

	private void createGlobalTemporaryTable(Connection conn, String tableName) throws SQLException {		
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create global temporary table " + 
					tableName + 
					getColumns() + 
			"on commit preserve rows");
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	private void createHeapTable(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create table " + 
					tableName + 
					getColumns() +
					" nologging");
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	public void createAsSelectFrom(Connection conn, String tableName, String sourceTableName) throws SQLException {
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create table " + 
					tableName +
					" nologging" +
					" as select * from " + 
					sourceTableName);
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
			stmt.executeUpdate("truncate table " + tableName);
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
	
	public void createIndexes(Connection conn, String tableName) throws SQLException {
		createIndexes(conn, tableName, "");
	}
	
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		// override in subclasses if necessary
	}
	
	public abstract CacheTableModelEnum getType();
	protected abstract String getColumns();
}
