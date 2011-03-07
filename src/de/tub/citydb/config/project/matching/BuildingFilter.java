package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BuildingFilterType", propOrder={
		"lodProjection",
		"overlap",
		"lodGeometry"
		})
public class BuildingFilter {
	@XmlElement(required=true)
	private int lodProjection = 2;
	@XmlElement(required=true)
	private double overlap = 0.8f;
	@XmlElement(required=true)
	private int lodGeometry = 3;
	
	public BuildingFilter() {
	}
	
	public BuildingFilter(int lodProjection, float overlap, int lodGeometry) {
		this.lodProjection = lodProjection;
		this.overlap = overlap;
		this.lodGeometry = lodGeometry;
	}
	
	public int getLodProjection() {
		return lodProjection;
	}
	
	public void setLodProjection(int lodProjection) {
		if (lodProjection >= 1 && lodProjection <= 4)
			this.lodProjection = lodProjection;
	}	
	
	public double getOverlap() {
		return overlap;
	}
	
	public void setOverlap(double overlap) {
		if (overlap >= 0.0 && overlap <= 1.0)
			this.overlap = overlap;
	}
	
	public int getLodGeometry() {
		return lodGeometry;
	}
	
	public void setLodGeometry(int lodGeometry) {
		if (lodGeometry >= 1 && lodGeometry <= 4)
			this.lodGeometry = lodGeometry;
	}
	
}
