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
package org.citydb.database.connection;

import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.version.DatabaseVersion;
import org.citydb.log.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseMetaData {
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionDetails connectionDetails;

	// database related information
	private DatabaseVersion cityDBVersion;
	private String databaseProductName;
	private String databaseProductString;
	private int databaseMajorVersion;
	private int databaseMinorVersion;

	// 3DCityDB related information
	private DatabaseSrs srs =  DatabaseSrs.createDefaultSrs();
	private Versioning versioning = Versioning.OFF;

	// ADE metadata
	private List<ADEMetadata> ades;

	public DatabaseMetaData(DatabaseConnectionDetails connectionDetails) {
		this.connectionDetails = connectionDetails;
	}
	
	public void reset() {
		databaseProductName = null;
		databaseProductString = null;
		databaseMajorVersion = 0;
		databaseMinorVersion = 0;
		srs = DatabaseSrs.createDefaultSrs();
		versioning = Versioning.OFF;
		ades = null;
	}

	public DatabaseVersion getCityDBVersion() {
		return cityDBVersion;
	}
	
	public void setCityDBVersion(DatabaseVersion cityDBVersion) {
		this.cityDBVersion = cityDBVersion;
	}

	public String getDatabaseProductName() {
		return databaseProductName;
	}

	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}

	public String getDatabaseProductVersion() {
		return databaseProductString;
	}
	
	public String getShortDatabaseProductVersion() {
		return getDatabaseProductVersion().replaceAll("\\n.*", "");
	}

	public void setDatabaseProductVersion(String databaseProductString) {
		this.databaseProductString = databaseProductString;
	}

	public int getDatabaseMajorVersion() {
		return databaseMajorVersion;
	}

	public void setDatabaseMajorVersion(int databaseMajorVersion) {
		this.databaseMajorVersion = databaseMajorVersion;
	}

	public int getDatabaseMinorVersion() {
		return databaseMinorVersion;
	}

	public void setDatabaseMinorVersion(int databaseMinorVersion) {
		this.databaseMinorVersion = databaseMinorVersion;
	}

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

	public boolean isVersionEnabled() {
		return versioning == Versioning.ON;
	}

	public Versioning getVersioning() {
		return versioning;
	}

	public void setVersioning(Versioning versioning) {
		this.versioning = versioning;
	}

	public List<ADEMetadata> getRegisteredADEs() {
		return ades != null ? new ArrayList<>(ades) : Collections.emptyList();
	}

	public boolean hasRegisteredADEs() {
		return ades != null && !ades.isEmpty();
	}
	
	public void setRegisteredADEs(List<ADEMetadata> ades) {
		this.ades = ades;
	}

	public void printToConsole() {
        log.info("3D City Database: " + getCityDBVersion());
		log.info("DBMS: " + getDatabaseProductName() + " " + getDatabaseProductVersion());
		log.info("Connection: " + connectionDetails.toConnectString());
		log.info("Schema: " + connectionDetails.getSchema());
		log.info("SRID: " + srs.getSrid() + " (" + srs.getType() + ')');
		log.info("SRS: " + srs.getDatabaseSrsName());
		log.info("gml:srsName: " + srs.getGMLSrsName());
		log.info("Versioning: " + versioning);

		if (hasRegisteredADEs()) {
			for (ADEMetadata ade : ades) {
				if (ade.isSupported())
					log.info("CityGML ADE: " + ade);
			}
		}
	}
	
	public enum Versioning {
		ON("On"),
		OFF("Off"),
		PARTLY("Partly"),
		NOT_SUPPORTED("Not supported");
		
		private final String value;
		
		private Versioning(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}

		public static Versioning fromValue(String v) {
			for (Versioning c: Versioning.values()) {
				if (c.value.toLowerCase().equals(v.toLowerCase())) {
					return c;
				}
			}

			return NOT_SUPPORTED;
		}
		
		public String toString() {
			return value;
		}
	}
}
