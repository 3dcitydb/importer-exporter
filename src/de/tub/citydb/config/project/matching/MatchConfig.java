package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchConfigType", propOrder={
		"tolerance",
		"lineage"
})		
public class MatchConfig {
	@XmlElement(required=true)
	private double tolerance = 0.001;
	@XmlElement(required=true)
	private String lineage = "";

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	public String getLineage() {
		return lineage;
	}

	public void setLineage(String lineage) {
		if (lineage != null)
			this.lineage = lineage;
	}
}
