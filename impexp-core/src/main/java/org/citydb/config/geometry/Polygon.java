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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="polygon")
@XmlType(name="PolygonType", propOrder={
		"exterior",
		"interior"
})
public class Polygon extends AbstractGeometry {
	@XmlElement(required=true)
    private PositionList exterior;
	@XmlElement(required=false)
	private List<PositionList> interior;
	
	public Polygon() {
		exterior = new PositionList();
	}
	
	public PositionList getExterior() {
		return exterior;
	}

	public void setExterior(PositionList exterior) {
		this.exterior = exterior;
	}

	public List<PositionList> getInterior() {
		return interior;
	}
	
	public boolean isSetInterior() {
		return interior != null;
	}

	public void setInterior(List<PositionList> interior) {
		this.interior = interior;
	}

	@Override
	public BoundingBox toBoundingBox() {
		int dim = is3D() ? 3 : 2;
		BoundingBox bbox = new BoundingBox(new Position(Double.MAX_VALUE, dim), new Position(-Double.MAX_VALUE, dim));
		
		List<Double> coords = exterior.getCoords();
		for (int i = 0; i < coords.size(); i += dim) {
			if (coords.get(i) < bbox.getLowerCorner().getX())
				bbox.getLowerCorner().setX(coords.get(i));
			else if (coords.get(i) > bbox.getUpperCorner().getX())
				bbox.getUpperCorner().setX(coords.get(i));
			
			if (coords.get(i + 1) < bbox.getLowerCorner().getY())
				bbox.getLowerCorner().setY(coords.get(i + 1));
			else if (coords.get(i + 1) > bbox.getUpperCorner().getY())
				bbox.getUpperCorner().setY(coords.get(i + 1));
			
			if (dim == 3) {
				if (coords.get(i + 2) < bbox.getLowerCorner().getZ())
					bbox.getLowerCorner().setZ(coords.get(i + 2));
				else if (coords.get(i + 2) > bbox.getUpperCorner().getZ())
					bbox.getUpperCorner().setZ(coords.get(i + 2));
			}
		}
		
		return bbox;
	}

	@Override
	public boolean is3D() {
		if (isValid()) {
			if (exterior.getDimension() != 3)
				return false;
			
			if (interior != null) {
				for (PositionList tmp : interior) {
					if (tmp.getDimension() != 3)
						return false;
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isValid() {
		if (exterior != null && exterior.isValid()) {
			if (interior != null) {
				for (PositionList tmp : interior) {
					if (!tmp.isValid())
						return false;
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public GeometryType getGeometryType() {
		return GeometryType.POLYGON;
	}

}
