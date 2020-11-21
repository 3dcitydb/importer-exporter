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

package org.citydb.cli.options.vis;

import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.DisplayFormType;
import org.citydb.config.project.kmlExporter.DisplayForms;
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

import java.util.Map;
import java.util.Set;

public class DisplayOption implements CliOption {
    @CommandLine.Option(names = {"-D", "--display-mode"}, split = ",", paramLabel = "<mode>", required = true,
            description = "Display mode: ${COMPLETION-CANDIDATES}.")
    private Set<Mode> modes;

    @CommandLine.Option(names = {"-l", "--lod"}, paramLabel = "<0..4 | halod>", required = true,
            description = "LoD to export from.")
    private Lod lod;

    @CommandLine.Option(names = {"-v", "--visible-from"}, split = ",", paramLabel = "<mode=value>",
            description = "Visibility for each display mode (default: 0).")
    private Map<Mode, Integer> visibleFrom;

    @CommandLine.Option(names = {"-a", "--appearance-theme"}, paramLabel = "<theme>",
            description = "Appearance theme to use for COLLADA/glTF exports. Use 'none' for the null theme.")
    private String theme;

    public Set<Mode> getModes() {
        return modes;
    }

    public int getLod() {
        return lod.ordinal();
    }

    public String getAppearanceTheme() {
        if (theme == null) {
            return KmlExportConfig.THEME_NONE;
        }

        return "none".equalsIgnoreCase(theme) ? KmlExportConfig.THEME_NULL : theme;
    }

    public DisplayForms toDisplayForms() {
        DisplayForms displayForms = new DisplayForms();

        int visibleTo = -1;
        for (Mode mode : Mode.values()) {
            DisplayForm displayForm = DisplayForm.of(mode.type);
            displayForm.setActive(modes.contains(mode) && mode.type.isAchievableFromLoD(lod.ordinal()));
            if (displayForm.isActive()) {
                int visibleFrom = this.visibleFrom != null ? this.visibleFrom.getOrDefault(mode, 0) : 0;
                displayForm.setVisibleFrom(visibleFrom);
                displayForm.setVisibleTo(visibleTo);
                visibleTo = visibleFrom;
            }

            displayForms.add(displayForm);
        }

        return displayForms;
    }

    public enum Mode {
        collada("collada", DisplayFormType.COLLADA),
        geometry("geometry", DisplayFormType.GEOMETRY),
        extruded("extruded", DisplayFormType.EXTRUDED),
        footprint("footprint", DisplayFormType.FOOTPRINT);

        private final String value;
        private final DisplayFormType type;

        Mode(String value, DisplayFormType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    enum Lod {
        lod0("0"),
        lod1("1"),
        lod2("2"),
        lod3("3"),
        lod4("4"),
        halod("halod");

        private final String value;

        Lod(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
