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
package org.citydb.api.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.citydb.api.database.DatabaseAdapter;
import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseConnectionDetails;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseVersionChecker;
import org.citydb.api.database.DatabaseVersionException;

public interface DatabaseController {
	public void connect(boolean showErrorDialog) throws DatabaseConfigurationException, DatabaseVersionException, SQLException;
	public void disconnect();
	public boolean isConnected();

	public Connection getConnection() throws SQLException;
	public List<DatabaseConnectionDetails> getConnectionDetails();
	public List<DatabaseSrs> getDatabaseSrs();
	
	public DatabaseAdapter getActiveDatabaseAdapter();
	public DatabaseVersionChecker getDatabaseVersionChecker();
}
