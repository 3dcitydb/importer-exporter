package org.citydb.query.builder.config;

import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.sql.SelectOperator;

public class SelectOperatorBuilder {

    protected SelectOperatorBuilder() {

    }

    protected Predicate buildSelectOperator(org.citydb.config.project.query.filter.selection.sql.SelectOperator selectConfig) throws QueryBuildException {
        if (!selectConfig.isSetValue())
            throw new QueryBuildException("No select statement provided for the SQL filter.");

        return new SelectOperator(selectConfig.getValue());
    }
}
