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
		"displayLevels",
		"exportAsKmz",
		"showTileBorders",
		"autoTileSideLength",
		"footprintHighlighting",
		"geometryHighlighting",
		"geometryHighlightingDistance",
		"appearanceTheme",
		"colladaHighlighting",
		"colladaHighlightingDistance",
		"generateTextureAtlases",
		"packingAlgorithm",
		"scaleImages",
		"imageScaleFactor",
		"groupBuildings",
		"includeDescription",
		"balloonContentMode",
		"balloonContentPath",
		"balloonContentTemplateFile",
		"balloonContentInSeparateFile",
		"altitudeMode",
		"altitudeOffsetMode",
		"altitudeOffsetValue",
		"callGElevationService",
		"groupSize",
		"system"
})
public class KmlExporter {
	private Path path;
	private ExportFilterConfig filter;
	private int lodToExportFrom;
	@XmlElement(name="displayLevel", required=true)
	@XmlElementWrapper(name="displayLevels")	
	private List<DisplayLevel> displayLevels;
	private boolean exportAsKmz;
	private boolean showTileBorders;
	private double autoTileSideLength;
	private boolean footprintHighlighting;
	private boolean geometryHighlighting;
	private double geometryHighlightingDistance;
	private String appearanceTheme;
	private boolean colladaHighlighting;
	private double colladaHighlightingDistance;
	private boolean generateTextureAtlases;
	private int packingAlgorithm;
	private boolean scaleImages;
	private double imageScaleFactor;
	private boolean groupBuildings;
	private boolean includeDescription;
	private BalloonContentMode balloonContentMode;
	private Path balloonContentPath;
	private String balloonContentTemplateFile;
	private boolean balloonContentInSeparateFile;
	private AltitudeMode altitudeMode;
	private AltitudeOffsetMode altitudeOffsetMode;
	private double altitudeOffsetValue;
	private boolean callGElevationService;
	private int groupSize;
	private System system;

	public KmlExporter() {
		path = new Path();
		filter = new ExportFilterConfig();
		lodToExportFrom = 2;
		displayLevels = new ArrayList<DisplayLevel>();
		exportAsKmz = true;
		showTileBorders = true;
		autoTileSideLength = 125.0;
		footprintHighlighting = false;
		geometryHighlighting = false;
		setGeometryHighlightingDistance(0.75);
		setAppearanceTheme("visual");
		colladaHighlighting = true;
		setColladaHighlightingDistance(0.75);
		generateTextureAtlases = true;
		packingAlgorithm = 5; // TextureAtlasGenerator.TPIM
		scaleImages = false;
		imageScaleFactor = 1.0;
		groupBuildings = false;
		includeDescription = true;
		setBalloonContentMode(BalloonContentMode.GEN_ATTRIB);
		balloonContentPath = new Path();
		balloonContentTemplateFile = "";
		balloonContentInSeparateFile = false;
		setAltitudeMode(AltitudeMode.ABSOLUTE);
		setAltitudeOffsetMode(AltitudeOffsetMode.GENERIC_ATTRIBUTE);
		altitudeOffsetValue = 0;
		callGElevationService = true;
		groupSize = 1;
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

	public void setDisplayLevels(List<DisplayLevel> displayLevels) {
		this.displayLevels = displayLevels;
	}

	public List<DisplayLevel> getDisplayLevels() {
		return displayLevels;
	}

	public int getActiveDisplayLevelAmount() {
		int activeAmount = 0; 
		for (DisplayLevel displayLevel : displayLevels) {
			if (displayLevel.isActive()) activeAmount++;
		}
		return activeAmount;
	}

	public void setExportAsKmz(boolean exportAsKmz) {
		this.exportAsKmz = exportAsKmz;
	}

	public boolean isExportAsKmz() {
		return exportAsKmz;
	}

	public void setGenerateTextureAtlases(boolean generateTextureAtlases) {
		this.generateTextureAtlases = generateTextureAtlases;
	}

	public boolean isGenerateTextureAtlases() {
		return generateTextureAtlases;
	}

	public void setImageScaleFactor(double imageScaleFactor) {
		this.imageScaleFactor = imageScaleFactor;
	}

	public double getImageScaleFactor() {
		return imageScaleFactor;
	}

	public void setGroupSize(int groupSize) {
		this.groupSize = groupSize;
	}

	public int getGroupSize() {
		return groupSize;
	}

	public void setShowTileBorders(boolean showTileBorders) {
		this.showTileBorders = showTileBorders;
	}

	public boolean isShowTileBorders() {
		return showTileBorders;
	}

	public void setIncludeDescription(boolean includeDescription) {
		this.includeDescription = includeDescription;
	}

	public boolean isIncludeDescription() {
		return includeDescription;
	}

	public void setScaleImages(boolean scaleImages) {
		this.scaleImages = scaleImages;
	}

	public boolean isScaleImages() {
		return scaleImages;
	}

	public void setGroupBuildings(boolean groupBuildings) {
		this.groupBuildings = groupBuildings;
	}

	public boolean isGroupBuildings() {
		return groupBuildings;
	}

	public void setFootprintHighlighting(boolean footprintHighlighting) {
		this.footprintHighlighting = footprintHighlighting;
	}

	public boolean isFootprintHighlighting() {
		return footprintHighlighting;
	}

	public void setGeometryHighlighting(boolean geometryHighlighting) {
		this.geometryHighlighting = geometryHighlighting;
	}

	public boolean isGeometryHighlighting() {
		return geometryHighlighting;
	}

	public void setGeometryHighlightingDistance(double geometryHighlightingDistance) {
		this.geometryHighlightingDistance = geometryHighlightingDistance;
	}

	public double getGeometryHighlightingDistance() {
		return geometryHighlightingDistance;
	}

	public void setAppearanceTheme(String appearanceTheme) {
		this.appearanceTheme = appearanceTheme;
	}

	public String getAppearanceTheme() {
		return appearanceTheme;
	}

	public void setColladaHighlighting(boolean colladaHighlighting) {
		this.colladaHighlighting = colladaHighlighting;
	}

	public boolean isColladaHighlighting() {
		return colladaHighlighting;
	}

	public void setColladaHighlightingDistance(double colladaHighlightingDistance) {
		this.colladaHighlightingDistance = colladaHighlightingDistance;
	}

	public double getColladaHighlightingDistance() {
		return colladaHighlightingDistance;
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

	public void setBalloonContentMode(BalloonContentMode balloonContentMode) {
		this.balloonContentMode = balloonContentMode;
	}

	public BalloonContentMode getBalloonContentMode() {
		return balloonContentMode;
	}

	public Path getBalloonContentPath() {
		return balloonContentPath;
	}

	public void setBalloonContentPath(Path balloonContentPath) {
		if (balloonContentPath != null)
			this.balloonContentPath = balloonContentPath;
	}

	public void setCallGElevationService(boolean callGElevationService) {
		this.callGElevationService = callGElevationService;
	}

	public boolean isCallGElevationService() {
		return callGElevationService;
	}

	public void setPackingAlgorithm(int packingAlgorithm) {
		this.packingAlgorithm = packingAlgorithm;
	}

	public int getPackingAlgorithm() {
		return packingAlgorithm;
	}

	public void setAutoTileSideLength(double autoTileSideLength) {
		this.autoTileSideLength = autoTileSideLength;
	}

	public double getAutoTileSideLength() {
		return autoTileSideLength;
	}

	public void setBalloonContentTemplateFile(String balloonContentTemplateFile) {
		this.balloonContentTemplateFile = balloonContentTemplateFile;
	}

	public String getBalloonContentTemplateFile() {
		return balloonContentTemplateFile;
	}

	public void setBalloonContentInSeparateFile(boolean balloonContentInSeparateFile) {
		this.balloonContentInSeparateFile = balloonContentInSeparateFile;
	}

	public boolean isBalloonContentInSeparateFile() {
		return balloonContentInSeparateFile;
	}

}
