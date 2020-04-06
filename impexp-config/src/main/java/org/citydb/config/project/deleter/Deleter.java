package org.citydb.config.project.deleter;

import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.Query;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DeleterType", propOrder={
		"query",
		"simpleQuery",
        "mode",
        "continuation"
})
public class Deleter {
	@XmlAttribute
	private boolean useSimpleQuery = true;
	private Query query;
	private SimpleQuery simpleQuery;
    @XmlElement(required = true)
    private DeleteMode mode = DeleteMode.DELETE;
    private Continuation continuation;

    public Deleter() {
		query = new Query();
		simpleQuery = new SimpleQuery();
        continuation = new Continuation();
    }
    
	public boolean isUseSimpleQuery() {
		return useSimpleQuery;
	}

	public void setUseSimpleQuery(boolean useSimpleQuery) {
		this.useSimpleQuery = useSimpleQuery;
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
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

    public Continuation getContinuation() {
        return continuation;
    }

    public void setContinuation(Continuation continuation) {
        if (continuation != null)
            this.continuation = continuation;
    }

}
