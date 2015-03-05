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

import org.citygml4j.model.module.ModuleType;

public class ProjectionPropertyFilter {
	private ProjectionPropertySet propertySet;
	
	public ProjectionPropertyFilter(ProjectionPropertySet propertySet) {
		this.propertySet = propertySet;
	}
	
	public boolean filter(ModuleType module, String propertyName) {
		return !pass(module, propertyName);
	}
	
	public boolean pass(ModuleType module, String propertyName) {
		return propertySet == null || propertySet.contains(module, propertyName);
	}
	
	public void combine(ProjectionPropertyFilter projectionPropertyFilter) {
		if (projectionPropertyFilter.propertySet != null) {
			if (propertySet != null)
				propertySet.addAll(projectionPropertyFilter.propertySet);
			else
				propertySet = projectionPropertyFilter.propertySet;
		}
	}
	
}
