/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.deleter.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citydb.database.connection.DatabaseConnectionPool;

public class BundledDBConnection {
	private final List<Connection> connections;
	private final boolean useSingleConnection;
	private boolean autoCommit = false;
	private volatile boolean shouldRollback = false;
	
	public BundledDBConnection(boolean useSingleConnection) {
		this.connections = Collections.synchronizedList(new ArrayList<>());
		this.useSingleConnection = useSingleConnection;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public boolean getShouldRollback() {
		return shouldRollback;
	}

	public void setShouldRollback(boolean shouldRollback) {
		this.shouldRollback = shouldRollback;
	}

	public Connection getGlobalConnection() {
		return connections.get(0);
	}
	
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		for (Connection connection: connections) 
			connection.setAutoCommit(autoCommit);
	}

	public Connection getOrCreateConnection() throws SQLException {
		Connection connection = DatabaseConnectionPool.getInstance().getConnection();
		connection.setAutoCommit(autoCommit);
		
		if (useSingleConnection) {
			if (connections.size() == 0) 
				connections.add(connection);
			else
				return connections.get(0);				
		}
		else
			connections.add(connection);

		return connection;
	}

	public void close() throws SQLException {
		for (Connection connection: connections) {
			if (!connection.getAutoCommit()) {
				if (shouldRollback)
					connection.rollback();		
				else
					connection.commit();
			}	
			connection.close();
		}
	}

}
