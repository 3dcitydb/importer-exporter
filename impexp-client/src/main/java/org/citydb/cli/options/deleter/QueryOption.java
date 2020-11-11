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

package org.citydb.cli.options.deleter;

import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.selection.id.DatabaseIdOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.logical.AndOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.plugin.cli.CliOption;
import org.citydb.plugin.cli.CounterOption;
import org.citydb.plugin.cli.DatabaseIdOption;
import org.citydb.plugin.cli.ResourceIdOption;
import org.citydb.plugin.cli.SQLSelectOption;
import org.citydb.plugin.cli.TypeNamesOption;
import org.citydb.plugin.cli.XMLQueryOption;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

public class QueryOption implements CliOption {
    @CommandLine.ArgGroup(exclusive = false)
    private TypeNamesOption typeNamesOption;

    @CommandLine.ArgGroup
    private ResourceIdOption resourceIdOption;

    @CommandLine.ArgGroup
    private DatabaseIdOption databaseIdOption;

    @CommandLine.ArgGroup(exclusive = false)
    private BoundingBoxOption boundingBoxOption;

    @CommandLine.ArgGroup(exclusive = false)
    private CounterOption counterOption;

    @CommandLine.ArgGroup
    private SQLSelectOption sqlSelectOption;

    @CommandLine.ArgGroup
    private XMLQueryOption xmlQueryOption;

    public QueryConfig toQueryConfig() {
        if (typeNamesOption != null
                || resourceIdOption != null
                || databaseIdOption != null
                || boundingBoxOption != null
                || counterOption != null
                || sqlSelectOption != null) {
            QueryConfig queryConfig = new QueryConfig();
            List<AbstractPredicate> predicates = new ArrayList<>();

            if (typeNamesOption != null) {
                queryConfig.setFeatureTypeFilter(typeNamesOption.toFeatureTypeFilter());
            }

            if (resourceIdOption != null) {
                ResourceIdOperator idOperator = resourceIdOption.toResourceIdOperator();
                if (idOperator != null) {
                    predicates.add(idOperator);
                }
            }

            if (databaseIdOption != null) {
                DatabaseIdOperator idOperator = databaseIdOption.toDatabaseIdOperator();
                if (idOperator != null) {
                    predicates.add(idOperator);
                }
            }

            if (boundingBoxOption != null) {
                AbstractSpatialOperator spatialOperator = boundingBoxOption.toSpatialOperator();
                if (spatialOperator != null) {
                    predicates.add(spatialOperator);
                }
            }

            if (counterOption != null) {
                queryConfig.setCounterFilter(counterOption.toCounterFilter());
            }

            if (sqlSelectOption != null) {
                SelectOperator selectOperator = sqlSelectOption.toSelectOperator();
                if (selectOperator != null) {
                    predicates.add(selectOperator);
                }
            }

            if (!predicates.isEmpty()) {
                AndOperator andOperator = new AndOperator();
                andOperator.setOperands(predicates);
                SelectionFilter selectionFilter = new SelectionFilter();
                selectionFilter.setPredicate(andOperator);
                queryConfig.setSelectionFilter(selectionFilter);
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
                        "Error: --type-name and --xml-query are mutually exclusive (specify only one)");
            } else if (resourceIdOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --gml-id and --xml-query are mutually exclusive (specify only one)");
            } else if (databaseIdOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --db-id and --xml-query are mutually exclusive (specify only one)");
            } else if (boundingBoxOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --bbox and --xml-query are mutually exclusive (specify only one)");
            } else if (counterOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: Counter options and --xml-query are mutually exclusive (specify only one)");
            } else if (sqlSelectOption != null) {
                throw new CommandLine.ParameterException(commandLine,
                        "Error: --sql-select and --xml-query are mutually exclusive (specify only one)");
            }

            xmlQueryOption.preprocess(commandLine);
        }

        if (typeNamesOption != null) {
            typeNamesOption.preprocess(commandLine);
        }

        if (databaseIdOption != null) {
            databaseIdOption.preprocess(commandLine);
        }

        if (boundingBoxOption != null) {
            boundingBoxOption.preprocess(commandLine);
        }

        if (counterOption != null) {
            counterOption.preprocess(commandLine);
        }
    }
}
