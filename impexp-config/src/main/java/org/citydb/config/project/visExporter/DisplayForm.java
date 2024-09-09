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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DisplayFormType", propOrder = {
        "active",
        "visibleFrom"
})
public class DisplayForm {
    @XmlAttribute(required = true)
    private DisplayFormType type;
    private boolean active = false;
    private Integer visibleFrom;
    @XmlTransient
    private int visibleTo = -1;

    public DisplayForm() {
    }

    DisplayForm(DisplayFormType type) {
        this.type = type;
    }

    public static DisplayForm of(DisplayFormType type) {
        return new DisplayForm(type);
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

    public void setVisibleFrom(int visibleFrom) {
        this.visibleFrom = visibleFrom;
    }

    public int getVisibleFrom() {
        return visibleFrom != null ? visibleFrom : 0;
    }

    public void setVisibleTo(int visibleTo) {
        this.visibleTo = visibleTo;
    }

    public int getVisibleTo() {
        return visibleTo;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
