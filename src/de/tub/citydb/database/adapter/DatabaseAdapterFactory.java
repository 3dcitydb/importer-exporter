package de.tub.citydb.database.adapter;

import de.tub.citydb.api.database.DatabaseType;
import de.tub.citydb.database.adapter.oracle.OracleAdapter;
import de.tub.citydb.database.adapter.postgis.PostGISAdapter;

public class DatabaseAdapterFactory {
	private static DatabaseAdapterFactory instance;
	
	public static synchronized DatabaseAdapterFactory getInstance() {
		if (instance == null)
			instance = new DatabaseAdapterFactory();
			
		return instance;
	}
	
	private DatabaseAdapterFactory() {
		// just to thwart instantiation
	}
	
	public AbstractDatabaseAdapter createDatabaseAdapter(DatabaseType databaseType) {
		switch (databaseType) {
		case ORACLE:
			return new OracleAdapter();
		case POSTGIS:
			return new PostGISAdapter();
		}
		
		return null;
	}
}
