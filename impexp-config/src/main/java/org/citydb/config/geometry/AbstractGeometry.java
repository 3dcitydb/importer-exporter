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
package org.citydb.config.geometry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.database.DatabaseSrs;

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
	@XmlAttribute
	private String srsName;
	
	public abstract boolean is3D();
	public abstract boolean isValid();
	public abstract GeometryType getGeometryType();
	public abstract BoundingBox toBoundingBox();
	
	public DatabaseSrs getSrs() {
		return srs;
	}

	public boolean isSetSrs() {
		return srs != null;
	}

	public void setSrs(DatabaseSrs srs) {
		this.srs = srs;
	}
	
	public String getSrsName() {
		return srsName;
	}
	
	public boolean isSetSrsName() {
		return srsName != null;
	}
	
	public void setSrsName(String srsName) {
		this.srsName = srsName;
	}
	
	public void unsetSrsName() {
		srsName = null;
	}
	
}
