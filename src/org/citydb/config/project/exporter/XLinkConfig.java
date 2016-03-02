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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="XLinkConfigType", propOrder={
		"mode",
		"idPrefix",
		"appendId"
		})
public class XLinkConfig {
	@XmlElement(name="multipleRepresentationMode", required=true)
	private XLinkMode mode = XLinkMode.XLINK;
	@XmlElement(defaultValue="UUID_")
	private String idPrefix = "UUID_";
	@XmlElement(defaultValue="false")
	private Boolean appendId = false;
	
	public XLinkConfig() {
	}

	public boolean isModeXLink() {
		return mode == XLinkMode.XLINK;
	}
	
	public boolean isModeCopy() {
		return mode == XLinkMode.COPY;
	}
	
	public XLinkMode getMode() {
		return mode;
	}

	public void setMode(XLinkMode mode) {
		this.mode = mode;
	}

	public String getIdPrefix() {
		return idPrefix;
	}

	public void setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
	}

	public boolean isSetAppendId() {
		if (appendId != null)
			return appendId.booleanValue();
		
		return false;
	}

	public Boolean getAppendId() {
		return appendId;
	}

	public void setAppendId(Boolean appendId) {
		this.appendId = appendId;
	}
	
}
