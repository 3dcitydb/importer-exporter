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

package org.citydb.core.query.builder.util;

import org.citydb.config.project.query.filter.selection.comparison.AbstractComparisonOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator;
import org.citydb.config.project.query.filter.sorting.SortProperty;
import org.citydb.config.util.ConfigNamespaceFilter;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.database.schema.util.SimpleXPathParser;
import org.citydb.core.database.schema.util.XPathException;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citygml4j.model.module.gml.GMLCoreModule;

import javax.xml.namespace.NamespaceContext;

public class ValueReferenceBuilder {
    private final Query query;
    private final SchemaMapping schemaMapping;
    private final NamespaceContext namespaceContext;
    private final SimpleXPathParser xPathParser;

    private FeatureType featureType;

    public ValueReferenceBuilder(Query query, SchemaMapping schemaMapping, NamespaceContext namespaceContext) {
        this.query = query;
        this.schemaMapping = schemaMapping;
        this.namespaceContext = namespaceContext != null ? namespaceContext : new ConfigNamespaceFilter();
        xPathParser = new SimpleXPathParser(schemaMapping);
    }

    public ValueReference buildValueReference(AbstractComparisonOperator operatorConfig) throws QueryBuildException {
        ValueReference valueReference = buildValueReference(operatorConfig.getValueReference());

        // reset XPath expression using default namespace prefixes
        operatorConfig.setValueReference(valueReference.getSchemaPath().toXPath());

        return valueReference;
    }

    public ValueReference buildValueReference(AbstractSpatialOperator operatorConfig) throws QueryBuildException {
        ValueReference valueReference;

        if (operatorConfig.isSetValueReference()) {
            valueReference = buildValueReference(operatorConfig.getValueReference());
            if (valueReference.getTarget().getElementType() != PathElementType.GEOMETRY_PROPERTY)
                throw new QueryBuildException("The value reference of the spatial operator " + operatorConfig.getOperatorName() + " must point to a geometry property.");

            // reset XPath expression using default namespace prefixes
            operatorConfig.setValueReference(valueReference.getSchemaPath().toXPath());
        } else {
            try {
                FeatureType featureType = getFeatureType();
                SchemaPath path = new SchemaPath(featureType);
                path.appendChild(featureType.getProperty("boundedBy", GMLCoreModule.v3_1_1.getNamespaceURI(), true));
                valueReference = new ValueReference(path);
            } catch (InvalidSchemaPathException e) {
                throw new QueryBuildException("Failed to parse the value reference " + operatorConfig.getValueReference() + ".", e);
            }
        }

        return valueReference;
    }

    public ValueReference buildValueReference(SortProperty sortBy) throws QueryBuildException {
        ValueReference valueReference = buildValueReference(sortBy.getValueReference());

        // reset XPath expression using default namespace prefixes
        sortBy.setValueReference(valueReference.getSchemaPath().toXPath());

        return valueReference;
    }

    private ValueReference buildValueReference(String valueReference) throws QueryBuildException {
        try {
            SchemaPath path = xPathParser.parse(valueReference, getFeatureType(), namespaceContext);
            return new ValueReference(path);
        } catch (XPathException | InvalidSchemaPathException e) {
            throw new QueryBuildException("Failed to parse the value reference " + valueReference + ".", e);
        }
    }

    private FeatureType getFeatureType() throws QueryBuildException {
        if (featureType == null) {
            featureType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
            if (featureType == null)
                throw new QueryBuildException("Failed to retrieve common super type from feature type filter.");
        }

        return featureType;
    }
}
