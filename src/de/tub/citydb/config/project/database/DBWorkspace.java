package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="WorkspaceType", propOrder={
		"importWorkspace",
		"exportWorkspace",
		"exportDate",
		"reportWorkspace"
		})
public class DBWorkspace {
	@XmlElement(required=true, defaultValue="LIVE")
	private String importWorkspace = "LIVE";
	@XmlElement(required=true, defaultValue="LIVE")
	private String exportWorkspace = "LIVE";
	private String exportDate = "";
	@XmlElement(defaultValue="LIVE")
	private String reportWorkspace = "LIVE";
	
	public DBWorkspace() {
	}

	public String getImportWorkspace() {
		return importWorkspace;
	}

	public void setImportWorkspace(String importWorkspace) {
		this.importWorkspace = importWorkspace;
	}

	public String getExportWorkspace() {
		return exportWorkspace;
	}

	public void setExportWorkspace(String exportWorkspace) {
		this.exportWorkspace = exportWorkspace;
	}

	public String getExportDate() {
		return exportDate;
	}

	public void setExportDate(String exportDate) {
		this.exportDate = exportDate;
	}

	public String getReportWorkspace() {
		return reportWorkspace;
	}

	public void setReportWorkspace(String reportWorkspace) {
		this.reportWorkspace = reportWorkspace;
	}
	
}
