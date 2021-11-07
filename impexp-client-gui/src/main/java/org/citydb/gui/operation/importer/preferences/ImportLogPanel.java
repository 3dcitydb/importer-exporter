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
import org.citydb.config.project.importer.ImportLog;
import org.citydb.core.util.CoreConstants;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.common.DefaultPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ImportLogPanel extends DefaultPreferencesComponent {
	private TitledPanel importLogPanel;
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
		ImportLog log = config.getImportConfig().getImportLog();
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
			String dir = browseFile(Language.I18N.getString("pref.tree.import.log"), logFile.getText());
			if (!dir.isEmpty())
				logFile.setText(dir);
		});

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				logFile.setPreferredSize(new Dimension(0, 0));
				content.add(logFileLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(logFile, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(browseButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			importLogPanel = new TitledPanel()
					.withToggleButton(logFeatures)
					.build(content);
		}

		add(importLogPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		logFeatures.addActionListener(e -> setEnabledLocalCachePath());
	}
	
	private void setEnabledLocalCachePath() {
		logFileLabel.setEnabled(logFeatures.isSelected());
		logFile.setEnabled(logFeatures.isSelected());
		browseButton.setEnabled(logFeatures.isSelected());
	}
	
	@Override
	public void doTranslation() {
		importLogPanel.setTitle(Language.I18N.getString("pref.import.log.label.useLog"));
		logFileLabel.setText(Language.I18N.getString("pref.import.log.label.logFile"));
		browseButton.setText(Language.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		ImportLog log = config.getImportConfig().getImportLog();
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
		ImportLog log = config.getImportConfig().getImportLog();
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
