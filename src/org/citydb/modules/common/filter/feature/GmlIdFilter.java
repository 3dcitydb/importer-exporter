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
package org.citydb.modules.common.filter.feature;

import java.util.List;

import org.citydb.config.Config;
import org.citydb.config.project.filter.AbstractFilterConfig;
import org.citydb.config.project.filter.GmlId;
import org.citydb.modules.common.filter.Filter;
import org.citydb.modules.common.filter.FilterMode;

public class GmlIdFilter implements Filter<String> {
	private final AbstractFilterConfig filterConfig;

	private boolean isActive;
	private GmlId gmlIdFilter;

	public GmlIdFilter(Config config, FilterMode mode) {
		if (mode == FilterMode.EXPORT)
			filterConfig = config.getProject().getExporter().getFilter();
		else if (mode == FilterMode.KML_EXPORT)
			filterConfig = config.getProject().getKmlExporter().getFilter();
		else
			filterConfig = config.getProject().getImporter().getFilter();

		init();
	}

	private void init() {
		isActive = filterConfig.isSetSimpleFilter();
		if (isActive)
			gmlIdFilter = filterConfig.getSimpleFilter().getGmlIdFilter();			
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void reset() {
		init();
	}

	public boolean filter(String gmlId) {
		if (isActive) {
			List<String> gmlIdList = gmlIdFilter.getGmlIds();
			if (gmlIdList != null) {
				for (String item : gmlIdList)
					if (gmlId.equals(item))
						return false;
			}

			return true;
		}

		return false;
	}

	public List<String> getFilterState() {
		return getInternalState(false);
	}

	public List<String> getNotFilterState() {
		return getInternalState(true);
	}

	private List<String> getInternalState(boolean inverse) {
		if (isActive) {
			if (!inverse)
				return gmlIdFilter.getGmlIds();
			else
				return null;			
		} 

		return null;
	}
}
