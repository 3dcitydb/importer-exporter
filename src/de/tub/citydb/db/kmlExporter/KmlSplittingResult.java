package de.tub.citydb.db.kmlExporter;

import de.tub.citydb.config.project.kmlExporter.DisplayLevel;

public class KmlSplittingResult {
	private String gmlId;
	private DisplayLevel displayLevel;

	public KmlSplittingResult(String gmlId, DisplayLevel displayLevel) {
		this.gmlId = gmlId;
		this.setDisplayLevel(displayLevel);
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setDisplayLevel(DisplayLevel displayLevel) {
		this.displayLevel = displayLevel;
	}

	public DisplayLevel getDisplayLevel() {
		return displayLevel;
	}
		
}
