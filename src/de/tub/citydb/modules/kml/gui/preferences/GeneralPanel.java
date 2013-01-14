/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.opengis.kml._2.ViewRefreshModeEnumType;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.kmlExporter.KmlExporter;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class GeneralPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JCheckBox kmzCheckbox = new JCheckBox();
	private JCheckBox showBoundingBoxCheckbox = new JCheckBox();
	private JCheckBox showTileBordersCheckbox = new JCheckBox();
	private JLabel autoTileSideLengthLabel = new JLabel();
	private JFormattedTextField autoTileSideLengthText;
	private JCheckBox oneFilePerObjectCheckbox = new JCheckBox();
	private JLabel visibleFromLabel = new JLabel();
	private JFormattedTextField visibleFromText;
	private JLabel pixelsLabel = new JLabel();
	private JLabel mLabel = new JLabel("m.");
	private JCheckBox writeJSONCheckbox = new JCheckBox();
	private JLabel viewRefreshModeLabel = new JLabel();
	private JComboBox viewRefreshModeComboBox = new JComboBox();
	private JLabel viewRefreshTimeLabel = new JLabel();
	private JLabel sLabel = new JLabel("s.");
	private JFormattedTextField viewRefreshTimeText;
	
	
	public GeneralPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		if (kmzCheckbox.isSelected() != kmlExporter.isExportAsKmz()) return true;
		if (showBoundingBoxCheckbox.isSelected() != kmlExporter.isShowBoundingBox()) return true;
		if (showTileBordersCheckbox.isSelected() != kmlExporter.isShowTileBorders()) return true;

		try { autoTileSideLengthText.commitEdit(); } catch (ParseException e) {}
		double autoTileSideLength = 125.0;
		try {
			autoTileSideLength = Double.parseDouble(autoTileSideLengthText.getText().trim());
			if (autoTileSideLength <= 1.0) {
				autoTileSideLength = 125.0;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		if (autoTileSideLength != kmlExporter.getAutoTileSideLength()) return true;

		if (oneFilePerObjectCheckbox.isSelected() != kmlExporter.isOneFilePerObject()) return true;

		try { visibleFromText.commitEdit(); } catch (ParseException e) {}
		double objectRegionSize = 50;
		try {
			objectRegionSize = Double.parseDouble(visibleFromText.getText().trim());
		}
		catch (NumberFormatException nfe) {return true;}
		if (objectRegionSize != kmlExporter.getSingleObjectRegionSize()) return true;

		if (!viewRefreshModeComboBox.getSelectedItem().equals(kmlExporter.getViewRefreshMode())) return true;

		try { viewRefreshTimeText.commitEdit(); } catch (ParseException e) {}
		double viewRefreshTime = 50;
		try {
			viewRefreshTime = Double.parseDouble(viewRefreshTimeText.getText().trim());
		}
		catch (NumberFormatException nfe) {return true;}
		if (viewRefreshTime != kmlExporter.getViewRefreshTime()) return true;

		if (writeJSONCheckbox.isSelected() != kmlExporter.isWriteJSONFile()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		DecimalFormat threeIntFormat = new DecimalFormat("###");	
		threeIntFormat.setMaximumIntegerDigits(3);
		threeIntFormat.setMinimumIntegerDigits(1);

		JPanel generalPanel = new JPanel();
		add(generalPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		generalPanel.setLayout(new GridBagLayout());
		kmzCheckbox.setIconTextGap(10);
		showBoundingBoxCheckbox.setIconTextGap(10);
		showTileBordersCheckbox.setIconTextGap(10);
		oneFilePerObjectCheckbox.setIconTextGap(10);
		writeJSONCheckbox.setIconTextGap(10);

		generalPanel.add(kmzCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		generalPanel.add(showBoundingBoxCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		generalPanel.add(showTileBordersCheckbox, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		
		autoTileSideLengthText = new JFormattedTextField(threeIntFormat);
		generalPanel.add(autoTileSideLengthLabel, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS));
		generalPanel.add(autoTileSideLengthText, GuiUtil.setConstraints(1,3,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints ml = GuiUtil.setConstraints(2,3,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		ml.anchor = GridBagConstraints.WEST;
		generalPanel.add(mLabel, ml);

		generalPanel.add(oneFilePerObjectCheckbox, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));

		visibleFromText = new JFormattedTextField(threeIntFormat);
		GridBagConstraints vfl = GuiUtil.setConstraints(0,5,1.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS);
		vfl.anchor = GridBagConstraints.EAST;
		generalPanel.add(visibleFromLabel, vfl);
		generalPanel.add(visibleFromText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints pl = GuiUtil.setConstraints(2,5,1.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		pl.anchor = GridBagConstraints.WEST;
		generalPanel.add(pixelsLabel, pl);

		for (ViewRefreshModeEnumType refreshMode: ViewRefreshModeEnumType.values()) {
			viewRefreshModeComboBox.addItem(refreshMode.value());
		}
		GridBagConstraints wrml = GuiUtil.setConstraints(0,6,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS);
		wrml.anchor = GridBagConstraints.EAST;
		generalPanel.add(viewRefreshModeLabel, wrml);
		generalPanel.add(viewRefreshModeComboBox, GuiUtil.setConstraints(1,6,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));

		viewRefreshTimeText = new JFormattedTextField(threeIntFormat);
		GridBagConstraints vrtl = GuiUtil.setConstraints(0,7,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS);
		vrtl.anchor = GridBagConstraints.EAST;
		generalPanel.add(viewRefreshTimeLabel, vrtl);
		generalPanel.add(viewRefreshTimeText, GuiUtil.setConstraints(1,7,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints sl = GuiUtil.setConstraints(2,7,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		sl.anchor = GridBagConstraints.WEST;
		generalPanel.add(sLabel, sl);

		generalPanel.add(writeJSONCheckbox, GuiUtil.setConstraints(0,8,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		PopupMenuDecorator.getInstance().decorate(autoTileSideLengthText, visibleFromText, viewRefreshTimeText);

		oneFilePerObjectCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		viewRefreshModeComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
	}

	@Override
	public void doTranslation() {
		kmzCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.exportAsKmz"));
		showBoundingBoxCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.showBoundingBox"));
		showTileBordersCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.showTileBorders"));
		autoTileSideLengthLabel.setText(Internal.I18N.getString("pref.kmlexport.label.autoTileSideLength"));
		oneFilePerObjectCheckbox.setText(Internal.I18N.getString("kmlExport.label.oneFilePerObject"));
		visibleFromLabel.setText(Internal.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsLabel.setText(Internal.I18N.getString("kmlExport.label.pixels"));
		viewRefreshModeLabel.setText(Internal.I18N.getString("kmlExport.label.viewRefreshMode"));
		viewRefreshTimeLabel.setText(Internal.I18N.getString("kmlExport.label.viewRefreshTime"));
		writeJSONCheckbox.setText(Internal.I18N.getString("pref.kmlexport.label.writeJSONFile"));
	}

	@Override
	public void loadSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmzCheckbox.setSelected(kmlExporter.isExportAsKmz());
		showBoundingBoxCheckbox.setSelected(kmlExporter.isShowBoundingBox());
		showTileBordersCheckbox.setSelected(kmlExporter.isShowTileBorders());
		autoTileSideLengthText.setText(String.valueOf(kmlExporter.getAutoTileSideLength()));
		oneFilePerObjectCheckbox.setSelected(kmlExporter.isOneFilePerObject());
		visibleFromText.setText(String.valueOf(kmlExporter.getSingleObjectRegionSize()));
		viewRefreshModeComboBox.setSelectedItem(kmlExporter.getViewRefreshMode());
		viewRefreshTimeText.setText(String.valueOf(kmlExporter.getViewRefreshTime()));
		writeJSONCheckbox.setSelected(kmlExporter.isWriteJSONFile());

		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmlExporter.setExportAsKmz(kmzCheckbox.isSelected());
		kmlExporter.setShowBoundingBox(showBoundingBoxCheckbox.isEnabled() && showBoundingBoxCheckbox.isSelected());

		kmlExporter.setShowTileBorders(showTileBordersCheckbox.isEnabled() && showTileBordersCheckbox.isSelected());
		try {
			kmlExporter.setAutoTileSideLength(Double.parseDouble(autoTileSideLengthText.getText().trim()));
			if (kmlExporter.getAutoTileSideLength() <= 1.0) {
				kmlExporter.setAutoTileSideLength(125.0);
			}
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setOneFilePerObject(oneFilePerObjectCheckbox.isSelected());
		try {
			kmlExporter.setSingleObjectRegionSize(Double.parseDouble(visibleFromText.getText().trim()));
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setViewRefreshMode(viewRefreshModeComboBox.getSelectedItem().toString());
		try {
			kmlExporter.setViewRefreshTime(Double.parseDouble(viewRefreshTimeText.getText().trim()));
		}
		catch (NumberFormatException nfe) {}

		kmlExporter.setWriteJSONFile(writeJSONCheckbox.isSelected());
	}

	private void setEnabledComponents() {
		visibleFromLabel.setEnabled(oneFilePerObjectCheckbox.isSelected());
		visibleFromText.setEnabled(oneFilePerObjectCheckbox.isSelected());
		pixelsLabel.setEnabled(oneFilePerObjectCheckbox.isSelected());

		viewRefreshModeLabel.setEnabled(oneFilePerObjectCheckbox.isSelected());
		viewRefreshModeComboBox.setEnabled(oneFilePerObjectCheckbox.isSelected());

		viewRefreshTimeLabel.setEnabled(oneFilePerObjectCheckbox.isSelected() && ViewRefreshModeEnumType.ON_STOP.value().equals(viewRefreshModeComboBox.getSelectedItem()));
		viewRefreshTimeText.setEnabled(oneFilePerObjectCheckbox.isSelected() && ViewRefreshModeEnumType.ON_STOP.value().equals(viewRefreshModeComboBox.getSelectedItem()));
		sLabel.setEnabled(oneFilePerObjectCheckbox.isSelected() && ViewRefreshModeEnumType.ON_STOP.value().equals(viewRefreshModeComboBox.getSelectedItem()));
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.kmlExport.general");
	}

}
