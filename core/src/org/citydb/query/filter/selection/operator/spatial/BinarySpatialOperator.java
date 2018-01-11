package org.citydb.query.filter.selection.operator.spatial;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.GeometryType;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

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
