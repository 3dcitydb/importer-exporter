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
package org.citydb.gui.operation.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class AddressPanel extends InternalPreferencesComponent {
    private TitledPanel importXALPanel;
    private JCheckBox importXAL;

    public AddressPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        if (importXAL.isSelected() != config.getImportConfig().getCityGMLOptions().isImportXalAddress()) return true;
        return false;
    }

    private void initGui() {
        importXAL = new JCheckBox();

        setLayout(new GridBagLayout());
        importXALPanel = new TitledPanel()
                .withToggleButton(importXAL)
                .showSeparator(false)
                .buildWithoutContent();

        add(importXALPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
    }

    @Override
    public void loadSettings() {
        importXAL.setSelected(config.getImportConfig().getCityGMLOptions().isImportXalAddress());
    }

    @Override
    public void setSettings() {
        config.getImportConfig().getCityGMLOptions().setImportXalAddress(importXAL.isSelected());
    }

    @Override
    public void switchLocale(Locale locale) {
        importXALPanel.setTitle(Language.I18N.getString("pref.import.address.label.importXAL"));
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("pref.tree.import.address");
    }

}
