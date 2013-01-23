/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.plugin;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.gui.ImpExpGui;

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
