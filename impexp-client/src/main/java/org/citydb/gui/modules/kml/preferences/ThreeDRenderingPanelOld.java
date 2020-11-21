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

/*
package org.citydb.gui.modules.kml.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.gui.components.common.AlphaButton;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.textureAtlas.TextureAtlasCreator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ThreeDRenderingPanelOld extends AbstractPreferencesComponent {
	private final String i18nTitle;
	private final List<DisplayForm> displayForms;
	private final ColladaOptions colladaOptions;
	private final boolean showFootprintAndExtrudedOptions;
	private final boolean showGeometryOptions;
	private final boolean showColladaOptions;
	private final boolean showThematicSurfaceOptions;
	private final ArrayList<DisplayForm> internalDisplayForms = new ArrayList<>();
	private final Map<String, Integer> packingAlgorithms = new HashMap<>();

	private JPanel footprintContentPanel;
	private JCheckBox footprintHighlightingCheckbox;
	private JLabel footprintFillColorLabel;
	private JButton footprintFillColorButton;
	private JLabel footprintLineColorLabel;
	private JButton footprintLineColorButton;
	private JLabel footprintHLFillColorLabel;
	private JButton footprintHLFillColorButton;
	private JLabel footprintHLLineColorLabel;
	private JButton footprintHLLineColorButton;
	private JLabel footprintAlphaLabel;
	private JSpinner footprintAlphaSpinner;

	private JPanel geometryContentPanel;
	private JLabel geometryAlphaLabel;
	private JSpinner geometryAlphaSpinner;
	private JLabel geometryFillColorLabel;
	private JButton geometryFillColorButton;
	private JLabel geometryLineColorLabel;
	private JButton geometryLineColorButton;
	private JCheckBox geometryHighlightingCheckbox;
	private JLabel geometryHLSurfaceDistanceLabel;
	private JFormattedTextField geometryHLSurfaceDistanceText;
	private JLabel geometryHLFillColorLabel;
	private JButton geometryHLFillColorButton;
	private JLabel geometryHLLineColorLabel;
	private JButton geometryHLLineColorButton;
	private JLabel geometryRoofFillColorLabel;
	private JButton geometryRoofFillColorButton;
	private JLabel geometryRoofLineColorLabel;
	private JButton geometryRoofLineColorButton;

	private JPanel colladaContentPanel;
	private JCheckBox ignoreSurfaceOrientationCheckbox;
	private JCheckBox generateSurfaceNormalsCheckbox;
	private JCheckBox cropImagesCheckbox;
	private JCheckBox textureAtlasCheckbox;
	private JCheckBox textureAtlasPotsCheckbox;
	private JCheckBox scaleTexImagesCheckbox;
	private JFormattedTextField scaleFactorText;
	private JPanel colladaColorSubPanel;
	private JLabel colladaAlphaLabel;
	private JSpinner colladaAlphaSpinner;
	private JLabel colladaFillColorLabel;
	private JButton colladaFillColorButton;
	private JRadioButton groupObjectsRButton;
	private JFormattedTextField groupSizeText;
	private JRadioButton colladaHighlightingRButton;
	private JLabel colladaHLSurfaceDistanceLabel;
	private JFormattedTextField colladaHLSurfaceDistanceText;
	private JLabel colladaHLFillColorLabel;
	private JButton colladaHLFillColorButton;
	private JLabel colladaHLLineColorLabel;
	private JButton colladaHLLineColorButton;
	private JComboBox<String> packingAlgorithmsComboBox;
	private JLabel colladaRoofFillColorLabel;
	private JButton colladaRoofFillColorButton;

	public ThreeDRenderingPanelOld(String i18nTitle,
                                   List<DisplayForm> displayForms,
                                   boolean showFootprintAndExtrudedOptions,
                                   boolean showGeometryOptions,
                                   boolean showColladaOptions,
                                   boolean showThematicSurfaceOptions,
                                   Config config) {
		super(config);
		this.i18nTitle = i18nTitle;
		this.displayForms = displayForms;
		this.showFootprintAndExtrudedOptions = showFootprintAndExtrudedOptions;
		this.showGeometryOptions = showGeometryOptions;
		this.showColladaOptions = showColladaOptions;
		this.showThematicSurfaceOptions = showThematicSurfaceOptions;

		colladaOptions = config.getKmlExportConfig().getColladaOptions();

		packingAlgorithms.put("BASIC", TextureAtlasCreator.BASIC);
		packingAlgorithms.put("TPIM", TextureAtlasCreator.TPIM);
		packingAlgorithms.put("TPIM w/o image rotation", TextureAtlasCreator.TPIM_WO_ROTATION);

		initGui();
	}

	public ThreeDRenderingPanelOld(String i18nTitle, List<DisplayForm> displayForms, Config config) {
		this(i18nTitle, displayForms, true, true, true, false, config);
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString(i18nTitle);
	}

	@Override
	public boolean isModified() {
		try { geometryHLSurfaceDistanceText.commitEdit(); } catch (ParseException ignored) { }
		try { colladaHLSurfaceDistanceText.commitEdit(); } catch (ParseException ignored) { }
		try { scaleFactorText.commitEdit(); } catch (ParseException ignored) { }
		try { groupSizeText.commitEdit(); } catch (ParseException ignored) { }

		setInternalDisplayFormValues();

		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.COLLADA; form++) {
			DisplayForm displayForm = DisplayForm.get(form, displayForms);
			if (displayForm == null) {
				continue;
			}

			if (notEqual(displayForm, DisplayForm.get(form, internalDisplayForms))) return true;
		}

		if (ignoreSurfaceOrientationCheckbox.isSelected() != colladaOptions.isIgnoreSurfaceOrientation()) return true;
		if (generateSurfaceNormalsCheckbox.isSelected() != colladaOptions.isGenerateSurfaceNormals()) return true;
		if (cropImagesCheckbox.isSelected() != colladaOptions.isCropImages()) return true;
		if (textureAtlasCheckbox.isSelected() != colladaOptions.isGenerateTextureAtlases()) return true;
		if (textureAtlasPotsCheckbox.isSelected() != colladaOptions.isTextureAtlasPots()) return true;
		if (packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()) != colladaOptions.getPackingAlgorithm()) return true;
		if (groupObjectsRButton.isSelected() != colladaOptions.isGroupObjects()) return true;
		if (((Number) groupSizeText.getValue()).intValue() != colladaOptions.getGroupSize()) return true;
		if (scaleTexImagesCheckbox.isSelected() != colladaOptions.isScaleImages()) return true;
		if (((Number) scaleFactorText.getValue()).doubleValue() != colladaOptions.getImageScaleFactor()) return true;

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		initFootprintPanel();
		initGeometryPanel();
		initColladaPanel();

		if (showFootprintAndExtrudedOptions) {
			add(footprintContentPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		if (showGeometryOptions) {
			add(geometryContentPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		if (showColladaOptions) {
			add(colladaContentPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
	}

	private void initFootprintPanel() {
		footprintHighlightingCheckbox = new JCheckBox();
		footprintFillColorLabel = new JLabel();
		footprintFillColorButton = new AlphaButton();
		footprintLineColorLabel = new JLabel();
		footprintLineColorButton = new AlphaButton();
		footprintHLFillColorLabel = new JLabel();
		footprintHLFillColorButton = new AlphaButton();
		footprintHLLineColorLabel = new JLabel();
		footprintHLLineColorButton = new AlphaButton();
		footprintAlphaLabel = new JLabel();

		footprintContentPanel = new JPanel();
		footprintContentPanel.setBorder(BorderFactory.createTitledBorder(""));
		footprintContentPanel.setLayout(new GridBagLayout());

		SpinnerModel falphaValueModel = new SpinnerNumberModel(200, 0, 255, 1);
		footprintAlphaSpinner = new JSpinner(falphaValueModel);
//		footprintAlphaSpinner.setMinimumSize(new Dimension(footprintAlphaSpinner.getPreferredSize().width, 20));
//		footprintAlphaSpinner.setMaximumSize(new Dimension(footprintAlphaSpinner.getPreferredSize().width, 20));

		GridBagConstraints fal = GuiUtil.setConstraints(0, 0, 0.25, 1, GridBagConstraints.NONE, 0, 5, 5, 5);
		fal.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintAlphaLabel, fal);
		footprintContentPanel.add(footprintAlphaSpinner, GuiUtil.setConstraints(1, 0, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));

		GridBagConstraints ffcl = GuiUtil.setConstraints(0, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		ffcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintFillColorLabel, ffcl);

		footprintFillColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_COLOR, true));
		footprintFillColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintFillColorButton, GuiUtil.setConstraints(1, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 0));

		GridBagConstraints flcl = GuiUtil.setConstraints(2, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		flcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintLineColorLabel, flcl);

		footprintLineColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_COLOR, true));
		footprintLineColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintLineColorButton, GuiUtil.setConstraints(3, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 5));

		GridBagConstraints fhlcb = GuiUtil.setConstraints(0, 2, 0.5, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 0);
		fhlcb.anchor = GridBagConstraints.WEST;
		fhlcb.gridwidth = 2;
		footprintContentPanel.add(footprintHighlightingCheckbox, fhlcb);

		GridBagConstraints fhlfcl = GuiUtil.setConstraints(0, 3, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		fhlfcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintHLFillColorLabel, fhlfcl);

		footprintHLFillColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintHLFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		footprintHLFillColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintHLFillColorButton, GuiUtil.setConstraints(1, 3, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

		GridBagConstraints fhllcl = GuiUtil.setConstraints(2, 3, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		fhllcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintHLLineColorLabel, fhllcl);

		footprintHLLineColorButton.setPreferredSize(footprintAlphaSpinner.getPreferredSize());
		footprintHLLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		footprintHLLineColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintHLLineColorButton, GuiUtil.setConstraints(3, 3, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 5));

		footprintFillColorButton.addActionListener(e -> {
			Color fillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"),
					footprintFillColorButton.getBackground());
			if (fillColor != null)
				footprintFillColorButton.setBackground(fillColor);
		});

		footprintLineColorButton.addActionListener(e -> {
			Color lineColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseLineColor"),
					footprintLineColorButton.getBackground());
			if (lineColor != null)
				footprintLineColorButton.setBackground(lineColor);
		});

		footprintHLFillColorButton.addActionListener(e -> {
			Color hlFillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
					footprintHLFillColorButton.getBackground());
			if (hlFillColor != null)
				footprintHLFillColorButton.setBackground(hlFillColor);
		});

		footprintHLLineColorButton.addActionListener(e -> {
			Color hlLineColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"),
					footprintHLLineColorButton.getBackground());
			if (hlLineColor != null)
				footprintHLLineColorButton.setBackground(hlLineColor);
		});

		footprintHighlightingCheckbox.addActionListener(e -> setEnabledFootprintHighlighting());
	}

	private void initGeometryPanel() {
		geometryAlphaLabel = new JLabel();
		geometryFillColorLabel = new JLabel();
		geometryFillColorButton = new AlphaButton();
		geometryLineColorLabel = new JLabel();
		geometryLineColorButton = new AlphaButton();
		geometryHighlightingCheckbox = new JCheckBox();
		geometryHLSurfaceDistanceLabel = new JLabel();
		geometryHLFillColorLabel = new JLabel();
		geometryHLFillColorButton = new AlphaButton();
		geometryHLLineColorLabel = new JLabel();
		geometryHLLineColorButton = new AlphaButton();

		DecimalFormat format = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		format.setMaximumIntegerDigits(2);
		format.setMaximumFractionDigits(3);
		geometryHLSurfaceDistanceText = new JFormattedTextField(format);

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());

		SpinnerModel galphaValueModel = new SpinnerNumberModel(200, 0, 255, 1);
		geometryAlphaSpinner = new JSpinner(galphaValueModel);
		geometryAlphaSpinner.setMinimumSize(new Dimension(geometryAlphaSpinner.getPreferredSize().width, 20));
		geometryAlphaSpinner.setMaximumSize(new Dimension(geometryAlphaSpinner.getPreferredSize().width, 20));

		GridBagConstraints gal = GuiUtil.setConstraints(0, 0, 0.25, 1, GridBagConstraints.NONE, 0, 5, 5, 5);
		gal.anchor = GridBagConstraints.EAST;
		content.add(geometryAlphaLabel, gal);
		content.add(geometryAlphaSpinner, GuiUtil.setConstraints(1, 0, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 5, 0));

		GridBagConstraints gwcl = GuiUtil.setConstraints(0, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		gwcl.anchor = GridBagConstraints.EAST;
		content.add(geometryFillColorLabel, gwcl);

		geometryFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryFillColorButton.setBackground(new Color(showThematicSurfaceOptions ?
				DisplayForm.DEFAULT_WALL_FILL_COLOR :
				DisplayForm.DEFAULT_FILL_COLOR,
				true));
		geometryFillColorButton.setContentAreaFilled(false);
		content.add(geometryFillColorButton, GuiUtil.setConstraints(1, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 0));

		GridBagConstraints grcl = GuiUtil.setConstraints(2, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		grcl.anchor = GridBagConstraints.EAST;
		content.add(geometryLineColorLabel, grcl);

		geometryLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryLineColorButton.setBackground(new Color(showThematicSurfaceOptions ?
				DisplayForm.DEFAULT_WALL_LINE_COLOR :
				DisplayForm.DEFAULT_LINE_COLOR,
				true));
		geometryLineColorButton.setContentAreaFilled(false);
		content.add(geometryLineColorButton, GuiUtil.setConstraints(3, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 5));

		if (showThematicSurfaceOptions) {
			geometryRoofFillColorLabel = new JLabel();
			geometryRoofFillColorButton = new AlphaButton();
			geometryRoofLineColorLabel = new JLabel();
			geometryRoofLineColorButton = new AlphaButton();

			GridBagConstraints grfcl = GuiUtil.setConstraints(0, 2, 0.25, 1.0, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
			grfcl.anchor = GridBagConstraints.EAST;
			content.add(geometryRoofFillColorLabel, grfcl);

			geometryRoofFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
			geometryRoofFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_ROOF_FILL_COLOR, true));
			geometryRoofFillColorButton.setContentAreaFilled(false);
			content.add(geometryRoofFillColorButton, GuiUtil.setConstraints(1, 2, 0.25, 1.0, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

			GridBagConstraints grlcl = GuiUtil.setConstraints(2, 2, 0.25, 1.0, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
			grlcl.anchor = GridBagConstraints.EAST;
			content.add(geometryRoofLineColorLabel, grlcl);

			geometryRoofLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
			geometryRoofLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_ROOF_LINE_COLOR, true));
			geometryRoofLineColorButton.setContentAreaFilled(false);
			content.add(geometryRoofLineColorButton, GuiUtil.setConstraints(3, 2, 0.25, 1.0, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 5));
		}

		GridBagConstraints ghcb = GuiUtil.setConstraints(0, 3, 0, 1, GridBagConstraints.BOTH, 0, 5, 2 * 5, 0);
		ghcb.gridwidth = 2;
		content.add(geometryHighlightingCheckbox, ghcb);

		GridBagConstraints ghlfcl = GuiUtil.setConstraints(0, 4, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		ghlfcl.anchor = GridBagConstraints.EAST;
		content.add(geometryHLFillColorLabel, ghlfcl);

		geometryHLFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		geometryHLFillColorButton.setContentAreaFilled(false);
		content.add(geometryHLFillColorButton, GuiUtil.setConstraints(1, 4, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

		GridBagConstraints ghllcl = GuiUtil.setConstraints(2, 4, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		ghllcl.anchor = GridBagConstraints.EAST;
		content.add(geometryHLLineColorLabel, ghllcl);

		geometryHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		geometryHLLineColorButton.setContentAreaFilled(false);
		content.add(geometryHLLineColorButton, GuiUtil.setConstraints(3, 4, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 5));

		GridBagConstraints ghdl = GuiUtil.setConstraints(0, 5, 0, 1, GridBagConstraints.NONE, 0, 2 * 5, 2 * 5, 5);
		ghdl.anchor = GridBagConstraints.EAST;
		content.add(geometryHLSurfaceDistanceLabel, ghdl);

		GridBagConstraints ghdt = GuiUtil.setConstraints(1, 5, 0, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0);
		content.add(geometryHLSurfaceDistanceText, ghdt);

		geometryContentPanel = new JPanel();
		geometryContentPanel.setBorder(BorderFactory.createTitledBorder(""));
		geometryContentPanel.setLayout(new GridBagLayout());
		{
			geometryContentPanel.add(content, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		}

		PopupMenuDecorator.getInstance().decorate(geometryHLSurfaceDistanceText);

		geometryFillColorButton.addActionListener(e -> {
			Color wallFillColor = chooseColor(Language.I18N.getString(showThematicSurfaceOptions ?
							"pref.kmlexport.label.chooseWallFillColor" :
							"pref.kmlexport.label.fillColor"),
					geometryFillColorButton.getBackground());
			if (wallFillColor != null)
				geometryFillColorButton.setBackground(wallFillColor);
		});

		geometryLineColorButton.addActionListener(e -> {
			Color wallLineColor = chooseColor(Language.I18N.getString(showThematicSurfaceOptions ?
							"pref.kmlexport.label.chooseWallLineColor" :
							"pref.kmlexport.label.lineColor"),
					geometryLineColorButton.getBackground());
			if (wallLineColor != null)
				geometryLineColorButton.setBackground(wallLineColor);
		});

		geometryHLFillColorButton.addActionListener(e -> {
			Color hlFillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
					geometryHLFillColorButton.getBackground());
			if (hlFillColor != null)
				geometryHLFillColorButton.setBackground(hlFillColor);
		});

		geometryHLLineColorButton.addActionListener(e -> {
			Color hlLineColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"),
					geometryHLLineColorButton.getBackground());
			if (hlLineColor != null)
				geometryHLLineColorButton.setBackground(hlLineColor);
		});

		if (showThematicSurfaceOptions) {
			geometryRoofFillColorButton.addActionListener(e -> {
				Color roofFillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseRoofFillColor"),
						geometryRoofFillColorButton.getBackground());
				if (roofFillColor != null)
					geometryRoofFillColorButton.setBackground(roofFillColor);
			});

			geometryRoofLineColorButton.addActionListener(e -> {
				Color roofLineColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseRoofLineColor"),
						geometryRoofLineColorButton.getBackground());
				if (roofLineColor != null)
					geometryRoofLineColorButton.setBackground(roofLineColor);
			});
		}

		geometryHLSurfaceDistanceText.addPropertyChangeListener(evt -> checkHighlightingDistance(geometryHLSurfaceDistanceText));
		geometryHighlightingCheckbox.addActionListener(e -> setEnabledGeometryHighlighting());
	}

	private void initColladaPanel() {
		ignoreSurfaceOrientationCheckbox = new JCheckBox();
		generateSurfaceNormalsCheckbox = new JCheckBox();
		cropImagesCheckbox = new JCheckBox();
		textureAtlasCheckbox = new JCheckBox();
		textureAtlasPotsCheckbox = new JCheckBox();
		scaleTexImagesCheckbox = new JCheckBox();
		colladaAlphaLabel = new JLabel();
		colladaFillColorLabel = new JLabel();
		colladaFillColorButton = new AlphaButton();
		groupObjectsRButton = new JRadioButton();
		colladaHighlightingRButton = new JRadioButton();
		colladaHLSurfaceDistanceLabel = new JLabel();
		colladaHLFillColorLabel = new JLabel();
		colladaHLFillColorButton = new AlphaButton();
		colladaHLLineColorLabel = new JLabel();
		colladaHLLineColorButton = new AlphaButton();
		packingAlgorithmsComboBox = new JComboBox<>();

		DecimalFormat scaleFormat = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		scaleFormat.setMaximumIntegerDigits(1);
		scaleFormat.setMaximumFractionDigits(3);
		scaleFactorText = new JFormattedTextField(scaleFormat);

		DecimalFormat highlightFormat = new DecimalFormat("##.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		highlightFormat.setMaximumIntegerDigits(2);
		highlightFormat.setMaximumFractionDigits(3);
		colladaHLSurfaceDistanceText = new JFormattedTextField(highlightFormat);

		DecimalFormat groupSizeFormat = new DecimalFormat("#");
		groupSizeFormat.setMaximumIntegerDigits(8);
		groupSizeFormat.setMinimumIntegerDigits(1);
		groupSizeText = new JFormattedTextField(groupSizeFormat);

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());

		GridBagConstraints isoc = GuiUtil.setConstraints(0, 0, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 0);
		isoc.gridwidth = 2;
		content.add(ignoreSurfaceOrientationCheckbox, isoc);

		GridBagConstraints gsnc = GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 0);
		gsnc.gridwidth = 2;
		content.add(generateSurfaceNormalsCheckbox, gsnc);

		GridBagConstraints cI = GuiUtil.setConstraints(0, 2, 0, 1, GridBagConstraints.BOTH, 0, 5, 0, 0);
		cI.gridwidth = 2;
		content.add(cropImagesCheckbox, cI);

		packingAlgorithmsComboBox.addItem("BASIC");
		packingAlgorithmsComboBox.addItem("TPIM");
		packingAlgorithmsComboBox.addItem("TPIM w/o image rotation");

		content.add(textureAtlasCheckbox, GuiUtil.setConstraints(0, 3, 0, 1, GridBagConstraints.BOTH, 0, 5, 2, 0));
		content.add(packingAlgorithmsComboBox, GuiUtil.setConstraints(1, 3, 1, 1, GridBagConstraints.BOTH, 0, 5, 2, 5));

		int lmargin = GuiUtil.getTextOffset(textureAtlasCheckbox) + 5;
		GridBagConstraints tapc = GuiUtil.setConstraints(0, 4, 0, 1, GridBagConstraints.BOTH, 5, lmargin, 5, 0);
		tapc.gridwidth = 2;
		content.add(textureAtlasPotsCheckbox, tapc);

		content.add(scaleTexImagesCheckbox, GuiUtil.setConstraints(0, 5, 0, 1, GridBagConstraints.BOTH, 0, 5, 2, 0));
		content.add(scaleFactorText, GuiUtil.setConstraints(1, 5, 1, 1, GridBagConstraints.BOTH, 0, 5, 2, 5));

		// color settings for collada and gltf
		colladaColorSubPanel = new JPanel();
		colladaColorSubPanel.setLayout(new GridBagLayout());
		colladaColorSubPanel.setBorder(BorderFactory.createTitledBorder(""));
		GridBagConstraints cclsp = GuiUtil.setConstraints(0, 6, 0, 1, GridBagConstraints.BOTH, 5 * 2, 5, 2 * 5, 5);
		cclsp.gridwidth = 2;
		content.add(colladaColorSubPanel, cclsp);

		SpinnerModel cAlphaValueModel = new SpinnerNumberModel(255, 0, 255, 1);
		colladaAlphaSpinner = new JSpinner(cAlphaValueModel);
		colladaAlphaSpinner.setPreferredSize(geometryAlphaSpinner.getPreferredSize());

		GridBagConstraints cal = GuiUtil.setConstraints(0, 0, 0.25, 1, GridBagConstraints.NONE, 5, 0, 5, 5);
		cal.anchor = GridBagConstraints.EAST;
		colladaColorSubPanel.add(colladaAlphaLabel, cal);
		colladaColorSubPanel.add(colladaAlphaSpinner, GuiUtil.setConstraints(1, 0, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 5, 5, 5));

		GridBagConstraints cwfcl = GuiUtil.setConstraints(0, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 5, 5);
		cwfcl.anchor = GridBagConstraints.EAST;
		colladaColorSubPanel.add(colladaFillColorLabel, cwfcl);

		colladaFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_COLLADA_FILL_COLOR, true));
		colladaFillColorButton.setContentAreaFilled(false);
		colladaColorSubPanel.add(colladaFillColorButton, GuiUtil.setConstraints(1, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 5, 0));

		if (showThematicSurfaceOptions) {
			colladaRoofFillColorLabel = new JLabel();
			colladaRoofFillColorButton = new AlphaButton();

			GridBagConstraints crfcl = GuiUtil.setConstraints(0,2,0.25,1.0,GridBagConstraints.NONE,5,5,5,5);
			crfcl.anchor = GridBagConstraints.EAST;
			colladaColorSubPanel.add(colladaRoofFillColorLabel, crfcl);

			colladaRoofFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
			colladaRoofFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_COLLADA_ROOF_FILL_COLOR, true));
			colladaRoofFillColorButton.setContentAreaFilled(false);
			colladaColorSubPanel.add(colladaRoofFillColorButton, GuiUtil.setConstraints(1,2,0.25,1.0,GridBagConstraints.HORIZONTAL,5,0,5,5));
		}

		// highlighting settings (just for collada and Google Earch)
		ButtonGroup colladaRadioGroup = new ButtonGroup();
		colladaRadioGroup.add(groupObjectsRButton);
		colladaRadioGroup.add(colladaHighlightingRButton);

		content.add(groupObjectsRButton, GuiUtil.setConstraints(0, 7, 0, 1, GridBagConstraints.BOTH, 0, 5, 2, 0));
		content.add(groupSizeText, GuiUtil.setConstraints(1, 7, 1, 1, GridBagConstraints.BOTH, 0, 5, 2, 5));

		GridBagConstraints chrb = GuiUtil.setConstraints(0, 8, 0, 1, GridBagConstraints.BOTH, 0, 5, 2 * 5, 0);
		chrb.gridwidth = 2;
		content.add(colladaHighlightingRButton, chrb);

		JPanel colladaHLSubPanel = new JPanel();
		colladaHLSubPanel.setLayout(new GridBagLayout());
		GridBagConstraints chlsp = GuiUtil.setConstraints(0, 9, 0, 1, GridBagConstraints.BOTH, 0, 0, 0, 0);
		chlsp.gridwidth = 2;
		content.add(colladaHLSubPanel, chlsp);

		GridBagConstraints chlfcl = GuiUtil.setConstraints(0, 0, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		chlfcl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLFillColorLabel, chlfcl);

		colladaHLFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaHLFillColorButton.setBackground(new Color(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR, true));
		colladaHLFillColorButton.setContentAreaFilled(false);
		colladaHLSubPanel.add(colladaHLFillColorButton, GuiUtil.setConstraints(1, 0, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

		GridBagConstraints chllcl = GuiUtil.setConstraints(2, 0, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		chllcl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLLineColorLabel, chllcl);

		colladaHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaHLLineColorButton.setBackground(new Color(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR, true));
		colladaHLLineColorButton.setContentAreaFilled(false);
		colladaHLSubPanel.add(colladaHLLineColorButton, GuiUtil.setConstraints(3, 0, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 5));

		GridBagConstraints chldl = GuiUtil.setConstraints(0, 1, 0, 1, GridBagConstraints.NONE, 0, 2 * 5, 2 * 5, 5);
		chldl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLSurfaceDistanceLabel, chldl);

		GridBagConstraints chldt = GuiUtil.setConstraints(1, 1, 0, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0);
		colladaHLSubPanel.add(colladaHLSurfaceDistanceText, chldt);

		colladaContentPanel = new JPanel();
		colladaContentPanel.setBorder(BorderFactory.createTitledBorder(""));
		colladaContentPanel.setLayout(new GridBagLayout());
		{
			colladaContentPanel.add(content, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
		}

		PopupMenuDecorator.getInstance().decorate(scaleFactorText, groupSizeText, colladaHLSurfaceDistanceText);

		colladaFillColorButton.addActionListener(e -> {
			Color wallFillColor = chooseColor(Language.I18N.getString(showThematicSurfaceOptions ?
							"pref.kmlexport.label.chooseWallFillColor" :
							"pref.kmlexport.label.chooseFillColor"),
					colladaFillColorButton.getBackground());
			if (wallFillColor != null)
				colladaFillColorButton.setBackground(wallFillColor);
		});

		if (showThematicSurfaceOptions) {
			colladaRoofFillColorButton.addActionListener(e -> {
				Color roofFillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseRoofFillColor"),
						colladaRoofFillColorButton.getBackground());
				if (roofFillColor != null)
					colladaRoofFillColorButton.setBackground(roofFillColor);
			});
		}

		colladaHLFillColorButton.addActionListener(e -> {
			Color hlFillColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"),
					colladaHLFillColorButton.getBackground());
			if (hlFillColor != null)
				colladaHLFillColorButton.setBackground(hlFillColor);
		});

		colladaHLLineColorButton.addActionListener(e -> {
			Color hlLineColor = chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"),
					colladaHLLineColorButton.getBackground());
			if (hlLineColor != null)
				colladaHLLineColorButton.setBackground(hlLineColor);
		});

		scaleFactorText.addPropertyChangeListener(evt -> {
			if (scaleFactorText.getValue() == null
					|| ((Number) scaleFactorText.getValue()).doubleValue() <= 0
					|| ((Number) scaleFactorText.getValue()).doubleValue() > 1)
				scaleFactorText.setValue(1);
		});

		groupSizeText.addPropertyChangeListener(evt -> {
			if (groupSizeText.getValue() == null
					|| ((Number) groupSizeText.getValue()).intValue() < 2)
				groupSizeText.setValue(1);
		});

		textureAtlasCheckbox.addActionListener(e -> {
			packingAlgorithmsComboBox.setEnabled(textureAtlasCheckbox.isSelected());
			textureAtlasPotsCheckbox.setEnabled(textureAtlasCheckbox.isSelected());
		});

		colladaHLSurfaceDistanceText.addPropertyChangeListener(evt -> checkHighlightingDistance(colladaHLSurfaceDistanceText));
		scaleTexImagesCheckbox.addActionListener(e -> scaleFactorText.setEnabled(scaleTexImagesCheckbox.isSelected()));
		groupObjectsRButton.addActionListener(e -> setEnabledColladaHighlighting());
		colladaHighlightingRButton.addActionListener(e -> setEnabledColladaHighlighting());
	}

	public void addFootprintAndExtrudedOptions(JLabel label, JComponent component) {
		if (showFootprintAndExtrudedOptions) {
			footprintContentPanel.add(label, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
			footprintContentPanel.add(component, GuiUtil.setConstraints(1, 4, 3, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
		}
	}

	public void addGeometryOptions(JComponent component) {
		if (showGeometryOptions) {
			geometryContentPanel.add(component, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
		}
	}

	public void addColladaOptions(JComponent component) {
		if (showColladaOptions) {
			colladaContentPanel.add(component, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
		}
	}

	@Override
	public void doTranslation() {

		((TitledBorder) footprintContentPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.border.footprint"));
		((TitledBorder) geometryContentPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.border.geometry"));
		((TitledBorder) colladaContentPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.border.collada"));

		footprintAlphaLabel.setText(Language.I18N.getString("pref.kmlexport.label.alpha"));
		footprintFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		footprintLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.lineColor"));
		footprintHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlighting"));
		footprintHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		footprintHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedLineColor"));

		geometryAlphaLabel.setText(Language.I18N.getString("pref.kmlexport.label.alpha"));
		geometryFillColorLabel.setText(Language.I18N.getString(showThematicSurfaceOptions ?
				"pref.kmlexport.label.wallFillColor" :
				"pref.kmlexport.label.fillColor"));
		geometryLineColorLabel.setText(Language.I18N.getString(showThematicSurfaceOptions ?
				"pref.kmlexport.label.wallLineColor" :
				"pref.kmlexport.label.lineColor"));

		if (showThematicSurfaceOptions) {
			geometryRoofFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.roofFillColor"));
			geometryRoofLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.roofLineColor"));
		}

		geometryHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlighting"));
		geometryHLSurfaceDistanceLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		geometryHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		geometryHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedLineColor"));

		ignoreSurfaceOrientationCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.ignoreSurfaceOrientation"));
		generateSurfaceNormalsCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.generateSurfaceNormals"));
		cropImagesCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.cropTexImages"));
		textureAtlasCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.generateTextureAtlases"));
		textureAtlasPotsCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.textureAtlasPots"));
		scaleTexImagesCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.scaleTexImages"));
		((TitledBorder)colladaColorSubPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.label.colladaGltfColorSettings"));
		colladaAlphaLabel.setText(Language.I18N.getString("pref.kmlexport.label.alpha"));
		colladaFillColorLabel.setText(Language.I18N.getString(showThematicSurfaceOptions ?
				"pref.kmlexport.label.wallFillColor" :
				"pref.kmlexport.label.fillColor"));

		if (showThematicSurfaceOptions) {
			colladaRoofFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.roofFillColor"));
		}

		groupObjectsRButton.setText(Language.I18N.getString("pref.kmlexport.label.groupObjects"));
		colladaHighlightingRButton.setText(Language.I18N.getString("pref.kmlexport.colladaDisplay.label.highlighting"));
		colladaHLSurfaceDistanceLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		colladaHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		colladaHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedLineColor"));
	}

	private void checkHighlightingDistance(JFormattedTextField textField) {
		if (textField.getValue() == null
				|| ((Number) textField.getValue()).doubleValue() <= 0
				|| ((Number) textField.getValue()).doubleValue() > 10)
			textField.setValue(1);
	}

	@Override
	public void loadSettings() {
		internalDisplayForms.clear();

		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.COLLADA; form++) {
			DisplayForm configDf = DisplayForm.of(form);
			int indexOfConfigDf = displayForms.indexOf(configDf);
			if (indexOfConfigDf != -1) {
				configDf = displayForms.get(indexOfConfigDf);
			}
			DisplayForm internalDf = configDf.clone();
			internalDisplayForms.add(internalDf);
		}

		for (DisplayForm displayForm : internalDisplayForms) {
			switch (displayForm.getForm()) {
				case DisplayForm.FOOTPRINT:
				case DisplayForm.EXTRUDED:
					footprintHighlightingCheckbox.setSelected(displayForm.isHighlightingEnabled());

					if (displayForm.isSetRgba0()) {
						footprintFillColorButton.setBackground(new Color(displayForm.getRgba0()));
						footprintAlphaSpinner.setValue(new Integer(new Color(displayForm.getRgba0(), true).getAlpha()));
					}
					if (displayForm.isSetRgba1())
						footprintLineColorButton.setBackground(new Color(displayForm.getRgba1()));
					if (displayForm.isSetRgba4())
						footprintHLFillColorButton.setBackground(new Color(displayForm.getRgba4()));
					if (displayForm.isSetRgba5())
						footprintHLLineColorButton.setBackground(new Color(displayForm.getRgba5()));
					break;
				case DisplayForm.GEOMETRY:
					geometryHighlightingCheckbox.setSelected(displayForm.isHighlightingEnabled());
					geometryHLSurfaceDistanceText.setValue(displayForm.getHighlightingDistance());

					if (displayForm.isSetRgba0()) {
						geometryFillColorButton.setBackground(new Color(displayForm.getRgba0()));
						geometryAlphaSpinner.setValue(new Integer(new Color(displayForm.getRgba0(), true).getAlpha()));
					}
					if (displayForm.isSetRgba1())
						geometryLineColorButton.setBackground(new Color(displayForm.getRgba1()));

					if (showThematicSurfaceOptions) {
						if (displayForm.isSetRgba2())
							geometryRoofFillColorButton.setBackground(new Color(displayForm.getRgba2()));
						if (displayForm.isSetRgba3())
							geometryRoofLineColorButton.setBackground(new Color(displayForm.getRgba3()));
					}

					if (displayForm.isSetRgba4())
						geometryHLFillColorButton.setBackground(new Color(displayForm.getRgba4()));
					if (displayForm.isSetRgba5())
						geometryHLLineColorButton.setBackground(new Color(displayForm.getRgba5()));
					break;
				case DisplayForm.COLLADA:
					colladaHighlightingRButton.setSelected(displayForm.isHighlightingEnabled());
					colladaHLSurfaceDistanceText.setValue(displayForm.getHighlightingDistance());

					if (displayForm.isSetRgba0()) {
						colladaFillColorButton.setBackground(new Color(displayForm.getRgba0()));
						colladaAlphaSpinner.setValue(new Integer(new Color(displayForm.getRgba0(), true).getAlpha()));
					}
					if (showThematicSurfaceOptions) {
						if (displayForm.isSetRgba2())
							colladaRoofFillColorButton.setBackground(new Color(displayForm.getRgba2()));
					}

					if (displayForm.isSetRgba4())
						colladaHLFillColorButton.setBackground(new Color(displayForm.getRgba4()));
					if (displayForm.isSetRgba5())
						colladaHLLineColorButton.setBackground(new Color(displayForm.getRgba5()));
					break;
			}
		}

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
		scaleFactorText.setValue(colladaOptions.getImageScaleFactor());
		groupObjectsRButton.setSelected(colladaOptions.isGroupObjects());
		groupSizeText.setValue(colladaOptions.getGroupSize());

		setEnabledSettings();
	}

	@Override
	public void setSettings() {
		setInternalDisplayFormValues();

		if (displayForms.isEmpty()) {
			displayForms.addAll(internalDisplayForms);
		} else {
			for (DisplayForm internalDf : internalDisplayForms) {
				int indexOfConfigDf = displayForms.indexOf(internalDf);
				if (indexOfConfigDf != -1) {
					DisplayForm configDf = displayForms.get(indexOfConfigDf);
					// clone cannot be used here because of isActive() and visibleFrom()
					copyColorAndHighlightingValues(internalDf, configDf);
				}
			}
		}

		colladaOptions.setIgnoreSurfaceOrientation(ignoreSurfaceOrientationCheckbox.isSelected());
		colladaOptions.setGenerateSurfaceNormals(generateSurfaceNormalsCheckbox.isSelected());
		colladaOptions.setCropImages(cropImagesCheckbox.isSelected());
		colladaOptions.setGenerateTextureAtlases(textureAtlasCheckbox.isSelected());
		colladaOptions.setTextureAtlasPots(textureAtlasPotsCheckbox.isSelected());
		colladaOptions.setPackingAlgorithm(packingAlgorithms.get(packingAlgorithmsComboBox.getSelectedItem()).intValue());
		colladaOptions.setScaleImages(scaleTexImagesCheckbox.isSelected());
		colladaOptions.setImageScaleFactor(((Number) scaleFactorText.getValue()).doubleValue());
		colladaOptions.setGroupObjects(groupObjectsRButton.isSelected());
		colladaOptions.setGroupSize(((Number) groupSizeText.getValue()).intValue());
	}

	private void setInternalDisplayFormValues() {
		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.EXTRUDED; form++) {
			DisplayForm df = DisplayForm.of(form);
			int indexOfDf = internalDisplayForms.indexOf(df);
			if (indexOfDf != -1) {
				df = internalDisplayForms.get(indexOfDf);
				df.setHighlightingEnabled(footprintHighlightingCheckbox.isSelected());

				Color rgba0 = new Color(footprintFillColorButton.getBackground().getRed(),
						footprintFillColorButton.getBackground().getGreen(),
						footprintFillColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba0(rgba0.getRGB());
				Color rgba1 = new Color(footprintLineColorButton.getBackground().getRed(),
						footprintLineColorButton.getBackground().getGreen(),
						footprintLineColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba1(rgba1.getRGB());
				Color rgba4 = new Color(footprintHLFillColorButton.getBackground().getRed(),
						footprintHLFillColorButton.getBackground().getGreen(),
						footprintHLFillColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba4(rgba4.getRGB());
				Color rgba5 = new Color(footprintHLLineColorButton.getBackground().getRed(),
						footprintHLLineColorButton.getBackground().getGreen(),
						footprintHLLineColorButton.getBackground().getBlue(),
						((Integer)footprintAlphaSpinner.getValue()).intValue());
				df.setRgba5(rgba5.getRGB());
			}
		}

		DisplayForm df = DisplayForm.of(DisplayForm.GEOMETRY);
		int indexOfDf = internalDisplayForms.indexOf(df);
		if (indexOfDf != -1) {
			df = internalDisplayForms.get(indexOfDf);
			df.setHighlightingEnabled(geometryHighlightingCheckbox.isSelected());
			df.setHighlightingDistance(((Number) geometryHLSurfaceDistanceText.getValue()).doubleValue());

			Color rgba0 = new Color(geometryFillColorButton.getBackground().getRed(),
					geometryFillColorButton.getBackground().getGreen(),
					geometryFillColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			df.setRgba0(rgba0.getRGB());
			Color rgba1 = new Color(geometryLineColorButton.getBackground().getRed(),
					geometryLineColorButton.getBackground().getGreen(),
					geometryLineColorButton.getBackground().getBlue(),
					((Integer)geometryAlphaSpinner.getValue()).intValue());
			df.setRgba1(rgba1.getRGB());

			if (showThematicSurfaceOptions) {
				Color rgba2 = new Color(geometryRoofFillColorButton.getBackground().getRed(),
						geometryRoofFillColorButton.getBackground().getGreen(),
						geometryRoofFillColorButton.getBackground().getBlue(),
						((Integer) geometryAlphaSpinner.getValue()).intValue());
				df.setRgba2(rgba2.getRGB());
				Color rgba3 = new Color(geometryRoofLineColorButton.getBackground().getRed(),
						geometryRoofLineColorButton.getBackground().getGreen(),
						geometryRoofLineColorButton.getBackground().getBlue(),
						((Integer) geometryAlphaSpinner.getValue()).intValue());
				df.setRgba3(rgba3.getRGB());
			}

			Color rgba4 = new Color(geometryHLFillColorButton.getBackground().getRed(),
					geometryHLFillColorButton.getBackground().getGreen(),
					geometryHLFillColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba4(rgba4.getRGB());
			Color rgba5 = new Color(geometryHLLineColorButton.getBackground().getRed(),
					geometryHLLineColorButton.getBackground().getGreen(),
					geometryHLLineColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba5(rgba5.getRGB());
		}

		df = DisplayForm.of(DisplayForm.COLLADA);
		indexOfDf = internalDisplayForms.indexOf(df);
		if (indexOfDf != -1) {
			df = internalDisplayForms.get(indexOfDf);
			df.setHighlightingEnabled(colladaHighlightingRButton.isSelected());
			df.setHighlightingDistance(((Number) colladaHLSurfaceDistanceText.getValue()).doubleValue());

			Color rgba0 = new Color(colladaFillColorButton.getBackground().getRed(),
					colladaFillColorButton.getBackground().getGreen(),
					colladaFillColorButton.getBackground().getBlue(),
					((Integer)colladaAlphaSpinner.getValue()).intValue());
			df.setRgba0(rgba0.getRGB());

			if (showThematicSurfaceOptions) {
				Color rgba2 = new Color(colladaRoofFillColorButton.getBackground().getRed(),
						colladaRoofFillColorButton.getBackground().getGreen(),
						colladaRoofFillColorButton.getBackground().getBlue(),
						((Integer)colladaAlphaSpinner.getValue()).intValue());
				df.setRgba2(rgba2.getRGB());
			}

			Color rgba4 = new Color(colladaHLFillColorButton.getBackground().getRed(),
					colladaHLFillColorButton.getBackground().getGreen(),
					colladaHLFillColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba4(rgba4.getRGB());
			Color rgba5 = new Color(colladaHLLineColorButton.getBackground().getRed(),
					colladaHLLineColorButton.getBackground().getGreen(),
					colladaHLLineColorButton.getBackground().getBlue(),
					DisplayForm.DEFAULT_ALPHA_VALUE);
			df.setRgba5(rgba5.getRGB());
		}
	}

	@Override
	public void resetSettings() {
		for (int form = DisplayForm.FOOTPRINT; form <= DisplayForm.EXTRUDED; form++) {
			DisplayForm df = DisplayForm.of(form);
			int indexOfDf = displayForms.indexOf(df);
			if (indexOfDf != -1) {
				df = displayForms.get(indexOfDf);
				df.setHighlightingEnabled(false);
				df.setRgba0(DisplayForm.DEFAULT_FILL_COLOR);
				df.setRgba1(DisplayForm.DEFAULT_LINE_COLOR);
				df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
				df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
			}
		}

		DisplayForm df = DisplayForm.of(DisplayForm.GEOMETRY);
		int indexOfDf = displayForms.indexOf(df);
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
			df.setHighlightingEnabled(false);
			df.setHighlightingDistance(0.75);
			df.setRgba0(showThematicSurfaceOptions ? DisplayForm.DEFAULT_WALL_FILL_COLOR : DisplayForm.DEFAULT_FILL_COLOR);
			df.setRgba1(showThematicSurfaceOptions ? DisplayForm.DEFAULT_WALL_LINE_COLOR : DisplayForm.DEFAULT_LINE_COLOR);
			df.setRgba2(DisplayForm.DEFAULT_ROOF_FILL_COLOR);
			df.setRgba3(DisplayForm.DEFAULT_ROOF_LINE_COLOR);
			df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}

		df = DisplayForm.of(DisplayForm.COLLADA);
		indexOfDf = displayForms.indexOf(df);
		if (indexOfDf != -1) {
			df = displayForms.get(indexOfDf);
			df.setHighlightingEnabled(false);
			df.setHighlightingDistance(0.75);
			df.setRgba0(DisplayForm.DEFAULT_COLLADA_FILL_COLOR);
			df.setRgba2(DisplayForm.DEFAULT_COLLADA_ROOF_FILL_COLOR);
			df.setRgba4(DisplayForm.DEFAULT_FILL_HIGHLIGHTED_COLOR);
			df.setRgba5(DisplayForm.DEFAULT_LINE_HIGHLIGHTED_COLOR);
		}

		ColladaOptions.resetSettings(colladaOptions);
		loadSettings();
	}

	private void setEnabledSettings() {
		scaleFactorText.setEnabled(scaleTexImagesCheckbox.isSelected());
		packingAlgorithmsComboBox.setEnabled(textureAtlasCheckbox.isSelected());
		textureAtlasPotsCheckbox.setEnabled(textureAtlasCheckbox.isSelected());

		setEnabledFootprintHighlighting();
		setEnabledGeometryHighlighting();
		setEnabledColladaHighlighting();
	}

	private void setEnabledFootprintHighlighting() {
		footprintHLFillColorLabel.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLFillColorButton.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLLineColorLabel.setEnabled(footprintHighlightingCheckbox.isSelected());
		footprintHLLineColorButton.setEnabled(footprintHighlightingCheckbox.isSelected());
	}

	private void setEnabledGeometryHighlighting() {
		geometryHLFillColorLabel.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLFillColorButton.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLLineColorLabel.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLLineColorButton.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLSurfaceDistanceLabel.setEnabled(geometryHighlightingCheckbox.isSelected());
		geometryHLSurfaceDistanceText.setEnabled(geometryHighlightingCheckbox.isSelected());
	}

	private void setEnabledColladaHighlighting() {
		colladaHLFillColorLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLFillColorButton.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLLineColorLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLLineColorButton.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLSurfaceDistanceLabel.setEnabled(colladaHighlightingRButton.isSelected());
		colladaHLSurfaceDistanceText.setEnabled(colladaHighlightingRButton.isSelected());
		groupSizeText.setEnabled(groupObjectsRButton.isSelected());
	}

	private boolean notEqual(DisplayForm first, DisplayForm second) {
		if (first == null || second == null) return true;
		if (first.isHighlightingEnabled() != second.isHighlightingEnabled()) return true;
		if (first.getHighlightingDistance() != second.getHighlightingDistance()) return true;
		if (first.getRgba0() != second.getRgba0()) return true;
		if (first.getRgba1() != second.getRgba1()) return true;
		if (first.getRgba2() != second.getRgba2()) return true;
		if (first.getRgba3() != second.getRgba3()) return true;
		if (first.getRgba4() != second.getRgba4()) return true;
		if (first.getRgba5() != second.getRgba5()) return true;
		return false;
	}

	private void copyColorAndHighlightingValues(DisplayForm original, DisplayForm copy) {
		copy.setHighlightingDistance(original.getHighlightingDistance());
		copy.setHighlightingEnabled(original.isHighlightingEnabled());
		copy.setRgba0(original.getRgba0());
		copy.setRgba1(original.getRgba1());
		copy.setRgba2(original.getRgba2());
		copy.setRgba3(original.getRgba3());
		copy.setRgba4(original.getRgba4());
		copy.setRgba5(original.getRgba5());
	}

	private Color chooseColor(String title, Color initialColor){
		return JColorChooser.showDialog(getTopLevelAncestor(), title, initialColor);
	}
}


 */