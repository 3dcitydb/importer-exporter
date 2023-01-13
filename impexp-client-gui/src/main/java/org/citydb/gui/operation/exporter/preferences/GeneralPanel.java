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

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.FeatureEnvelopeMode;
import org.citydb.config.project.exporter.GeneralOptions;
import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.AddTokenMenu;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.components.popup.TokenSettingsMenu;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.global.PropertyChangeEvent;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class GeneralPanel extends InternalPreferencesComponent {
	private TitledPanel generalPanel;
	private JRadioButton cityGMLv2;
	private JRadioButton cityGMLv1;
	private JLabel versionHintLabel;
	private JCheckBox failFastOnErrors;
	private JCheckBox computeNumberMatched;
	private JLabel featureEnvelopeLabel;
	private JComboBox<FeatureEnvelopeMode> featureEnvelope;
	private JLabel compressedOutputFormatLabel;
	private JComboBox<OutputFormat> compressedOutputFormat;

	private TitledPanel metadataPanel;
	private JLabel nameLabel;
	private JTextField name;
	private JButton nameTokenButton;
	private JButton nameTokenSettingsButton;
	private TokenSettingsMenu nameTokenSettings;
	private JLabel descriptionLabel;
	private JTextField description;
	private JButton descriptionTokenButton;
	private JButton descriptionTokenSettingsButton;
	private TokenSettingsMenu descriptionTokenSettings;
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
		if (failFastOnErrors.isSelected() != generalOptions.isFailFastOnErrors()) return true;
		if (computeNumberMatched.isSelected() != generalOptions.getComputeNumberMatched().isEnabled()) return true;
		if (compressedOutputFormat.getSelectedItem() != generalOptions.getCompressedOutputFormat()) return true;
		if (featureEnvelope.getSelectedItem() != generalOptions.getEnvelope().getFeatureMode()) return true;
		if (!name.getText().equals(generalOptions.getDatasetName().getValue())) return true;
		if (nameTokenSettings.isModified(generalOptions.getDatasetName())) return true;
		if (!description.getText().equals(generalOptions.getDatasetDescription().getValue())) return true;
		if (descriptionTokenSettings.isModified(generalOptions.getDatasetDescription())) return true;
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

		failFastOnErrors = new JCheckBox();
		computeNumberMatched = new JCheckBox();

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

		compressedOutputFormatLabel = new JLabel();
		compressedOutputFormat = new JComboBox<>();
		for (OutputFormat outputFormat : OutputFormat.values()) {
			compressedOutputFormat.addItem(outputFormat);
		}

		nameLabel = new JLabel();
		name = new JTextField();
		nameTokenButton = new JButton();
		nameTokenButton.setHorizontalTextPosition(SwingConstants.LEFT);
		nameTokenButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/expand_more.svg"));
		nameTokenSettingsButton = new JButton();
		nameTokenSettingsButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/settings.svg"));
		nameTokenSettings = new TokenSettingsMenu();

		descriptionLabel = new JLabel();
		description = new JTextField();
		descriptionTokenButton = new JButton();
		descriptionTokenButton.setHorizontalTextPosition(SwingConstants.LEFT);
		descriptionTokenButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/expand_more.svg"));
		descriptionTokenSettingsButton = new JButton();
		descriptionTokenSettingsButton.setIcon(new FlatSVGIcon("org/citydb/gui/icons/settings.svg"));
		descriptionTokenSettings = new TokenSettingsMenu();

		cityModelEnvelope = new JCheckBox();
		useTileExtent = new JCheckBox();

		PopupMenuDecorator.getInstance().decorate(name);

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());

			JPanel envelopePanel = new JPanel();
			envelopePanel.setLayout(new GridBagLayout());
			{
				envelopePanel.add(featureEnvelopeLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				envelopePanel.add(featureEnvelope, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}

			JPanel compressedOutputPanel = new JPanel();
			compressedOutputPanel.setLayout(new GridBagLayout());
			{
				compressedOutputPanel.add(compressedOutputFormatLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
				compressedOutputPanel.add(compressedOutputFormat, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			}

			int lmargin = GuiUtil.getTextOffset(cityGMLv2);
			content.add(cityGMLv2, GuiUtil.setConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			content.add(versionHintLabel, GuiUtil.setConstraints(0, 1, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, lmargin, 0, 0));
			content.add(cityGMLv1, GuiUtil.setConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(failFastOnErrors, GuiUtil.setConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(computeNumberMatched, GuiUtil.setConstraints(0, 4, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(envelopePanel, GuiUtil.setConstraints(0, 5, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));
			content.add(compressedOutputPanel, GuiUtil.setConstraints(0, 6, 1, 0, GridBagConstraints.HORIZONTAL, 5, 5, 0, 0));

			generalPanel = new TitledPanel().build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());

			JToolBar nameToolBar = new JToolBar();
			nameToolBar.add(nameTokenButton);
			nameToolBar.addSeparator();
			nameToolBar.add(nameTokenSettingsButton);

			JToolBar descriptionToolBar = new JToolBar();
			descriptionToolBar.add(descriptionTokenButton);
			descriptionToolBar.addSeparator();
			descriptionToolBar.add(descriptionTokenSettingsButton);

			int lmargin = GuiUtil.getTextOffset(cityModelEnvelope);
			content.add(nameLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
			content.add(name, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
			content.add(nameToolBar, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			content.add(descriptionLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
			content.add(description, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
			content.add(descriptionToolBar, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
			content.add(cityModelEnvelope, GuiUtil.setConstraints(0, 2, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(useTileExtent, GuiUtil.setConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.BOTH, 5, lmargin, 0, 0));

			metadataPanel = new TitledPanel().build(content);
		}

		add(generalPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(metadataPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		cityModelEnvelope.addActionListener(e -> setEnabledEnvelopeOptions());

		int x = UIManager.getInsets("Button.toolbar.spacingInsets").left;
		nameTokenButton.addActionListener(e -> AddTokenMenu.newInstance()
				.withTarget(name)
				.show(nameTokenButton, x, nameTokenButton.getHeight()));
		descriptionTokenButton.addActionListener(e -> AddTokenMenu.newInstance()
				.withTarget(description)
				.show(descriptionTokenButton, x, descriptionTokenButton.getHeight()));

		nameTokenSettingsButton.addActionListener(
				e -> nameTokenSettings.show(nameTokenSettingsButton, x, nameTokenSettingsButton.getHeight()));
		descriptionTokenSettingsButton.addActionListener(
				e -> descriptionTokenSettings.show(descriptionTokenSettingsButton, x, descriptionTokenSettingsButton.getHeight()));

		UIManager.addPropertyChangeListener(e -> {
			if ("lookAndFeel".equals(e.getPropertyName())) {
				SwingUtilities.invokeLater(this::updateComponentUI);
			}
		});

		updateComponentUI();
	}

	private void setEnabledEnvelopeOptions() {
		useTileExtent.setEnabled(cityModelEnvelope.isSelected());
	}

	private void updateComponentUI() {
		nameTokenSettings.updateUI();
		descriptionTokenSettings.updateUI();
	}

	@Override
	public void switchLocale(Locale locale) {
		generalPanel.setTitle(Language.I18N.getString("pref.export.general.border.general"));
		cityGMLv2.setText(Language.I18N.getString("pref.export.general.label.citygmlv2"));
		cityGMLv1.setText(Language.I18N.getString("pref.export.general.label.citygmlv1"));
		versionHintLabel.setText(Language.I18N.getString("pref.export.general.label.versionHint"));
		failFastOnErrors.setText(Language.I18N.getString("pref.export.general.failFastOnError"));
		computeNumberMatched.setText(Language.I18N.getString("pref.export.general.computeNumberMatched"));
		featureEnvelopeLabel.setText(Language.I18N.getString("pref.export.general.label.feature"));
		compressedOutputFormatLabel.setText(Language.I18N.getString("pref.export.general.label.compressedFormat"));

		metadataPanel.setTitle(Language.I18N.getString("pref.export.general.border.metadata"));
		nameLabel.setText(Language.I18N.getString("pref.export.general.label.datasetName"));
		nameTokenButton.setText(Language.I18N.getString("pref.export.tiling.label.token"));
		descriptionLabel.setText(Language.I18N.getString("pref.export.general.label.datasetDescription"));
		descriptionTokenButton.setText(Language.I18N.getString("pref.export.tiling.label.token"));
		cityModelEnvelope.setText(Language.I18N.getString("pref.export.general.label.cityModel"));
		useTileExtent.setText(Language.I18N.getString("pref.export.general.label.useTileExtent"));

		nameTokenSettings.switchLocale(locale);
		descriptionTokenSettings.switchLocale(locale);
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
		failFastOnErrors.setSelected(generalOptions.isFailFastOnErrors());
		computeNumberMatched.setSelected(generalOptions.getComputeNumberMatched().isEnabled());
		featureEnvelope.setSelectedItem(generalOptions.getEnvelope().getFeatureMode());
		compressedOutputFormat.setSelectedItem(generalOptions.getCompressedOutputFormat());

		name.setText(generalOptions.getDatasetName().getValue());
		name.setCaretPosition(name.getText().length());
		nameTokenSettings.loadSettings(generalOptions.getDatasetName());
		description.setText(generalOptions.getDatasetDescription().getValue());
		description.setCaretPosition(description.getText().length());
		descriptionTokenSettings.loadSettings(generalOptions.getDatasetDescription());
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
		generalOptions.setFailFastOnErrors(failFastOnErrors.isSelected());
		generalOptions.getComputeNumberMatched().setEnabled(computeNumberMatched.isSelected());
		generalOptions.getEnvelope().setFeatureMode((FeatureEnvelopeMode) featureEnvelope.getSelectedItem());
		generalOptions.setCompressedOutputFormat((OutputFormat) compressedOutputFormat.getSelectedItem());

		generalOptions.getDatasetName().setValue(name.getText());
		nameTokenSettings.setSettings(generalOptions.getDatasetName());
		generalOptions.getDatasetDescription().setValue(description.getText());
		descriptionTokenSettings.setSettings(generalOptions.getDatasetDescription());
		generalOptions.getEnvelope().setUseEnvelopeOnCityModel(cityModelEnvelope.isSelected());
		generalOptions.getEnvelope().setUseTileExtentForCityModel(useTileExtent.isSelected());
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.export.general");
	}

	private void firePropertyChange(CityGMLVersionType version) {
		ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(
				new PropertyChangeEvent("citygml.version", null, Util.toCityGMLVersion(version)));
	}
}
