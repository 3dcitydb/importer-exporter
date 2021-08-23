/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "StyleType", propOrder = {
        "rgba0",
        "rgba1",
        "rgba2",
        "rgba3",
        "rgba4",
        "rgba5",
        "highlightingEnabled",
        "highlightingDistance"
})
public class Style {
    public static final String HIGHLIGTHTED_STR = "highlighted";

    public static final int DEFAULT_FOOTPRINT_FILL_COLOR = 0xc8ffcc00;
    public static final int DEFAULT_FOOTPRINT_OUTLINE_COLOR = 0xc8ff6633;
    public static final int DEFAULT_GEOMETRY_FILL_COLOR = 0xc8cccccc;
    public static final int DEFAULT_GEOMETRY_OUTLINE_COLOR = 0xc8666666;
    public static final int DEFAULT_GEOMETRY_ROOF_FILL_COLOR = 0xc8ff3333;
    public static final int DEFAULT_GEOMETRY_ROOF_OUTLINE_COLOR = 0xc8990000;
    public static final int DEFAULT_HIGHLIGHT_FILL_COLOR = 0xc866ff66;
    public static final int DEFAULT_HIGHLIGHT_OUTLINE_COLOR = 0xc8ffff66;
    public static final int DEFAULT_COLLADA_FILL_COLOR = 0xffcccccc;
    public static final int DEFAULT_COLLADA_ROOF_FILL_COLOR = 0xffff3333;

    @XmlAttribute(required = true)
    private DisplayFormType type;
    private Integer rgba0;
    private Integer rgba1;
    private Integer rgba2;
    private Integer rgba3;
    private Integer rgba4;
    private Integer rgba5;
    private Boolean highlightingEnabled;
    private Double highlightingDistance;

    public Style() {
    }

    Style(DisplayFormType type) {
        this.type = type;
    }

    public static Style of(DisplayFormType type) {
        Style style = new Style(type);
        switch (type) {
            case FOOTPRINT:
            case EXTRUDED:
                style.setRgba0(Style.DEFAULT_FOOTPRINT_FILL_COLOR);
                style.setRgba1(Style.DEFAULT_FOOTPRINT_OUTLINE_COLOR);
                style.setRgba4(Style.DEFAULT_HIGHLIGHT_FILL_COLOR);
                style.setRgba5(Style.DEFAULT_HIGHLIGHT_OUTLINE_COLOR);
                break;
            case GEOMETRY:
                style.setRgba0(Style.DEFAULT_GEOMETRY_FILL_COLOR);
                style.setRgba1(Style.DEFAULT_GEOMETRY_OUTLINE_COLOR);
                style.setRgba2(Style.DEFAULT_GEOMETRY_ROOF_FILL_COLOR);
                style.setRgba3(Style.DEFAULT_GEOMETRY_ROOF_OUTLINE_COLOR);
                style.setRgba4(Style.DEFAULT_HIGHLIGHT_FILL_COLOR);
                style.setRgba5(Style.DEFAULT_HIGHLIGHT_OUTLINE_COLOR);
                break;
            case COLLADA:
                style.setRgba0(Style.DEFAULT_COLLADA_FILL_COLOR);
                style.setRgba2(Style.DEFAULT_COLLADA_ROOF_FILL_COLOR);
                style.setRgba4(Style.DEFAULT_HIGHLIGHT_FILL_COLOR);
                style.setRgba5(Style.DEFAULT_HIGHLIGHT_OUTLINE_COLOR);
                break;
        }

        return style;
    }

    public boolean isAchievableFromLoD(int lod) {
        return type != null && type.isAchievableFromLoD(lod);
    }

    public String getName() {
        return type != null ? type.getName() : "unknown";
    }

    public DisplayFormType getType() {
        return type;
    }

    public void setRgba0(int rgba0) {
        this.rgba0 = rgba0;
    }

    public int getRgba0() {
        return rgba0 != null ? rgba0 : -1;
    }

    public boolean isSetRgba0() {
        return rgba0 != null;
    }

    public void setRgba1(int rgba1) {
        this.rgba1 = rgba1;
    }

    public int getRgba1() {
        return rgba1 != null ? rgba1 : -1;
    }

    public boolean isSetRgba1() {
        return rgba1 != null;
    }

    public void setRgba2(int rgba2) {
        this.rgba2 = rgba2;
    }

    public int getRgba2() {
        return rgba2 != null ? rgba2 : -1;
    }

    public boolean isSetRgba2() {
        return rgba2 != null;
    }

    public void setRgba3(int rgba3) {
        this.rgba3 = rgba3;
    }

    public int getRgba3() {
        return rgba3 != null ? rgba3 : -1;
    }

    public boolean isSetRgba3() {
        return rgba3 != null;
    }

    public void setRgba4(int rgba4) {
        this.rgba4 = rgba4;
    }

    public int getRgba4() {
        return rgba4 != null ? rgba4 : -1;
    }

    public boolean isSetRgba4() {
        return rgba4 != null;
    }

    public void setRgba5(int rgba5) {
        this.rgba5 = rgba5;
    }

    public int getRgba5() {
        return rgba5 != null ? rgba5 : -1;
    }

    public boolean isSetRgba5() {
        return rgba5 != null;
    }

    public void setHighlightingEnabled(boolean highlightingEnabled) {
        this.highlightingEnabled = highlightingEnabled;
    }

    public boolean isHighlightingEnabled() {
        return highlightingEnabled != null ? highlightingEnabled : false;
    }

    public void setHighlightingDistance(double highlightingDistance) {
        this.highlightingDistance = highlightingDistance;
    }

    public double getHighlightingDistance() {
        return highlightingDistance != null ? highlightingDistance : 0.75;
    }

    public static String formatColorStringForKML(String rgbColor) {
        // red and blue component change places in KML
        return rgbColor.substring(0, 2) +
                rgbColor.substring(6) +
                rgbColor.substring(4, 6) +
                rgbColor.substring(2, 4);
    }

    public Style copy() {
        Style copy = new Style();
        copy.type = type;
        copy.rgba0 = rgba0;
        copy.rgba1 = rgba1;
        copy.rgba2 = rgba2;
        copy.rgba3 = rgba3;
        copy.rgba4 = rgba4;
        copy.rgba5 = rgba5;
        copy.highlightingDistance = highlightingDistance;
        copy.highlightingEnabled = highlightingEnabled;
        return copy;
    }
}
