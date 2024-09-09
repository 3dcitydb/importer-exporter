/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

package org.citydb.config.project.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "ComputeNumberMatched")
public class ComputeNumberMatched {
    @XmlAttribute(required = true)
    private boolean onlyInGuiMode = true;
    @XmlValue
    private Boolean enabled = true;

    public boolean isOnlyInGuiMode() {
        return onlyInGuiMode;
    }

    public void setOnlyInGuiMode(boolean onlyInGuiMode) {
        this.onlyInGuiMode = onlyInGuiMode;
    }

    public boolean isEnabled() {
        return enabled != null ? enabled : true;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
