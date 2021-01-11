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

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.config.project.database.Workspace;

public class DatabaseConnectionDetails {
	private String description;
	private DatabaseType type;
	private String server;
	private int port;
	private String sid;
	private String schema;
	private Workspace workspace;
	private String user;
	
	public DatabaseConnectionDetails(DatabaseConnection connection) {
		description = connection.getDescription();
		type = connection.getDatabaseType();
		server = connection.getServer();
		port = connection.getPort() != null ? connection.getPort() : 0;
		sid = connection.getSid();
		schema = connection.getSchema();
		user = connection.getUser();

		if (connection.isSetWorkspace()) {
			workspace = new Workspace(connection.getWorkspace());
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	void setDescription(String description) {
		this.description = description;
	}
	
	public DatabaseType getDatabaseType() {
		return type;
	}
	
	void setType(DatabaseType type) {
		this.type = type;
	}
	
	public String getServer() {
		return server;
	}
	
	void setServer(String server) {
		this.server = server;
	}
	
	public int getPort() {
		return port;
	}
	
	void setPort(int port) {
		this.port = port;
	}
	
	public String getSid() {
		return sid;
	}
	
	void setSid(String sid) {
		this.sid = sid;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public boolean isSetSchema() {
		return schema != null && !schema.trim().isEmpty();
	}

	void setSchema(String schema) {
		this.schema = schema;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public boolean isSetWorkspace() {
		return workspace != null && workspace.isSetName();
	}

	void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public String getUser() {
		return user;
	}
	
	void setUser(String user) {
		this.user = user;
	}
	
	public String toConnectString() {
		return user + "@" + server + ":" + port + "/" + sid;
	}
	
}
