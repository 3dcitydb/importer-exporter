package org.citydb.config.project.deleter;

import org.citydb.config.project.database.Workspace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;

@XmlType(name="DeleteConfigType", propOrder={
        "workspace"
})
public class DeleteConfig {
    @XmlElement(required = true)
    private DeleteMode mode = DeleteMode.DELETE;
    private Workspace workspace;

    @XmlTransient
    LocalDateTime terminationDate;
    @XmlTransient
    private String updatingPerson;
    @XmlTransient
    String reasonForUpdate;
    @XmlTransient
    private String lineage;

    public DeleteConfig() {
        workspace = new Workspace();
    }

    public DeleteMode getMode() {
        return mode != null ? mode : DeleteMode.DELETE;
    }

    public void setMode(DeleteMode mode) {
        this.mode = mode;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        if (workspace != null)
            this.workspace = workspace;
    }

    public boolean isSetTerminationDate() {
        return terminationDate != null;
    }

    public LocalDateTime getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDateTime terminationDate) {
        this.terminationDate = terminationDate;
    }

    public boolean isSetReasonForUpdate() {
        return reasonForUpdate != null;
    }

    public boolean isSetUpdatingPerson() {
        return updatingPerson != null;
    }

    public String getUpdatingPerson() {
        return updatingPerson;
    }

    public void setUpdatingPerson(String updatingPerson) {
        this.updatingPerson = updatingPerson;
    }

    public String getReasonForUpdate() {
        return reasonForUpdate;
    }

    public void setReasonForUpdate(String reasonForUpdate) {
        this.reasonForUpdate = reasonForUpdate;
    }

    public boolean isSetLineage() {
        return lineage != null;
    }

    public String getLineage() {
        return lineage;
    }

    public void setLineage(String lineage) {
        this.lineage = lineage;
    }
}
