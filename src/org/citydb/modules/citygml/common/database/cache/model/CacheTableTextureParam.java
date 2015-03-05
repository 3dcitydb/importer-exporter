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

import org.citydb.database.adapter.AbstractSQLAdapter;


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
		.append("WORLD_TO_TEXTURE ").append(sqlAdapter.getCharacterVarying(1000))
		.append(")");
		
		return builder.toString();
	}

}
