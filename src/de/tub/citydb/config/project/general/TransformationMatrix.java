/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.general;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.citygml4j.geometry.Matrix;

@XmlType(name="TransformationMatrixType", propOrder={
		"value"
})
public class TransformationMatrix {
	@XmlValue
	private List<Double> value;

	public TransformationMatrix() {
		value = Matrix.identity(3, 4).toRowPackedList();
	}

	public List<Double> getValue() {
		if (value == null)
			value = new ArrayList<Double>();

		return value;
	}

	public void setValue(List<Double> value) {
		this.value = value;
	}

	public boolean isSetValue() {
		return value != null && !value.isEmpty();
	}

	public Matrix toMatrix3x4() {
		Matrix matrix = null;

		if (isSetValue() && value.size() == 12)
			matrix = new Matrix(value, 3);
		else {
			matrix = Matrix.identity(3, 4);
			value = matrix.toRowPackedList();
		}

		return matrix;
	}
	
	public Matrix toMatrix4x4() {
		Matrix matrix = new Matrix(4, 4);
		matrix.setMatrix(0, 2, 0, 3, toMatrix3x4());
		
		matrix.set(3, 0, 0);
		matrix.set(3, 1, 0);
		matrix.set(3, 2, 0);
		matrix.set(3, 3, 1);
		
		return matrix;
	}

}
