package org.citydb.query.builder.config;

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.util.ValueReferenceBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.sorting.SortOrder;
import org.citydb.query.filter.sorting.SortProperty;
import org.citydb.query.filter.sorting.Sorting;

public class SortingBuilder {
    private final ValueReferenceBuilder valueReferenceBuilder;

    protected SortingBuilder(ValueReferenceBuilder valueReferenceBuilder) {
        this.valueReferenceBuilder = valueReferenceBuilder;
    }

    protected Sorting buildSorting(org.citydb.config.project.query.filter.sorting.Sorting sortingConfig) throws QueryBuildException {
        try {
            Sorting sorting = new Sorting();

            for (org.citydb.config.project.query.filter.sorting.SortProperty sortBy : sortingConfig.getSortProperties()) {
                if (!sortBy.isSetValueReference())
                    throw new QueryBuildException("A sort property requires a value reference.");

                // build the value reference
                ValueReference valueReference = valueReferenceBuilder.buildValueReference(sortBy);
                if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
                    throw new QueryBuildException("The value reference of the sorting clause must point to a simple thematic attribute.");

                SortProperty sortProperty = new SortProperty(valueReference);

                switch (sortBy.getSortOrder()) {
                    case ASCENDING:
                        sortProperty.setSortOrder(SortOrder.ASCENDING);
                        break;
                    case DESCENDING:
                        sortProperty.setSortOrder(SortOrder.DESCENDING);
                        break;
                }

                sorting.addSortProperty(sortProperty);
            }

            return sorting;
        } catch (FilterException e) {
            throw new QueryBuildException("Failed to build the sorting clause.", e);
        }
    }
}
