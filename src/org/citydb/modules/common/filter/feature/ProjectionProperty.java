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

import org.citygml4j.model.module.ModuleType;

public class ProjectionProperty {
	private final String name;
	private final ModuleType moduleType;

	public ProjectionProperty(ModuleType moduleType, String name) {
		this.moduleType = moduleType;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ModuleType getModuleType() {
		return moduleType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ProjectionProperty))
			return false;

		if (obj == this)
			return true;
		
		ProjectionProperty property = (ProjectionProperty)obj;
		return moduleType.equals(property.moduleType) && name.equals(property.name);
	}

	@Override
	public int hashCode() {
		return moduleType.hashCode() ^ name.hashCode();
	}

}
