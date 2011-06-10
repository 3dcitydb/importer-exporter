/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.api.database.DatabaseSrs;

@XmlType(name="ReferenceSystemType", propOrder={
		"srid",
		"srsName",
		"description"
		})
public class ReferenceSystem implements DatabaseSrs, Comparable<ReferenceSystem> {
	public static final ReferenceSystem DEFAULT = new ReferenceSystem("", 0, "n/a", "", true);

	@XmlAttribute
	@XmlID
	private String id;
	private int srid = 0;
	private String srsName = "";
	private String description = "";
	@XmlTransient
	private boolean isSupported = true;

	public ReferenceSystem() {
		id = DefaultGMLIdManager.getInstance().generateUUID();
	}

	public ReferenceSystem(int srid, String srsName, String description, boolean isSupported) {
		this(DefaultGMLIdManager.getInstance().generateUUID(), srid, srsName, description, isSupported);
	}
	
	public ReferenceSystem(ReferenceSystem other) {
		this(other.getSrid(), other.getSrsName(), other.getDescription(), other.isSupported);
	}
	
	public ReferenceSystem(String id, int srid, String srsName, String description, boolean isSupported) {
		this.id = id;
		this.srid = srid;
		this.srsName = srsName;
		this.description = description;
		this.isSupported = isSupported;
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

	@Override
	public int getSrid() {
		return srid;
	}

	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}

	@Override
	public String getSrsName() {
		return srsName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public String toString() {
		return getDescription();
	}

	@Override
	public boolean isSupported() {
		return isSupported;
	}

	public void setSupported(boolean isSupported) {
		this.isSupported = isSupported;
	}

	@Override
	public int compareTo(ReferenceSystem o) {
		return getDescription().toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
}
