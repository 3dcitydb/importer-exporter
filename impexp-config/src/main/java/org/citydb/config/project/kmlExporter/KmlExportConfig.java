/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.config.project.kmlExporter;

import org.citydb.config.project.common.Path;
import org.citydb.config.project.resources.Resources;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@XmlRootElement(name = "kmlExport")
@XmlType(name = "KmlExportType", propOrder = {
        "query",
        "path",
        "lodToExportFrom",
        "buildingDisplayForms",
        "buildingBalloon",
        "waterBodyDisplayForms",
        "waterBodyBalloon",
        "landUseDisplayForms",
        "landUseBalloon",
        "vegetationDisplayForms",
        "vegetationBalloon",
        "transportationDisplayForms",
        "transportationBalloon",
        "reliefDisplayForms",
        "reliefBalloon",
        "cityFurnitureDisplayForms",
        "cityFurnitureBalloon",
        "genericCityObjectDisplayForms",
        "genericCityObject3DBalloon",
        "genericCityObjectPointAndCurve",
        "cityObjectGroupDisplayForms",
        "cityObjectGroupBalloon",
        "bridgeDisplayForms",
        "bridgeBalloon",
        "tunnelDisplayForms",
        "tunnelBalloon",
        "colladaOptions",
        "lod0FootprintMode",
        "exportAsKmz",
        "showBoundingBox",
        "showTileBorders",
        "exportEmptyTiles",
        "oneFilePerObject",
        "singleObjectRegionSize",
        "viewRefreshMode",
        "viewRefreshTime",
        "writeJSONFile",
        "createGltfModel",
        "pathOfGltfConverter",
        "notCreateColladaFiles",
        "embedTexturesInGltfFiles",
        "exportGltfBinary",
        "exportGltfV1",
        "enableGltfDracoCompression",
        "appearanceTheme",
        "altitudeMode",
        "altitudeOffsetMode",
        "altitudeOffsetValue",
        "callGElevationService",
        "useOriginalZCoords",
        "idPrefixes",
        "adePreferences",
        "resources"
})
public class KmlExportConfig {
    private SimpleKmlQuery query;
    private Path path;
    private int lodToExportFrom;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms buildingDisplayForms;
    private Balloon buildingBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms waterBodyDisplayForms;
    private Balloon waterBodyBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms landUseDisplayForms;
    private Balloon landUseBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms vegetationDisplayForms;
    private Balloon vegetationBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms transportationDisplayForms;
    private Balloon transportationBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms reliefDisplayForms;
    private Balloon reliefBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms cityFurnitureDisplayForms;
    private Balloon cityFurnitureBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms genericCityObjectDisplayForms;
    private Balloon genericCityObject3DBalloon;
    private PointAndCurve genericCityObjectPointAndCurve;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms cityObjectGroupDisplayForms;
    private Balloon cityObjectGroupBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms bridgeDisplayForms;
    private Balloon bridgeBalloon;
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms tunnelDisplayForms;
    private Balloon tunnelBalloon;
    private ColladaOptions colladaOptions;
    private Lod0FootprintMode lod0FootprintMode;
    private boolean showBoundingBox;
    private boolean showTileBorders;
    private boolean exportEmptyTiles;
    private boolean oneFilePerObject;
    private double singleObjectRegionSize;
    private String viewRefreshMode;
    private double viewRefreshTime;
    private boolean writeJSONFile;
    private boolean createGltfModel;
    private String pathOfGltfConverter;
    private boolean notCreateColladaFiles;
    private boolean exportAsKmz;
    private boolean embedTexturesInGltfFiles;
    private boolean exportGltfBinary;
    private boolean exportGltfV1;
    private boolean enableGltfDracoCompression;
    private String appearanceTheme;
    private AltitudeMode altitudeMode;
    private AltitudeOffsetMode altitudeOffsetMode;
    private double altitudeOffsetValue;
    private boolean callGElevationService;
    private boolean useOriginalZCoords;
    private IdPrefixes idPrefixes;
    @XmlJavaTypeAdapter(ADEPreferencesAdapter.class)
    private Map<String, ADEPreferences> adePreferences;
    private Resources resources;

    public static final String THEME_NONE = "none";
    public static final String THEME_NULL = "<unknown>";

    @XmlTransient
    private List<String> gltfConverterOptions;

    public KmlExportConfig() {
        query = new SimpleKmlQuery();
        path = new Path();
        lodToExportFrom = 2;

        setBuildingDisplayForms(new DisplayForms());
        setBuildingBalloon(new Balloon());
        setWaterBodyDisplayForms(new DisplayForms());
        setWaterBodyBalloon(new Balloon());
        setLandUseDisplayForms(new DisplayForms());
        setLandUseBalloon(new Balloon());
        setVegetationDisplayForms(new DisplayForms());
        setVegetationBalloon(new Balloon());
        setTransportationDisplayForms(new DisplayForms());
        setTransportationBalloon(new Balloon());
        setReliefDisplayForms(new DisplayForms());
        setReliefBalloon(new Balloon());
        setCityFurnitureDisplayForms(new DisplayForms());
        setCityFurnitureBalloon(new Balloon());
        setGenericCityObjectDisplayForms(new DisplayForms());
        setGenericCityObject3DBalloon(new Balloon());
        setGenericCityObjectPointAndCurve(new PointAndCurve());
        setCityObjectGroupDisplayForms(new DisplayForms());
        setCityObjectGroupBalloon(new Balloon());
        setBridgeDisplayForms(new DisplayForms());
        setBridgeBalloon(new Balloon());
        setTunnelDisplayForms(new DisplayForms());
        setTunnelBalloon(new Balloon());
        setColladaOptions(new ColladaOptions());
        setLod0FootprintMode(Lod0FootprintMode.FOOTPRINT);

        exportAsKmz = false;
        exportGltfV1 = true;
        showBoundingBox = false;
        showTileBorders = false;
        exportEmptyTiles = true;
        oneFilePerObject = false;
        singleObjectRegionSize = 50.0;
        viewRefreshMode = "onRegion";
        viewRefreshTime = 1;
        writeJSONFile = false;
        createGltfModel = false;
        notCreateColladaFiles = false;
        embedTexturesInGltfFiles = true;
        exportGltfBinary = false;
        exportGltfV1 = false;
        enableGltfDracoCompression = true;

        pathOfGltfConverter = "contribs" + File.separator + "collada2gltf";
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (osName.contains("windows"))
            pathOfGltfConverter += File.separator + "COLLADA2GLTF-v2.1.3-windows-Release-x64" + File.separator + "COLLADA2GLTF-bin.exe";
        else if (osName.contains("mac"))
            pathOfGltfConverter += File.separator + "COLLADA2GLTF-v2.1.3-osx" + File.separator + "COLLADA2GLTF-bin";
        else if (osName.contains("nux"))
            pathOfGltfConverter += File.separator + "COLLADA2GLTF-v2.1.3-linux" + File.separator + "COLLADA2GLTF-bin";

        setAppearanceTheme(THEME_NONE);
        setAltitudeMode(AltitudeMode.ABSOLUTE);
        setAltitudeOffsetMode(AltitudeOffsetMode.NO_OFFSET);
        altitudeOffsetValue = 0;
        callGElevationService = false;
        setUseOriginalZCoords(true);

        idPrefixes = new IdPrefixes();
        adePreferences = new HashMap<>();
        resources = new Resources();
    }

    public SimpleKmlQuery getQuery() {
        return query;
    }

    public void setQuery(SimpleKmlQuery query) {
        if (query != null)
            this.query = query;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        if (path != null)
            this.path = path;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        if (resources != null)
            this.resources = resources;
    }

    public IdPrefixes getIdPrefixes() {
        return idPrefixes;
    }

    public void setIdPrefixes(IdPrefixes idPrefixes) {
        if (idPrefixes != null)
            this.idPrefixes = idPrefixes;
    }

    public void setLodToExportFrom(int lodToExportFrom) {
        this.lodToExportFrom = lodToExportFrom;
    }

    public int getLodToExportFrom() {
        return lodToExportFrom;
    }

    public void setBuildingDisplayForms(DisplayForms buildingDisplayForms) {
        if (buildingDisplayForms != null) {
            this.buildingDisplayForms = buildingDisplayForms;
        }
    }

    public DisplayForms getBuildingDisplayForms() {
        return buildingDisplayForms;
    }

    public void setWaterBodyDisplayForms(DisplayForms waterBodyDisplayForms) {
        if (waterBodyDisplayForms != null) {
            this.waterBodyDisplayForms = waterBodyDisplayForms;
        }
    }

    public DisplayForms getWaterBodyDisplayForms() {
        return waterBodyDisplayForms;
    }

    public void setLandUseDisplayForms(DisplayForms landUseDisplayForms) {
        if (landUseDisplayForms != null) {
            this.landUseDisplayForms = landUseDisplayForms;
        }
    }

    public DisplayForms getLandUseDisplayForms() {
        return landUseDisplayForms;
    }

    public void setCityObjectGroupDisplayForms(DisplayForms cityObjectGroupDisplayForms) {
        if (cityObjectGroupDisplayForms != null) {
            this.cityObjectGroupDisplayForms = cityObjectGroupDisplayForms;
        }
    }

    public DisplayForms getCityObjectGroupDisplayForms() {
        return cityObjectGroupDisplayForms;
    }

    public void setVegetationDisplayForms(DisplayForms vegetationDisplayForms) {
        if (vegetationDisplayForms != null) {
            this.vegetationDisplayForms = vegetationDisplayForms;
        }
    }

    public DisplayForms getVegetationDisplayForms() {
        return vegetationDisplayForms;
    }

    public ColladaOptions getColladaOptions() {
        return colladaOptions;
    }

    public void setColladaOptions(ColladaOptions colladaOptions) {
        if (colladaOptions != null) {
            this.colladaOptions = colladaOptions;
        }
    }

    public Lod0FootprintMode getLod0FootprintMode() {
        return lod0FootprintMode;
    }

    public void setLod0FootprintMode(Lod0FootprintMode lod0FootprintMode) {
        this.lod0FootprintMode = lod0FootprintMode;
    }

    public void setExportAsKmz(boolean exportAsKmz) {
        this.exportAsKmz = exportAsKmz;
    }

    public boolean isExportAsKmz() {
        return exportAsKmz;
    }

    public boolean isExportGltfV1() {
        return exportGltfV1;
    }

    public void setExportGltfV1(boolean exportGltfV1) {
        this.exportGltfV1 = exportGltfV1;
    }

    public boolean isExportGltfV2() {
        return !exportGltfV1;
    }

    public void setExportGltfV2(boolean exportGltfV2) {
        this.exportGltfV1 = !exportGltfV2;
    }

    public void setCreateGltfModel(boolean createGltfModel) {
        this.createGltfModel = createGltfModel;
    }

    public boolean isCreateGltfModel() {
        return createGltfModel;
    }

    public void setPathOfGltfConverter(String pathOfGltfConverter) {
        this.pathOfGltfConverter = pathOfGltfConverter;
    }

    public String getPathOfGltfConverter() {
        return pathOfGltfConverter;
    }

    public void setNotCreateColladaFiles(boolean notCreateColladaFiles) {
        this.notCreateColladaFiles = notCreateColladaFiles;
    }

    public boolean isNotCreateColladaFiles() {
        return notCreateColladaFiles;
    }

    public void setEmbedTexturesInGltfFiles(boolean embedTexturesInGltfFiles) {
        this.embedTexturesInGltfFiles = embedTexturesInGltfFiles;
    }

    public boolean isEmbedTexturesInGltfFiles() {
        return this.embedTexturesInGltfFiles;
    }

    public void setExportGltfBinary(boolean exportGltfBinary) {
        this.exportGltfBinary = exportGltfBinary;
    }

    public boolean isExportGltfBinary() {
        return this.exportGltfBinary;
    }

    public void setEnableGltfDracoCompression(boolean enableGltfDracoCompression) {
        this.enableGltfDracoCompression = enableGltfDracoCompression;
    }

    public boolean isEnableGltfDracoCompression() {
        return this.enableGltfDracoCompression;
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

    public boolean isExportEmptyTiles() {
        return exportEmptyTiles;
    }

    public void setExportEmptyTiles(boolean exportEmptyTiles) {
        this.exportEmptyTiles = exportEmptyTiles;
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

    public void setLandUseBalloon(Balloon landUseBalloon) {
        this.landUseBalloon = landUseBalloon;
    }

    public Balloon getLandUseBalloon() {
        return landUseBalloon;
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

    public void setGenericCityObjectDisplayForms(DisplayForms genericCityObjectDisplayForms) {
        if (genericCityObjectDisplayForms != null) {
            this.genericCityObjectDisplayForms = genericCityObjectDisplayForms;
        }
    }

    public DisplayForms getGenericCityObjectDisplayForms() {
        return genericCityObjectDisplayForms;
    }

    public void setGenericCityObject3DBalloon(Balloon genericCityObject3DBalloon) {
        this.genericCityObject3DBalloon = genericCityObject3DBalloon;
    }

    public Balloon getGenericCityObject3DBalloon() {
        return genericCityObject3DBalloon;
    }

    public void setGenericCityObjectPointAndCurve(PointAndCurve genericCityObjectPointAndCurve) {
        this.genericCityObjectPointAndCurve = genericCityObjectPointAndCurve;
    }

    public PointAndCurve getGenericCityObjectPointAndCurve() {
        return genericCityObjectPointAndCurve;
    }

    public void setCityFurnitureDisplayForms(DisplayForms cityFurnitureDisplayForms) {
        if (cityFurnitureDisplayForms != null) {
            this.cityFurnitureDisplayForms = cityFurnitureDisplayForms;
        }
    }

    public DisplayForms getCityFurnitureDisplayForms() {
        return cityFurnitureDisplayForms;
    }

    public void setCityFurnitureBalloon(Balloon cityFurnitureBalloon) {
        this.cityFurnitureBalloon = cityFurnitureBalloon;
    }

    public Balloon getCityFurnitureBalloon() {
        return cityFurnitureBalloon;
    }

    public void setTransportationDisplayForms(DisplayForms transportationDisplayForms) {
        if (transportationDisplayForms != null) {
            this.transportationDisplayForms = transportationDisplayForms;
        }
    }

    public DisplayForms getTransportationDisplayForms() {
        return transportationDisplayForms;
    }

    public void setTransportationBalloon(Balloon transportationBalloon) {
        this.transportationBalloon = transportationBalloon;
    }

    public Balloon getTransportationBalloon() {
        return transportationBalloon;
    }

    public DisplayForms getReliefDisplayForms() {
        return reliefDisplayForms;
    }

    public void setReliefDisplayForms(DisplayForms reliefDisplayForms) {
        if (reliefDisplayForms != null) {
            this.reliefDisplayForms = reliefDisplayForms;
        }
    }

    public Balloon getReliefBalloon() {
        return reliefBalloon;
    }

    public void setReliefBalloon(Balloon reliefBalloon) {
        this.reliefBalloon = reliefBalloon;
    }

    public void setBridgeDisplayForms(DisplayForms bridgeDisplayForms) {
        if (bridgeDisplayForms != null) {
            this.bridgeDisplayForms = bridgeDisplayForms;
        }
    }

    public DisplayForms getBridgeDisplayForms() {
        return bridgeDisplayForms;
    }

    public void setBridgeBalloon(Balloon bridgeBalloon) {
        this.bridgeBalloon = bridgeBalloon;
    }

    public Balloon getBridgeBalloon() {
        return bridgeBalloon;
    }

    public void setTunnelDisplayForms(DisplayForms tunnelDisplayForms) {
        if (tunnelDisplayForms != null) {
            this.tunnelDisplayForms = tunnelDisplayForms;
        }
    }

    public DisplayForms getTunnelDisplayForms() {
        return tunnelDisplayForms;
    }

    public void setTunnelBalloon(Balloon tunnelBalloon) {
        this.tunnelBalloon = tunnelBalloon;
    }

    public Balloon getTunnelBalloon() {
        return tunnelBalloon;
    }

    public Map<String, ADEPreferences> getADEPreferences() {
        return adePreferences;
    }

    public void setADEPreferences(Map<String, ADEPreferences> adePreferences) {
        this.adePreferences = adePreferences;
    }

    public boolean isSetGltfConverterOptions() {
        return  gltfConverterOptions != null;
    }

    public List<String> getGltfConverterOptions() {
        return gltfConverterOptions;
    }

    public void setGltfConverterOptions(List<String> gltfConverterOptions) {
        this.gltfConverterOptions = gltfConverterOptions;
    }
}
