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
package org.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AbstractFilterType", propOrder={
		"mode",
		"simpleFilter"
})
public abstract class AbstractFilterConfig {
	@XmlElement(required=true)
	private FilterMode mode = FilterMode.COMPLEX;
	@XmlElement(name="simple", required=true)
	private SimpleFilter simpleFilter;
	
	public abstract AbstractComplexFilter getComplexFilter();
	public abstract void setComplexFilter(AbstractComplexFilter complexFilter);
	
	public AbstractFilterConfig() {
		simpleFilter = new SimpleFilter();
	}

	public FilterMode getMode() {
		return mode;
	}

	public boolean isSetSimpleFilter() {
		return mode == FilterMode.SIMPLE;
	}
	
	public boolean isSetComplexFilter() {
		return mode == FilterMode.COMPLEX;
	}
	
	public void setMode(FilterMode mode) {
		this.mode = mode;
	}

	public SimpleFilter getSimpleFilter() {
		return simpleFilter;
	}

	public void setSimpleFilter(SimpleFilter simpleFilter) {
		if (simpleFilter != null)
			this.simpleFilter = simpleFilter;
	}

}
