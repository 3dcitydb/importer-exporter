/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.modules.common.filter.statistic;

import java.util.ArrayList;
import java.util.List;

import org.citydb.config.Config;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.FeatureCount;
import org.citydb.modules.common.filter.Filter;
import org.citydb.modules.common.filter.FilterMode;

public class FeatureCounterFilter implements Filter<Long> {
	private final AbstractFilterConfig filterConfig;

	private boolean isActive;
	private FeatureCount featureCountFilter;

	public FeatureCounterFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();

		init();
	}

	private void init() {
		isActive = filterConfig.isSetComplexFilter() &&
			filterConfig.getComplexFilter().getFeatureCount().isSet();

		if (isActive)
			featureCountFilter = filterConfig.getComplexFilter().getFeatureCount();			
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(Long number) {
		if (isActive) {			
			Long firstElement = featureCountFilter.getFrom();
			Long lastElement = featureCountFilter.getTo();

			if (firstElement != null && number < firstElement)
				return true;

			if (lastElement != null && number > lastElement)
				return true;
		}

		return false;
	}

	public List<Long> getFilterState() {
		return getInternalState(false);
	}

	public List<Long> getNotFilterState() {
		return getInternalState(true);
	}

	private List<Long> getInternalState(boolean inverse) {
		List<Long> state = new ArrayList<Long>();

		if (isActive) {						
			Long firstElement = featureCountFilter.getFrom();
			Long lastElement = featureCountFilter.getTo();

			if (!inverse)
				state.add(firstElement);
			else 
				state.add(lastElement);

			if (!inverse)
				state.add(lastElement);
			else 
				state.add(firstElement);
		} else {
			state.add(null);
			state.add(null);
		}		

		return state;
	}
}
