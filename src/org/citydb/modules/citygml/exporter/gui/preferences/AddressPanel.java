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
package org.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.AddressMode;
import org.citydb.config.project.exporter.ExportAddress;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class AddressPanel extends AbstractPreferencesComponent {
	private JPanel exportXALPanel;
	private JRadioButton exportXAL;
	private JRadioButton exportDB;
	private JCheckBox exportFallback;

	public AddressPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportAddress address = config.getProject().getExporter().getAddress();
		
		if (exportDB.isSelected() && address.getMode() != AddressMode.DB) return true;
		if (exportXAL.isSelected() && address.getMode() != AddressMode.XAL) return true;
		if (exportFallback.isSelected() != address.isSetUseFallback()) return true;
		
		return false;
	}

	private void initGui() {
		exportXAL = new JRadioButton();
		exportXAL.setIconTextGap(10);
		exportDB = new JRadioButton();
		exportDB.setIconTextGap(10);
		ButtonGroup exportGroup = new ButtonGroup();
		exportGroup.add(exportXAL);
		exportGroup.add(exportDB);
		exportFallback = new JCheckBox();
		exportFallback.setIconTextGap(10);

		setLayout(new GridBagLayout());
		{
			exportXALPanel = new JPanel();
			add(exportXALPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			exportXALPanel.setBorder(BorderFactory.createTitledBorder(""));
			exportXALPanel.setLayout(new GridBagLayout());
			{
				exportXALPanel.add(exportDB, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				exportXALPanel.add(exportXAL, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				exportXALPanel.add(exportFallback, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}

	@Override
	public void setSettings() {
		ExportAddress address = config.getProject().getExporter().getAddress();
		
		address.setMode(exportDB.isSelected() ? AddressMode.DB : AddressMode.XAL);
		address.setUseFallback(exportFallback.isSelected());
	}

	@Override
	public void loadSettings() {
		ExportAddress address = config.getProject().getExporter().getAddress();
		
		switch (address.getMode()) {
		case XAL:
			exportXAL.setSelected(true);
			break;
		default:
			exportDB.setSelected(true);
		}
		
		exportFallback.setSelected(address.isSetUseFallback());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)exportXALPanel.getBorder()).setTitle(Language.I18N.getString("pref.export.address.border.export"));	
		exportXAL.setText(Language.I18N.getString("pref.export.address.label.exportXAL"));
		exportDB.setText(Language.I18N.getString("pref.export.address.label.exportDB"));
		exportFallback.setText(Language.I18N.getString("pref.export.address.label.exportFallback"));
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.address");
	}

}
