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
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.ExportCityObjectGroup;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class CityObjectGroupPanel extends InternalPreferencesComponent {
    private TitledPanel exportGroupPanel;
    private JCheckBox exportMemberAsXLink;
    private JLabel exportMemberAsXLinkDescr;

    public CityObjectGroupPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        ExportCityObjectGroup group = config.getExportConfig().getCityObjectGroup();
        if (exportMemberAsXLink.isSelected() != group.isExportMemberAsXLinks()) return true;
        return false;
    }

    private void initGui() {
        exportMemberAsXLink = new JCheckBox();
        exportMemberAsXLinkDescr = new JLabel();
        exportMemberAsXLinkDescr.setFont(exportMemberAsXLinkDescr.getFont().deriveFont(Font.ITALIC));

        setLayout(new GridBagLayout());
        exportGroupPanel = new TitledPanel()
                .withToggleButton(exportMemberAsXLink)
                .showSeparator(false)
                .build(exportMemberAsXLinkDescr);

        add(exportGroupPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

        exportMemberAsXLink.addActionListener(e -> setEnabledHint());
    }

    private void setEnabledHint() {
        exportMemberAsXLinkDescr.setEnabled(exportMemberAsXLink.isSelected());
    }

    @Override
    public void loadSettings() {
        ExportCityObjectGroup group = config.getExportConfig().getCityObjectGroup();
        exportMemberAsXLink.setSelected(group.isExportMemberAsXLinks());

        setEnabledHint();
    }

    @Override
    public void setSettings() {
        ExportCityObjectGroup group = config.getExportConfig().getCityObjectGroup();
        group.setExportMemberAsXLinks(exportMemberAsXLink.isSelected());
    }

    @Override
    public void switchLocale(Locale locale) {
        exportGroupPanel.setTitle(Language.I18N.getString("pref.export.group.label.exportMember"));
        exportMemberAsXLinkDescr.setText(Language.I18N.getString("pref.export.group.label.exportMember.description"));
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("pref.tree.export.group");
    }

}
