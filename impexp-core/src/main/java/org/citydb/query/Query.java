package org.citydb.query;

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.cache.model.CacheTableModel;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.apperance.AppearanceFilter;
import org.citydb.query.filter.counter.CounterFilter;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.tiling.Tiling;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CityGMLVersion;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Query {
	private CityGMLVersion targetVersion;
	private DatabaseSrs targetSRS;
	private FeatureTypeFilter featureTypeFilter;
	private CounterFilter counterFilter;
	private LodFilter lodFilter;
	private ConcurrentHashMap<Integer, ProjectionFilter> projectionFilters;
	private SelectionFilter selection;
	private AppearanceFilter appearanceFilter;
	private Tiling tiling;
	private ConcurrentLinkedQueue<CacheTable> materializedQueries;

	public Query() {
		projectionFilters = new ConcurrentHashMap<>();
		materializedQueries = new ConcurrentLinkedQueue<>();
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

	public boolean isSetTargetSRS() {
		return targetSRS != null;
	}

	public DatabaseSrs getTargetSRS() {
		return targetSRS;
	}

	public void setTargetSRS(DatabaseSrs targetSRS) {
		this.targetSRS = targetSRS;
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

	public boolean isSetCounterFilter() {
		return counterFilter != null;
	}

	public CounterFilter getCounterFilter() {
		return counterFilter;
	}

	public void setCounterFilter(CounterFilter counterFilter) {
		this.counterFilter = counterFilter;
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

	public boolean isSetProjection() {
		return !projectionFilters.isEmpty();
	}

	public ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType) {
		ProjectionFilter projectionFilter = projectionFilters.get(objectType.getObjectClassId());
		if (projectionFilter == null) {
			ProjectionFilter tmp = new ProjectionFilter(objectType);
			projectionFilter = projectionFilters.putIfAbsent(objectType.getObjectClassId(), tmp);
			if (projectionFilter == null)
				projectionFilter = tmp;
		}

		return projectionFilter;
	}

	public void addProjectionFilter(ProjectionFilter projectionFilter) {
		projectionFilters.putIfAbsent(projectionFilter.getObjectType().getObjectClassId(), projectionFilter);
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

	public AppearanceFilter getAppearanceFilter() {
		return appearanceFilter;
	}
	
	public boolean isSetAppearanceFilter() {
		return appearanceFilter != null && appearanceFilter.containsThemes();
	}

	public void setAppearanceFilter(AppearanceFilter appearanceFilter) {
		this.appearanceFilter = appearanceFilter;
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

	public boolean isSetOrderBy() {
		return false;
	}

	public boolean hasMaterializedQueries() {
		return !materializedQueries.isEmpty();
	}

	public Collection<CacheTable> getMaterializedQueries() {
		return materializedQueries;
	}

	public void addMaterializedQuery(CacheTable cacheTable) throws FilterException {
		if (cacheTable.getModelType() != CacheTableModel.ID_LIST)
			throw new FilterException("Only cache tables of type " + CacheTableModel.ID_LIST +
					"may be used as materialized queries.");

		materializedQueries.add(cacheTable);
	}

	public void copyFrom(Query query) {
		targetVersion = query.targetVersion;
		targetSRS = query.targetSRS;
		featureTypeFilter = query.featureTypeFilter;
		lodFilter = query.lodFilter;
		projectionFilters = query.projectionFilters;
		selection = query.selection;
		tiling = query.tiling;
		materializedQueries = query.materializedQueries;
	}

}
