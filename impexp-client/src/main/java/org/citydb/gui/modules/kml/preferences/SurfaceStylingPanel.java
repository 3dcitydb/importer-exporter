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
import org.citydb.gui.components.common.ColorPicker;
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
import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Supplier;

public class SurfaceStylingPanel extends AbstractPreferencesComponent {
	private final String i18nTitle;
	private final boolean showFootprintAndExtrudedOptions;
	private final boolean showGeometryOptions;
	private final boolean showColladaOptions;
	private final boolean showThematicSurfaceOptions;
	private final Styles internalStyles = new Styles();
	private final Supplier<Styles> stylesSupplier;

	private TitledPanel footprintPanel;
	private TitledPanel geometryPanel;
	private TitledPanel colladaPanel;

	private int footprintContentRows;
	private JPanel footprintContentPanel;
	private JLabel footprintDefaultStyleLabel;
	private JCheckBox footprintHighlightingCheckbox;
	private JLabel footprintFillColorLabel;
	private ColorPicker footprintFillColorButton;
	private JLabel footprintLineColorLabel;
	private ColorPicker footprintLineColorButton;
	private JLabel footprintHLFillColorLabel;
	private ColorPicker footprintHLFillColorButton;
	private JLabel footprintHLLineColorLabel;
	private ColorPicker footprintHLLineColorButton;

	private int geometryContentRows;
	private JPanel geometryContentPanel;
	private JLabel geometryDefaultStyleLabel;
	private JLabel geometryRoofStyleLabel;
	private JLabel geometryFillColorLabel;
	private ColorPicker geometryFillColorButton;
	private JLabel geometryLineColorLabel;
	private ColorPicker geometryLineColorButton;
	private JCheckBox geometryHighlightingCheckbox;
	private JLabel geometryHLSurfaceDistanceLabel;
	private JFormattedTextField geometryHLSurfaceDistanceText;
	private JLabel geometryHLFillColorLabel;
	private ColorPicker geometryHLFillColorButton;
	private JLabel geometryHLLineColorLabel;
	private ColorPicker geometryHLLineColorButton;
	private JLabel geometryRoofFillColorLabel;
	private ColorPicker geometryRoofFillColorButton;
	private JLabel geometryRoofLineColorLabel;
	private ColorPicker geometryRoofLineColorButton;
	private JLabel geometryHLDistanceHint;

	private int colladaContentRows;
	private JPanel colladaContentPanel;
	private JLabel colladaDefaultStyleLabel;
	private JLabel colladaColorNote;
	private JLabel colladaRoofStyleLabel;
	private JLabel colladaFillColorLabel;
	private ColorPicker colladaFillColorButton;
	private JCheckBox colladaHighlightingCheckbox;
	private JLabel colladaHLSurfaceDistanceLabel;
	private JFormattedTextField colladaHLSurfaceDistanceText;
	private JLabel colladaHLFillColorLabel;
	private ColorPicker colladaHLFillColorButton;
	private JLabel colladaHLLineColorLabel;
	private ColorPicker colladaHLLineColorButton;
	private JLabel colladaRoofFillColorLabel;
	private ColorPicker colladaRoofFillColorButton;
	private JLabel colladaHLDistanceHint;

	public SurfaceStylingPanel(
			String i18nTitle,
			Supplier<Styles> stylesSupplier,
			boolean showFootprintAndExtrudedOptions,
			boolean showGeometryOptions,
			boolean showColladaOptions,
			boolean showThematicSurfaceOptions,
			Config config) {
		super(config);
		this.i18nTitle = i18nTitle;
		this.stylesSupplier = stylesSupplier;
		this.showFootprintAndExtrudedOptions = showFootprintAndExtrudedOptions;
		this.showGeometryOptions = showGeometryOptions;
		this.showColladaOptions = showColladaOptions;
		this.showThematicSurfaceOptions = showThematicSurfaceOptions;

		initGui();
	}

	public SurfaceStylingPanel(String i18nTitle, Supplier<Styles> stylesSupplier, Config config) {
		this(i18nTitle, stylesSupplier, true, true, true, false, config);
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString(i18nTitle);
	}

	@Override
	public boolean isModified() {
		try { geometryHLSurfaceDistanceText.commitEdit(); } catch (ParseException ignored) { }
		try { colladaHLSurfaceDistanceText.commitEdit(); } catch (ParseException ignored) { }

		Styles styles = stylesSupplier.get();
		setInternalStyles();

		for (DisplayFormType type : DisplayFormType.values()) {
			if (notEqual(styles.getOrSet(type), internalStyles.get(type))) {
				return true;
			}
		}

		return false;
	}

	private void initGui() {
		setLayout(new GridBagLayout());

		initFootprintPanel();
		initGeometryPanel();
		initColladaPanel();

		if (showFootprintAndExtrudedOptions) {
			add(footprintPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		if (showGeometryOptions) {
			add(geometryPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		if (showColladaOptions) {
			add(colladaPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}
	}

	private void initFootprintPanel() {
		footprintHighlightingCheckbox = new JCheckBox();
		footprintDefaultStyleLabel = new JLabel();
		footprintFillColorLabel = new JLabel();
		footprintFillColorButton = new ColorPicker();
		footprintLineColorLabel = new JLabel();
		footprintLineColorButton = new ColorPicker();
		footprintHLFillColorLabel = new JLabel();
		footprintHLFillColorButton = new ColorPicker();
		footprintHLLineColorLabel = new JLabel();
		footprintHLLineColorButton = new ColorPicker();

		footprintContentPanel = new JPanel();
		footprintContentPanel.setLayout(new GridBagLayout());
		{
			JPanel defaultStyle = createStylePanel(footprintFillColorLabel, footprintFillColorButton, footprintLineColorLabel, footprintLineColorButton);
			JPanel highlightStyle = createStylePanel(footprintHLFillColorLabel, footprintHLFillColorButton, footprintHLLineColorLabel, footprintHLLineColorButton);

			footprintContentPanel.add(footprintDefaultStyleLabel, GuiUtil.setConstraints(0, footprintContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
			footprintContentPanel.add(defaultStyle, GuiUtil.setConstraints(1, footprintContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0));
			footprintContentPanel.add(footprintHighlightingCheckbox, GuiUtil.setConstraints(0, footprintContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
			footprintContentPanel.add(highlightStyle, GuiUtil.setConstraints(1, footprintContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
		}

		footprintPanel = new TitledPanel().build(footprintContentPanel);
		footprintHighlightingCheckbox.addActionListener(e -> setEnabledFootprintHighlighting());
	}

	private void initGeometryPanel() {
		geometryDefaultStyleLabel = new JLabel();
		geometryFillColorLabel = new JLabel();
		geometryFillColorButton = new ColorPicker();
		geometryLineColorLabel = new JLabel();
		geometryLineColorButton = new ColorPicker();
		geometryHighlightingCheckbox = new JCheckBox();
		geometryHLSurfaceDistanceLabel = new JLabel();
		geometryHLFillColorLabel = new JLabel();
		geometryHLFillColorButton = new ColorPicker();
		geometryHLLineColorLabel = new JLabel();
		geometryHLLineColorButton = new ColorPicker();
		geometryHLDistanceHint = new JLabel("(0-10m)");

		NumberFormatter format = new NumberFormatter(new DecimalFormat("#.###",
				DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
		format.setMaximum(10.0);
		format.setMinimum(0.0);
		geometryHLSurfaceDistanceText = new JFormattedTextField(format);
		geometryHLSurfaceDistanceText.setColumns(5);

		geometryContentPanel = new JPanel();
		geometryContentPanel = new JPanel();
		geometryContentPanel.setLayout(new GridBagLayout());
		{
			JPanel defaultStyle = createStylePanel(geometryFillColorLabel, geometryFillColorButton, geometryLineColorLabel, geometryLineColorButton);
			JPanel highlightStyle = createStylePanel(geometryHLFillColorLabel, geometryHLFillColorButton, geometryHLLineColorLabel, geometryHLLineColorButton);

			Box box = Box.createHorizontalBox();
			box.add(geometryHLSurfaceDistanceText);
			box.add(Box.createHorizontalStrut(5));
			box.add(geometryHLDistanceHint);

			int lmargin = GuiUtil.getTextOffset(geometryHighlightingCheckbox);

			geometryContentPanel.add(geometryDefaultStyleLabel, GuiUtil.setConstraints(0, geometryContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
			geometryContentPanel.add(defaultStyle, GuiUtil.setConstraints(1, geometryContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0));

			if (showThematicSurfaceOptions) {
				geometryRoofStyleLabel = new JLabel();
				geometryRoofFillColorLabel = new JLabel();
				geometryRoofFillColorButton = new ColorPicker();
				geometryRoofLineColorLabel = new JLabel();
				geometryRoofLineColorButton = new ColorPicker();

				JPanel roofStyle = createStylePanel(geometryRoofFillColorLabel, geometryRoofFillColorButton, geometryRoofLineColorLabel, geometryRoofLineColorButton);
				geometryContentPanel.add(geometryRoofStyleLabel, GuiUtil.setConstraints(0, geometryContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				geometryContentPanel.add(roofStyle, GuiUtil.setConstraints(1, geometryContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			}

			geometryContentPanel.add(geometryHighlightingCheckbox, GuiUtil.setConstraints(0, geometryContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
			geometryContentPanel.add(highlightStyle, GuiUtil.setConstraints(1, geometryContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			geometryContentPanel.add(geometryHLSurfaceDistanceLabel, GuiUtil.setConstraints(0, geometryContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, lmargin, 0, 10));
			geometryContentPanel.add(box, GuiUtil.setConstraints(1, geometryContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));

			geometryPanel = new TitledPanel().build(geometryContentPanel);
		}

		PopupMenuDecorator.getInstance().decorate(geometryHLSurfaceDistanceText);

		geometryHighlightingCheckbox.addActionListener(e -> setEnabledGeometryHighlighting());
	}

	private void initColladaPanel() {
		colladaDefaultStyleLabel = new JLabel();
		colladaColorNote = new JLabel();
		colladaFillColorLabel = new JLabel();
		colladaFillColorButton = new ColorPicker();
		colladaHighlightingCheckbox = new JCheckBox();
		colladaHLSurfaceDistanceLabel = new JLabel();
		colladaHLFillColorLabel = new JLabel();
		colladaHLFillColorButton = new ColorPicker();
		colladaHLLineColorLabel = new JLabel();
		colladaHLLineColorButton = new ColorPicker();
		colladaHLDistanceHint = new JLabel("(0-10m)");

		NumberFormatter format = new NumberFormatter(new DecimalFormat("#.###",
				DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
		format.setMaximum(10.0);
		format.setMinimum(0.0);
		colladaHLSurfaceDistanceText = new JFormattedTextField(format);
		colladaHLSurfaceDistanceText.setColumns(5);

		colladaContentPanel = new JPanel();
		colladaContentPanel.setLayout(new GridBagLayout());
		{
			JPanel defaultStyle = createStylePanel(colladaFillColorLabel, colladaFillColorButton);
			JPanel highlightStyle = createStylePanel(colladaHLFillColorLabel, colladaHLFillColorButton, colladaHLLineColorLabel, colladaHLLineColorButton);

			Box box = Box.createHorizontalBox();
			box.add(colladaHLSurfaceDistanceText);
			box.add(Box.createHorizontalStrut(5));
			box.add(colladaHLDistanceHint);

			int lmargin = GuiUtil.getTextOffset(geometryHighlightingCheckbox);

			colladaColorNote.setFont(colladaColorNote.getFont().deriveFont(Font.ITALIC));
			colladaContentPanel.add(colladaColorNote, GuiUtil.setConstraints(0, colladaContentRows++, 2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
			colladaContentPanel.add(colladaDefaultStyleLabel, GuiUtil.setConstraints(0, colladaContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 10, 0, 0, 10));
			colladaContentPanel.add(defaultStyle, GuiUtil.setConstraints(1, colladaContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 10, 0, 0, 0));

			if (showThematicSurfaceOptions) {
				colladaRoofStyleLabel = new JLabel();
				colladaRoofFillColorLabel = new JLabel();
				colladaRoofFillColorButton = new ColorPicker();

				JPanel roofStyle = createStylePanel(colladaRoofFillColorLabel, colladaRoofFillColorButton);
				colladaContentPanel.add(colladaRoofStyleLabel, GuiUtil.setConstraints(0, colladaContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				colladaContentPanel.add(roofStyle, GuiUtil.setConstraints(1, colladaContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			}

			colladaContentPanel.add(colladaHighlightingCheckbox, GuiUtil.setConstraints(0, colladaContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
			colladaContentPanel.add(highlightStyle, GuiUtil.setConstraints(1, colladaContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			colladaContentPanel.add(colladaHLSurfaceDistanceLabel, GuiUtil.setConstraints(0, colladaContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, lmargin, 0, 10));
			colladaContentPanel.add(box, GuiUtil.setConstraints(1, colladaContentRows++, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));

			colladaPanel = new TitledPanel().build(colladaContentPanel);
		}

		PopupMenuDecorator.getInstance().decorate(colladaHLSurfaceDistanceText);

		colladaHighlightingCheckbox.addActionListener(e -> setEnabledColladaHighlighting());
	}

	private JPanel createStylePanel(JLabel fillColorLabel, ColorPicker fillColor, JLabel outlineColorLabel, ColorPicker outlineColor) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		{
			panel.add(fillColor, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
			panel.add(fillColorLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			if (outlineColorLabel != null && outlineColor != null) {
				panel.add(outlineColor, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 20, 0, 0));
				panel.add(outlineColorLabel, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}
		}

		return panel;
	}

	private JPanel createStylePanel(JLabel fillColorLabel, ColorPicker fillColor) {
		return createStylePanel(fillColorLabel, fillColor, null, null);
	}

	public void addFootprintAndExtrudedOptions(JLabel label, JComponent component) {
		if (showFootprintAndExtrudedOptions) {
			footprintContentPanel.add(label, GuiUtil.setConstraints(0, footprintContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
			footprintContentPanel.add(component, GuiUtil.setConstraints(1, footprintContentRows++, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
		}
	}

	public void addGeometryOptions(JLabel label, JComponent component) {
		if (showGeometryOptions) {
			geometryContentPanel.add(label, GuiUtil.setConstraints(0, geometryContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
			geometryContentPanel.add(component, GuiUtil.setConstraints(1, geometryContentRows++, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
		}
	}

	public void addColladaOptions(JLabel label, JComponent component) {
		if (showColladaOptions) {
			colladaContentPanel.add(label, GuiUtil.setConstraints(0, colladaContentRows, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
			colladaContentPanel.add(component, GuiUtil.setConstraints(1, colladaContentRows++, 1, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
		}
	}

	@Override
	public void doTranslation() {
		footprintPanel.setTitle(Language.I18N.getString("pref.kmlexport.border.footprint"));
		geometryPanel.setTitle(Language.I18N.getString("pref.kmlexport.border.geometry"));
		colladaPanel.setTitle(Language.I18N.getString("pref.kmlexport.border.collada"));

		footprintDefaultStyleLabel.setText(Language.I18N.getString("pref.kmlexport.label.defaultStyle"));
		footprintFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		footprintLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.outlineColor"));
		footprintHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlightStyle"));
		footprintHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		footprintHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.outlineColor"));

		footprintFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		footprintLineColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseOutlineColor"));
		footprintHLFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		footprintHLLineColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseOutlineColor"));

		geometryDefaultStyleLabel.setText(Language.I18N.getString("pref.kmlexport.label.defaultStyle"));
		geometryFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		geometryLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.outlineColor"));
		geometryFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		geometryLineColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseOutlineColor"));

		if (showThematicSurfaceOptions) {
			geometryRoofStyleLabel.setText(Language.I18N.getString("pref.kmlexport.label.roofStyle"));
			geometryRoofFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
			geometryRoofLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.outlineColor"));
			geometryRoofFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
			geometryRoofLineColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.outlineColor"));
		}

		geometryHLFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		geometryHLLineColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseOutlineColor"));
		geometryHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.highlightStyle"));
		geometryHLSurfaceDistanceLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		geometryHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		geometryHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.outlineColor"));

		colladaColorNote.setText(Language.I18N.getString("pref.kmlexport.label.colladaGltfColorSettings"));
		colladaDefaultStyleLabel.setText(Language.I18N.getString("pref.kmlexport.label.defaultStyle"));
		colladaFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		colladaFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		colladaHLFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		colladaHLLineColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseOutlineColor"));

		if (showThematicSurfaceOptions) {
			colladaRoofStyleLabel.setText(Language.I18N.getString("pref.kmlexport.label.roofStyle"));
			colladaRoofFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
			colladaRoofFillColorButton.setDialogTitle(Language.I18N.getString("pref.kmlexport.label.chooseFillColor"));
		}

		colladaHighlightingCheckbox.setText(Language.I18N.getString("pref.kmlexport.label.colladaHighlightStyle"));
		colladaHLSurfaceDistanceLabel.setText(Language.I18N.getString("pref.kmlexport.label.highlightingDistance"));
		colladaHLFillColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.fillColor"));
		colladaHLLineColorLabel.setText(Language.I18N.getString("pref.kmlexport.label.outlineColor"));
	}

	@Override
	public void loadSettings() {
		Styles styles = stylesSupplier.get();

		for (DisplayFormType type : DisplayFormType.values()) {
			Style style = styles.getOrDefault(type);
			internalStyles.add(style.copy());

			switch (style.getType()) {
				case FOOTPRINT:
				case EXTRUDED:
					footprintHighlightingCheckbox.setSelected(style.isHighlightingEnabled());
					footprintFillColorButton.setColor(new Color(style.getRgba0(), true));
					footprintLineColorButton.setColor(new Color(style.getRgba1(), true));
					footprintHLFillColorButton.setColor(new Color(style.getRgba4(), true));
					footprintHLLineColorButton.setColor(new Color(style.getRgba5(), true));
					break;
				case GEOMETRY:
					geometryHighlightingCheckbox.setSelected(style.isHighlightingEnabled());
					geometryHLSurfaceDistanceText.setValue(style.getHighlightingDistance());
					geometryFillColorButton.setColor(new Color(style.getRgba0(), true));
					geometryLineColorButton.setColor(new Color(style.getRgba1(), true));
					geometryHLFillColorButton.setColor(new Color(style.getRgba4(), true));
					geometryHLLineColorButton.setColor(new Color(style.getRgba5(), true));

					if (showThematicSurfaceOptions) {
						geometryRoofFillColorButton.setColor(new Color(style.getRgba2(), true));
						geometryRoofLineColorButton.setColor(new Color(style.getRgba3(), true));
					}
					break;
				case COLLADA:
					colladaHighlightingCheckbox.setSelected(style.isHighlightingEnabled());
					colladaHLSurfaceDistanceText.setValue(style.getHighlightingDistance());
					colladaFillColorButton.setColor(new Color(style.getRgba0(), true));
					colladaHLFillColorButton.setColor(new Color(style.getRgba4(), true));
					colladaHLLineColorButton.setColor(new Color(style.getRgba5(), true));

					if (showThematicSurfaceOptions) {
						colladaRoofFillColorButton.setBackground(new Color(style.getRgba2(), true));
					}
					break;
			}
		}

		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		Styles styles = stylesSupplier.get();
		setInternalStyles();

		for (DisplayFormType type : DisplayFormType.values()) {
			copyColorAndHighlightingValues(internalStyles.get(type), styles.getOrSet(type));
		}
	}

	private void setInternalStyles() {
		for (DisplayFormType type : EnumSet.of(DisplayFormType.FOOTPRINT, DisplayFormType.EXTRUDED)) {
			Style style = internalStyles.getOrSet(type);
			style.setHighlightingEnabled(footprintHighlightingCheckbox.isSelected());
			style.setRgba0(footprintFillColorButton.getColor().getRGB());
			style.setRgba1(footprintLineColorButton.getColor().getRGB());
			style.setRgba4(footprintHLFillColorButton.getColor().getRGB());
			style.setRgba5(footprintHLLineColorButton.getColor().getRGB());
		}
		{
			Style style = internalStyles.getOrSet(DisplayFormType.GEOMETRY);
			style.setHighlightingEnabled(geometryHighlightingCheckbox.isSelected());
			style.setHighlightingDistance(((Number) geometryHLSurfaceDistanceText.getValue()).doubleValue());
			style.setRgba0(geometryFillColorButton.getColor().getRGB());
			style.setRgba1(geometryLineColorButton.getColor().getRGB());
			style.setRgba4(geometryHLFillColorButton.getColor().getRGB());
			style.setRgba5(geometryHLLineColorButton.getColor().getRGB());

			if (showThematicSurfaceOptions) {
				style.setRgba2(geometryRoofFillColorButton.getColor().getRGB());
				style.setRgba3(geometryRoofLineColorButton.getColor().getRGB());
			}
		}
		{
			Style style = internalStyles.getOrSet(DisplayFormType.COLLADA);
			style.setHighlightingEnabled(colladaHighlightingCheckbox.isSelected());
			style.setHighlightingDistance(((Number) colladaHLSurfaceDistanceText.getValue()).doubleValue());
			style.setRgba0(colladaFillColorButton.getColor().getRGB());
			style.setRgba4(colladaHLFillColorButton.getColor().getRGB());
			style.setRgba5(colladaHLLineColorButton.getColor().getRGB());

			if (showThematicSurfaceOptions) {
				style.setRgba2(colladaRoofFillColorButton.getColor().getRGB());
			}
		}
	}

	private void setEnabledComponents() {
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
		geometryHLDistanceHint.setEnabled(geometryHighlightingCheckbox.isSelected());
	}

	private void setEnabledColladaHighlighting() {
		colladaHLFillColorLabel.setEnabled(colladaHighlightingCheckbox.isSelected());
		colladaHLFillColorButton.setEnabled(colladaHighlightingCheckbox.isSelected());
		colladaHLLineColorLabel.setEnabled(colladaHighlightingCheckbox.isSelected());
		colladaHLLineColorButton.setEnabled(colladaHighlightingCheckbox.isSelected());
		colladaHLSurfaceDistanceLabel.setEnabled(colladaHighlightingCheckbox.isSelected());
		colladaHLSurfaceDistanceText.setEnabled(colladaHighlightingCheckbox.isSelected());
		colladaHLDistanceHint.setEnabled(colladaHighlightingCheckbox.isSelected());
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
}
