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
