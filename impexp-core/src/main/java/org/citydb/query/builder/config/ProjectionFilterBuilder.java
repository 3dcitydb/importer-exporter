package org.citydb.query.builder.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.citydb.config.project.query.filter.projection.AbstractPropertyName;
import org.citydb.config.project.query.filter.projection.GenericAttributeName;
import org.citydb.config.project.query.filter.projection.ProjectionContext;
import org.citydb.config.project.query.filter.projection.PropertyName;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.query.filter.projection.ProjectionMode;
import org.citygml4j.model.citygml.CityGMLClass;

public class ProjectionFilterBuilder {
	private final SchemaMapping schemaMapping;

	protected ProjectionFilterBuilder(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	public List<ProjectionFilter> buildProjectionFilter(org.citydb.config.project.query.filter.projection.ProjectionFilter projectionFilterConfig) throws QueryBuildException {
		List<ProjectionFilter> projectionFilters = new ArrayList<>();

		for (ProjectionContext context : projectionFilterConfig.getProjectionContexts()) {
			// get feature type
			QName typeName = context.getTypeName();
			if (typeName == null)
				throw new QueryBuildException("Failed to retrieve the qualified name of the object type.");

			AbstractObjectType<?> objectType = schemaMapping.getAbstractObjectType(typeName);
			if (objectType == null)
				throw new QueryBuildException("'" + typeName + "' is not a valid object type.");

			if (objectType.isAbstract())
				throw new QueryBuildException("Projection filters must not be defined for abstract object types.");
			
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
			
			// create projection filter for feature type
			ProjectionFilter projectionFilter = new ProjectionFilter(objectType, mode);
			projectionFilters.add(projectionFilter);

			try {				
				// add properties and generic attributes
				if (context.isSetPropertyNames()) {
					for (AbstractPropertyName abstractPropertyName : context.getPropertyNames()) {
						if (abstractPropertyName instanceof PropertyName)
							projectionFilter.addProperty(((PropertyName)abstractPropertyName).getName());

						else if (abstractPropertyName instanceof GenericAttributeName) {
							GenericAttributeName genericAttributeName = (GenericAttributeName)abstractPropertyName;
							CityGMLClass type = genericAttributeName.isSetType() ? genericAttributeName.getType().getCityGMLClass() : null;
							projectionFilter.addGenericAttribute(genericAttributeName.getName(), type);
						}
					}
				}
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build the projection filter.", e);
			}
		}

		return projectionFilters;
	}

}
