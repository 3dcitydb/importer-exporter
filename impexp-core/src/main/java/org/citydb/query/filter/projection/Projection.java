package org.citydb.query.filter.projection;

import org.citydb.database.schema.mapping.AbstractObjectType;

import java.util.concurrent.ConcurrentHashMap;

public class Projection {
    private ConcurrentHashMap<Integer, ProjectionFilter> projectionFilters;

    public Projection() {
        projectionFilters = new ConcurrentHashMap<>();
    }

    public void addProjectionFilter(ProjectionFilter filter) {
        if (filter != null) {
            projectionFilters.put(filter.getObjectType().getObjectClassId(), filter);

            if (filter.getObjectType().isSetExtension()) {
                ProjectionFilter parentFilter = projectionFilters.get(filter.getObjectType().getExtension().getBase().getObjectClassId());
                if (parentFilter != null && filter.getMode() == parentFilter.getMode()) {
                    filter.addProperties(parentFilter.getProperties());
                    filter.addGenericAttributes(parentFilter.getGenericAttributes());
                }
            }

            for (AbstractObjectType<?> child : filter.getObjectType().listSubTypes(false)) {
                ProjectionFilter childFilter = projectionFilters.get(child.getObjectClassId());
                if (childFilter == null) {
                    childFilter = new ProjectionFilter(child, filter.getMode(), filter.getProperties(), filter.getGenericAttributes());
                    projectionFilters.put(child.getObjectClassId(), childFilter);
                } else if (childFilter.getMode() == filter.getMode()) {
                    childFilter.addProperties(filter.getProperties());
                    childFilter.addGenericAttributes(filter.getGenericAttributes());
                }
            }
        }
    }

    public ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType) {
        return projectionFilters.computeIfAbsent(objectType.getObjectClassId(), v -> new ProjectionFilter(objectType));
    }
}
