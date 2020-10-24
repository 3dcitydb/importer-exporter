/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.deleter.DeleteConfig;
import org.citydb.config.project.exporter.ExportConfig;
import org.citydb.config.project.global.GlobalConfig;
import org.citydb.config.project.importer.ImportConfig;
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.config.project.plugin.PluginConfig;
import org.citydb.config.project.plugin.PluginConfigListAdapter;
import org.citydb.config.util.ConfigNamespaceFilter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "project")
@XmlType(name = "ProjectType", propOrder = {
        "databaseConfig",
        "importConfig",
        "exportConfig",
        "deleteConfig",
        "kmlExportConfig",
        "globalConfig",
        "extensions"
})
public class ProjectConfig {
    @XmlElement(name = "database")
    private DatabaseConfig databaseConfig;
    @XmlElement(name = "import")
    private ImportConfig importConfig;
    @XmlElement(name = "export")
    private ExportConfig exportConfig;
    @XmlElement(name = "delete")
    private DeleteConfig deleteConfig;
    @XmlElement(name = "kmlExport")
    private KmlExportConfig kmlExportConfig;
    @XmlElement(name = "global")
    private GlobalConfig globalConfig;
    @XmlJavaTypeAdapter(PluginConfigListAdapter.class)
    private final Map<Class<? extends PluginConfig>, PluginConfig> extensions;

    @XmlTransient
    private ConfigNamespaceFilter namespaceFilter;

    public ProjectConfig(DatabaseConfig databaseConfig, ImportConfig importConfig, ExportConfig exportConfig, DeleteConfig deleteConfig, KmlExportConfig kmlExportConfig, GlobalConfig globalConfig) {
        this.databaseConfig = databaseConfig;
        this.importConfig = importConfig;
        this.exportConfig = exportConfig;
        this.deleteConfig = deleteConfig;
        this.kmlExportConfig = kmlExportConfig;
        this.globalConfig = globalConfig;

        namespaceFilter = new ConfigNamespaceFilter();
        extensions = new HashMap<>();
    }

    public ProjectConfig() {
        this(new DatabaseConfig(), new ImportConfig(), new ExportConfig(), new DeleteConfig(), new KmlExportConfig(), new GlobalConfig());
    }

    DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    void setDatabaseConfig(DatabaseConfig databaseConfig) {
        if (databaseConfig != null)
            this.databaseConfig = databaseConfig;
    }

    ImportConfig getImportConfig() {
        return importConfig;
    }

    void setImportConfig(ImportConfig importConfig) {
        if (importConfig != null)
            this.importConfig = importConfig;
    }

    ExportConfig getExportConfig() {
        return exportConfig;
    }

    void setExportConfig(ExportConfig exportConfig) {
        if (exportConfig != null)
            this.exportConfig = exportConfig;
    }

    DeleteConfig getDeleteConfig() {
        return deleteConfig;
    }

    void setDeleteConfig(DeleteConfig deleteConfig) {
        if (deleteConfig != null)
            this.deleteConfig = deleteConfig;
    }

    KmlExportConfig getKmlExportConfig() {
        return kmlExportConfig;
    }

    void setKmlExportConfig(KmlExportConfig kmlExportConfig) {
        if (kmlExportConfig != null)
            this.kmlExportConfig = kmlExportConfig;
    }

    GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    void setGlobalConfig(GlobalConfig globalConfig) {
        if (globalConfig != null)
            this.globalConfig = globalConfig;
    }

    <T extends PluginConfig> T getPluginConfig(Class<T> type) {
        PluginConfig config = extensions.get(type);
        return type.isInstance(config) ? type.cast(config) : null;
    }

    PluginConfig registerPluginConfig(PluginConfig pluginConfig) {
        return extensions.put(pluginConfig.getClass(), pluginConfig);
    }

    ConfigNamespaceFilter getNamespaceFilter() {
        return namespaceFilter;
    }

    void setNamespaceFilter(ConfigNamespaceFilter namespaceFilter) {
        this.namespaceFilter = namespaceFilter;
    }
}
