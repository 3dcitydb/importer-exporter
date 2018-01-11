package org.citydb.query.filter.selection.operator.spatial;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;

public class SpatialOperationFactory {

	public static BinarySpatialOperator bbox(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.BBOX, spatialDescription);
	}
	
	public static BinarySpatialOperator bbox(GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(null, SpatialOperatorName.BBOX, spatialDescription);
	}
	
	public static BinarySpatialOperator equals(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.EQUALS, spatialDescription);
	}
	
	public static BinarySpatialOperator disjoint(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.DISJOINT, spatialDescription);
	}
	
	public static BinarySpatialOperator touches(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.TOUCHES, spatialDescription);
	}
	
	public static BinarySpatialOperator within(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.WITHIN, spatialDescription);
	}
	
	public static BinarySpatialOperator overlaps(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.OVERLAPS, spatialDescription);
	}
	
	public static BinarySpatialOperator intersects(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.INTERSECTS, spatialDescription);
	}
	
	public static BinarySpatialOperator contains(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
		return new BinarySpatialOperator(leftOperand, SpatialOperatorName.CONTAINS, spatialDescription);
	}
	
	public static DistanceOperator dWithin(Expression leftOperand, GeometryObject spatialDescription, Distance distance) throws FilterException {
		return new DistanceOperator(leftOperand, SpatialOperatorName.DWITHIN, spatialDescription, distance);
	}
	
	public static DistanceOperator beyond(Expression leftOperand, GeometryObject spatialDescription, Distance distance) throws FilterException {
		return new DistanceOperator(leftOperand, SpatialOperatorName.BEYOND, spatialDescription, distance);
	}
	
}
