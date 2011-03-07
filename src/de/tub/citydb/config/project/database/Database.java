package de.tub.citydb.config.project.database;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DatabaseType", propOrder={
		"referenceSystems",
		"connections",
		"activeConnection",
		"updateBatching",
		"workspaces",
		"operation"
})
public class Database {
	private ReferenceSystems referenceSystems;
	@XmlElement(name="connection", required=true)
	@XmlElementWrapper(name="connections")	
	private List<DBConnection> connections;
	@XmlIDREF
	private DBConnection activeConnection;
	@XmlElement(required=true)
	private UpdateBatching updateBatching;
	@XmlElement(required=true)
	private Workspaces workspaces;
	private DBOperation operation;
	
	public Database() {
		referenceSystems = new ReferenceSystems();
		connections = new ArrayList<DBConnection>();
		updateBatching = new UpdateBatching();
		workspaces = new Workspaces();
		operation = new DBOperation();
	}

	public List<ReferenceSystem> getReferenceSystems() {
		return referenceSystems.getItems();
	}

	public void setReferenceSystems(List<ReferenceSystem> referenceSystems) {
		if (referenceSystems != null)
			this.referenceSystems.setItems(referenceSystems);
	}

	public void addReferenceSystem(ReferenceSystem referenceSystem) {
		referenceSystems.addItem(referenceSystem);
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
		return activeConnection;
	}

	public void setActiveConnection(DBConnection activeConnection) {
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
