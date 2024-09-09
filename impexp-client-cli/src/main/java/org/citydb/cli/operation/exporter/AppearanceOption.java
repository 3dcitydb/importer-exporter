/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.cli.operation.exporter;

import org.citydb.cli.option.CliOption;
import org.citydb.config.project.query.filter.appearance.AppearanceFilter;
import picocli.CommandLine;

public class AppearanceOption implements CliOption {
    @CommandLine.Option(names = "--no-appearance", defaultValue = "true",
            description = "Do not export appearance information.")
    private boolean exportAppearances;

    @CommandLine.Option(names = {"-a", "--appearance-theme"}, split = ",", paramLabel = "<theme>",
            description = "Only export appearances with a matching theme. Use 'none' for the null theme.")
    private String[] themes;

    public boolean isExportAppearances() {
        return exportAppearances;
    }

    public boolean isSetAppearanceFilter() {
        return exportAppearances && themes != null;
    }

    public AppearanceFilter toAppearanceFilter() {
        if (exportAppearances && themes != null) {
            AppearanceFilter appearanceFilter = new AppearanceFilter();
            for (String theme : themes) {
                if ("none".equalsIgnoreCase(theme)) {
                    appearanceFilter.setIncludeNullTheme(true);
                } else {
                    appearanceFilter.addTheme(theme);
                }
            }

            return appearanceFilter;
        }

        return null;
    }
}
