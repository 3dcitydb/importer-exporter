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
package de.tub.citydb.config.project;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.tub.citydb.api.plugin.extension.config.PluginConfig;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.Exporter;
import de.tub.citydb.config.project.global.Global;
import de.tub.citydb.config.project.importer.Importer;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;

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
	@XmlElement(required=true)
	private Database database;
	@XmlElement(name="import", required=true)
	private Importer importer;
	@XmlElement(name="export", required=true)
	private Exporter exporter;
	@XmlElement(name="kmlExport", required=true)
	private KmlExporter kmlExporter;
	private Global global;
	@XmlJavaTypeAdapter(de.tub.citydb.config.project.plugin.PluginConfigListAdapter.class)
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
