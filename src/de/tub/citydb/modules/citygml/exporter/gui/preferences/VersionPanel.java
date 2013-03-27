/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citygml4j.model.module.citygml.CityGMLVersion;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.exporter.CityGMLVersionType;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

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

			if (CityGMLVersionType.values()[i].toCityGMLVersion() == CityGMLVersion.DEFAULT)
				cityGMLVersionBox[i].setSelected(true);
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
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.export.version.border.versions"));	
	}

	@Override
	public void loadSettings() {
		CityGMLVersionType version = config.getProject().getExporter().getCityGMLVersion();
		if (version != null) {
			for (int i = 0; i < CityGMLVersionType.values().length; i++) {
				if (CityGMLVersionType.values()[i].toCityGMLVersion() == version.toCityGMLVersion()) {
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
		return Internal.I18N.getString("pref.tree.export.version");
	}

}
