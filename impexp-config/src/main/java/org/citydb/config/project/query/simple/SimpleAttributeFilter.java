/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.config.project.query.simple;

import org.citydb.config.project.query.filter.selection.comparison.LikeOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleAttributeFilterType", propOrder={
		"gmlIdFilter",
		"gmlNameFilter"
})
public class SimpleAttributeFilter {
	@XmlElement(name = "gmlIds", required = true)
	private ResourceIdOperator gmlIdFilter;
	@XmlElement(name = "gmlName", required = true)
	private LikeOperator gmlNameFilter;

	public SimpleAttributeFilter() {
		gmlIdFilter = new ResourceIdOperator();
		gmlNameFilter = new LikeOperator();
	}
	
	public ResourceIdOperator getGmlIdFilter() {
		return gmlIdFilter;
	}
	
	public boolean isSetGmlIdFilter() {
		return gmlIdFilter != null;
	}

	public void setGmlIdFilter(ResourceIdOperator gmlIdFilter) {
		this.gmlIdFilter = gmlIdFilter;
	}

	public LikeOperator getGmlNameFilter() {
		return gmlNameFilter;
	}
	
	public boolean isSetGmlNameFilter() {
		return gmlNameFilter != null;
	}

	public void setGmlNameFilter(LikeOperator gmlNameFilter) {
		this.gmlNameFilter = gmlNameFilter;
	}

}
