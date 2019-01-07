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

import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseType;

public class DatabaseConnectionDetails {
	private String description;
	private DatabaseType type;
	private String server;
	private int port;
	private String sid;
	private String schema;
	private String user;
	
	public DatabaseConnectionDetails(DBConnection connection) {
		description = connection.getDescription();
		type = connection.getDatabaseType();
		server = connection.getServer();
		port = connection.getPort() != null ? connection.getPort() : 0;
		sid = connection.getSid();
		schema = connection.getSchema();
		user = connection.getUser();
	}
	
	public String getDescription() {
		return description;
	}
	
	protected void setDescription(String description) {
		this.description = description;
	}
	
	public DatabaseType getDatabaseType() {
		return type;
	}
	
	protected void setType(DatabaseType type) {
		this.type = type;
	}
	
	public String getServer() {
		return server;
	}
	
	protected void setServer(String server) {
		this.server = server;
	}
	
	public int getPort() {
		return port;
	}
	
	protected void setPort(int port) {
		this.port = port;
	}
	
	public String getSid() {
		return sid;
	}
	
	protected void setSid(String sid) {
		this.sid = sid;
	}
	
	public String getSchema() {
		return schema;
	}
	
	protected boolean isSetSchema() {
		return schema != null && !schema.trim().isEmpty();
	}

	protected void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getUser() {
		return user;
	}
	
	protected void setUser(String user) {
		this.user = user;
	}
	
	public String toConnectString() {
		return user + "@" + server + ":" + port + "/" + sid;
	}
	
}
