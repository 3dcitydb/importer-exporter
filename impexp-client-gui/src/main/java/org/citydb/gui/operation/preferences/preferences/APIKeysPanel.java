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

package org.citydb.gui.operation.preferences.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.APIKeys;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class APIKeysPanel extends AbstractPreferencesComponent {
    private TitledPanel google;

    private JLabel googleGeocodingLabel;
    private JTextField googleGeocodingText;
    private JLabel googleElevationLabel;
    private JTextField googleElevationText;

    public APIKeysPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        APIKeys apiKeys = config.getGlobalConfig().getApiKeys();

        if (!googleGeocodingText.getText().equals(apiKeys.getGoogleGeocoding())) return true;
        if (!googleElevationText.getText().equals(apiKeys.getGoogleElevation())) return true;

        return false;
    }

    private void initGui() {
        googleGeocodingLabel = new JLabel();
        googleGeocodingText = new JTextField();
        googleElevationLabel = new JLabel();
        googleElevationText = new JTextField();

        setLayout(new GridBagLayout());
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(googleGeocodingLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 5));
                content.add(googleGeocodingText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 5, 0));
                content.add(googleElevationLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(googleElevationText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
            }

            google = new TitledPanel().build(content);
        }

        add(google, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

        PopupMenuDecorator.getInstance().decorate(googleGeocodingText, googleElevationText);
    }

    @Override
    public void doTranslation() {
        google.setTitle(Language.I18N.getString("pref.general.apiKeys.google.border"));
        googleGeocodingLabel.setText(Language.I18N.getString("pref.general.apiKeys.google.geocoding.label"));
        googleElevationLabel.setText(Language.I18N.getString("pref.general.apiKeys.google.elevation.label"));
    }

    @Override
    public void loadSettings() {
        APIKeys apiKeys = config.getGlobalConfig().getApiKeys();
        googleGeocodingText.setText(apiKeys.getGoogleGeocoding());
        googleElevationText.setText(apiKeys.getGoogleElevation());
    }

    @Override
    public void setSettings() {
        APIKeys apiKeys = config.getGlobalConfig().getApiKeys();

        String googleGeocoding = googleGeocodingText.getText().trim();
        apiKeys.setGoogleGeocoding(googleGeocoding);
        googleGeocodingText.setText(googleGeocoding);

        String googleElevation = googleElevationText.getText().trim();
        apiKeys.setGoogleElevation(googleElevation);
        googleElevationText.setText(googleElevation);
    }

    @Override
    public String getTitle() {
        return Language.I18N.getString("pref.tree.general.apiKeys");
    }

}
