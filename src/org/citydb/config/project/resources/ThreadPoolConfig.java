/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		minThreads = 2;
		maxThreads = Math.max(minThreads, Runtime.getRuntime().availableProcessors());
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
