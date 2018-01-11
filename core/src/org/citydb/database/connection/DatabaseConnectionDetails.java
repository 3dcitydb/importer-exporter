package org.citydb.database.connection;

import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseType;

public class DatabaseConnectionDetails {
	private String description;
	private DatabaseType type;
	private String server;
	private int port;
	private String sid;
	private String schema;
	private String user;
	
	public DatabaseConnectionDetails(DBConnection connection) {
		description = connection.getDescription();
		type = connection.getDatabaseType();
		server = connection.getServer();
		port = connection.getPort() != null ? connection.getPort() : 0;
		sid = connection.getSid();
		schema = connection.getSchema();
		user = connection.getUser();
	}
	
	public String getDescription() {
		return description;
	}
	
	protected void setDescription(String description) {
		this.description = description;
	}
	
	public DatabaseType getDatabaseType() {
		return type;
	}
	
	protected void setType(DatabaseType type) {
		this.type = type;
	}
	
	public String getServer() {
		return server;
	}
	
	protected void setServer(String server) {
		this.server = server;
	}
	
	public int getPort() {
		return port;
	}
	
	protected void setPort(int port) {
		this.port = port;
	}
	
	public String getSid() {
		return sid;
	}
	
	protected void setSid(String sid) {
		this.sid = sid;
	}
	
	public String getSchema() {
		return schema;
	}
	
	protected boolean isSetSchema() {
		return schema != null && !schema.trim().isEmpty();
	}

	protected void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getUser() {
		return user;
	}
	
	protected void setUser(String user) {
		this.user = user;
	}
	
	public String toConnectString() {
		return user + "@" + server + ":" + port + "/" + sid;
	}
	
}
