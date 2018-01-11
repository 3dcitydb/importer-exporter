/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="multiPolygon")
@XmlType(name="MultiPolygonType", propOrder={
		"polygons"
})
public class MultiPolygon extends AbstractGeometry {
	@XmlElement(name="polygon", required=true)
	private List<Polygon> polygons;
	
	public MultiPolygon() {
		polygons = new ArrayList<>();
	}

	public List<Polygon> getPolygons() {
		return polygons;
	}

	public void setPolygons(List<Polygon> polygons) {
		this.polygons = polygons;
	}
	
	@Override
	public BoundingBox toBoundingBox() {
		int dim = is3D() ? 3 : 2;
		BoundingBox bbox = new BoundingBox(new Position(Double.MAX_VALUE, dim), new Position(-Double.MAX_VALUE, dim));
		
		for (Polygon polygon : polygons)
			bbox.update(polygon.toBoundingBox());
		
		return bbox;
	}

	@Override
	public boolean is3D() {
		if (isValid()) {
			for (Polygon polygon : polygons) {
				if (!polygon.is3D())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		if (polygons != null && !polygons.isEmpty()) {
			for (Polygon polygon : polygons) {
				if (!polygon.isValid())
					return false;
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.MULTI_POLYGON;
	}
	
	
}
