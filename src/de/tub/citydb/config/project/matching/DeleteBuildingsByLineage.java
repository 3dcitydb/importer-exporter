package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DeleteBuildingsByLineageType", propOrder={
		"lineage"
		})		
public class DeleteBuildingsByLineage {
	@XmlElement(required=true)
	private String lineage = "";
	
	public String getLineage() {
		return lineage;
	}
	public void setLineage(String lineage) {
		this.lineage = lineage;
	}
}

