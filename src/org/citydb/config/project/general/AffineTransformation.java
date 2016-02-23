/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.config.project.general;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AffineTransformationType", propOrder={
		"transformationMatrix"
})
public class AffineTransformation {
	@XmlAttribute(required=true)
	private Boolean useAffineTransformation = false;
	private TransformationMatrix transformationMatrix;

	public AffineTransformation() {
		transformationMatrix = new TransformationMatrix();
	}

	public boolean isSetUseAffineTransformation() {
		if (useAffineTransformation != null)
			return useAffineTransformation.booleanValue();

		return false;
	}

	public Boolean getUseAffineTransformation() {
		return useAffineTransformation;
	}

	public void setUseAffineTransformation(Boolean useAffineTransformation) {
		this.useAffineTransformation = useAffineTransformation;
	}

	public TransformationMatrix getTransformationMatrix() {
		return transformationMatrix;
	}

	public void setTransformationMatrix(TransformationMatrix transformationMatrix) {
		if (transformationMatrix != null)
			this.transformationMatrix = transformationMatrix;
	}
	
	public boolean isSetTransformationMatrix() {
		return transformationMatrix != null;
	}
	
}
