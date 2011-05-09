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
package de.tub.citydb.gui.panel.matching;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.controller.Matcher;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEnum;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.StandardEditingPopupMenuDecorator;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

@SuppressWarnings("serial")
public class MatchingPanel extends JPanel implements PropertyChangeListener, EventListener {	
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final Config config;
	private final ImpExpGui topFrame;
	
	private JComboBox masterLODCombo;
	private JComboBox candLODCombo;
	private JTextField matchLineageText;
	private JButton matchButton;
	private JButton overlapButton;

	private JFormattedTextField candOverlapText;
	private JFormattedTextField masterOverlapText;
	private JFormattedTextField matchToleranceText;

	private JComboBox masterMergeLODCombo;
	private JComboBox candMergeLODCombo;
	private JTextField mergeLineageText;
	private JButton mergeButton;
	private JLabel warningLabel;

	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JTextField workspaceText;
	private JFormattedTextField timestampText;

	private JTextField deleteLinageText;
	private JButton deleteButton;

	private JPanel workspacePanel;
	private JPanel warning;
	private JPanel refPanel;
	private JPanel candPanel;
	private JPanel matchPanel;
	private JPanel mergePanel;
	private JPanel tools;

	private JLabel masterLODLabel;
	private JLabel candLODLabel;
	private JLabel matchLineageLabel;
	private JLabel candOverlapLabel;
	private JLabel masterOverlapLabel;
	
	private JLabel matchToleranceLabel;
	private JLabel masterMergeLODLabel;
	private JLabel candMergeLODLabel;
	private JLabel mergeLineageLabel;
	private JLabel mergePrefs;	
	private JLabel deleteLineageLabel;
	private JLabel settingsAText;
	private JLabel settingsBText;

	public MatchingPanel(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;

		config.getProject().getMatching().getMergeConfig().addPropertyChangeListener(this);
		initGui();
	}

	private void initGui() {
		masterLODCombo = new JComboBox();
		candLODCombo = new JComboBox();
		matchLineageText = new JTextField();
		matchButton = new JButton();
		overlapButton = new JButton();

		DecimalFormat percentFormat = new DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));	
		candOverlapText = new JFormattedTextField(percentFormat);
		masterOverlapText = new JFormattedTextField(percentFormat);
		candOverlapText.setColumns(3);
		masterOverlapText.setColumns(3);
		matchToleranceText = new JFormattedTextField(new DecimalFormat("###.##########", DecimalFormatSymbols.getInstance(Locale.ENGLISH)));
		matchToleranceText.setColumns(14);
		warningLabel = new JLabel();

		masterMergeLODCombo = new JComboBox();
		candMergeLODCombo = new JComboBox();
		mergeLineageText = new JTextField();
		mergeButton = new JButton();

		deleteLinageText = new JTextField();
		deleteButton = new JButton();

		workspaceText = new JTextField();
		timestampText = new JFormattedTextField(new SimpleDateFormat("dd.MM.yyyy"));
		timestampText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		timestampText.setColumns(10);
		workspaceLabel = new JLabel();
		timestampLabel = new JLabel();

		StandardEditingPopupMenuDecorator.decorate(matchLineageText, candOverlapText, masterOverlapText, matchToleranceText,
				mergeLineageText, deleteLinageText, workspaceText, timestampText);
		
		for (int lod = 1; lod < 5; lod++) {
			masterLODCombo.addItem("LOD " + lod);
			candLODCombo.addItem("LOD " + lod);
			masterMergeLODCombo.addItem("LOD " + lod);
			candMergeLODCombo.addItem("LOD " + lod);			
		}
		
		candOverlapText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkValue(candOverlapText);
			}
		});

		masterOverlapText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				checkValue(masterOverlapText);
			}
		});
		
		matchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						match();
					}
				};
				thread.start();
			}
		});
		
		mergeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						merge();
					}
				};
				thread.start();
			}
		});
		
		overlapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						calcRelevantMatches();
					}
				};
				thread.start();
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						delete();
					}
				};
				thread.start();
			}
		});

		setLayout(new GridBagLayout());
		{
			workspacePanel = new JPanel();
			add(workspacePanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,10,5,5,5));
			workspacePanel.setBorder(BorderFactory.createTitledBorder(""));
			workspacePanel.setLayout(new GridBagLayout());
			{
				workspacePanel.add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				workspacePanel.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				workspacePanel.add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
				workspacePanel.add(timestampText, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				timestampText.setMinimumSize(timestampText.getPreferredSize());
			}

			JPanel buildings = new JPanel();
			add(buildings, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			buildings.setBorder(BorderFactory.createEmptyBorder());
			buildings.setLayout(new GridBagLayout());
			{
				masterLODLabel = new JLabel();
				candLODLabel = new JLabel();
				candOverlapLabel = new JLabel();
				masterOverlapLabel = new JLabel();
				masterMergeLODLabel = new JLabel();
				candMergeLODLabel = new JLabel();
				
				candPanel = new JPanel();
				buildings.add(candPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				candPanel.setBorder(BorderFactory.createTitledBorder(""));
				candPanel.setLayout(new GridBagLayout());
				{
					candPanel.add(candLODLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candLODCombo, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candOverlapLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candOverlapText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candMergeLODLabel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candMergeLODCombo, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));					
				}
				
				refPanel = new JPanel();
				buildings.add(refPanel, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				refPanel.setBorder(BorderFactory.createTitledBorder(""));
				refPanel.setLayout(new GridBagLayout());
				{
					refPanel.add(masterLODLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterLODCombo, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterOverlapLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterOverlapText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterMergeLODLabel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterMergeLODCombo, GuiUtil.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				}
			}
			
			matchPanel = new JPanel();
			add(matchPanel, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			matchPanel.setBorder(BorderFactory.createTitledBorder(""));
			matchPanel.setLayout(new GridBagLayout());
			matchToleranceLabel = new JLabel();
			matchLineageLabel = new JLabel();
			{
				matchPanel.add(matchToleranceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				GridBagConstraints c = GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;			
				matchPanel.add(matchToleranceText, c);
				matchPanel.add(matchLineageLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				matchPanel.add(matchLineageText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				matchToleranceText.setMinimumSize(matchToleranceText.getPreferredSize());

				warning = new JPanel();
				warning.setBorder(BorderFactory.createEtchedBorder());
				warning.setBackground(new Color(255, 255, 255));
				warning.setLayout(new GridBagLayout());
				c = GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5);
				c.gridwidth = 2;
				matchPanel.add(warning, c);				
				{
					warning.add(warningLabel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				}
				
				JPanel buttons = new JPanel();
				c = GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5);
				c.gridwidth = 2;
				matchPanel.add(buttons, c);	
				buttons.setLayout(new GridBagLayout());
				{
					c = GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.NONE,5,5,5,5);
					c.gridwidth = 2;
					buttons.add(matchButton, c);				

					c = GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,5,5,5,0);
					c.anchor = GridBagConstraints.EAST;
					buttons.add(overlapButton, c);
				}
			}

			mergePanel = new JPanel();
			add(mergePanel, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			mergePanel.setBorder(BorderFactory.createTitledBorder(""));
			mergePanel.setLayout(new GridBagLayout());
			mergeLineageLabel = new JLabel();
			mergePrefs = new JLabel();
			{						
				mergePanel.add(mergePrefs, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,0,5));

				JPanel settings = new JPanel();
				settings.setBorder(BorderFactory.createEtchedBorder());
				settings.setBackground(new Color(255, 255, 255));
				settings.setLayout(new GridBagLayout());

				GridBagConstraints c = GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,10,5);
				c.anchor = GridBagConstraints.WEST;
				c.gridwidth = 2;
				mergePanel.add(settings, c);

				settingsAText = new JLabel();
				settingsBText = new JLabel();
				{
					settings.add(settingsAText, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					settings.add(settingsBText, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				}

				mergePanel.add(mergeLineageLabel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				mergePanel.add(mergeLineageText, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

				c = new GridBagConstraints();
				c = GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,10,5,5,5);
				c.gridwidth = 2;
				mergePanel.add(mergeButton,c);
			}

			tools = new JPanel();
			add(tools, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			tools.setBorder(BorderFactory.createTitledBorder(""));
			tools.setLayout(new GridBagLayout());
			deleteLineageLabel = new JLabel();
			{
				tools.add(deleteLineageLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				tools.add(deleteLinageText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				tools.add(deleteButton, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
			}
		}

		JPanel panel = new JPanel();
		add(panel, GuiUtil.setConstraints(0,5,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));

		setEnabledLineage();
		setEnabledButtons(false);
	}

	public void doTranslation() {
		workspacePanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("common.border.versioning")));
		workspaceLabel.setText(Internal.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Internal.I18N.getString("common.label.timestamp"));

		refPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.reference.building.border")));
		candPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.candidate.building.border")));	
		masterLODLabel.setText(Internal.I18N.getString("match.common.lod"));
		candLODLabel.setText(Internal.I18N.getString("match.common.lod"));
		candOverlapLabel.setText(Internal.I18N.getString("match.reference.overlap"));
		masterOverlapLabel.setText(Internal.I18N.getString("match.candidate.overlap"));		
		masterMergeLODLabel.setText(Internal.I18N.getString("match.reference.merge.lod"));
		candMergeLODLabel.setText(Internal.I18N.getString("match.candidate.merge.lod"));
				
		matchPanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.match.border")));
		matchToleranceLabel.setText(Internal.I18N.getString("match.match.tolerance.label"));
		matchLineageLabel.setText(Internal.I18N.getString("match.match.lineage"));
		warningLabel.setText(Internal.I18N.getString("match.match.warning.label"));
		matchButton.setText(Internal.I18N.getString("match.match.button"));
		overlapButton.setText(Internal.I18N.getString("match.match.button.overlap"));

		mergePanel.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.merge.border")));
		mergeLineageLabel.setText(Internal.I18N.getString("match.merge.lineage"));
		mergePrefs.setText(Internal.I18N.getString("match.merge.setting"));
		mergeButton.setText(Internal.I18N.getString("match.merge.button"));
				
		createSettingText();

		tools.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("match.tools.border")));
		deleteLineageLabel.setText(Internal.I18N.getString("match.tools.lineage"));
		deleteButton.setText(Internal.I18N.getString("match.tools.delete"));
	}

	private void setEnabledLineage() {
		mergeLineageLabel.setEnabled(config.getProject().getMatching().getMergeConfig().isDeleteModeRename());	
		mergeLineageText.setEnabled(config.getProject().getMatching().getMergeConfig().isDeleteModeRename());	
	}
	
	private void setEnabledButtons(boolean enable) {
		overlapButton.setEnabled(enable);
		mergeButton.setEnabled(enable);
	}

	public void createSettingText() {
		String prefixA = Internal.I18N.getString("pref.matching.master.name.short");
		String prefixB = Internal.I18N.getString("pref.matching.candidate.postMerge.short");

		if (config.getProject().getMatching().getMergeConfig().isGmlNameModeIgnore())
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Internal.I18N.getString("pref.matching.master.name.ignore") + "</b></html>");
		else if (config.getProject().getMatching().getMergeConfig().isGmlNameModeReplace())
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Internal.I18N.getString("pref.matching.master.name.replace") + "</b></html>");
		else
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Internal.I18N.getString("pref.matching.master.name.append") + "</b></html>");

		if (config.getProject().getMatching().getMergeConfig().isDeleteModeDelAll())
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Internal.I18N.getString("pref.matching.candidate.postMerge.delete") + "</b></html>");
		else if (config.getProject().getMatching().getMergeConfig().isDeleteModeRename())
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Internal.I18N.getString("pref.matching.candidate.postMerge.rename") + "</b></html>");
		else 
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Internal.I18N.getString("pref.matching.candidate.postMerge.geom") + "</b></html>");
	}

	public void checkValue(JFormattedTextField field) {
		if (((Number)field.getValue()).doubleValue() > 100.0) 
			field.setValue(new Double(100.0));
		else if (((Number)field.getValue()).doubleValue() < 0.0)
			field.setValue(new Double(0.0));
	}

	public void loadSettings() {
		workspaceText.setText(config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace().getName());
		timestampText.setText(config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace().getTimestamp());

		masterLODCombo.setSelectedIndex(config.getProject().getMatching().getMasterBuildings().getLodProjection()-1);
		masterOverlapText.setValue(config.getProject().getMatching().getMasterBuildings().getOverlap() * 100);
		masterMergeLODCombo.setSelectedIndex(config.getProject().getMatching().getMasterBuildings().getLodGeometry()-1);
		candLODCombo.setSelectedIndex(config.getProject().getMatching().getCandidateBuildings().getLodProjection()-1);	
		candOverlapText.setValue(config.getProject().getMatching().getCandidateBuildings().getOverlap() * 100);
		candMergeLODCombo.setSelectedIndex(config.getProject().getMatching().getCandidateBuildings().getLodGeometry()-1);

		matchLineageText.setText(config.getProject().getMatching().getMatchConfig().getLineage());
		matchToleranceText.setValue(new Double(config.getProject().getMatching().getMatchConfig().getTolerance()));
		mergeLineageText.setText(config.getProject().getMatching().getMergeConfig().getLineage());
		deleteLinageText.setText(config.getProject().getMatching().getDeleteBuildingsByLineage().getLineage());
	}

	public void setSettings() {
		if (workspaceText.getText().trim().length() == 0)
			workspaceText.setText("LIVE");

		config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace().setName(workspaceText.getText());
		config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace().setTimestamp(timestampText.getText());

		config.getProject().getMatching().getMasterBuildings().setLodProjection(masterLODCombo.getSelectedIndex()+1);
		config.getProject().getMatching().getMasterBuildings().setOverlap(((Number)masterOverlapText.getValue()).doubleValue() / 100);
		config.getProject().getMatching().getMasterBuildings().setLodGeometry(masterMergeLODCombo.getSelectedIndex()+1);
		config.getProject().getMatching().getCandidateBuildings().setLodProjection(candLODCombo.getSelectedIndex()+1);	
		config.getProject().getMatching().getCandidateBuildings().setOverlap(((Number)candOverlapText.getValue()).doubleValue() / 100);
		config.getProject().getMatching().getCandidateBuildings().setLodGeometry(candMergeLODCombo.getSelectedIndex()+1);

		config.getProject().getMatching().getMatchConfig().setLineage(matchLineageText.getText());
		config.getProject().getMatching().getMatchConfig().setTolerance(((Number)matchToleranceText.getValue()).doubleValue());
		config.getProject().getMatching().getMergeConfig().setLineage(mergeLineageText.getText());
		config.getProject().getMatching().getDeleteBuildingsByLineage().setLineage(deleteLinageText.getText());
	}
	
	private void match() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			topFrame.getConsoleText().setText("");
			Database db = config.getProject().getDatabase();

			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getMatchingWorkspace())) {
				topFrame.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			if (!config.getInternal().isConnected()) {
				topFrame.connectToDatabase();
				if (!config.getInternal().isConnected())
					return;
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.match.label"));
			LOG.info("Initializing matching process...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			eventDispatcher.addListener(EventType.Counter, this);
			
			final StatusDialog status = new StatusDialog(topFrame, 
					Internal.I18N.getString("match.match.dialog.window"), 
					Internal.I18N.getString("match.match.dialog.msg"),
					" ",
					Internal.I18N.getString("match.match.dialog.details"),
					true,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(topFrame);
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(topFrame.getDBPool(), config, eventDispatcher);

			status.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									InterruptEnum.USER_ABORT, 
									"User abort of matching process.", 
									LogLevelType.INFO));
						}
					});
				}
			});

			boolean success = matcher.match();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});

			if (success) {
				LOG.info("Matching process successfully finished.");
			} else {
				LOG.warn("Matching process aborted.");
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}
	
	private void merge() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			topFrame.getConsoleText().setText("");
			Database db = config.getProject().getDatabase();

			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getMatchingWorkspace())) {
				topFrame.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			if (!config.getInternal().isConnected()) {
				topFrame.connectToDatabase();
				if (!config.getInternal().isConnected())
					return;
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.merge.label"));
			LOG.info("Initializing merging process...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final StatusDialog status = new StatusDialog(topFrame, 
					Internal.I18N.getString("match.merge.dialog.window"), 
					Internal.I18N.getString("match.merge.dialog.msg"),
					" ",
					Internal.I18N.getString("match.merge.dialog.details"),
					false,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(topFrame);
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(topFrame.getDBPool(), config, eventDispatcher);
			boolean success = matcher.merge();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});

			if (success) {
				LOG.info("Merging process successfully finished.");
			} else {
				LOG.warn("Merging process aborted abnormally.");
			}

			setEnabledButtons(false);
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void calcRelevantMatches() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			topFrame.getConsoleText().setText("");

			if (!config.getInternal().isConnected()) {
				topFrame.connectToDatabase();
				if (!config.getInternal().isConnected())
					return;
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.overlap.label"));
			LOG.info("Initializing matching process...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			eventDispatcher.addListener(EventType.Counter, this);
			
			final StatusDialog status = new StatusDialog(topFrame, 
					Internal.I18N.getString("match.overlap.dialog.window"), 
					Internal.I18N.getString("match.overlap.dialog.msg"),
					" ",
					Internal.I18N.getString("match.overlap.dialog.details"),
					false,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(topFrame);
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(topFrame.getDBPool(), config, eventDispatcher);
			boolean success = matcher.calcRelevantMatches();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});

			if (success) {
				LOG.info("Matching process successfully finished.");
			} else {
				LOG.warn("Matching process aborted abnormally.");
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}
	
	private void delete() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();
			
			topFrame.getConsoleText().setText("");
			Database db = config.getProject().getDatabase();

			// workspace timestamp
			if (!Util.checkWorkspaceTimestamp(db.getWorkspaces().getMatchingWorkspace())) {
				topFrame.errorMessage(Internal.I18N.getString("export.dialog.error.incorrectData"), 
						Internal.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			if (!config.getInternal().isConnected()) {
				topFrame.connectToDatabase();

				if (!config.getInternal().isConnected())
					return;
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.delete.label"));
			LOG.info("Initializing deletion of buildings...");

			String msg = Internal.I18N.getString("match.tools.dialog.msg");
			Object[] args = new Object[]{ config.getProject().getMatching().getDeleteBuildingsByLineage().getLineage() };
			String result = MessageFormat.format(msg, args);

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = new EventDispatcher();
			final StatusDialog status = new StatusDialog(topFrame, 
					Internal.I18N.getString("match.tools.dialog.title"), 
					result,
					"",
					Internal.I18N.getString("match.tools.dialog.details"),
					false,
					eventDispatcher);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(topFrame);
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(topFrame.getDBPool(), config, eventDispatcher);
			boolean success = matcher.delete();

			try {
				eventDispatcher.shutdownAndWait();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});

			if (success) {
				LOG.info("Deletion of buildings successfully finished.");
			} else {
				LOG.warn("Deletion of buildings aborted abnormally.");
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.Counter &&
				((CounterEvent)e).getType() == CounterType.RELEVANT_MATCHES) {
			setEnabledButtons(((CounterEvent)e).getCounter() > 0);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("matchPref.deleteMode")) {
			createSettingText();
			setEnabledLineage();
		}

		else if (evt.getPropertyName().equals("matchPref.gmlNameMode")) {
			createSettingText();
		}
	}

}
