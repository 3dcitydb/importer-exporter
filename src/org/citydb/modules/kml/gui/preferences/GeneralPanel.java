/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.kml.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.CropImageFilter;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.opengis.kml._2.ViewRefreshModeEnumType;

import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.config.language.Language;
import org.citydb.config.project.kmlExporter.KmlExporter;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class GeneralPanel extends AbstractPreferencesComponent {

	protected static final int BORDER_THICKNESS = 5;
	protected static final int MAX_TEXTFIELD_HEIGHT = 20;

	private JCheckBox kmzCheckbox = new JCheckBox();
	private JCheckBox showBoundingBoxCheckbox = new JCheckBox();
	private JCheckBox showTileBordersCheckbox = new JCheckBox();
	private JCheckBox exportEmptyTilesCheckbox = new JCheckBox();
	private JLabel autoTileSideLengthLabel = new JLabel();
	private JFormattedTextField autoTileSideLengthText;
	private JCheckBox oneFilePerObjectCheckbox = new JCheckBox();
	private JLabel visibleFromLabel = new JLabel();
	private JFormattedTextField visibleFromText;
	private JLabel pixelsLabel = new JLabel();
	private JLabel mLabel = new JLabel("m.");
	private JLabel viewRefreshModeLabel = new JLabel();
	private JComboBox viewRefreshModeComboBox = new JComboBox();
	private JLabel viewRefreshTimeLabel = new JLabel();
	private JLabel sLabel = new JLabel("s.");
	private JFormattedTextField viewRefreshTimeText;
	private JCheckBox writeJSONCheckbox = new JCheckBox();
	private JCheckBox writeJSONPCheckbox = new JCheckBox();
	private JLabel callbackNameJSONPLabel = new JLabel("s.");
	private JTextField callbackNameJSONPText = new JTextField();
	private JCheckBox cropImageCheckbox = new JCheckBox();
	
	private JCheckBox createGltfCheckbox = new JCheckBox();
	private JTextField gltfConverterBrowseText = new JTextField("");
	private JButton gltfConverterBrowseButton = new JButton("");
	
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
		if (exportEmptyTilesCheckbox.isSelected() != kmlExporter.isExportEmptyTiles()) return true;

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
		if (writeJSONPCheckbox.isSelected() != kmlExporter.isWriteJSONPFile()) return true;
		if (!callbackNameJSONPText.getText().trim().equals(kmlExporter.getCallbackNameJSONP())) return true;
		if (cropImageCheckbox.isSelected() != kmlExporter.isCropImage()) return true;
		if (cropImageCheckbox.isSelected() != kmlExporter.isCropImage()) return true;
		
		if (createGltfCheckbox.isSelected() != kmlExporter.isCreateGltfModel()) return true;
		if (!gltfConverterBrowseText.getText().equals(kmlExporter.getPathOfGltfConverter())) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		DecimalFormat fourIntFormat = new DecimalFormat("####");	
		fourIntFormat.setMaximumIntegerDigits(4);
		fourIntFormat.setMinimumIntegerDigits(1);
		
		DecimalFormat threeIntFormat = new DecimalFormat("###");	
		threeIntFormat.setMaximumIntegerDigits(3);
		threeIntFormat.setMinimumIntegerDigits(1);

		JPanel generalPanel = new JPanel();
		add(generalPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		generalPanel.setLayout(new GridBagLayout());
		kmzCheckbox.setIconTextGap(10);
		showBoundingBoxCheckbox.setIconTextGap(10);
		showTileBordersCheckbox.setIconTextGap(10);
		exportEmptyTilesCheckbox.setIconTextGap(10);
		oneFilePerObjectCheckbox.setIconTextGap(10);
		writeJSONCheckbox.setIconTextGap(10);
		writeJSONPCheckbox.setIconTextGap(10);
		cropImageCheckbox.setIconTextGap(10);

		generalPanel.add(kmzCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		generalPanel.add(showBoundingBoxCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		generalPanel.add(showTileBordersCheckbox, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

		generalPanel.add(exportEmptyTilesCheckbox, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		
		autoTileSideLengthText = new JFormattedTextField(fourIntFormat);
		generalPanel.add(autoTileSideLengthLabel, GuiUtil.setConstraints(0,4,0.0,1.0,GridBagConstraints.WEST,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS));
		generalPanel.add(autoTileSideLengthText, GuiUtil.setConstraints(1,4,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints ml = GuiUtil.setConstraints(2,4,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		ml.anchor = GridBagConstraints.WEST;
		generalPanel.add(mLabel, ml);

		generalPanel.add(oneFilePerObjectCheckbox, GuiUtil.setConstraints(0,5,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,0));

		visibleFromText = new JFormattedTextField(threeIntFormat);
		GridBagConstraints vfl = GuiUtil.setConstraints(0,6,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS);
		vfl.anchor = GridBagConstraints.EAST;
		generalPanel.add(visibleFromLabel, vfl);
		generalPanel.add(visibleFromText, GuiUtil.setConstraints(1,5,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints pl = GuiUtil.setConstraints(2,6,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		pl.anchor = GridBagConstraints.WEST;
		generalPanel.add(pixelsLabel, pl);

		for (ViewRefreshModeEnumType refreshMode: ViewRefreshModeEnumType.values()) {
			viewRefreshModeComboBox.addItem(refreshMode.value());
		}
		GridBagConstraints wrml = GuiUtil.setConstraints(0,7,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS);
		wrml.anchor = GridBagConstraints.EAST;
		generalPanel.add(viewRefreshModeLabel, wrml);
		generalPanel.add(viewRefreshModeComboBox, GuiUtil.setConstraints(1,7,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));

		viewRefreshTimeText = new JFormattedTextField(threeIntFormat);
		GridBagConstraints vrtl = GuiUtil.setConstraints(0,8,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS * 2,0,BORDER_THICKNESS);
		vrtl.anchor = GridBagConstraints.EAST;
		generalPanel.add(viewRefreshTimeLabel, vrtl);
		generalPanel.add(viewRefreshTimeText, GuiUtil.setConstraints(1,8,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		GridBagConstraints sl = GuiUtil.setConstraints(2,8,0.0,1.0,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS);
		sl.anchor = GridBagConstraints.WEST;
		generalPanel.add(sLabel, sl);

		generalPanel.add(writeJSONCheckbox, GuiUtil.setConstraints(0,9,0.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,0));
		generalPanel.add(writeJSONPCheckbox, GuiUtil.setConstraints(0,10,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,0,0,0,1));
		generalPanel.add(callbackNameJSONPLabel, GuiUtil.setConstraints(0,11,0.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
		generalPanel.add(callbackNameJSONPText, GuiUtil.setConstraints(1,12,1.0,0.0,GridBagConstraints.HORIZONTAL,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
				
		JPanel cesiumPanel = new JPanel();
		cesiumPanel.setBorder(BorderFactory.createTitledBorder("Settings for Cesium"));
		cesiumPanel.setLayout(new GridBagLayout());
		add(cesiumPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS*10,0,BORDER_THICKNESS,0));		
		cesiumPanel.add(cropImageCheckbox, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,0));
		
		JPanel gltfConverterPanel = new JPanel();
		gltfConverterPanel.setLayout(new GridBagLayout());
		cesiumPanel.add(gltfConverterPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		createGltfCheckbox.setIconTextGap(10);
		gltfConverterPanel.add(createGltfCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		gltfConverterPanel.add(gltfConverterBrowseText, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS*6,0,BORDER_THICKNESS));
		gltfConverterPanel.add(gltfConverterBrowseButton, GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));

		PopupMenuDecorator.getInstance().decorate(autoTileSideLengthText, visibleFromText, viewRefreshTimeText, callbackNameJSONPText);

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

		writeJSONCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		writeJSONPCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
		
		createGltfCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});
		
		gltfConverterBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseGltfConverterFile("select collada2Gltf converter program");
			}
		});
	}

	@Override
	public void doTranslation() {
		kmzCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.exportAsKmz"));
		showBoundingBoxCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.showBoundingBox"));
		showTileBordersCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.showTileBorders"));
		exportEmptyTilesCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.exportEmptyTiles"));
		autoTileSideLengthLabel.setText(Language.I18N.getString("pref.kmlexport.label.autoTileSideLength"));
		oneFilePerObjectCheckbox.setText(Language.I18N.getString("kmlExport.label.oneFilePerObject"));
		visibleFromLabel.setText(Language.I18N.getString("kmlExport.label.visibleFrom"));
		pixelsLabel.setText(Language.I18N.getString("kmlExport.label.pixels"));
		viewRefreshModeLabel.setText(Language.I18N.getString("kmlExport.label.viewRefreshMode"));
		viewRefreshTimeLabel.setText(Language.I18N.getString("kmlExport.label.viewRefreshTime"));
		writeJSONCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.writeJSONFile"));
		writeJSONPCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.writeJSONPFile"));
		callbackNameJSONPLabel.setText(Language.I18N.getString("pref.kmlexport.label.callbackNameJSONP"));
		cropImageCheckbox.setText("Crop Images before generating texture atlas when exporting gltf/COLLADA model");
		createGltfCheckbox.setText("Create gltf model when exporting COLLADA model; Path of the gltf converter:");
		gltfConverterBrowseButton.setText("Browse");
	}

	@Override
	public void loadSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmzCheckbox.setSelected(kmlExporter.isExportAsKmz());
		showBoundingBoxCheckbox.setSelected(kmlExporter.isShowBoundingBox());
		showTileBordersCheckbox.setSelected(kmlExporter.isShowTileBorders());
		exportEmptyTilesCheckbox.setSelected(kmlExporter.isExportEmptyTiles());
		autoTileSideLengthText.setText(String.valueOf(kmlExporter.getAutoTileSideLength()));
		oneFilePerObjectCheckbox.setSelected(kmlExporter.isOneFilePerObject());
		visibleFromText.setText(String.valueOf(kmlExporter.getSingleObjectRegionSize()));
		viewRefreshModeComboBox.setSelectedItem(kmlExporter.getViewRefreshMode());
		viewRefreshTimeText.setText(String.valueOf(kmlExporter.getViewRefreshTime()));
		writeJSONCheckbox.setSelected(kmlExporter.isWriteJSONFile());
		writeJSONPCheckbox.setSelected(kmlExporter.isWriteJSONPFile());
		callbackNameJSONPText.setText(kmlExporter.getCallbackNameJSONP());
		cropImageCheckbox.setSelected(kmlExporter.isCropImage());
		createGltfCheckbox.setSelected(kmlExporter.isCreateGltfModel());
		gltfConverterBrowseText.setText(kmlExporter.getPathOfGltfConverter());

		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		KmlExporter kmlExporter = config.getProject().getKmlExporter();

		kmlExporter.setExportAsKmz(kmzCheckbox.isSelected());
		kmlExporter.setShowBoundingBox(showBoundingBoxCheckbox.isEnabled() && showBoundingBoxCheckbox.isSelected());

		kmlExporter.setShowTileBorders(showTileBordersCheckbox.isEnabled() && showTileBordersCheckbox.isSelected());
		kmlExporter.setExportEmptyTiles(exportEmptyTilesCheckbox.isSelected());
		
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
		kmlExporter.setWriteJSONPFile(writeJSONPCheckbox.isSelected());
		kmlExporter.setCallbackNameJSONP(callbackNameJSONPText.getText().trim());
		kmlExporter.setCropImage(cropImageCheckbox.isSelected());
		kmlExporter.setCreateGltfModel(createGltfCheckbox.isSelected());
		kmlExporter.setPathOfGltfConverter(gltfConverterBrowseText.getText());
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

		writeJSONPCheckbox.setEnabled(writeJSONCheckbox.isSelected());
		callbackNameJSONPLabel.setEnabled(writeJSONPCheckbox.isEnabled() && writeJSONPCheckbox.isSelected());
		callbackNameJSONPText.setEnabled(writeJSONPCheckbox.isEnabled() && writeJSONPCheckbox.isSelected());
		
		gltfConverterBrowseText.setEnabled(createGltfCheckbox.isSelected());
		gltfConverterBrowseButton.setEnabled(createGltfCheckbox.isSelected());
	}
	
	private void browseGltfConverterFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		FileNameExtensionFilter filter = new FileNameExtensionFilter("executable file (*.exe)", "exe");
		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
		chooser.setFileFilter(filter);
		
		if (!gltfConverterBrowseText.getText().trim().isEmpty())
			chooser.setCurrentDirectory(new File(gltfConverterBrowseText.getText()));
		else
			chooser.setCurrentDirectory(new File(Internal.USER_PATH));
		
		int result = chooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) 
			return;

		gltfConverterBrowseText.setText(chooser.getSelectedFile().toString());
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.kmlExport.general");
	}

}
