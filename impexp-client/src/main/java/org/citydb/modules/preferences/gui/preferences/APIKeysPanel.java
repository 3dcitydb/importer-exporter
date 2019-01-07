/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

package org.citydb.modules.preferences.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.global.APIKeys;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.jdesktop.swingx.JXTextField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class APIKeysPanel extends AbstractPreferencesComponent {
    private JPanel google;

    private JLabel googleGeocodingLabel;
    private JXTextField googleGeocodingText;
    private JLabel googleElevationLabel;
    private JXTextField googleElevationText;

    public APIKeysPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        APIKeys apiKeys = config.getProject().getGlobal().getApiKeys();

        if (!googleGeocodingText.getText().equals(apiKeys.getGoogleGeocoding())) return true;
        if (!googleElevationText.getText().equals(apiKeys.getGoogleElevation())) return true;

        return false;
    }

    private void initGui() {
        googleGeocodingLabel = new JLabel();
        googleGeocodingText = new JXTextField();
        googleElevationLabel = new JLabel();
        googleElevationText = new JXTextField();

        setLayout(new GridBagLayout());
        {
            google = new JPanel();
            add(google, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
            google.setBorder(BorderFactory.createTitledBorder(""));
            google.setLayout(new GridBagLayout());
            {
                google.add(googleGeocodingLabel, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
                google.add(googleGeocodingText, GuiUtil.setConstraints(1,0,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
                google.add(googleElevationLabel, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
                google.add(googleElevationText, GuiUtil.setConstraints(1,1,1,0,GridBagConstraints.HORIZONTAL,0,5,5,5));
            }
        }

        PopupMenuDecorator.getInstance().decorate(googleGeocodingText, googleElevationText);
    }

    @Override
    public void doTranslation() {
        ((TitledBorder)google.getBorder()).setTitle(Language.I18N.getString("pref.general.apiKeys.google.border"));
        googleGeocodingLabel.setText(Language.I18N.getString("pref.general.apiKeys.google.geocoding.label"));
        googleElevationLabel.setText(Language.I18N.getString("pref.general.apiKeys.google.elevation.label"));
    }

    @Override
    public void loadSettings() {
        APIKeys apiKeys = config.getProject().getGlobal().getApiKeys();
        googleGeocodingText.setText(apiKeys.getGoogleGeocoding());
        googleElevationText.setText(apiKeys.getGoogleElevation());
    }

    @Override
    public void setSettings() {
        APIKeys apiKeys = config.getProject().getGlobal().getApiKeys();

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
