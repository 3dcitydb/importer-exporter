/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.ade.importer;

import org.citygml4j.model.citygml.ade.binding.ADEModelObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ADEPropertyCollection {
	private HashMap<Class<? extends ADEModelObject>, List<ADEModelObject>> properties = new HashMap<>();
	
	public void register(ADEModelObject property) {
		List<ADEModelObject> properties = this.properties.get(property.getClass());
		if (properties == null) {
			properties = new ArrayList<>();
			this.properties.put(property.getClass(), properties);
		}
		
		properties.add(property);
	}
	
	public boolean containsOneOf(Class<?>... propertyTypes) {
		for (Class<?> propertyType : propertyTypes) {
			if (properties.containsKey(propertyType))
				return true;
		}
		
		return false;
	}
	
	public boolean contains(Class<? extends ADEModelObject> propertyType) {
		return properties.containsKey(propertyType);
	}
	
	public boolean containsOne(Class<? extends ADEModelObject> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		return properties != null && properties.size() == 1;
	}
	
	public boolean containsMultiple(Class<? extends ADEModelObject> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		return properties != null && properties.size() > 1;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ADEModelObject> List<T> getAll(Class<T> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		return properties != null ? (List<T>)properties : Collections.emptyList();
	}
	
	public <T extends ADEModelObject> T getFirst(Class<T> propertyType) {
		List<ADEModelObject> properties = this.properties.get(propertyType);
		if (properties != null && !properties.isEmpty())
			return propertyType.cast(properties.get(0));
		
		return null;
	}

}
