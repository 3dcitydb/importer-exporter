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
