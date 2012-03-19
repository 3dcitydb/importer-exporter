/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.common.filter.statistic;

import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.FeatureCount;
import de.tub.citydb.modules.common.filter.Filter;
import de.tub.citydb.modules.common.filter.FilterMode;

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
