/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="PointAndCurve", propOrder={
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
		"pointCubeLengthOfSide",
		"pointCubeFillColor",
		"pointCubeHighlightingEnabled",
		"pointCubeHighlightedColor",
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
	private Balloon pointBalloon;
	private AltitudeMode pointAltitudeMode;
	private PointDisplayMode pointDisplayMode;
	private double pointThickness;
	private int pointNormalColor = -1;
	private boolean pointHighlightingEnabled = false;
	private double pointHighlightedThickness;
	private int pointHighlightedColor = -1;
	private double pointIconScale;
	private int pointIconColor = -1;	
	private double pointCubeLengthOfSide;
	private int pointCubeFillColor;
	private boolean pointCubeHighlightingEnabled;
	private int pointCubeHighlightedColor;
		
	private Balloon curveBalloon;
	private AltitudeMode curveAltitudeMode;
	private double curveThickness;
	private int curveNormalColor = -1;
	private boolean curveHighlightingEnabled = false;
	private double curveHighlightedThickness;
	private int curveHighlightedColor = -1;

	public PointAndCurve() {
		setPointBalloon(new Balloon());
		setPointDisplayMode(PointDisplayMode.CROSS_LINE);
		setPointAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
		setPointThickness(3);
		setPointNormalColor(DisplayForm.DEFAULT_LINE_COLOR);
		setPointHighlightingEnabled(true);
		setPointHighlightedThickness(6);
		setPointNormalColor(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		setPointIconColor(DisplayForm.DEFAULT_LINE_COLOR);
		setPointIconScale(1);
		setPointCubeLengthOfSide(1);
		setPointCubeFillColor(DisplayForm.DEFAULT_FILL_COLOR);
		setPointCubeHighlightingEnabled(true);
		setPointCubeHighlightedColor(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
		setCurveBalloon(new Balloon());
		setCurveAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
		setCurveThickness(3);
		setCurveNormalColor(DisplayForm.DEFAULT_LINE_COLOR);
		setCurveHighlightingEnabled(true);
		setCurveHighlightedThickness(6);
		setCurveNormalColor(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
	}

	public void setPointBalloon(Balloon pointBalloon) {
		this.pointBalloon = pointBalloon;
	}

	public Balloon getPointBalloon() {
		return pointBalloon;
	}
	
	public void setPointDisplayMode(PointDisplayMode pointDisplayMode) {
		this.pointDisplayMode = pointDisplayMode;
	}

	public PointDisplayMode getPointDisplayMode() {
		return pointDisplayMode;
	}

	public void setPointAltitudeMode(AltitudeMode pointAltitudeMode) {
		this.pointAltitudeMode = pointAltitudeMode;
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
		this.curveBalloon = curveBalloon;
	}

	public Balloon getCurveBalloon() {
		return curveBalloon;
	}

	public void setCurveAltitudeMode(AltitudeMode curveAltitudeMode) {
		this.curveAltitudeMode = curveAltitudeMode;
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

	public boolean isPointCubeHighlightingEnabled() {
		return pointCubeHighlightingEnabled;
	}

	public void setPointCubeHighlightingEnabled(boolean pointCubeHighlightingEnabled) {
		this.pointCubeHighlightingEnabled = pointCubeHighlightingEnabled;
	}

}
