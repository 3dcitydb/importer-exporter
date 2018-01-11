package org.citydb.config.project.query;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.query.filter.appearance.AppearanceFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.config.project.query.filter.lod.LodFilter;
import org.citydb.config.project.query.filter.projection.ProjectionFilter;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.tiling.Tiling;
import org.citydb.config.project.query.filter.type.FeatureTypeFilter;

@XmlRootElement(name="query")
@XmlType(name="QueryType", propOrder={
		"featureTypeFilter",
		"projectionFilter",
		"selectionFilter",
		"counterFilter",
		"lodFilter",
		"appearanceFilter",
		"tiling"
})
public class Query {
	@XmlIDREF
	@XmlAttribute
	private DatabaseSrs targetSRS;
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
	private Tiling tiling;
	
	public DatabaseSrs getTargetSRS() {
		return targetSRS;
	}
	
	public boolean isSetTargetSRS() {
		return targetSRS != null;
	}
	
	public void setTargetSRS(DatabaseSrs targetSRS) {
		this.targetSRS = targetSRS;
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

	public Tiling getTiling() {
		return tiling;
	}
	
	public boolean isSetTiling() {
		return tiling != null;
	}

	public void setTiling(Tiling tiling) {
		this.tiling = tiling;
	}
	
}
