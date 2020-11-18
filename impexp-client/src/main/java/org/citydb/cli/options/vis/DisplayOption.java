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
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    public void toDisplayForms(List<DisplayForm> displayForms) {
        Map<Integer, DisplayForm> candidates = displayForms.stream()
                .collect(Collectors.toMap(DisplayForm::getForm, d -> d));

        int visibleTo = -1;
        for (Mode mode : Mode.values()) {
            DisplayForm displayForm = candidates.computeIfAbsent(mode.type, v -> DisplayForm.of(mode.type));
            displayForm.setActive(modes.contains(mode) && lod.ordinal() >= mode.minimumLod);
            if (displayForm.isActive()) {
                int visibleFrom = this.visibleFrom != null ? this.visibleFrom.getOrDefault(mode, 0) : 0;
                displayForm.setVisibleFrom(visibleFrom);
                displayForm.setVisibleUpTo(visibleTo);
                visibleTo = visibleFrom;
            }
        }

        candidates.values().forEach(displayForm -> {
            if (!displayForms.contains(displayForm)) {
                displayForms.add(displayForm);
            }
        });
    }

    public enum Mode {
        collada("collada", DisplayForm.COLLADA, 1),
        geometry("geometry", DisplayForm.GEOMETRY, 1),
        extruded("extruded", DisplayForm.EXTRUDED, 1),
        footprint("footprint", DisplayForm.FOOTPRINT, 0);

        private final String value;
        private final int type;
        private final int minimumLod;

        Mode(String value, int type, int minimumLod) {
            this.value = value;
            this.type = type;
            this.minimumLod = minimumLod;
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
