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

package org.citydb.cli.operation.exporter;

import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.lod.LodFilterMode;
import org.citydb.config.project.query.filter.lod.LodSearchMode;
import org.citydb.plugin.cli.CliOption;
import picocli.CommandLine;

public class LodOption implements CliOption {
    enum Mode {or, and, minimum, maximum}

    @CommandLine.Option(names = {"-l", "--lod"}, split = ",", paramLabel = "<0..4>", required = true,
            description = "LoD representations to export.")
    private int[] lods;

    @CommandLine.Option(names = "--lod-mode", defaultValue = "or",
            description = "LoD filter mode: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE}).")
    private Mode mode;

    @CommandLine.Option(names = "--lod-search-depth", paramLabel = "<0..n|all>", defaultValue = "1",
            description = "Levels of sub-features to search for matching LoDs (default: ${DEFAULT-VALUE}).")
    private String searchDepth;

    private LodFilter lodFilter;

    public LodFilter toLodFilter() {
        return lodFilter;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        lodFilter = new LodFilter();
        for (int lod : lods) {
            if (lod < 0 || lod > 4) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: An LoD value must be between 0 and 4 but was '" + lod + "'");
            }

            lodFilter.setLod(lod);
        }

        if (mode != null) {
            switch (mode) {
                case and:
                    lodFilter.setMode(LodFilterMode.AND);
                    break;
                case minimum:
                    lodFilter.setMode(LodFilterMode.MINIMUM);
                    break;
                case maximum:
                    lodFilter.setMode(LodFilterMode.MAXIMUM);
                    break;
                default:
                    lodFilter.setMode(LodFilterMode.OR);
            }
        }

        if (searchDepth != null) {
            if ("all".equalsIgnoreCase(searchDepth)) {
                lodFilter.setSearchMode(LodSearchMode.ALL);
            } else {
                try {
                    int level = Integer.parseInt(searchDepth);
                    if (level < 0) {
                        throw new CommandLine.ParameterException(commandLine,
                                "Error: The LoD search depth must be a non-negative integer but was '" + searchDepth + "'");
                    }

                    lodFilter.setSearchMode(LodSearchMode.DEPTH);
                    lodFilter.setSearchDepth(level);
                } catch (NumberFormatException e) {
                    throw new CommandLine.ParameterException(commandLine,
                            "Error: The LoD search depth must be an integer or 'all' but was '" + searchDepth + "'");
                }
            }
        }
    }
}
