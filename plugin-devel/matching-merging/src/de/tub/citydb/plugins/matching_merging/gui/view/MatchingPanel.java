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
package de.tub.citydb.plugins.matching_merging.gui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.controller.LogController;
import de.tub.citydb.api.controller.ViewController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.plugins.matching_merging.PluginImpl;
import de.tub.citydb.plugins.matching_merging.controller.Matcher;
import de.tub.citydb.plugins.matching_merging.events.CounterEvent;
import de.tub.citydb.plugins.matching_merging.events.EventType;
import de.tub.citydb.plugins.matching_merging.events.InterruptEvent;
import de.tub.citydb.plugins.matching_merging.gui.components.StatusDialog;
import de.tub.citydb.plugins.matching_merging.util.Util;

@SuppressWarnings("serial")
public class MatchingPanel extends JPanel implements PropertyChangeListener, EventHandler {	
	private final ReentrantLock mainLock = new ReentrantLock();
	private final LogController logController;
	private final PluginImpl plugin;
	private final DatabaseController databaseController;
	private final ViewController viewController;
	private EventDispatcher eventDispatcher;

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

	public MatchingPanel(PluginImpl plugin) {
		this.plugin = plugin;

		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		viewController = ObjectRegistry.getInstance().getViewController();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		logController = ObjectRegistry.getInstance().getLogController();

		plugin.getConfig().getMerging().addPropertyChangeListener(this);
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

		viewController.getComponentFactory().createPopupMenuDecorator().decorate(
				matchLineageText, candOverlapText, masterOverlapText, matchToleranceText,
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

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		
		JPanel view = new JPanel();		
		view.setLayout(new GridBagLayout());
		{
			workspacePanel = new JPanel();
			view.add(workspacePanel, Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.HORIZONTAL,10,5,5,5));
			workspacePanel.setBorder(BorderFactory.createTitledBorder(""));
			workspacePanel.setLayout(new GridBagLayout());
			{
				workspacePanel.add(workspaceLabel, Util.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				workspacePanel.add(workspaceText, Util.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				workspacePanel.add(timestampLabel, Util.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
				workspacePanel.add(timestampText, Util.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				timestampText.setMinimumSize(timestampText.getPreferredSize());
			}

			JPanel buildings = new JPanel();
			view.add(buildings, Util.setConstraints(0,1,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
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
				buildings.add(candPanel, Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				candPanel.setBorder(BorderFactory.createTitledBorder(""));
				candPanel.setLayout(new GridBagLayout());
				{
					candPanel.add(candLODLabel, Util.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candLODCombo, Util.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candOverlapLabel, Util.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candOverlapText, Util.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candMergeLODLabel, Util.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					candPanel.add(candMergeLODCombo, Util.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));					
				}

				refPanel = new JPanel();
				buildings.add(refPanel, Util.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
				refPanel.setBorder(BorderFactory.createTitledBorder(""));
				refPanel.setLayout(new GridBagLayout());
				{
					refPanel.add(masterLODLabel, Util.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterLODCombo, Util.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterOverlapLabel, Util.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterOverlapText, Util.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterMergeLODLabel, Util.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
					refPanel.add(masterMergeLODCombo, Util.setConstraints(1,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				}
			}

			matchPanel = new JPanel();
			view.add(matchPanel, Util.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			matchPanel.setBorder(BorderFactory.createTitledBorder(""));
			matchPanel.setLayout(new GridBagLayout());
			matchToleranceLabel = new JLabel();
			matchLineageLabel = new JLabel();
			{
				matchPanel.add(matchToleranceLabel, Util.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				GridBagConstraints c = Util.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;			
				matchPanel.add(matchToleranceText, c);
				matchPanel.add(matchLineageLabel, Util.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				matchPanel.add(matchLineageText, Util.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				matchToleranceText.setMinimumSize(matchToleranceText.getPreferredSize());

				warning = new JPanel();
				warning.setBorder(BorderFactory.createEtchedBorder());
				warning.setBackground(new Color(255, 255, 255));
				warning.setLayout(new GridBagLayout());
				c = Util.setConstraints(0,2,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5);
				c.gridwidth = 2;
				matchPanel.add(warning, c);				
				{
					warning.add(warningLabel, Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				}

				JPanel buttons = new JPanel();
				c = Util.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5);
				c.gridwidth = 2;
				matchPanel.add(buttons, c);	
				buttons.setLayout(new GridBagLayout());
				{
					c = Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.NONE,5,5,5,5);
					c.gridwidth = 2;
					buttons.add(matchButton, c);				

					c = Util.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,5,5,5,0);
					c.anchor = GridBagConstraints.EAST;
					buttons.add(overlapButton, c);
				}
			}

			mergePanel = new JPanel();
			view.add(mergePanel, Util.setConstraints(0,3,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			mergePanel.setBorder(BorderFactory.createTitledBorder(""));
			mergePanel.setLayout(new GridBagLayout());
			mergeLineageLabel = new JLabel();
			mergePrefs = new JLabel();
			{						
				mergePanel.add(mergePrefs, Util.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,0,5));

				JPanel settings = new JPanel();
				settings.setBorder(BorderFactory.createEtchedBorder());
				settings.setBackground(new Color(255, 255, 255));
				settings.setLayout(new GridBagLayout());

				GridBagConstraints c = Util.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,10,5);
				c.anchor = GridBagConstraints.WEST;
				c.gridwidth = 2;
				mergePanel.add(settings, c);

				settingsAText = new JLabel();
				settingsBText = new JLabel();
				{
					settings.add(settingsAText, Util.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
					settings.add(settingsBText, Util.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				}

				mergePanel.add(mergeLineageLabel, Util.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				mergePanel.add(mergeLineageText, Util.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

				c = new GridBagConstraints();
				c = Util.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,10,5,5,5);
				c.gridwidth = 2;
				mergePanel.add(mergeButton,c);
			}

			tools = new JPanel();
			view.add(tools, Util.setConstraints(0,4,1.0,0.0,GridBagConstraints.HORIZONTAL,5,5,5,5));
			tools.setBorder(BorderFactory.createTitledBorder(""));
			tools.setLayout(new GridBagLayout());
			deleteLineageLabel = new JLabel();
			{
				tools.add(deleteLineageLabel, Util.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				tools.add(deleteLinageText, Util.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				tools.add(deleteButton, Util.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
			}
		}

		view.add(Box.createVerticalGlue(), Util.setConstraints(0,5,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));

		scrollPane.setViewportView(view);
		setLayout(new BorderLayout());
		add(scrollPane);
		
		setEnabledLineage();
		setEnabledButtons(false);
	}

	public void switchLocale() {
		workspacePanel.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("common.border.versioning")));
		workspaceLabel.setText(Util.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Util.I18N.getString("common.label.timestamp"));

		refPanel.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("match.reference.building.border")));
		candPanel.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("match.candidate.building.border")));	
		masterLODLabel.setText(Util.I18N.getString("match.common.lod"));
		candLODLabel.setText(Util.I18N.getString("match.common.lod"));
		candOverlapLabel.setText(Util.I18N.getString("match.reference.overlap"));
		masterOverlapLabel.setText(Util.I18N.getString("match.candidate.overlap"));		
		masterMergeLODLabel.setText(Util.I18N.getString("match.reference.merge.lod"));
		candMergeLODLabel.setText(Util.I18N.getString("match.candidate.merge.lod"));

		matchPanel.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("match.match.border")));
		matchToleranceLabel.setText(Util.I18N.getString("match.match.tolerance.label"));
		matchLineageLabel.setText(Util.I18N.getString("match.match.lineage"));
		warningLabel.setText(Util.I18N.getString("match.match.warning.label"));
		matchButton.setText(Util.I18N.getString("match.match.button"));
		overlapButton.setText(Util.I18N.getString("match.match.button.overlap"));

		mergePanel.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("match.merge.border")));
		mergeLineageLabel.setText(Util.I18N.getString("match.merge.lineage"));
		mergePrefs.setText(Util.I18N.getString("match.merge.setting"));
		mergeButton.setText(Util.I18N.getString("match.merge.button"));

		createSettingText();

		tools.setBorder(BorderFactory.createTitledBorder(Util.I18N.getString("match.tools.border")));
		deleteLineageLabel.setText(Util.I18N.getString("match.tools.lineage"));
		deleteButton.setText(Util.I18N.getString("match.tools.delete"));
	}

	private void setEnabledLineage() {
		mergeLineageLabel.setEnabled(plugin.getConfig().getMerging().isDeleteModeRename());	
		mergeLineageText.setEnabled(plugin.getConfig().getMerging().isDeleteModeRename());	
	}

	private void setEnabledButtons(boolean enable) {
		overlapButton.setEnabled(enable);
		mergeButton.setEnabled(enable);
	}

	public void createSettingText() {
		String prefixA = Util.I18N.getString("pref.matching.master.name.short");
		String prefixB = Util.I18N.getString("pref.matching.candidate.postMerge.short");

		if (plugin.getConfig().getMerging().isGmlNameModeIgnore())
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Util.I18N.getString("pref.matching.master.name.ignore") + "</b></html>");
		else if (plugin.getConfig().getMerging().isGmlNameModeReplace())
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Util.I18N.getString("pref.matching.master.name.replace") + "</b></html>");
		else
			settingsAText.setText("<html>" + prefixA + ": " + "<b>" + Util.I18N.getString("pref.matching.master.name.append") + "</b></html>");

		if (plugin.getConfig().getMerging().isDeleteModeDelAll())
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Util.I18N.getString("pref.matching.candidate.postMerge.delete") + "</b></html>");
		else if (plugin.getConfig().getMerging().isDeleteModeRename())
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Util.I18N.getString("pref.matching.candidate.postMerge.rename") + "</b></html>");
		else 
			settingsBText.setText("<html>" + prefixB + ": " + "<b>" + Util.I18N.getString("pref.matching.candidate.postMerge.geom") + "</b></html>");
	}

	public void checkValue(JFormattedTextField field) {
		if (((Number)field.getValue()).doubleValue() > 100.0) 
			field.setValue(new Double(100.0));
		else if (((Number)field.getValue()).doubleValue() < 0.0)
			field.setValue(new Double(0.0));
	}

	public void loadSettings() {
		workspaceText.setText(plugin.getConfig().getWorkspace().getName());
		timestampText.setText(plugin.getConfig().getWorkspace().getTimestamp());

		masterLODCombo.setSelectedIndex(plugin.getConfig().getMasterBuildings().getLodProjection()-1);
		masterOverlapText.setValue(plugin.getConfig().getMasterBuildings().getOverlap() * 100);
		masterMergeLODCombo.setSelectedIndex(plugin.getConfig().getMasterBuildings().getLodGeometry()-1);
		candLODCombo.setSelectedIndex(plugin.getConfig().getCandidateBuildings().getLodProjection()-1);	
		candOverlapText.setValue(plugin.getConfig().getCandidateBuildings().getOverlap() * 100);
		candMergeLODCombo.setSelectedIndex(plugin.getConfig().getCandidateBuildings().getLodGeometry()-1);

		matchLineageText.setText(plugin.getConfig().getMatching().getLineage());
		matchToleranceText.setValue(new Double(plugin.getConfig().getMatching().getTolerance()));
		mergeLineageText.setText(plugin.getConfig().getMerging().getLineage());
		deleteLinageText.setText(plugin.getConfig().getDeleteBuildingsByLineage().getLineage());
	}

	public void setSettings() {
		String workspace = workspaceText.getText().trim();
		if (!workspace.equals("LIVE") && 
				(workspace.length() == 0 || workspace.toUpperCase().equals("LIVE")))
			workspaceText.setText("LIVE");

		plugin.getConfig().getWorkspace().setName(workspaceText.getText());
		plugin.getConfig().getWorkspace().setTimestamp(timestampText.getText());

		plugin.getConfig().getMasterBuildings().setLodProjection(masterLODCombo.getSelectedIndex()+1);
		plugin.getConfig().getMasterBuildings().setOverlap(((Number)masterOverlapText.getValue()).doubleValue() / 100);
		plugin.getConfig().getMasterBuildings().setLodGeometry(masterMergeLODCombo.getSelectedIndex()+1);
		plugin.getConfig().getCandidateBuildings().setLodProjection(candLODCombo.getSelectedIndex()+1);	
		plugin.getConfig().getCandidateBuildings().setOverlap(((Number)candOverlapText.getValue()).doubleValue() / 100);
		plugin.getConfig().getCandidateBuildings().setLodGeometry(candMergeLODCombo.getSelectedIndex()+1);

		plugin.getConfig().getMatching().setLineage(matchLineageText.getText());
		plugin.getConfig().getMatching().setTolerance(((Number)matchToleranceText.getValue()).doubleValue());
		plugin.getConfig().getMerging().setLineage(mergeLineageText.getText());
		plugin.getConfig().getDeleteBuildingsByLineage().setLineage(deleteLinageText.getText());
	}

	private void match() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			viewController.clearConsole();

			if (!Util.checkWorkspaceTimestamp(plugin.getConfig().getWorkspace())) {
				errorMessage(Util.I18N.getString("common.dialog.error.incorrectData"), 
						Util.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
				} catch (DatabaseConfigurationException e) {
					return;
				} catch (SQLException e) {
					return;
				}

				if (!databaseController.isConnected())
					return;
			}

			viewController.setStatusText(Util.I18N.getString("main.status.match.label"));
			logController.info("Initializing matching process...");

			// initialize event dispatcher
			eventDispatcher.addEventHandler(EventType.RELEVANT_MATCHES, this);

			final StatusDialog status = new StatusDialog(viewController.getTopFrame(), 
					Util.I18N.getString("match.match.dialog.window"), 
					Util.I18N.getString("match.match.dialog.msg"),
					" ",
					Util.I18N.getString("match.match.dialog.details"),
					true);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(viewController.getTopFrame());
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(plugin);

			status.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							eventDispatcher.triggerEvent(new InterruptEvent(
									"User abort of matching process.", 
									LogLevel.INFO, 
									this));
						}
					});
				}
			});

			boolean success = matcher.match();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});

			// cleanup
			matcher.cleanup();
			
			if (success) {
				logController.info("Matching process successfully finished.");
			} else {
				logController.warn("Matching process aborted.");
			}

			viewController.setStatusText(Util.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void merge() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			viewController.clearConsole();

			if (!Util.checkWorkspaceTimestamp(plugin.getConfig().getWorkspace())) {
				errorMessage(Util.I18N.getString("common.dialog.error.incorrectData"), 
						Util.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
				} catch (DatabaseConfigurationException e) {
					return;
				} catch (SQLException e) {
					return;
				}

				if (!databaseController.isConnected())
					return;
			}

			viewController.setStatusText(Util.I18N.getString("main.status.merge.label"));
			logController.info("Initializing merging process...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final StatusDialog status = new StatusDialog(viewController.getTopFrame(), 
					Util.I18N.getString("match.merge.dialog.window"), 
					Util.I18N.getString("match.merge.dialog.msg"),
					" ",
					Util.I18N.getString("match.merge.dialog.details"),
					false);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(viewController.getTopFrame());
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(plugin);
			boolean success = matcher.merge();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});
			
			// cleanup
			matcher.cleanup();

			if (success) {
				logController.info("Merging process successfully finished.");
			} else {
				logController.warn("Merging process aborted abnormally.");
			}

			setEnabledButtons(false);
			viewController.setStatusText(Util.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void calcRelevantMatches() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			viewController.clearConsole();

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
				} catch (DatabaseConfigurationException e) {
					return;
				} catch (SQLException e) {
					return;
				}

				if (!databaseController.isConnected())
					return;
			}

			viewController.setStatusText(Util.I18N.getString("main.status.overlap.label"));
			logController.info("Initializing matching process...");

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			eventDispatcher.addEventHandler(EventType.RELEVANT_MATCHES, this);

			final StatusDialog status = new StatusDialog(viewController.getTopFrame(), 
					Util.I18N.getString("match.overlap.dialog.window"), 
					Util.I18N.getString("match.overlap.dialog.msg"),
					" ",
					Util.I18N.getString("match.overlap.dialog.details"),
					false);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(viewController.getTopFrame());
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(plugin);
			boolean success = matcher.calcRelevantMatches();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});
			
			// cleanup
			matcher.cleanup();

			if (success) {
				logController.info("Matching process successfully finished.");
			} else {
				logController.warn("Matching process aborted abnormally.");
			}

			viewController.setStatusText(Util.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void delete() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();

			viewController.clearConsole();

			if (!Util.checkWorkspaceTimestamp(plugin.getConfig().getWorkspace())) {
				errorMessage(Util.I18N.getString("common.dialog.error.incorrectData"), 
						Util.I18N.getString("common.dialog.error.incorrectData.date"));
				return;
			}

			if (!databaseController.isConnected()) {
				try {
					databaseController.connect(true);
				} catch (DatabaseConfigurationException e) {
					return;
				} catch (SQLException e) {
					return;
				}

				if (!databaseController.isConnected())
					return;
			}

			viewController.setStatusText(Util.I18N.getString("main.status.delete.label"));
			logController.info("Initializing deletion of buildings...");

			String msg = Util.I18N.getString("match.tools.dialog.msg");
			Object[] args = new Object[]{ plugin.getConfig().getDeleteBuildingsByLineage().getLineage() };
			String result = MessageFormat.format(msg, args);

			// initialize event dispatcher
			final EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
			final StatusDialog status = new StatusDialog(viewController.getTopFrame(), 
					Util.I18N.getString("match.tools.dialog.title"), 
					result,
					"",
					Util.I18N.getString("match.tools.dialog.details"),
					false);	

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.setLocationRelativeTo(viewController.getTopFrame());
					status.setVisible(true);
				}
			});

			Matcher matcher = new Matcher(plugin);
			boolean success = matcher.delete();

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e1) {
				//
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					status.dispose();
				}
			});
			
			// cleanup
			matcher.cleanup();

			if (success) {
				logController.info("Deletion of buildings successfully finished.");
			} else {
				logController.warn("Deletion of buildings aborted abnormally.");
			}

			viewController.setStatusText(Util.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void errorMessage(String title, String text) {
		JOptionPane.showMessageDialog(viewController.getTopFrame(), text, title, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		setEnabledButtons(((CounterEvent)e).getCounter() > 0);
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
