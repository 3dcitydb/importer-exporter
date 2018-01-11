package org.citydb.database.connection;

public class ADEMetadata {
	private String adeId;
	private String name;
	private String description;
	private String version;
	private String dbPrefix;
	private boolean isSupported;
	
	public String getADEId() {
		return adeId;
	}

	public void setADEId(String adeId) {
		this.adeId = adeId;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDBPrefix() {
		return dbPrefix;
	}

	public void setDBPrefix(String dbPrefix) {
		this.dbPrefix = dbPrefix;
	}

	public boolean isSupported() {
		return isSupported;
	}

	public void setSupported(boolean isSupported) {
		this.isSupported = isSupported;
	}
	
	public String toString() {
		return new StringBuilder(name)
				.append(" ")
				.append(version)
				.toString();
	}
	
}
