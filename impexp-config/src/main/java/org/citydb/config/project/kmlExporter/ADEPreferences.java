/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

@XmlType(name = "ADEKmlExportPreferencesType", propOrder = {
        "extensionId",
        "preferences"
})
public class ADEPreferences {
    @XmlElement(required = true)
    private String extensionId;
    @XmlJavaTypeAdapter(ADEPreferenceAdapter.class)
    private Map<String, ADEPreference> preferences;

    public ADEPreferences() {
        preferences = new HashMap<>();
    }

    public ADEPreferences(String extensionId) {
        this();
        this.extensionId = extensionId;
    }

    public String getExtensionId() {
        return extensionId;
    }

    public boolean isSetExtensionId() {
        return extensionId != null;
    }

    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    public Map<String, ADEPreference> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, ADEPreference> preferences) {
        this.preferences = preferences;
    }

}
