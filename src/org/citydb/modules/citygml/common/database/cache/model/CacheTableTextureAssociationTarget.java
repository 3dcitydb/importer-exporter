/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
import java.sql.SQLException;
import java.sql.Statement;

import org.citydb.database.adapter.AbstractSQLAdapter;


public class CacheTableTextureAssociationTarget extends CacheTableModel {
	public static CacheTableTextureAssociationTarget instance = null;
	
	private CacheTableTextureAssociationTarget() {		
	}
	
	public synchronized static CacheTableTextureAssociationTarget getInstance() {
		if (instance == null)
			instance = new CacheTableTextureAssociationTarget();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.TEXTUREASSOCIATION_TARGET;
	}
	
	@Override
	protected String getColumns(AbstractSQLAdapter sqlAdapter) {
		StringBuilder builder = new StringBuilder("(")
		.append("SURFACE_DATA_ID ").append(sqlAdapter.getInteger()).append(", ")
		.append("SURFACE_GEOMETRY_ID ").append(sqlAdapter.getInteger()).append(", ")
		.append("GMLID ").append(sqlAdapter.getCharacterVarying(256))
		.append(")");
		
		return builder.toString();
	}
}
