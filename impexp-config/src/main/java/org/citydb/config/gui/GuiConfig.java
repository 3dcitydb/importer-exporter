/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.config.gui;

import org.citydb.config.gui.database.DatabaseGuiConfig;
import org.citydb.config.gui.exporter.ExportGuiConfig;
import org.citydb.config.gui.importer.ImportGuiConfig;
import org.citydb.config.gui.kmlExporter.KmlExportGuiConfig;
import org.citydb.config.gui.preferences.PreferencesGuiConfig;
import org.citydb.config.gui.style.Appearance;
import org.citydb.config.gui.window.ConsoleWindow;
import org.citydb.config.gui.window.MainWindow;
import org.citydb.config.gui.window.MapWindow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlType(name = "GuiType", propOrder = {
        "main",
        "console",
        "map",
        "appearance",
        "databaseGuiConfig",
        "importGuiConfig",
        "exportGuiConfig",
        "kmlExportGuiConfig",
        "preferencesGuiConfig",
        "recentlyUsedProjects"
})
public class GuiConfig {
    private MainWindow main;
    private ConsoleWindow console;
    private MapWindow map;
    private Appearance appearance;
    @XmlElement(name = "database")
    private DatabaseGuiConfig databaseGuiConfig;
    @XmlElement(name = "import")
    private ImportGuiConfig importGuiConfig;
    @XmlElement(name = "export")
    private ExportGuiConfig exportGuiConfig;
    @XmlElement(name = "kmlExport")
    private KmlExportGuiConfig kmlExportGuiConfig;
    @XmlElement(name = "preferences")
    private PreferencesGuiConfig preferencesGuiConfig;
    @XmlElementWrapper(name = "recentlyUsedProjects")
    @XmlElement(name = "fileName")
    private List<String> recentlyUsedProjects;

    @XmlTransient
    private final int maxLastUsedEntries = 5;

    public GuiConfig() {
        main = new MainWindow();
        console = new ConsoleWindow();
        map = new MapWindow();
        appearance = new Appearance();
        databaseGuiConfig = new DatabaseGuiConfig();
        importGuiConfig = new ImportGuiConfig();
        exportGuiConfig = new ExportGuiConfig();
        kmlExportGuiConfig = new KmlExportGuiConfig();
        preferencesGuiConfig = new PreferencesGuiConfig();
        recentlyUsedProjects = new ArrayList<>(maxLastUsedEntries + 1);
    }

    public MainWindow getMainWindow() {
        return main;
    }

    public void setMainWindow(MainWindow main) {
        if (main != null) {
            this.main = main;
        }
    }

    public ConsoleWindow getConsoleWindow() {
        return console;
    }

    public void setConsoleWindow(ConsoleWindow console) {
        if (console != null) {
            this.console = console;
        }
    }

    public MapWindow getMapWindow() {
        return map;
    }

    public void setMapWindow(MapWindow map) {
        if (map != null) {
            this.map = map;
        }
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public void setAppearance(Appearance appearance) {
        if (appearance != null) {
            this.appearance = appearance;
        }
    }

    public DatabaseGuiConfig getDatabaseGuiConfig() {
        return databaseGuiConfig;
    }

    public void setDatabaseGuiConfig(DatabaseGuiConfig databaseGuiConfig) {
        if (databaseGuiConfig != null) {
            this.databaseGuiConfig = databaseGuiConfig;
        }
    }

    public ImportGuiConfig getImportGuiConfig() {
        return importGuiConfig;
    }

    public void setImportGuiConfig(ImportGuiConfig importGuiConfig) {
        if (importGuiConfig != null) {
            this.importGuiConfig = importGuiConfig;
        }
    }

    public ExportGuiConfig getExportGuiConfig() {
        return exportGuiConfig;
    }

    public void setExportGuiConfig(ExportGuiConfig exportGuiConfig) {
        if (exportGuiConfig != null) {
            this.exportGuiConfig = exportGuiConfig;
        }
    }

    public KmlExportGuiConfig getKmlExportGuiConfig() {
        return kmlExportGuiConfig;
    }

    public void setKmlExportGuiConfig(KmlExportGuiConfig kmlExportGuiConfig) {
        if (kmlExportGuiConfig != null) {
            this.kmlExportGuiConfig = kmlExportGuiConfig;
        }
    }

    public PreferencesGuiConfig getPreferencesGuiConfig() {
        return preferencesGuiConfig;
    }

    public void setPreferencesGuiConfig(PreferencesGuiConfig preferencesGuiConfig) {
        if (preferencesGuiConfig != null) {
            this.preferencesGuiConfig = preferencesGuiConfig;
        }
    }

    public List<String> getRecentlyUsedConfigFiles() {
        return recentlyUsedProjects;
    }

    public void setRecentlyUsedConfigFiles(List<String> recentlyUsedProjects) {
        if (recentlyUsedProjects != null)
            this.recentlyUsedProjects = recentlyUsedProjects;
    }

    public int getMaxLastUsedEntries() {
        return maxLastUsedEntries;
    }

}
