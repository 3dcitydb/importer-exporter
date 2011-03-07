package de.tub.citydb.config.project.database;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DatabaseType", propOrder={
		"connections",
		"activeConnection",
		"updateBatching",
		"workspace"
		})
public class Database {
	@XmlElement(name="connection", required=true)
	@XmlElementWrapper(name="connections")	
	private List<DBConnection> connections;
	@XmlIDREF
	private DBConnection activeConnection;
	@XmlElement(required=true)
	private DBUpdateBatching updateBatching;
	@XmlElement(required=true)
	private DBWorkspace workspace;
	
	public Database() {
		connections = new ArrayList<DBConnection>();
		updateBatching = new DBUpdateBatching();
		workspace = new DBWorkspace();
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

	public DBUpdateBatching getUpdateBatching() {
		return updateBatching;
	}

	public void setUpdateBatching(DBUpdateBatching updateBatching) {
		if (updateBatching != null)
			this.updateBatching = updateBatching;
	}

	public DBWorkspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(DBWorkspace workspace) {
		if (workspace != null)
			this.workspace = workspace;
	}
	
}
