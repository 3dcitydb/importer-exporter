/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2023
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

package org.citydb.gui.components.popup;

import com.formdev.flatlaf.extras.components.FlatTextField;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.TileTokenValue;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class TokenSettingsMenu extends AbstractPopupMenu {
    private final JPanel settingsPanel;
    private final JLabel rowLabel;
    private final JLabel columnLabel;
    private final JTextField rowFormat;
    private final JTextField columnFormat;
    private final JTextField xminFormat;
    private final JTextField yminFormat;
    private final JTextField xmaxFormat;
    private final JTextField ymaxFormat;

    public TokenSettingsMenu() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        {
            rowLabel = new JLabel();
            columnLabel = new JLabel();
            rowFormat = createTextField();
            columnFormat = createTextField();
            xminFormat = createTextField();
            yminFormat = createTextField();
            xmaxFormat = createTextField();
            ymaxFormat = createTextField();

            JLabel xminLabel = new JLabel("<html>x<sub>min</sub></html>");
            JLabel yminLabel = new JLabel("<html>y<sub>min</sub></html>");
            JLabel xmaxLabel = new JLabel("<html>x<sub>max</sub></html>");
            JLabel ymaxLabel = new JLabel("<html>y<sub>max</sub></html>");

            int separatorHeight = UIManager.getInt("Separator.height");
            settingsPanel.add(rowLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
            settingsPanel.add(rowFormat, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 10));
            settingsPanel.add(columnLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 10, 0, 5));
            settingsPanel.add(columnFormat, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 10));
            settingsPanel.add(new JSeparator(), GuiUtil.setConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5 + separatorHeight, 0, separatorHeight, 0));
            settingsPanel.add(xminLabel, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.HORIZONTAL, 5, 10, 0, 5));
            settingsPanel.add(xminFormat, GuiUtil.setConstraints(1, 3, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 10));
            settingsPanel.add(yminLabel, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.HORIZONTAL, 5, 10, 0, 5));
            settingsPanel.add(yminFormat, GuiUtil.setConstraints(1, 4, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 10));
            settingsPanel.add(xmaxLabel, GuiUtil.setConstraints(0, 5, 0, 0, GridBagConstraints.HORIZONTAL, 5, 10, 0, 5));
            settingsPanel.add(xmaxFormat, GuiUtil.setConstraints(1, 5, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 10));
            settingsPanel.add(ymaxLabel, GuiUtil.setConstraints(0, 6, 0, 0, GridBagConstraints.HORIZONTAL, 5, 10, 0, 5));
            settingsPanel.add(ymaxFormat, GuiUtil.setConstraints(1, 6, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 10));
        }

        add(settingsPanel);
    }

    private JTextField createTextField() {
        FlatTextField textField = new FlatTextField();
        textField.setColumns(8);
        textField.setPlaceholderText(TileTokenValue.DEFAULT_TOKEN_FORMAT);
        return textField;
    }

    public boolean isModified(TileTokenValue value) {
        if (!rowFormat.getText().equals(value.getRowFormat())) return true;
        if (!columnFormat.getText().equals(value.getColumnFormat())) return true;
        if (!xminFormat.getText().equals(value.getXminFormat())) return true;
        if (!yminFormat.getText().equals(value.getYminFormat())) return true;
        if (!xmaxFormat.getText().equals(value.getXmaxFormat())) return true;
        return !ymaxFormat.getText().equals(value.getYmaxFormat());
    }

    public void loadSettings(TileTokenValue value) {
        rowFormat.setText(value.getRowFormat());
        columnFormat.setText(value.getColumnFormat());
        xminFormat.setText(value.getXminFormat());
        yminFormat.setText(value.getYminFormat());
        xmaxFormat.setText(value.getXmaxFormat());
        ymaxFormat.setText(value.getYmaxFormat());
    }

    public void setSettings(TileTokenValue value) {
        value.setRowFormat(rowFormat.getText());
        value.setColumnFormat(columnFormat.getText());
        value.setXminFormat(xminFormat.getText());
        value.setYminFormat(yminFormat.getText());
        value.setXmaxFormat(xmaxFormat.getText());
        value.setYmaxFormat(ymaxFormat.getText());
        loadSettings(value);
    }

    @Override
    public void switchLocale(Locale locale) {
        rowLabel.setText(Language.I18N.getString("pref.export.tiling.label.row"));
        columnLabel.setText(Language.I18N.getString("pref.export.tiling.label.column"));
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (settingsPanel != null) {
            SwingUtilities.updateComponentTreeUI(settingsPanel);
            settingsPanel.setBackground(getBackground());
        }
    }
}
