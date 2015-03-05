/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.common.filter;

import org.citydb.config.Config;
import org.citydb.modules.common.filter.feature.BoundingBoxFilter;
import org.citydb.modules.common.filter.feature.FeatureClassFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.modules.common.filter.feature.GmlNameFilter;
import org.citydb.modules.common.filter.statistic.FeatureCounterFilter;

public class ImportFilter {
	private FeatureClassFilter featureClassFilter;
	private FeatureCounterFilter featureCounterFilter;
	private GmlIdFilter gmlIdFilter;
	private GmlNameFilter gmlNameFilter;
	private BoundingBoxFilter boundingBoxFilter;
	
	public ImportFilter(Config config) {
		featureClassFilter = new FeatureClassFilter(config, FilterMode.IMPORT);
		featureCounterFilter = new FeatureCounterFilter(config, FilterMode.IMPORT);
		gmlIdFilter = new GmlIdFilter(config, FilterMode.IMPORT);
		gmlNameFilter = new GmlNameFilter(config, FilterMode.IMPORT);
		boundingBoxFilter = new BoundingBoxFilter(config, FilterMode.IMPORT);
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
