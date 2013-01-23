/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.plugins.matching_merging.config;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.api.plugin.extension.config.PluginConfig;

@XmlType(name="MatchingMegingConfigType", propOrder={
		"workspace",
		"candidateBuildings",
		"masterBuildings",		
		"matching",
		"merging",
		"deleteBuildings"
})
public class ConfigImpl extends PluginConfig {
	private Workspace workspace;
	private BuildingFilter candidateBuildings;
	private BuildingFilter masterBuildings;
	private Matching matching;
	private Merging merging;
	private DeleteBuildingsByLineage deleteBuildings;

	public ConfigImpl() {
		workspace = new Workspace();
		candidateBuildings = new BuildingFilter(3, 0.8f, 3);
		masterBuildings = new BuildingFilter(2, 0.8f, 3);
		matching = new Matching();
		merging = new Merging();
		deleteBuildings = new DeleteBuildingsByLineage();
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		if (workspace != null)
			this.workspace = workspace;
	}

	public Matching getMatching() {
		return matching;
	}

	public void setMatching(Matching matching) {
		if (matching != null)
			this.matching = matching;
	}

	public BuildingFilter getCandidateBuildings() {
		return candidateBuildings;
	}

	public void setCandidateBuildings(BuildingFilter candidateBuildings) {
		if (candidateBuildings != null)
			this.candidateBuildings = candidateBuildings;
	}

	public BuildingFilter getMasterBuildings() {
		return masterBuildings;
	}

	public void setMasterBuildings(BuildingFilter masterBuildings) {
		if (masterBuildings != null)
			this.masterBuildings = masterBuildings;
	}

	public Merging getMerging() {
		return merging;
	}

	public void setMerging(Merging merging) {
		if (merging != null)
			this.merging = merging;
	}

	public DeleteBuildingsByLineage getDeleteBuildingsByLineage() {
		return deleteBuildings;
	}

	public void setDeleteBuildingsByLineage(DeleteBuildingsByLineage deleteBuildings) {
		if (deleteBuildings != null)
			this.deleteBuildings = deleteBuildings;
	}
}
