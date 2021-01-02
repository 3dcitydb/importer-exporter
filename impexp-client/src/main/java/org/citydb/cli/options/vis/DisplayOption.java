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
    @CommandLine.Option(names = {"-D", "--display-mode"}, split = ",", paramLabel = "<mode[=pixels]>",
            required = true, mapFallbackValue = "0",
            description = "Display mode: collada, geometry, extruded, footprint. Optionally specify the visibility " +
                    "in terms of screen pixels (default: ${MAP-FALLBACK-VALUE}).")
    private Map<Mode, Integer> modes;

    @CommandLine.Option(names = {"-l", "--lod"}, paramLabel = "<0..4 | halod>", required = true,
            description = "LoD to export from.")
    private String lodOption;

    @CommandLine.Option(names = {"-a", "--appearance-theme"}, paramLabel = "<theme>",
            description = "Appearance theme to use for COLLADA/glTF exports. Use 'none' for the null theme.")
    private String theme;

    private int lod;

    public Set<Mode> getModes() {
        return modes.keySet();
    }

    public int getLod() {
        return lod;
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
            displayForm.setActive(modes.containsKey(mode) && mode.type.isAchievableFromLoD(lod));
            if (displayForm.isActive()) {
                int visibleFrom = modes.get(mode);
                displayForm.setVisibleFrom(visibleFrom);
                displayForm.setVisibleTo(visibleTo);
                visibleTo = visibleFrom;
            }

            displayForms.add(displayForm);
        }

        return displayForms;
    }

    public enum Mode {
        collada(DisplayFormType.COLLADA),
        geometry(DisplayFormType.GEOMETRY),
        extruded(DisplayFormType.EXTRUDED),
        footprint(DisplayFormType.FOOTPRINT);

        private final DisplayFormType type;

        Mode(DisplayFormType type) {
            this.type = type;
        }
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (lodOption != null) {
            switch (lodOption.toLowerCase()) {
                case "0":
                case "1":
                case "2":
                case "3":
                case "4":
                    lod = Integer.parseInt(lodOption);
                    break;
                case "halod":
                    lod = 5;
                    break;
                default:
                    throw new CommandLine.ParameterException(commandLine, "Invalid value for option '--lod': expected " +
                            "one of [0, 1, 2, 3, 4, halod] (case-insensitive) but was '" + lodOption + "'");
            }
        }
    }
}
