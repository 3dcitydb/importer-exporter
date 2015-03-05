/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.api.geometry;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.citydb.api.database.DatabaseSrs;

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
