/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.Arrays;

@XmlType(name = "PositionType")
public class Position {
	@XmlValue
	@XmlList
	private Double[] coords;

	@XmlTransient
	private int dimension;

	private Position(int dimension) {
		if (dimension < 2 || dimension > 3)
			throw new IllegalArgumentException("Dimension must be 2 or 3.");

		this.dimension = dimension;
		coords = new Double[3];
	}

	public Position() {
		this(2);
	}

	public Position(Double x, Double y) {
		this(2);
		coords[0] = x;
		coords[1] = y;
	}

	public Position(Double x, Double y, Double z) {
		this(3);
		coords[0] = x;
		coords[1] = y;
		coords[2] = z;
	}

	public Position(Double value, int dimension) {
		this(dimension);
		for (int i = 0; i < coords.length; i++)
			coords[i] = value;
	}

	public Double getX() {
		prepareCoords(false);
		return coords[0];
	}

	public boolean isSetX() {
		return coords.length == 3 && coords[0] != null;
	}

	public void setX(Double x) {
		prepareCoords(false);
		coords[0] = x;
	}

	public Double getY() {
		prepareCoords(false);
		return coords[1];
	}

	public boolean isSetY() {
		return coords.length == 3 && coords[1] != null;
	}

	public void setY(Double y) {
		prepareCoords(false);
		coords[1] = y;
	}

	public Double getZ() {
		prepareCoords(true);
		return coords[2];
	}

	public boolean isSetZ() {
		return coords.length == 3 && dimension == 3 && coords[2] != null;
	}

	public void setZ(Double z) {
		prepareCoords(true);
		coords[2] = z;
		dimension = z != null ? 3 : 2;
	}

	public boolean is3D() {
		return isSetZ();
	}

	public boolean isValid() {
		if (coords != null && coords.length > 1) {
			for (int i = 0; i < dimension; i++) {
				if (coords[i] == null)
					return false;
			}

			return true;
		}

		return false;
	}

	private void prepareCoords(boolean extent) {
		if (coords.length < 2 || (extent && coords.length == 2)) {
			dimension = 2;
			coords = Arrays.copyOf(coords, 3);
		}
	}
}