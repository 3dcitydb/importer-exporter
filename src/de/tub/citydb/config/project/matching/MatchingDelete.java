package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingDeleteType", propOrder={
		"lineage"
		})		
public class MatchingDelete {
	@XmlElement(required=true)
	private String lineage = "";
	
	public String getLineage() {
		return lineage;
	}
	public void setLineage(String lineage) {
		this.lineage = lineage;
	}
}

