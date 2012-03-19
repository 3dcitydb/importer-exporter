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
