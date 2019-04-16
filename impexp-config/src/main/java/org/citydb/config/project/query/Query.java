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
package org.citydb.config.project.query;

import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.query.filter.appearance.AppearanceFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.projection.ProjectionFilter;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.sorting.Sorting;
import org.citydb.config.project.query.filter.tiling.Tiling;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;

@XmlRootElement(name="query")
@XmlType(name="QueryType", propOrder={
		"featureTypeFilter",
		"projectionFilter",
		"selectionFilter",
		"counterFilter",
		"lodFilter",
		"appearanceFilter",
		"sorting",
		"tiling"
})
public class Query {
	@XmlIDREF
	@XmlAttribute
	private DatabaseSrs targetSrs;
	@XmlAttribute
	private Integer targetSrid;
	@XmlAttribute
	private String targetSrsName;
	@XmlElement(name = "typeNames")
	private FeatureTypeFilter featureTypeFilter;
	@XmlElement(name="propertyNames")
	private ProjectionFilter projectionFilter;
	@XmlElement(name="filter")
	private SelectionFilter selectionFilter;
	@XmlElement(name = "count")
	private CounterFilter counterFilter;
	@XmlElement(name = "lods")
	private LodFilter lodFilter;
	@XmlElement(name = "appearance")
	private AppearanceFilter appearanceFilter;
	@XmlElement(name = "sortBy")
	private Sorting sorting;
	private Tiling tiling;

	@XmlTransient
	private HashMap<String, Object> localProperties;
	
	public DatabaseSrs getTargetSrs() {
		if (targetSrs != null)
			return targetSrs;
		else if (targetSrid != null) {
			DatabaseSrs srs = new DatabaseSrs(targetSrid);
			srs.setGMLSrsName(targetSrsName);
			srs.setDescription(targetSrsName);
			srs.setSupported(true);
			return srs;
		} else
			return null;
	}
	
	public boolean isSetTargetSrs() {
		return targetSrs != null || targetSrid != null;
	}
	
	public void setTargetSrs(DatabaseSrs targetSrs) {
		this.targetSrs = targetSrs;
		targetSrid = null;
		targetSrsName = null;
	}

	public void unsetTargetSrs() {
		targetSrs = null;
		targetSrid = null;
		targetSrsName = null;
	}

	public void setTargetSrs(int targetSrid, String targetSrsName) {
		this.targetSrid = targetSrid;
		this.targetSrsName = targetSrsName;
		targetSrs = null;
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

	public ProjectionFilter getProjectionFilter() {
		return projectionFilter;
	}
	
	public boolean isSetProjectionFilter() {
		return projectionFilter != null;
	}

	public void setProjectionFilter(ProjectionFilter projectionFilter) {
		this.projectionFilter = projectionFilter;
	}

	public SelectionFilter getSelectionFilter() {
		return selectionFilter;
	}
	
	public boolean isSetSelectionFilter() {
		return selectionFilter != null;
	}

	public void setSelectionFilter(SelectionFilter selectionFilter) {
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

	public AppearanceFilter getAppearanceFilter() {
		return appearanceFilter;
	}
	
	public boolean isSetAppearanceFilter() {
		return appearanceFilter != null;
	}

	public void setAppearanceFilter(AppearanceFilter appearanceFilter) {
		this.appearanceFilter = appearanceFilter;
	}

	public Sorting getSorting() {
		return sorting;
	}

	public boolean isSetSorting() {
		return sorting != null;
	}

	public void setSorting(Sorting sorting) {
		this.sorting = sorting;
	}

	public Tiling getTiling() {
		return tiling;
	}
	
	public boolean isSetTiling() {
		return tiling != null;
	}

	public void setTiling(Tiling tiling) {
		this.tiling = tiling;
	}

	public Object getLocalProperty(String name) {
		return localProperties != null ? localProperties.get(name) : null;
	}

	public void setLocalProperty(String name, Object value) {
		if (localProperties == null)
			localProperties = new HashMap<>();

		localProperties.put(name, value);
	}

	public boolean hasLocalProperty(String name) {
		return localProperties != null && localProperties.containsKey(name);
	}
	
}
