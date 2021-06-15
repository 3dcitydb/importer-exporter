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
package org.citydb.core.operation.deleter.database;

import org.citydb.core.database.connection.ConnectionManager;
import org.citydb.core.database.connection.DatabaseConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BundledConnection implements ConnectionManager {
	private final ReentrantLock lock = new ReentrantLock();
	private final List<Connection> connections;
	private final DatabaseConnectionPool connectionPool;

	private boolean singleConnection;
	private boolean autoCommit;
	private volatile boolean shouldRollback = false;
	
	public BundledConnection() {
		connections = Collections.synchronizedList(new ArrayList<>());
		connectionPool = DatabaseConnectionPool.getInstance();
	}

	public boolean isSingleConnection() {
		return singleConnection;
	}

	public BundledConnection withSingleConnection(boolean singleConnection) {
		this.singleConnection = singleConnection;
		return this;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public BundledConnection withAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
		return this;
	}

	public boolean isShouldRollback() {
		return shouldRollback;
	}

	public BundledConnection setShouldRollback(boolean shouldRollback) {
		this.shouldRollback = shouldRollback;
		return this;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (singleConnection) {
			lock.lock();
			try {
				return connections.isEmpty() ? newConnection() : connections.get(0);
			} finally {
				lock.unlock();
			}
		} else {
			return newConnection();
		}
	}

	private Connection newConnection() throws SQLException {
		Connection connection = connectionPool.getConnection();
		connection.setAutoCommit(autoCommit);
		connections.add(connection);
		return connection;
	}

	public void close() throws SQLException {
		try {
			for (Connection connection : connections) {
				if (!autoCommit && shouldRollback) {
					connection.rollback();
				} else {
					connection.commit();
				}

				connection.close();
			}
		} finally {
			connections.clear();
		}
	}

}
