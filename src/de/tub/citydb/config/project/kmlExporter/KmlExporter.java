/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.kmlExporter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.general.Path;
import de.tub.citydb.config.project.system.System;

@XmlType(name="KmlExportType", propOrder={
		"path",
		"filter",
		"lodToExportFrom",

		"buildingDisplayForms",
		"buildingColladaOptions",
		"buildingBalloon",
		"waterBodyDisplayForms",
		"waterBodyColladaOptions",
		"waterBodyBalloon",
		"vegetationDisplayForms",
		"vegetationColladaOptions",
		"vegetationBalloon",
		"cityFurnitureDisplayForms",
		"cityFurnitureColladaOptions",
		"cityFurnitureBalloon",
		"genericCityObjectDisplayForms",
		"genericCityObjectColladaOptions",
		"genericCityObjectBalloon",
		"cityObjectGroupDisplayForms",
		"cityObjectGroupBalloon",

		"exportAsKmz",
		"showBoundingBox",
		"showTileBorders",
		"autoTileSideLength",
		"oneFilePerObject",
		"singleObjectRegionSize",
		"viewRefreshMode",
		"viewRefreshTime",
		"writeJSONFile",
		"appearanceTheme",
		"altitudeMode",
		"altitudeOffsetMode",
		"altitudeOffsetValue",
		"callGElevationService",
		"useOriginalZCoords",
		"system"
})
public class KmlExporter {
	private Path path;
	private ExportFilterConfig filter;
	private int lodToExportFrom;

	@XmlElement(name="displayForm", required=true)
	@XmlElementWrapper(name="buildingDisplayForms")	
	private List<DisplayForm> buildingDisplayForms;
	private ColladaOptions buildingColladaOptions;
	private Balloon buildingBalloon;
	@XmlElement(name="displayForm", required=true)
	@XmlElementWrapper(name="waterBodyDisplayForms")	
	private List<DisplayForm> waterBodyDisplayForms;
	private ColladaOptions waterBodyColladaOptions;
	private Balloon waterBodyBalloon;
	@XmlElement(name="displayForm", required=true)
	@XmlElementWrapper(name="vegetationDisplayForms")	
	private List<DisplayForm> vegetationDisplayForms;
	private ColladaOptions vegetationColladaOptions;
	private Balloon vegetationBalloon;
	@XmlElement(name="displayForm", required=true)
	@XmlElementWrapper(name="cityFurnitureDisplayForms")	
	private List<DisplayForm> cityFurnitureDisplayForms;
	private ColladaOptions cityFurnitureColladaOptions;
	private Balloon cityFurnitureBalloon;
	@XmlElement(name="displayForm", required=true)
	@XmlElementWrapper(name="genericCityObjectDisplayForms")	
	private List<DisplayForm> genericCityObjectDisplayForms;
	private ColladaOptions genericCityObjectColladaOptions;
	private Balloon genericCityObjectBalloon;
	@XmlElement(name="displayForm", required=true)
	@XmlElementWrapper(name="cityObjectGroupDisplayForms")	
	private List<DisplayForm> cityObjectGroupDisplayForms;
	private Balloon cityObjectGroupBalloon;

	private boolean exportAsKmz;
	private boolean showBoundingBox;
	private boolean showTileBorders;
	private double autoTileSideLength;
	private boolean oneFilePerObject;
	private double singleObjectRegionSize;
	private String viewRefreshMode;
	private double viewRefreshTime;
	private boolean writeJSONFile;
	private String appearanceTheme;
	private AltitudeMode altitudeMode;
	private AltitudeOffsetMode altitudeOffsetMode;
	private double altitudeOffsetValue;
	private boolean callGElevationService;
	private boolean useOriginalZCoords;
	private System system;

	public static final String THEME_NONE = "none";

	public KmlExporter() {
		path = new Path();
		filter = new ExportFilterConfig();
		lodToExportFrom = 2;

		setBuildingDisplayForms(new ArrayList<DisplayForm>());
		setBuildingColladaOptions(new ColladaOptions());
		setBuildingBalloon(new Balloon());
		setWaterBodyDisplayForms(new ArrayList<DisplayForm>());
		setWaterBodyColladaOptions(new ColladaOptions());
		setWaterBodyBalloon(new Balloon());
		setVegetationDisplayForms(new ArrayList<DisplayForm>());
		setVegetationColladaOptions(new ColladaOptions());
		setVegetationBalloon(new Balloon());
		setCityFurnitureDisplayForms(new ArrayList<DisplayForm>());
		setCityFurnitureColladaOptions(new ColladaOptions());
		setCityFurnitureBalloon(new Balloon());
		setGenericCityObjectDisplayForms(new ArrayList<DisplayForm>());
		setGenericCityObjectColladaOptions(new ColladaOptions());
		setGenericCityObjectBalloon(new Balloon());
		setCityObjectGroupDisplayForms(new ArrayList<DisplayForm>());
		setCityObjectGroupBalloon(new Balloon());

		exportAsKmz = true;
		showBoundingBox = true;
		showTileBorders = true;
		autoTileSideLength = 125.0;
		oneFilePerObject = false;
		singleObjectRegionSize = 50.0;
		viewRefreshMode = "onRegion";
		viewRefreshTime = 1;
		writeJSONFile = false;
		setAppearanceTheme(THEME_NONE);
		setAltitudeMode(AltitudeMode.ABSOLUTE);
		setAltitudeOffsetMode(AltitudeOffsetMode.GENERIC_ATTRIBUTE);
		altitudeOffsetValue = 0;
		callGElevationService = true;
		setUseOriginalZCoords(false);
		system = new System();
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		if (path != null)
			this.path = path;
	}

	public System getSystem() {
		return system;
	}

	public void setSystem(System system) {
		if (system != null)
			this.system = system;
	}

	public void setFilter(ExportFilterConfig filter) {
		if (filter != null)
			this.filter = filter;
	}

	public ExportFilterConfig getFilter() {
		return filter;
	}

	public void setLodToExportFrom(int lodToExportFrom) {
		this.lodToExportFrom = lodToExportFrom;
	}

	public int getLodToExportFrom() {
		return lodToExportFrom;
	}

	public void setBuildingDisplayForms(List<DisplayForm> buildingDisplayForms) {
		this.buildingDisplayForms = buildingDisplayForms;
	}

	public List<DisplayForm> getBuildingDisplayForms() {
		return buildingDisplayForms;
	}

	public void setBuildingColladaOptions(ColladaOptions buildingColladaOptions) {
		this.buildingColladaOptions = buildingColladaOptions;
	}

	public ColladaOptions getBuildingColladaOptions() {
		return buildingColladaOptions;
	}

	public void setWaterBodyDisplayForms(List<DisplayForm> waterBodyDisplayForms) {
		this.waterBodyDisplayForms = waterBodyDisplayForms;
	}

	public List<DisplayForm> getWaterBodyDisplayForms() {
		return waterBodyDisplayForms;
	}

	public void setWaterBodyColladaOptions(ColladaOptions waterBodyColladaOptions) {
		this.waterBodyColladaOptions = waterBodyColladaOptions;
	}

	public ColladaOptions getWaterBodyColladaOptions() {
		return waterBodyColladaOptions;
	}

	public void setCityObjectGroupDisplayForms(List<DisplayForm> cityObjectGroupDisplayForms) {
		this.cityObjectGroupDisplayForms = cityObjectGroupDisplayForms;
	}

	public List<DisplayForm> getCityObjectGroupDisplayForms() {
		return cityObjectGroupDisplayForms;
	}

	public void setVegetationDisplayForms(List<DisplayForm> vegetationDisplayForms) {
		this.vegetationDisplayForms = vegetationDisplayForms;
	}

	public List<DisplayForm> getVegetationDisplayForms() {
		return vegetationDisplayForms;
	}

	public void setVegetationColladaOptions(ColladaOptions vegetationColladaOptions) {
		this.vegetationColladaOptions = vegetationColladaOptions;
	}

	public ColladaOptions getVegetationColladaOptions() {
		return vegetationColladaOptions;
	}

	public static int getActiveDisplayFormsAmount(List<DisplayForm> displayForms) {
		int activeAmount = 0; 
		for (DisplayForm displayForm : displayForms) {
			if (displayForm.isActive()) activeAmount++;
		}
		return activeAmount;
	}

	public void setExportAsKmz(boolean exportAsKmz) {
		this.exportAsKmz = exportAsKmz;
	}

	public boolean isExportAsKmz() {
		return exportAsKmz;
	}

	public void setShowBoundingBox(boolean showBoundingBox) {
		this.showBoundingBox = showBoundingBox;
	}

	public boolean isShowBoundingBox() {
		return showBoundingBox;
	}

	public void setShowTileBorders(boolean showTileBorders) {
		this.showTileBorders = showTileBorders;
	}

	public boolean isShowTileBorders() {
		return showTileBorders;
	}

	public void setAppearanceTheme(String appearanceTheme) {
		this.appearanceTheme = appearanceTheme;
	}

	public String getAppearanceTheme() {
		return appearanceTheme;
	}

	public void setAltitudeMode(AltitudeMode altitudeMode) {
		this.altitudeMode = altitudeMode;
	}

	public AltitudeMode getAltitudeMode() {
		return altitudeMode;
	}

	public void setAltitudeOffsetMode(AltitudeOffsetMode altitudeOffsetMode) {
		this.altitudeOffsetMode = altitudeOffsetMode;
	}

	public AltitudeOffsetMode getAltitudeOffsetMode() {
		return altitudeOffsetMode;
	}

	public void setAltitudeOffsetValue(double altitudeOffsetValue) {
		this.altitudeOffsetValue = altitudeOffsetValue;
	}

	public double getAltitudeOffsetValue() {
		return altitudeOffsetValue;
	}

	public void setCallGElevationService(boolean callGElevationService) {
		this.callGElevationService = callGElevationService;
	}

	public boolean isCallGElevationService() {
		return callGElevationService;
	}

	public void setAutoTileSideLength(double autoTileSideLength) {
		this.autoTileSideLength = autoTileSideLength;
	}

	public double getAutoTileSideLength() {
		return autoTileSideLength;
	}

	public void setWriteJSONFile(boolean writeJSONFile) {
		this.writeJSONFile = writeJSONFile;
	}

	public boolean isWriteJSONFile() {
		return writeJSONFile;
	}

	public void setOneFilePerObject(boolean oneFilePerObject) {
		this.oneFilePerObject = oneFilePerObject;
	}

	public boolean isOneFilePerObject() {
		return oneFilePerObject;
	}

	public void setSingleObjectRegionSize(double singleObjectRegionSize) {
		this.singleObjectRegionSize = singleObjectRegionSize;
	}

	public double getSingleObjectRegionSize() {
		return singleObjectRegionSize;
	}

	public void setViewRefreshMode(String viewRefreshMode) {
		this.viewRefreshMode = viewRefreshMode;
	}

	public String getViewRefreshMode() {
		return viewRefreshMode;
	}

	public void setViewRefreshTime(double viewRefreshTime) {
		this.viewRefreshTime = viewRefreshTime;
	}

	public double getViewRefreshTime() {
		return viewRefreshTime;
	}

	public void setUseOriginalZCoords(boolean useOriginalZCoords) {
		this.useOriginalZCoords = useOriginalZCoords;
	}

	public boolean isUseOriginalZCoords() {
		return useOriginalZCoords;
	}

	public void setBuildingBalloon(Balloon buildingBalloon) {
		this.buildingBalloon = buildingBalloon;
	}

	public Balloon getBuildingBalloon() {
		return buildingBalloon;
	}

	public void setWaterBodyBalloon(Balloon waterBodyBalloon) {
		this.waterBodyBalloon = waterBodyBalloon;
	}

	public Balloon getWaterBodyBalloon() {
		return waterBodyBalloon;
	}

	public void setCityObjectGroupBalloon(Balloon cityObjectGroupBalloon) {
		this.cityObjectGroupBalloon = cityObjectGroupBalloon;
	}

	public Balloon getCityObjectGroupBalloon() {
		return cityObjectGroupBalloon;
	}

	public void setVegetationBalloon(Balloon vegetationBalloon) {
		this.vegetationBalloon = vegetationBalloon;
	}

	public Balloon getVegetationBalloon() {
		return vegetationBalloon;
	}

	public void setGenericCityObjectDisplayForms(
			List<DisplayForm> genericCityObjectDisplayForms) {
		this.genericCityObjectDisplayForms = genericCityObjectDisplayForms;
	}

	public List<DisplayForm> getGenericCityObjectDisplayForms() {
		return genericCityObjectDisplayForms;
	}

	public void setGenericCityObjectColladaOptions(
			ColladaOptions genericCityObjectColladaOptions) {
		this.genericCityObjectColladaOptions = genericCityObjectColladaOptions;
	}

	public ColladaOptions getGenericCityObjectColladaOptions() {
		return genericCityObjectColladaOptions;
	}

	public void setGenericCityObjectBalloon(Balloon genericCityObjectBalloon) {
		this.genericCityObjectBalloon = genericCityObjectBalloon;
	}

	public Balloon getGenericCityObjectBalloon() {
		return genericCityObjectBalloon;
	}

	public void setCityFurnitureDisplayForms(
			List<DisplayForm> cityFurnitureDisplayForms) {
		this.cityFurnitureDisplayForms = cityFurnitureDisplayForms;
	}

	public List<DisplayForm> getCityFurnitureDisplayForms() {
		return cityFurnitureDisplayForms;
	}

	public void setCityFurnitureColladaOptions(
			ColladaOptions cityFurnitureColladaOptions) {
		this.cityFurnitureColladaOptions = cityFurnitureColladaOptions;
	}

	public ColladaOptions getCityFurnitureColladaOptions() {
		return cityFurnitureColladaOptions;
	}

	public void setCityFurnitureBalloon(Balloon cityFurnitureBalloon) {
		this.cityFurnitureBalloon = cityFurnitureBalloon;
	}

	public Balloon getCityFurnitureBalloon() {
		return cityFurnitureBalloon;
	}

}
