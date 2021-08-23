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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "ADEVisExportPreferenceType", propOrder = {
        "target",
        "styles",
        "balloon",
        "pointAndCurve"
})
public class ADEPreference {
    @XmlElement(required = true)
    private String target;
    @XmlJavaTypeAdapter(StylesAdapter.class)
    private Styles styles;
    private Balloon balloon;
    private PointAndCurve pointAndCurve;

    public ADEPreference() {
        styles = new Styles();
        balloon = new Balloon();
        pointAndCurve = new PointAndCurve();
    }

    public ADEPreference(String target) {
        this();
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public boolean isSetTarget() {
        return target != null;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Styles getStyles() {
        return styles;
    }

    public void setStyles(Styles styles) {
        if (styles != null) {
            this.styles = styles;
        }
    }

    public Balloon getBalloon() {
        return balloon;
    }

    public void setBalloon(Balloon balloon) {
        this.balloon = balloon;
    }

    public PointAndCurve getPointAndCurve() {
        return pointAndCurve;
    }

    public void setPointAndCurve(PointAndCurve pointAndCurve) {
        this.pointAndCurve = pointAndCurve;
    }

}
