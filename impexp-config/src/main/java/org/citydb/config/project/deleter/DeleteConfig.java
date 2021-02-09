package org.citydb.config.project.deleter;

import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.QueryConfig;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "delete")
@XmlType(name = "DeleteType", propOrder = {
        "query",
        "simpleQuery",
        "mode",
        "cleanupGlobalAppearances",
        "continuation"
})
public class DeleteConfig {
    @XmlAttribute
    private boolean useSimpleQuery = true;
    private QueryConfig query;
    private SimpleQuery simpleQuery;
    @XmlElement(required = true)
    private DeleteMode mode = DeleteMode.DELETE;
    private boolean cleanupGlobalAppearances;
    private Continuation continuation;

    public DeleteConfig() {
        query = new QueryConfig();
        simpleQuery = new SimpleQuery();
        continuation = new Continuation();
    }

    public boolean isUseSimpleQuery() {
        return useSimpleQuery;
    }

    public void setUseSimpleQuery(boolean useSimpleQuery) {
        this.useSimpleQuery = useSimpleQuery;
    }

    public QueryConfig getQuery() {
        return query;
    }

    public void setQuery(QueryConfig query) {
        if (query != null)
            this.query = query;
    }

    public SimpleQuery getSimpleQuery() {
        return simpleQuery;
    }

    public void setSimpleQuery(SimpleQuery query) {
        if (query != null)
            this.simpleQuery = query;
    }

    public DeleteMode getMode() {
        return mode != null ? mode : DeleteMode.DELETE;
    }

    public void setMode(DeleteMode mode) {
        this.mode = mode;
    }

    public boolean isCleanupGlobalAppearances() {
        return cleanupGlobalAppearances;
    }

    public void setCleanupGlobalAppearances(boolean cleanupGlobalAppearances) {
        this.cleanupGlobalAppearances = cleanupGlobalAppearances;
    }

    public Continuation getContinuation() {
        return continuation;
    }

    public void setContinuation(Continuation continuation) {
        if (continuation != null)
            this.continuation = continuation;
    }

}
