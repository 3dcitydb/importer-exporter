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
package de.tub.citydb.plugins.matching_merging.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BuildingFilterType", propOrder={
		"lodProjection",
		"overlap",
		"lodGeometry"
		})
public class BuildingFilter {
	@XmlElement(required=true)
	private int lodProjection = 2;
	@XmlElement(required=true)
	private double overlap = 0.8f;
	@XmlElement(required=true)
	private int lodGeometry = 3;
	
	public BuildingFilter() {
	}
	
	public BuildingFilter(int lodProjection, float overlap, int lodGeometry) {
		this.lodProjection = lodProjection;
		this.overlap = overlap;
		this.lodGeometry = lodGeometry;
	}
	
	public int getLodProjection() {
		return lodProjection;
	}
	
	public void setLodProjection(int lodProjection) {
		if (lodProjection >= 1 && lodProjection <= 4)
			this.lodProjection = lodProjection;
	}	
	
	public double getOverlap() {
		return overlap;
	}
	
	public void setOverlap(double overlap) {
		if (overlap >= 0.0 && overlap <= 1.0)
			this.overlap = overlap;
	}
	
	public int getLodGeometry() {
		return lodGeometry;
	}
	
	public void setLodGeometry(int lodGeometry) {
		if (lodGeometry >= 1 && lodGeometry <= 4)
			this.lodGeometry = lodGeometry;
	}
	
}
