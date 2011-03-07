package de.tub.citydb.filter;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.feature.BoundingBoxFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.filter.feature.GmlIdFilter;
import de.tub.citydb.filter.feature.GmlNameFilter;
import de.tub.citydb.filter.statistic.FeatureCounterFilter;

public class ExportFilter {
	private FeatureClassFilter featureClassFilter;
	private FeatureCounterFilter featureCounterFilter;
	private GmlIdFilter gmlIdFilter;
	private GmlNameFilter gmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;
	
	public ExportFilter(Config config) {
		featureClassFilter = new FeatureClassFilter(config, FilterMode.EXPORT);
		featureCounterFilter = new FeatureCounterFilter(config, FilterMode.EXPORT);
		gmlIdFilter = new GmlIdFilter(config, FilterMode.EXPORT);
		gmlNameFilter = new GmlNameFilter(config, FilterMode.EXPORT);
		boundingBoxFilter = new BoundingBoxFilter(config, FilterMode.EXPORT);
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
	
}
