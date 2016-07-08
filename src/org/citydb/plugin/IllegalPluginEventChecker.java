/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.plugin;

import org.citydb.api.event.Event;
import org.citydb.api.event.EventHandler;
import org.citydb.api.event.global.GlobalEvents;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.gui.ImpExpGui;

public class IllegalPluginEventChecker implements EventHandler {
	private static IllegalPluginEventChecker instance;
	
	private IllegalPluginEventChecker() {
		// just to thwart instantiation
	}
	
	public static synchronized IllegalPluginEventChecker getInstance() {
		if (instance == null)
			instance = new IllegalPluginEventChecker();
		
		return instance;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getEventType() == GlobalEvents.DATABASE_CONNECTION_STATE && event.getSource() != DatabaseConnectionPool.getInstance())
			throw new IllegalArgumentException("Events of type " + GlobalEvents.DATABASE_CONNECTION_STATE + " may not be triggered by plugins.");

		else if (event.getEventType() == GlobalEvents.SWITCH_LOCALE && !(event.getSource() instanceof ImpExpGui))
			throw new IllegalArgumentException("Events of type " + GlobalEvents.SWITCH_LOCALE + " may not be triggered by plugins.");
	}
	
}
