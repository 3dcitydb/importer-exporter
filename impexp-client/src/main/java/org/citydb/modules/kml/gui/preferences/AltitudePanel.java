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
package org.citydb.modules.kml.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.kmlExporter.AltitudeMode;
import org.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

@SuppressWarnings("serial")
public class AltitudePanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JPanel modePanel; 
	private JComboBox<AltitudeMode> modeComboBox = new JComboBox<>();
	
	private JRadioButton noOffsetRadioButton = new JRadioButton("");
	private JRadioButton constantOffsetRadioButton = new JRadioButton("");
	private JTextField constantOffsetText = new JTextField("", 3);
	private JRadioButton bottomZeroRadioButton = new JRadioButton("");
	private JRadioButton genericAttributeRadioButton = new JRadioButton("");
	private JCheckBox callGElevationService = new JCheckBox();
	private JLabel callGElevationServiceHint = new JLabel();
	private JCheckBox useOriginalZCoords = new JCheckBox();
	private JPanel offsetPanel; 

	public AltitudePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		if (!modeComboBox.getSelectedItem().equals(config.getProject().getKmlExportConfig().getAltitudeMode()))
			return true;

		double altitudeOffsetValue = 0;
		try {
			altitudeOffsetValue = Double.parseDouble(constantOffsetText.getText().trim());
			if (altitudeOffsetValue != config.getProject().getKmlExportConfig().getAltitudeOffsetValue())
				return true;
		}
		catch (NumberFormatException nfe) {
			return true; // saved altitudeOffsetValues are always valid, so an invalid one must have been changed
		}

		switch (config.getProject().getKmlExportConfig().getAltitudeOffsetMode()) {
			case NO_OFFSET:
				if (!noOffsetRadioButton.isSelected())
					return true;
				break;
			case CONSTANT:
				if (!constantOffsetRadioButton.isSelected())
					return true;
				break;
			case BOTTOM_ZERO:
				if (!bottomZeroRadioButton.isSelected())
					return true;
				break;
			case GENERIC_ATTRIBUTE:
				if (!genericAttributeRadioButton.isSelected())
					return true;
				break;
		}

		if (callGElevationService.isSelected() != config.getProject().getKmlExportConfig().isCallGElevationService())
			return true;

		if (useOriginalZCoords.isSelected() != config.getProject().getKmlExportConfig().isUseOriginalZCoords())
			return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		useOriginalZCoords.setIconTextGap(10);
		add(useOriginalZCoords, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		modePanel = new JPanel();
		modePanel.setLayout(new GridBagLayout());
		modePanel.setBorder(BorderFactory.createTitledBorder(""));
		add(modePanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		modePanel.add(modeComboBox, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,BORDER_THICKNESS,BORDER_THICKNESS));

		offsetPanel = new JPanel();
		offsetPanel.setLayout(new GridBagLayout());
		offsetPanel.setBorder(BorderFactory.createTitledBorder(""));
		add(offsetPanel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));

		ButtonGroup offsetRadioGroup = new ButtonGroup();
		offsetRadioGroup.add(noOffsetRadioButton);
		noOffsetRadioButton.setIconTextGap(10);
		offsetRadioGroup.add(constantOffsetRadioButton);
		constantOffsetRadioButton.setIconTextGap(10);
		offsetRadioGroup.add(bottomZeroRadioButton);
		bottomZeroRadioButton.setIconTextGap(10);
		offsetRadioGroup.add(genericAttributeRadioButton);
		genericAttributeRadioButton.setIconTextGap(10);
		callGElevationService.setIconTextGap(10);

		offsetPanel.add(noOffsetRadioButton, GuiUtil.setConstraints(0,0,2,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		offsetPanel.add(constantOffsetRadioButton, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		offsetPanel.add(constantOffsetText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		offsetPanel.add(bottomZeroRadioButton, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		offsetPanel.add(genericAttributeRadioButton, GuiUtil.setConstraints(0,3,2,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

		int lmargin = genericAttributeRadioButton.getPreferredSize().width + 6;
		offsetPanel.add(callGElevationService, GuiUtil.setConstraints(0,4,2,1,0.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));
		lmargin += callGElevationService.getPreferredSize().width + 6;
		offsetPanel.add(callGElevationServiceHint, GuiUtil.setConstraints(0,5,2,1,0.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));

		noOffsetRadioButton.addActionListener(e -> setEnabledComponents());
		constantOffsetRadioButton.addActionListener(e -> setEnabledComponents());
		bottomZeroRadioButton.addActionListener(e -> setEnabledComponents());
		genericAttributeRadioButton.addActionListener(e -> setEnabledComponents());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)modePanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.altitude.mode.border"));	
		((TitledBorder)offsetPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.altitude.offset.border"));	

		modeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
    		modeComboBox.addItem(c);
        }
		modeComboBox.setSelectedItem(config.getProject().getKmlExportConfig().getAltitudeMode());
		
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
		modeComboBox.setSelectedItem(config.getProject().getKmlExportConfig().getAltitudeMode());
		constantOffsetText.setText(String.valueOf(config.getProject().getKmlExportConfig().getAltitudeOffsetValue()));
		switch (config.getProject().getKmlExportConfig().getAltitudeOffsetMode()) {
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
		callGElevationService.setSelected(config.getProject().getKmlExportConfig().isCallGElevationService());
		useOriginalZCoords.setSelected(config.getProject().getKmlExportConfig().isUseOriginalZCoords());
		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		config.getProject().getKmlExportConfig().setAltitudeMode((AltitudeMode)modeComboBox.getSelectedItem());

		double altitudeOffsetValue = 0;
		try {
			altitudeOffsetValue = Double.parseDouble(constantOffsetText.getText().trim());
			config.getProject().getKmlExportConfig().setAltitudeOffsetValue(altitudeOffsetValue);
		}
		catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, Language.I18N.getString("pref.kmlexport.altitude.invalidOffsetValue"),
					Language.I18N.getString("pref.kmlexport.error.incorrectData"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (noOffsetRadioButton.isSelected()) {
			config.getProject().getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.NO_OFFSET);
		}
		else if (constantOffsetRadioButton.isSelected()) {
			config.getProject().getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.CONSTANT);
		}
		else if (bottomZeroRadioButton.isSelected()) {
			config.getProject().getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.BOTTOM_ZERO);
		}
		else if (genericAttributeRadioButton.isSelected()) {
			config.getProject().getKmlExportConfig().setAltitudeOffsetMode(AltitudeOffsetMode.GENERIC_ATTRIBUTE);
		}
		config.getProject().getKmlExportConfig().setCallGElevationService(callGElevationService.isSelected());
		config.getProject().getKmlExportConfig().setUseOriginalZCoords(useOriginalZCoords.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.kmlExport.altitude");
	}

	private void setEnabledComponents() {
		constantOffsetText.setEnabled(constantOffsetRadioButton.isSelected());
		callGElevationService.setEnabled(genericAttributeRadioButton.isSelected());
		callGElevationServiceHint.setEnabled(genericAttributeRadioButton.isSelected());
	}

}
