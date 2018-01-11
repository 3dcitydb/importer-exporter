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

import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "PositionType")
public class Position {	
	@XmlValue
	@XmlList
    private Double[] coords;
	
	public Position() {
		coords = new Double[3];
	}
	
	public Position(Double x, Double y) {
		this();
		coords[0] = x;
		coords[1] = y;
	}
	
	public Position(Double x, Double y, Double z) {
		this();
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}
	
	public Position(Double value, int dimension) {
		this();
		
		if (dimension < 2 || dimension > 3)
			throw new IllegalArgumentException("Dimension must be 2 or 3.");
		
		coords[0] = coords[1] = value;
		if (dimension == 3)
			coords[2] =value;
	}

	public Double getX() {
		prepareCoords();
		return coords[0];
	}
	
	public boolean isSetX() {
		return isValid() && coords[0] != null;
	}

	public void setX(Double x) {
		prepareCoords();
		coords[0] = x;
	}

	public Double getY() {
		prepareCoords();
		return coords[1];
	}
	
	public boolean isSetY() {
		return isValid() && coords[1] != null;
	}

	public void setY(Double y) {
		prepareCoords();
		coords[1] = y;
	}
	
	public Double getZ() {
		prepareCoords();
		return coords[2];
	}
	
	public boolean isSetZ() {
		return isValid() && coords.length > 2 && coords[2] != null;
	}

	public void setZ(Double z) {
		prepareCoords();
		coords[2] = z;
	}
	
	public boolean is3D() {
		return isSetZ();
	}
	
	public boolean isValid() {
		return coords != null && coords.length > 1;
	}
	
	private void prepareCoords() {
		if (!isValid())
			coords = new Double[3];
	}
	
}
