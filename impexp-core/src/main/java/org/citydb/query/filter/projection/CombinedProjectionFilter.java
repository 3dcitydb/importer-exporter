/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.query.filter.projection;

import org.citydb.database.schema.mapping.AppSchema;
import org.citygml4j.model.citygml.CityGMLClass;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

public class CombinedProjectionFilter {
	private final List<ProjectionFilter> filters;
	
	public CombinedProjectionFilter(List<ProjectionFilter> filters) {
		this.filters = filters;
	}
	
	public CombinedProjectionFilter(ProjectionFilter... filters) {
		this.filters = Arrays.asList(filters);
	}
	
	public boolean containsProperty(String name, String namespaceURI) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsProperty(name, namespaceURI))
				return true;
		}
		
		return false;
	}

	public boolean containsProperty(QName name) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsProperty(name))
				return true;
		}
		
		return false;
	}

	public boolean containsProperty(String name, AppSchema schema) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsProperty(name, schema))
				return true;
		}
		
		return false;
	}

	public boolean containsGenericAttribute(GenericAttribute genericAttribute) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsGenericAttribute(genericAttribute))
				return true;
		}
		
		return false;
	}

	public boolean containsGenericAttribute(String name, CityGMLClass type) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsGenericAttribute(name, type))
				return true;
		}
		
		return false;
	}
	
}
