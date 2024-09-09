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

package org.citydb.cli.operation.visExporter;

import org.citydb.cli.option.*;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.config.project.query.simple.SimpleAttributeFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;
import org.citydb.config.project.visExporter.SimpleVisQuery;
import picocli.CommandLine;

public class QueryOption implements CliOption {
    @CommandLine.ArgGroup(exclusive = false)
    private TypeNamesOption typeNamesOption;

    @CommandLine.ArgGroup(exclusive = false)
    private FeatureVersionOption featureVersionOption;

    @CommandLine.ArgGroup
    private ResourceIdOption resourceIdOption;

    @CommandLine.ArgGroup
    private BoundingBoxOption boundingBoxOption;

    @CommandLine.ArgGroup
    private TilingOption tilingOption;

    @CommandLine.ArgGroup
    private SQLSelectOption sqlSelectOption;

    public SimpleVisQuery toSimpleVisQuery() {
        SimpleVisQuery query = new SimpleVisQuery();

        if (typeNamesOption != null) {
            query.setUseTypeNames(true);
            query.setFeatureTypeFilter(typeNamesOption.toFeatureTypeFilter());
        }

        if (featureVersionOption != null) {
            SimpleFeatureVersionFilter versionFilter = featureVersionOption.toFeatureVersionFilter();
            if (versionFilter != null) {
                query.setUseFeatureVersionFilter(true);
                query.setFeatureVersionFilter(versionFilter);
            }
        }

        if (resourceIdOption != null) {
            ResourceIdOperator idOperator = resourceIdOption.toResourceIdOperator();
            if (idOperator != null) {
                query.setUseAttributeFilter(true);
                SimpleAttributeFilter attributeFilter = new SimpleAttributeFilter();
                attributeFilter.setResourceIdFilter(idOperator);
                query.setAttributeFilter(attributeFilter);
            }
        }

        if (tilingOption != null) {
            query.setBboxFilter(tilingOption.toVisTiling());
        }

        if (boundingBoxOption != null) {
            query.setUseBboxFilter(true);
            query.getBboxFilter().setExtent(boundingBoxOption.toBoundingBox());
        }

        if (sqlSelectOption != null) {
            SelectOperator selectOperator = sqlSelectOption.toSelectOperator();
            if (selectOperator != null) {
                query.setUseSQLFilter(true);
                query.setSQLFilter(selectOperator);
            }
        }

        return query;
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (typeNamesOption != null) {
            typeNamesOption.preprocess(commandLine);
        }

        if (featureVersionOption != null) {
            featureVersionOption.preprocess(commandLine);
        }

        if (boundingBoxOption != null) {
            boundingBoxOption.preprocess(commandLine);
        }

        if (tilingOption != null) {
            tilingOption.preprocess(commandLine);
        }
    }
}
