/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.filter.AbstractComplexFilter;
import org.citydb.config.project.filter.AbstractFilterConfig;

@XmlType(name="ExportFilterType", propOrder={
		"complexFilter"
		})
public class ExportFilterConfig extends AbstractFilterConfig {
	@XmlElement(name="complex", required=true)
	private ExportComplexFilter complexFilter;
	
	public ExportFilterConfig() {
		complexFilter = new ExportComplexFilter();
	}

	public ExportComplexFilter getComplexFilter() {
		return complexFilter;
	}

	public void setComplexFilter(ExportComplexFilter complexFilter) {
		if (complexFilter != null)
			this.complexFilter = complexFilter;
	}

	@Override
	public void setComplexFilter(AbstractComplexFilter complexFilter) {
		if (complexFilter instanceof ExportComplexFilter)
			this.complexFilter = (ExportComplexFilter)complexFilter;
		else {
			this.complexFilter.setBoundingBox(complexFilter.getBoundingBox());
			this.complexFilter.setFeatureClass(complexFilter.getFeatureClass());
			this.complexFilter.setFeatureCount(complexFilter.getFeatureCount());
			this.complexFilter.setGmlName(complexFilter.getGmlName());
		}
	}
	
}
