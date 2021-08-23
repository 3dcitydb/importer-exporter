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
package org.citydb.core.query.filter.selection.operator.spatial;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.expression.Expression;
import org.citydb.core.query.filter.selection.expression.ExpressionName;
import org.citydb.core.query.filter.selection.expression.ValueReference;

public class DistanceOperator extends AbstractSpatialOperator {
	private final SpatialOperatorName name;
	private Expression leftOperand;
	private GeometryObject spatialDescription;
	private Distance distance;
	
	public DistanceOperator(Expression leftOperand, SpatialOperatorName name, GeometryObject spatialDescription, Distance distance) throws FilterException {
		if (!SpatialOperatorName.DISTANCE_OPERATORS.contains(name))
			throw new FilterException("Allowed distance operators only include " + SpatialOperatorName.DISTANCE_OPERATORS);
				
		this.name = name;
		this.distance = distance;
		if (distance != null && distance.getValue() < 0)
			throw new FilterException("The distance value of the '" + name + "' operator may not be negative");
		
		setLeftOperand(leftOperand);
		setSpatialDescription(spatialDescription);
	}
	
	public boolean isSetLeftOperand() {
		return leftOperand != null;
	}
	
	public Expression getLeftOperand() {
		return leftOperand;
	}

	public void setLeftOperand(Expression leftOperand) throws FilterException {
		if (leftOperand != null && leftOperand.getExpressionName() == ExpressionName.VALUE_REFERENCE 
				&& ((ValueReference)leftOperand).getSchemaPath().getLastNode().getPathElement().getElementType() != PathElementType.GEOMETRY_PROPERTY)
			throw new FilterException("The value reference of a spatial comparison must point to a geometry property.");

		this.leftOperand = leftOperand;
	}

	public boolean isSetSpatialDescription() {
		return spatialDescription != null;
	}

	public GeometryObject getSpatialDescription() {
		return spatialDescription;
	}

	public void setSpatialDescription(GeometryObject spatialDescription) throws FilterException {
		this.spatialDescription = spatialDescription;
	}

	public boolean isSetDistance() {
		return distance != null;
	}

	public Distance getDistance() {
		return distance;
	}

	public void setDistance(Distance distance) {
		this.distance = distance;
	}

	@Override
	public SpatialOperatorName getOperatorName() {
		return name;
	}

	@Override
	public DistanceOperator copy() throws FilterException {
		return new DistanceOperator(leftOperand, name, spatialDescription, distance);
	}

}
