/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
