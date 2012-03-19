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
package de.tub.citydb.api.database;

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
