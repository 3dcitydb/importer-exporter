package org.citydb.citygml.importer.filter;

import org.citydb.citygml.importer.filter.selection.SelectionFilter;
import org.citydb.citygml.importer.filter.type.FeatureTypeFilter;
import org.citydb.config.project.query.filter.counter.CounterFilter;

public class CityGMLFilter {
	private FeatureTypeFilter featureTypeFilter;
	private SelectionFilter selectionFilter;
	private CounterFilter counterFilter;
	
	public CityGMLFilter() {
		featureTypeFilter = new FeatureTypeFilter();
		selectionFilter = new SelectionFilter();
	}

	public FeatureTypeFilter getFeatureTypeFilter() {
		return featureTypeFilter;
	}
	
	public boolean isSetFeatureTypeFilter() {
		return featureTypeFilter != null;
	}
	
	public void setFeatureTypeFilter(FeatureTypeFilter featureTypeFilter) {
		if (featureTypeFilter != null)
			this.featureTypeFilter = featureTypeFilter;
	}

	public SelectionFilter getSelectionFilter() {
		return selectionFilter;
	}
	
	public boolean isSetSelectionFilter() {
		return selectionFilter != null;
	}

	public void setSelectionFilter(SelectionFilter selectionFilter) {
		if (selectionFilter != null)
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
	
}
