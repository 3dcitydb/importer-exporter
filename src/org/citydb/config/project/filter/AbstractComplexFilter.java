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

import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractComplexFilterType", propOrder={
		"featureCount",
		"gmlName",
		"featureClass"
})
public abstract class AbstractComplexFilter {
	private FeatureCount featureCount;
	private GmlName gmlName;
	private FeatureClass featureClass;

	public AbstractComplexFilter() {
		featureCount = new FeatureCount();
		gmlName = new GmlName();
		featureClass = new FeatureClass();
	}

	public abstract FilterBoundingBox getBoundingBox();
	public abstract void setBoundingBox(FilterBoundingBox boundingBox);
	
	public GmlName getGmlName() {
		return gmlName;
	}

	public void setGmlName(GmlName gmlName) {
		if (gmlName != null)
			this.gmlName = gmlName;
	}

	public FeatureCount getFeatureCount() {
		return featureCount;
	}

	public void setFeatureCount(FeatureCount featureCount) {
		if (featureCount != null)
			this.featureCount = featureCount;
	}

	public FeatureClass getFeatureClass() {
		return featureClass;
	}

	public void setFeatureClass(FeatureClass featureClass) {
		if (featureClass != null)
			this.featureClass = featureClass;
	}

}
