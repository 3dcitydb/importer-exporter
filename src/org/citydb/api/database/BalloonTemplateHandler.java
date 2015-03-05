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
package org.citydb.api.database;

import java.util.HashMap;
import java.util.Set;

public interface BalloonTemplateHandler {

	// Constants
	public static final String START_TAG = "<3DCityDB>";
	public static final String END_TAG = "</3DCityDB>";
	public static final String FOREACH_TAG = "FOREACH";
	public static final String END_FOREACH_TAG = "END FOREACH";

	public String getBalloonContent(String gmlId, int lod) throws Exception;	
	public Set<String> getSupportedAggregationFunctions();
	public HashMap<String, Set<String>> getSupportedTablesAndColumns();		
}
