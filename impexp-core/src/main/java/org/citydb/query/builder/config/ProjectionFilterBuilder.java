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
package org.citydb.query.builder.config;

import org.citydb.config.project.query.filter.projection.AbstractPropertyName;
import org.citydb.config.project.query.filter.projection.GenericAttributeName;
import org.citydb.config.project.query.filter.projection.ProjectionContext;
import org.citydb.config.project.query.filter.projection.PropertyName;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.projection.Projection;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.projection.ProjectionMode;
import org.citygml4j.model.citygml.CityGMLClass;

import javax.xml.namespace.QName;
import java.util.IdentityHashMap;
import java.util.Map;

public class ProjectionFilterBuilder {
	private final SchemaMapping schemaMapping;

	protected ProjectionFilterBuilder(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	protected Projection buildProjectionFilter(org.citydb.config.project.query.filter.projection.ProjectionFilter projectionFilterConfig) throws QueryBuildException {
		Projection projection = new Projection();
		Map<AbstractObjectType<?>, ProjectionFilter> projectionFilters = new IdentityHashMap<>();

		for (ProjectionContext context : projectionFilterConfig.getProjectionContexts()) {
			// get feature type
			QName typeName = context.getTypeName();
			if (typeName == null)
				throw new QueryBuildException("Failed to retrieve the qualified name of the object type.");

			AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(typeName);
			if (objectType == null)
				throw new QueryBuildException("'" + typeName + "' is not a valid object type.");

			if (projectionFilters.containsKey(objectType))
				throw new QueryBuildException("Multiple projection filters defined for '" + typeName + "'.");
			
			// get projection mode
			ProjectionMode mode = null;
			switch (context.getMode()) {
			case KEEP:
				mode = ProjectionMode.KEEP;
				break;
			case REMOVE:
				mode = ProjectionMode.REMOVE;
				break;
			}

			// create projection filter for object type
			ProjectionFilter projectionFilter = new ProjectionFilter(objectType, mode);
			projectionFilters.put(objectType, projectionFilter);

			try {				
				// add properties and generic attributes
				if (context.isSetPropertyNames()) {
					for (AbstractPropertyName abstractPropertyName : context.getPropertyNames()) {
						if (abstractPropertyName instanceof PropertyName)
							projectionFilter.addProperty(((PropertyName) abstractPropertyName).getName());

						else if (abstractPropertyName instanceof GenericAttributeName) {
							GenericAttributeName name = (GenericAttributeName) abstractPropertyName;
							CityGMLClass type = name.isSetType() ? name.getType().getCityGMLClass() : null;
							projectionFilter.addGenericAttribute(name.getName(), type);
						}
					}
				}
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the projection filter.", e);
			}

			projection.addProjectionFilter(projectionFilter);
		}

		return projection;
	}

}
