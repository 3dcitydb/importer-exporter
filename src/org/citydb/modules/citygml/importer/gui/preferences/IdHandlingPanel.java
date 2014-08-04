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
package org.citydb.modules.citygml.importer.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.importer.ImportGmlId;
import org.citydb.config.project.importer.UUIDMode;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class IdHandlingPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JCheckBox impIdCheckExtRef;

	private JRadioButton impIdRadioAdd;
	private JRadioButton impIdRadioExchange;

	public IdHandlingPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();
		
		if (impIdCheckExtRef.isSelected() != gmlId.isSetKeepGmlIdAsExternalReference()) return true;
		if (impIdRadioAdd.isSelected() != gmlId.isUUIDModeComplement()) return true;
		if (impIdRadioExchange.isSelected() != gmlId.isUUIDModeReplace()) return true;
		return false;
	}

	private void initGui(){
		impIdRadioAdd = new JRadioButton();
		impIdRadioExchange = new JRadioButton();
		ButtonGroup impIdRadio = new ButtonGroup();
		impIdRadio.add(impIdRadioAdd);
		impIdRadio.add(impIdRadioExchange);
		impIdCheckExtRef = new JCheckBox();
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			impIdRadioAdd.setIconTextGap(10);
			impIdRadioExchange.setIconTextGap(10);
			impIdCheckExtRef.setIconTextGap(10);
			int lmargin = (int)(impIdRadioAdd.getPreferredSize().getWidth()) + 11;
			{
				block1.add(impIdRadioAdd, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impIdRadioExchange, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(impIdCheckExtRef, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));		
			}
		}
		
		ActionListener gmlIdListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledGmlId();
			}
		};
		
		impIdRadioAdd.addActionListener(gmlIdListener);
		impIdRadioExchange.addActionListener(gmlIdListener);
	}
	
	private void setEnabledGmlId() {
		impIdCheckExtRef.setEnabled(impIdRadioExchange.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.idHandling.border.id"));	
		impIdCheckExtRef.setText(Language.I18N.getString("pref.import.idHandling.label.id.extReference"));
		impIdRadioAdd.setText(Language.I18N.getString("pref.import.idHandling.label.id.add"));
		impIdRadioExchange.setText(Language.I18N.getString("pref.import.idHandling.label.id.exchange"));
	}

	@Override
	public void loadSettings() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();
		
		if (gmlId.isUUIDModeReplace())
			impIdRadioExchange.setSelected(true);
		else
			impIdRadioAdd.setSelected(true);

		impIdCheckExtRef.setSelected(gmlId.isSetKeepGmlIdAsExternalReference());
		
		setEnabledGmlId();
	}

	@Override
	public void setSettings() {
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();
		
		if (impIdRadioAdd.isSelected()) {
			gmlId.setUuidMode(UUIDMode.COMPLEMENT);
		}
		else {
			gmlId.setUuidMode(UUIDMode.REPLACE);
		}

		gmlId.setKeepGmlIdAsExternalReference(impIdCheckExtRef.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.idHandling");
	}

}

