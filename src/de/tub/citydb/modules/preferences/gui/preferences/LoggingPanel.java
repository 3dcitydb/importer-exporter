/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.preferences.gui.preferences;

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

import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.global.Logging;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class LoggingPanel extends AbstractPreferencesComponent {
	private final Logger LOG = Logger.getInstance();

	private JPanel block1;
	private JPanel block2;

	private JLabel logLevelConsoleLabel;
	private JComboBox logLevelConsoleCombo;
	private JCheckBox wrapTextConsole;
	private JCheckBox useLogFile;
	private JLabel logLevelFileLabel;
	private JComboBox logLevelFileCombo;
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
		logLevelConsoleCombo = new JComboBox();
		wrapTextConsole = new JCheckBox();
		useLogFile = new JCheckBox();
		logLevelFileLabel = new JLabel();
		logLevelFileCombo = new JComboBox();
		useLogPath = new JCheckBox();
		logPathText = new JTextField();
		logPathButton = new JButton();

		PopupMenuDecorator.getInstance().decorate(logPathText);
		
		logPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sExp = browseFile(Internal.I18N.getString("pref.general.logging.label.useLogPath"), logPathText.getText());
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
		((TitledBorder)block1.getBorder()).setTitle(Internal.I18N.getString("pref.general.logging.border.console"));
		wrapTextConsole.setText(Internal.I18N.getString("pref.general.logging.label.wrapTextConsole"));
		logLevelConsoleLabel.setText(Internal.I18N.getString("pref.general.logging.label.logLevel"));

		((TitledBorder)block2.getBorder()).setTitle(Internal.I18N.getString("pref.general.logging.border.file"));
		useLogFile.setText(Internal.I18N.getString("pref.general.logging.label.useLogFile"));
		logLevelFileLabel.setText(Internal.I18N.getString("pref.general.logging.label.logLevel"));
		useLogPath.setText(Internal.I18N.getString("pref.general.logging.label.useLogPath"));
		logPathButton.setText(Internal.I18N.getString("common.button.browse"));
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

		if (useLogPath.isSelected() && logPathText.getText().trim().length() == 0)
			useLogPath.setSelected(false);

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
					logging.getFile().getAlternativeLogPath() : 
						config.getInternal().getLogPath();

					if (!logPath.equals(config.getInternal().getCurrentLogPath())) {
						boolean success = LOG.appendLogFile(logPath);

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
		return Internal.I18N.getString("pref.tree.general.logging");
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
