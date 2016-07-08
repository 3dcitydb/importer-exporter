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
