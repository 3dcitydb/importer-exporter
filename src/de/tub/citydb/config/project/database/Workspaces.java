package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="WorkspacesType", propOrder={
		"importWorkspace",
		"exportWorkspace",
		"kmlExportWorkspace",
		"operationWorkspace",
		"matchingWorkspace"
})
public class Workspaces {
	@XmlElement(name="import", required=true)
	private Workspace importWorkspace;
	@XmlElement(name="export", required=true)
	private Workspace exportWorkspace;
	@XmlElement(name="kmlExport", required=true)
	private Workspace kmlExportWorkspace;
	@XmlElement(name="operations", required=true)
	private Workspace operationWorkspace;
	@XmlElement(name="matching", required=true)
	private Workspace matchingWorkspace;

	public Workspaces() {
		importWorkspace = new Workspace();
		exportWorkspace = new Workspace();
		kmlExportWorkspace = new Workspace();
		operationWorkspace = new Workspace();
		matchingWorkspace = new Workspace();
	}

	public Workspace getImportWorkspace() {
		return importWorkspace;
	}

	public void setImportWorkspace(Workspace importWorkspace) {
		if (importWorkspace != null)
			this.importWorkspace = importWorkspace;
	}

	public Workspace getExportWorkspace() {
		return exportWorkspace;
	}

	public void setExportWorkspace(Workspace exportWorkspace) {
		if (exportWorkspace != null)
			this.exportWorkspace = exportWorkspace;
	}

	public Workspace getOperationWorkspace() {
		return operationWorkspace;
	}

	public void setOperationWorkspace(Workspace operationWorkspace) {
		if (operationWorkspace != null)
			this.operationWorkspace = operationWorkspace;
	}

	public Workspace getMatchingWorkspace() {
		return matchingWorkspace;
	}

	public void setMatchingWorkspace(Workspace matchingWorkspace) {
		if (matchingWorkspace != null)
			this.matchingWorkspace = matchingWorkspace;
	}

	public void setKmlExportWorkspace(Workspace kmlExportWorkspace) {
		if (kmlExportWorkspace != null)
			this.kmlExportWorkspace = kmlExportWorkspace;
	}

	public Workspace getKmlExportWorkspace() {
		return kmlExportWorkspace;
	}

}
