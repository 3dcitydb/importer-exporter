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
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.KmlExportConfig;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.textureAtlas.TextureAtlasCreator;
import org.citydb.util.ClientConstants;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GeneralPanel extends AbstractPreferencesComponent {
	private final Map<String, Integer> packingAlgorithms = new HashMap<>();

	private TitledPanel generalPanel;
	private TitledPanel colladaPanel;
	private TitledPanel gltfPanel;

	private JCheckBox kmzCheckbox;
	private JCheckBox showBoundingBoxCheckbox;
	private JCheckBox showTileBordersCheckbox;
	private JCheckBox exportEmptyTilesCheckbox;
	private JLabel autoTileSideLengthLabel;
	private JFormattedTextField autoTileSideLengthText;
	private JCheckBox writeJSONCheckbox;

	private JCheckBox ignoreSurfaceOrientationCheckbox;
	private JCheckBox generateSurfaceNormalsCheckbox;
	private JCheckBox cropImagesCheckbox;
	private JCheckBox textureAtlasCheckbox;
	private JComboBox<String> packingAlgorithmsComboBox;
	private JCheckBox textureAtlasPotsCheckbox;
	private JCheckBox scaleTexImagesCheckbox;
	private JSpinner scaleFactorSpinner;
	private JCheckBox groupObjectsCheckbox;
	private JFormattedTextField groupSizeText;

	private JCheckBox createGltfCheckbox;
	private JLabel collada2gltfLabel;
	private JTextField gltfConverterBrowseText;
	private JButton gltfConverterBrowseButton;
	private JCheckBox notCreateColladaCheckbox;
	private JCheckBox embedTexturesInGltfCheckbox;
	private JCheckBox exportGltfBinary;
	private JRadioButton exportGltfV1;
	private JRadioButton exportGltfV2;
	private JCheckBox enableGltfDracoCompression;
	
	public GeneralPanel(Config config) {
		super(config);

		packingAlgorithms.put("BASIC", TextureAtlasCreator.BASIC);
		packingAlgorithms.put("TPIM", TextureAtlasCreator.TPIM);
		packingAlgorithms.put("TPIM w/o image rotation", TextureAtlasCreator.TPIM_WO_ROTATION);

		initGui();
	}

	@Override
	public boolean isModified() {
		KmlExportConfig kmlExportConfig = config.getKmlExportConfig();

		try { autoTileSideLengthText.commitEdit(); } catch (ParseException e) {}
		try { groupSizeText.commitEdit(); } catch (ParseException ignored) { }

		if (kmzCheckbox.isSelected() != kmlExportConfig.isExportAsKmz()) return true;
		if (showBoundingBoxCheckbox.isSelected() != kmlExportConfig.isShowBoundingBox()) return true;
		if (showTileBordersCheckbox.isSelected() != kmlExportConfig.isShowTileBorders()) return true;
		if (exportEmptyTilesCheckbox.isSelected() != kmlExportConfig.isExportEmptyTiles()) return true;
		if (writeJSONCheckbox.isSelected() != kmlExportConfig.isWriteJSONFile()) return true;
		if (((Number) autoTileSideLengthText.getValue()).intValue() != kmlExportConfig.getQuery().getBboxFilter().getTilingOptions().getAutoTileSideLength()) return true;

		ColladaOptions colladaOptions = kmlExportConfig.getColladaOptions();
		if (ignoreSurfaceOrientationCheckbox.isSelected() != colladaOptions.isIgnoreSurfaceOrientation()) return true;
		if (generateSurfaceNormalsCheckbox.isSelected() != colladaOptions.isGenerateSurfaceNormals()) return true;
		if (cropImagesCheckbox.isSelected() != colladaOptions.isCropImages()) return true;
		if (textureAtlasCheckbox.isSelected() != colladaOptions.isGenerateTextureAtlases()) return true;
		if (textureAtlasPotsCheckbox.isSelected() != colladaOptions.isTextureAtlasPots()) return true;
		if (packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()) != colladaOptions.getPackingAlgorithm()) return true;
		if (groupObjectsCheckbox.isSelected() != colladaOptions.isGroupObjects()) return true;
		if (((Number) groupSizeText.getValue()).intValue() != colladaOptions.getGroupSize()) return true;
		if (scaleTexImagesCheckbox.isSelected() != colladaOptions.isScaleImages()) return true;
		if (((Number) scaleFactorSpinner.getValue()).doubleValue() != colladaOptions.getImageScaleFactor()) return true;

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

	private void initGui() {
		kmzCheckbox = new JCheckBox();
		showBoundingBoxCheckbox = new JCheckBox();
		showTileBordersCheckbox = new JCheckBox();
		exportEmptyTilesCheckbox = new JCheckBox();
		autoTileSideLengthLabel = new JLabel();
		writeJSONCheckbox = new JCheckBox();

		ignoreSurfaceOrientationCheckbox = new JCheckBox();
		generateSurfaceNormalsCheckbox = new JCheckBox();
		cropImagesCheckbox = new JCheckBox();
		textureAtlasCheckbox = new JCheckBox();
		textureAtlasPotsCheckbox = new JCheckBox();
		scaleTexImagesCheckbox = new JCheckBox();
		groupObjectsCheckbox = new JCheckBox();
		packingAlgorithmsComboBox = new JComboBox<>();

		createGltfCheckbox = new JCheckBox();
		collada2gltfLabel = new JLabel();
		gltfConverterBrowseText = new JTextField();
		gltfConverterBrowseButton = new JButton();
		notCreateColladaCheckbox = new JCheckBox();
		embedTexturesInGltfCheckbox = new JCheckBox();
		exportGltfBinary = new JCheckBox();
		exportGltfV1 = new JRadioButton();
		exportGltfV2 = new JRadioButton();
		enableGltfDracoCompression = new JCheckBox();

		ButtonGroup exportGltfVersions = new ButtonGroup();
		exportGltfVersions.add(exportGltfV1);
		exportGltfVersions.add(exportGltfV2);

		DecimalFormat tileSizeFormat = new DecimalFormat("#");
		tileSizeFormat.setMaximumIntegerDigits(4);
		tileSizeFormat.setMinimumIntegerDigits(1);
		autoTileSideLengthText = new JFormattedTextField(tileSizeFormat);
		autoTileSideLengthText.setColumns(5);

		SpinnerModel scaleFactor = new SpinnerNumberModel(1, 0.01, 1.0, 0.1);
		scaleFactorSpinner = new JSpinner(scaleFactor);
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(scaleFactorSpinner, "#.###");
		DecimalFormat format = editor.getFormat();
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		scaleFactorSpinner.setEditor(editor);

		DecimalFormat groupSizeFormat = new DecimalFormat("#");
		groupSizeFormat.setMaximumIntegerDigits(8);
		groupSizeFormat.setMinimumIntegerDigits(1);
		groupSizeText = new JFormattedTextField(groupSizeFormat);
		groupSizeText.setColumns(5);

		packingAlgorithmsComboBox.addItem("BASIC");
		packingAlgorithmsComboBox.addItem("TPIM");
		packingAlgorithmsComboBox.addItem("TPIM w/o image rotation");

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				JPanel tileSize = new JPanel();
				tileSize.setLayout(new GridBagLayout());
				tileSize.add(autoTileSideLengthLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				tileSize.add(autoTileSideLengthText, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NONE, 0, 5, 0, 5));
				tileSize.add(new JLabel("m"), GuiUtil.setConstraints(2, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));

				content.add(kmzCheckbox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(showBoundingBoxCheckbox, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(showTileBordersCheckbox, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(writeJSONCheckbox, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(tileSize, GuiUtil.setConstraints(0, 4, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			generalPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				int lmargin = GuiUtil.getTextOffset(textureAtlasCheckbox);

				JPanel textureAtlas = new JPanel();
				textureAtlas.setLayout(new GridBagLayout());
				textureAtlas.add(textureAtlasCheckbox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				textureAtlas.add(packingAlgorithmsComboBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

				JPanel scaleTextures = new JPanel();
				scaleTextures.setLayout(new GridBagLayout());
				scaleTextures.add(scaleTexImagesCheckbox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				scaleTextures.add(scaleFactorSpinner, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 5, 0, 5));

				JPanel groupSize = new JPanel();
				groupSize.setLayout(new GridBagLayout());
				groupSize.add(groupObjectsCheckbox, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				groupSize.add(groupSizeText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 5, 0, 5));

				content.add(ignoreSurfaceOrientationCheckbox, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(generateSurfaceNormalsCheckbox, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(cropImagesCheckbox, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(textureAtlas, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(textureAtlasPotsCheckbox, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
				content.add(scaleTextures, GuiUtil.setConstraints(0, 5, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(groupSize, GuiUtil.setConstraints(0, 6, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			colladaPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				JPanel converter = new JPanel();
				converter.setLayout(new GridBagLayout());
				int lmargin = GuiUtil.getTextOffset(exportGltfV2);
				gltfConverterBrowseText.setPreferredSize(new Dimension(0, 0));

				converter.add(collada2gltfLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				converter.add(gltfConverterBrowseText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 5));
				converter.add(gltfConverterBrowseButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
				content.add(converter, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				content.add(notCreateColladaCheckbox, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(embedTexturesInGltfCheckbox, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(exportGltfBinary, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(exportGltfV1, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(exportGltfV2, GuiUtil.setConstraints(0, 5, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 5));
				content.add(enableGltfDracoCompression, GuiUtil.setConstraints(0, 6, 0, 0, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
			}

			gltfPanel = new TitledPanel()
					.withToggleButton(createGltfCheckbox)
					.build(content);
		}


		add(generalPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(colladaPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(gltfPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		PopupMenuDecorator.getInstance().decorate(autoTileSideLengthText);

		createGltfCheckbox.addActionListener(e -> excludeGltfAndKMZ(true));
		kmzCheckbox.addActionListener(e -> excludeGltfAndKMZ(false));
		createGltfCheckbox.addActionListener(e -> setEnabledGltfComponents());
		gltfConverterBrowseButton.addActionListener(e -> browseGltfConverterFile(Language.I18N.getString("pref.kmlexport.dialog.gltf.title")));
		exportGltfV1.addItemListener(e -> enableGltfDracoCompression.setEnabled(!exportGltfV1.isSelected()));

		autoTileSideLengthText.addPropertyChangeListener("value", evt -> {
			if (autoTileSideLengthText.getValue() == null
					|| ((Number) autoTileSideLengthText.getValue()).intValue() <= 1) {
				autoTileSideLengthText.setValue(125);
			}
		});

		groupSizeText.addPropertyChangeListener("value", evt -> {
			if (groupSizeText.getValue() == null
					|| ((Number) groupSizeText.getValue()).intValue() < 2)
				groupSizeText.setValue(1);
		});

		textureAtlasCheckbox.addActionListener(e -> {
			packingAlgorithmsComboBox.setEnabled(textureAtlasCheckbox.isSelected());
			textureAtlasPotsCheckbox.setEnabled(textureAtlasCheckbox.isSelected());
		});

		scaleTexImagesCheckbox.addActionListener(e -> scaleFactorSpinner.setEnabled(scaleTexImagesCheckbox.isSelected()));
		groupObjectsCheckbox.addActionListener(e -> groupSizeText.setEnabled(groupObjectsCheckbox.isSelected()));
	}

	private void excludeGltfAndKMZ(boolean deactivateKmz) {
		if (createGltfCheckbox.isSelected() && kmzCheckbox.isSelected()) {
			String option = Language.I18N.getString(deactivateKmz ?
					"pref.kmlexport.label.deactivateKmz" :
					"pref.kmlexport.label.deactivateGlTF");

			Object[] options = {option, Language.I18N.getString("common.button.cancel")};
			int choice = JOptionPane.showOptionDialog(getTopLevelAncestor(),
					Language.I18N.getString("pref.kmlexport.label.kmzGltfWarning"),
					Language.I18N.getString("common.dialog.warning.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			boolean activateKmz = (choice == JOptionPane.YES_OPTION && !deactivateKmz)
					|| (choice != JOptionPane.YES_OPTION && deactivateKmz);

			kmzCheckbox.setSelected(activateKmz);
			createGltfCheckbox.setSelected(!activateKmz);
		}

		setEnabledComponents();
	}

	@Override
	public void doTranslation() {
		generalPanel.setTitle(Language.I18N.getString("pref.kmlexport.border.generalOptions"));
		kmzCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.exportAsKmz"));
		showBoundingBoxCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.showBoundingBox"));
		showTileBordersCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.showTileBorders"));
		exportEmptyTilesCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.exportEmptyTiles"));
		writeJSONCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.writeJSONFile"));
		autoTileSideLengthLabel.setText(Language.I18N.getString("pref.kmlexport.label.autoTileSideLength"));

		colladaPanel.setTitle(Language.I18N.getString("pref.kmlexport.border.colladaOptions"));
		ignoreSurfaceOrientationCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.ignoreSurfaceOrientation"));
		generateSurfaceNormalsCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.generateSurfaceNormals"));
		cropImagesCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.cropTexImages"));
		textureAtlasCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.generateTextureAtlases"));
		textureAtlasPotsCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.textureAtlasPots"));
		scaleTexImagesCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.scaleTexImages"));
		groupObjectsCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.groupObjects"));

		gltfPanel.setTitle(Language.I18N.getString("pref.kmlexport.border.createGlTF"));
		collada2gltfLabel.setText(Language.I18N.getString("pref.kmlexport.label.collada2gltfTool"));
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
		writeJSONCheckbox.setSelected(kmlExportConfig.isWriteJSONFile());
		autoTileSideLengthText.setValue(kmlExportConfig.getQuery().getBboxFilter().getTilingOptions().getAutoTileSideLength());

		ColladaOptions colladaOptions = kmlExportConfig.getColladaOptions();
		ignoreSurfaceOrientationCheckbox.setSelected(colladaOptions.isIgnoreSurfaceOrientation());
		generateSurfaceNormalsCheckbox.setSelected(colladaOptions.isGenerateSurfaceNormals());
		cropImagesCheckbox.setSelected(colladaOptions.isCropImages());
		textureAtlasCheckbox.setSelected(colladaOptions.isGenerateTextureAtlases());
		textureAtlasPotsCheckbox.setSelected(colladaOptions.isTextureAtlasPots());
		for (String key: packingAlgorithms.keySet()) {
			if (packingAlgorithms.get(key).intValue() == colladaOptions.getPackingAlgorithm()) {
				packingAlgorithmsComboBox.setSelectedItem(key);
				break;
			}
		}

		scaleTexImagesCheckbox.setSelected(colladaOptions.isScaleImages());
		scaleFactorSpinner.setValue(colladaOptions.getImageScaleFactor());
		groupObjectsCheckbox.setSelected(colladaOptions.isGroupObjects());
		groupSizeText.setValue(colladaOptions.getGroupSize());

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
		kmlExportConfig.setWriteJSONFile(writeJSONCheckbox.isSelected());
		kmlExportConfig.getQuery().getBboxFilter().getTilingOptions().setAutoTileSideLength(((Number) autoTileSideLengthText.getValue()).intValue());

		ColladaOptions colladaOptions = kmlExportConfig.getColladaOptions();
		colladaOptions.setIgnoreSurfaceOrientation(ignoreSurfaceOrientationCheckbox.isSelected());
		colladaOptions.setGenerateSurfaceNormals(generateSurfaceNormalsCheckbox.isSelected());
		colladaOptions.setCropImages(cropImagesCheckbox.isSelected());
		colladaOptions.setGenerateTextureAtlases(textureAtlasCheckbox.isSelected());
		colladaOptions.setTextureAtlasPots(textureAtlasPotsCheckbox.isSelected());
		colladaOptions.setPackingAlgorithm(packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()).intValue());
		colladaOptions.setScaleImages(scaleTexImagesCheckbox.isSelected());
		colladaOptions.setImageScaleFactor(((Number) scaleFactorSpinner.getValue()).doubleValue());
		colladaOptions.setGroupObjects(groupObjectsCheckbox.isSelected());
		colladaOptions.setGroupSize(((Number) groupSizeText.getValue()).intValue());

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
		setEnabledColladaComponents();
		setEnabledGltfComponents();
	}

	private void setEnabledColladaComponents() {
		scaleFactorSpinner.setEnabled(scaleTexImagesCheckbox.isSelected());
		packingAlgorithmsComboBox.setEnabled(textureAtlasCheckbox.isSelected());
		textureAtlasPotsCheckbox.setEnabled(textureAtlasCheckbox.isSelected());
		groupSizeText.setEnabled(groupObjectsCheckbox.isSelected());
	}

	private void setEnabledGltfComponents() {
		collada2gltfLabel.setEnabled(createGltfCheckbox.isSelected());
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
