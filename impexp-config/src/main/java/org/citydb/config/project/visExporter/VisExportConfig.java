/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.config.project.visExporter;

import org.citydb.config.project.common.Path;
import org.citydb.config.project.resources.Resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement(name = "visExport")
@XmlType(name = "VisExportType", propOrder = {
        "query",
        "path",
        "lodToExportFrom",
        "displayForms",
        "colladaOptions",
        "gltfOptions",
        "elevation",
        "buildingStyles",
        "buildingBalloon",
        "waterBodyStyles",
        "waterBodyBalloon",
        "landUseStyles",
        "landUseBalloon",
        "vegetationStyles",
        "vegetationBalloon",
        "solitaryVegetationObjectPointAndCurve",
        "transportationStyles",
        "transportationBalloon",
        "transportationPointAndCurve",
        "reliefStyles",
        "reliefBalloon",
        "cityFurnitureStyles",
        "cityFurnitureBalloon",
        "cityFurniturePointAndCurve",
        "genericCityObjectStyles",
        "genericCityObject3DBalloon",
        "genericCityObjectPointAndCurve",
        "cityObjectGroupStyles",
        "cityObjectGroupBalloon",
        "bridgeStyles",
        "bridgeBalloon",
        "tunnelStyles",
        "tunnelBalloon",
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
        "appearanceTheme",
        "idPrefixes",
        "adePreferences",
        "resources"
})
public class VisExportConfig {
    private SimpleVisQuery query;
    private Path path;
    private int lodToExportFrom;
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(DisplayFormsAdapter.class)
    private DisplayForms displayForms;
    private ColladaOptions colladaOptions;
    private GltfOptions gltfOptions;
    private Elevation elevation;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles buildingStyles;
    private Balloon buildingBalloon;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles waterBodyStyles;
    private Balloon waterBodyBalloon;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles landUseStyles;
    private Balloon landUseBalloon;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles vegetationStyles;
    private Balloon vegetationBalloon;
    private PointAndCurve solitaryVegetationObjectPointAndCurve;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles transportationStyles;
    private Balloon transportationBalloon;
    private PointAndCurve transportationPointAndCurve;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles reliefStyles;
    private Balloon reliefBalloon;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles cityFurnitureStyles;
    private Balloon cityFurnitureBalloon;
    private PointAndCurve cityFurniturePointAndCurve;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles genericCityObjectStyles;
    private Balloon genericCityObject3DBalloon;
    private PointAndCurve genericCityObjectPointAndCurve;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles cityObjectGroupStyles;
    private Balloon cityObjectGroupBalloon;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles bridgeStyles;
    private Balloon bridgeBalloon;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles tunnelStyles;
    private Balloon tunnelBalloon;
    private Lod0FootprintMode lod0FootprintMode;
    private boolean showBoundingBox;
    private boolean showTileBorders;
    private boolean exportEmptyTiles;
    private boolean oneFilePerObject;
    private double singleObjectRegionSize;
    private String viewRefreshMode;
    private double viewRefreshTime;
    private boolean writeJSONFile;
    private boolean exportAsKmz;
    private String appearanceTheme;
    private IdPrefixes idPrefixes;
    @XmlJavaTypeAdapter(ADEPreferencesAdapter.class)
    private Map<String, ADEPreferences> adePreferences;
    private Resources resources;

    public static final String THEME_NONE = "none";
    public static final String THEME_NULL = "<unknown>";

    public VisExportConfig() {
        query = new SimpleVisQuery();
        path = new Path();
        lodToExportFrom = 2;
        displayForms = new DisplayForms();
        colladaOptions = new ColladaOptions();
        gltfOptions = new GltfOptions();
        elevation = new Elevation();

        buildingStyles = new Styles();
        buildingBalloon = new Balloon();
        waterBodyStyles = new Styles();
        waterBodyBalloon = new Balloon();
        landUseStyles = new Styles();
        landUseBalloon = new Balloon();
        vegetationStyles = new Styles();
        vegetationBalloon = new Balloon();
        solitaryVegetationObjectPointAndCurve = new PointAndCurve();
        transportationStyles = new Styles();
        transportationBalloon = new Balloon();
        transportationPointAndCurve = new PointAndCurve();
        reliefStyles = new Styles();
        reliefBalloon = new Balloon();
        cityFurnitureStyles = new Styles();
        cityFurnitureBalloon = new Balloon();
        cityFurniturePointAndCurve = new PointAndCurve();
        genericCityObjectStyles = new Styles();
        genericCityObject3DBalloon = new Balloon();
        genericCityObjectPointAndCurve = new PointAndCurve();
        cityObjectGroupStyles = new Styles();
        cityObjectGroupBalloon = new Balloon();
        bridgeStyles = new Styles();
        bridgeBalloon = new Balloon();
        tunnelStyles = new Styles();
        tunnelBalloon = new Balloon();

        lod0FootprintMode = Lod0FootprintMode.FOOTPRINT;
        exportAsKmz = false;
        showBoundingBox = false;
        showTileBorders = false;
        exportEmptyTiles = true;
        oneFilePerObject = false;
        singleObjectRegionSize = 50.0;
        viewRefreshMode = "onRegion";
        viewRefreshTime = 1;
        writeJSONFile = false;

        appearanceTheme = THEME_NONE;
        idPrefixes = new IdPrefixes();
        adePreferences = new HashMap<>();
        resources = new Resources();
    }

    public SimpleVisQuery getQuery() {
        return query;
    }

    public void setQuery(SimpleVisQuery query) {
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

    public void setLodToExportFrom(int lodToExportFrom) {
        this.lodToExportFrom = lodToExportFrom;
    }

    public int getLodToExportFrom() {
        return lodToExportFrom;
    }

    public DisplayForms getDisplayForms() {
        return displayForms;
    }

    public void setDisplayForms(DisplayForms displayForms) {
        if (displayForms != null) {
            this.displayForms = displayForms;
        }
    }

    public ColladaOptions getColladaOptions() {
        return colladaOptions;
    }

    public void setColladaOptions(ColladaOptions colladaOptions) {
        if (colladaOptions != null) {
            this.colladaOptions = colladaOptions;
        }
    }

    public GltfOptions getGltfOptions() {
        return gltfOptions;
    }

    public void setGltfOptions(GltfOptions gltfOptions) {
        if (gltfOptions != null) {
            this.gltfOptions = gltfOptions;
        }
    }

    public Elevation getElevation() {
        return elevation;
    }

    public void setElevation(Elevation elevation) {
        if (elevation != null) {
            this.elevation = elevation;
        }
    }

    public void setBuildingStyles(Styles buildingStyles) {
        if (buildingStyles != null) {
            this.buildingStyles = buildingStyles;
        }
    }

    public Styles getBuildingStyles() {
        return buildingStyles;
    }

    public void setWaterBodyStyles(Styles waterBodyStyles) {
        if (waterBodyStyles != null) {
            this.waterBodyStyles = waterBodyStyles;
        }
    }

    public Styles getWaterBodyStyles() {
        return waterBodyStyles;
    }

    public void setLandUseStyles(Styles landUseStyles) {
        if (landUseStyles != null) {
            this.landUseStyles = landUseStyles;
        }
    }

    public Styles getLandUseStyles() {
        return landUseStyles;
    }

    public void setCityObjectGroupStyles(Styles cityObjectGroupStyles) {
        if (cityObjectGroupStyles != null) {
            this.cityObjectGroupStyles = cityObjectGroupStyles;
        }
    }

    public Styles getCityObjectGroupStyles() {
        return cityObjectGroupStyles;
    }

    public void setVegetationStyles(Styles vegetationStyles) {
        if (vegetationStyles != null) {
            this.vegetationStyles = vegetationStyles;
        }
    }

    public Styles getVegetationStyles() {
        return vegetationStyles;
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

    public void setGenericCityObjectStyles(Styles genericCityObjectStyles) {
        if (genericCityObjectStyles != null) {
            this.genericCityObjectStyles = genericCityObjectStyles;
        }
    }

    public void setSolitaryVegetationObjectPointAndCurve(PointAndCurve solitaryVegetationObjectPointAndCurve) {
        this.solitaryVegetationObjectPointAndCurve = solitaryVegetationObjectPointAndCurve;
    }

    public PointAndCurve getSolitaryVegetationObjectPointAndCurve() {
        return solitaryVegetationObjectPointAndCurve;
    }

    public Styles getGenericCityObjectStyles() {
        return genericCityObjectStyles;
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

    public void setCityFurnitureStyles(Styles cityFurnitureStyles) {
        if (cityFurnitureStyles != null) {
            this.cityFurnitureStyles = cityFurnitureStyles;
        }
    }

    public Styles getCityFurnitureStyles() {
        return cityFurnitureStyles;
    }

    public void setCityFurnitureBalloon(Balloon cityFurnitureBalloon) {
        this.cityFurnitureBalloon = cityFurnitureBalloon;
    }

    public Balloon getCityFurnitureBalloon() {
        return cityFurnitureBalloon;
    }

    public void setCityFurniturePointAndCurve(PointAndCurve cityFurniturePointAndCurve) {
        this.cityFurniturePointAndCurve = cityFurniturePointAndCurve;
    }

    public PointAndCurve getCityFurniturePointAndCurve() {
        return cityFurniturePointAndCurve;
    }

    public void setTransportationStyles(Styles transportationStyles) {
        if (transportationStyles != null) {
            this.transportationStyles = transportationStyles;
        }
    }

    public Styles getTransportationStyles() {
        return transportationStyles;
    }

    public void setTransportationBalloon(Balloon transportationBalloon) {
        this.transportationBalloon = transportationBalloon;
    }

    public Balloon getTransportationBalloon() {
        return transportationBalloon;
    }

    public void setTransportationPointAndCurve(PointAndCurve transportationPointAndCurve) {
        this.transportationPointAndCurve = transportationPointAndCurve;
    }

    public PointAndCurve getTransportationPointAndCurve() {
        return transportationPointAndCurve;
    }

    public Styles getReliefStyles() {
        return reliefStyles;
    }

    public void setReliefStyles(Styles reliefStyles) {
        if (reliefStyles != null) {
            this.reliefStyles = reliefStyles;
        }
    }

    public Balloon getReliefBalloon() {
        return reliefBalloon;
    }

    public void setReliefBalloon(Balloon reliefBalloon) {
        this.reliefBalloon = reliefBalloon;
    }

    public void setBridgeStyles(Styles bridgeStyles) {
        if (bridgeStyles != null) {
            this.bridgeStyles = bridgeStyles;
        }
    }

    public Styles getBridgeStyles() {
        return bridgeStyles;
    }

    public void setBridgeBalloon(Balloon bridgeBalloon) {
        this.bridgeBalloon = bridgeBalloon;
    }

    public Balloon getBridgeBalloon() {
        return bridgeBalloon;
    }

    public void setTunnelStyles(Styles tunnelStyles) {
        if (tunnelStyles != null) {
            this.tunnelStyles = tunnelStyles;
        }
    }

    public Styles getTunnelStyles() {
        return tunnelStyles;
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
}
