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
import org.citydb.config.project.kmlExporter.AltitudeMode;
import org.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class AltitudePanel extends AbstractPreferencesComponent {
	private TitledPanel modePanel;
	private TitledPanel offsetPanel;

	private JLabel modeLabel;
	private JComboBox<AltitudeMode> modeComboBox;
	private JCheckBox useOriginalZCoords;
	private JRadioButton noOffsetRadioButton;
	private JRadioButton constantOffsetRadioButton;
	private JFormattedTextField constantOffsetText;
	private JRadioButton bottomZeroRadioButton;
	private JRadioButton genericAttributeRadioButton;
	private JCheckBox callGElevationService;
	private JLabel callGElevationServiceHint;

	public AltitudePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		try { constantOffsetText.commitEdit(); } catch (ParseException ignored) { }

		switch (config.getKmlExportConfig().getAltitudeOffsetMode()) {
			case NO_OFFSET:
				if (!noOffsetRadioButton.isSelected()) return true;
				break;
			case CONSTANT:
				if (!constantOffsetRadioButton.isSelected()) return true;
				break;
			case BOTTOM_ZERO:
				if (!bottomZeroRadioButton.isSelected()) return true;
				break;
			case GENERIC_ATTRIBUTE:
				if (!genericAttributeRadioButton.isSelected()) return true;
				break;
		}

		if (!config.getKmlExportConfig().getAltitudeMode().equals(modeComboBox.getSelectedItem())) return true;
		if (((Number) constantOffsetText.getValue()).doubleValue() != config.getKmlExportConfig().getAltitudeOffsetValue()) return true;
		if (callGElevationService.isSelected() != config.getKmlExportConfig().isCallGElevationService()) return true;
		if (useOriginalZCoords.isSelected() != config.getKmlExportConfig().isUseOriginalZCoords()) return true;

		return false;
	}

	private void initGui() {
		modeLabel = new JLabel();
		modeComboBox = new JComboBox<>();
		noOffsetRadioButton = new JRadioButton();
		constantOffsetRadioButton = new JRadioButton();
		bottomZeroRadioButton = new JRadioButton();
		genericAttributeRadioButton = new JRadioButton();
		callGElevationService = new JCheckBox();
		callGElevationServiceHint = new JLabel();
		useOriginalZCoords = new JCheckBox();

		ButtonGroup offsetRadioGroup = new ButtonGroup();
		offsetRadioGroup.add(noOffsetRadioButton);
		offsetRadioGroup.add(constantOffsetRadioButton);
		offsetRadioGroup.add(bottomZeroRadioButton);
		offsetRadioGroup.add(genericAttributeRadioButton);

		NumberFormatter format = new NumberFormatter(new DecimalFormat("#.#####", DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
		format.setMaximum(99999.99999);
		format.setMinimum(-99999.99999);
		constantOffsetText = new JFormattedTextField(format);
		constantOffsetText.setColumns(6);

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				content.add(modeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				content.add(modeComboBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
				content.add(useOriginalZCoords, GuiUtil.setConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
			}

			modePanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				JPanel offset = new JPanel();
				offset.setLayout(new GridBagLayout());
				offset.add(constantOffsetRadioButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				offset.add(constantOffsetText, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 5));
				offset.add(new JLabel("m"), GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

				int lmargin = GuiUtil.getTextOffset(genericAttributeRadioButton);
				content.add(noOffsetRadioButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));
				content.add(offset, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(bottomZeroRadioButton, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
				content.add(genericAttributeRadioButton, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
				content.add(callGElevationService, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.HORIZONTAL, 5, lmargin, 0, 0));
				lmargin += GuiUtil.getTextOffset(callGElevationService);
				content.add(callGElevationServiceHint, GuiUtil.setConstraints(0, 5, 0, 0, GridBagConstraints.HORIZONTAL, 5, lmargin, 0, 0));

			}

			offsetPanel = new TitledPanel().build(content);
		}

		add(modePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(offsetPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		PopupMenuDecorator.getInstance().decorate(constantOffsetText);

		noOffsetRadioButton.addActionListener(e -> setEnabledComponents());
		constantOffsetRadioButton.addActionListener(e -> setEnabledComponents());
		bottomZeroRadioButton.addActionListener(e -> setEnabledComponents());
		genericAttributeRadioButton.addActionListener(e -> setEnabledComponents());
	}

	@Override
	public void doTranslation() {
		modePanel.setTitle(Language.I18N.getString("pref.kmlexport.altitude.mode.border"));
		offsetPanel.setTitle(Language.I18N.getString("pref.kmlexport.altitude.offset.border"));

		modeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
    		modeComboBox.addItem(c);
        }

		modeComboBox.setSelectedItem(config.getKmlExportConfig().getAltitudeMode());
        modeLabel.setText(Language.I18N.getString("pref.kmlexport.altitude.label.mode"));
		noOffsetRadioButton.setText(Language.I18N.getString("pref.kmlexport.altitude.label.noOffset"));
		constantOffsetRadioButton.setText(Language.I18N.getString("pref.kmlexport.altitude.label.constantOffset"));
		bottomZeroRadioButton.setText(Language.I18N.getString("pref.kmlexport.altitude.label.bottomZero"));
		genericAttributeRadioButton.setText(Language.I18N.getString("pref.kmlexport.altitude.label.genericAttributeOffset"));
		callGElevationService.setText(Language.I18N.getString("pref.kmlexport.altitude.label.callGElevationService"));
		callGElevationServiceHint.setText(Language.I18N.getString("pref.kmlexport.altitude.label.callGElevationService.hint"));
		useOriginalZCoords.setText(Language.I18N.getString("pref.kmlexport.altitude.label.useOriginalZCoords"));
	}

	@Override
	public void loadSettings() {
		switch (config.getKmlExportConfig().getAltitudeOffsetMode()) {
			case NO_OFFSET:
				noOffsetRadioButton.setSelected(true);
				break;
			case CONSTANT:
				constantOffsetRadioButton.setSelected(true);
				break;
			case BOTTOM_ZERO:
				bottomZeroRadioButton.setSelected(true);
				break;
			case GENERIC_ATTRIBUTE:
				genericAttributeRadioButton.setSelected(true);
				break;
		}

		modeComboBox.setSelectedItem(config.getKmlExportConfig().getAltitudeMode());
		constantOffsetText.setValue(config.getKmlExportConfig().getAltitudeOffsetValue());
		callGElevationService.setSelected(config.getKmlExportConfig().isCallGElevationService());
		useOriginalZCoords.setSelected(config.getKmlExportConfig().isUseOriginalZCoords());
		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		if (noOffsetRadioButton.isSelected()) {
			config.getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.NO_OFFSET);
		} else if (constantOffsetRadioButton.isSelected()) {
			config.getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.CONSTANT);
		} else if (bottomZeroRadioButton.isSelected()) {
			config.getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.BOTTOM_ZERO);
		} else if (genericAttributeRadioButton.isSelected()) {
			config.getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.GENERIC_ATTRIBUTE);
		}

		config.getKmlExportConfig().setAltitudeMode((AltitudeMode) modeComboBox.getSelectedItem());
		config.getKmlExportConfig().setAltitudeOffsetValue(((Number) constantOffsetText.getValue()).doubleValue());
		config.getKmlExportConfig().setCallGElevationService(callGElevationService.isSelected());
		config.getKmlExportConfig().setUseOriginalZCoords(useOriginalZCoords.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.kmlExport.elevation");
	}

	private void setEnabledComponents() {
		constantOffsetText.setEnabled(constantOffsetRadioButton.isSelected());
		callGElevationService.setEnabled(genericAttributeRadioButton.isSelected());
		callGElevationServiceHint.setEnabled(genericAttributeRadioButton.isSelected());
	}

}
