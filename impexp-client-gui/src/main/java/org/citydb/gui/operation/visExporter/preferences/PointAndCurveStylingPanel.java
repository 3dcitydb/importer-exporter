/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.visExporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.visExporter.AltitudeMode;
import org.citydb.config.project.visExporter.PointAndCurve;
import org.citydb.config.project.visExporter.PointDisplayMode;
import org.citydb.gui.components.common.ColorPicker;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.operation.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Supplier;

public class PointAndCurveStylingPanel extends AbstractPreferencesComponent {
	private final Supplier<PointAndCurve> pointAndCurveSupplier;

	private TitledPanel pointPanel;
	private TitledPanel crossLinePanel;
	private TitledPanel iconPanel;
	private TitledPanel cubePanel;
	private TitledPanel curvePanel;

	private JRadioButton iconRButton;
	private JRadioButton crossLineRButton;
	private JRadioButton cubeRButton;

	private JLabel pointCrossLineDefaultStyle;
	private JLabel pointAltitudeModeLabel;
	private JComboBox<AltitudeMode> pointAltitudeModeComboBox;

	private JLabel pointCrossLineThicknessLabel;
	private JSpinner pointCrossLineThicknessSpinner;
	private JLabel pointCrossLineNormalColorLabel;
	private ColorPicker pointCrossLineNormalColorButton;
	private JCheckBox pointCrossLineHighlightingCheckbox;
	private JLabel pointCrossLineHighlightingThicknessLabel;
	private JSpinner pointCrossLineHighlightingThicknessSpinner;
	private JLabel pointCrossLineHighlightingColorLabel;
	private ColorPicker pointCrossLineHighlightingColorButton;

	private JLabel pointIconDefaultStyle;
	private JLabel pointIconColorLabel;
	private ColorPicker pointIconColorButton;
	private JLabel pointIconScaleLabel;
	private JSpinner pointIconScaleSpinner;
	private JCheckBox pointIconHighlightingCheckbox;
	private JLabel pointIconHighlightingColorLabel;
	private ColorPicker pointIconHighlightingColorButton;
	private JLabel pointIconHighlightingScaleLabel;
	private JSpinner pointIconHighlightingScaleSpinner;

	private JLabel pointCubeDefaultStyle;
	private JLabel pointCubeLengthOfSideLabel;
	private JSpinner pointCubeLengthOfSideSpinner;
	private JLabel pointCubeFillColorLabel;
	private ColorPicker pointCubeFillColorButton;
	private JCheckBox pointCubeHighlightingCheckbox;
	private JLabel pointCubeHighlightingColorLabel;
	private ColorPicker pointCubeHighlightingColorButton;
	private JLabel pointCubeHighlightingLineThicknessLabel;
	private JSpinner pointCubeHighlightingLineThicknessSpinner;

	private JLabel curveDefaultStyle;
	private JLabel curveAltitudeModeLabel;
	private JComboBox<AltitudeMode> curveAltitudeModeComboBox;
	private JLabel curveThicknessLabel;
	private JSpinner curveThicknessSpinner;
	private JLabel curveNormalColorLabel;
	private ColorPicker curveNormalColorButton;
	private JCheckBox curveHighlightingCheckbox;
	private JLabel curveHighlightingThicknessLabel;
	private JSpinner curveHighlightingThicknessSpinner;
	private JLabel curveHighlightingColorLabel;
	private ColorPicker curveHighlightingColorButton;

	public PointAndCurveStylingPanel(Supplier<PointAndCurve> pointAndCurveSupplier, Config config) {
		super(config);
		this.pointAndCurveSupplier = pointAndCurveSupplier;

		initGui();
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.visExport.pointAndCurve.styling");
	}

	@Override
	public boolean isModified() {
		PointAndCurve pacSettings = pointAndCurveSupplier.get();

		switch (pacSettings.getPointDisplayMode()) {
			case CROSS_LINE:
				if (!crossLineRButton.isSelected())
					return true;
				break;
			case ICON:
				if (!iconRButton.isSelected())
					return true;
				break;
			case CUBE:
				if (!cubeRButton.isSelected())
					return true;
				break;
		}

		if (!pacSettings.getPointAltitudeMode().equals(pointAltitudeModeComboBox.getSelectedItem())) return true;
		if (pacSettings.getPointThickness() != (Double) pointCrossLineThicknessSpinner.getValue()) return true;
		if (pacSettings.getPointNormalColor() != pointCrossLineNormalColorButton.getBackground().getRGB()) return true;
		if (pacSettings.isPointHighlightingEnabled() != pointCrossLineHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getPointHighlightedThickness() != (Double) pointCrossLineHighlightingThicknessSpinner.getValue()) return true;
		if (pacSettings.getPointHighlightedColor() != pointCrossLineHighlightingColorButton.getBackground().getRGB()) return true;
		if (pacSettings.getPointIconColor() != pointIconColorButton.getBackground().getRGB()) return true;
		if (pacSettings.getPointIconScale() != (Double) pointIconScaleSpinner.getValue()) return true;
		if (pacSettings.isPointIconHighlightingEnabled() != pointIconHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getPointIconHighlightedColor() != pointIconHighlightingColorButton.getBackground().getRGB()) return true;
		if (pacSettings.getPointIconHighlightedScale() != (Double) pointIconHighlightingScaleSpinner.getValue()) return true;
		if (pacSettings.getPointCubeLengthOfSide() != (Double) pointCubeLengthOfSideSpinner.getValue()) return true;
		if (pacSettings.getPointCubeFillColor() != pointCubeFillColorButton.getBackground().getRGB()) return true;
		if (pacSettings.isPointCubeHighlightingEnabled() != pointCubeHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getPointCubeHighlightedColor() != pointCubeHighlightingColorButton.getBackground().getRGB()) return true;
		if (pacSettings.getPointCubeHighlightedOutlineThickness() != (Double) pointCubeHighlightingLineThicknessSpinner.getValue()) return true;
		if (!pacSettings.getCurveAltitudeMode().equals(curveAltitudeModeComboBox.getSelectedItem())) return true;
		if (pacSettings.getCurveThickness() != (Double) curveThicknessSpinner.getValue()) return true;
		if (pacSettings.getCurveNormalColor() != curveNormalColorButton.getBackground().getRGB()) return true;
		if (pacSettings.isCurveHighlightingEnabled() != curveHighlightingCheckbox.isSelected()) return true;
		if (pacSettings.getCurveHighlightedThickness() != (Double) curveHighlightingThicknessSpinner.getValue()) return true;
		if (pacSettings.getCurveHighlightedColor() != curveHighlightingColorButton.getBackground().getRGB()) return true;
		return false;
	}

	private void initGui() {
		crossLineRButton = new JRadioButton();
		iconRButton = new JRadioButton();
		cubeRButton = new JRadioButton();

		pointAltitudeModeLabel = new JLabel();
		pointAltitudeModeComboBox = new JComboBox<>();
		pointCrossLineDefaultStyle = new JLabel();
		pointCrossLineThicknessLabel = new JLabel();
		pointCrossLineNormalColorLabel = new JLabel();
		pointCrossLineNormalColorButton = new ColorPicker();
		pointCrossLineHighlightingCheckbox = new JCheckBox();
		pointCrossLineHighlightingThicknessLabel = new JLabel();
		pointCrossLineHighlightingColorLabel = new JLabel();
		pointCrossLineHighlightingColorButton = new ColorPicker();

		pointIconDefaultStyle = new JLabel();
		pointIconColorLabel = new JLabel();
		pointIconColorButton = new ColorPicker();
		pointIconScaleLabel = new JLabel();
		pointIconHighlightingCheckbox = new JCheckBox();
		pointIconHighlightingColorLabel = new JLabel();
		pointIconHighlightingColorButton = new ColorPicker();
		pointIconHighlightingScaleLabel = new JLabel();

		pointCubeDefaultStyle = new JLabel();
		pointCubeLengthOfSideLabel = new JLabel();
		pointCubeFillColorLabel = new JLabel();
		pointCubeFillColorButton = new ColorPicker();
		pointCubeHighlightingCheckbox = new JCheckBox();
		pointCubeHighlightingColorLabel = new JLabel();
		pointCubeHighlightingColorButton = new ColorPicker();
		pointCubeHighlightingLineThicknessLabel = new JLabel();

		curveDefaultStyle = new JLabel();
		curveAltitudeModeLabel = new JLabel();
		curveAltitudeModeComboBox = new JComboBox<>();
		curveThicknessLabel = new JLabel();
		curveNormalColorLabel = new JLabel();
		curveNormalColorButton = new ColorPicker();
		curveHighlightingCheckbox = new JCheckBox();
		curveHighlightingThicknessLabel = new JLabel();
		curveHighlightingColorLabel = new JLabel();
		curveHighlightingColorButton = new ColorPicker();

		ButtonGroup pointRadioGroup = new ButtonGroup();
		pointRadioGroup.add(crossLineRButton);
		pointRadioGroup.add(iconRButton);
		pointRadioGroup.add(cubeRButton);

		SpinnerModel pointThicknessModel = new SpinnerNumberModel(1, 0.1, 10, 0.1);
		pointCrossLineThicknessSpinner = new JSpinner(pointThicknessModel);
		setSpinnerFormat(pointCrossLineThicknessSpinner, "#.#");

		SpinnerModel pointHighlightingThicknessModel = new SpinnerNumberModel(2, 0.1, 10, 0.1);
		pointCrossLineHighlightingThicknessSpinner = new JSpinner(pointHighlightingThicknessModel);
		setSpinnerFormat(pointCrossLineHighlightingThicknessSpinner, "#.#");

		SpinnerModel pointIconScaleModel = new SpinnerNumberModel(1, 0.1, 10, 0.1);
		pointIconScaleSpinner = new JSpinner(pointIconScaleModel);
		setSpinnerFormat(pointIconScaleSpinner, "#.#");

		SpinnerModel pointIconHighlightingScaleModel = new SpinnerNumberModel(1, 0.1, 10, 0.1);
		pointIconHighlightingScaleSpinner = new JSpinner(pointIconHighlightingScaleModel);
		setSpinnerFormat(pointIconHighlightingScaleSpinner, "#.#");

		SpinnerModel pointCubeLengthOfSideModel = new SpinnerNumberModel(1, 0.1, 10, 0.1);
		pointCubeLengthOfSideSpinner = new JSpinner(pointCubeLengthOfSideModel);
		setSpinnerFormat(pointCubeLengthOfSideSpinner, "#.#");

		SpinnerModel pointCubeHighlightingThicknessModel = new SpinnerNumberModel(2, 0.1, 10, 0.1);
		pointCubeHighlightingLineThicknessSpinner = new JSpinner(pointCubeHighlightingThicknessModel);
		setSpinnerFormat(pointCubeHighlightingLineThicknessSpinner, "#.#");

		SpinnerModel curveThicknessModel = new SpinnerNumberModel(1, 0.1, 10, 0.1);
		curveThicknessSpinner = new JSpinner(curveThicknessModel);
		setSpinnerFormat(curveThicknessSpinner, "#.#");

		SpinnerModel curveHighlightingThicknessModel = new SpinnerNumberModel(2, 0.1, 10, 0.1);
		curveHighlightingThicknessSpinner = new JSpinner(curveHighlightingThicknessModel);
		setSpinnerFormat(curveHighlightingThicknessSpinner, "#.#");

		setLayout(new GridBagLayout());
		{
			JPanel crossLineContent = new JPanel();
			crossLineContent.setLayout(new GridBagLayout());
			{
				JPanel defaultStyle = createStylePanel(pointCrossLineNormalColorLabel, pointCrossLineNormalColorButton,
						pointCrossLineThicknessLabel, pointCrossLineThicknessSpinner);
				JPanel highlightStyle = createStylePanel(pointCrossLineHighlightingColorLabel, pointCrossLineHighlightingColorButton,
						pointCrossLineHighlightingThicknessLabel, pointCrossLineHighlightingThicknessSpinner);

				crossLineContent.add(pointCrossLineDefaultStyle, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
				crossLineContent.add(defaultStyle, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0));
				crossLineContent.add(pointCrossLineHighlightingCheckbox, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				crossLineContent.add(highlightStyle, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			}

			crossLinePanel = new TitledPanel()
					.withToggleButton(crossLineRButton)
					.showSeparator(false)
					.withMargin(new Insets(0, 0, 0, 0))
					.build(crossLineContent);

			JPanel iconContent = new JPanel();
			iconContent.setLayout(new GridBagLayout());
			{
				JPanel defaultStyle = createStylePanel(pointIconColorLabel, pointIconColorButton,
						pointIconScaleLabel, pointIconScaleSpinner);
				JPanel highlightStyle = createStylePanel(pointIconHighlightingColorLabel, pointIconHighlightingColorButton,
						pointIconHighlightingScaleLabel, pointIconHighlightingScaleSpinner);

				iconContent.add(pointIconDefaultStyle, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
				iconContent.add(defaultStyle, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0));
				iconContent.add(pointIconHighlightingCheckbox, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				iconContent.add(highlightStyle, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			}

			iconPanel = new TitledPanel()
					.withToggleButton(iconRButton)
					.showSeparator(false)
					.withMargin(new Insets(0, 0, 0, 0))
					.build(iconContent);

			JPanel cubeContent = new JPanel();
			cubeContent.setLayout(new GridBagLayout());
			{
				JPanel defaultStyle = createStylePanel(pointCubeFillColorLabel, pointCubeFillColorButton,
						pointCubeLengthOfSideLabel, pointCubeLengthOfSideSpinner);
				JPanel highlightStyle = createStylePanel(pointCubeHighlightingColorLabel, pointCubeHighlightingColorButton,
						pointCubeHighlightingLineThicknessLabel, pointCubeHighlightingLineThicknessSpinner);

				cubeContent.add(pointCubeDefaultStyle, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
				cubeContent.add(defaultStyle, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, 0, 0));
				cubeContent.add(pointCubeHighlightingCheckbox, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				cubeContent.add(highlightStyle, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			}

			cubePanel = new TitledPanel()
					.withToggleButton(cubeRButton)
					.showSeparator(false)
					.withMargin(new Insets(0, 0, 0, 0))
					.build(cubeContent);

			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			content.add(pointAltitudeModeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
			content.add(pointAltitudeModeComboBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			content.add(crossLinePanel, GuiUtil.setConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(iconPanel, GuiUtil.setConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(cubePanel, GuiUtil.setConstraints(0, 3, 2, 1, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));

			pointPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				JPanel defaultStyle = createStylePanel(curveNormalColorLabel, curveNormalColorButton,
						curveThicknessLabel, curveThicknessSpinner);
				JPanel highlightStyle = createStylePanel(curveHighlightingColorLabel, curveHighlightingColorButton,
						curveHighlightingThicknessLabel, curveHighlightingThicknessSpinner);

				content.add(curveAltitudeModeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 10));
				content.add(curveAltitudeModeComboBox, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
				content.add(curveDefaultStyle, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				content.add(defaultStyle, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
				content.add(curveHighlightingCheckbox, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 10));
				content.add(highlightStyle, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, 5, 0, 0, 0));
			}

			curvePanel = new TitledPanel().build(content);
		}

		add(pointPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(curvePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		PopupMenuDecorator.getInstance().decorate(((JSpinner.DefaultEditor) pointCrossLineThicknessSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) pointCrossLineHighlightingThicknessSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) pointIconScaleSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) pointIconHighlightingScaleSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) pointCubeLengthOfSideSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) pointCubeHighlightingLineThicknessSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) curveThicknessSpinner.getEditor()).getTextField(),
				((JSpinner.DefaultEditor) curveHighlightingThicknessSpinner.getEditor()).getTextField());

		iconRButton.addActionListener(e -> setEnabledPointComponents());
        crossLineRButton.addActionListener(e -> setEnabledPointComponents());
        cubeRButton.addActionListener(e -> setEnabledPointComponents());
		pointCrossLineHighlightingCheckbox.addActionListener(e -> setEnabledPointComponents());
		pointIconHighlightingCheckbox.addActionListener(e -> setEnabledPointComponents());
		pointCubeHighlightingCheckbox.addActionListener(e -> setEnabledPointComponents());
		curveHighlightingCheckbox.addActionListener(e -> setEnabledCurveComponents());
	}

	private JPanel createStylePanel(JLabel colorLabel, ColorPicker color, JLabel strokeLabel, JSpinner stroke) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		{
			panel.add(color, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 0, 0, 0, 0));
			panel.add(colorLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			if (strokeLabel != null && stroke != null) {
				panel.add(stroke, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.NONE, 0, 20, 0, 0));
				panel.add(strokeLabel, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}
		}

		return panel;
	}

	private JPanel createStylePanel(JLabel fillColorLabel, ColorPicker fillColor) {
		return createStylePanel(fillColorLabel, fillColor, null, null);
	}

	@Override
	public void doTranslation() {
		PointAndCurve pacSettings = pointAndCurveSupplier.get();

		pointPanel.setTitle(Language.I18N.getString("pref.visExport.border.point"));
		pointAltitudeModeLabel.setText(Language.I18N.getString("pref.visExport.label.curveAltitudeMode"));
		pointAltitudeModeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
        	pointAltitudeModeComboBox.addItem(c);
        }

		pointCrossLineDefaultStyle.setText(Language.I18N.getString("pref.visExport.label.defaultStyle"));
		pointCrossLineNormalColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.choosePointColor"));
		pointCrossLineHighlightingColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.choosePointHighlightingColor"));
		pointIconColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.choosePointIconColor"));
		pointCubeFillColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.chooseFillColor"));
		pointCubeHighlightingColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.chooseFillColor"));
		curveNormalColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.chooseCurveColor"));
		curveHighlightingColorButton.setDialogTitle(Language.I18N.getString("pref.visExport.label.chooseCurveHighlightingColor"));

		iconPanel.setTitle(Language.I18N.getString("pref.visExport.pointdisplay.mode.label.icon"));
		crossLinePanel.setTitle(Language.I18N.getString("pref.visExport.pointdisplay.mode.label.cross"));
		cubePanel.setTitle(Language.I18N.getString("pref.visExport.pointdisplay.mode.label.cube"));

		pointCubeDefaultStyle.setText(Language.I18N.getString("pref.visExport.label.defaultStyle"));
        pointCubeLengthOfSideLabel.setText(Language.I18N.getString("pref.visExport.label.cubeSideLength"));
        pointCubeFillColorLabel.setText(Language.I18N.getString("pref.visExport.label.pointColor"));
        pointCubeHighlightingCheckbox.setText(Language.I18N.getString("pref.visExport.label.highlightStyle"));
        pointCubeHighlightingColorLabel.setText(Language.I18N.getString("pref.visExport.label.pointColor"));
		pointCubeHighlightingLineThicknessLabel.setText(Language.I18N.getString("pref.visExport.label.curveThickness"));

		pointIconDefaultStyle.setText(Language.I18N.getString("pref.visExport.label.defaultStyle"));
        pointIconColorLabel.setText(Language.I18N.getString("pref.visExport.label.pointColor"));
        pointIconScaleLabel.setText(Language.I18N.getString("pref.visExport.label.pointIconScale"));
        pointIconHighlightingCheckbox.setText(Language.I18N.getString("pref.visExport.label.highlightStyle"));
		pointIconHighlightingColorLabel.setText(Language.I18N.getString("pref.visExport.label.pointColor"));
		pointIconHighlightingScaleLabel.setText(Language.I18N.getString("pref.visExport.label.pointIconScale"));
        pointAltitudeModeComboBox.setSelectedItem(pacSettings.getPointAltitudeMode());
       
        pointCrossLineThicknessLabel.setText(Language.I18N.getString("pref.visExport.label.curveThickness"));
        pointCrossLineNormalColorLabel.setText(Language.I18N.getString("pref.visExport.label.pointColor"));
		pointCrossLineHighlightingCheckbox.setText(Language.I18N.getString("pref.visExport.label.highlightStyle"));
		pointCrossLineHighlightingThicknessLabel.setText(Language.I18N.getString("pref.visExport.label.curveThickness"));
		pointCrossLineHighlightingColorLabel.setText(Language.I18N.getString("pref.visExport.label.pointColor"));

		curvePanel.setTitle(Language.I18N.getString("pref.visExport.border.curve"));
    	curveAltitudeModeLabel.setText(Language.I18N.getString("pref.visExport.label.curveAltitudeMode"));
		curveAltitudeModeComboBox.removeAllItems();
        for (AltitudeMode c: AltitudeMode.values()) {
        	curveAltitudeModeComboBox.addItem(c);
        }

		curveDefaultStyle.setText(Language.I18N.getString("pref.visExport.label.defaultStyle"));
        curveAltitudeModeComboBox.setSelectedItem(pacSettings.getCurveAltitudeMode());
    	curveThicknessLabel.setText(Language.I18N.getString("pref.visExport.label.curveThickness"));
    	curveNormalColorLabel.setText(Language.I18N.getString("pref.visExport.label.curveColor"));
		curveHighlightingCheckbox.setText(Language.I18N.getString("pref.visExport.label.highlightStyle"));
    	curveHighlightingThicknessLabel.setText(Language.I18N.getString("pref.visExport.label.curveThickness"));
    	curveHighlightingColorLabel.setText(Language.I18N.getString("pref.visExport.label.curveColor"));
	}

	@Override
	public void loadSettings() {
		PointAndCurve pacSettings = pointAndCurveSupplier.get();

		switch (pacSettings.getPointDisplayMode()) {
			case ICON:
				iconRButton.setSelected(true);
				break;
			case CROSS_LINE:
				crossLineRButton.setSelected(true);
				break;
			case CUBE:
				cubeRButton.setSelected(true);
				break;
		}

		pointAltitudeModeComboBox.setSelectedItem(pacSettings.getPointAltitudeMode());
		pointCrossLineThicknessSpinner.setValue(pacSettings.getPointThickness());
		pointCrossLineNormalColorButton.setColor(new Color(pacSettings.getPointNormalColor(), true));
		pointCrossLineHighlightingCheckbox.setSelected(pacSettings.isPointHighlightingEnabled());
		pointCrossLineHighlightingThicknessSpinner.setValue(pacSettings.getPointHighlightedThickness());
		pointCrossLineHighlightingColorButton.setColor(new Color(pacSettings.getPointHighlightedColor(), true));

		pointIconColorButton.setColor(new Color(pacSettings.getPointIconColor(), true));
		pointIconScaleSpinner.setValue(pacSettings.getPointIconScale());
		pointIconHighlightingCheckbox.setSelected(pacSettings.isPointIconHighlightingEnabled());
		pointIconHighlightingColorButton.setColor(new Color(pacSettings.getPointIconHighlightedColor(), true));
		pointIconHighlightingScaleSpinner.setValue(pacSettings.getPointIconHighlightedScale());
		
		pointCubeLengthOfSideSpinner.setValue(pacSettings.getPointCubeLengthOfSide());
		pointCubeFillColorButton.setColor(new Color(pacSettings.getPointCubeFillColor(), true));
		pointCubeHighlightingCheckbox.setSelected(pacSettings.isPointCubeHighlightingEnabled());
		pointCubeHighlightingColorButton.setColor(new Color(pacSettings.getPointCubeHighlightedColor(), true));
		pointCubeHighlightingLineThicknessSpinner.setValue(pacSettings.getPointCubeHighlightedOutlineThickness());

		curveAltitudeModeComboBox.setSelectedItem(pacSettings.getCurveAltitudeMode());
		curveThicknessSpinner.setValue(pacSettings.getCurveThickness());
		curveNormalColorButton.setColor(new Color(pacSettings.getCurveNormalColor(), true));
		curveHighlightingCheckbox.setSelected(pacSettings.isCurveHighlightingEnabled());
		curveHighlightingThicknessSpinner.setValue(pacSettings.getCurveHighlightedThickness());
		curveHighlightingColorButton.setColor(new Color(pacSettings.getCurveHighlightedColor(), true));

		setEnabledComponents();
	}

	@Override
	public void setSettings() {
		PointAndCurve pacSettings = pointAndCurveSupplier.get();

		if (iconRButton.isSelected()) {
			pacSettings.setPointDisplayMode(PointDisplayMode.ICON);
		} else if (crossLineRButton.isSelected()) {
			pacSettings.setPointDisplayMode(PointDisplayMode.CROSS_LINE);
		} else if (cubeRButton.isSelected()) {
			pacSettings.setPointDisplayMode(PointDisplayMode.CUBE);
		}

		pacSettings.setPointAltitudeMode((AltitudeMode) pointAltitudeModeComboBox.getSelectedItem());
		pacSettings.setPointThickness((Double) pointCrossLineThicknessSpinner.getValue());
		pacSettings.setPointNormalColor(pointCrossLineNormalColorButton.getColor().getRGB());
		pacSettings.setPointHighlightingEnabled(pointCrossLineHighlightingCheckbox.isSelected());
		pacSettings.setPointHighlightedThickness((Double) pointCrossLineHighlightingThicknessSpinner.getValue());
		pacSettings.setPointHighlightedColor(pointCrossLineHighlightingColorButton.getColor().getRGB());
		pacSettings.setPointIconColor(pointIconColorButton.getColor().getRGB());
		pacSettings.setPointIconScale((Double) pointIconScaleSpinner.getValue());
		pacSettings.setPointIconHighlightingEnabled(pointIconHighlightingCheckbox.isSelected());
		pacSettings.setPointIconHighlightedColor(pointIconHighlightingColorButton.getColor().getRGB());
		pacSettings.setPointIconHighlightedScale((Double) pointIconHighlightingScaleSpinner.getValue());

		pacSettings.setPointCubeLengthOfSide((Double) pointCubeLengthOfSideSpinner.getValue());
		pacSettings.setPointCubeFillColor(pointCubeFillColorButton.getColor().getRGB());
		pacSettings.setPointCubeHighlightingEnabled(pointCubeHighlightingCheckbox.isSelected());
		pacSettings.setPointCubeHighlightedColor(pointCubeHighlightingColorButton.getColor().getRGB());
		pacSettings.setPointCubeHighlightedOutlineThickness((Double) pointCubeHighlightingLineThicknessSpinner.getValue());

		pacSettings.setCurveAltitudeMode((AltitudeMode) curveAltitudeModeComboBox.getSelectedItem());
		pacSettings.setCurveThickness((Double) curveThicknessSpinner.getValue());
		pacSettings.setCurveNormalColor(curveNormalColorButton.getColor().getRGB());
		pacSettings.setCurveHighlightingEnabled(curveHighlightingCheckbox.isSelected());
		pacSettings.setCurveHighlightedThickness((Double) curveHighlightingThicknessSpinner.getValue());
		pacSettings.setCurveHighlightedColor(curveHighlightingColorButton.getColor().getRGB());
	}

	private void setEnabledComponents() {
		setEnabledPointComponents();
		setEnabledCurveComponents();
	}

	private void setEnabledPointComponents() {
		pointIconDefaultStyle.setEnabled(iconRButton.isSelected());
		pointIconColorLabel.setEnabled(iconRButton.isSelected());
		pointIconColorButton.setEnabled(iconRButton.isSelected());
		pointIconScaleLabel.setEnabled(iconRButton.isSelected());
		pointIconScaleSpinner.setEnabled(iconRButton.isSelected());
		pointIconHighlightingCheckbox.setEnabled(iconRButton.isSelected());
		pointIconHighlightingColorLabel.setEnabled(pointIconHighlightingCheckbox.isSelected() && iconRButton.isSelected());
		pointIconHighlightingColorButton.setEnabled(pointIconHighlightingCheckbox.isSelected() && iconRButton.isSelected());
		pointIconHighlightingScaleLabel.setEnabled(pointIconHighlightingCheckbox.isSelected() && iconRButton.isSelected());
		pointIconHighlightingScaleSpinner.setEnabled(pointIconHighlightingCheckbox.isSelected() && iconRButton.isSelected());

		pointCrossLineDefaultStyle.setEnabled(crossLineRButton.isSelected());
		pointCrossLineThicknessLabel.setEnabled(crossLineRButton.isSelected());
		pointCrossLineThicknessSpinner.setEnabled(crossLineRButton.isSelected());
		pointCrossLineNormalColorLabel.setEnabled(crossLineRButton.isSelected());
		pointCrossLineNormalColorButton.setEnabled(crossLineRButton.isSelected());
		pointCrossLineHighlightingCheckbox.setEnabled(crossLineRButton.isSelected());
		pointCrossLineHighlightingColorLabel.setEnabled(pointCrossLineHighlightingCheckbox.isSelected() && crossLineRButton.isSelected());
		pointCrossLineHighlightingColorButton.setEnabled(pointCrossLineHighlightingCheckbox.isSelected() && crossLineRButton.isSelected());
		pointCrossLineHighlightingThicknessLabel.setEnabled(pointCrossLineHighlightingCheckbox.isSelected() && crossLineRButton.isSelected());
		pointCrossLineHighlightingThicknessSpinner.setEnabled(pointCrossLineHighlightingCheckbox.isSelected() && crossLineRButton.isSelected());

		pointCubeDefaultStyle.setEnabled(cubeRButton.isSelected());
		pointCubeLengthOfSideLabel.setEnabled(cubeRButton.isSelected());
		pointCubeLengthOfSideSpinner.setEnabled(cubeRButton.isSelected());
		pointCubeFillColorLabel.setEnabled(cubeRButton.isSelected());
		pointCubeFillColorButton.setEnabled(cubeRButton.isSelected());
		pointCubeHighlightingCheckbox.setEnabled(cubeRButton.isSelected());
		pointCubeHighlightingColorLabel.setEnabled(pointCubeHighlightingCheckbox.isSelected() && cubeRButton.isSelected());
		pointCubeHighlightingColorButton.setEnabled(pointCubeHighlightingCheckbox.isSelected() && cubeRButton.isSelected());
		pointCubeHighlightingLineThicknessLabel.setEnabled(pointCubeHighlightingCheckbox.isSelected() && cubeRButton.isSelected());
		pointCubeHighlightingLineThicknessSpinner.setEnabled(pointCubeHighlightingCheckbox.isSelected() && cubeRButton.isSelected());
	}

	private void setEnabledCurveComponents() {
		curveHighlightingColorLabel.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingColorButton.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingThicknessLabel.setEnabled(curveHighlightingCheckbox.isSelected());
		curveHighlightingThicknessSpinner.setEnabled(curveHighlightingCheckbox.isSelected());
	}

	private void setSpinnerFormat(JSpinner spinner, String decimalFormatPattern) {
		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, decimalFormatPattern);
		DecimalFormat format = editor.getFormat();
		format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		spinner.setEditor(editor);
	}
}
