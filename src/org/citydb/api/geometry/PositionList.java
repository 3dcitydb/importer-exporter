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
package org.citydb.api.geometry;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "PositionListType")
public class PositionList {
	@XmlAttribute(required=false)
	private Integer dimension = 2;
	@XmlValue
    private DoubleList coords;
	
	public PositionList() {
		coords = new DoubleList();
	}

	public List<Double> getCoords() {
		return coords.getValues();
	}

	public void setCoords(List<Double> coords) {
		this.coords.setValues(coords);
	}
	
	public int getDimension() {
		return dimension != null ? dimension : 2;
	}

	public void setDimension(int dimension) {
		if (dimension < 2 || dimension > 3)
			throw new IllegalArgumentException("Dimension must be 2 or 3.");
		
		this.dimension = dimension;
	}

	public boolean isValid() {
		return coords.getValues() != null && !coords.getValues().isEmpty() && coords.getValues().size() % getDimension() == 0;
	}
	
}