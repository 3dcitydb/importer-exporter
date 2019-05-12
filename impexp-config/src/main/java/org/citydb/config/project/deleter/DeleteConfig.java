package org.citydb.config.project.deleter;

import org.citydb.config.project.database.Workspace;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="DeleteConfigType", propOrder={
        "workspace"
})
public class DeleteConfig {
    private Workspace workspace;

    public DeleteConfig() {
        workspace = new Workspace();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        if (workspace != null)
            this.workspace = workspace;
    }
}
