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
import org.citydb.config.project.query.filter.version.CityGMLVersionType;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class VersionPanel extends AbstractPreferencesComponent {
	private TitledPanel versionPanel;
	private JRadioButton[] cityGMLVersionBox;

	public VersionPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		CityGMLVersionType version = config.getExportConfig().getSimpleQuery().getVersion();
		for (int i = 0; i < CityGMLVersionType.values().length; i++) {
			if (cityGMLVersionBox[i].isSelected()) {
				return version != CityGMLVersionType.fromValue(cityGMLVersionBox[i].getText());
			}
		}

		return false;
	}

	private void initGui() {
		ButtonGroup group = new ButtonGroup();
		cityGMLVersionBox = new JRadioButton[CityGMLVersionType.values().length];

		for (int i = 0; i < CityGMLVersionType.values().length; i++) {			
			cityGMLVersionBox[i] = new JRadioButton();
			cityGMLVersionBox[i].setText(CityGMLVersionType.values()[i].toString());
			group.add(cityGMLVersionBox[i]);
		}

		setLayout(new GridBagLayout());
		JPanel content = new JPanel();
		content.setLayout(new GridBagLayout());
		{
			for (int i = 0; i < cityGMLVersionBox.length; i++) {
				content.add(cityGMLVersionBox[i], GuiUtil.setConstraints(0, i, 1, 1, GridBagConstraints.BOTH, i == 0 ? 0 : 5, 0, 0, 0));
			}

			versionPanel = new TitledPanel().build(content);
		}

		add(versionPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
	}

	@Override
	public void doTranslation() {
		versionPanel.setTitle(Language.I18N.getString("pref.export.version.border.versions"));
	}

	@Override
	public void loadSettings() {
		CityGMLVersionType version = config.getExportConfig().getSimpleQuery().getVersion();
		if (version != null) {
			for (int i = 0; i < CityGMLVersionType.values().length; i++) {
				if (CityGMLVersionType.values()[i] == version) {
					cityGMLVersionBox[i].setSelected(true);
					break;
				}
			}
		} else {
			cityGMLVersionBox[0].setSelected(true);
		}
	}

	@Override
	public void setSettings() {
		for (int i = 0; i < CityGMLVersionType.values().length; i++) {
			if (cityGMLVersionBox[i].isSelected()) {
				config.getExportConfig().getSimpleQuery().setVersion(CityGMLVersionType.fromValue(cityGMLVersionBox[i].getText()));
				break;
			}
		}
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.version");
	}
}
