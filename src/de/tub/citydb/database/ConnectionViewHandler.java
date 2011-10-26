package de.tub.citydb.database;

import java.sql.SQLException;

import de.tub.citydb.api.database.DatabaseConfigurationException;

public interface ConnectionViewHandler {
	public void commitConnectionDetails();
	public void printConnectionState(ConnectionStateEnum state);
	public void printError(ConnectionStateEnum error, DatabaseConfigurationException e, boolean showErrorDialog);
	public void printError(ConnectionStateEnum error, SQLException e, boolean showErrorDialog);
}
