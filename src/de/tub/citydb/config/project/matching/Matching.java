package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingType", propOrder={
		"match",
		"merge",
		"deleteBuildings",
		"settings"
		})
		
public class Matching {
	private Match match;
	private Merge merge;
	private MatchingDelete deleteBuildings;
	private MatchingSettings settings;

	public Matching() {
		match = new Match();
		merge = new Merge();
		deleteBuildings = new MatchingDelete();
		settings = new MatchingSettings();
	}

	public Match getMatch() {
		return match;
	}

	public void setMatch(Match match) {
		if (match != null)
			this.match = match;
	}
	
	public Merge getMerge() {
		return merge;
	}

	public void setMerge(Merge merge) {
		if (merge != null)
			this.merge = merge;
	}
	
	public MatchingDelete getMatchingDelete() {
		return deleteBuildings;
	}

	public void setMatchingDelete(MatchingDelete deleteBuildings) {
		if (deleteBuildings != null)
			this.deleteBuildings = deleteBuildings;
	}
	
	public MatchingSettings getMatchingSettings() {
		return settings;
	}

	public void setMatchingSettings(MatchingSettings matchPref) {
		if (matchPref != null)
			this.settings = matchPref;
	}
}