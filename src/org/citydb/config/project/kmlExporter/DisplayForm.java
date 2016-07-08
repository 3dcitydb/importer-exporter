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
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DisplayFormType", propOrder={
		"form",
		"active",
		"visibleFrom",
		"rgba0",
		"rgba1",
		"rgba2",
		"rgba3",
		"rgba4",
		"rgba5",
		"highlightingEnabled",
		"highlightingDistance"
})
public class DisplayForm {

	public static final int FOOTPRINT = 1;  
	public static final int EXTRUDED = 2;  
	public static final int GEOMETRY = 3;
	public static final int COLLADA = 4;

	public static final String FOOTPRINT_STR = "footprint";  
	public static final String EXTRUDED_STR = "extruded";  
	public static final String GEOMETRY_STR = "geometry";
	public static final String COLLADA_STR = "collada";
	public static final String HIGHLIGTHTED_STR = "highlighted";

	public static final int DEFAULT_FILL_COLOR = 0xc8ffcc00;
	public static final int DEFAULT_LINE_COLOR = 0xc8ff6633;
	public static final int DEFAULT_FILL_HIGHLIGHTED_COLOR = 0xc866ff66;
	public static final int DEFAULT_LINE_HIGHLIGHTED_COLOR = 0xc8ffff66;
	public static final int DEFAULT_WALL_FILL_COLOR = 0xc8cccccc;
 	public static final int DEFAULT_WALL_LINE_COLOR = 0xc8666666;
	public static final int DEFAULT_ROOF_FILL_COLOR = 0xc8ff3333;
	public static final int DEFAULT_ROOF_LINE_COLOR = 0xc8990000;
	public static final int DEFAULT_COLLADA_WALL_FILL_COLOR = 0xffcccccc;
 	public static final int DEFAULT_COLLADA_ROOF_FILL_COLOR = 0xffff3333;

	public static final int DEFAULT_ALPHA_VALUE = 200; // 0xc8;

	private int form;
	private boolean active = false;
	private int visibleFrom = 0;
	@XmlTransient
	private int visibleUpTo = -1;
	//colors
	private int rgba0 = -1;
	private int rgba1 = -1;
	private int rgba2 = -1;
	private int rgba3 = -1;
	private int rgba4 = -1;
	private int rgba5 = -1;

	private boolean highlightingEnabled = false;
	private double highlightingDistance = 0.75;
	
	public DisplayForm() {}

	public DisplayForm(int form, int visibleFrom, int visibleUpTo) {
		this.form = form;
		this.setVisibleFrom(visibleFrom);
		this.setVisibleUpTo(visibleUpTo);
	}

	public static boolean isAchievableFromLoD (int displayForm, int lod) {
		boolean achievable = true; // FOOTPRINT always achievable
		switch (displayForm) {
		case EXTRUDED:
		case GEOMETRY:
		case COLLADA:
			achievable = (lod > 0);
			break;
		}
		return achievable;
	}

	public boolean isAchievableFromLoD (int lod) {
		return isAchievableFromLoD(form, lod);
	}

	public String getName() {
		String formByName = "unknown";
		switch (form) {
		case FOOTPRINT:
			formByName = FOOTPRINT_STR;
			break;
		case EXTRUDED:
			formByName = EXTRUDED_STR;
			break;
		case GEOMETRY:
			formByName = GEOMETRY_STR;
			break;
		case COLLADA:
			formByName = COLLADA_STR;
			break;
		}
		return formByName;
	}

	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public void setVisibleFrom(int visibleFrom) {
		this.visibleFrom = visibleFrom;
	}

	public int getVisibleFrom() {
		return visibleFrom;
	}

	public void setVisibleUpTo(int visibleUpTo) {
		this.visibleUpTo = visibleUpTo;
	}

	public int getVisibleUpTo() {
		return visibleUpTo;
	}

	public void setRgba0(int rgba0) {
		this.rgba0 = rgba0;
	}

	public int getRgba0() {
		return rgba0;
	}

	public boolean isSetRgba0() {
		return this.rgba0 != -1;
	}

	public void setRgba1(int rgba1) {
		this.rgba1 = rgba1;
	}

	public int getRgba1() {
		return rgba1;
	}

	public boolean isSetRgba1() {
		return this.rgba1 != -1;
	}

	public void setRgba2(int rgba2) {
		this.rgba2 = rgba2;
	}

	public int getRgba2() {
		return rgba2;
	}

	public boolean isSetRgba2() {
		return this.rgba2 != -1;
	}

	public void setRgba3(int rgba3) {
		this.rgba3 = rgba3;
	}

	public int getRgba3() {
		return rgba3;
	}

	public boolean isSetRgba3() {
		return this.rgba3 != -1;
	}
	
	public void setRgba4(int rgba4) {
		this.rgba4 = rgba4;
	}

	public int getRgba4() {
		return rgba4;
	}

	public boolean isSetRgba4() {
		return this.rgba4 != -1;
	}
	
	public void setRgba5(int rgba5) {
		this.rgba5 = rgba5;
	}

	public int getRgba5() {
		return rgba5;
	}

	public boolean isSetRgba5() {
		return this.rgba5 != -1;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setHighlightingEnabled(boolean highlightingEnabled) {
		this.highlightingEnabled = highlightingEnabled;
	}

	public boolean isHighlightingEnabled() {
		return highlightingEnabled;
	}

	public void setHighlightingDistance(double highlightingDistance) {
		this.highlightingDistance = highlightingDistance;
	}

	public double getHighlightingDistance() {
		return highlightingDistance;
	}

	@Override
	public boolean equals(Object obj) {
		boolean value = false;
		if (obj instanceof DisplayForm) {
			DisplayForm dl = (DisplayForm)obj;
			value = dl.getForm() == this.getForm();
		}
		return value;
	}
	
	public static String formatColorStringForKML (String rgbColor) {
		String bgrColor;
		// red and blue component change places in KML
		String red = rgbColor.substring(2, 4);
		String blue = rgbColor.substring(6);
		bgrColor = rgbColor.substring(0, 2) + blue + rgbColor.substring(4, 6) + red; 
		return bgrColor;
	}

	@Override
	public DisplayForm clone() {
		DisplayForm clonedDisplayForm = new DisplayForm();
		clonedDisplayForm.setActive(this.isActive());
		clonedDisplayForm.setForm(this.getForm());
		clonedDisplayForm.setHighlightingDistance(this.getHighlightingDistance());
		clonedDisplayForm.setHighlightingEnabled(this.isHighlightingEnabled());
		clonedDisplayForm.setRgba0(this.getRgba0());
		clonedDisplayForm.setRgba1(this.getRgba1());
		clonedDisplayForm.setRgba2(this.getRgba2());
		clonedDisplayForm.setRgba3(this.getRgba3());
		clonedDisplayForm.setRgba4(this.getRgba4());
		clonedDisplayForm.setRgba5(this.getRgba5());
		clonedDisplayForm.setVisibleFrom(this.getVisibleFrom());
		clonedDisplayForm.setVisibleUpTo(this.getVisibleUpTo());
		return clonedDisplayForm;
	}

}
