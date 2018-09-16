/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.query.builder.config;

import javax.xml.namespace.NamespaceContext;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.query.filter.selection.spatial.AbstractBinarySpatialOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractDistanceOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.database.schema.util.SimpleXPathParser;
import org.citydb.database.schema.util.XPathException;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.spatial.AbstractSpatialOperator;
import org.citydb.query.filter.selection.operator.spatial.BinarySpatialOperator;
import org.citydb.query.filter.selection.operator.spatial.Distance;
import org.citydb.query.filter.selection.operator.spatial.DistanceOperator;
import org.citydb.query.filter.selection.operator.spatial.DistanceUnit;
import org.citydb.query.filter.selection.operator.spatial.SpatialOperationFactory;
import org.citydb.query.geometry.GeometryParseException;
import org.citydb.query.geometry.config.SpatialOperandParser;
import org.citygml4j.model.module.gml.GMLCoreModule;

public class SpatialOperatorBuilder {
	private final Query query;
	private final SimpleXPathParser xPathParser;
	private final SchemaMapping schemaMapping;
	private final SpatialOperandParser spatialOperandParser;
	private final NamespaceContext namespaceContext;

	protected SpatialOperatorBuilder(Query query, SimpleXPathParser xPathParser, SchemaMapping schemaMapping, NamespaceContext namespaceContext, AbstractDatabaseAdapter databaseAdapter) {
		this.query = query;
		this.xPathParser = xPathParser;
		this.schemaMapping = schemaMapping;
		this.namespaceContext = namespaceContext;
		
		spatialOperandParser = new SpatialOperandParser(databaseAdapter);
	}

	protected Predicate buildSpatialOperator(org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator operatorConfig) throws QueryBuildException {
		AbstractSpatialOperator operator = null;

		try {
			switch (operatorConfig.getOperatorName()) {
			case BBOX:
				operator = buildBBOXOperator((BBOXOperator)operatorConfig);
				break;
			case CONTAINS:
			case DISJOINT:
			case EQUALS:
			case INTERSECTS:
			case OVERLAPS:
			case TOUCHES:
			case WITHIN:
				operator = buildBinaryOperator((AbstractBinarySpatialOperator)operatorConfig);
				break;
			case BEYOND:
			case DWITHIN:
				operator = buildDistanceOperator((AbstractDistanceOperator)operatorConfig);
				break;
			}
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build the spatial " + operatorConfig.getOperatorName() + " operator.", e);
		}

		return operator;
	}

	private BinarySpatialOperator buildBBOXOperator(BBOXOperator bboxConfig) throws FilterException, QueryBuildException {
		if (!bboxConfig.isSetEnvelope())
			throw new QueryBuildException("The bbox operator requires an " + GeometryType.ENVELOPE + " as spatial operand.");

		// build the value reference
		ValueReference valueReference = buildValueReference(bboxConfig);
				
		// convert the spatial operand
		GeometryObject spatialOperand = null;
		try {
			spatialOperand = spatialOperandParser.parseOperand(bboxConfig.getEnvelope());
		} catch (GeometryParseException e) {
			throw new QueryBuildException("Failed to parse the spatial reference system of bbox operator.", e);
		}

		return SpatialOperationFactory.bbox(valueReference, spatialOperand);
	}

	private BinarySpatialOperator buildBinaryOperator(AbstractBinarySpatialOperator operatorConfig) throws FilterException, QueryBuildException {
		if (!operatorConfig.isSetSpatialOperand())
			throw new QueryBuildException("No spatial operand provided for the binary spatial operator " + operatorConfig.getOperatorName() + ".");

		// build the value reference
		ValueReference valueReference = buildValueReference(operatorConfig);

		// convert the spatial operand
		GeometryObject spatialOperand = null;
		try {
			spatialOperand = spatialOperandParser.parseOperand(operatorConfig.getSpatialOperand());
		} catch (GeometryParseException e) {
			throw new QueryBuildException("Failed to parse the spatial operand of the binary spatial operator " + operatorConfig.getOperatorName() + ".", e);
		}

		switch (operatorConfig.getOperatorName()) {
		case CONTAINS:
			return SpatialOperationFactory.contains(valueReference, spatialOperand);
		case DISJOINT:
			return SpatialOperationFactory.disjoint(valueReference, spatialOperand);
		case EQUALS:
			return SpatialOperationFactory.equals(valueReference, spatialOperand);
		case INTERSECTS:
			return SpatialOperationFactory.intersects(valueReference, spatialOperand);
		case OVERLAPS:
			return SpatialOperationFactory.overlaps(valueReference, spatialOperand);
		case TOUCHES:
			return SpatialOperationFactory.touches(valueReference, spatialOperand);
		case WITHIN:
			return SpatialOperationFactory.within(valueReference, spatialOperand);
		default:
			throw new QueryBuildException("Failed to build the binary spatial operator " + operatorConfig.getOperatorName() + ".");
		}
	}

	private DistanceOperator buildDistanceOperator(AbstractDistanceOperator operatorConfig) throws FilterException, QueryBuildException {
		if (!operatorConfig.isSetSpatialOperand())
			throw new QueryBuildException("No spatial operand provided for the distance operator " + operatorConfig.getOperatorName() + ".");

		if (!operatorConfig.isSetDistance())
			throw new QueryBuildException("The distance operator " + operatorConfig.getOperatorName() + " lacks a distance measure.");

		// build the value reference
		ValueReference valueReference = buildValueReference(operatorConfig);

		// convert the spatial operand
		GeometryObject spatialOperand = null;
		try {
			spatialOperand = spatialOperandParser.parseOperand(operatorConfig.getSpatialOperand());
		} catch (GeometryParseException e) {
			throw new QueryBuildException("Failed to parse the spatial operand of the distance operator " + operatorConfig.getOperatorName() + ".", e);
		}

		// build the distance measure
		DistanceUnit unit = operatorConfig.getDistance().isSetUom() ? DistanceUnit.fromSymbol(operatorConfig.getDistance().getUom()) : DistanceUnit.METER;
		Distance distance = new Distance(operatorConfig.getDistance().getValue(), unit);

		switch (operatorConfig.getOperatorName()) {
		case BEYOND:
			return SpatialOperationFactory.beyond(valueReference, spatialOperand, distance);
		case DWITHIN:
			return SpatialOperationFactory.dWithin(valueReference, spatialOperand, distance);
		default:
			throw new QueryBuildException("Failed to build the distance operator " + operatorConfig.getOperatorName() + ".");
		}
	}

	private ValueReference buildValueReference(org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator operatorConfig) throws QueryBuildException {
		ValueReference valueReference = null;

		try {
			if (operatorConfig.isSetValueReference()) {
				FeatureType featureType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
				SchemaPath path = xPathParser.parse(operatorConfig.getValueReference(), featureType, namespaceContext);
				valueReference = new ValueReference(path);

				if (valueReference.getTarget().getElementType() != PathElementType.GEOMETRY_PROPERTY)
					throw new QueryBuildException("The value reference of the spatial operator " + operatorConfig.getOperatorName() + " must point to a geometry property.");
				
				// reset XPath expression using default namespace prefixes
				operatorConfig.setValueReference(path.toXPath());
			}

			else {
				FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());	
				SchemaPath path = new SchemaPath(superType);	
				path.appendChild(superType.getProperty("boundedBy", GMLCoreModule.v3_1_1.getNamespaceURI(), true));
				valueReference = new ValueReference(path);
			}
		} catch (XPathException | InvalidSchemaPathException e) {
			throw new QueryBuildException("Failed to parse the value reference " + operatorConfig.getValueReference() + ".", e);
		}

		return valueReference;
	}
}
