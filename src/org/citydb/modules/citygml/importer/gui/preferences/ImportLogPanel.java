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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.config.language.Language;
import org.citydb.config.project.importer.ImportLog;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class ImportLogPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JCheckBox logFeatures;
	private JTextField logPath;
	private JButton browseButton;
	
	public ImportLogPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportLog log = config.getProject().getImporter().getImportLog();
		
		if (logFeatures.isSelected() != log.isSetLogImportedFeatures()) return true;
		if (!logPath.getText().equals(log.getLogPath())) return true;
		return false;
	}

	private void initGui() {
		logFeatures = new JCheckBox();
		logPath = new JTextField();
		browseButton = new JButton();
		
		PopupMenuDecorator.getInstance().decorate(logPath);
		
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dir = browseFile(Language.I18N.getString("pref.import.log.label.useLog"), logPath.getText());
				if (!dir.isEmpty())
					logPath.setText(dir);
			}
		});
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));

			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			logFeatures.setIconTextGap(10);
			logPath.setPreferredSize(logPath.getSize());
			int lmargin = (int)(logFeatures.getPreferredSize().getWidth()) + 11;
			{
				block1.add(logFeatures, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block1.add(logPath, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
				block1.add(browseButton, GuiUtil.setConstraints(1,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}
		}
		
		ActionListener cacheListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledLocalCachePath();
			}
		};
		
		logFeatures.addActionListener(cacheListener);
	}
	
	private void setEnabledLocalCachePath() {
		logPath.setEnabled(logFeatures.isSelected());
		browseButton.setEnabled(logFeatures.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.log.border"));
		logFeatures.setText(Language.I18N.getString("pref.import.log.label.useLog"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));		
	}

	@Override
	public void loadSettings() {
		ImportLog log = config.getProject().getImporter().getImportLog();
		
		logFeatures.setSelected(log.isSetLogImportedFeatures());
		if (log.isSetLogPath())
			logPath.setText(log.getLogPath());
		else {
			logPath.setText(Internal.DEFAULT_IMPORT_LOG_PATH);
			log.setLogPath(Internal.DEFAULT_IMPORT_LOG_PATH);
		}
		
		setEnabledLocalCachePath();
	}

	@Override
	public void setSettings() {
		ImportLog log = config.getProject().getImporter().getImportLog();

		log.setLogImportedFeatures(logFeatures.isSelected());
		if (!logPath.getText().isEmpty())
			log.setLogPath(logPath.getText());
		else {
			logPath.setText(Internal.DEFAULT_IMPORT_LOG_PATH);
			log.setLogPath(Internal.DEFAULT_IMPORT_LOG_PATH);
		}
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.log");
	}

	private String browseFile(String title, String oldDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(oldDir));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		if (result == JFileChooser.CANCEL_OPTION) return "";
		String browseString = chooser.getSelectedFile().toString();
		return browseString;
	}
}
