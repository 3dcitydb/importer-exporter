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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilter;
import org.citydb.config.project.query.filter.selection.SimpleSelectionFilterMode;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;

@XmlType(name="SimpleExportQueryType", propOrder={
		"featureTypeFilter",
		"filter",
		"counterFilter",
		"lodFilter",
		"tilingOptions"
})
public class SimpleQuery {
	@XmlAttribute
	protected CityGMLVersionType version;
	@XmlIDREF
	@XmlAttribute
	protected DatabaseSrs targetSRS;
	@XmlAttribute
	private SimpleSelectionFilterMode mode = SimpleSelectionFilterMode.COMPLEX;
	@XmlAttribute
	private boolean useTypeNames;
	@XmlAttribute
	private boolean useCountFilter;
	@XmlAttribute
	private boolean useLodFilter;
	@XmlAttribute
	private boolean useGmlNameFilter;
	@XmlAttribute
	private boolean useBboxFilter;
	@XmlAttribute
	private boolean useTiling;
	
	@XmlElement(name = "typeNames")
	protected FeatureTypeFilter featureTypeFilter;
	private SimpleSelectionFilter filter;
	@XmlElement(name = "count")
	protected CounterFilter counterFilter;
	@XmlElement(name = "lods")
	protected LodFilter lodFilter;
	private TilingOptions tilingOptions;

	public SimpleQuery() {
		featureTypeFilter = new FeatureTypeFilter();
		filter = new SimpleSelectionFilter();
		counterFilter = new CounterFilter();
		lodFilter = new LodFilter();
		tilingOptions = new TilingOptions();
	}
	
	public CityGMLVersionType getVersion() {
		return version != null ? version : CityGMLVersionType.v2_0_0;
	}
	
	public void setVersion(CityGMLVersionType version) {
		this.version = version;
	}

	public DatabaseSrs getTargetSRS() {
		return targetSRS;
	}
	
	public boolean isSetTargetSRS() {
		return targetSRS != null;
	}
	
	public void setTargetSRS(DatabaseSrs targetSRS) {
		this.targetSRS = targetSRS;
	}
	
	public SimpleSelectionFilterMode getMode() {
		return mode;
	}

	public void setMode(SimpleSelectionFilterMode mode) {
		this.mode = mode;
	}
	
	public boolean isUseTypeNames() {
		return useTypeNames;
	}

	public void setUseTypeNames(boolean useTypeNames) {
		this.useTypeNames = useTypeNames;
	}

	public boolean isUseCountFilter() {
		return useCountFilter;
	}

	public void setUseCountFilter(boolean useCountFilter) {
		this.useCountFilter = useCountFilter;
	}

	public boolean isUseLodFilter() {
		return useLodFilter;
	}

	public void setUseLodFilter(boolean useLodFilter) {
		this.useLodFilter = useLodFilter;
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

	public boolean isUseTiling() {
		return useTiling;
	}

	public void setUseTiling(boolean useTiling) {
		this.useTiling = useTiling;
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
	
	public LodFilter getLodFilter() {
		return lodFilter;
	}
	
	public boolean isSetLodFilter() {
		return lodFilter != null;
	}

	public void setLodFilter(LodFilter lodFilter) {
		this.lodFilter = lodFilter;
	}
	
	public TilingOptions getTilingOptions() {
		return tilingOptions;
	}
	
	public boolean isSetTilingOptions() {
		return tilingOptions != null;
	}

	public void setTilingOptions(TilingOptions tilingOptions) {
		this.tilingOptions = tilingOptions;
	}
	
}
