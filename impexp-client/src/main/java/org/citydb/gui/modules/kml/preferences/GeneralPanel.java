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

import net.opengis.kml._2.ViewRefreshModeEnumType;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.util.ClientConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;

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
	private JComboBox<String> viewRefreshModeComboBox = new JComboBox<>();
	private JLabel viewRefreshTimeLabel = new JLabel();
	private JLabel sLabel = new JLabel("s.");
	private JFormattedTextField viewRefreshTimeText;
	private JCheckBox writeJSONCheckbox = new JCheckBox();

	private JCheckBox createGltfCheckbox = new JCheckBox();
	private JTextField gltfConverterBrowseText = new JTextField("");
	private JButton gltfConverterBrowseButton = new JButton("");
	private JCheckBox notCreateColladaCheckbox = new JCheckBox();
	private JCheckBox embedTexturesInGltfCheckbox = new JCheckBox();
	private JCheckBox exportGltfBinary = new JCheckBox();
	private JRadioButton exportGltfV1 = new JRadioButton();
	private JRadioButton exportGltfV2 = new JRadioButton();
	private JCheckBox enableGltfDracoCompression = new JCheckBox();
	
	public GeneralPanel(ViewController viewController, Config config) {
		super(config);
		initGui(viewController);
	}

	@Override
	public boolean isModified() {
		KmlExportConfig kmlExportConfig = config.getKmlExportConfig();

		if (kmzCheckbox.isSelected() != kmlExportConfig.isExportAsKmz()) return true;
		if (showBoundingBoxCheckbox.isSelected() != kmlExportConfig.isShowBoundingBox()) return true;
		if (showTileBordersCheckbox.isSelected() != kmlExportConfig.isShowTileBorders()) return true;
		if (exportEmptyTilesCheckbox.isSelected() != kmlExportConfig.isExportEmptyTiles()) return true;

		try { autoTileSideLengthText.commitEdit(); } catch (ParseException e) {}
		double autoTileSideLength = 125.0;
		try {
			autoTileSideLength = Double.parseDouble(autoTileSideLengthText.getText().trim());
			if (autoTileSideLength <= 1.0) {
				autoTileSideLength = 125.0;
			}
		}
		catch (NumberFormatException nfe) {return true;}
		if (autoTileSideLength != kmlExportConfig.getQuery().getBboxFilter().getTilingOptions().getAutoTileSideLength()) return true;

		if (oneFilePerObjectCheckbox.isSelected() != kmlExportConfig.isOneFilePerObject()) return true;

		try { visibleFromText.commitEdit(); } catch (ParseException e) {}
		double objectRegionSize = 50;
		try {
			objectRegionSize = Double.parseDouble(visibleFromText.getText().trim());
		}
		catch (NumberFormatException nfe) {return true;}
		if (objectRegionSize != kmlExportConfig.getSingleObjectRegionSize()) return true;

		if (!viewRefreshModeComboBox.getSelectedItem().equals(kmlExportConfig.getViewRefreshMode())) return true;

		try { viewRefreshTimeText.commitEdit(); } catch (ParseException e) {}
		double viewRefreshTime = 50;
		try {
			viewRefreshTime = Double.parseDouble(viewRefreshTimeText.getText().trim());
		}
		catch (NumberFormatException nfe) {return true;}
		if (viewRefreshTime != kmlExportConfig.getViewRefreshTime()) return true;

		if (writeJSONCheckbox.isSelected() != kmlExportConfig.isWriteJSONFile()) return true;
		if (createGltfCheckbox.isSelected() != kmlExportConfig.isCreateGltfModel()) return true;
		if (!gltfConverterBrowseText.getText().equals(kmlExportConfig.getPathOfGltfConverter())) return true;
		if (notCreateColladaCheckbox.isSelected() != kmlExportConfig.isNotCreateColladaFiles()) return true;
		if (embedTexturesInGltfCheckbox.isSelected() != kmlExportConfig.isEmbedTexturesInGltfFiles()) return true;
		if (exportGltfBinary.isSelected() != kmlExportConfig.isExportGltfBinary()) return true;
		if (exportGltfV1.isSelected() != kmlExportConfig.isExportGltfV1()) return true;
		if (exportGltfV2.isSelected() != kmlExportConfig.isExportGltfV2()) return true;
		if (enableGltfDracoCompression.isSelected() != kmlExportConfig.isEnableGltfDracoCompression()) return true;
		
		return false;
	}

	private void initGui(ViewController viewController) {
		setLayout(new GridBagLayout());

		DecimalFormat fourIntFormat = new DecimalFormat("####");
		fourIntFormat.setMaximumIntegerDigits(4);
		fourIntFormat.setMinimumIntegerDigits(1);

		DecimalFormat threeIntFormat = new DecimalFormat("###");
		threeIntFormat.setMaximumIntegerDigits(3);
		threeIntFormat.setMinimumIntegerDigits(1);

		JPanel gltfSettingsPanel = new JPanel();
		gltfSettingsPanel.setLayout(new GridBagLayout());
		add(gltfSettingsPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,0,0));
		JPanel collada2gltfConverterPanel = new JPanel();
		collada2gltfConverterPanel.setLayout(new GridBagLayout());
		gltfSettingsPanel.add(collada2gltfConverterPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,0,0));

		int lmargin = GuiUtil.getTextOffset(createGltfCheckbox) + BORDER_THICKNESS;
		gltfConverterBrowseText.setPreferredSize(gltfConverterBrowseText.getSize());
		collada2gltfConverterPanel.add(createGltfCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(gltfConverterBrowseText, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(gltfConverterBrowseButton, GuiUtil.setConstraints(1,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(notCreateColladaCheckbox, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,lmargin,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(embedTexturesInGltfCheckbox, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(exportGltfBinary, GuiUtil.setConstraints(0,4,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(exportGltfV1, GuiUtil.setConstraints(0,5,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));
		collada2gltfConverterPanel.add(exportGltfV2, GuiUtil.setConstraints(0,6,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));

		lmargin += GuiUtil.getTextOffset(exportGltfV2);
		collada2gltfConverterPanel.add(enableGltfDracoCompression, GuiUtil.setConstraints(0,7,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,BORDER_THICKNESS));
		ButtonGroup exportGltfVersions = new ButtonGroup();
		exportGltfVersions.add(exportGltfV1);
		exportGltfVersions.add(exportGltfV2);

		JPanel generalPanel = new JPanel();
		add(generalPanel, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,BORDER_THICKNESS,0,BORDER_THICKNESS,0));
		generalPanel.setLayout(new GridBagLayout());
		generalPanel.add(kmzCheckbox, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		generalPanel.add(showBoundingBoxCheckbox, GuiUtil.setConstraints(0,1,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));
		generalPanel.add(showTileBordersCheckbox, GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.BOTH,0,BORDER_THICKNESS,0,0));

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
		generalPanel.add(visibleFromText, GuiUtil.setConstraints(1,6,1.0,1.0,GridBagConstraints.BOTH,BORDER_THICKNESS,BORDER_THICKNESS,0,BORDER_THICKNESS));
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

		PopupMenuDecorator.getInstance().decorate(autoTileSideLengthText, visibleFromText, viewRefreshTimeText);

		createGltfCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (createGltfCheckbox.isSelected() && kmzCheckbox.isSelected()) {
					Object[] options = {Language.I18N.getString("pref.kmlexport.label.deactivateKmz"), Language.I18N.getString("common.button.cancel")};
					int choice = JOptionPane.showOptionDialog(viewController.getTopFrame(),
							Language.I18N.getString("pref.kmlexport.label.kmzGltfWarning"),
							Language.I18N.getString("common.dialog.warning.title"),
							JOptionPane.YES_NO_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[0]);
					if (choice == 0) {
						kmzCheckbox.setSelected(false);
					} else {
						createGltfCheckbox.setSelected(false);
					}
				}
				setEnabledComponents();
			}
		});

		kmzCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (createGltfCheckbox.isSelected() && kmzCheckbox.isSelected()) {
					if (createGltfCheckbox.isSelected() && kmzCheckbox.isSelected()) {
						Object[] options = {Language.I18N.getString("pref.kmlexport.label.deactivateGlTF"), Language.I18N.getString("common.button.cancel")};
						int choice = JOptionPane.showOptionDialog(viewController.getTopFrame(),
								Language.I18N.getString("pref.kmlexport.label.kmzGltfWarning"),
								Language.I18N.getString("common.dialog.warning.title"),
								JOptionPane.YES_NO_OPTION,
							    JOptionPane.QUESTION_MESSAGE,
							    null,
							    options,
							    options[0]);
						if (choice == 0) {
							createGltfCheckbox.setSelected(false);
						} else {
							kmzCheckbox.setSelected(false);
						}
					}
				}
				setEnabledComponents();
			}
		});

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

		createGltfCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledComponents();
			}
		});

		gltfConverterBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				browseGltfConverterFile(Language.I18N.getString("pref.kmlexport.dialog.gltf.title"));
			}
		});

		// when glTF 1.0 is selected, disable Draco
		exportGltfV1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (exportGltfV1.isSelected()) {
					enableGltfDracoCompression.setEnabled(false);
				} else {
					enableGltfDracoCompression.setEnabled(true);
				}
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
		createGltfCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.createGlTF"));
		gltfConverterBrowseButton.setText(Language.I18N.getString("common.button.browse"));
		notCreateColladaCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.notCreateColladaFiles"));
		embedTexturesInGltfCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.embedTexturesInGltfFiles"));
		exportGltfBinary.setText(Language.I18N.getString("pref.kmlexport.label.exportGltfBinary"));
		exportGltfV1.setText(Language.I18N.getString("pref.kmlexport.label.exportGltfV1"));
		exportGltfV2.setText(Language.I18N.getString("pref.kmlexport.label.exportGltfV2"));
		enableGltfDracoCompression.setText(Language.I18N.getString("pref.kmlexport.label.enableGltfDracoCompression"));
	}

	@Override
	public void loadSettings() {
		KmlExportConfig kmlExportConfig = config.getKmlExportConfig();

		kmzCheckbox.setSelected(kmlExportConfig.isExportAsKmz());
		showBoundingBoxCheckbox.setSelected(kmlExportConfig.isShowBoundingBox());
		showTileBordersCheckbox.setSelected(kmlExportConfig.isShowTileBorders());
		exportEmptyTilesCheckbox.setSelected(kmlExportConfig.isExportEmptyTiles());
		autoTileSideLengthText.setText(String.valueOf(kmlExportConfig.getQuery().getBboxFilter().getTilingOptions().getAutoTileSideLength()));
		oneFilePerObjectCheckbox.setSelected(kmlExportConfig.isOneFilePerObject());
		visibleFromText.setText(String.valueOf(kmlExportConfig.getSingleObjectRegionSize()));
		viewRefreshModeComboBox.setSelectedItem(kmlExportConfig.getViewRefreshMode());
		viewRefreshTimeText.setText(String.valueOf(kmlExportConfig.getViewRefreshTime()));
		writeJSONCheckbox.setSelected(kmlExportConfig.isWriteJSONFile());
		createGltfCheckbox.setSelected(kmlExportConfig.isCreateGltfModel());
		gltfConverterBrowseText.setText(kmlExportConfig.getPathOfGltfConverter());
		notCreateColladaCheckbox.setSelected(kmlExportConfig.isNotCreateColladaFiles());
		embedTexturesInGltfCheckbox.setSelected(kmlExportConfig.isEmbedTexturesInGltfFiles());
		exportGltfBinary.setSelected(kmlExportConfig.isExportGltfBinary());
		exportGltfV1.setSelected(kmlExportConfig.isExportGltfV1());
		exportGltfV2.setSelected(kmlExportConfig.isExportGltfV2());
		enableGltfDracoCompression.setSelected(kmlExportConfig.isEnableGltfDracoCompression());
		
		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		KmlExportConfig kmlExportConfig = config.getKmlExportConfig();

		kmlExportConfig.setExportAsKmz(kmzCheckbox.isSelected());
		kmlExportConfig.setShowBoundingBox(showBoundingBoxCheckbox.isEnabled() && showBoundingBoxCheckbox.isSelected());

		kmlExportConfig.setShowTileBorders(showTileBordersCheckbox.isEnabled() && showTileBordersCheckbox.isSelected());
		kmlExportConfig.setExportEmptyTiles(exportEmptyTilesCheckbox.isSelected());
		
		try {
			KmlTilingOptions tilingOptions = kmlExportConfig.getQuery().getBboxFilter().getTilingOptions();
			tilingOptions.setAutoTileSideLength(Double.parseDouble(autoTileSideLengthText.getText().trim()));
			if (tilingOptions.getAutoTileSideLength() <= 1.0) {
				tilingOptions.setAutoTileSideLength(125.0);
			}
		}
		catch (NumberFormatException nfe) {}

		kmlExportConfig.setOneFilePerObject(oneFilePerObjectCheckbox.isSelected());
		try {
			kmlExportConfig.setSingleObjectRegionSize(Double.parseDouble(visibleFromText.getText().trim()));
		}
		catch (NumberFormatException nfe) {}

		kmlExportConfig.setViewRefreshMode(viewRefreshModeComboBox.getSelectedItem().toString());
		try {
			kmlExportConfig.setViewRefreshTime(Double.parseDouble(viewRefreshTimeText.getText().trim()));
		}
		catch (NumberFormatException nfe) {}

		kmlExportConfig.setWriteJSONFile(writeJSONCheckbox.isSelected());
		kmlExportConfig.setCreateGltfModel(createGltfCheckbox.isSelected());
		kmlExportConfig.setPathOfGltfConverter(gltfConverterBrowseText.getText());
		kmlExportConfig.setNotCreateColladaFiles(notCreateColladaCheckbox.isSelected());
		kmlExportConfig.setEmbedTexturesInGltfFiles(embedTexturesInGltfCheckbox.isSelected());
		kmlExportConfig.setExportGltfBinary(exportGltfBinary.isSelected());
		kmlExportConfig.setExportGltfV1(exportGltfV1.isSelected());
		kmlExportConfig.setExportGltfV2(exportGltfV2.isSelected());
		kmlExportConfig.setEnableGltfDracoCompression(enableGltfDracoCompression.isSelected());
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

		gltfConverterBrowseText.setEnabled(createGltfCheckbox.isSelected());
		gltfConverterBrowseButton.setEnabled(createGltfCheckbox.isSelected());			
		notCreateColladaCheckbox.setEnabled(createGltfCheckbox.isSelected());
		embedTexturesInGltfCheckbox.setEnabled(createGltfCheckbox.isSelected());
		exportGltfBinary.setEnabled(createGltfCheckbox.isSelected());
		exportGltfV1.setEnabled(createGltfCheckbox.isSelected());
		exportGltfV2.setEnabled(createGltfCheckbox.isSelected());
		enableGltfDracoCompression.setEnabled(createGltfCheckbox.isSelected());
	}
	
	private void browseGltfConverterFile(String title) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (!gltfConverterBrowseText.getText().trim().isEmpty()) {
			Path path = Paths.get(gltfConverterBrowseText.getText());
			if (!path.isAbsolute())
				path = ClientConstants.IMPEXP_HOME.resolve(path);

			chooser.setCurrentDirectory(path.toFile());
		} else
			chooser.setCurrentDirectory(ClientConstants.IMPEXP_HOME.resolve(ClientConstants.COLLADA2GLTF_DIR).toFile());
		
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
