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
package org.citydb.modules.kml.database;

import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.modules.kml.util.CityObject4JSON;
import org.citygml4j.model.citygml.CityGMLClass;

public class KmlSplittingResult {

	private long id;
	private String gmlId;
	private DisplayForm displayForm;
	private CityGMLClass cityObjectType;
	private CityObject4JSON json;

	public KmlSplittingResult(long id, String gmlId, CityGMLClass cityObjectType, CityObject4JSON json, DisplayForm displayForm) {
		this.setId(id);
		this.setGmlId(gmlId);
		this.setCityObjectType(cityObjectType);
		this.setDisplayForm(displayForm);
		this.setJson(json);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setDisplayForm(DisplayForm displayForm) {
		this.displayForm = displayForm;
	}

	public DisplayForm getDisplayForm() {
		return displayForm;
	}

	public CityGMLClass getCityObjectType() {
		return cityObjectType;
	}

	public void setCityObjectType(CityGMLClass cityObjectType) {
		this.cityObjectType = cityObjectType;
	}
	
	public CityObject4JSON getJson() {
		return json;
	}

	public void setJson(CityObject4JSON json) {
		this.json = json;
	}

	public boolean isBuilding() {
		return getCityObjectType().compareTo(CityGMLClass.BUILDING) == 0;
	}
		
	public boolean isCityObjectGroup() {
		return getCityObjectType().compareTo(CityGMLClass.CITY_OBJECT_GROUP) == 0;
	}

	public boolean isVegetation() {
		return (getCityObjectType().compareTo(CityGMLClass.SOLITARY_VEGETATION_OBJECT) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.PLANT_COVER) == 0);
	}

	public boolean isGenericCityObject() {
		return getCityObjectType().compareTo(CityGMLClass.GENERIC_CITY_OBJECT) == 0;
	}

	public boolean isCityFurniture() {
		return getCityObjectType().compareTo(CityGMLClass.CITY_FURNITURE) == 0;
	}

	public boolean isWaterBody() {
		return (getCityObjectType().compareTo(CityGMLClass.WATER_BODY) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.WATER_CLOSURE_SURFACE) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.WATER_GROUND_SURFACE) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.WATER_SURFACE) == 0);
	}

	public boolean isLandUse() {
		return getCityObjectType().compareTo(CityGMLClass.LAND_USE) == 0;
	}

	public boolean isTransportation() {
		return (isTrafficArea() || isTransportationComplex());
	}

	public boolean isTrafficArea() {
		return (getCityObjectType().compareTo(CityGMLClass.TRAFFIC_AREA) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.AUXILIARY_TRAFFIC_AREA) == 0);
	}

	public boolean isTransportationComplex() {
		return (getCityObjectType().compareTo(CityGMLClass.TRANSPORTATION_COMPLEX) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.TRACK) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.RAILWAY) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.ROAD) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.SQUARE) == 0);
	}

	public boolean isRelief() {
		return (getCityObjectType().compareTo(CityGMLClass.RELIEF_FEATURE) == 0 /* ||
				getCityObjectType().compareTo(CityGMLClass.RASTER_RELIEF) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.MASSPOINT_RELIEF) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.BREAKLINE_RELIEF) == 0 ||
				getCityObjectType().compareTo(CityGMLClass.TIN_RELIEF) == 0 */);
	}
	public boolean isBridge() {
		return getCityObjectType().compareTo(CityGMLClass.BRIDGE) == 0;
	}
	public boolean isTunnel() {
		return getCityObjectType().compareTo(CityGMLClass.TUNNEL) == 0;
	}
}
