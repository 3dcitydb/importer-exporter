/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.modules.common.filter.feature;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.filter.AbstractFilterConfig;
import de.tub.citydb.config.project.filter.GmlName;
import de.tub.citydb.modules.common.filter.Filter;
import de.tub.citydb.modules.common.filter.FilterMode;

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
