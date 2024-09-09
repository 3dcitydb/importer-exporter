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

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "PointAndCurve", propOrder = {
        "pointBalloon",
        "pointDisplayMode",
        "pointAltitudeMode",
        "pointThickness",
        "pointNormalColor",
        "pointHighlightingEnabled",
        "pointHighlightedThickness",
        "pointHighlightedColor",
        "pointIconColor",
        "pointIconScale",
        "pointIconHighlightingEnabled",
        "pointIconHighlightedColor",
        "pointIconHighlightedScale",
        "pointCubeLengthOfSide",
        "pointCubeFillColor",
        "pointCubeHighlightingEnabled",
        "pointCubeHighlightedColor",
        "pointCubeHighlightedOutlineThickness",
        "curveBalloon",
        "curveAltitudeMode",
        "curveThickness",
        "curveNormalColor",
        "curveHighlightingEnabled",
        "curveHighlightedThickness",
        "curveHighlightedColor"
})
public class PointAndCurve {
    public static final String DefaultIconHref = "http://maps.google.com/mapfiles/kml/paddle/wht-circle.png";

    public static final int DEFAULT_POINT_COLOR = 0xc8ffff66;
    public static final int DEFAULT_POINT_HIGHLIGHT_COLOR = 0xc866ff66;
    public static final int DEFAULT_POINT_ICON_COLOR = 0xc8ff3333;
    public static final int DEFAULT_POINT_ICON_HIGHLIGHT_COLOR = 0xc866ff66;
    public static final int DEFAULT_POINT_CUBE_FILL_COLOR = 0xc8ffcc00;
    public static final int DEFAULT_POINT_CUBE_HIGHLIGHT_COLOR = 0xc866ff66;
    public static final int DEFAULT_CURVE_COLOR = 0xc8ffff66;
    public static final int DEFAULT_CURVE_HIGHLIGHT_COLOR = 0xc866ff66;

    private Balloon pointBalloon;
    private AltitudeMode pointAltitudeMode;
    private PointDisplayMode pointDisplayMode;
    private double pointThickness;
    private int pointNormalColor;
    private boolean pointHighlightingEnabled;
    private double pointHighlightedThickness;
    private int pointHighlightedColor;
    private double pointIconScale;
    private int pointIconColor;
    private boolean pointIconHighlightingEnabled;
    private int pointIconHighlightedColor;
    private double pointIconHighlightedScale;
    private double pointCubeLengthOfSide;
    private int pointCubeFillColor;
    private boolean pointCubeHighlightingEnabled;
    private int pointCubeHighlightedColor;
    private double pointCubeHighlightedOutlineThickness;

    private Balloon curveBalloon;
    private AltitudeMode curveAltitudeMode;
    private double curveThickness;
    private int curveNormalColor;
    private boolean curveHighlightingEnabled;
    private double curveHighlightedThickness;
    private int curveHighlightedColor;

    public PointAndCurve() {
        pointBalloon = new Balloon();
        pointAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
        pointDisplayMode = PointDisplayMode.CROSS_LINE;
        pointThickness = 3;
        pointNormalColor = DEFAULT_POINT_COLOR;
        pointHighlightingEnabled = false;
        pointHighlightedThickness = 6;
        pointHighlightedColor = DEFAULT_POINT_HIGHLIGHT_COLOR;
        pointIconScale = 1;
        pointIconColor = DEFAULT_POINT_ICON_COLOR;
        pointIconHighlightingEnabled = false;
        pointIconHighlightedColor = DEFAULT_POINT_ICON_HIGHLIGHT_COLOR;
        pointIconHighlightedScale = 2;
        pointCubeLengthOfSide = 1;
        pointCubeFillColor = DEFAULT_POINT_CUBE_FILL_COLOR;
        pointCubeHighlightingEnabled = false;
        pointCubeHighlightedColor = DEFAULT_POINT_CUBE_HIGHLIGHT_COLOR;
        pointCubeHighlightedOutlineThickness = 3;

        curveBalloon = new Balloon();
        curveAltitudeMode = AltitudeMode.CLAMP_TO_GROUND;
        curveThickness = 3;
        curveNormalColor = DEFAULT_CURVE_COLOR;
        curveHighlightingEnabled = false;
        curveHighlightedThickness = 6;
        curveHighlightedColor = DEFAULT_CURVE_HIGHLIGHT_COLOR;
    }

    public void setPointBalloon(Balloon pointBalloon) {
        if (pointBalloon != null) {
            this.pointBalloon = pointBalloon;
        }
    }

    public Balloon getPointBalloon() {
        return pointBalloon;
    }

    public void setPointDisplayMode(PointDisplayMode pointDisplayMode) {
        if (pointDisplayMode != null) {
            this.pointDisplayMode = pointDisplayMode;
        }
    }

    public PointDisplayMode getPointDisplayMode() {
        return pointDisplayMode;
    }

    public void setPointAltitudeMode(AltitudeMode pointAltitudeMode) {
        if (pointAltitudeMode != null) {
            this.pointAltitudeMode = pointAltitudeMode;
        }
    }

    public AltitudeMode getPointAltitudeMode() {
        return pointAltitudeMode;
    }

    public void setPointThickness(double pointThickness) {
        this.pointThickness = pointThickness;
    }

    public double getPointThickness() {
        return pointThickness;
    }

    public void setPointNormalColor(int pointNormalColor) {
        this.pointNormalColor = pointNormalColor;
    }

    public int getPointNormalColor() {
        return pointNormalColor;
    }

    public void setPointHighlightingEnabled(boolean pointHighlightingEnabled) {
        this.pointHighlightingEnabled = pointHighlightingEnabled;
    }

    public boolean isPointHighlightingEnabled() {
        return pointHighlightingEnabled;
    }

    public void setPointHighlightedColor(int pointHighlightedColor) {
        this.pointHighlightedColor = pointHighlightedColor;
    }

    public int getPointHighlightedColor() {
        return pointHighlightedColor;
    }

    public double getPointHighlightedThickness() {
        return pointHighlightedThickness;
    }

    public void setPointHighlightedThickness(double pointHighlightedThickness) {
        this.pointHighlightedThickness = pointHighlightedThickness;
    }

    public void setCurveBalloon(Balloon curveBalloon) {
        if (curveBalloon != null) {
            this.curveBalloon = curveBalloon;
        }
    }

    public Balloon getCurveBalloon() {
        return curveBalloon;
    }

    public void setCurveAltitudeMode(AltitudeMode curveAltitudeMode) {
        if (curveAltitudeMode != null) {
            this.curveAltitudeMode = curveAltitudeMode;
        }
    }

    public AltitudeMode getCurveAltitudeMode() {
        return curveAltitudeMode;
    }

    public void setCurveThickness(double curveThickness) {
        this.curveThickness = curveThickness;
    }

    public double getCurveThickness() {
        return curveThickness;
    }

    public void setCurveNormalColor(int curveNormalColor) {
        this.curveNormalColor = curveNormalColor;
    }

    public int getCurveNormalColor() {
        return curveNormalColor;
    }

    public void setCurveHighlightingEnabled(boolean curveHighlightingEnabled) {
        this.curveHighlightingEnabled = curveHighlightingEnabled;
    }

    public boolean isCurveHighlightingEnabled() {
        return curveHighlightingEnabled;
    }

    public void setCurveHighlightedColor(int curveHighlightedColor) {
        this.curveHighlightedColor = curveHighlightedColor;
    }

    public int getCurveHighlightedColor() {
        return curveHighlightedColor;
    }

    public double getCurveHighlightedThickness() {
        return curveHighlightedThickness;
    }

    public void setCurveHighlightedThickness(double curveHighlightedThickness) {
        this.curveHighlightedThickness = curveHighlightedThickness;
    }

    public int getPointIconColor() {
        return pointIconColor;
    }

    public void setPointIconColor(int pointIconColor) {
        this.pointIconColor = pointIconColor;
    }

    public double getPointIconScale() {
        return pointIconScale;
    }

    public void setPointIconScale(double pointIconScale) {
        this.pointIconScale = pointIconScale;
    }

    public boolean isPointIconHighlightingEnabled() {
        return pointIconHighlightingEnabled;
    }

    public void setPointIconHighlightingEnabled(boolean pointIconHighlightingEnabled) {
        this.pointIconHighlightingEnabled = pointIconHighlightingEnabled;
    }

    public int getPointIconHighlightedColor() {
        return pointIconHighlightedColor;
    }

    public void setPointIconHighlightedColor(int pointIconHighlightedColor) {
        this.pointIconHighlightedColor = pointIconHighlightedColor;
    }

    public double getPointIconHighlightedScale() {
        return pointIconHighlightedScale;
    }

    public void setPointIconHighlightedScale(double pointIconHighlightedScale) {
        this.pointIconHighlightedScale = pointIconHighlightedScale;
    }

    public double getPointCubeLengthOfSide() {
        return pointCubeLengthOfSide;
    }

    public void setPointCubeLengthOfSide(double pointCubeLengthOfSide) {
        this.pointCubeLengthOfSide = pointCubeLengthOfSide;
    }

    public int getPointCubeFillColor() {
        return pointCubeFillColor;
    }

    public void setPointCubeFillColor(int pointCubeFillColor) {
        this.pointCubeFillColor = pointCubeFillColor;
    }

    public int getPointCubeHighlightedColor() {
        return pointCubeHighlightedColor;
    }

    public void setPointCubeHighlightedColor(int pointCubeHighlightedColor) {
        this.pointCubeHighlightedColor = pointCubeHighlightedColor;
    }

    public double getPointCubeHighlightedOutlineThickness() {
        return pointCubeHighlightedOutlineThickness;
    }

    public void setPointCubeHighlightedOutlineThickness(double pointCubeHighlightedOutlineThickness) {
        this.pointCubeHighlightedOutlineThickness = pointCubeHighlightedOutlineThickness;
    }

    public boolean isPointCubeHighlightingEnabled() {
        return pointCubeHighlightingEnabled;
    }

    public void setPointCubeHighlightingEnabled(boolean pointCubeHighlightingEnabled) {
        this.pointCubeHighlightingEnabled = pointCubeHighlightingEnabled;
    }

}
