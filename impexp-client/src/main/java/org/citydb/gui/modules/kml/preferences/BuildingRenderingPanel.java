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

package org.citydb.gui.modules.kml.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.kmlExporter.Lod0FootprintMode;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;

import javax.swing.*;
import java.awt.*;

public class BuildingRenderingPanel extends AbstractPreferencesComponent {
    private final ThreeDRenderingPanel parent;
    private JLabel lod0FootprintLabel;
    private JComboBox<Lod0FootprintMode> lod0FootprintComboBox;

    public BuildingRenderingPanel(Config config) {
        super(config);
        parent = new ThreeDRenderingPanel("pref.tree.kmlExport.buildingRendering",
                config.getKmlExportConfig().getBuildingDisplayForms(),
                config.getKmlExportConfig().getBuildingColladaOptions()
                , true, true, true, true, config);

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
        return isModified || lod0FootprintComboBox.getSelectedItem() != config.getKmlExportConfig().getLod0FootprintMode();
    }

    @Override
    public void doTranslation() {
        parent.doTranslation();
        lod0FootprintLabel.setText(Language.I18N.getString("pref.kmlexport.label.footprintGeometry"));
    }

    @Override
    public void loadSettings() {
        parent.loadSettings();
        lod0FootprintComboBox.setSelectedItem(config.getKmlExportConfig().getLod0FootprintMode());
    }

    @Override
    public void setSettings() {
        parent.setSettings();
        config.getKmlExportConfig().setLod0FootprintMode((Lod0FootprintMode) lod0FootprintComboBox.getSelectedItem());
    }

    @Override
    public String getTitle() {
        return parent.getTitle();
    }
}
