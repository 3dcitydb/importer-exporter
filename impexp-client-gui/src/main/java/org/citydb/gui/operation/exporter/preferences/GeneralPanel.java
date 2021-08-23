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
package org.citydb.gui.operation.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.FeatureEnvelopeMode;
import org.citydb.config.project.exporter.GeneralOptions;
import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.util.event.global.PropertyChangeEvent;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.operation.common.DefaultPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;

import javax.swing.*;
import java.awt.*;

public class GeneralPanel extends DefaultPreferencesComponent {
	private TitledPanel versionPanel;
	private JRadioButton cityGMLv2;
	private JRadioButton cityGMLv1;
	private JLabel versionHintLabel;
	private JLabel compressedOutputFormatLabel;
	private JComboBox<OutputFormat> compressedOutputFormat;

	private TitledPanel envelopePanel;
	private JLabel featureEnvelopeLabel;
	private JComboBox<FeatureEnvelopeMode> featureEnvelope;
	private JCheckBox cityModelEnvelope;
	private JCheckBox useTileExtent;

	public GeneralPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		CityGMLVersionType version = config.getExportConfig().getSimpleQuery().getVersion();
		if (cityGMLv2.isSelected() && version != CityGMLVersionType.v2_0_0) return true;
		if (cityGMLv1.isSelected() && version != CityGMLVersionType.v1_0_0) return true;

		GeneralOptions generalOptions = config.getExportConfig().getGeneralOptions();
		if (compressedOutputFormat.getSelectedItem() != generalOptions.getCompressedOutputFormat()) return true;
		if (featureEnvelope.getSelectedItem() != generalOptions.getEnvelope().getFeatureMode()) return true;
		if (cityModelEnvelope.isSelected() != generalOptions.getEnvelope().isUseEnvelopeOnCityModel()) return true;
		if (useTileExtent.isSelected() != generalOptions.getEnvelope().isUseTileExtentForCityModel()) return true;

		return false;
	}

	private void initGui() {
		cityGMLv2 = new JRadioButton();
		cityGMLv1 = new JRadioButton();

		ButtonGroup versionGroup = new ButtonGroup();
		versionGroup.add(cityGMLv2);
		versionGroup.add(cityGMLv1);

		versionHintLabel = new JLabel();
		versionHintLabel.setFont(versionHintLabel.getFont().deriveFont(Font.ITALIC));

		compressedOutputFormatLabel = new JLabel();
		compressedOutputFormat = new JComboBox<>();
		for (OutputFormat outputFormat : OutputFormat.values()) {
			compressedOutputFormat.addItem(outputFormat);
		}

		featureEnvelopeLabel = new JLabel();
		featureEnvelope = new JComboBox<>();
		for (FeatureEnvelopeMode mode : FeatureEnvelopeMode.values()) {
			featureEnvelope.addItem(mode);
		}

		featureEnvelope.setRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				if (value == FeatureEnvelopeMode.TOP_LEVEL) {
					value = Language.I18N.getString("pref.export.general.feature.topLevel");
				} else if (value == FeatureEnvelopeMode.ALL) {
					value = Language.I18N.getString("pref.export.general.feature.all");
				} else {
					value = Language.I18N.getString("pref.export.general.feature.none");
				}

				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		});

		cityModelEnvelope = new JCheckBox();
		useTileExtent = new JCheckBox();

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());

			int lmargin = GuiUtil.getTextOffset(cityGMLv2);
			content.add(cityGMLv2, GuiUtil.setConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			content.add(versionHintLabel, GuiUtil.setConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
			content.add(cityGMLv1, GuiUtil.setConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(compressedOutputFormatLabel, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 5));
			content.add(compressedOutputFormat, GuiUtil.setConstraints(1, 3, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));

			versionPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());

			int lmargin = GuiUtil.getTextOffset(cityModelEnvelope);
			content.add(featureEnvelopeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
			content.add(featureEnvelope, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			content.add(cityModelEnvelope, GuiUtil.setConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(useTileExtent, GuiUtil.setConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, lmargin, 0, 0));

			envelopePanel = new TitledPanel().build(content);
		}

		add(versionPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(envelopePanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		cityModelEnvelope.addActionListener(e -> setEnabledEnvelopeOptions());
	}

	@Override
	public void doTranslation() {
		versionPanel.setTitle(Language.I18N.getString("pref.export.general.border.general"));
		cityGMLv2.setText(Language.I18N.getString("pref.export.general.label.citygmlv2"));
		cityGMLv1.setText(Language.I18N.getString("pref.export.general.label.citygmlv1"));
		versionHintLabel.setText(Language.I18N.getString("pref.export.general.label.versionHint"));
		compressedOutputFormatLabel.setText(Language.I18N.getString("pref.export.general.label.compressedFormat"));
		envelopePanel.setTitle(Language.I18N.getString("pref.export.general.border.bbox"));
		featureEnvelopeLabel.setText(Language.I18N.getString("pref.export.general.label.feature"));
		cityModelEnvelope.setText(Language.I18N.getString("pref.export.general.label.cityModel"));
		useTileExtent.setText(Language.I18N.getString("pref.export.general.label.useTileExtent"));
	}

	@Override
	public void loadSettings() {
		CityGMLVersionType version = config.getExportConfig().getSimpleQuery().getVersion();
		if (version == null) {
			version = CityGMLVersionType.v2_0_0;
		}

		cityGMLv2.setSelected(version == CityGMLVersionType.v2_0_0);
		cityGMLv1.setSelected(version == CityGMLVersionType.v1_0_0);
		firePropertyChange(version);

		GeneralOptions generalOptions = config.getExportConfig().getGeneralOptions();
		compressedOutputFormat.setSelectedItem(generalOptions.getCompressedOutputFormat());
		featureEnvelope.setSelectedItem(generalOptions.getEnvelope().getFeatureMode());
		cityModelEnvelope.setSelected(generalOptions.getEnvelope().isUseEnvelopeOnCityModel());
		useTileExtent.setSelected(generalOptions.getEnvelope().isUseTileExtentForCityModel());

		setEnabledEnvelopeOptions();
	}

	@Override
	public void setSettings() {
		CityGMLVersionType version = cityGMLv1.isSelected() ? CityGMLVersionType.v1_0_0 : CityGMLVersionType.v2_0_0;
		config.getExportConfig().getSimpleQuery().setVersion(version);
		firePropertyChange(version);

		GeneralOptions generalOptions = config.getExportConfig().getGeneralOptions();
		generalOptions.setCompressedOutputFormat((OutputFormat) compressedOutputFormat.getSelectedItem());
		generalOptions.getEnvelope().setFeatureMode((FeatureEnvelopeMode) featureEnvelope.getSelectedItem());
		generalOptions.getEnvelope().setUseEnvelopeOnCityModel(cityModelEnvelope.isSelected());
		generalOptions.getEnvelope().setUseTileExtentForCityModel(useTileExtent.isSelected());
	}

	private void setEnabledEnvelopeOptions() {
		useTileExtent.setEnabled(cityModelEnvelope.isSelected());
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.general");
	}

	private void firePropertyChange(CityGMLVersionType version) {
		ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(
				new PropertyChangeEvent("citygml.version", null, Util.toCityGMLVersion(version), this));
	}
}
