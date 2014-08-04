package org.citydb.api.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.api.geometry.GeometryObject;

public interface DatabaseGeometryConverter {
	public GeometryObject getEnvelope(Object geomObj) throws SQLException;
	public GeometryObject getPoint(Object geomObj) throws SQLException;
	public GeometryObject getMultiPoint(Object geomObj) throws SQLException;
	public GeometryObject getCurve(Object geomObj) throws SQLException;
	public GeometryObject getMultiCurve(Object geomObj) throws SQLException;
	public GeometryObject getPolygon(Object geomObj) throws SQLException;
	public GeometryObject getMultiPolygon(Object geomObj) throws SQLException;
	public GeometryObject getGeometry(Object geomObj) throws SQLException;
	public Object getDatabaseObject(GeometryObject geomObj, Connection connection) throws SQLException;
	public int getNullGeometryType();
	public String getNullGeometryTypeName();
}
