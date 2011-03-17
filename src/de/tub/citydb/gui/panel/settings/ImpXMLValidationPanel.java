/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.gui.panel.settings;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.LocalXMLSchemaType;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.gui.util.GuiUtil;

@SuppressWarnings("serial")
public class ImpXMLValidationPanel extends PrefPanelBase {
	private JPanel block1;
	private JPanel block2;
	private JPanel block3;
	private JCheckBox useXMLValidation;
	private JLabel useXMLValidationDescr;
	private JRadioButton remoteSchemaRadio;
	private JRadioButton localSchemaRadio;
	private JCheckBox[] localSchemas;
	private JCheckBox oneError;
	
	public ImpXMLValidationPanel(Config config) {
		super(config);
		initGui();
	}
	
	@Override
	public boolean isModified() {
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		
		if (useXMLValidation.isSelected() != xmlValidation.isSetUseXMLValidation()) return true;
		if (remoteSchemaRadio.isSelected() && xmlValidation.getUseLocalSchemas().isSet()) return true;
		if (localSchemaRadio.isSelected() && !xmlValidation.getUseLocalSchemas().isSet()) return true;
		
		Set<LocalXMLSchemaType> schemas = xmlValidation.getUseLocalSchemas().getSchemas();
		for (JCheckBox schema : localSchemas) 
			if (schema.isSelected() != schemas.contains(LocalXMLSchemaType.fromValue(schema.getText()))) return true;
		
		if (oneError.isSelected() != xmlValidation.isSetReportOneErrorPerFeature()) return true;
		
		return false;
	}
	
	private void initGui() {
		useXMLValidation = new JCheckBox("");
		useXMLValidationDescr = new JLabel("");
		remoteSchemaRadio = new JRadioButton("");
		localSchemaRadio = new JRadioButton("");
		ButtonGroup schemas = new ButtonGroup();
		schemas.add(remoteSchemaRadio);
		schemas.add(localSchemaRadio);
		localSchemas = new JCheckBox[LocalXMLSchemaType.values().length];
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
			{
				block1.add(useXMLValidation, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(useXMLValidationDescr, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,lmargin,5,5));		
			}
			
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			remoteSchemaRadio.setIconTextGap(10);
			localSchemaRadio.setIconTextGap(10);
			{
				block2.add(remoteSchemaRadio, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(localSchemaRadio, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				int i = 0;
				for (LocalXMLSchemaType type : LocalXMLSchemaType.values()) {
					localSchemas[i] = new JCheckBox(type.value());
					localSchemas[i].setIconTextGap(10);
					lmargin = (int)(localSchemaRadio.getPreferredSize().getWidth()) + 11;
					block2.add(localSchemas[i], GuiUtil.setConstraints(0,++i+1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,0,5));					
				}
			}
			
			block3 = new JPanel();
			add(block3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block3.setBorder(BorderFactory.createTitledBorder(""));
			block3.setLayout(new GridBagLayout());
			oneError.setIconTextGap(10);
			{
				block3.add(oneError, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
			}
		}
	}
	
	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.xmlValidation.border.import")));
		useXMLValidation.setText(Internal.I18N.getString("pref.import.xmlValidation.label.useXMLValidation"));
		useXMLValidationDescr.setText(Internal.I18N.getString("pref.import.xmlValidation.label.useXMLValidation.description"));

		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.xmlValidation.border.schemas")));	
		remoteSchemaRadio.setText(Internal.I18N.getString("pref.import.xmlValidation.label.schemas.remote"));
		localSchemaRadio.setText(Internal.I18N.getString("pref.import.xmlValidation.label.schemas.local"));
		
		block3.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.import.xmlValidation.border.options")));
		oneError.setText(Internal.I18N.getString("pref.import.xmlValidation.label.oneError"));
	}

	@Override
	public void loadSettings() {
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();

		useXMLValidation.setSelected(xmlValidation.isSetUseXMLValidation());
		
		if (!xmlValidation.getUseLocalSchemas().isSet())
			remoteSchemaRadio.setSelected(true);
		else 
			localSchemaRadio.setSelected(true);
		
		Set<LocalXMLSchemaType> schemas = xmlValidation.getUseLocalSchemas().getSchemas();
		if (schemas.isEmpty() && xmlValidation.getUseLocalSchemas().isSet())
			for (LocalXMLSchemaType schema : LocalXMLSchemaType.values())
				schemas.add(schema);
		
		for (JCheckBox schema : localSchemas) 
			schema.setSelected(schemas.contains(LocalXMLSchemaType.fromValue(schema.getText())));
		
		oneError.setSelected(xmlValidation.isSetReportOneErrorPerFeature());	
	}

	@Override
	public void setSettings() {
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();

		xmlValidation.setUseXMLValidation(useXMLValidation.isSelected());
		
		if (localSchemaRadio.isSelected())
			xmlValidation.getUseLocalSchemas().setActive(true);
		else
			xmlValidation.getUseLocalSchemas().setActive(false);
		
		Set<LocalXMLSchemaType> schemas = xmlValidation.getUseLocalSchemas().getSchemas();
		for (JCheckBox schema : localSchemas) {
			LocalXMLSchemaType type = LocalXMLSchemaType.fromValue(schema.getText());			
			if (schema.isSelected())
				schemas.add(type);
			else
				schemas.remove(type);
		}
		
		if (schemas.isEmpty() && localSchemaRadio.isSelected()) {
			LocalXMLSchemaType defaultSchema = LocalXMLSchemaType.CityGML_v1_0_0;
			schemas.add(defaultSchema);
			for (JCheckBox schema : localSchemas) {
				if (LocalXMLSchemaType.fromValue(schema.getText()) == defaultSchema) {
					schema.setSelected(true);
					break;
				}
			}
		}
		
		xmlValidation.setReportOneErrorPerFeature(oneError.isSelected());
	}

}
