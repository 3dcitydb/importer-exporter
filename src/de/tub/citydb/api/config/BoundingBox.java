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
package de.tub.citydb.api.config;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BoundingBoxType", propOrder={
		"srs",
		"lowerLeftCorner",
		"upperRightCorner"		
})
public class BoundingBox {
	@XmlIDREF
	private DatabaseSrs srs;
	private BoundingBoxCorner lowerLeftCorner;
	private BoundingBoxCorner upperRightCorner;

	public BoundingBox() {
		lowerLeftCorner = new BoundingBoxCorner();
		upperRightCorner = new BoundingBoxCorner();
	}
	
	public boolean isSetLowerLeftCorner() {
		return lowerLeftCorner != null;
	}

	public BoundingBoxCorner getLowerLeftCorner() {
		return lowerLeftCorner;
	}

	public void setLowerLeftCorner(BoundingBoxCorner lowerLeftCorner) {
		if (lowerLeftCorner != null)
			this.lowerLeftCorner = lowerLeftCorner;
	}

	public boolean isSetUpperRightCorner() {
		return upperRightCorner != null;
	}
	
	public BoundingBoxCorner getUpperRightCorner() {
		return upperRightCorner;
	}

	public void setUpperRightCorner(BoundingBoxCorner upperRightCorner) {
		if (upperRightCorner != null)
			this.upperRightCorner = upperRightCorner;
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
		lowerLeftCorner = new BoundingBoxCorner(other.getLowerLeftCorner().getX(), other.getLowerLeftCorner().getY());
		upperRightCorner = new BoundingBoxCorner(other.getUpperRightCorner().getX(), other.getUpperRightCorner().getY());
	}

}
