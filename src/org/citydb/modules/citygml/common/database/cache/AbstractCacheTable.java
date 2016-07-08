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
package org.citydb.modules.citygml.common.database.cache;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;

public abstract class AbstractCacheTable {
	protected static long ID;
	protected final Connection connection;
	protected final AbstractSQLAdapter sqlAdapter;
	
	protected AbstractCacheTable(Connection connection, AbstractSQLAdapter sqlAdapter) {
		ID++;
		this.connection = connection;
		this.sqlAdapter = sqlAdapter;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public abstract CacheTableModelEnum getModelType();
	public abstract boolean isCreated();
	protected abstract void create() throws SQLException;
	protected abstract void createAndIndex() throws SQLException;
	protected abstract void drop() throws SQLException;	
}
