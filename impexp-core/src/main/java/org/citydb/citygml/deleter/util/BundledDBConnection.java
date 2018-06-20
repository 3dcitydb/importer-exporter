package org.citydb.citygml.deleter.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.database.connection.DatabaseConnectionPool;

public class BundledDBConnection {
	private final List<Connection> connections;
	private final boolean useSingleConnection;
	private boolean autoCommit = false;
	private volatile boolean shouldRollback = false;
	
	public BundledDBConnection(boolean useSingleConnection) {
		this.connections = new ArrayList<Connection>();
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
