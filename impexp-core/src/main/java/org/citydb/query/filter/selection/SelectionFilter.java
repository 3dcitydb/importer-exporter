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
package org.citydb.query.filter.selection;

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

import java.util.EnumSet;
import java.util.Iterator;

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
			case SPATIAL_OPERATOR:
				return reduceToGenericSpatialFilter((AbstractSpatialOperator) predicate, valueReference);
			case LOGICAL_OPERATOR:
				return reduceToGenericSpatialFilter((AbstractLogicalOperator) predicate, valueReference);
			default:
				return false;
		}
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

			return !binaryLogicalOperator.getOperands().isEmpty();
		}

	}

	private boolean reduceToGenericSpatialFilter(AbstractSpatialOperator spatialOperator, ValueReference valueReference) throws FilterException {
		if (SpatialOperatorName.BINARY_SPATIAL_OPERATORS.contains(spatialOperator.getOperatorName()))
			((BinarySpatialOperator)spatialOperator).setLeftOperand(valueReference);
		else if (SpatialOperatorName.DISTANCE_OPERATORS.contains(spatialOperator.getOperatorName()))
			((DistanceOperator)spatialOperator).setLeftOperand(valueReference);

		return true;
	}
}
