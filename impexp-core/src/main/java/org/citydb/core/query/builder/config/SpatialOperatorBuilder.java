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
package org.citydb.core.query.builder.config;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.config.project.query.filter.selection.spatial.AbstractBinarySpatialOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractDistanceOperator;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.util.ValueReferenceBuilder;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.Predicate;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.selection.operator.spatial.*;
import org.citydb.core.query.geometry.GeometryParseException;
import org.citydb.core.query.geometry.config.SpatialOperandParser;

public class SpatialOperatorBuilder {
	private final ValueReferenceBuilder valueReferenceBuilder;
	private final SpatialOperandParser spatialOperandParser;

	protected SpatialOperatorBuilder(ValueReferenceBuilder valueReferenceBuilder, AbstractDatabaseAdapter databaseAdapter) {
		this.valueReferenceBuilder = valueReferenceBuilder;
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
		ValueReference valueReference = valueReferenceBuilder.buildValueReference(bboxConfig);
				
		// convert the spatial operand
		GeometryObject spatialOperand = null;
		try {
			spatialOperand = spatialOperandParser.parseOperand(bboxConfig.getEnvelope());
		} catch (GeometryParseException e) {
			throw new QueryBuildException("Failed to parse the envelope of the bbox operator.", e);
		}

		return SpatialOperationFactory.bbox(valueReference, spatialOperand);
	}

	private BinarySpatialOperator buildBinaryOperator(AbstractBinarySpatialOperator operatorConfig) throws FilterException, QueryBuildException {
		if (!operatorConfig.isSetSpatialOperand())
			throw new QueryBuildException("No spatial operand provided for the binary spatial operator " + operatorConfig.getOperatorName() + ".");

		// build the value reference
		ValueReference valueReference = valueReferenceBuilder.buildValueReference(operatorConfig);

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
		ValueReference valueReference = valueReferenceBuilder.buildValueReference(operatorConfig);

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
}
