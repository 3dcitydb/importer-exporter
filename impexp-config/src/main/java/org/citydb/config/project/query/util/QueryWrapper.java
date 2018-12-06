package org.citydb.config.project.query.util;

import org.citydb.config.project.query.Query;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="wrapper")
@XmlType(name="QueryWrapperType", propOrder={
        "query"
})
public class QueryWrapper {
    private Query query;

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
