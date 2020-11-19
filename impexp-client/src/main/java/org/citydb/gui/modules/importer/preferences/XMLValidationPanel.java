/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.modules.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.XMLValidation;
import org.citydb.gui.components.common.TitledPanel;
import org.citydb.gui.modules.common.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;

public class XMLValidationPanel extends AbstractPreferencesComponent {
	private TitledPanel validationPanel;
	private JCheckBox useXMLValidation;
	private JLabel useXMLValidationDescr;
	private JCheckBox oneError;
	
	public XMLValidationPanel(Config config) {
		super(config);
		initGui();
	}
	
	@Override
	public boolean isModified() {
		XMLValidation xmlValidation = config.getImportConfig().getXMLValidation();
		
		if (useXMLValidation.isSelected() != xmlValidation.isSetUseXMLValidation()) return true;		
		if (oneError.isSelected() != xmlValidation.isSetReportOneErrorPerFeature()) return true;
		
		return false;
	}
	
	private void initGui() {
		useXMLValidation = new JCheckBox("");
		useXMLValidationDescr = new JLabel("");
		oneError = new JCheckBox("");

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			useXMLValidationDescr.setFont(useXMLValidationDescr.getFont().deriveFont(Font.ITALIC));
			{
				content.add(useXMLValidationDescr, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
				content.add(oneError, GuiUtil.setConstraints(0, 2, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			validationPanel = new TitledPanel()
					.withToggleButton(useXMLValidation)
					.build(content);

			add(validationPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		}

		useXMLValidation.addActionListener(e -> setEnabledValidation());
	}
	
	@Override
	public void doTranslation() {
		validationPanel.setTitle(Language.I18N.getString("pref.import.xmlValidation.label.useXMLValidation"));
		useXMLValidationDescr.setText(Language.I18N.getString("pref.import.xmlValidation.label.useXMLValidation.description"));
		oneError.setText(Language.I18N.getString("pref.import.xmlValidation.label.oneError"));
	}

	private void setEnabledValidation() {
		useXMLValidationDescr.setEnabled(useXMLValidation.isSelected());
		oneError.setEnabled(useXMLValidation.isSelected());
	}

	@Override
	public void loadSettings() {
		XMLValidation xmlValidation = config.getImportConfig().getXMLValidation();

		useXMLValidation.setSelected(xmlValidation.isSetUseXMLValidation());
		oneError.setSelected(xmlValidation.isSetReportOneErrorPerFeature());

		setEnabledValidation();
	}

	@Override
	public void setSettings() {
		XMLValidation xmlValidation = config.getImportConfig().getXMLValidation();

		xmlValidation.setUseXMLValidation(useXMLValidation.isSelected());
		xmlValidation.setReportOneErrorPerFeature(oneError.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.xmlValidation");
	}

}
