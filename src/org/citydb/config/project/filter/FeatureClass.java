/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FeatureClassType", propOrder={
		"building",
		"bridge",
		"tunnel",
		"waterBody",
		"landUse",
		"vegetation",
		"plantCover",
		"solitaryVegetationObject",
		"transportation",
		"transportationComplex",
		"road",
		"square",
		"railway",
		"track",
		"reliefFeature",
		"cityFurniture",
		"genericCityObject",
		"cityObjectGroup"
})
public class FeatureClass {
	@XmlElement(defaultValue="true")
	private Boolean building = true;
	@XmlElement(defaultValue="true")
	private Boolean bridge = true;
	@XmlElement(defaultValue="true")
	private Boolean tunnel = true;
	@XmlElement(defaultValue="true")
	private Boolean waterBody = true;
	@XmlElement(defaultValue="true")
	private Boolean landUse = true;
	@XmlElement(defaultValue="true")
	private Boolean vegetation = true;
	@XmlElement(defaultValue="true")
	private Boolean plantCover = true;
	@XmlElement(defaultValue="true")
	private Boolean solitaryVegetationObject = true;
	@XmlElement(defaultValue="true")
	private Boolean transportation = true;
	@XmlElement(defaultValue="true")
	private Boolean transportationComplex = true;
	@XmlElement(defaultValue="true")
	private Boolean road = true;
	@XmlElement(defaultValue="true")
	private Boolean square = true;
	@XmlElement(defaultValue="true")
	private Boolean railway = true;
	@XmlElement(defaultValue="true")
	private Boolean track = true;
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

	public FeatureClass() {
	}

	public boolean isSetBuilding() {
		if (building != null)
			return building.booleanValue();

		return false;
	}

	public Boolean getBuilding() {
		return building;
	}

	public void setBuilding(Boolean building) {
		this.building = building;
	}
	
	public boolean isSetBridge() {
		if (bridge != null)
			return bridge.booleanValue();

		return false;
	}

	public Boolean getBridge() {
		return bridge;
	}

	public void setBridge(Boolean bridge) {
		this.bridge = bridge;
	}
	
	public boolean isSetTunnel() {
		if (tunnel != null)
			return tunnel.booleanValue();

		return false;
	}

	public Boolean getTunnel() {
		return tunnel;
	}

	public void setTunnel(Boolean tunnel) {
		this.tunnel = tunnel;
	}

	public boolean isSetWaterBody() {
		if (waterBody != null)
			return waterBody.booleanValue();

		return false;
	}

	public Boolean getWaterBody() {
		return waterBody;
	}

	public void setWaterBody(Boolean waterBody) {
		this.waterBody = waterBody;
	}

	public boolean isSetLandUse() {
		if (landUse != null)
			return landUse.booleanValue();

		return false;
	}

	public Boolean getLandUse() {
		return landUse;
	}

	public void setLandUse(Boolean landUse) {
		this.landUse = landUse;
	}

	public boolean isSetVegetation() {
		if (vegetation != null)
			return vegetation.booleanValue();

		return false;
	}

	public Boolean getVegetation() {
		return vegetation;
	}

	public void setVegetation(Boolean vegetation) {
		this.vegetation = vegetation;
	}

	public boolean isSetTransportation() {
		if (transportation != null)
			return transportation.booleanValue();

		return false;
	}

	public Boolean getTransportation() {
		return transportation;
	}

	public void setTransportation(Boolean transportation) {
		this.transportation = transportation;
	}
	
	public boolean isSetTransportationComplex() {
		if (transportationComplex != null)
			return transportationComplex.booleanValue();

		return false;
	}

	public Boolean getTransportationComplex() {
		return transportationComplex;
	}

	public void setTransportationComplex(Boolean transportationComplex) {
		this.transportationComplex = transportationComplex;
	}
	
	public Boolean getRoad() {
		return road;
	}

	public void setRoad(Boolean road) {
		this.road = road;
	}
	
	public boolean isSetRoad() {
		if (road != null)
			return road.booleanValue();

		return false;
	}

	public Boolean getSquare() {
		return square;
	}

	public void setSquare(Boolean square) {
		this.square = square;
	}
	
	public boolean isSetSquare() {
		if (square != null)
			return square.booleanValue();

		return false;
	}

	public Boolean getRailway() {
		return railway;
	}

	public void setRailway(Boolean railway) {
		this.railway = railway;
	}
	
	public boolean isSetRailway() {
		if (railway != null)
			return railway.booleanValue();

		return false;
	}

	public Boolean getTrack() {
		return track;
	}

	public void setTrack(Boolean track) {
		this.track = track;
	}

	public boolean isSetTrack() {
		if (track != null)
			return track.booleanValue();

		return false;
	}

	public Boolean getPlantCover() {
		return plantCover;
	}

	public void setPlantCover(Boolean plantCover) {
		this.plantCover = plantCover;
	}

	public boolean isSetPlantCover() {
		if (plantCover != null)
			return plantCover.booleanValue();

		return false;
	}
	
	public Boolean getSolitaryVegetationObject() {
		return solitaryVegetationObject;
	}

	public void setSolitaryVegetationObject(Boolean solitaryVegetationObject) {
		this.solitaryVegetationObject = solitaryVegetationObject;
	}
	
	public boolean isSetSolitaryVegetationObject() {
		if (solitaryVegetationObject != null)
			return solitaryVegetationObject.booleanValue();

		return false;
	}

	public boolean isSetReliefFeature() {
		if (reliefFeature != null)
			return reliefFeature.booleanValue();

		return false;
	}

	public Boolean getReliefFeature() {
		return reliefFeature;
	}

	public void setReliefFeature(Boolean reliefFeature) {
		this.reliefFeature = reliefFeature;
	}

	public boolean isSetCityFurniture() {
		if (cityFurniture != null)
			return cityFurniture.booleanValue();

		return false;
	}

	public Boolean getCityFurniture() {
		return cityFurniture;
	}

	public void setCityFurniture(Boolean cityFurniture) {
		this.cityFurniture = cityFurniture;
	}

	public boolean isSetGenericCityObject() {
		if (genericCityObject != null)
			return genericCityObject.booleanValue();

		return false;
	}

	public Boolean getGenericCityObject() {
		return genericCityObject;
	}

	public void setGenericCityObject(Boolean genericCityObject) {
		this.genericCityObject = genericCityObject;
	}

	public boolean isSetCityObjectGroup() {
		if (cityObjectGroup != null)
			return cityObjectGroup.booleanValue();

		return false;
	}

	public Boolean getCityObjectGroup() {
		return cityObjectGroup;
	}

	public void setCityObjectGroup(Boolean cityObjectGroup) {
		this.cityObjectGroup = cityObjectGroup;
	}

	public boolean isSet() {
		if (active != null)
			return active.booleanValue();

		return false;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
