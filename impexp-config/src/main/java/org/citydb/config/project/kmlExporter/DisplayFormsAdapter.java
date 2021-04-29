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

package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.List;

public class DisplayFormsAdapter extends XmlAdapter<DisplayFormsAdapter.DisplayFormList, DisplayForms> {

    public static class DisplayFormList {
        @XmlElement(name = "displayForm")
        private List<DisplayForm> displayForms;
    }

    @Override
    public DisplayForms unmarshal(DisplayFormList v) {
        DisplayForms displayForms = new DisplayForms();

        if (v != null
                && v.displayForms != null
                && !v.displayForms.isEmpty()) {
            v.displayForms.forEach(displayForms::add);
        }

        return displayForms;
    }

    @Override
    public DisplayFormList marshal(DisplayForms v) {
        DisplayFormList list = null;

        if (v != null && !v.isEmpty()) {
            list = new DisplayFormList();
            list.displayForms = new ArrayList<>(v.values());
        }

        return list;
    }
}