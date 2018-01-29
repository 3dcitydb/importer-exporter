package org.citydb.query.filter.selection;

import java.util.EnumSet;
import java.util.Iterator;

import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.logical.AbstractLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;
import org.citydb.query.filter.selection.operator.logical.NotOperator;
import org.citydb.query.filter.selection.operator.spatial.AbstractSpatialOperator;
import org.citydb.query.filter.selection.operator.spatial.BinarySpatialOperator;
import org.citydb.query.filter.selection.operator.spatial.DistanceOperator;
import org.citydb.query.filter.selection.operator.spatial.SpatialOperatorName;
import org.citygml4j.model.module.gml.GMLCoreModule;

public class SelectionFilter {
	private Predicate predicate;

	public SelectionFilter(Predicate predicate) {
		this.predicate = predicate;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	public boolean isSetPredicate() {
		return predicate != null;
	}

	public void setPredicate(Predicate predicate) {
		this.predicate = predicate;
	}

	public boolean containsSpatialOperators() {
		return containsOperator(predicate, EnumSet.of(PredicateName.SPATIAL_OPERATOR));
	}

	private boolean containsOperator(Predicate predicate, EnumSet<PredicateName> predicateNames) {
		if (predicateNames.contains(predicate.getPredicateName()))
			return true;

		if (predicate.getPredicateName() == PredicateName.LOGICAL_OPERATOR) {
			if (((AbstractLogicalOperator)predicate).getOperatorName() == LogicalOperatorName.NOT)
				return containsOperator(((NotOperator)predicate).getOperand(), predicateNames);
			else {
				BinaryLogicalOperator binaryLogicalOperator = (BinaryLogicalOperator)predicate;

				for (Predicate operand : binaryLogicalOperator.getOperands()) {
					if (containsOperator(operand, predicateNames))
						return true;
				}
			}
		}

		return false;
	}

	public Predicate getGenericSpatialFilter(FeatureType featureType) throws FilterException {
		ValueReference valueReference = null;
		try {
			SchemaPath schemaPath = new SchemaPath(featureType);
			schemaPath.appendChild(featureType.getProperty("boundedBy", GMLCoreModule.v3_1_1.getNamespaceURI(), true));
			valueReference = new ValueReference(schemaPath);
		} catch (InvalidSchemaPathException e) {
			throw new FilterException("Failed to build schema path.", e);
		}

		Predicate predicate = this.predicate.copy();
		return reduceToGenericSpatialFilter(predicate, valueReference) ? predicate : null;
	}

	private boolean reduceToGenericSpatialFilter(Predicate predicate, ValueReference valueReference) throws FilterException {
		switch (predicate.getPredicateName()) {
		case COMPARISON_OPERATOR:
		case ID_OPERATOR:
			return false;
		case SPATIAL_OPERATOR:
			return reduceToGenericSpatialFilter((AbstractSpatialOperator)predicate, valueReference);
		case LOGICAL_OPERATOR:
			return reduceToGenericSpatialFilter((AbstractLogicalOperator)predicate, valueReference);
		}

		return false;
	}

	private boolean reduceToGenericSpatialFilter(AbstractLogicalOperator logicalOperator, ValueReference valueReference) throws FilterException {
		if (logicalOperator.getOperatorName() == LogicalOperatorName.NOT) {
			return reduceToGenericSpatialFilter(((NotOperator)logicalOperator).getOperand(), valueReference);
		} else {
			BinaryLogicalOperator binaryLogicalOperator = (BinaryLogicalOperator)logicalOperator;

			Iterator<Predicate> iter = binaryLogicalOperator.getOperands().iterator();
			while (iter.hasNext()) {
				if (!reduceToGenericSpatialFilter(iter.next(), valueReference))
					iter.remove();
			}

			if (!binaryLogicalOperator.getOperands().isEmpty())
				return true;
		}	

		return false;
	}

	private boolean reduceToGenericSpatialFilter(AbstractSpatialOperator spatialOperator, ValueReference valueReference) throws FilterException {
		if (SpatialOperatorName.BINARY_SPATIAL_OPERATORS.contains(spatialOperator.getOperatorName()))
			((BinarySpatialOperator)spatialOperator).setLeftOperand(valueReference);
		else if (SpatialOperatorName.DISTANCE_OPERATORS.contains(spatialOperator.getOperatorName()))
			((DistanceOperator)spatialOperator).setLeftOperand(valueReference);

		return true;
	}
}
