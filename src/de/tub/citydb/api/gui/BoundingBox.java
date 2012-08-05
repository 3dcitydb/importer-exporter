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
package de.tub.citydb.api.gui;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.api.database.DatabaseSrs;

@XmlType(name="BoundingBoxType", propOrder={
		"srs",
		"lowerLeft",
		"upperRight"		
})
public class BoundingBox {
	@XmlIDREF
	private DatabaseSrs srs;
	private BoundingBoxCorner lowerLeft;
	private BoundingBoxCorner upperRight;

	public BoundingBox(BoundingBoxCorner lowerleft, BoundingBoxCorner upperRight) {
		this.lowerLeft = lowerleft;
		this.upperRight = upperRight;
	}
	
	public BoundingBox(BoundingBoxCorner lowerleft, BoundingBoxCorner upperRight, DatabaseSrs srs) {
		this(lowerleft, upperRight);
		this.srs = srs;
	}
	
	public BoundingBox() {
		this(new BoundingBoxCorner(), new BoundingBoxCorner());
	}

	public BoundingBox(BoundingBox bbox) {
		copyFrom(bbox);
	}
	
	public BoundingBoxCorner getLowerLeftCorner() {
		return lowerLeft;
	}

	public void setLowerLeftCorner(BoundingBoxCorner lowerLeft) {
		if (lowerLeft != null)
			this.lowerLeft = lowerLeft;
	}
	
	public BoundingBoxCorner getUpperRightCorner() {
		return upperRight;
	}

	public void setUpperRightCorner(BoundingBoxCorner upperRight) {
		if (upperRight != null)
			this.upperRight = upperRight;
	}

	public boolean isSetSrs() {
		return srs != null;
	}
	
	public void setSrs(DatabaseSrs srs) {
		if (srs != null)
			this.srs = srs;
	}

	public DatabaseSrs getSrs() {
		return srs;
	}
	
	public void copyFrom(BoundingBox other) {
		srs = other.srs;
		lowerLeft = new BoundingBoxCorner(other.getLowerLeftCorner().getX(), other.getLowerLeftCorner().getY());
		upperRight = new BoundingBoxCorner(other.getUpperRightCorner().getX(), other.getUpperRightCorner().getY());
	}

}
