package de.tub.citydb.db.exporter;

import org.citygml4j.model.gml.AbstractGeometry;
import org.citygml4j.model.gml.GMLClass;

public class DBSurfaceGeometryResult {
	private AbstractGeometry abstractGeometry;
	private String target;
	private GMLClass type;

	public DBSurfaceGeometryResult(AbstractGeometry abstractGeometry) {
		this.abstractGeometry = abstractGeometry;
		type = abstractGeometry.getGMLClass();
	}

	public DBSurfaceGeometryResult(String target, GMLClass type) {
		this.target = target;
		this.type = type;
	}

	public AbstractGeometry getAbstractGeometry() {
		return abstractGeometry;
	}

	public String getTarget() {
		return target;
	}

	public GMLClass getType() {
		return type;
	}
}
