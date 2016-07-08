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
package org.citydb.api.registry;

import java.util.concurrent.ConcurrentHashMap;

import org.citydb.api.controller.DatabaseController;
import org.citydb.api.controller.IOController;
import org.citydb.api.controller.LogController;
import org.citydb.api.controller.PluginConfigController;
import org.citydb.api.controller.ViewController;
import org.citydb.api.event.EventDispatcher;
import org.citygml4j.builder.CityGMLBuilder;

public class ObjectRegistry {
	private static ObjectRegistry instance;

	private ConcurrentHashMap<String, Object> registry;
	private EventDispatcher eventDispatcher;
	private ViewController viewController;
	private DatabaseController databaseController;
	private LogController logController;
	private PluginConfigController pluginConfigController;
	private IOController ioController;
	private CityGMLBuilder cityGMLBuilder;

	private ObjectRegistry() {
		// just to thwart instantiation
	}

	public static synchronized ObjectRegistry getInstance() {
		if (instance == null)
			instance = new ObjectRegistry();
		
		return instance;
	}

	public void register(String name, Object object) {
		if (registry == null)
			registry = new ConcurrentHashMap<String, Object>();

		registry.put(name, object);
	}

	public void unregister(String name) {
		registry.remove(name);
	}

	public Object lookup(String name) {
		return registry.get(name);
	}

	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		if (this.eventDispatcher != null)
			throw new IllegalArgumentException("Event dispatcher is already registered with the object registry.");

		this.eventDispatcher = eventDispatcher;
	}

	public ViewController getViewController() {
		return viewController;
	}

	public void setViewController(ViewController viewController) {
		if (this.viewController != null)
			throw new IllegalArgumentException("View controller is already registered with the object registry.");

		this.viewController = viewController;
	}

	public DatabaseController getDatabaseController() {
		return databaseController;
	}

	public void setDatabaseController(DatabaseController databaseController) {
		if (this.databaseController != null)
			throw new IllegalArgumentException("Database controller is already registered with the object registry.");

		this.databaseController = databaseController;
	}

	public LogController getLogController() {
		return logController;
	}

	public void setLogController(LogController logController) {
		if (this.logController != null)
			throw new IllegalArgumentException("Log controller is already registered with the object registry.");

		this.logController = logController;
	}

	public PluginConfigController getPluginConfigController() {
		return pluginConfigController;
	}

	public void setPluginConfigController(PluginConfigController pluginConfigController) {
		if (this.pluginConfigController != null)
			throw new IllegalArgumentException("Plugin config controller is already registered with the object registry.");

		this.pluginConfigController = pluginConfigController;
	}

	public IOController getIOController() {
		return ioController;
	}

	public void setIOController(IOController ioController) {
		if (this.ioController != null)
			throw new IllegalArgumentException("I/O controller is already registered with the object registry.");

		this.ioController = ioController;
	}

	public CityGMLBuilder getCityGMLBuilder() {
		return cityGMLBuilder;
	}

	public void setCityGMLBuilder(CityGMLBuilder cityGMLBuilder) {
		if (this.cityGMLBuilder != null)
			throw new IllegalArgumentException("CityGML Builder is already registered with the object registry.");

		this.cityGMLBuilder = cityGMLBuilder;
	}
	
	public void cleanup() {
		if (registry != null)
			registry.clear();
		
		eventDispatcher = null;
		viewController = null;
		databaseController = null;
		logController = null;
		pluginConfigController = null;
		ioController = null;
		cityGMLBuilder = null;
	}

}
