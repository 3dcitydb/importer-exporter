/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.importer.ImportAddress;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class AddressPanel extends AbstractPreferencesComponent {
	private JPanel importXALPanel;
	private JCheckBox importXAL;

	public AddressPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportAddress address = config.getProject().getImporter().getAddress();
		if (importXAL.isSelected() != address.isSetImportXAL()) return true;
		return false;
	}

	private void initGui() {
		importXAL = new JCheckBox();
		importXAL.setIconTextGap(10);

		setLayout(new GridBagLayout());
		{
			importXALPanel = new JPanel();
			add(importXALPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			importXALPanel.setBorder(BorderFactory.createTitledBorder(""));
			importXALPanel.setLayout(new GridBagLayout());
			{
				importXALPanel.add(importXAL, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}
	
	@Override
	public void loadSettings() {
		ImportAddress address = config.getProject().getImporter().getAddress();
		importXAL.setSelected(address.isSetImportXAL());
	}

	@Override
	public void setSettings() {
		ImportAddress address = config.getProject().getImporter().getAddress();
		address.setImportXAL(importXAL.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)importXALPanel.getBorder()).setTitle(Language.I18N.getString("pref.import.address.border.import"));	
		importXAL.setText(Language.I18N.getString("pref.import.address.label.importXAL"));
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.address");
	}

}
