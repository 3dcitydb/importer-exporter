/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;

@XmlType(name="XLinkConfigType", propOrder={
		"mode",
		"idPrefix",
		"appendId"
		})
public class XLinkConfig {
	@XmlElement(name="multipleRepresentationMode", required=true)
	private XLinkMode mode = XLinkMode.XLINK;
	@XmlElement(defaultValue="UUID_")
	private String idPrefix = DefaultGMLIdManager.getInstance().getDefaultPrefix();
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
