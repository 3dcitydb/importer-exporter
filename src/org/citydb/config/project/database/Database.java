/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.config.project.database;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseSrsType;

@XmlType(name="DatabaseType", propOrder={
		"referenceSystems",
		"connections",
		"activeConnection",
		"updateBatching",
		"workspaces",
		"operation"
})
public class Database {
	public static final int MAX_BATCH_SIZE = 65535;
	public static final EnumMap<PredefinedSrsName, DatabaseSrs> PREDEFINED_SRS = new EnumMap<PredefinedSrsName, DatabaseSrs>(PredefinedSrsName.class);

	public enum PredefinedSrsName {
		WGS84_2D
	}
	
	static {
		PREDEFINED_SRS.put(PredefinedSrsName.WGS84_2D, new DatabaseSrs(4326, "urn:ogc:def:crs:EPSG::4326", "[Default] WGS 84", "", DatabaseSrsType.GEOGRAPHIC2D, true));
	}
	
	private DatabaseSrsList referenceSystems;
	@XmlElement(name="connection", required=true)
	@XmlElementWrapper(name="connections")	
	private List<DBConnection> connections;
	@XmlIDREF
	private DBConnection activeConnection;
	@XmlElement
	private UpdateBatching updateBatching;
	@XmlElement
	private Workspaces workspaces;
	private DBOperation operation;

	public Database() {
		referenceSystems = new DatabaseSrsList();
		connections = new ArrayList<DBConnection>();
		updateBatching = new UpdateBatching();
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

	public UpdateBatching getUpdateBatching() {
		return updateBatching;
	}

	public void setUpdateBatching(UpdateBatching updateBatching) {
		if (updateBatching != null)
			this.updateBatching = updateBatching;
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
