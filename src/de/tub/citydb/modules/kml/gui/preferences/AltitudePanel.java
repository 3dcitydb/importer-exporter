/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.kml.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.kmlExporter.AltitudeMode;
import de.tub.citydb.config.project.kmlExporter.AltitudeOffsetMode;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class AltitudePanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JPanel modePanel; 
	private JComboBox modeComboBox = new JComboBox();
	
	private JRadioButton noOffsetRadioButton = new JRadioButton("");
	private JRadioButton constantOffsetRadioButton = new JRadioButton("");
	private JTextField constantOffsetText = new JTextField("", 3);
	private JRadioButton genericAttributeRadioButton = new JRadioButton("");
	private JCheckBox callGElevationService = new JCheckBox();
	private JCheckBox useOriginalZCoords = new JCheckBox();
	private JPanel offsetPanel; 

	public AltitudePanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		if (!modeComboBox.getSelectedItem().equals(config.getProject().getKmlExporter().getAltitudeMode()))
			return true;

		double altitudeOffsetValue = 0;
		try {
			altitudeOffsetValue = Double.parseDouble(constantOffsetText.getText().trim());
			if (altitudeOffsetValue != config.getProject().getKmlExporter().getAltitudeOffsetValue())
				return true;
		}
		catch (NumberFormatException nfe) {
			return true; // saved altitudeOffsetValues are always valid, so an invalid one must have been changed
		}

		switch (config.getProject().getKmlExporter().getAltitudeOffsetMode()) {
			case NO_OFFSET:
				if (!noOffsetRadioButton.isSelected())
					return true;
				break;
			case CONSTANT:
				if (!constantOffsetRadioButton.isSelected())
					return true;
				break;
			case GENERIC_ATTRIBUTE:
				if (!genericAttributeRadioButton.isSelected())
					return true;
				break;
		}

		if (callGElevationService.isSelected() != config.getProject().getKmlExporter().isCallGElevationService())
			return true;

		if (useOriginalZCoords.isSelected() != config.getProject().getKmlExporter().isUseOriginalZCoords())
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
		offsetRadioGroup.add(genericAttributeRadioButton);
		genericAttributeRadioButton.setIconTextGap(10);

		GridBagConstraints norb = GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS);
		norb.gridwidth = 2;
		offsetPanel.add(noOffsetRadioButton, norb);
		offsetPanel.add(constantOffsetRadioButton, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		offsetPanel.add(constantOffsetText, GuiUtil.setConstraints(1,1,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints garb = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS);
		garb.gridwidth = 2;
		offsetPanel.add(genericAttributeRadioButton, garb);
		GridBagConstraints cgesl = GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,28,0,BORDER_THICKNESS);
		cgesl.gridwidth = 2;
		offsetPanel.add(callGElevationService, cgesl);


		noOffsetRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		constantOffsetRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		genericAttributeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

	}

	@Override
	public void doTranslation() {
		((TitledBorder)modePanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.altitude.mode.border"));	
		((TitledBorder)offsetPanel.getBorder()).setTitle(Internal.I18N.getString("pref.kmlexport.altitude.offset.border"));	

		modeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
    		modeComboBox.addItem(c);
        }
		modeComboBox.setSelectedItem(config.getProject().getKmlExporter().getAltitudeMode());
		
		noOffsetRadioButton.setText(Internal.I18N.getString("pref.kmlexport.altitude.label.noOffset"));
		constantOffsetRadioButton.setText(Internal.I18N.getString("pref.kmlexport.altitude.label.constantOffset"));
		genericAttributeRadioButton.setText(Internal.I18N.getString("pref.kmlexport.altitude.label.genericAttributeOffset"));
		callGElevationService.setText(Internal.I18N.getString("pref.kmlexport.altitude.label.callGElevationService"));
		useOriginalZCoords.setText(Internal.I18N.getString("pref.kmlexport.altitude.label.useOriginalZCoords"));
	}

	@Override
	public void loadSettings() {
		modeComboBox.setSelectedItem(config.getProject().getKmlExporter().getAltitudeMode());
		constantOffsetText.setText(String.valueOf(config.getProject().getKmlExporter().getAltitudeOffsetValue()));
		switch (config.getProject().getKmlExporter().getAltitudeOffsetMode()) {
			case NO_OFFSET:
				noOffsetRadioButton.setSelected(true);
				break;
			case CONSTANT:
				constantOffsetRadioButton.setSelected(true);
				break;
			case GENERIC_ATTRIBUTE:
				genericAttributeRadioButton.setSelected(true);
				break;
		}
		callGElevationService.setSelected(config.getProject().getKmlExporter().isCallGElevationService());
		useOriginalZCoords.setSelected(config.getProject().getKmlExporter().isUseOriginalZCoords());
		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		config.getProject().getKmlExporter().setAltitudeMode((AltitudeMode)modeComboBox.getSelectedItem());

		double altitudeOffsetValue = 0;
		try {
			altitudeOffsetValue = Double.parseDouble(constantOffsetText.getText().trim());
			config.getProject().getKmlExporter().setAltitudeOffsetValue(altitudeOffsetValue);
		}
		catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, Internal.I18N.getString("pref.kmlexport.altitude.invalidOffsetValue"),
					Internal.I18N.getString("pref.kmlexport.error.incorrectData"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (noOffsetRadioButton.isSelected()) {
			config.getProject().getKmlExporter().setAltitudeOffsetMode(AltitudeOffsetMode.NO_OFFSET);
		}
		else if (constantOffsetRadioButton.isSelected()) {
			config.getProject().getKmlExporter().setAltitudeOffsetMode(AltitudeOffsetMode.CONSTANT);
		}
		else if (genericAttributeRadioButton.isSelected()) {
			config.getProject().getKmlExporter().setAltitudeOffsetMode(AltitudeOffsetMode.GENERIC_ATTRIBUTE);
		}
		config.getProject().getKmlExporter().setCallGElevationService(callGElevationService.isSelected());
		config.getProject().getKmlExporter().setUseOriginalZCoords(useOriginalZCoords.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.altitude");
	}

	private void setEnabledComponents() {
		constantOffsetText.setEnabled(constantOffsetRadioButton.isSelected());
		callGElevationService.setEnabled(genericAttributeRadioButton.isSelected());
	}

}
