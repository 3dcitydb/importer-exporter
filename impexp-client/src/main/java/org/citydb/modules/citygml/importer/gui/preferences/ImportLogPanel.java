/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.modules.citygml.importer.gui.preferences;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.importer.ImportLog;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.CoreConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class ImportLogPanel extends AbstractPreferencesComponent {
	private JPanel block1;
	private JCheckBox logFeatures;
	private JLabel logFileLabel;
	private JTextField logFile;
	private JButton browseButton;
	
	public ImportLogPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportLog log = config.getProject().getImporter().getImportLog();
		if (logFeatures.isSelected() != log.isSetLogImportedFeatures()) return true;
		if (!logFile.getText().equals(log.getLogFile())) return true;
		return false;
	}

	private void initGui() {
		logFeatures = new JCheckBox();
		logFileLabel = new JLabel();
		logFile = new JTextField();
		browseButton = new JButton();
		
		PopupMenuDecorator.getInstance().decorate(logFile);
		
		browseButton.addActionListener(e -> {
			String dir = browseFile(Language.I18N.getString("pref.import.log.label.useLog"), logFile.getText());
			if (!dir.isEmpty())
				logFile.setText(dir);
		});
		
		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));

			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			logFeatures.setIconTextGap(10);
			logFile.setPreferredSize(logFile.getSize());
			int lmargin = (int)(logFeatures.getPreferredSize().getWidth()) + 11;
			{
				block1.add(logFeatures, GuiUtil.setConstraints(0, 0, 3, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				block1.add(logFileLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, lmargin, 5, 5));
				block1.add(logFile, GuiUtil.setConstraints(1, 1, 1, 1, GridBagConstraints.BOTH, 0, 5, 5, 5));
				block1.add(browseButton, GuiUtil.setConstraints(2, 1, 0, 0, GridBagConstraints.BOTH, 0, 5, 5, 5));
			}
		}
		
		ActionListener cacheListener = e -> setEnabledLocalCachePath();
		logFeatures.addActionListener(cacheListener);
	}
	
	private void setEnabledLocalCachePath() {
		logFileLabel.setEnabled(logFeatures.isSelected());
		logFile.setEnabled(logFeatures.isSelected());
		browseButton.setEnabled(logFeatures.isSelected());
	}
	
	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.import.log.border"));
		logFeatures.setText(Language.I18N.getString("pref.import.log.label.useLog"));
		logFileLabel.setText(Language.I18N.getString("pref.import.log.label.logFile"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		ImportLog log = config.getProject().getImporter().getImportLog();
		logFeatures.setSelected(log.isSetLogImportedFeatures());

		if (log.isSetLogFile()) {
			logFile.setText(log.getLogFile());
		} else {
			String defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR).toString();
			logFile.setText(defaultDir);
			log.setLogFile(defaultDir);
		}
		
		setEnabledLocalCachePath();
	}

	@Override
	public void setSettings() {
		ImportLog log = config.getProject().getImporter().getImportLog();
		log.setLogImportedFeatures(logFeatures.isSelected());

		if (!logFile.getText().isEmpty()) {
			log.setLogFile(logFile.getText());
		} else {
			String defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR).toString();
			logFile.setText(defaultDir);
			log.setLogFile(defaultDir);
		}
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.import.log");
	}

	private String browseFile(String title, String currentFile) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setCurrentDirectory(new File(currentFile));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		return result == JFileChooser.CANCEL_OPTION ? "" : chooser.getSelectedFile().toString();
	}
}
