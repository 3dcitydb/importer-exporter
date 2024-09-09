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
package org.citydb.gui.operation.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.gui.operation.common.GeometryPanel;
import org.citydb.gui.operation.common.XSLTransformationPanel;
import org.citydb.gui.plugin.util.DefaultPreferences;
import org.citydb.gui.plugin.util.DefaultPreferencesEntry;

public class CityGMLExportPreferences extends DefaultPreferences {

    public CityGMLExportPreferences(Config config) {
        super(new CityGMLExportEntry());

        rootEntry.addChildEntry(new DefaultPreferencesEntry(new GeneralPanel(config)));
        rootEntry.addChildEntry(new DefaultPreferencesEntry(new ResourceIdPanel(config)));
        rootEntry.addChildEntry(new DefaultPreferencesEntry(new TilingOptionsPanel(config)));
        rootEntry.addChildEntry(new DefaultPreferencesEntry(new CityObjectGroupPanel(config)));
        rootEntry.addChildEntry(new DefaultPreferencesEntry(new AppearancePanel(config)));
        rootEntry.addChildEntry(new DefaultPreferencesEntry(new GeometryPanel(
                () -> config.getExportConfig().getAffineTransformation(),
                config)));

        DefaultPreferencesEntry cityGMLOptions = new CityGMLOptionsPanel();
        rootEntry.addChildEntry(cityGMLOptions);
        cityGMLOptions.addChildEntry(new DefaultPreferencesEntry(new CityGMLGeneralPanel(config)));
        cityGMLOptions.addChildEntry(new DefaultPreferencesEntry(new AddressPanel(config)));
        cityGMLOptions.addChildEntry(new DefaultPreferencesEntry(new XLinkPanel(config)));
        cityGMLOptions.addChildEntry(new DefaultPreferencesEntry(new NamespacesPanel(config)));
        cityGMLOptions.addChildEntry(new DefaultPreferencesEntry(new XSLTransformationPanel(
                () -> config.getExportConfig().getCityGMLOptions().getXSLTransformation(),
                config)));

        rootEntry.addChildEntry(new DefaultPreferencesEntry(new CityJSONOptionsPanel(config)));
        rootEntry.addChildEntry(new DefaultPreferencesEntry(new ResourcesPanel(config)));
    }

}
