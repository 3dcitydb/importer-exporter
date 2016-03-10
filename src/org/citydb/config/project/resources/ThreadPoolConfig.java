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
package org.citydb.config.project.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ThreadPoolConfigType", propOrder={
		"minThreads",
		"maxThreads"		
})
public class ThreadPoolConfig {
	@XmlElement(required=true)
	@XmlSchemaType(name="positiveInteger")
	private Integer minThreads;
	@XmlElement(required=true)
	@XmlSchemaType(name="positiveInteger")
	private Integer maxThreads;
	
	public ThreadPoolConfig() {
		minThreads = 1;
		maxThreads = Runtime.getRuntime().availableProcessors() * 2;
	}

	public Integer getMinThreads() {
		return minThreads;
	}

	public void setMinThreads(Integer minThreads) {
		if (minThreads != null && minThreads > 0)
			this.minThreads = minThreads;
	}

	public Integer getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(Integer maxThreads) {
		if (maxThreads != null && maxThreads > 0)
			this.maxThreads = maxThreads;
	}
	
}
