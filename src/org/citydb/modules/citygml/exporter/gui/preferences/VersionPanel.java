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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.CityGMLVersionType;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.modules.common.event.PropertyChangeEvent;
import org.citydb.util.Util;
import org.citydb.util.gui.GuiUtil;
import org.citygml4j.model.module.citygml.CityGMLVersion;

@SuppressWarnings("serial")
public class VersionPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JRadioButton[] cityGMLVersionBox;

	public VersionPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		CityGMLVersionType version = config.getProject().getExporter().getCityGMLVersion();

		for (int i = 0; i < CityGMLVersionType.values().length; i++)
			if (cityGMLVersionBox[i].isSelected())
				return version != CityGMLVersionType.fromValue(cityGMLVersionBox[i].getText());

		return false;
	}

	private void initGui() {
		ButtonGroup group = new ButtonGroup();
		cityGMLVersionBox = new JRadioButton[CityGMLVersionType.values().length];

		for (int i = 0; i < CityGMLVersionType.values().length; i++) {			
			cityGMLVersionBox[i] = new JRadioButton();
			cityGMLVersionBox[i].setText(CityGMLVersionType.values()[i].toString());
			cityGMLVersionBox[i].setIconTextGap(10);
			group.add(cityGMLVersionBox[i]);

			if (Util.toCityGMLVersion(CityGMLVersionType.values()[i]) == CityGMLVersion.DEFAULT)
				cityGMLVersionBox[i].setSelected(true);
			
			// fire property change event
			cityGMLVersionBox[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					for (int i = 0; i < CityGMLVersionType.values().length; i++) {
						if (cityGMLVersionBox[i] == e.getSource()) {
							ObjectRegistry.getInstance().getEventDispatcher().triggerEvent(
									new PropertyChangeEvent("citygml.version", null, CityGMLVersionType.values()[i], VersionPanel.this));
							break;
						}
					}
				}
			});
		}

		setLayout(new GridBagLayout());
		block1 = new JPanel();
		add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
		block1.setBorder(BorderFactory.createTitledBorder(""));
		block1.setLayout(new GridBagLayout());
		{
			for (int i = 0; i < cityGMLVersionBox.length; i++)
				block1.add(cityGMLVersionBox[i], GuiUtil.setConstraints(0,i,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));										
		}
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.export.version.border.versions"));	
	}

	@Override
	public void loadSettings() {
		CityGMLVersionType version = config.getProject().getExporter().getCityGMLVersion();
		if (version != null) {
			for (int i = 0; i < CityGMLVersionType.values().length; i++) {
				if (CityGMLVersionType.values()[i] == version) {
					cityGMLVersionBox[i].setSelected(true);
					break;
				}
			}
		} else
			cityGMLVersionBox[0].setSelected(true);
	}

	@Override
	public void setSettings() {
		for (int i = 0; i < CityGMLVersionType.values().length; i++) {
			if (cityGMLVersionBox[i].isSelected()) {
				config.getProject().getExporter().setCityGMLVersion(CityGMLVersionType.fromValue(cityGMLVersionBox[i].getText()));
				break;
			}
		}
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.version");
	}

}
