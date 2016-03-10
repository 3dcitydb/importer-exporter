/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.exporter.gui.preferences;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.exporter.ExportCityObjectGroup;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class CityObjectGroupPanel extends AbstractPreferencesComponent {
	private JPanel exportGroupPanel;
	private JCheckBox exportMemberAsXLink;
	private JLabel exportMemberAsXLinkDescr;

	public CityObjectGroupPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ExportCityObjectGroup group = config.getProject().getExporter().getCityObjectGroup();
		if (exportMemberAsXLink.isSelected() != group.isExportMemberAsXLinks()) return true;
		return false;
	}

	private void initGui() {
		exportMemberAsXLink = new JCheckBox();
		exportMemberAsXLink.setIconTextGap(10);
		exportMemberAsXLinkDescr = new JLabel();

		setLayout(new GridBagLayout());
		{
			exportGroupPanel = new JPanel();
			add(exportGroupPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			exportGroupPanel.setBorder(BorderFactory.createTitledBorder(""));
			exportMemberAsXLinkDescr.setFont(exportMemberAsXLinkDescr.getFont().deriveFont(Font.ITALIC));
			int lmargin = (int)(exportMemberAsXLink.getPreferredSize().getWidth()) + 11;
			exportGroupPanel.setLayout(new GridBagLayout());
			{
				exportGroupPanel.add(exportMemberAsXLink, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				exportGroupPanel.add(exportMemberAsXLinkDescr, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}
		}
	}
	
	@Override
	public void loadSettings() {
		ExportCityObjectGroup group = config.getProject().getExporter().getCityObjectGroup();
		exportMemberAsXLink.setSelected(group.isExportMemberAsXLinks());
	}

	@Override
	public void setSettings() {
		ExportCityObjectGroup group = config.getProject().getExporter().getCityObjectGroup();
		group.setExportMemberAsXLinks(exportMemberAsXLink.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)exportGroupPanel.getBorder()).setTitle(Language.I18N.getString("pref.export.group.border.member"));	
		exportMemberAsXLink.setText(Language.I18N.getString("pref.export.group.label.exportMember"));
		exportMemberAsXLinkDescr.setText(Language.I18N.getString("pref.export.group.label.exportMember.description"));
	}

	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.export.group");
	}

}
