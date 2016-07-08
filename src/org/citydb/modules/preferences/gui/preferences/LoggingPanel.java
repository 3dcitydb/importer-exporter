/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.preferences.gui.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.citydb.api.log.LogLevel;
import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.config.language.Language;
import org.citydb.config.project.global.Logging;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.preferences.AbstractPreferencesComponent;
import org.citydb.log.Logger;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class LoggingPanel extends AbstractPreferencesComponent {
	private final Logger LOG = Logger.getInstance();

	private JPanel block1;
	private JPanel block2;

	private JLabel logLevelConsoleLabel;
	private JComboBox<LogLevel> logLevelConsoleCombo;
	private JCheckBox wrapTextConsole;
	private JCheckBox useLogFile;
	private JLabel logLevelFileLabel;
	private JComboBox<LogLevel> logLevelFileCombo;
	private JCheckBox useLogPath;
	private JTextField logPathText;
	private JButton logPathButton;

	private ImpExpGui topFrame;

	public LoggingPanel(Config config, ImpExpGui topFrame) {
		super(config);
		this.topFrame = topFrame;
		initGui();
	}

	@Override
	public boolean isModified() {
		Logging logging = config.getProject().getGlobal().getLogging();

		if ((LogLevel)logLevelConsoleCombo.getSelectedItem() != logging.getConsole().getLogLevel()) return true;
		if (wrapTextConsole.isSelected() != logging.getConsole().isWrapText()) return true;
		if (useLogFile.isSelected() != logging.getFile().isSet()) return true;		
		if (useLogPath.isSelected() != logging.getFile().isSetUseAlternativeLogPath()) return true;
		if (!logPathText.getText().equals(logging.getFile().getAlternativeLogPath())) return true;
		if ((LogLevel)logLevelFileCombo.getSelectedItem() != logging.getFile().getLogLevel()) return true;

		return false;
	}

	private boolean isLogFileModified() {
		Logging logging = config.getProject().getGlobal().getLogging();

		if (useLogFile.isSelected() != logging.getFile().isSet()) return true;		
		if (useLogPath.isSelected() != logging.getFile().isSetUseAlternativeLogPath()) return true;
		if (!logPathText.getText().equals(logging.getFile().getAlternativeLogPath())) return true;

		return false;
	}

	private void initGui() {
		logLevelConsoleLabel = new JLabel();
		logLevelConsoleCombo = new JComboBox<LogLevel>();
		wrapTextConsole = new JCheckBox();
		useLogFile = new JCheckBox();
		logLevelFileLabel = new JLabel();
		logLevelFileCombo = new JComboBox<LogLevel>();
		useLogPath = new JCheckBox();
		logPathText = new JTextField();
		logPathButton = new JButton();

		PopupMenuDecorator.getInstance().decorate(logPathText);
		
		logPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sExp = browseFile(Language.I18N.getString("pref.general.logging.label.useLogPath"), logPathText.getText());
				if (!sExp.isEmpty())
					logPathText.setText(sExp);
			}
		});

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			{
				GridBagConstraints wtc = GuiUtil.setConstraints(0,0,1.0,0.5,GridBagConstraints.HORIZONTAL,0,5,5,5);
				wtc.gridwidth = 2;
				wrapTextConsole.setIconTextGap(10);
				block1.add(wrapTextConsole, wtc);

				block1.add(logLevelConsoleLabel, GuiUtil.setConstraints(0,1,0.0,0.5,GridBagConstraints.HORIZONTAL,0,5,5,5));
				block1.add(logLevelConsoleCombo, GuiUtil.setConstraints(1,1,1.0,0.5,GridBagConstraints.HORIZONTAL,0,5,5,5));										
			}

			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			useLogFile.setIconTextGap(10);
			useLogPath.setIconTextGap(10);
			logPathText.setPreferredSize(logPathText.getSize());
			int lmargin = (int)(useLogPath.getPreferredSize().getWidth()) + 11;
			{
				block2.add(useLogFile, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));
				block2.add(useLogPath, GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,0,5,0,5));	
				JPanel sub2 = new JPanel();
				sub2.setLayout(new GridBagLayout());
				block2.add(sub2, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,0,5,0));
				{
					sub2.add(logPathText, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,0,lmargin,5,5));
					sub2.add(logPathButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				}
				JPanel sub1 = new JPanel();
				sub1.setLayout(new GridBagLayout());
				block2.add(sub1, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				{				
					sub1.add(logLevelFileLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					sub1.add(logLevelFileCombo, GuiUtil.setConstraints(1,0,1.0,1.0,GridBagConstraints.BOTH,0,5,5,5));										
				}
			}
		}
		
		ActionListener logFileListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledLogFile();
			}
		};
		
		useLogFile.addActionListener(logFileListener);
		useLogPath.addActionListener(logFileListener);
	}
	
	private void setEnabledLogFile() {
		useLogPath.setEnabled(useLogFile.isSelected());
		logLevelFileLabel.setEnabled(useLogFile.isSelected());
		logLevelFileCombo.setEnabled(useLogFile.isSelected());
		
		logPathText.setEnabled(useLogFile.isSelected() && useLogPath.isSelected());
		logPathButton.setEnabled(useLogFile.isSelected() && useLogPath.isSelected());
	}

	@Override
	public void doTranslation() {
		((TitledBorder)block1.getBorder()).setTitle(Language.I18N.getString("pref.general.logging.border.console"));
		wrapTextConsole.setText(Language.I18N.getString("pref.general.logging.label.wrapTextConsole"));
		logLevelConsoleLabel.setText(Language.I18N.getString("pref.general.logging.label.logLevel"));

		((TitledBorder)block2.getBorder()).setTitle(Language.I18N.getString("pref.general.logging.border.file"));
		useLogFile.setText(Language.I18N.getString("pref.general.logging.label.useLogFile"));
		logLevelFileLabel.setText(Language.I18N.getString("pref.general.logging.label.logLevel"));
		useLogPath.setText(Language.I18N.getString("pref.general.logging.label.useLogPath"));
		logPathButton.setText(Language.I18N.getString("common.button.browse"));
	}

	@Override
	public void loadSettings() {
		Logging logging = config.getProject().getGlobal().getLogging();

		wrapTextConsole.setSelected(logging.getConsole().isWrapText());
		topFrame.getConsole().setLineWrap(wrapTextConsole.isSelected());
		topFrame.getConsole().setWrapStyleWord(wrapTextConsole.isSelected());
		topFrame.getConsole().repaint();

		useLogFile.setSelected(logging.getFile().isSet());
		useLogPath.setSelected(logging.getFile().isSetUseAlternativeLogPath());
		logPathText.setText(logging.getFile().getAlternativeLogPath());

		logLevelConsoleCombo.removeAllItems();
		logLevelFileCombo.removeAllItems();		
		for (LogLevel level : LogLevel.values()) {		
			logLevelConsoleCombo.addItem(level);
			logLevelFileCombo.addItem(level);
		}

		logLevelConsoleCombo.setSelectedItem(logging.getConsole().getLogLevel());
		logLevelFileCombo.setSelectedItem(logging.getFile().getLogLevel());
		
		setEnabledLogFile();
	}

	@Override
	public void setSettings() {
		Logging logging = config.getProject().getGlobal().getLogging();
		boolean isModified = isLogFileModified();

		if (useLogPath.isSelected() && logPathText.getText().trim().length() == 0) {
			useLogPath.setSelected(false);
			setEnabledLogFile();
		}

		logging.getFile().setActive(useLogFile.isSelected());
		logging.getFile().setUseAlternativeLogPath(useLogPath.isSelected());
		logging.getFile().setAlternativeLogPath(logPathText.getText());

		LogLevel consoleLogLevel = (LogLevel)logLevelConsoleCombo.getSelectedItem();
		logging.getConsole().setLogLevel(consoleLogLevel);
		LOG.setDefaultConsoleLogLevel(consoleLogLevel);

		logging.getConsole().setWrapText(wrapTextConsole.isSelected());
		topFrame.getConsole().setLineWrap(wrapTextConsole.isSelected());
		topFrame.getConsole().setWrapStyleWord(wrapTextConsole.isSelected());
		topFrame.getConsole().repaint();

		LogLevel fileLogLevel = (LogLevel)logLevelFileCombo.getSelectedItem();
		logging.getFile().setLogLevel(fileLogLevel);
		LOG.setDefaultFileLogLevel(fileLogLevel);

		// change log file
		if (isModified && useLogFile.isSelected()) {
			String logPath = useLogPath.isSelected() ? 
					logging.getFile().getAlternativeLogPath() : Internal.DEFAULT_LOG_PATH;

					if (!logPath.equals(config.getInternal().getCurrentLogPath())) {
						boolean success = LOG.appendLogFile(logPath, true);

						if (!success) {
							useLogFile.setSelected(false);
							useLogPath.setSelected(false);
							logging.getFile().setActive(false);
							logging.getFile().setUseAlternativeLogPath(false);

							LOG.detachLogFile();
						} else
							config.getInternal().setCurrentLogPath(logPath);
					}
		} else if (isModified && !useLogFile.isSelected()) {
			LOG.detachLogFile();
			config.getInternal().setCurrentLogPath("");
		}
	}
	
	@Override
	public String getTitle() {
		return Language.I18N.getString("pref.tree.general.logging");
	}

	private String browseFile(String title, String oldDir) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(oldDir));

		int result = chooser.showSaveDialog(this);
		if (result == JFileChooser.CANCEL_OPTION) return "";
		String browseString = chooser.getSelectedFile().toString();
		return browseString;
	}
}
