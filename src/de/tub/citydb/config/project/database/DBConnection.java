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
package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.api.database.DatabaseConnectionDetails;

@XmlType(name="ConnectionType", propOrder={
		"description",
		"server",
		"port",
		"sid",
		"user",
		"password",
		"savePassword"
		})
public class DBConnection implements DatabaseConnectionDetails, Comparable<DBConnection> {
	@XmlAttribute(required=true)
	@XmlID
	private String id;
	private String description = "";
	@XmlSchemaType(name="anyURI")
	private String server = "";
	@XmlSchemaType(name="positiveInteger")
	private Integer port = 1521;
	private String sid = "";
	private String user = "";
	private String password = "";
	private Boolean savePassword = false;
	@XmlTransient
	private String internalPassword;
		
	public DBConnection() {
		id = DefaultGMLIdManager.getInstance().generateUUID();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	@Override
	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	@Override
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isSetSavePassword() {
		if (savePassword != null)
			return savePassword.booleanValue();
		
		return false;
	}

	public Boolean getSavePassword() {
		return savePassword;
	}

	public void setSavePassword(Boolean savePassword) {
		this.savePassword = savePassword;
	}
	
	public String toString() {
		return description;
	}
	
	public String getInternalPassword() {
		return internalPassword;
	}

	public void setInternalPassword(String internalPassword) {
		this.internalPassword = internalPassword;
	}
	
	@Override
	public int compareTo(DBConnection o) {
		return description.toUpperCase().compareTo(o.getDescription().toUpperCase());
	}
	
	public String toConnectString() {
		return user + "@" + server + ":" + port + "/" + sid;
	}
	
}
