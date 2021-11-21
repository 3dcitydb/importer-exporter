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
package org.citydb.core.query.builder.sql;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.GeometryProperty;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.selection.expression.ExpressionName;
import org.citydb.core.query.filter.selection.expression.ValueReference;
import org.citydb.core.query.filter.selection.operator.spatial.*;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;
import org.citygml4j.model.module.gml.GMLCoreModule;
import org.geotools.measure.Units;
import org.geotools.referencing.util.CRSUtilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import si.uom.SI;

import javax.measure.Unit;
import javax.measure.UnitConverter;
import java.sql.SQLException;

public class SpatialOperatorBuilder {
	private final Query query;
	private final SchemaPathBuilder schemaPathBuilder;
	private final SchemaMapping schemaMapping;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final String schemaName;

	protected SpatialOperatorBuilder(Query query, SchemaPathBuilder schemaPathBuilder, SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter, String schemaName) {
		this.query = query;
		this.schemaPathBuilder = schemaPathBuilder;
		this.schemaMapping = schemaMapping;
		this.databaseAdapter = databaseAdapter;
		this.schemaName = schemaName;
	}

	protected void buildSpatialOperator(AbstractSpatialOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		switch (operator.getOperatorName()) {
		case BBOX:
		case EQUALS:
		case DISJOINT:
		case TOUCHES:
		case WITHIN:
		case OVERLAPS:
		case INTERSECTS:
		case CONTAINS:
			buildBinaryOperator((BinarySpatialOperator)operator, queryContext, negate, useLeftJoins);
			break;
		case DWITHIN:
		case BEYOND:
			buildDistanceOperator((DistanceOperator)operator, queryContext, negate, useLeftJoins);
			break;
		}

	}

	private void buildBinaryOperator(BinarySpatialOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		if (!SpatialOperatorName.BINARY_SPATIAL_OPERATORS.contains(operator.getOperatorName()))
			throw new QueryBuildException(operator.getOperatorName() + " is not a binary spatial operator.");

		ValueReference valueReference = null;
		GeometryObject spatialDescription = operator.getSpatialDescription();

		// we currently only support ValueReference as left operand	
		if (operator.getLeftOperand() != null && operator.getLeftOperand().getExpressionName() == ExpressionName.VALUE_REFERENCE)
			valueReference = (ValueReference)operator.getLeftOperand();

		if (valueReference == null && operator.getOperatorName() != SpatialOperatorName.BBOX)
			throw new QueryBuildException("The spatial " + operator.getOperatorName() + " operator requires a ValueReference pointing to the geometry property to be tested.");

		if (spatialDescription == null)
			throw new QueryBuildException("The spatial description may not be null.");

		if (valueReference == null)
			valueReference = getBoundedByProperty(query);

		// transform coordinate values if required
		DatabaseSrs targetSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();

		if (spatialDescription.getSrid() != targetSrs.getSrid()) {
			try {
				spatialDescription = databaseAdapter.getUtil().transform(spatialDescription, targetSrs);
			} catch (SQLException e) {
				throw new QueryBuildException("Failed to transform coordinates of test geometry.", e);
			}
		}

		// build the value reference and spatial predicate
		schemaPathBuilder.addSchemaPath(valueReference.getSchemaPath(), queryContext, useLeftJoins);
		Table toTable = queryContext.getToTable();
		Column targetColumn = queryContext.getTargetColumn();

		GeometryProperty property = (GeometryProperty)valueReference.getSchemaPath().getLastNode().getPathElement();
		if (property.isSetInlineColumn()) {
			PredicateToken predicate = databaseAdapter.getSQLAdapter().getBinarySpatialPredicate(operator.getOperatorName(), queryContext.getTargetColumn(), spatialDescription, negate);
			queryContext.addPredicate(predicate);
		} else {
			SpatialOperatorName operatorName = operator.getOperatorName();
			if (operatorName == SpatialOperatorName.CONTAINS || operatorName == SpatialOperatorName.EQUALS)
				throw new QueryBuildException("The spatial " + operatorName + " operator is not supported for the geometry property '" + valueReference.getSchemaPath().getLastNode() + "'.");

			if (operatorName == SpatialOperatorName.DISJOINT) {
				operatorName = SpatialOperatorName.INTERSECTS;
				negate = !negate;
			}

			GeometryObject bbox = spatialDescription.toEnvelope();
			boolean all = operatorName == SpatialOperatorName.WITHIN;
			Table surfaceGeometry = new Table(MappingConstants.SURFACE_GEOMETRY, schemaName, schemaPathBuilder.getAliasGenerator());
			Table cityObject = getCityObjectTable(query, queryContext, useLeftJoins);

			Select inner = new Select()
					.addProjection(surfaceGeometry.getColumn(MappingConstants.ID))
					.addSelection(ComparisonFactory.equalTo(surfaceGeometry.getColumn(MappingConstants.ROOT_ID), targetColumn))
					.addSelection(ComparisonFactory.isNotNull(surfaceGeometry.getColumn(MappingConstants.GEOMETRY)))
					.addSelection(databaseAdapter.getSQLAdapter().getBinarySpatialPredicate(operatorName, surfaceGeometry.getColumn(MappingConstants.GEOMETRY), spatialDescription, all));

			PredicateToken spatialPredicate = LogicalOperationFactory.AND(
					databaseAdapter.getSQLAdapter().getBinarySpatialPredicate(SpatialOperatorName.BBOX, cityObject.getColumn(MappingConstants.ENVELOPE), bbox, false),
					ComparisonFactory.exists(inner, all));

			if (negate)
				spatialPredicate = LogicalOperationFactory.NOT(spatialPredicate);

			queryContext.addPredicates(ComparisonFactory.isNotNull(targetColumn), spatialPredicate);
		}

		// add optimizer hint if required
		if (databaseAdapter.getSQLAdapter().spatialPredicateRequiresNoIndexHint()
				&& targetColumn.getName().equalsIgnoreCase(MappingConstants.ENVELOPE))
			queryContext.getSelect().addOptimizerHint("no_index(" + toTable.getAlias() + " cityobject_objectclass_fkx)");
	}

	private void buildDistanceOperator(DistanceOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		if (!SpatialOperatorName.DISTANCE_OPERATORS.contains(operator.getOperatorName()))
			throw new QueryBuildException(operator.getOperatorName() + " is not a distance operator.");

		ValueReference valueReference;
		GeometryObject spatialDescription = operator.getSpatialDescription();
		Distance distance = operator.getDistance();

		// we currently only support ValueReference as left operand	
		if (operator.getLeftOperand() != null && operator.getLeftOperand().getExpressionName() == ExpressionName.VALUE_REFERENCE)
			valueReference = (ValueReference)operator.getLeftOperand();
		else 
			valueReference = getBoundedByProperty(query);

		if (spatialDescription == null)
			throw new QueryBuildException("The spatial description may not be null.");

		// transform coordinate values if required
		DatabaseSrs targetSrs = databaseAdapter.getConnectionMetaData().getReferenceSystem();

		if (spatialDescription.getSrid() != targetSrs.getSrid()) {
			try {
				spatialDescription = databaseAdapter.getUtil().transform(spatialDescription, targetSrs);
			} catch (SQLException e) {
				throw new QueryBuildException("Failed to transform coordinates of test geometry.", e);
			}
		}

		// convert distance value into unit of srs
		Unit<?> srsUnit;
		DistanceUnit distanceUnit = distance.isSetUnit() ? distance.getUnit() : DistanceUnit.METER;
		UnitConverter converter;

		try {
			CoordinateReferenceSystem crs = databaseAdapter.getUtil().decodeDatabaseSrs(targetSrs);
			srsUnit = CRSUtilities.getUnit(crs.getCoordinateSystem());
		} catch (FactoryException e) {
			// assume meter per default
			srsUnit = SI.METRE;
		}

		try {
			converter = Units.getConverterToAny(distanceUnit.toUnit(), srsUnit);
		} catch (IllegalArgumentException e) {
			throw new QueryBuildException("Cannot convert from the unit '" + distanceUnit + "' to the unit of the database SRS.");
		}

		double value = converter.convert(distance.getValue());

		// build the value reference and spatial predicate
		schemaPathBuilder.addSchemaPath(valueReference.getSchemaPath(), queryContext, useLeftJoins);
		Table toTable = queryContext.getToTable();
		Column targetColumn = queryContext.getTargetColumn();

		GeometryProperty property = (GeometryProperty)valueReference.getSchemaPath().getLastNode().getPathElement();
		if (property.isSetInlineColumn()) {
			PredicateToken predicate = databaseAdapter.getSQLAdapter().getDistancePredicate(operator.getOperatorName(), queryContext.getTargetColumn(), spatialDescription, value, negate);
			queryContext.addPredicate(predicate);
		} else {
			SpatialOperatorName operatorName = operator.getOperatorName();
			if (operatorName == SpatialOperatorName.BEYOND) {
				operatorName = SpatialOperatorName.DWITHIN;
				negate = !negate;
			}

			// get bbox of query geometry and buffer it by the distance
			GeometryObject bbox = spatialDescription.toEnvelope();
			double[] coords = bbox.getCoordinates(0);
			coords[0] -= value;
			coords[1] -= value;
			coords[bbox.getDimension()] += value;
			coords[bbox.getDimension() + 1] += value;

			Table surfaceGeometry = new Table(MappingConstants.SURFACE_GEOMETRY, schemaName, schemaPathBuilder.getAliasGenerator());
			Table cityObject = getCityObjectTable(query, queryContext, useLeftJoins);

			Select inner = new Select()
					.addProjection(surfaceGeometry.getColumn(MappingConstants.ID))
					.addSelection(ComparisonFactory.equalTo(surfaceGeometry.getColumn(MappingConstants.ROOT_ID), targetColumn))
					.addSelection(ComparisonFactory.isNotNull(surfaceGeometry.getColumn(MappingConstants.GEOMETRY)))
					.addSelection(databaseAdapter.getSQLAdapter().getDistancePredicate(operatorName, surfaceGeometry.getColumn(MappingConstants.GEOMETRY), spatialDescription, value, false));

			PredicateToken spatialPredicate = LogicalOperationFactory.AND(
					databaseAdapter.getSQLAdapter().getBinarySpatialPredicate(SpatialOperatorName.BBOX, cityObject.getColumn(MappingConstants.ENVELOPE), bbox, false),
					ComparisonFactory.exists(inner, false));

			if (negate)
				spatialPredicate = LogicalOperationFactory.NOT(spatialPredicate);

			queryContext.addPredicates(ComparisonFactory.isNotNull(targetColumn), spatialPredicate);
		}

		// add optimizer hint if required
		if (databaseAdapter.getSQLAdapter().spatialPredicateRequiresNoIndexHint()
				&& targetColumn.getName().equalsIgnoreCase(MappingConstants.ENVELOPE))
			queryContext.getSelect().addOptimizerHint("no_index(" + toTable.getAlias() + " cityobject_objectclass_fkx)");
	}

	private ValueReference getBoundedByProperty(Query query) throws QueryBuildException {
		try {
			FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());	
			SchemaPath path = new SchemaPath(superType);	
			path.appendChild(superType.getProperty("boundedBy", GMLCoreModule.v3_1_1.getNamespaceURI(), true));
			return new ValueReference(path);
		} catch (InvalidSchemaPathException e) {
			throw new QueryBuildException(e.getMessage());
		}
	}

	private Table getCityObjectTable(Query query, SQLQueryContext queryContext, boolean useLeftJoins) throws QueryBuildException {
		SchemaPath schemaPath = getBoundedByProperty(query).getSchemaPath();
		schemaPathBuilder.addSchemaPath(schemaPath, queryContext, useLeftJoins);
		return queryContext.getToTable();
	}

}
