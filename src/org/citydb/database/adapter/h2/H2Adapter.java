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
package org.citydb.database.adapter.h2;

import org.citydb.api.database.DatabaseType;
import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class H2Adapter extends AbstractDatabaseAdapter {
	// NOTE: this adapter is currently only used for cache tables
	
	public H2Adapter() {
		geometryAdapter = new GeometryConverterAdapter();
		sqlAdapter = new SQLAdapter();
	}
	
	@Override
	public DatabaseType getDatabaseType() {
		return null;
	}

	@Override
	public boolean hasVersioningSupport() {
		return false;
	}
	
	@Override
	public boolean hasTableStatsSupport() {
		return false;
	}

	@Override
	public int getDefaultPort() {
		return -1;
	}

	@Override
	public String getConnectionFactoryClassName() {
		return "org.h2.Driver";
	}

	@Override
	public String getJDBCUrl(String server, int port, String database) {
		return "jdbc:h2:" + server + ";MULTI_THREADED=TRUE;LOG=0;LOCK_MODE=3;UNDO_LOG=0;MV_STORE=FALSE";
	}

	@Override
	public int getMaxBatchSize() {
		return 65535;
	}

}
