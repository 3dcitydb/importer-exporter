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