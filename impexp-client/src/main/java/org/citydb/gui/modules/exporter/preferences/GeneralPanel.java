/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.gui.modules.exporter.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.event.global.PropertyChangeEvent;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;

import javax.swing.*;
import java.awt.*;

public class GeneralPanel extends AbstractPreferencesComponent {
	private TitledPanel versionPanel;
	private JRadioButton cityGMLv2;
	private JRadioButton cityGMLv1;
	private JLabel compressedOutputFormatLabel;
	private JComboBox<OutputFormat> compressedOutputFormat;

	public GeneralPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		CityGMLVersionType version = config.getExportConfig().getSimpleQuery().getVersion();
		if (cityGMLv2.isSelected() && version != CityGMLVersionType.v2_0_0) return true;
		if (cityGMLv1.isSelected() && version != CityGMLVersionType.v1_0_0) return true;
		if (compressedOutputFormat.getSelectedItem() != config.getExportConfig().getGeneralOptions().getCompressedOutputFormat()) return true;

		return false;
	}

	private void initGui() {
		cityGMLv2 = new JRadioButton();
		cityGMLv1 = new JRadioButton();

		ButtonGroup versionGroup = new ButtonGroup();
		versionGroup.add(cityGMLv2);
		versionGroup.add(cityGMLv1);

		compressedOutputFormatLabel = new JLabel();
		compressedOutputFormat = new JComboBox<>();
		for (OutputFormat outputFormat : OutputFormat.values()) {
			compressedOutputFormat.addItem(outputFormat);
		}

		setLayout(new GridBagLayout());
		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		{
			JPanel formatPanel = new JPanel();
			formatPanel.setLayout(new GridBagLayout());
			formatPanel.add(compressedOutputFormatLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
			formatPanel.add(compressedOutputFormat, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));

			content.add(cityGMLv2, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
			content.add(cityGMLv1, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			content.add(formatPanel, GuiUtil.setConstraints(0, 2, 1, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));

			versionPanel = new TitledPanel().build(content);
		}

		add(versionPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
	}

	@Override
	public void doTranslation() {
		versionPanel.setTitle(Language.I18N.getString("pref.export.general.border.general"));
		cityGMLv2.setText(Language.I18N.getString("pref.export.general.label.citygmlv2"));
		cityGMLv1.setText(Language.I18N.getString("pref.export.general.label.citygmlv1"));
		compressedOutputFormatLabel.setText(Language.I18N.getString("pref.export.general.label.compressedFormat"));
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

		compressedOutputFormat.setSelectedItem(config.getExportConfig().getGeneralOptions().getCompressedOutputFormat());
	}

	@Override
	public void setSettings() {
		CityGMLVersionType version = cityGMLv1.isSelected() ? CityGMLVersionType.v1_0_0 : CityGMLVersionType.v2_0_0;
		config.getExportConfig().getSimpleQuery().setVersion(version);
		firePropertyChange(version);

		config.getExportConfig().getGeneralOptions().setCompressedOutputFormat((OutputFormat) compressedOutputFormat.getSelectedItem());
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
