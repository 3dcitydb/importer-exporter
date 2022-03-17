/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.importer.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.XMLValidation;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

public class XMLValidationPanel extends InternalPreferencesComponent {
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
		XMLValidation xmlValidation = config.getImportConfig().getCityGMLOptions().getXMLValidation();
		
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
	public void switchLocale(Locale locale) {
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
		XMLValidation xmlValidation = config.getImportConfig().getCityGMLOptions().getXMLValidation();

		useXMLValidation.setSelected(xmlValidation.isSetUseXMLValidation());
		oneError.setSelected(xmlValidation.isSetReportOneErrorPerFeature());

		setEnabledValidation();
	}

	@Override
	public void setSettings() {
		XMLValidation xmlValidation = config.getImportConfig().getCityGMLOptions().getXMLValidation();

		xmlValidation.setUseXMLValidation(useXMLValidation.isSelected());
		xmlValidation.setReportOneErrorPerFeature(oneError.isSelected());
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.import.xmlValidation");
	}

}
