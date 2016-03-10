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
package org.citydb.modules.citygml.importer.gui.preferences;

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
import org.citydb.config.project.importer.XMLValidation;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class XMLValidationPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JCheckBox useXMLValidation;
	private JLabel useXMLValidationDescr;
	private JCheckBox oneError;
	
	public XMLValidationPanel(Config config) {
		super(config);
		initGui();
	}
	
	@Override
	public boolean isModified() {
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		
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
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			useXMLValidation.setIconTextGap(10);
			useXMLValidationDescr.setFont(useXMLValidationDescr.getFont().deriveFont(Font.ITALIC));
			int lmargin = (int)(useXMLValidation.getPreferredSize().getWidth()) + 11;
			oneError.setIconTextGap(10);
			{
				block1.add(useXMLValidation, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(useXMLValidationDescr, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,lmargin,5,5));		
				block1.add(oneError, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.xmlValidation.border.import"));	
		useXMLValidation.setText(Language.I18N.getString("pref.import.xmlValidation.label.useXMLValidation"));
		useXMLValidationDescr.setText(Language.I18N.getString("pref.import.xmlValidation.label.useXMLValidation.description"));
		oneError.setText(Language.I18N.getString("pref.import.xmlValidation.label.oneError"));
	}

	@Override
	public void loadSettings() {
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();

		useXMLValidation.setSelected(xmlValidation.isSetUseXMLValidation());
		oneError.setSelected(xmlValidation.isSetReportOneErrorPerFeature());	
	}

	@Override
	public void setSettings() {
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();

		xmlValidation.setUseXMLValidation(useXMLValidation.isSelected());
		xmlValidation.setReportOneErrorPerFeature(oneError.isSelected());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.xmlValidation");
	}

}
