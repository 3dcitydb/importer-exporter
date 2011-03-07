package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FeatureClassType", propOrder={
		"building",
		"waterBody",
		"landUse",
		"vegetation",
		"transportation",
		"reliefFeature",
		"cityFurniture",
		"genericCityObject",
		"cityObjectGroup"
		})
public class FilterFeatureClass {
	@XmlElement(defaultValue="true")
	private Boolean building = true;
	@XmlElement(defaultValue="true")
	private Boolean waterBody = true;
	@XmlElement(defaultValue="true")
	private Boolean landUse = true;
	@XmlElement(defaultValue="true")
	private Boolean vegetation = true;
	@XmlElement(defaultValue="true")
	private Boolean transportation = true;
	@XmlElement(defaultValue="true")
	private Boolean reliefFeature = true;
	@XmlElement(defaultValue="true")
	private Boolean cityFurniture = true;
	@XmlElement(defaultValue="true")
	private Boolean genericCityObject = true;
	@XmlElement(defaultValue="true")
	private Boolean cityObjectGroup = true;
	@XmlAttribute(required=true)
	private Boolean active = false;
	
	public FilterFeatureClass() {
	}

	public boolean isSetBuilding() {
		return building == true;
	}
	
	public Boolean getBuilding() {
		return building;
	}

	public void setBuilding(Boolean building) {
		this.building = building;
	}

	public boolean isSetWaterBody() {
		return waterBody == true;
	}
	
	public Boolean getWaterBody() {
		return waterBody;
	}

	public void setWaterBody(Boolean waterBody) {
		this.waterBody = waterBody;
	}

	public boolean isSetLandUse() {
		return landUse == true;
	}
	
	public Boolean getLandUse() {
		return landUse;
	}

	public void setLandUse(Boolean landUse) {
		this.landUse = landUse;
	}

	public boolean isSetVegetation() {
		return vegetation == true;
	}
	
	public Boolean getVegetation() {
		return vegetation;
	}

	public void setVegetation(Boolean vegetation) {
		this.vegetation = vegetation;
	}

	public boolean isSetTransportation() {
		return transportation == true;
	}
	
	public Boolean getTransportation() {
		return transportation;
	}

	public void setTransportation(Boolean transportation) {
		this.transportation = transportation;
	}

	public boolean isSetReliefFeature() {
		return reliefFeature == true;
	}
	
	public Boolean getReliefFeature() {
		return reliefFeature;
	}

	public void setReliefFeature(Boolean reliefFeature) {
		this.reliefFeature = reliefFeature;
	}
	
	public boolean isSetCityFurniture() {
		return cityFurniture == true;
	}
	
	public Boolean getCityFurniture() {
		return cityFurniture;
	}

	public void setCityFurniture(Boolean cityFurniture) {
		this.cityFurniture = cityFurniture;
	}

	public boolean isSetGenericCityObject() {
		return genericCityObject == true;
	}
	
	public Boolean getGenericCityObject() {
		return genericCityObject;
	}

	public void setGenericCityObject(Boolean genericCityObject) {
		this.genericCityObject = genericCityObject;
	}

	public boolean isSetCityObjectGroup() {
		return cityObjectGroup == true;
	}
	
	public Boolean getCityObjectGroup() {
		return cityObjectGroup;
	}

	public void setCityObjectGroup(Boolean cityObjectGroup) {
		this.cityObjectGroup = cityObjectGroup;
	}

	public boolean isSet() {
		return active == true;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
		
}
