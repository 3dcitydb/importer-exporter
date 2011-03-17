package de.tub.citydb.config.project.database;

import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;

public class DBMetaData {
	private static final Logger LOG = Logger.getInstance();	
	
	private String productName;
	private String productVersion;
	private int majorVersion;
	private int minorVersion;
	private int srid;
	private String srsName;
	private Versioning versioning = Versioning.OFF;
	
	public DBMetaData() {
	}
	
	public void reset() {
		productName = null;
		productVersion = null;
		majorVersion = 0;
		minorVersion = 0;
		versioning = Versioning.OFF;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductVersion() {
		return productVersion;
	}
	
	public String getShortProductVersion() {
		return getProductVersion().replaceAll("\\n.*", "");
	}

	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	public int getSrid() {
		return srid;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public String getSrsName() {
		return srsName;
	}

	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	public Versioning getVersioning() {
		return versioning;
	}

	public void setVersioning(Versioning versioning) {
		this.versioning = versioning;
	}

	public void toConsole(LogLevelType level) {
		LOG.log(level, getShortProductVersion());
		LOG.log(level, "SRID: " + srid);
		LOG.log(level, "gml:srsName: " + srsName);
		LOG.log(level, "Versioning: " + versioning);
	}
	
	public enum Versioning {
		ON("ON"),
		OFF("OFF"),
		PARTLY("PARTLY");
		
		private final String value;
		
		private Versioning(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}

		public static Versioning fromValue(String v) {
			for (Versioning c: Versioning.values()) {
				if (c.value.equals(v)) {
					return c;
				}
			}

			return OFF;
		}
		
		public String toString() {
			return value;
		}
	}
}
