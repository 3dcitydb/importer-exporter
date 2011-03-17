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
package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingType", propOrder={
		"candidateBuildings",
		"masterBuildings",		
		"matchConfig",
		"mergeConfig",
		"deleteBuildings"
})
public class Matching {
	private BuildingFilter candidateBuildings;
	private BuildingFilter masterBuildings;
	@XmlElement(name="match")
	private MatchConfig matchConfig;
	@XmlElement(name="merge")
	private MergeConfig mergeConfig;
	private DeleteBuildingsByLineage deleteBuildings;

	public Matching() {
		candidateBuildings = new BuildingFilter(3, 0.8f, 3);
		masterBuildings = new BuildingFilter(2, 0.8f, 3);
		matchConfig = new MatchConfig();
		mergeConfig = new MergeConfig();
		deleteBuildings = new DeleteBuildingsByLineage();
	}

	public MatchConfig getMatchConfig() {
		return matchConfig;
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

	public void setMatchConfig(MatchConfig matchConfig) {
		if (matchConfig != null)
			this.matchConfig = matchConfig;
	}

	public MergeConfig getMergeConfig() {
		return mergeConfig;
	}

	public void setMergeConfig(MergeConfig mergeConfig) {
		if (mergeConfig != null)
			this.mergeConfig = mergeConfig;
	}

	public DeleteBuildingsByLineage getDeleteBuildingsByLineage() {
		return deleteBuildings;
	}

	public void setDeleteBuildingsByLineage(DeleteBuildingsByLineage deleteBuildings) {
		if (deleteBuildings != null)
			this.deleteBuildings = deleteBuildings;
	}
}