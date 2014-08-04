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
package org.citydb.modules.preferences.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.global.Global;
import org.citydb.config.project.global.LanguageType;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class LanguagePanel extends AbstractPreferencesComponent {
	private JRadioButton importLanguageRadioDe;
	private JRadioButton importLanguageRadioEn;
	private JPanel language;
	private ImpExpGui mainView;
	
	public LanguagePanel(Config config, ImpExpGui mainView) {
		super(config);
		this.mainView = mainView;
		initGui();
	}
	
	@Override
	public boolean isModified() {
		LanguageType language = config.getProject().getGlobal().getLanguage();
		
		if (importLanguageRadioDe.isSelected() && !(language == LanguageType.DE)) return true;
		if (importLanguageRadioEn.isSelected() && !(language == LanguageType.EN)) return true;
		return false;
	}
	
	private void initGui() {		
		importLanguageRadioDe = new JRadioButton("");
		importLanguageRadioEn = new JRadioButton("");
		ButtonGroup importLanguageRadio = new ButtonGroup();
		importLanguageRadio.add(importLanguageRadioDe);
		importLanguageRadio.add(importLanguageRadioEn);
		
		setLayout(new GridBagLayout());
		{
			language = new JPanel();
			add(language, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			language.setBorder(BorderFactory.createTitledBorder(""));
			language.setLayout(new GridBagLayout());
			importLanguageRadioDe.setIconTextGap(10);
			importLanguageRadioEn.setIconTextGap(10);
			{
				language.add(importLanguageRadioDe, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				language.add(importLanguageRadioEn, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)language.getBorder()).setTitle(Language.I18N.getString("pref.general.language.border.selection"));
		importLanguageRadioDe.setText(Language.I18N.getString("pref.general.language.label.de"));
		importLanguageRadioEn.setText(Language.I18N.getString("pref.general.language.label.en"));
	}
	
	@Override
	public void loadSettings() {		
		LanguageType language = config.getProject().getGlobal().getLanguage();
		
		if (language == LanguageType.DE) {
			importLanguageRadioDe.setSelected(true);
		}
		else if (language == LanguageType.EN) {
			importLanguageRadioEn.setSelected(true);
		}
	}
	
	@Override
	public void setSettings() {
		Global global = config.getProject().getGlobal();
		
		if (importLanguageRadioDe.isSelected()) {
			global.setLanguage(LanguageType.DE);
		}
		else if (importLanguageRadioEn.isSelected()) {
			global.setLanguage(LanguageType.EN);
		}
		
		mainView.doTranslation();
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.general.language");
	}
}
