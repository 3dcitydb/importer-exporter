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

package org.citydb.gui.operation.visExporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.visExporter.Lod0FootprintMode;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class BuildingStylingPanel extends InternalPreferencesComponent {
    private final SurfaceStylingPanel parent;
    private JLabel lod0FootprintLabel;
    private JComboBox<Lod0FootprintMode> lod0FootprintComboBox;

    public BuildingStylingPanel(Config config) {
        super(config);
        parent = new SurfaceStylingPanel("pref.tree.visExport.building.styling",
                () -> config.getVisExportConfig().getBuildingStyles(),
                true, true, true, true, config);

        initGui();
    }

    private void initGui() {
        lod0FootprintLabel = new JLabel();
        lod0FootprintComboBox = new JComboBox<>();

        for (Lod0FootprintMode mode : Lod0FootprintMode.values()) {
            lod0FootprintComboBox.addItem(mode);
        }

        parent.addFootprintAndExtrudedOptions(lod0FootprintLabel, lod0FootprintComboBox);

        setLayout(new BorderLayout());
        add(parent, BorderLayout.CENTER);
    }

    @Override
    public boolean isModified() {
        boolean isModified = parent.isModified();
        return isModified || lod0FootprintComboBox.getSelectedItem() != config.getVisExportConfig().getLod0FootprintMode();
    }

    @Override
    public void switchLocale(Locale locale) {
        parent.switchLocale(locale);
        lod0FootprintLabel.setText(Language.I18N.getString("pref.visExport.label.footprintGeometry"));
    }

    @Override
    public void loadSettings() {
        parent.loadSettings();
        lod0FootprintComboBox.setSelectedItem(config.getVisExportConfig().getLod0FootprintMode());
    }

    @Override
    public void setSettings() {
        parent.setSettings();
        config.getVisExportConfig().setLod0FootprintMode((Lod0FootprintMode) lod0FootprintComboBox.getSelectedItem());
    }

    @Override
    public void resetSettings() {
        parent.resetSettings();
    }

    @Override
    public String getLocalizedTitle() {
        return parent.getLocalizedTitle();
    }
}
