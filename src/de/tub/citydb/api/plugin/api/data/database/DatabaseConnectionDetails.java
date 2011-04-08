package de.tub.citydb.api.plugin.api.data.database;

public class DatabaseConnectionDetails {
	private String description;
	private String server;
	private int port;
	private String serviceName;
	private String user;
	private DatabaseMetaData metaData;
	
	public DatabaseConnectionDetails(
			String description,
			String server,
			int port,
			String serviceName,
			String user,
			DatabaseMetaData metaData) {
		this.description = description;
		this.server = server;
		this.port = port;
		this.serviceName = serviceName;
		this.user = user;
		this.metaData = metaData;
	}
	
	public String getDescription() {
		return description;
	}

	public String getServer() {
		return server;
	}

	public Integer getPort() {
		return port;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getUser() {
		return user;
	}

	public DatabaseMetaData getMetaData() {
		return metaData;
	}
}
