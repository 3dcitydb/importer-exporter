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
import java.sql.SQLException;
import java.sql.Statement;

import de.tub.citydb.database.adapter.AbstractSQLAdapter;


public class CacheTableTextureParam extends CacheTableModel {
	public static CacheTableTextureParam instance = null;
	
	private CacheTableTextureParam() {		
	}
	
	public synchronized static CacheTableTextureParam getInstance() {
		if (instance == null)
			instance = new CacheTableTextureParam();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (TYPE) " + properties);
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (TEXTURE_COORDINATES_ID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.TEXTUREPARAM;
	}
	
	@Override
	protected String getColumns(AbstractSQLAdapter sqlAdapter) {
		StringBuilder builder = new StringBuilder("(")
		.append("ID ").append(sqlAdapter.getInteger()).append(", ")
		.append("GMLID ").append(sqlAdapter.getCharacterVarying(256)).append(", ")
		.append("TYPE ").append(sqlAdapter.getNumeric(3)).append(", ")
		.append("IS_TEXTURE_PARAMETERIZATION ").append(sqlAdapter.getNumeric(1, 0)).append(", ")
		.append("TEXPARAM_GMLID ").append(sqlAdapter.getCharacterVarying(256)).append(", ")
		.append("WORLD_TO_TEXTURE ").append(sqlAdapter.getCharacterVarying(1000)).append(", ")
		.append("TEXTURE_COORDINATES ").append(sqlAdapter.getPolygon2D()).append(", ")
		.append("TARGET_URI ").append(sqlAdapter.getCharacterVarying(256)).append(", ")
		.append("TEXTURE_COORDINATES_ID ").append(sqlAdapter.getInteger())
		.append(")");
		
		return builder.toString();
	}

}
