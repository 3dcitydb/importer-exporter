package de.tub.citydb.plugin.api.data.database;


public class DatabaseMetaData {
	// database related information
	private String databaseProductName;
	private String databaseProductString;
	private int databaseMajorVersion;
	private int databaseMinorVersion;
	
	// 3DCityDB related information
	private String referenceSystemName;
	private boolean isReferenceSystem3D;
	private int srid;
	private String srsName;
	private boolean versionEnabled;
	
	public DatabaseMetaData(
			String databaseProductName,
			String databaseProductString,
			int databaseMajorVersion,
			int databaseMinorVersion,
			String referenceSystemName,
			boolean isReferenceSystem3D,
			int srid,
			String srsName,
			boolean versionEnabled) {
		this.databaseProductName = databaseProductName;
		this.databaseProductString = databaseProductString;
		this.databaseMajorVersion = databaseMajorVersion;
		this.databaseMinorVersion = databaseMinorVersion;
		this.referenceSystemName = referenceSystemName;
		this.isReferenceSystem3D = isReferenceSystem3D;
		this.srid = srid;
		this.versionEnabled = versionEnabled;
	}
	
	public String getDatabaseProductName() {
		return databaseProductName;
	}

	public String getDatabaseProductVersion() {
		return databaseProductString;
	}
	
	public int getDatabaseMajorVersion() {
		return databaseMajorVersion;
	}

	public int getDatabaseMinorVersion() {
		return databaseMinorVersion;
	}

	public String getDatabaseProductString() {
		return databaseProductString;
	}

	public String getReferenceSystemName() {
		return referenceSystemName;
	}

	public boolean isReferenceSystem3D() {
		return isReferenceSystem3D;
	}

	public int getSrid() {
		return srid;
	}

	public String getSrsName() {
		return srsName;
	}

	public boolean isVersionEnabled() {
		return versionEnabled;
	}
	
}
