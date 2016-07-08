/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.api.database;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="DatabaseSrsType", propOrder={
		"srid",
		"gmlSrsName",
		"description"
		})
public final class DatabaseSrs implements Comparable<DatabaseSrs> {	
	private static DatabaseSrs DEFAULT = new DatabaseSrs(0, "", "n/a", "n/a", DatabaseSrsType.UNKNOWN, false);
	
	@XmlAttribute
	@XmlID
	protected String id;
	protected int srid;
	protected String gmlSrsName;
	protected String description;
	@XmlTransient
	private DatabaseSrsType type;
	@XmlTransient
	private boolean isSupported;
	@XmlTransient
	protected String dbSrsName;
	
	protected DatabaseSrs() {
		this(DEFAULT);
	}
	
	public static DatabaseSrs createDefaultSrs() {
		return new DatabaseSrs();
	}
	
	public DatabaseSrs(int srid, String gmlSrsName, String description, String dbSrsName, DatabaseSrsType type, boolean isSupported) {
		this(generateUUID(), srid, gmlSrsName, description, dbSrsName, type, isSupported);
	}

	public DatabaseSrs(int srid) {
		this(srid, "", "n/a", "n/a", DatabaseSrsType.UNKNOWN, false);
	}
	
	public DatabaseSrs(DatabaseSrs other) {
		this(other.srid, other.gmlSrsName, other.description, other.dbSrsName, other.type, other.isSupported);
	}

	public DatabaseSrs(String id, int srid, String gmlSrsName, String description, String dbSrsName, DatabaseSrsType type, boolean isSupported) {
		this.id = id;
		this.srid = srid;
		this.gmlSrsName = gmlSrsName;
		this.description = description;
		this.dbSrsName = dbSrsName;
		this.type = type;
		this.isSupported = isSupported;
	}
	
	public int getSrid() {
		return srid;
	}
	
	public String getGMLSrsName() {
		return gmlSrsName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isSupported() {
		return isSupported;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSrid(int srid) {
		this.srid = srid;
	}

	public void setGMLSrsName(String srsName) {
		this.gmlSrsName = srsName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setSupported(boolean isSupported) {
		this.isSupported = isSupported;
	}

	public DatabaseSrsType getType() {
		return type;
	}

	public void setType(DatabaseSrsType type) {
		this.type = type;
	}

	public boolean is3D() {
		return type == DatabaseSrsType.COMPOUND || type == DatabaseSrsType.GEOGRAPHIC3D;
	}
	
	public String getDatabaseSrsName() {
		return dbSrsName;
	}

	public void setDatabaseSrsName(String dbSrsName) {
		this.dbSrsName = dbSrsName;
	}

	@Override
	public int compareTo(DatabaseSrs o) {
		return getDescription().toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
	@Override
	public String toString() {
		return getDescription();
	}
	
	private static String generateUUID() {
		return new StringBuilder("UUID_").append(UUID.randomUUID().toString()).toString();
	}
	
}
