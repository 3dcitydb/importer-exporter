/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.config.project.query.simple.SimpleSelectionFilter;
import org.citydb.config.project.query.simple.SimpleFeatureVersionFilter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleExportQueryType", propOrder={
		"featureTypeFilter",
		"featureVersionFilter",
		"selectionFilter",
		"counterFilter",
		"lodFilter",
		"bboxFilter"
})
public class SimpleQuery {
	@XmlAttribute
	protected CityGMLVersionType version;
	@XmlIDREF
	@XmlAttribute
	protected DatabaseSrs targetSrs;
	@XmlAttribute
	private boolean useTypeNames;
	@XmlAttribute
	private boolean useFeatureVersionFilter;
	@XmlAttribute
	private boolean useSelectionFilter;
	@XmlAttribute
	private boolean useCountFilter;
	@XmlAttribute
	private boolean useLodFilter;
	@XmlAttribute
	private boolean useBboxFilter;

	@XmlElement(name = "typeNames")
	protected FeatureTypeFilter featureTypeFilter;
	@XmlElement(name = "featureVersion")
	private SimpleFeatureVersionFilter featureVersionFilter;
	@XmlElement(name = "selection")
	private SimpleSelectionFilter selectionFilter;
	@XmlElement(name = "limit")
	protected CounterFilter counterFilter;
	@XmlElement(name = "lods")
	protected LodFilter lodFilter;
	@XmlElement(name = "bbox", required = true)
	private SimpleTiling bboxFilter;

	public SimpleQuery() {
		featureTypeFilter = new FeatureTypeFilter();
		featureVersionFilter = new SimpleFeatureVersionFilter();
		selectionFilter = new SimpleSelectionFilter();
		counterFilter = new CounterFilter();
		lodFilter = new LodFilter();
		bboxFilter = new SimpleTiling();
	}
	
	public CityGMLVersionType getVersion() {
		return version != null ? version : CityGMLVersionType.v2_0_0;
	}
	
	public void setVersion(CityGMLVersionType version) {
		this.version = version;
	}

	public DatabaseSrs getTargetSrs() {
		return targetSrs;
	}
	
	public boolean isSetTargetSrs() {
		return targetSrs != null;
	}
	
	public void setTargetSrs(DatabaseSrs targetSrs) {
		this.targetSrs = targetSrs;
	}

	public boolean isUseTypeNames() {
		return useTypeNames;
	}

	public void setUseTypeNames(boolean useTypeNames) {
		this.useTypeNames = useTypeNames;
	}

	public boolean isUseFeatureVersionFilter() {
		return useFeatureVersionFilter;
	}

	public void setUseFeatureVersionFilter(boolean useFeatureVersionFilter) {
		this.useFeatureVersionFilter = useFeatureVersionFilter;
	}

	public boolean isUseSelectionFilter() {
		return useSelectionFilter;
	}

	public void setUseSelectionFilter(boolean useSelectionFilter) {
		this.useSelectionFilter = useSelectionFilter;
	}

	public boolean isUseLodFilter() {
		return useLodFilter;
	}

	public boolean isUseCountFilter() {
		return useCountFilter;
	}

	public void setUseCountFilter(boolean useCountFilter) {
		this.useCountFilter = useCountFilter;
	}

	public void setUseLodFilter(boolean useLodFilter) {
		this.useLodFilter = useLodFilter;
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

	public SimpleFeatureVersionFilter getFeatureVersionFilter() {
		return featureVersionFilter;
	}

	public boolean isSetFeatureVersionFilter() {
		return featureVersionFilter != null;
	}

	public void setFeatureVersionFilter(SimpleFeatureVersionFilter featureVersionFilter) {
		this.featureVersionFilter = featureVersionFilter;
	}
	
	public SimpleSelectionFilter getSelectionFilter() {
		return selectionFilter;
	}

	public boolean isSetSelectionFilter() {
		return selectionFilter != null;
	}

	public void setSelectionFilter(SimpleSelectionFilter selectionFilter) {
		this.selectionFilter = selectionFilter;
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

	public SimpleTiling getBboxFilter() {
		return bboxFilter;
	}

	public boolean isSetBboxFilter() {
		return bboxFilter != null;
	}

	public void setBboxFilter(SimpleTiling bboxFilter) {
		this.bboxFilter = bboxFilter;
	}
	
}
