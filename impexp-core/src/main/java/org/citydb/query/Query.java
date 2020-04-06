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
package org.citydb.query;

import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.query.filter.apperance.AppearanceFilter;
import org.citydb.query.filter.counter.CounterFilter;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;
import org.citydb.query.filter.projection.Projection;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.sorting.Sorting;
import org.citydb.query.filter.tiling.Tiling;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CityGMLVersion;

public class Query {
	private CityGMLVersion targetVersion;
	private DatabaseSrs targetSrs;
	private FeatureTypeFilter featureTypeFilter;
	private CounterFilter counterFilter;
	private LodFilter lodFilter;
	private Projection projectionFilter;
	private SelectionFilter selection;
	private AppearanceFilter appearanceFilter;
	private Sorting sorting;
	private Tiling tiling;

	public Query() {

	}

	public Query(Query other) {
		copyFrom(other);
	}

	public boolean isSetTargetVersion() {
		return targetVersion != null;
	}

	public CityGMLVersion getTargetVersion() {
		return targetVersion;
	}

	public void setTargetVersion(CityGMLVersion targetVersion) {
		this.targetVersion = targetVersion;
	}

	public boolean isSetTargetSrs() {
		return targetSrs != null;
	}

	public DatabaseSrs getTargetSrs() {
		return targetSrs;
	}

	public void setTargetSrs(DatabaseSrs targetSrs) {
		this.targetSrs = targetSrs;
	}

	public boolean isSetFeatureTypeFilter() {
		return featureTypeFilter != null;
	}

	public FeatureTypeFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}

	public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
		this.featureTypeFilter = featureTypeFilter;
	}

	public void unsetFeatureTypeFilter() {
		featureTypeFilter = null;
	}

	public boolean isSetCounterFilter() {
		return counterFilter != null;
	}

	public CounterFilter getCounterFilter() {
		return counterFilter;
	}

	public void setCounterFilter(CounterFilter counterFilter) {
		this.counterFilter = counterFilter;
	}

	public void unsetCounterFilter() {
		counterFilter = null;
	}

	public boolean isSetLodFilter() {
		return lodFilter != null;
	}

	public LodFilter getLodFilter() {
		if (lodFilter == null)
			lodFilter = new LodFilter(LodFilterMode.OR);

		return lodFilter;
	}

	public void setLodFilter(LodFilter lodFilter) {
		this.lodFilter = lodFilter;
	}

	public void unsetLodFilter() {
		lodFilter = null;
	}

	public boolean isSetProjection() {
		return projectionFilter != null;
	}

	public ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType) {
		return projectionFilter != null ? projectionFilter.getProjectionFilter(objectType) : new ProjectionFilter(objectType);
	}

	public void setProjection(Projection projectionFilter) {
		this.projectionFilter = projectionFilter;
	}

	public void unsetProjection() {
		projectionFilter = null;
	}

	public boolean isSetSelection() {
		return selection != null && selection.isSetPredicate();
	}

	public SelectionFilter getSelection() {
		return selection;
	}

	public void setSelection(SelectionFilter selection) {
		this.selection = selection;
	}

	public void unsetSelection() {
		selection = null;
	}

	public AppearanceFilter getAppearanceFilter() {
		return appearanceFilter;
	}
	
	public boolean isSetAppearanceFilter() {
		return appearanceFilter != null && appearanceFilter.containsThemes();
	}

	public void setAppearanceFilter(AppearanceFilter appearanceFilter) {
		this.appearanceFilter = appearanceFilter;
	}

	public void unsetAppearanceFilter() {
		appearanceFilter = null;
	}

	public boolean isSetSorting() {
		return sorting != null && sorting.hasSortProperties();
	}

	public Sorting getSorting() {
		return sorting;
	}

	public void setSorting(Sorting sorting) {
		this.sorting = sorting;
	}

	public void unsetSorting() {
		sorting = null;
	}

	public boolean isSetTiling() {
		return tiling != null;
	}

	public Tiling getTiling() {
		return tiling;
	}

	public void setTiling(Tiling tiling) {
		this.tiling = tiling;
	}

	public void unsetTiling() {
		tiling = null;
	}

	public void copyFrom(Query query) {
		targetVersion = query.targetVersion;
		targetSrs = query.targetSrs;
		featureTypeFilter = query.featureTypeFilter;
		counterFilter = query.counterFilter;
		lodFilter = query.lodFilter;
		projectionFilter = query.projectionFilter;
		selection = query.selection;
		appearanceFilter = query.appearanceFilter;
		sorting = query.sorting;
		tiling = query.tiling;
	}

}
