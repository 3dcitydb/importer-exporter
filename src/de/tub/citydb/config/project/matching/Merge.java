package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MergeType", propOrder={
		"overlapOfMerge",
		"overlapOfReference",
		"lodReference",
		"lodMerge",
		"lineage"
		})		
public class Merge {
	@XmlElement(required=true)
	private float overlapOfMerge = 0.8f;
	@XmlElement(required=true)
	private float overlapOfReference = 0.8f;
	@XmlElement(required=true)
	private int lodReference = 3;
	@XmlElement(required=true)
	private int lodMerge = 3;
	@XmlElement(required=true)
	private String lineage = "";
	
	public Float getOverlapOfMerge() {
		return overlapOfMerge;
	}
	public void setOverlapOfMerge(float overlapOfMerge) {
		if (overlapOfMerge >= 0.0 && overlapOfMerge <= 1.0)
			this.overlapOfMerge = overlapOfMerge;
	}
	
	public Float getOverlapOfReference() {
		return overlapOfReference;
	}
	public void setOverlapOfReference(float overlapOfReference) {
		if (overlapOfReference >= 0.0 && overlapOfReference <= 1.0)
			this.overlapOfReference = overlapOfReference;
	}
	
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
}

