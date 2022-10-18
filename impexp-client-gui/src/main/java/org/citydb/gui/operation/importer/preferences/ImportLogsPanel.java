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
import org.citydb.config.project.importer.DuplicateLog;
import org.citydb.config.project.importer.ImportLog;
import org.citydb.config.project.importer.ImportLogFileMode;
import org.citydb.core.util.CoreConstants;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.plugin.internal.InternalPreferencesComponent;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Locale;

public class ImportLogsPanel extends InternalPreferencesComponent {
	private TitledPanel importLogPanel;
	private JCheckBox logFeatures;
	private JLabel featureLogFileLabel;
	private JTextField featureLogFile;
	private JButton featureBrowseButton;
	private JCheckBox truncateFeatureLogFile;
	private JCheckBox uniqueFeatureLogFileName;

	private TitledPanel duplicateLogPanel;
	private JCheckBox logDuplicates;
	private JLabel duplicateLogFileLabel;
	private JTextField duplicateLogFile;
	private JButton duplicateBrowseButton;
	
	public ImportLogsPanel(Config config) {
		super(config);
		initGui();
	}

	@Override
	public boolean isModified() {
		ImportLog featureLog = config.getImportConfig().getImportLog();
		if (logFeatures.isSelected() != featureLog.isSetLogImportedFeatures()) return true;
		if (!featureLogFile.getText().equals(featureLog.getLogFile())) return true;
		if (truncateFeatureLogFile.isSelected() != (featureLog.getLogFileMode() == ImportLogFileMode.TRUNCATE)) return true;
		if (uniqueFeatureLogFileName.isSelected() != (featureLog.getLogFileMode() == ImportLogFileMode.UNIQUE)) return true;

		DuplicateLog duplicateLog = config.getImportConfig().getDuplicateLog();
		if (logDuplicates.isSelected() != duplicateLog.isSetLogDuplicates()) return true;
		if (!duplicateLogFile.getText().equals(duplicateLog.getLogFile())) return true;

		return false;
	}

	private void initGui() {
		logFeatures = new JCheckBox();
		featureLogFileLabel = new JLabel();
		featureLogFile = new JTextField();
		featureBrowseButton = new JButton();
		truncateFeatureLogFile = new JCheckBox();
		uniqueFeatureLogFileName = new JCheckBox();

		logDuplicates = new JCheckBox();
		duplicateLogFileLabel = new JLabel();
		duplicateLogFile = new JTextField();
		duplicateBrowseButton = new JButton();

		PopupMenuDecorator.getInstance().decorate(featureLogFile, duplicateLogFile);
		
		featureBrowseButton.addActionListener(e -> {
			String dir = browseFile(Language.I18N.getString("pref.tree.import.logs"), featureLogFile.getText(),
					JFileChooser.FILES_ONLY);
			if (!dir.isEmpty()) {
				featureLogFile.setText(dir);
			}
		});

		truncateFeatureLogFile.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				uniqueFeatureLogFileName.setSelected(false);
			}
		});

		uniqueFeatureLogFileName.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				truncateFeatureLogFile.setSelected(false);
			}
		});

		duplicateBrowseButton.addActionListener(e -> {
			String dir = browseFile(Language.I18N.getString("pref.tree.import.logs"), duplicateLogFile.getText(),
					JFileChooser.FILES_AND_DIRECTORIES);
			if (!dir.isEmpty()) {
				duplicateLogFile.setText(dir);
			}
		});

		setLayout(new GridBagLayout());
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				featureLogFile.setPreferredSize(new Dimension(0, 0));
				content.add(featureLogFileLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(featureLogFile, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(featureBrowseButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
				content.add(truncateFeatureLogFile, GuiUtil.setConstraints(0, 1, 3, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
				content.add(uniqueFeatureLogFileName, GuiUtil.setConstraints(0, 2, 3, 1, 0, 0, GridBagConstraints.BOTH, 5, 0, 0, 0));
			}

			importLogPanel = new TitledPanel()
					.withToggleButton(logFeatures)
					.build(content);
		}
		{
			JPanel content = new JPanel();
			content.setLayout(new GridBagLayout());
			{
				duplicateLogFile.setPreferredSize(new Dimension(0, 0));
				content.add(duplicateLogFileLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
				content.add(duplicateLogFile, GuiUtil.setConstraints(1, 0, 1, 1, GridBagConstraints.BOTH, 0, 5, 0, 5));
				content.add(duplicateBrowseButton, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
			}

			duplicateLogPanel = new TitledPanel()
					.withToggleButton(logDuplicates)
					.build(content);
		}

		add(importLogPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));
		add(duplicateLogPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 0));

		logFeatures.addActionListener(e -> setEnabledFeatureLog());
		logDuplicates.addActionListener(e -> setEnabledDuplicateLog());
	}
	
	private void setEnabledFeatureLog() {
		featureLogFileLabel.setEnabled(logFeatures.isSelected());
		featureLogFile.setEnabled(logFeatures.isSelected());
		featureBrowseButton.setEnabled(logFeatures.isSelected());
		truncateFeatureLogFile.setEnabled(logFeatures.isSelected());
		uniqueFeatureLogFileName.setEnabled(logFeatures.isSelected());
	}

	private void setEnabledDuplicateLog() {
		duplicateLogFileLabel.setEnabled(logDuplicates.isSelected());
		duplicateLogFile.setEnabled(logDuplicates.isSelected());
		duplicateBrowseButton.setEnabled(logDuplicates.isSelected());
	}
	
	@Override
	public void switchLocale(Locale locale) {
		importLogPanel.setTitle(Language.I18N.getString("pref.import.log.feature.label.useLog"));
		featureLogFileLabel.setText(Language.I18N.getString("pref.import.log.feature.label.logFile"));
		featureBrowseButton.setText(Language.I18N.getString("common.button.browse"));
		truncateFeatureLogFile.setText(Language.I18N.getString("pref.import.log.feature.label.truncate"));
		uniqueFeatureLogFileName.setText(Language.I18N.getString("pref.import.log.feature.label.unique"));
		duplicateLogPanel.setTitle(Language.I18N.getString("pref.import.log.duplicate.label.useLog"));
		duplicateLogFileLabel.setText(Language.I18N.getString("pref.import.log.duplicate.label.logFile"));
		duplicateBrowseButton.setText(Language.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		ImportLog featureLog = config.getImportConfig().getImportLog();
		logFeatures.setSelected(featureLog.isSetLogImportedFeatures());

		if (featureLog.isSetLogFile()) {
			featureLogFile.setText(featureLog.getLogFile());
		} else {
			String defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR).toString();
			featureLogFile.setText(defaultDir);
			featureLog.setLogFile(defaultDir);
		}

		if (featureLog.getLogFileMode() == ImportLogFileMode.UNIQUE) {
			uniqueFeatureLogFileName.setSelected(true);
		} else {
			truncateFeatureLogFile.setSelected(featureLog.getLogFileMode() == ImportLogFileMode.TRUNCATE);
		}

		DuplicateLog duplicateLog = config.getImportConfig().getDuplicateLog();
		logDuplicates.setSelected(duplicateLog.isSetLogDuplicates());

		if (duplicateLog.isSetLogFile()) {
			duplicateLogFile.setText(duplicateLog.getLogFile());
		} else {
			String defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DUPLICATE_LOG_DIR).toString();
			duplicateLogFile.setText(defaultDir);
			duplicateLog.setLogFile(defaultDir);
		}

		setEnabledFeatureLog();
		setEnabledDuplicateLog();
	}

	@Override
	public void setSettings() {
		ImportLog featureLog = config.getImportConfig().getImportLog();
		featureLog.setLogImportedFeatures(logFeatures.isSelected());

		if (!featureLogFile.getText().isEmpty()) {
			featureLog.setLogFile(featureLogFile.getText());
		} else {
			String defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR).toString();
			featureLogFile.setText(defaultDir);
			featureLog.setLogFile(defaultDir);
		}

		if (uniqueFeatureLogFileName.isSelected()) {
			featureLog.setLogFileMode(ImportLogFileMode.UNIQUE);
		} else {
			featureLog.setLogFileMode(truncateFeatureLogFile.isSelected() ?
					ImportLogFileMode.TRUNCATE :
					ImportLogFileMode.APPEND);
		}

		DuplicateLog duplicateLog = config.getImportConfig().getDuplicateLog();
		duplicateLog.setLogDuplicates(logDuplicates.isSelected());

		if (!duplicateLogFile.getText().isEmpty()) {
			duplicateLog.setLogFile(duplicateLogFile.getText());
		} else {
			String defaultDir = CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DUPLICATE_LOG_DIR).toString();
			duplicateLogFile.setText(defaultDir);
			duplicateLog.setLogFile(defaultDir);
		}
	}
	
	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("pref.tree.import.logs");
	}

	private String browseFile(String title, String currentFile, int mode) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(mode);
		chooser.setCurrentDirectory(new File(currentFile));

		int result = chooser.showSaveDialog(getTopLevelAncestor());
		return result == JFileChooser.CANCEL_OPTION ? "" : chooser.getSelectedFile().toString();
	}
}
