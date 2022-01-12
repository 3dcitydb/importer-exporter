/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.database.adapter.h2;

import org.citydb.config.project.database.DatabaseType;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;

public class H2Adapter extends AbstractDatabaseAdapter {
	// NOTE: this adapter is currently only used for cache tables
	
	public H2Adapter() {
		geometryAdapter = new GeometryConverterAdapter(this);
		sqlAdapter = new SQLAdapter(this);
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
		return "jdbc:h2:" + server;
	}

	@Override
	public int getMaxBatchSize() {
		return 65535;
	}

}
