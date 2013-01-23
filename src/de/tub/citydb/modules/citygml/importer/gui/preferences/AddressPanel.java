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
package de.tub.citydb.modules.citygml.importer.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.ImportAddress;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

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
		((TitledBorder)importXALPanel.getBorder()).setTitle(Internal.I18N.getString("pref.import.address.border.import"));	
		importXAL.setText(Internal.I18N.getString("pref.import.address.label.importXAL"));
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import.address");
	}

}
