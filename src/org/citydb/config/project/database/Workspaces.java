/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.config.project.database;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="WorkspacesType", propOrder={
		"importWorkspace",
		"exportWorkspace",
		"kmlExportWorkspace",
		"operationWorkspace"
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

	public Workspaces() {
		importWorkspace = new Workspace();
		exportWorkspace = new Workspace();
		kmlExportWorkspace = new Workspace();
		operationWorkspace = new Workspace();
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

	public void setKmlExportWorkspace(Workspace kmlExportWorkspace) {
		if (kmlExportWorkspace != null)
			this.kmlExportWorkspace = kmlExportWorkspace;
	}

	public Workspace getKmlExportWorkspace() {
		return kmlExportWorkspace;
	}
	
	public Workspace getOperationWorkspace() {
		return operationWorkspace;
	}

	public void setOperationWorkspace(Workspace operationWorkspace) {
		if (operationWorkspace != null)
			this.operationWorkspace = operationWorkspace;
	}

}
