package org.citydb.query.filter.selection.operator.sql;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.Operator;

public class SelectOperator implements Operator {
    private String select;

    public SelectOperator(String select) {
        this.select = select;
    }

    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    @Override
    public SQLOperatorName getOperatorName() {
        return SQLOperatorName.SELECT;
    }

    @Override
    public PredicateName getPredicateName() {
        return PredicateName.SQL_OPERATOR;
    }

    @Override
    public SelectOperator copy() throws FilterException {
        return new SelectOperator(select);
    }
}
