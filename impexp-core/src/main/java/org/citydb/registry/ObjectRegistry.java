/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.registry;

import java.util.concurrent.ConcurrentHashMap;

import org.citydb.database.DatabaseController;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citygml4j.builder.jaxb.CityGMLBuilder;

public class ObjectRegistry {
	private static ObjectRegistry instance;

	private ConcurrentHashMap<String, Object> properties;
	private EventDispatcher eventDispatcher;
	private DatabaseController databaseController;
	private CityGMLBuilder cityGMLBuilder;
	private SchemaMapping schemaMapping;
	
	private ObjectRegistry() {
		// just to thwart instantiation
	}

	public static synchronized ObjectRegistry getInstance() {
		if (instance == null)
			instance = new ObjectRegistry();
		
		return instance;
	}

	public void register(String name, Object object) {
		if (properties == null)
			properties = new ConcurrentHashMap<>();

		properties.put(name, object);
	}

	public void unregister(String name) {
		properties.remove(name);
	}

	public Object lookup(String name) {
		return properties != null ? properties.get(name) : null;
	}

	public EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public void setEventDispatcher(EventDispatcher eventDispatcher) {
		if (this.eventDispatcher != null)
			throw new IllegalArgumentException("Event dispatcher is already registered with the object registry.");

		this.eventDispatcher = eventDispatcher;
	}

	public DatabaseController getDatabaseController() {
		return databaseController;
	}

	public void setDatabaseController(DatabaseController databaseController) {
		if (this.databaseController != null)
			throw new IllegalArgumentException("Database controller is already registered with the object registry.");

		this.databaseController = databaseController;
	}

	public CityGMLBuilder getCityGMLBuilder() {
		return cityGMLBuilder;
	}

	public void setCityGMLBuilder(CityGMLBuilder cityGMLBuilder) {
		if (this.cityGMLBuilder != null)
			throw new IllegalArgumentException("CityGML Builder is already registered with the object registry.");

		this.cityGMLBuilder = cityGMLBuilder;
	}

	public SchemaMapping getSchemaMapping() {
		return schemaMapping;
	}

	public void setSchemaMapping(SchemaMapping schemaMapping) {
		if (this.schemaMapping != null)
			throw new IllegalArgumentException("Schema mapping is already registered with the object registry.");

		this.schemaMapping = schemaMapping;
	}
	
	public void cleanup() {
		if (properties != null)
			properties.clear();
		
		eventDispatcher = null;
		databaseController = null;
		cityGMLBuilder = null;
		schemaMapping = null;
	}

}
