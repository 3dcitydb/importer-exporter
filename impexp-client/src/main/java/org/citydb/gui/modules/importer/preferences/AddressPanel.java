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
package org.citydb.gui.modules.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportAddress;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class AddressPanel extends AbstractPreferencesComponent {
	private TitledPanel importXALPanel;
	private JCheckBox importXAL;

	public AddressPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportAddress address = config.getImportConfig().getAddress();
		if (importXAL.isSelected() != address.isSetImportXAL()) return true;
		return false;
	}

	private void initGui() {
		importXAL = new JCheckBox();

		setLayout(new GridBagLayout());
		importXALPanel = new TitledPanel()
				.withToggleButton(importXAL)
				.showSeparator(false)
				.buildWithoutContent();

		add(importXALPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
	}
	
	@Override
	public void loadSettings() {
		ImportAddress address = config.getImportConfig().getAddress();
		importXAL.setSelected(address.isSetImportXAL());
	}

	@Override
	public void setSettings() {
		ImportAddress address = config.getImportConfig().getAddress();
		address.setImportXAL(importXAL.isSelected());
	}

	@Override
	public void doTranslation() {
		importXALPanel.setTitle(Language.I18N.getString("pref.import.address.label.importXAL"));
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.address");
	}

}
