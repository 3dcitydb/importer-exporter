/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.importer.CodeSpaceMode;
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
	private JPanel block2;
	private JRadioButton impIdCSRadioFile;
	private JRadioButton impIdCSRadioFilePath;
	private JRadioButton impIdCSRadioUser;
	private JTextField impIdCSUserText;

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
		if (!impIdCSUserText.getText().equals(gmlId.getCodeSpace())) return true;
		if (impIdCSRadioFile.isSelected() != gmlId.isSetRelativeCodeSpaceMode()) return true;
		if (impIdCSRadioFilePath.isSelected() != gmlId.isSetAbsoluteCodeSpaceMode()) return true;
		if (impIdCSRadioUser.isSelected() != gmlId.isSetUserCodeSpaceMode()) return true;
		return false;
	}

	private void initGui(){
		impIdRadioAdd = new JRadioButton();
		impIdRadioExchange = new JRadioButton();
		ButtonGroup impIdRadio = new ButtonGroup();
		impIdRadio.add(impIdRadioAdd);
		impIdRadio.add(impIdRadioExchange);
		impIdCheckExtRef = new JCheckBox();
		
		impIdCSRadioFile = new JRadioButton();
		impIdCSRadioFilePath = new JRadioButton();
		impIdCSRadioUser = new JRadioButton();
		ButtonGroup impIdCSRadio = new ButtonGroup();
		impIdCSRadio.add(impIdCSRadioFile);
		impIdCSRadio.add(impIdCSRadioFilePath);
		impIdCSRadio.add(impIdCSRadioUser);
		impIdCSUserText = new JTextField();
		
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
			
			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			impIdCSRadioFile.setIconTextGap(10);
			impIdCSRadioFilePath.setIconTextGap(10);
			impIdCSRadioUser.setIconTextGap(10);
			{
				block2.add(impIdCSRadioFile, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioFilePath, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSRadioUser, GuiUtil.setConstraints(0,2,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(impIdCSUserText, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
			}
		}
		
		ActionListener gmlIdListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledGmlId();
			}
		};
		
		ActionListener codeSpaceListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledCodeSpace();
			}
		};
		
		impIdRadioAdd.addActionListener(gmlIdListener);
		impIdRadioExchange.addActionListener(gmlIdListener);
		
		impIdCSRadioFile.addActionListener(codeSpaceListener);
		impIdCSRadioFilePath.addActionListener(codeSpaceListener);
		impIdCSRadioUser.addActionListener(codeSpaceListener);
	}
	
	private void setEnabledGmlId() {
		impIdCheckExtRef.setEnabled(impIdRadioExchange.isSelected());
	}

	private void setEnabledCodeSpace() {
		impIdCSUserText.setEnabled(impIdCSRadioUser.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.idHandling.border.id"));	
		impIdCheckExtRef.setText(Language.I18N.getString("pref.import.idHandling.label.id.extReference"));
		impIdRadioAdd.setText(Language.I18N.getString("pref.import.idHandling.label.id.add"));
		impIdRadioExchange.setText(Language.I18N.getString("pref.import.idHandling.label.id.exchange"));
		
		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.import.idHandling.border.idCodeSpace"));	
		impIdCSRadioFile.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.file"));
		impIdCSRadioFilePath.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.filePath"));
		impIdCSRadioUser.setText(Language.I18N.getString("pref.import.idHandling.label.idCodeSpace.user"));
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
		
		if (gmlId.isSetUserCodeSpaceMode())
			impIdCSRadioUser.setSelected(true);
		else if (gmlId.isSetRelativeCodeSpaceMode())
			impIdCSRadioFile.setSelected(true);
		else
			impIdCSRadioFilePath.setSelected(true);

		impIdCSUserText.setText(gmlId.getCodeSpace());
		
		setEnabledCodeSpace();
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
		
		if (impIdCSRadioFile.isSelected()) {
			gmlId.setCodeSpaceMode(CodeSpaceMode.RELATIVE);
		}
		else if (impIdCSRadioFilePath.isSelected()) {
			gmlId.setCodeSpaceMode(CodeSpaceMode.ABSOLUTE);
		}
		else {
			gmlId.setCodeSpaceMode(CodeSpaceMode.USER);
		}
		gmlId.setCodeSpace(impIdCSUserText.getText());
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.idHandling");
	}

}

