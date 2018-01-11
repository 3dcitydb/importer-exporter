package org.citydb.query.filter.selection.operator.spatial;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

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
