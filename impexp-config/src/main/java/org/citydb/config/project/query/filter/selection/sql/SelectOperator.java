package org.citydb.config.project.query.filter.selection.sql;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.PredicateName;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="sql")
@XmlType(name="SelectOperatorType", propOrder={
        "value"
})
public class SelectOperator extends AbstractPredicate {
    @XmlElement(name = "select", required = true)
    private String value;

    public String getValue() {
        return value;
    }

    public boolean isSetValue() {
        return value != null && value.trim().length() != 0;
    }

    public void setValue(String value) {
        if (value != null && value.trim().length() != 0)
            this.value = value.trim();
    }

    @Override
    public void reset() {
        value = null;
    }

    @Override
    public PredicateName getPredicateName() {
        return PredicateName.SQL_OPERATOR;
    }
}
