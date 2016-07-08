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
