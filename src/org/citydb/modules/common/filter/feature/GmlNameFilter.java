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
package org.citydb.modules.common.filter.feature;

import org.citydb.config.Config;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.GmlName;
import org.citydb.modules.common.filter.Filter;
import org.citydb.modules.common.filter.FilterMode;

public class GmlNameFilter implements Filter<String> {
	private final AbstractFilterConfig filterConfig;

	private boolean isActive;
	private GmlName gmlNameFilter;

	public GmlNameFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else if (mode == FilterMode.KML_EXPORT)
			filterConfig = config.getProject().getKmlExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();

		init();
	}

	private void init() {
		isActive = filterConfig.isSetComplexFilter() &&
			filterConfig.getComplexFilter().getGmlName().isSet();

		if (isActive)
			gmlNameFilter = filterConfig.getComplexFilter().getGmlName();			
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(String gmlName) {
		if (isActive) {
			if (gmlNameFilter.getValue() != null && gmlNameFilter.getValue().length() > 0) {
				String adaptedValue = gmlNameFilter.getValue().trim().toUpperCase();				
				if (!gmlName.trim().toUpperCase().equals(adaptedValue))
					return true;
			}
		}

		return false;
	}

	public String getFilterState() {
		return getInternalState(false);
	}

	public String getNotFilterState() {
		return getInternalState(true);
	}

	private String getInternalState(boolean inverse) {
		if (isActive) {
			if (!inverse && gmlNameFilter.getValue() != null && gmlNameFilter.getValue().length() > 0)
				return gmlNameFilter.getValue();
			else
				return null;			
		} 

		return null;
	}
}
