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
import org.citydb.config.project.kmlExporter.DisplayFormType;
import org.citydb.config.project.kmlExporter.Style;
import org.citydb.config.project.kmlExporter.Styles;
import org.citydb.gui.components.common.AlphaButton;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.Locale;

public class SurfaceAndSolidStylingPanel extends AbstractPreferencesComponent {
	private final String i18nTitle;
	private final boolean showFootprintAndExtrudedOptions;
	private final boolean showGeometryOptions;
	private final boolean showColladaOptions;
	private final boolean showThematicSurfaceOptions;
	private final Styles internalStyles = new Styles();
	private Styles styles;

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
	private JPanel colladaColorSubPanel;
	private JLabel colladaAlphaLabel;
	private JLabel colladaFillColorLabel;
	private JButton colladaFillColorButton;
	private JRadioButton colladaHighlightingRButton;
	private JLabel colladaHLSurfaceDistanceLabel;
	private JFormattedTextField colladaHLSurfaceDistanceText;
	private JLabel colladaHLFillColorLabel;
	private JButton colladaHLFillColorButton;
	private JLabel colladaHLLineColorLabel;
	private JButton colladaHLLineColorButton;
	private JLabel colladaRoofFillColorLabel;
	private JButton colladaRoofFillColorButton;

	public SurfaceAndSolidStylingPanel(
			String i18nTitle,
			Styles styles,
			boolean showFootprintAndExtrudedOptions,
			boolean showGeometryOptions,
			boolean showColladaOptions,
			boolean showThematicSurfaceOptions,
			Config config) {
		super(config);
		this.i18nTitle = i18nTitle;
		this.styles = styles;
		this.showFootprintAndExtrudedOptions = showFootprintAndExtrudedOptions;
		this.showGeometryOptions = showGeometryOptions;
		this.showColladaOptions = showColladaOptions;
		this.showThematicSurfaceOptions = showThematicSurfaceOptions;

		for (DisplayFormType type : DisplayFormType.values()) {
			if (!styles.contains(type)) {
				styles.add(Style.of(type));
			}
		}

		initGui();
	}

	public SurfaceAndSolidStylingPanel(String i18nTitle, Styles styles, Config config) {
		this(i18nTitle, styles, true, true, true, false, config);
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString(i18nTitle);
	}

	@Override
	public boolean isModified() {
		try { geometryHLSurfaceDistanceText.commitEdit(); } catch (ParseException ignored) { }
		try { colladaHLSurfaceDistanceText.commitEdit(); } catch (ParseException ignored) { }

		setInternalStyles();
		for (DisplayFormType type : DisplayFormType.values()) {
			if (notEqual(styles.get(type), internalStyles.get(type))) {
				return true;
			}
		}

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		initFootprintPanel(Style.of(DisplayFormType.FOOTPRINT));
		initGeometryPanel(Style.of(DisplayFormType.GEOMETRY));
		initColladaPanel(Style.of(DisplayFormType.COLLADA));

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

	private void initFootprintPanel(Style style) {
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

		GridBagConstraints ffcl = GuiUtil.setConstraints(0, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		ffcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintFillColorLabel, ffcl);

		footprintFillColorButton.setBackground(new Color(style.getRgba0(), true));
		footprintFillColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintFillColorButton, GuiUtil.setConstraints(1, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 0));

		GridBagConstraints flcl = GuiUtil.setConstraints(2, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		flcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintLineColorLabel, flcl);

		footprintLineColorButton.setBackground(new Color(style.getRgba1(), true));
		footprintLineColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintLineColorButton, GuiUtil.setConstraints(3, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 5));

		GridBagConstraints fhlcb = GuiUtil.setConstraints(0, 2, 0.5, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 0);
		fhlcb.anchor = GridBagConstraints.WEST;
		fhlcb.gridwidth = 2;
		footprintContentPanel.add(footprintHighlightingCheckbox, fhlcb);

		GridBagConstraints fhlfcl = GuiUtil.setConstraints(0, 3, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		fhlfcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintHLFillColorLabel, fhlfcl);

		footprintHLFillColorButton.setBackground(new Color(style.getRgba4(), true));
		footprintHLFillColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintHLFillColorButton, GuiUtil.setConstraints(1, 3, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

		GridBagConstraints fhllcl = GuiUtil.setConstraints(2, 3, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		fhllcl.anchor = GridBagConstraints.EAST;
		footprintContentPanel.add(footprintHLLineColorLabel, fhllcl);

		footprintHLLineColorButton.setBackground(new Color(style.getRgba5(), true));
		footprintHLLineColorButton.setContentAreaFilled(false);
		footprintContentPanel.add(footprintHLLineColorButton, GuiUtil.setConstraints(3, 3, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 5));

		footprintFillColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"), footprintFillColorButton));
		footprintLineColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseLineColor"), footprintLineColorButton));
		footprintHLFillColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"), footprintHLFillColorButton));
		footprintHLLineColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"), footprintHLLineColorButton));
		footprintHighlightingCheckbox.addActionListener(e -> setEnabledFootprintHighlighting());
	}

	private void initGeometryPanel(Style style) {
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
		geometryFillColorButton.setBackground(new Color(style.getRgba0(), true));
		geometryFillColorButton.setContentAreaFilled(false);
		content.add(geometryFillColorButton, GuiUtil.setConstraints(1, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 2 * 5, 0));

		GridBagConstraints grcl = GuiUtil.setConstraints(2, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 2 * 5, 5);
		grcl.anchor = GridBagConstraints.EAST;
		content.add(geometryLineColorLabel, grcl);

		geometryLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryLineColorButton.setBackground(new Color(style.getRgba1(), true));
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
			geometryRoofFillColorButton.setBackground(new Color(style.getRgba2(), true));
			geometryRoofFillColorButton.setContentAreaFilled(false);
			content.add(geometryRoofFillColorButton, GuiUtil.setConstraints(1, 2, 0.25, 1.0, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

			GridBagConstraints grlcl = GuiUtil.setConstraints(2, 2, 0.25, 1.0, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
			grlcl.anchor = GridBagConstraints.EAST;
			content.add(geometryRoofLineColorLabel, grlcl);

			geometryRoofLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
			geometryRoofLineColorButton.setBackground(new Color(style.getRgba3(), true));
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
		geometryHLFillColorButton.setBackground(new Color(style.getRgba4(), true));
		geometryHLFillColorButton.setContentAreaFilled(false);
		content.add(geometryHLFillColorButton, GuiUtil.setConstraints(1, 4, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

		GridBagConstraints ghllcl = GuiUtil.setConstraints(2, 4, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		ghllcl.anchor = GridBagConstraints.EAST;
		content.add(geometryHLLineColorLabel, ghllcl);

		geometryHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		geometryHLLineColorButton.setBackground(new Color(style.getRgba5(), true));
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

		geometryFillColorButton.addActionListener(e -> chooseColor(
				Language.I18N.getString(showThematicSurfaceOptions ?
						"pref.kmlexport.label.chooseWallFillColor" :
						"pref.kmlexport.label.fillColor"),
				geometryFillColorButton));

		geometryLineColorButton.addActionListener(e -> chooseColor
				(Language.I18N.getString(showThematicSurfaceOptions ?
								"pref.kmlexport.label.chooseWallLineColor" :
								"pref.kmlexport.label.lineColor"),
						geometryLineColorButton));

		geometryHLFillColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"), geometryHLFillColorButton));
		geometryHLLineColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"), geometryHLLineColorButton));
		geometryHLSurfaceDistanceText.addPropertyChangeListener(evt -> checkHighlightingDistance(geometryHLSurfaceDistanceText));
		geometryHighlightingCheckbox.addActionListener(e -> setEnabledGeometryHighlighting());

		if (showThematicSurfaceOptions) {
			geometryRoofFillColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseRoofFillColor"), geometryRoofFillColorButton));
			geometryRoofLineColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseRoofLineColor"), geometryRoofLineColorButton));
		}
	}

	private void initColladaPanel(Style style) {
		colladaAlphaLabel = new JLabel();
		colladaFillColorLabel = new JLabel();
		colladaFillColorButton = new AlphaButton();
		colladaHighlightingRButton = new JRadioButton();
		colladaHLSurfaceDistanceLabel = new JLabel();
		colladaHLFillColorLabel = new JLabel();
		colladaHLFillColorButton = new AlphaButton();
		colladaHLLineColorLabel = new JLabel();
		colladaHLLineColorButton = new AlphaButton();

		DecimalFormat scaleFormat = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		scaleFormat.setMaximumIntegerDigits(1);
		scaleFormat.setMaximumFractionDigits(3);

		DecimalFormat highlightFormat = new DecimalFormat("##.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		highlightFormat.setMaximumIntegerDigits(2);
		highlightFormat.setMaximumFractionDigits(3);
		colladaHLSurfaceDistanceText = new JFormattedTextField(highlightFormat);

		DecimalFormat groupSizeFormat = new DecimalFormat("#");
		groupSizeFormat.setMaximumIntegerDigits(8);
		groupSizeFormat.setMinimumIntegerDigits(1);

		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());

		colladaColorSubPanel = new JPanel();
		colladaColorSubPanel.setLayout(new GridBagLayout());
		colladaColorSubPanel.setBorder(BorderFactory.createTitledBorder(""));
		GridBagConstraints cclsp = GuiUtil.setConstraints(0, 6, 0, 1, GridBagConstraints.BOTH, 5 * 2, 5, 2 * 5, 5);
		cclsp.gridwidth = 2;
		content.add(colladaColorSubPanel, cclsp);

		GridBagConstraints cwfcl = GuiUtil.setConstraints(0, 1, 0.25, 1, GridBagConstraints.NONE, 5, 5, 5, 5);
		cwfcl.anchor = GridBagConstraints.EAST;
		colladaColorSubPanel.add(colladaFillColorLabel, cwfcl);

		colladaFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaFillColorButton.setBackground(new Color(style.getRgba0(), true));
		colladaFillColorButton.setContentAreaFilled(false);
		colladaColorSubPanel.add(colladaFillColorButton, GuiUtil.setConstraints(1, 1, 0.25, 1, GridBagConstraints.HORIZONTAL, 5, 0, 5, 0));

		if (showThematicSurfaceOptions) {
			colladaRoofFillColorLabel = new JLabel();
			colladaRoofFillColorButton = new AlphaButton();

			GridBagConstraints crfcl = GuiUtil.setConstraints(0,2,0.25,1.0,GridBagConstraints.NONE,5,5,5,5);
			crfcl.anchor = GridBagConstraints.EAST;
			colladaColorSubPanel.add(colladaRoofFillColorLabel, crfcl);

			colladaRoofFillColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
			colladaRoofFillColorButton.setBackground(new Color(style.getRgba2(), true));
			colladaRoofFillColorButton.setContentAreaFilled(false);
			colladaColorSubPanel.add(colladaRoofFillColorButton, GuiUtil.setConstraints(1,2,0.25,1.0,GridBagConstraints.HORIZONTAL,5,0,5,5));
		}

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
		colladaHLFillColorButton.setBackground(new Color(style.getRgba4(), true));
		colladaHLFillColorButton.setContentAreaFilled(false);
		colladaHLSubPanel.add(colladaHLFillColorButton, GuiUtil.setConstraints(1, 0, 0.25, 1, GridBagConstraints.HORIZONTAL, 0, 0, 2 * 5, 0));

		GridBagConstraints chllcl = GuiUtil.setConstraints(2, 0, 0.25, 1, GridBagConstraints.NONE, 0, 5, 2 * 5, 5);
		chllcl.anchor = GridBagConstraints.EAST;
		colladaHLSubPanel.add(colladaHLLineColorLabel, chllcl);

		colladaHLLineColorButton.setPreferredSize(geometryAlphaSpinner.getPreferredSize());
		colladaHLLineColorButton.setBackground(new Color(style.getRgba5(), true));
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

		PopupMenuDecorator.getInstance().decorate(colladaHLSurfaceDistanceText);

		colladaFillColorButton.addActionListener(e -> chooseColor(
				Language.I18N.getString(showThematicSurfaceOptions ?
						"pref.kmlexport.label.chooseWallFillColor" :
						"pref.kmlexport.label.chooseFillColor"),
				colladaFillColorButton));

		colladaHLFillColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedFillColor"), colladaHLFillColorButton));
		colladaHLLineColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseHighlightedLineColor"), colladaHLLineColorButton));
		colladaHLSurfaceDistanceText.addPropertyChangeListener(evt -> checkHighlightingDistance(colladaHLSurfaceDistanceText));
		colladaHighlightingRButton.addActionListener(e -> setEnabledColladaHighlighting());

		if (showThematicSurfaceOptions) {
			colladaRoofFillColorButton.addActionListener(e -> chooseColor(Language.I18N.getString("pref.kmlexport.label.chooseRoofFillColor"), colladaRoofFillColorButton));
		}
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

		((TitledBorder)colladaColorSubPanel.getBorder()).setTitle(Language.I18N.getString("pref.kmlexport.label.colladaGltfColorSettings"));
		colladaAlphaLabel.setText(Language.I18N.getString("pref.kmlexport.label.alpha"));
		colladaFillColorLabel.setText(Language.I18N.getString(showThematicSurfaceOptions ?
				"pref.kmlexport.label.wallFillColor" :
				"pref.kmlexport.label.fillColor"));

		if (showThematicSurfaceOptions) {
			colladaRoofFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.roofFillColor"));
		}

		colladaHighlightingRButton.setText(Language.I18N.getString("pref.kmlexport.colladaDisplay.label.highlighting"));
		colladaHLSurfaceDistanceLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		colladaHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedFillColor"));
		colladaHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightedLineColor"));
	}

	@Override
	public void loadSettings() {
		for (DisplayFormType type : DisplayFormType.values()) {
			Style style = styles.getOrDefault(type);
			internalStyles.add(style.copy());

			switch (style.getType()) {
				case FOOTPRINT:
				case EXTRUDED:
					footprintHighlightingCheckbox.setSelected(style.isHighlightingEnabled());
					footprintFillColorButton.setBackground(new Color(style.getRgba0(), true));
					footprintLineColorButton.setBackground(new Color(style.getRgba1(), true));
					footprintHLFillColorButton.setBackground(new Color(style.getRgba4(), true));
					footprintHLLineColorButton.setBackground(new Color(style.getRgba5(), true));
					break;
				case GEOMETRY:
					geometryHighlightingCheckbox.setSelected(style.isHighlightingEnabled());
					geometryHLSurfaceDistanceText.setValue(style.getHighlightingDistance());
					geometryFillColorButton.setBackground(new Color(style.getRgba0(), true));
					geometryLineColorButton.setBackground(new Color(style.getRgba1(), true));
					geometryHLFillColorButton.setBackground(new Color(style.getRgba4(), true));
					geometryHLLineColorButton.setBackground(new Color(style.getRgba5(), true));

					if (showThematicSurfaceOptions) {
						geometryRoofFillColorButton.setBackground(new Color(style.getRgba2(), true));
						geometryRoofLineColorButton.setBackground(new Color(style.getRgba3(), true));
					}
					break;
				case COLLADA:
					colladaHighlightingRButton.setSelected(style.isHighlightingEnabled());
					colladaHLSurfaceDistanceText.setValue(style.getHighlightingDistance());
					colladaFillColorButton.setBackground(new Color(style.getRgba0(), true));
					colladaHLFillColorButton.setBackground(new Color(style.getRgba4(), true));
					colladaHLLineColorButton.setBackground(new Color(style.getRgba5(), true));

					if (showThematicSurfaceOptions) {
						colladaRoofFillColorButton.setBackground(new Color(style.getRgba2(), true));
					}
					break;
			}
		}

		setEnabledSettings();
	}

	@Override
	public void setSettings() {
		setInternalStyles();
		for (DisplayFormType type : DisplayFormType.values()) {
			copyColorAndHighlightingValues(internalStyles.get(type), styles.get(type));
		}
	}

	private void setInternalStyles() {
		for (DisplayFormType type : EnumSet.of(DisplayFormType.FOOTPRINT, DisplayFormType.EXTRUDED)) {
			Style style = internalStyles.getOrDefault(type);
			style.setHighlightingEnabled(footprintHighlightingCheckbox.isSelected());
			style.setRgba0(footprintFillColorButton.getBackground().getRGB());
			style.setRgba1(footprintLineColorButton.getBackground().getRGB());
			style.setRgba4(footprintHLFillColorButton.getBackground().getRGB());
			style.setRgba5(footprintHLLineColorButton.getBackground().getRGB());
			internalStyles.add(style);
		}
		{
			Style style = internalStyles.getOrDefault(DisplayFormType.GEOMETRY);
			style.setHighlightingEnabled(geometryHighlightingCheckbox.isSelected());
			style.setHighlightingDistance(((Number) geometryHLSurfaceDistanceText.getValue()).doubleValue());
			style.setRgba0(geometryFillColorButton.getBackground().getRGB());
			style.setRgba1(geometryLineColorButton.getBackground().getRGB());
			style.setRgba4(geometryHLFillColorButton.getBackground().getRGB());
			style.setRgba5(geometryHLLineColorButton.getBackground().getRGB());

			if (showThematicSurfaceOptions) {
				style.setRgba2(geometryRoofFillColorButton.getBackground().getRGB());
				style.setRgba3(geometryRoofLineColorButton.getBackground().getRGB());
			}

			internalStyles.add(style);
		}
		{
			Style style = internalStyles.getOrDefault(DisplayFormType.COLLADA);
			style.setHighlightingEnabled(colladaHighlightingRButton.isSelected());
			style.setHighlightingDistance(((Number) colladaHLSurfaceDistanceText.getValue()).doubleValue());
			style.setRgba0(colladaFillColorButton.getBackground().getRGB());
			style.setRgba4(colladaHLFillColorButton.getBackground().getRGB());
			style.setRgba5(colladaHLLineColorButton.getBackground().getRGB());

			if (showThematicSurfaceOptions) {
				style.setRgba2(colladaRoofFillColorButton.getBackground().getRGB());
			}

			internalStyles.add(style);
		}
	}

	@Override
	public void resetSettings() {
		Styles defaults = new Styles();
		for (DisplayFormType type : DisplayFormType.values()) {
			defaults.add(Style.of(type));
		}

		Styles tmp = styles;
		styles = defaults;
		loadSettings();
		styles = tmp;
	}

	private void setEnabledSettings() {
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
	}

	private boolean notEqual(Style first, Style second) {
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

	private void checkHighlightingDistance(JFormattedTextField textField) {
		if (textField.getValue() == null
				|| ((Number) textField.getValue()).doubleValue() <= 0
				|| ((Number) textField.getValue()).doubleValue() > 10)
			textField.setValue(1);
	}

	private void copyColorAndHighlightingValues(Style source, Style target) {
		target.setHighlightingDistance(source.getHighlightingDistance());
		target.setHighlightingEnabled(source.isHighlightingEnabled());
		target.setRgba0(source.getRgba0());
		target.setRgba1(source.getRgba1());
		target.setRgba2(source.getRgba2());
		target.setRgba3(source.getRgba3());
		target.setRgba4(source.getRgba4());
		target.setRgba5(source.getRgba5());
	}

	private void chooseColor(String title, JButton button) {
		Color color = JColorChooser.showDialog(getTopLevelAncestor(), title, button.getBackground());
		if (color != null) {
			button.setBackground(color);
		}
	}
}
