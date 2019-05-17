package org.citydb.config.project.deleter;

import org.citydb.config.project.database.Workspace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DeleteConfigType", propOrder={
        "mode",
        "continuation",
        "workspace"
})
public class DeleteConfig {
    @XmlElement(required = true)
    private DeleteMode mode = DeleteMode.DELETE;
    private Continuation continuation;
    private Workspace workspace;

    public DeleteConfig() {
        continuation = new Continuation();
        workspace = new Workspace();
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

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        if (workspace != null)
            this.workspace = workspace;
    }

}
