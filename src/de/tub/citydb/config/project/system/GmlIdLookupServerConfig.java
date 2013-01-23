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
package de.tub.citydb.config.project.system;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GmlIdLookupServerConfigType", propOrder={
		"cacheSize",
		"pageFactor",
		"partitions"
})
public class GmlIdLookupServerConfig {
	@XmlSchemaType(name="positiveInteger")
	@XmlElement(required=true, defaultValue="200000")
	private Integer cacheSize = 200000;
	@XmlElement(required=true, defaultValue="0.85")
	private Float pageFactor = 0.85f;
	@XmlElement(required=true, defaultValue="50")
	private Integer partitions = 10;
	
	public GmlIdLookupServerConfig() {
	}

	public Integer getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(Integer cacheSize) {
		if (cacheSize != null && cacheSize > 0)
			this.cacheSize = cacheSize;
	}

	public Float getPageFactor() {
		return pageFactor;
	}

	public void setPageFactor(Float pageFactor) {
		if (pageFactor != null && pageFactor > 0 && pageFactor <= 1)
			this.pageFactor = pageFactor;
	}

	public Integer getPartitions() {
		return partitions;
	}

	public void setPartitions(Integer concurrentTempTables) {
		if (concurrentTempTables != null && 
				concurrentTempTables > 0 && 
				concurrentTempTables <= 100)
			this.partitions = concurrentTempTables;
	}
	
}
