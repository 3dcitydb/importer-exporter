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
