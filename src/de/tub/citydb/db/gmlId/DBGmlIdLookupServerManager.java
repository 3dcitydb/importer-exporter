package de.tub.citydb.db.gmlId;

import java.sql.SQLException;
import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;

public class DBGmlIdLookupServerManager {
	private final HashMap<DBGmlIdLookupServerEnum, GmlIdLookupServer> serverMap;

	public DBGmlIdLookupServerManager() {
		serverMap = new HashMap<DBGmlIdLookupServerEnum, GmlIdLookupServer>();
	}

	public void initServer(
		DBGmlIdLookupServerEnum serverType,
		DBCacheModel model,
		int cacheSize,
		float drainFactor,
		int concurrencyLevel) throws SQLException {

		serverMap.put(serverType, new GmlIdLookupServer(
				model,
				cacheSize,
				drainFactor,
				concurrencyLevel
		));
	}

	public GmlIdLookupServer getLookupServer(CityGMLClass type) {
		DBGmlIdLookupServerEnum lookupServer;

		switch (type) {
		case GMLGEOMETRY:
			lookupServer = DBGmlIdLookupServerEnum.GEOMETRY;
			break;
		default:
			lookupServer = DBGmlIdLookupServerEnum.FEATURE;
		}

		return serverMap.get(lookupServer);
	}
	
	public void shutdownAll() throws SQLException {
		for (GmlIdLookupServer server : serverMap.values())
			server.shutdown();
	}
}
