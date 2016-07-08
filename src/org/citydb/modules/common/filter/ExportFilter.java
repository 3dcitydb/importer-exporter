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
package org.citydb.modules.common.filter;

import java.util.EnumMap;

import org.citydb.config.Config;
import org.citydb.modules.common.filter.feature.BoundingBoxFilter;
import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.modules.common.filter.feature.GmlNameFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertyFilter;
import org.citydb.modules.common.filter.feature.ProjectionPropertySet;
import org.citydb.modules.common.filter.statistic.FeatureCounterFilter;
import org.citygml4j.model.citygml.CityGMLClass;

public class ExportFilter {
	private FeatureClassFilter featureClassFilter;
	private FeatureCounterFilter featureCounterFilter;
	private GmlIdFilter gmlIdFilter;
	private GmlNameFilter gmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;
	private EnumMap<CityGMLClass, ProjectionPropertySet> projectionMap;
	
	public ExportFilter(Config config) {
		featureClassFilter = new FeatureClassFilter(config, FilterMode.EXPORT);
		featureCounterFilter = new FeatureCounterFilter(config, FilterMode.EXPORT);
		gmlIdFilter = new GmlIdFilter(config, FilterMode.EXPORT);
		gmlNameFilter = new GmlNameFilter(config, FilterMode.EXPORT);
		boundingBoxFilter = new BoundingBoxFilter(config, FilterMode.EXPORT);
	}

	public ExportFilter(Config config, FilterMode filterMode) {
		featureClassFilter = new FeatureClassFilter(config, filterMode);
		featureCounterFilter = new FeatureCounterFilter(config, filterMode);
		gmlIdFilter = new GmlIdFilter(config, filterMode);
		gmlNameFilter = new GmlNameFilter(config, filterMode);
		boundingBoxFilter = new BoundingBoxFilter(config, filterMode);
	}

	public FeatureClassFilter getFeatureClassFilter() {
		return featureClassFilter;
	}

	public FeatureCounterFilter getFeatureCounterFilter() {
		return featureCounterFilter;
	}

	public GmlIdFilter getGmlIdFilter() {
		return gmlIdFilter;
	}

	public GmlNameFilter getGmlNameFilter() {
		return gmlNameFilter;
	}

	public BoundingBoxFilter getBoundingBoxFilter() {
		return boundingBoxFilter;
	}

	public void setFeatureClassFilter(FeatureClassFilter featureClassFilter) {
		this.featureClassFilter = featureClassFilter;
	}

	public void setFeatureCounterFilter(FeatureCounterFilter featureCounterFilter) {
		this.featureCounterFilter = featureCounterFilter;
	}

	public void setGmlIdFilter(GmlIdFilter gmlIdFilter) {
		this.gmlIdFilter = gmlIdFilter;
	}

	public void setGmlNameFilter(GmlNameFilter gmlNameFilter) {
		this.gmlNameFilter = gmlNameFilter;
	}

	public void setBoundingBoxFilter(BoundingBoxFilter boundingBoxFilter) {
		this.boundingBoxFilter = boundingBoxFilter;
	}

	public void setProjectionMap(EnumMap<CityGMLClass, ProjectionPropertySet> projectionMap) {
		this.projectionMap = projectionMap;
	}
	
	public ProjectionPropertyFilter getProjectionPropertyFilter(CityGMLClass cityGMLClass) {
		return new ProjectionPropertyFilter(projectionMap != null ? projectionMap.get(cityGMLClass) : null);
	}
	
}
