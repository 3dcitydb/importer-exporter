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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="XMLValidationType", propOrder={
		"useXMLValidation",
		"reportOneErrorPerFeature"
})
public class XMLValidation {
	@XmlElement(required=true, defaultValue="false")
	private Boolean useXMLValidation = false;
	@XmlElement(defaultValue="false")
	private Boolean reportOneErrorPerFeature = false;

	public XMLValidation() {
	}

	public boolean isSetUseXMLValidation() {
		if (useXMLValidation != null)
			return useXMLValidation.booleanValue();

		return false;
	}

	public Boolean getUseXMLValidation() {
		return useXMLValidation;
	}

	public void setUseXMLValidation(Boolean useXMLValidation) {
		this.useXMLValidation = useXMLValidation;
	}

	public boolean isSetReportOneErrorPerFeature() {
		if (reportOneErrorPerFeature != null)
			return reportOneErrorPerFeature.booleanValue();
		
		return false;
	}
	
	public Boolean getReportOneErrorPerFeature() {
		return reportOneErrorPerFeature;
	}

	public void setReportOneErrorPerFeature(Boolean reportOneErrorPerFeature) {
		this.reportOneErrorPerFeature = reportOneErrorPerFeature;
	}
	
}
