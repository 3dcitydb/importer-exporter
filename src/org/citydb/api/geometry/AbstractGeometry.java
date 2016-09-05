package org.citydb.api.geometry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;

@XmlType(name="AbstractGeometryType")
@XmlSeeAlso({
	BoundingBox.class,
	Point.class,
	MultiPoint.class,
	LineString.class,
	MultiLineString.class,
	Polygon.class,
	MultiPolygon.class
})
public abstract class AbstractGeometry {
	@XmlIDREF
	@XmlAttribute(name="srsRef", required=false)
	private DatabaseSrs srs;
	
	public abstract boolean is3D();
	public abstract boolean isValid();
	public abstract GeometryType getGeometryType();
	
	public DatabaseSrs getSrs() {
		return srs;
	}

	public boolean isSetSrs() {
		return srs != null;
	}

	public void setSrs(DatabaseSrs srs) {
		this.srs = srs;
	}
}
