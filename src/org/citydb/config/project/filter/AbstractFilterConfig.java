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
package org.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractFilterType", propOrder={
		"mode",
		"simpleFilter"
})
public abstract class AbstractFilterConfig {
	@XmlElement(required=true)
	private FilterMode mode = FilterMode.COMPLEX;
	@XmlElement(name="simple", required=true)
	private SimpleFilter simpleFilter;
	
	public abstract AbstractComplexFilter getComplexFilter();
	public abstract void setComplexFilter(AbstractComplexFilter complexFilter);
	
	public AbstractFilterConfig() {
		simpleFilter = new SimpleFilter();
	}

	public FilterMode getMode() {
		return mode;
	}

	public boolean isSetSimpleFilter() {
		return mode == FilterMode.SIMPLE;
	}
	
	public boolean isSetComplexFilter() {
		return mode == FilterMode.COMPLEX;
	}
	
	public void setMode(FilterMode mode) {
		this.mode = mode;
	}

	public SimpleFilter getSimpleFilter() {
		return simpleFilter;
	}

	public void setSimpleFilter(SimpleFilter simpleFilter) {
		if (simpleFilter != null)
			this.simpleFilter = simpleFilter;
	}

}
