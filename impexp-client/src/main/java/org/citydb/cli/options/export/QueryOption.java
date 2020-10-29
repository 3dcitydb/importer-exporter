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

package org.citydb.cli.options.export;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.exporter.ExportAppearance;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.plugin.cli.CliOption;
import org.citydb.plugin.cli.TypeNamesOption;
import org.citydb.plugin.cli.XMLQueryOption;
import org.citydb.registry.ObjectRegistry;
import picocli.CommandLine;

public class QueryOption implements CliOption {
    @CommandLine.ArgGroup(exclusive = false)
    private TypeNamesOption typeNamesOption;

    @CommandLine.ArgGroup
    private BoundingBoxOption boundingBoxOption;

    @CommandLine.ArgGroup(exclusive = false)
    private LodOption lodOption;

    @CommandLine.ArgGroup
    private AppearanceOption appearanceOption;

    @CommandLine.ArgGroup
    private XMLQueryOption xmlQueryOption;

    public QueryConfig toQueryConfig() {
        if (typeNamesOption != null
                || boundingBoxOption != null
                || lodOption != null
                || appearanceOption != null) {
            QueryConfig queryConfig = new QueryConfig();
            if (typeNamesOption != null) {
                queryConfig.setFeatureTypeFilter(typeNamesOption.toFeatureTypeFilter());
            }

            if (boundingBoxOption != null) {
                BoundingBox bbox = boundingBoxOption.toBoundingBox();
                if (bbox != null) {
                    BBOXOperator bboxOperator = new BBOXOperator();
                    bboxOperator.setEnvelope(bbox);
                    SelectionFilter selectionFilter = new SelectionFilter();
                    selectionFilter.setPredicate(bboxOperator);
                    queryConfig.setSelectionFilter(selectionFilter);
                }
            }

            if (lodOption != null) {
                queryConfig.setLodFilter(lodOption.toLodFilter());
            }

            if (appearanceOption != null) {
                if (appearanceOption.isExportAppearances()) {
                    queryConfig.setAppearanceFilter(appearanceOption.toAppearanceFilter());
                } else {
                    ExportAppearance appearance = ObjectRegistry.getInstance().getConfig().getExportConfig().getAppearances();
                    appearance.setExportAppearances(false);
                }
            }

            return queryConfig;
        } else {
            return xmlQueryOption.toQueryConfig();
        }
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (xmlQueryOption != null) {
            if (typeNamesOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --type-names and --xml-query are mutually exclusive (specify only one)");
            }

            if (boundingBoxOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --bbox and --xml-query are mutually exclusive (specify only one)");
            }

            if (lodOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --lod and --xml-query are mutually exclusive (specify only one)");
            }

            if (appearanceOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: Appearance options and --xml-query are mutually exclusive (specify only one)");
            }

            xmlQueryOption.preprocess(commandLine);
        }

        if (typeNamesOption != null) {
            typeNamesOption.preprocess(commandLine);
        }

        if (boundingBoxOption != null) {
            boundingBoxOption.preprocess(commandLine);
        }

        if (lodOption != null) {
            lodOption.preprocess(commandLine);
        }

        if (appearanceOption != null) {
            appearanceOption.preprocess(commandLine);
        }
    }
}
