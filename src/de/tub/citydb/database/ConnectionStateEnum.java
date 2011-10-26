package de.tub.citydb.database;

public enum ConnectionStateEnum {
	INIT_CONNECT,
	FINISH_CONNECT,
	INIT_DISCONNECT,
	FINISH_DISCONNECT,
	CONNECT_ERROR,
	DISCONNECT_ERROR
}
