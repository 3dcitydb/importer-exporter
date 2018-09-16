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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;

@XmlType(name="SimpleImportFilterType", propOrder={
		"featureTypeFilter",
		"filter",
		"counterFilter"
})
public class ImportFilter {
	@XmlAttribute
	private boolean useTypeNames;
	@XmlAttribute
	private SimpleSelectionFilterMode mode = SimpleSelectionFilterMode.COMPLEX;
	@XmlAttribute
	private boolean useCountFilter;
	@XmlAttribute
	private boolean useGmlNameFilter;
	@XmlAttribute
	private boolean useBboxFilter;
	
	@XmlElement(name = "typeNames")
	private FeatureTypeFilter featureTypeFilter;
	@XmlElement(name="simpleFilter")
	private SimpleSelectionFilter filter;
	@XmlElement(name = "count")
	private CounterFilter counterFilter;

	public ImportFilter() {
		featureTypeFilter = new FeatureTypeFilter();
		filter = new SimpleSelectionFilter();
		counterFilter = new CounterFilter();
	}
	
	public boolean isUseTypeNames() {
		return useTypeNames;
	}

	public void setUseTypeNames(boolean useTypeNames) {
		this.useTypeNames = useTypeNames;
	}
	
	public SimpleSelectionFilterMode getMode() {
		return mode;
	}

	public void setMode(SimpleSelectionFilterMode mode) {
		this.mode = mode;
	}

	public boolean isUseCountFilter() {
		return useCountFilter;
	}

	public void setUseCountFilter(boolean useCountFilter) {
		this.useCountFilter = useCountFilter;
	}

	public boolean isUseGmlNameFilter() {
		return useGmlNameFilter;
	}

	public void setUseGmlNameFilter(boolean useGmlNameFilter) {
		this.useGmlNameFilter = useGmlNameFilter;
	}

	public boolean isUseBboxFilter() {
		return useBboxFilter;
	}

	public void setUseBboxFilter(boolean useBboxFilter) {
		this.useBboxFilter = useBboxFilter;
	}
	
	public FeatureTypeFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}
	
	public boolean isSetFeatureTypeFilter() {
		return featureTypeFilter != null;
	}
	
	public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
		this.featureTypeFilter = featureTypeFilter;
	}

	public SimpleSelectionFilter getFilter() {
		return filter;
	}
	
	public boolean isSetFilter() {
		return filter != null;
	}

	public void setFilter(SimpleSelectionFilter filter) {
		this.filter = filter;
	}
	
	public CounterFilter getCounterFilter() {
		return counterFilter;
	}
	
	public boolean isSetCounterFilter() {
		return counterFilter != null;
	}

	public void setCounterFilter(CounterFilter counterFilter) {
		this.counterFilter = counterFilter;
	}
	
}
