package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchType", propOrder={
		"lodReference",
		"lodMerge",
		"lineage",
		"showTable"
		})		
public class Match {
	@XmlElement(required=true)
	private int lodReference = 2;
	@XmlElement(required=true)
	private int lodMerge = 3;
	@XmlElement(required=true)
	private String lineage = "";
	@XmlElement(required=true)
	private boolean showTable = true;
	
	public int getLodReference() {
		return lodReference;
	}
	
	public void setLodReference(int lodReference) {
		if (lodReference >= 1 && lodReference <= 4)
			this.lodReference = lodReference;
	}
	
	public int getLodMerge() {
		return lodMerge;
	}
	
	public void setLodMerge(int lodMerge) {
		if (lodMerge >= 1 && lodMerge <= 4)
			this.lodMerge = lodMerge;
	}
	
	public String getLineage() {
		return lineage;
	}
	
	public void setLineage(String lineage) {
		this.lineage = lineage;
	}
	
	public boolean getShowTable() {
		return showTable;
	}
	
	public void setShowTable(boolean showTable) {
		this.showTable = showTable;
	}
}
