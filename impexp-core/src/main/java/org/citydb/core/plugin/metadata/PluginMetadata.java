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

package org.citydb.core.plugin.metadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@XmlRootElement(name = "plugin")
@XmlType(name = "PluginType", propOrder = {})
public class PluginMetadata {
    @XmlElement(required = true)
    private String name;
    private String version;
    @XmlElement(name = "description")
    private List<PluginDescription> descriptions;
    private String url;
    private PluginVendor vendor;
    @XmlElement(name = "ade-support", defaultValue = "false")
    private Boolean adeSupport;
    private Boolean startEnabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<PluginDescription> getDescriptions() {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }

        return descriptions;
    }

    public PluginDescription getDescriptionForLocaleOrDefault(Locale locale) {
        if (descriptions != null && !descriptions.isEmpty()) {
            return descriptions.stream()
                    .filter(v -> v.getLang() != null && locale.equals(Locale.forLanguageTag(v.getLang())))
                    .findFirst()
                    .orElse(descriptions.get(0));
        }

        return null;
    }

    public void setDescriptions(List<PluginDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PluginVendor getVendor() {
        return vendor;
    }

    public void setVendor(PluginVendor vendor) {
        this.vendor = vendor;
    }

    public boolean hasADESupport() {
        return adeSupport != null ? adeSupport : false;
    }

    public void setADESupport(boolean adeSupport) {
        this.adeSupport = adeSupport;
    }

    public boolean isStartEnabled() {
        return startEnabled != null ? startEnabled : true;
    }

    public void setStartEnabled(boolean startEnabled) {
        this.startEnabled = startEnabled;
    }
}
