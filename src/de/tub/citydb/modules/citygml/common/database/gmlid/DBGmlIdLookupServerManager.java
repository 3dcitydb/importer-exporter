/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.common.database.gmlid;

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
		int concurrencyLevel) {

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
		case ABSTRACT_GML_GEOMETRY:
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
