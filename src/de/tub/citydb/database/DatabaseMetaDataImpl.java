/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.database;

import de.tub.citydb.api.database.DatabaseMetaData;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.log.Logger;

public class DatabaseMetaDataImpl implements DatabaseMetaData {
	private static final Logger LOG = Logger.getInstance();	
	
	// database related information
	private String databaseProductName;
	private String databaseProductString;
	private int databaseMajorVersion;
	private int databaseMinorVersion;
	
	// 3DCityDB related information
	private DatabaseSrs srs =  DatabaseSrs.createDefaultSrs();
	private Versioning versioning = Versioning.OFF;
	
	public void reset() {
		databaseProductName = null;
		databaseProductString = null;
		databaseMajorVersion = 0;
		databaseMinorVersion = 0;
		srs = DatabaseSrs.createDefaultSrs();
		versioning = Versioning.OFF;
	}

	@Override
	public String getDatabaseProductName() {
		return databaseProductName;
	}

	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

	@Override
	public String getDatabaseProductVersion() {
		return databaseProductString;
	}
	
	@Override
	public String getShortDatabaseProductVersion() {
		return getDatabaseProductVersion().replaceAll("\\n.*", "");
	}

	public void setDatabaseProductVersion(String databaseProductString) {
		this.databaseProductString = databaseProductString;
	}

	@Override
	public int getDatabaseMajorVersion() {
		return databaseMajorVersion;
	}

	public void setDatabaseMajorVersion(int databaseMajorVersion) {
		this.databaseMajorVersion = databaseMajorVersion;
	}

	@Override
	public int getDatabaseMinorVersion() {
		return databaseMinorVersion;
	}

	public void setDatabaseMinorVersion(int databaseMinorVersion) {
		this.databaseMinorVersion = databaseMinorVersion;
	}

	@Override
	public String getDatabaseProductString() {
		return databaseProductString;
	}

	public void setDatabaseProductString(String databaseProductString) {
		this.databaseProductString = databaseProductString;
	}

	public DatabaseSrs getReferenceSystem() {
		return srs;
	}

	public void setReferenceSystem(DatabaseSrs srs) {
		this.srs = srs;
	}

	@Override
	public boolean isVersionEnabled() {
		return versioning == Versioning.ON;
	}

	public Versioning getVersioning() {
		return versioning;
	}

	public void setVersioning(Versioning versioning) {
		this.versioning = versioning;
	}

	@Override
	public void printToConsole() {
		LOG.all(LogLevel.INFO, getShortDatabaseProductVersion());
		LOG.all(LogLevel.INFO, "SRID: " + srs.getSrid() + " (" + srs.getType() + ')');
		LOG.all(LogLevel.INFO, "SRS: " + srs.getDatabaseSrsName());
		LOG.all(LogLevel.INFO, "gml:srsName: " + srs.getGMLSrsName());
		LOG.all(LogLevel.INFO, "Versioning: " + versioning);
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
