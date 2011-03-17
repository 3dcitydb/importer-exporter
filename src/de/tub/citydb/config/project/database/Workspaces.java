/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
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
