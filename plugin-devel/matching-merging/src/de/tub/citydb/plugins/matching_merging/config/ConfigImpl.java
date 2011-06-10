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
