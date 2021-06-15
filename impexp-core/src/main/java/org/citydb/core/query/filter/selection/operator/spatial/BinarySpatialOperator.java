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
import org.citydb.config.geometry.GeometryType;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.expression.Expression;
import org.citydb.core.query.filter.selection.expression.ExpressionName;
import org.citydb.core.query.filter.selection.expression.ValueReference;

public class BinarySpatialOperator extends AbstractSpatialOperator {
	private final SpatialOperatorName name;
	private Expression leftOperand;
	private GeometryObject spatialDescription;
	
	public BinarySpatialOperator(Expression leftOperand, SpatialOperatorName name, GeometryObject spatialDescription) throws FilterException {
		if (!SpatialOperatorName.BINARY_SPATIAL_OPERATORS.contains(name))
			throw new FilterException("Allowed binary spatial operators only include " + SpatialOperatorName.BINARY_SPATIAL_OPERATORS);
				
		this.name = name;
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
		if (name != SpatialOperatorName.BBOX && leftOperand == null)
			throw new FilterException("The binary spatial '" + name + "' operator requires a geometry property as operand.");

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
		if (name == SpatialOperatorName.BBOX && spatialDescription.getGeometryType() != GeometryType.ENVELOPE)
			throw new FilterException("The '" + name + "' operator may only be used with an envelope geometry as operand.");

		this.spatialDescription = spatialDescription;
	}

	@Override
	public SpatialOperatorName getOperatorName() {
		return name;
	}

	@Override
	public BinarySpatialOperator copy() throws FilterException {
		return new BinarySpatialOperator(leftOperand, name, spatialDescription);
	}
	
}
