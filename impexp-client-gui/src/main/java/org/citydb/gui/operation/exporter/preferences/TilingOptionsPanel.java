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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.AddTokenMenu;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.components.popup.TokenSettingsMenu;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class TilingOptionsPanel extends InternalPreferencesComponent {
    private TitledPanel outputOptionsPanel;
    private TitledPanel genericAttributePanel;

    private JCheckBox useSubDir;
    private JTextField subDirName;
    private JButton subDirTokenButton;
    private JButton subDirTokenSettingsButton;
    private TokenSettingsMenu subDirTokenSettings;
    private JCheckBox useSuffix;
    private JTextField suffixName;
    private JButton suffixTokenButton;
    private JButton suffixTokenSettingsButton;
    private TokenSettingsMenu suffixTokenSettings;
    private JCheckBox useSuffixAsName;

    private JCheckBox setGenericAttribute;
    private JLabel attributeNameLabel;
    private JTextField attributeName;
    private JLabel attributeValueLabel;
    private JTextField attributeValue;
    private JButton attributeTokenButton;
    private JButton attributeTokenSettingsButton;
    private TokenSettingsMenu attributeTokenSettings;

    public TilingOptionsPanel(Config config) {
        super(config);
        initGui();
    }

    @Override
    public boolean isModified() {
        SimpleTilingOptions tilingOptions = config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions();

        if (useSubDir.isSelected() != tilingOptions.isUseSubDir()) return true;
        if (!subDirName.getText().equals(tilingOptions.getSubDir().getValue())) return true;
        if (subDirTokenSettings.isModified(tilingOptions.getSubDir())) return true;

        if (useSuffix.isSelected() != tilingOptions.isUseFilenameSuffix()) return true;
        if (!suffixName.getText().equals(tilingOptions.getFilenameSuffix().getValue())) return true;
        if (suffixTokenSettings.isModified(tilingOptions.getFilenameSuffix())) return true;
        if (useSuffixAsName.isSelected() != tilingOptions.isUseSuffixAsFilename()) return true;

        if (setGenericAttribute.isSelected() != tilingOptions.isUseGenericAttribute()) return true;
        if (!attributeName.getText().equals(tilingOptions.getAttributeName())) return true;
        if (!attributeValue.getText().equals(tilingOptions.getAttributeValue().getValue())) return true;
        if (attributeTokenSettings.isModified(tilingOptions.getAttributeValue())) return true;

        return false;
    }

    private void initGui() {
        useSubDir = new JCheckBox();
        subDirName = new JTextField();
        subDirTokenButton = new JButton();
        subDirTokenButton.setHorizontalTextPosition(SwingConstants.LEFT);
        subDirTokenButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/expand_more.svg"));
        subDirTokenSettingsButton = new JButton();
        subDirTokenSettingsButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/settings.svg"));
        subDirTokenSettings = new TokenSettingsMenu();

        useSuffix = new JCheckBox();
        suffixName = new JTextField();
        suffixTokenButton = new JButton();
        suffixTokenButton.setHorizontalTextPosition(SwingConstants.LEFT);
        suffixTokenButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/expand_more.svg"));
        useSuffixAsName = new JCheckBox();
        suffixTokenSettingsButton = new JButton();
        suffixTokenSettingsButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/settings.svg"));
        suffixTokenSettings = new TokenSettingsMenu();

        setGenericAttribute = new JCheckBox();
        attributeNameLabel = new JLabel();
        attributeName = new JTextField();
        attributeValueLabel = new JLabel();
        attributeValue = new JTextField();
        attributeTokenButton = new JButton();
        attributeTokenButton.setHorizontalTextPosition(SwingConstants.LEFT);
        attributeTokenButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/expand_more.svg"));
        attributeTokenSettingsButton = new JButton();
        attributeTokenSettingsButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/settings.svg"));
        attributeTokenSettings = new TokenSettingsMenu();

        PopupMenuDecorator.getInstance().decorate(subDirName, suffixName, attributeName, attributeValue);

        setLayout(new GridBagLayout());
        {
            JToolBar dirToolBar = new JToolBar();
            dirToolBar.add(subDirTokenButton);
            dirToolBar.addSeparator();
            dirToolBar.add(subDirTokenSettingsButton);

            JToolBar suffixToolBar = new JToolBar();
            suffixToolBar.add(suffixTokenButton);
            suffixToolBar.addSeparator();
            suffixToolBar.add(suffixTokenSettingsButton);

            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                content.add(useSubDir, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(subDirName, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
                content.add(dirToolBar, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

                int lmargin = GuiUtil.getTextOffset(useSuffix);
                content.add(useSuffix, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(suffixName, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
                content.add(suffixToolBar, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
                content.add(useSuffixAsName, GuiUtil.setConstraints(0, 2, 3, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, lmargin, 0, 0));
            }

            outputOptionsPanel = new TitledPanel().build(content);
        }
        {
            JPanel content = new JPanel();
            content.setLayout(new GridBagLayout());
            {
                JToolBar attributeToolBar = new JToolBar();
                attributeToolBar.add(attributeTokenButton);
                attributeToolBar.addSeparator();
                attributeToolBar.add(attributeTokenSettingsButton);

                content.add(attributeNameLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
                content.add(attributeName, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
                content.add(attributeValueLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
                content.add(attributeValue, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 5));
                content.add(attributeToolBar, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
            }

            genericAttributePanel = new TitledPanel()
                    .withToggleButton(setGenericAttribute)
                    .build(content);
        }

        add(outputOptionsPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
        add(genericAttributePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

        useSubDir.addActionListener(e -> setEnabledSubDir());
        useSuffix.addActionListener(e -> setEnabledSuffix());
        setGenericAttribute.addActionListener(e -> setEnabledGenericAttribute());

        int x = UIManager.getInsets("Button.toolbar.spacingInsets").left;
        subDirTokenButton.addActionListener(e -> AddTokenMenu.newInstance()
                .withTarget(subDirName)
                .show(subDirTokenButton, x, subDirTokenButton.getHeight()));
        suffixTokenButton.addActionListener(e -> AddTokenMenu.newInstance()
                .withTarget(suffixName)
                .show(suffixTokenButton, x, suffixTokenButton.getHeight()));
        attributeTokenButton.addActionListener(e -> AddTokenMenu.newInstance()
                .withTarget(attributeValue)
                .show(attributeTokenButton, x, attributeTokenButton.getHeight()));

        subDirTokenSettingsButton.addActionListener(e -> subDirTokenSettings.show(subDirTokenSettingsButton, x, subDirTokenSettingsButton.getHeight()));
        suffixTokenSettingsButton.addActionListener(e -> suffixTokenSettings.show(suffixTokenSettingsButton, x, suffixTokenSettingsButton.getHeight()));
        attributeTokenSettingsButton.addActionListener(e -> attributeTokenSettings.show(attributeTokenSettingsButton, x, attributeTokenSettingsButton.getHeight()));

        UIManager.addPropertyChangeListener(e -> {
            if ("lookAndFeel".equals(e.getPropertyName())) {
                SwingUtilities.invokeLater(this::updateComponentUI);
            }
        });

        updateComponentUI();
    }

    private void setEnabledOptions() {
        setEnabledSubDir();
        setEnabledSuffix();
        setEnabledGenericAttribute();
    }

    private void setEnabledSubDir() {
        subDirName.setEnabled(useSubDir.isSelected());
        subDirTokenButton.setEnabled(useSubDir.isSelected());
        subDirTokenSettingsButton.setEnabled(useSubDir.isSelected());
    }

    private void setEnabledSuffix() {
        suffixName.setEnabled(useSuffix.isSelected());
        suffixTokenButton.setEnabled(useSuffix.isSelected());
        suffixTokenSettingsButton.setEnabled(useSuffix.isSelected());
        useSuffixAsName.setEnabled(useSuffix.isSelected());
    }

    private void setEnabledGenericAttribute() {
        attributeNameLabel.setEnabled(setGenericAttribute.isSelected());
        attributeName.setEnabled(setGenericAttribute.isSelected());
        attributeValueLabel.setEnabled(setGenericAttribute.isSelected());
        attributeValue.setEnabled(setGenericAttribute.isSelected());
        attributeTokenButton.setEnabled(setGenericAttribute.isSelected());
        attributeTokenSettingsButton.setEnabled(setGenericAttribute.isSelected());
    }

    private void updateComponentUI() {
        subDirTokenSettings.updateUI();
        suffixTokenSettings.updateUI();
        attributeTokenSettings.updateUI();
    }

    @Override
    public void switchLocale(Locale locale) {
        outputOptionsPanel.setTitle(Language.I18N.getString("pref.export.tiling.border.path"));
        useSubDir.setText(Language.I18N.getString("pref.export.tiling.label.subDir"));
        subDirTokenButton.setText(Language.I18N.getString("pref.export.tiling.label.token"));
        useSuffix.setText(Language.I18N.getString("pref.export.tiling.label.suffix"));
        suffixTokenButton.setText(Language.I18N.getString("pref.export.tiling.label.token"));
        useSuffixAsName.setText(Language.I18N.getString("pref.export.tiling.label.suffixAsName"));

        genericAttributePanel.setTitle(Language.I18N.getString("pref.export.tiling.label.attribute"));
        attributeNameLabel.setText(Language.I18N.getString("pref.export.tiling.label.attributeName"));
        attributeValueLabel.setText(Language.I18N.getString("pref.export.tiling.label.attributeValue"));
        attributeTokenButton.setText(Language.I18N.getString("pref.export.tiling.label.token"));

        subDirTokenSettings.switchLocale(locale);
        suffixTokenSettings.switchLocale(locale);
        attributeTokenSettings.switchLocale(locale);
    }

    @Override
    public void loadSettings() {
        SimpleTilingOptions tilingOptions = config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions();

        useSubDir.setSelected(tilingOptions.isUseSubDir());
        subDirName.setText(tilingOptions.getSubDir().getValue());
        subDirName.setCaretPosition(subDirName.getText().length());
        subDirTokenSettings.loadSettings(tilingOptions.getSubDir());

        useSuffix.setSelected(tilingOptions.isUseFilenameSuffix());
        suffixName.setText(tilingOptions.getFilenameSuffix().getValue());
        suffixName.setCaretPosition(suffixName.getText().length());
        suffixTokenSettings.loadSettings(tilingOptions.getFilenameSuffix());
        useSuffixAsName.setSelected(tilingOptions.isUseSuffixAsFilename());

        setGenericAttribute.setSelected(tilingOptions.isUseGenericAttribute());
        attributeName.setText(tilingOptions.getAttributeName());
        attributeValue.setText(tilingOptions.getAttributeValue().getValue());
        attributeValue.setCaretPosition(attributeValue.getText().length());
        attributeTokenSettings.loadSettings(tilingOptions.getAttributeValue());

        setEnabledOptions();
    }

    @Override
    public void setSettings() {
        SimpleTilingOptions tilingOptions = config.getExportConfig().getSimpleQuery().getBboxFilter().getTilingOptions();

        if (subDirName.getText().isEmpty()) {
            subDirName.setText(tilingOptions.getDefaultSubDir());
        }

        tilingOptions.setUseSubDir(useSubDir.isSelected());
        tilingOptions.getSubDir().setValue(subDirName.getText());
        subDirTokenSettings.setSettings(tilingOptions.getSubDir());

        if (useSuffix.isSelected() && suffixName.getText().isEmpty()) {
            useSuffix.setSelected(false);
            setEnabledSuffix();
        }

        tilingOptions.setUseFilenameSuffix(useSuffix.isSelected());
        tilingOptions.getFilenameSuffix().setValue(suffixName.getText());
        suffixTokenSettings.setSettings(tilingOptions.getFilenameSuffix());
        tilingOptions.setUseSuffixAsFilename(useSuffixAsName.isSelected());

        if (attributeName.getText().isEmpty()) {
            attributeName.setText(tilingOptions.getDefaultAttributeName());
        }

        if (setGenericAttribute.isSelected() && attributeValue.getText().isEmpty()) {
            setGenericAttribute.setSelected(false);
            setEnabledGenericAttribute();
        }

        tilingOptions.setUseGenericAttribute(setGenericAttribute.isSelected());
        tilingOptions.setAttributeName(attributeName.getText());
        tilingOptions.getAttributeValue().setValue(attributeValue.getText());
        attributeTokenSettings.setSettings(tilingOptions.getAttributeValue());
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("pref.tree.export.tiling");
    }
}
