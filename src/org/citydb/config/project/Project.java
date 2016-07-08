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
package org.citydb.config.project;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.citydb.api.plugin.extension.config.PluginConfig;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.exporter.Exporter;
import org.citydb.config.project.global.Global;
import org.citydb.config.project.importer.Importer;
import org.citydb.config.project.kmlExporter.KmlExporter;

@XmlRootElement
@XmlType(name="ProjectType", propOrder={
		"database",
		"importer",
		"exporter",
		"kmlExporter",
		"global",
		"extensions"
})
public class Project {
	@XmlElement
	private Database database;
	@XmlElement(name="import")
	private Importer importer;
	@XmlElement(name="export")
	private Exporter exporter;
	@XmlElement(name="kmlExport")
	private KmlExporter kmlExporter;
	private Global global;
	@XmlJavaTypeAdapter(org.citydb.config.project.plugin.PluginConfigListAdapter.class)
	private HashMap<Class<? extends PluginConfig>, PluginConfig> extensions;

	public Project() {
		database = new Database();
		importer = new Importer();
		exporter = new Exporter();
		kmlExporter = new KmlExporter();
		global = new Global();
		extensions = new HashMap<Class<? extends PluginConfig>, PluginConfig>();
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		if (database != null)
			this.database = database;
	}

	public Importer getImporter() {
		return importer;
	}

	public void setImporter(Importer importer) {
		if (importer != null)
			this.importer = importer;
	}

	public Exporter getExporter() {
		return exporter;
	}

	public void setExporter(Exporter exporter) {
		if (exporter != null)
			this.exporter = exporter;
	}

	public Global getGlobal() {
		return global;
	}

	public void setGlobal(Global global) {
		if (global != null)
			this.global = global;
	}

	public void setKmlExporter(KmlExporter kmlExporter) {
		if (kmlExporter != null)
			this.kmlExporter = kmlExporter;
	}

	public KmlExporter getKmlExporter() {
		return kmlExporter;
	}

	public PluginConfig getExtension(Class<? extends PluginConfig> pluginConfigClass) {
		return extensions.get(pluginConfigClass);
	}
	
	public PluginConfig registerExtension(PluginConfig pluginConfig) {
		return extensions.put(pluginConfig.getClass(), pluginConfig);
	}
	
}
