/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;


public class CacheTableGmlId extends CacheTableModel {
	private static HashMap<CacheTableModelEnum, CacheTableGmlId> cacheTableMap;
	private final CacheTableModelEnum type;
	
	private CacheTableGmlId(CacheTableModelEnum type) {
		// just to thwart instantiation
		this.type = type;
	}
	
	public synchronized static CacheTableGmlId getInstance(CacheTableModelEnum type) {
		switch (type) {
		case GMLID_FEATURE:
		case GMLID_GEOMETRY:
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type " + type);
		}
		
		if (cacheTableMap == null)
			cacheTableMap = new HashMap<CacheTableModelEnum, CacheTableGmlId>();
		
		CacheTableGmlId cacheTable = cacheTableMap.get(type);
		if (cacheTable == null) {
			cacheTable = new CacheTableGmlId(type);
			cacheTableMap.put(type, cacheTable);
		}
		
		return cacheTable;
	}
	
	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (ID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return type;
	}
	
	@Override
	protected String getColumns() {
		return "(GMLID VARCHAR2(256), " +
		"ID NUMBER, " +
		"ROOT_ID NUMBER, " +
		"REVERSE NUMBER(1,0), " +
		"MAPPING VARCHAR2(256)," +
		"TYPE NUMBER(3))";
	}
}
