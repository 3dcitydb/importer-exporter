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
package org.citydb.citygml.deleter.database;

import org.citydb.database.connection.ConnectionManager;
import org.citydb.database.connection.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundledConnection implements ConnectionManager {
	private final List<Connection> connections;
	private final DatabaseConnectionPool connectionPool;
	private final boolean autoCommit;

	private volatile boolean shouldRollback = false;
	
	public BundledConnection(boolean autoCommit) {
		this.autoCommit = autoCommit;
		connections = Collections.synchronizedList(new ArrayList<>());
		connectionPool = DatabaseConnectionPool.getInstance();
	}

	public BundledConnection() {
		this(false);
	}

	public boolean isShouldRollback() {
		return shouldRollback;
	}

	public void setShouldRollback(boolean shouldRollback) {
		this.shouldRollback = shouldRollback;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = connectionPool.getConnection();
		connection.setAutoCommit(autoCommit);
		connections.add(connection);
		return connection;
	}

	public void close() throws SQLException {
		try {
			for (Connection connection : connections) {
				if (!autoCommit && shouldRollback)
					connection.rollback();
				else
					connection.commit();

				connection.close();
			}
		} finally {
			connections.clear();
		}
	}

}
