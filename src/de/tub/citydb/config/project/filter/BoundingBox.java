/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.database.ReferenceSystem;

@XmlType(name="BoundingBoxType", propOrder={
		"mode",
		"srs",
		"lowerLeftCorner",
		"upperRightCorner"		
})
public class BoundingBox {
	@XmlElement(required=true)
	private BoundingBoxMode mode = BoundingBoxMode.OVERLAP;
	@XmlIDREF
	private ReferenceSystem srs = ReferenceSystem.DEFAULT;
	private BoundingBoxPoint lowerLeftCorner;
	private BoundingBoxPoint upperRightCorner;
	@XmlAttribute(required=true)
	private Boolean active = false;

	public BoundingBox() {
		lowerLeftCorner = new BoundingBoxPoint();
		upperRightCorner = new BoundingBoxPoint();
	}

	public boolean isSetContainMode() {
		return mode == BoundingBoxMode.CONTAIN;
	}

	public boolean isSetOverlapMode() {
		return mode == BoundingBoxMode.OVERLAP;
	}

	public BoundingBoxMode getMode() {
		return mode;
	}

	public void setMode(BoundingBoxMode mode) {
		this.mode = mode;
	}

	public BoundingBoxPoint getLowerLeftCorner() {
		return lowerLeftCorner;
	}

	public void setLowerLeftCorner(BoundingBoxPoint lowerLeftCorner) {
		if (lowerLeftCorner != null)
			this.lowerLeftCorner = lowerLeftCorner;
	}

	public BoundingBoxPoint getUpperRightCorner() {
		return upperRightCorner;
	}

	public void setUpperRightCorner(BoundingBoxPoint upperRightCorner) {
		if (upperRightCorner != null)
			this.upperRightCorner = upperRightCorner;
	}

	public boolean isSet() {
		if (active != null)
			return active.booleanValue();

		return false;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setSRS(ReferenceSystem srs) {
		if (srs != null)
			this.srs = srs;
	}

	public ReferenceSystem getSRS() {
		return srs;
	}

}
