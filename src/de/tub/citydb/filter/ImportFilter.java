package de.tub.citydb.filter;

import de.tub.citydb.config.Config;
import de.tub.citydb.filter.feature.BoundingBoxFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.filter.feature.GmlIdFilter;
import de.tub.citydb.filter.feature.GmlNameFilter;
import de.tub.citydb.filter.statistic.FeatureCounterFilter;
import de.tub.citydb.util.DBUtil;

public class ImportFilter {
	private FeatureClassFilter featureClassFilter;
	private FeatureCounterFilter featureCounterFilter;
	private GmlIdFilter gmlIdFilter;
	private GmlNameFilter gmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;
	
	public ImportFilter(Config config, DBUtil dbUtil) {
		featureClassFilter = new FeatureClassFilter(config, FilterMode.IMPORT);
		featureCounterFilter = new FeatureCounterFilter(config, FilterMode.IMPORT);
		gmlIdFilter = new GmlIdFilter(config, FilterMode.IMPORT);
		gmlNameFilter = new GmlNameFilter(config, FilterMode.IMPORT);
		boundingBoxFilter = new BoundingBoxFilter(config, FilterMode.IMPORT, dbUtil);
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
