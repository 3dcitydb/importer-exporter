/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.config;

import org.citydb.config.gui.GuiConfig;
import org.citydb.config.internal.Internal;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.deleter.DeleteConfig;
import org.citydb.config.project.exporter.ExportConfig;
import org.citydb.config.project.global.GlobalConfig;
import org.citydb.config.project.importer.ImportConfig;
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.config.project.plugin.PluginConfig;
import org.citydb.config.util.ConfigNamespaceFilter;

public class Config {
	private ProjectConfig projectConfig;
	private GuiConfig guiConfig;
	private Internal internal;

	public Config(ProjectConfig projectConfig, GuiConfig guiConfig, Internal internal) {
		this.projectConfig = projectConfig;
		this.guiConfig = guiConfig;
		this.internal = internal;
	}
	
	public Config() {
		this(new ProjectConfig(), new GuiConfig(), new Internal());
	}

	public ProjectConfig getProjectConfig() {
		return projectConfig;
	}

	public void setProjectConfig(ProjectConfig projectConfig) {
		if (projectConfig != null) {
			this.projectConfig = projectConfig;
			
			// add things to be done after changing the project settings
			// (e.g., after unmarshalling the config file) here 
			projectConfig.getDatabaseConfig().addDefaultReferenceSystems();
		}
	}

	public DatabaseConfig getDatabaseConfig() {
		return projectConfig.getDatabaseConfig();
	}

	public void setDatabaseConfig(DatabaseConfig databaseConfig) {
		projectConfig.setDatabaseConfig(databaseConfig);
	}

	public ImportConfig getImportConfig() {
		return projectConfig.getImportConfig();
	}

	public void setImportConfig(ImportConfig importConfig) {
		projectConfig.setImportConfig(importConfig);
	}

	public ExportConfig getExportConfig() {
		return projectConfig.getExportConfig();
	}

	public void setExportConfig(ExportConfig exportConfig) {
		projectConfig.setExportConfig(exportConfig);
	}

	public DeleteConfig getDeleteConfig() {
		return projectConfig.getDeleteConfig();
	}

	public void setDeleteConfig(DeleteConfig deleteConfig) {
		projectConfig.setDeleteConfig(deleteConfig);
	}

	public KmlExportConfig getKmlExportConfig() {
		return projectConfig.getKmlExportConfig();
	}

	public void setKmlExportConfig(KmlExportConfig kmlExportConfig) {
		projectConfig.setKmlExportConfig(kmlExportConfig);
	}

	public GlobalConfig getGlobalConfig() {
		return projectConfig.getGlobalConfig();
	}

	public void setGlobalConfig(GlobalConfig globalConfig) {
		projectConfig.setGlobalConfig(globalConfig);
	}

	public <T extends PluginConfig> T getPluginConfig(Class<T> type) {
		return projectConfig.getPluginConfig(type);
	}

	public PluginConfig registerPluginConfig(PluginConfig pluginConfig) {
		return projectConfig.registerPluginConfig(pluginConfig);
	}

	public ConfigNamespaceFilter getNamespaceFilter() {
		return projectConfig.getNamespaceFilter();
	}

	public void setNamespaceFilter(ConfigNamespaceFilter namespaceFilter) {
		projectConfig.setNamespaceFilter(namespaceFilter);
	}

	public GuiConfig getGuiConfig() {
		return guiConfig;
	}

	public void setGuiConfig(GuiConfig guiConfig) {
		if (guiConfig != null)
			this.guiConfig = guiConfig;
	}

	public Internal getInternal() {
		return internal;
	}

}
