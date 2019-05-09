/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	@XmlElement(name="import")
	private Workspace importWorkspace;
	@XmlElement(name="export")
	private Workspace exportWorkspace;
	@XmlElement(name="kmlExport")
	private Workspace kmlExportWorkspace;
	@XmlElement(name="operations")
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
