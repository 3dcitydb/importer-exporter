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
package org.citydb.config.project.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@XmlType(name = "DatabaseType", propOrder = {
		"referenceSystems",
		"connections",
		"activeConnection",
		"importBatching",
		"exportBatching",
		"workspaces",
		"operation"
})
public class Database {
	public static final String CITYDB_PRODUCT_NAME = "3D City Database";
	public static final EnumMap<PredefinedSrsName, DatabaseSrs> PREDEFINED_SRS = new EnumMap<>(PredefinedSrsName.class);

	public enum PredefinedSrsName {
		WGS84_2D
	}

	static {
		PREDEFINED_SRS.put(PredefinedSrsName.WGS84_2D, new DatabaseSrs(4326, "urn:ogc:def:crs:EPSG::4326", "[Default] WGS 84", "", DatabaseSrsType.GEOGRAPHIC2D, true));
	}

	private final DatabaseSrsList referenceSystems;
	@XmlElement(name = "connection", required = true)
	@XmlElementWrapper(name = "connections")
	private List<DBConnection> connections;
	@XmlIDREF
	private DBConnection activeConnection;
	private ImportBatching importBatching;
	private ExportBatching exportBatching;
	private Workspaces workspaces;
	private DBOperation operation;

	public Database() {
		referenceSystems = new DatabaseSrsList();
		connections = new ArrayList<>();
		importBatching = new ImportBatching();
		exportBatching = new ExportBatching();
		workspaces = new Workspaces();
		operation = new DBOperation();
	}

	public List<DatabaseSrs> getReferenceSystems() {
		return referenceSystems.getItems();
	}

	public void setReferenceSystems(List<DatabaseSrs> referenceSystems) {
		if (referenceSystems != null)
			this.referenceSystems.setItems(referenceSystems);
	}

	public void addReferenceSystem(DatabaseSrs referenceSystem) {
		referenceSystems.addItem(referenceSystem);
	}

	public void addDefaultReferenceSystems() {
		referenceSystems.addDefaultItems();
	}

	public List<DBConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<DBConnection> connections) {
		if (connections != null)
			this.connections = connections;
	}

	public void addConnection(DBConnection connection) {
		connections.add(connection);
	}

	public DBConnection getActiveConnection() {
		if (activeConnection == null && !connections.isEmpty())
			activeConnection = connections.get(0);

		return activeConnection;
	}

	public void setActiveConnection(DBConnection activeConnection) {
		if (activeConnection != null)
			this.activeConnection = activeConnection;
	}

	public ImportBatching getImportBatching() {
		return importBatching;
	}

	public void setImportBatching(ImportBatching importBatching) {
		if (importBatching != null)
			this.importBatching = importBatching;
	}

	public ExportBatching getExportBatching() {
		return exportBatching;
	}

	public void setExportBatching(ExportBatching exportBatching) {
		if (exportBatching != null)
			this.exportBatching = exportBatching;
	}

	public Workspaces getWorkspaces() {
		return workspaces;
	}

	public void setWorkspaces(Workspaces workspaces) {
		if (workspaces != null)
			this.workspaces = workspaces;
	}
	
	public DBOperation getOperation() {
		return operation;
	}

	public void setOperation(DBOperation operation) {
		if (operation != null)
			this.operation = operation;
	}

}
