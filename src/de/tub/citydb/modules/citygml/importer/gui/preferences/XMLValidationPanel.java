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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.util.gui.GuiUtil;

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
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.import.xmlValidation.border.import"));	
		useXMLValidation.setText(Internal.I18N.getString("pref.import.xmlValidation.label.useXMLValidation"));
		useXMLValidationDescr.setText(Internal.I18N.getString("pref.import.xmlValidation.label.useXMLValidation.description"));
		oneError.setText(Internal.I18N.getString("pref.import.xmlValidation.label.oneError"));
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
		return Internal.I18N.getString("pref.tree.import.xmlValidation");
	}

}
