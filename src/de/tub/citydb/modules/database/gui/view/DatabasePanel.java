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
package de.tub.citydb.modules.database.gui.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.citygml4j.geometry.BoundingBox;

import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.common.ApplicationEvent;
import de.tub.citydb.api.event.common.DatabaseConnectionStateEvent;
import de.tub.citydb.api.log.LogLevelType;
import de.tub.citydb.api.log.Logger;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.DBOperationMode;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.gui.factory.SrsComboBoxFactory;
import de.tub.citydb.gui.factory.SrsComboBoxFactory.SrsComboBox;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class DatabasePanel extends JPanel implements EventHandler {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final ImpExpGui topFrame;
	private final DBConnectionPool dbPool;

	private JComboBox connCombo;
	private JTextField descriptionText;
	private JTextField serverText;
	private JFormattedTextField portText;
	private JTextField databaseText;
	private JTextField userText;
	private JTextField workspaceText;
	private JFormattedTextField timestampText;
	private JPasswordField passwordText;
	private JCheckBox passwordCheck;
	private JButton applyButton;
	private JButton newButton;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton connectButton;
	private JButton executeButton;
	private JButton infoButton;
	private JComboBox bboxComboBox;
	private JRadioButton dbReport;
	private JRadioButton dbBBox;
	private SrsComboBox srsComboBox;
	private SrsComboBoxFactory srsComboBoxFactory;

	private JPanel row2;
	private JPanel row2_buttons;
	private JPanel row3;

	private JLabel connLabel;
	private JLabel descriptionLabel;
	private JLabel userLabel;
	private JLabel passwordLabel;
	private JLabel serverLabel;
	private JLabel portLabel;
	private JLabel databaseLabel;
	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JLabel featureClassLabel;
	private JLabel srsLabel;

	private Config config;
	private Database databaseConfig;
	private boolean isSettingsLoaded;

	public DatabasePanel(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;
		dbPool = DBConnectionPool.getInstance();

		initGui();		
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(ApplicationEvent.DATABASE_CONNECTION_STATE, this);
	}

	private boolean isModified() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();	
		if (!descriptionText.getText().trim().equals(dbConnection.getDescription())) return true;
		if (!serverText.getText().equals(dbConnection.getServer())) return true;
		if (!userText.getText().equals(dbConnection.getUser())) return true;		
		if (!String.valueOf(passwordText.getPassword()).equals(dbConnection.getInternalPassword())) return true;
		if (!databaseText.getText().equals(dbConnection.getSid())) return true;
		if (passwordCheck.isSelected() != dbConnection.isSetSavePassword()) return true;		
		if (portText.getValue() != null && ((Number)portText.getValue()).intValue() != dbConnection.getPort()) return true;

		return false;
	}

	private void initGui() {
		connCombo = new JComboBox();
		descriptionText = new JTextField();
		serverText = new JTextField();
		DecimalFormat df = new DecimalFormat("#####");
		df.setMaximumIntegerDigits(5);
		df.setMinimumIntegerDigits(1);
		portText = new JFormattedTextField(df);
		portText.setColumns(5);
		databaseText = new JTextField();
		userText = new JTextField();
		workspaceText = new JTextField();
		timestampText = new JFormattedTextField(new SimpleDateFormat("dd.MM.yyyy"));
		timestampText.setFocusLostBehavior(JFormattedTextField.COMMIT);
		timestampText.setColumns(10);
		passwordText = new JPasswordField();
		passwordCheck = new JCheckBox();
		
		applyButton = new JButton();
		newButton = new JButton();
		copyButton = new JButton();
		deleteButton = new JButton();
		connectButton = new JButton();
		executeButton = new JButton();
		infoButton = new JButton();

		dbReport = new JRadioButton();
		dbBBox = new JRadioButton();
		ButtonGroup dbOperations = new ButtonGroup();
		dbOperations.add(dbReport);
		dbOperations.add(dbBBox);

		bboxComboBox = new JComboBox();
		for (FeatureClassMode type : FeatureClassMode.values())
			bboxComboBox.addItem(type);

		srsComboBoxFactory = SrsComboBoxFactory.getInstance(config);
		srsComboBox = srsComboBoxFactory.createSrsComboBox(true);

		PopupMenuDecorator.getInstance().decorate(
				descriptionText, serverText, portText, databaseText, userText, passwordText, 
				workspaceText, timestampText);
		
		portText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (portText.getValue() != null) {
					if (((Number)portText.getValue()).intValue() < 0)
						portText.setValue(1521);
				}
			}
		});
		
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});

		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (requestChange())
					newConn();
			}
		});

		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (requestChange())
					copy();
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (requestDelete())
					delete();
			}
		});

		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						if (!dbPool.isConnected()) {
							try {
								connect(true);
							} catch (DatabaseConfigurationException e) {
								//
							} catch (SQLException e) {
								//
							}
						} else {
							try {
								disconnect(true);
							} catch (SQLException e) {
								//
							}
						}
					}
				};
				thread.start();
			}
		});

		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				topFrame.clearConsole();
				DBConnection conn = dbPool.getActiveConnection();
				LOG.info("Connected to database profile '" + conn.getDescription() + "'.");
				conn.getMetaData().printToConsole(LogLevelType.INFO);
			}
		});

		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						if (dbReport.isSelected())
							report();
						else if (dbBBox.isSelected())
							boundingBox((FeatureClassMode)bboxComboBox.getSelectedItem());
					}
				};
				thread.start();
			}
		});

		connCombo.addItemListener(new ItemListener() {
			public void itemStateChanged( ItemEvent e ) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					selectConnection();
			}
		});

		setLayout(new GridBagLayout());
		{
			JPanel row1 = new JPanel();
			add(row1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
			row1.setLayout(new GridBagLayout());
			connLabel = new JLabel();
			{
				row1.add(connLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				row1.add(connCombo, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			}
		}
		{
			row2 = new JPanel();
			add(row2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			row2.setBorder(BorderFactory.createTitledBorder(""));
			row2.setLayout(new GridBagLayout());
			descriptionLabel = new JLabel();
			userLabel = new JLabel();
			passwordLabel = new JLabel();
			serverLabel = new JLabel();
			portLabel = new JLabel();
			databaseLabel = new JLabel();
			passwordCheck.setIconTextGap(10);
			{
				row2.add(descriptionLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(descriptionText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(userLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(userText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(passwordLabel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(passwordText, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(passwordCheck, GuiUtil.setConstraints(1,3,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(serverLabel, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(serverText, GuiUtil.setConstraints(1,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(portLabel, GuiUtil.setConstraints(0,5,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

				GridBagConstraints c = GuiUtil.setConstraints(1,5,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row2.add(portText, c);
				portText.setMinimumSize(portText.getPreferredSize());

				row2.add(databaseLabel, GuiUtil.setConstraints(0,6,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(databaseText, GuiUtil.setConstraints(1,6,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

				row2_buttons = new JPanel();
				c = GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5);
				c.gridheight = 6;
				row2.add(row2_buttons,c);
				row2_buttons.setLayout(new GridBagLayout());
				row2_buttons.add(applyButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
				row2_buttons.add(newButton, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
				row2_buttons.add(copyButton, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));

				c = GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.HORIZONTAL,5,0,0,0);
				c.anchor = GridBagConstraints.NORTH;				
				row2_buttons.add(deleteButton, c);

				c = GuiUtil.setConstraints(0,7,0.0,0.0,GridBagConstraints.NONE,10,5,5,5);
				c.gridwidth = 3;
				row2.add(connectButton, c);
				row2.add(infoButton, GuiUtil.setConstraints(2,7,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
			}
		}
		{
			row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			row3.setBorder(BorderFactory.createTitledBorder(""));
			row3.setLayout(new GridBagLayout());
			workspaceLabel = new JLabel("");
			timestampLabel = new JLabel("");
			featureClassLabel = new JLabel("");
			srsLabel = new JLabel("");
			{
				row3.add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,5,5));
				row3.add(workspaceText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				row3.add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
				row3.add(timestampText, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
				timestampText.setMinimumSize(timestampText.getPreferredSize());

				JPanel row3_1 = new JPanel();
				row3_1.setBorder(BorderFactory.createEmptyBorder());
				row3_1.setLayout(new GridBagLayout());

				GridBagConstraints c = GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,10,5,5);
				c.gridwidth = 4;
				row3.add(row3_1, c);

				dbReport.setIconTextGap(10);
				dbBBox.setIconTextGap(10);				
				row3_1.add(dbReport, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,5,0,0));
				row3_1.add(dbBBox, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,0,0));

				int lmargin = (int)(dbBBox.getPreferredSize().getWidth()) + 11; 
				JPanel row3_2 = new JPanel();
				row3_1.add(row3_2, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,0,lmargin,5,0));
				row3_2.setBorder(BorderFactory.createEmptyBorder());
				row3_2.setLayout(new GridBagLayout());

				row3_2.add(featureClassLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,0,5,5));
				row3_2.add(bboxComboBox, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,0));
				row3_2.add(srsLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,0,5,5));
				row3_2.add(srsComboBox, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,0));

				row3_1.add(executeButton, GuiUtil.setConstraints(0,3,0.0,0.0,GridBagConstraints.NONE,5,5,0,5));	
			}
		}
		{
			JPanel row4 = new JPanel();
			add(row4, GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,20,5,15,5));
		}

		// influence focus policy
		row2.setFocusCycleRoot(false);
		row2_buttons.setFocusCycleRoot(true);

		ActionListener featureClassListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setEnabledFeatureClass();
			}
		};

		dbReport.addActionListener(featureClassListener);
		dbBBox.addActionListener(featureClassListener);
	}

	public void doTranslation() {
		row2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("db.border.connectionDetails")));
		connLabel.setText(Internal.I18N.getString("db.label.connection"));
		descriptionLabel.setText(Internal.I18N.getString("db.label.description"));
		userLabel.setText(Internal.I18N.getString("db.label.user"));
		passwordLabel.setText(Internal.I18N.getString("db.label.password"));
		serverLabel.setText(Internal.I18N.getString("db.label.server"));
		portLabel.setText(Internal.I18N.getString("db.label.port"));
		databaseLabel.setText(Internal.I18N.getString("db.label.sid"));
		passwordCheck.setText(Internal.I18N.getString("db.label.passwordCheck"));
		applyButton.setText(Internal.I18N.getString("common.button.apply"));
		newButton.setText(Internal.I18N.getString("db.button.new"));
		copyButton.setText(Internal.I18N.getString("db.button.copy"));
		deleteButton.setText(Internal.I18N.getString("db.button.delete"));
		infoButton.setText(Internal.I18N.getString("db.button.info"));

		row3.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("db.border.databaseOperations")));
		workspaceLabel.setText(Internal.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Internal.I18N.getString("common.label.timestamp"));
		dbReport.setText(Internal.I18N.getString("db.label.report"));
		dbBBox.setText(Internal.I18N.getString("db.label.bbox"));
		featureClassLabel.setText(Internal.I18N.getString("db.label.bbox.featureClass"));
		executeButton.setText(Internal.I18N.getString("db.button.execute"));

		srsLabel.setText(Internal.I18N.getString("common.label.boundingBox.crs"));

		if (!dbPool.isConnected())
			connectButton.setText(Internal.I18N.getString("db.button.connect"));
		else
			connectButton.setText(Internal.I18N.getString("db.button.disconnect"));
	}

	private void selectConnection() {
		if (isSettingsLoaded)
			setDbConnection(databaseConfig.getActiveConnection());

		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		databaseConfig.setActiveConnection(dbConnection);
		getDbConnection(dbConnection);
	}

	private void apply() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		setDbConnection(dbConnection);

		List<DBConnection> connList = databaseConfig.getConnections();
		Collections.sort(connList);

		connCombo.removeAllItems();
		for (DBConnection conn : connList)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(dbConnection);
		LOG.info("Settings successfully applied.");
	}

	private void copy() {
		DBConnection source = (DBConnection)connCombo.getSelectedItem();
		DBConnection dbConnection = new DBConnection();
		setDbConnection(dbConnection);
		dbConnection.setDescription(getCopyOfDescription(source));

		databaseConfig.getConnections().add(0, dbConnection);
		connCombo.insertItemAt(dbConnection, 0);
		connCombo.setSelectedItem(dbConnection);
	}

	private void newConn() {
		DBConnection dbConnection = new DBConnection();
		dbConnection.setDescription(getNewConnDescription());

		databaseConfig.getConnections().add(0, dbConnection);
		connCombo.insertItemAt(dbConnection, 0);
		connCombo.setSelectedItem(dbConnection);
	}

	private void delete() {
		int index = connCombo.getSelectedIndex();
		connCombo.removeItemAt(index);
		databaseConfig.getConnections().remove(index);

		if (connCombo.getItemCount() == 0)
			newConn();
		else
			connCombo.setSelectedIndex(index < connCombo.getItemCount() ? index : index - 1);		
	}

	public void connect(boolean showErrorDialog) throws DatabaseConfigurationException, SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!dbPool.isConnected()) {
				setSettings();
				
				DBConnection conn = config.getProject().getDatabase().getActiveConnection();			
				topFrame.setStatusText(Internal.I18N.getString("main.status.database.connect.label"));
				LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");

				try {
					dbPool.connect(config);
				} catch (DatabaseConfigurationException e) {					
					if (showErrorDialog)
						topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"), e.getMessage());				

					LOG.error("Connection to database could not be established.");
					topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
					throw e;
				} catch (SQLException e) {
					if (showErrorDialog) {
						String text = Internal.I18N.getString("db.dialog.error.openConn");
						Object[] args = new Object[]{ e.getMessage() };
						String result = MessageFormat.format(text, args);					

						topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));	
						topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
					}

					LOG.error("Connection to database could not be established.");
					if (config.getProject().getGlobal().getLogging().getConsole().getLogLevel() == LogLevelType.DEBUG) {
						LOG.debug("Check the following stack trace for details:");
						e.printStackTrace();
					}

					throw e;
				}

				if (dbPool.isConnected()) {
					LOG.info("Database connection established.");
					conn.getMetaData().printToConsole(LogLevelType.INFO);
					
					// log whether user-defined SRSs are supported
					for (ReferenceSystem refSys : config.getProject().getDatabase().getReferenceSystems()) {
						if (refSys.isSupported())
							LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
						else
							LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
					}					
				}

				topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));	
			}
		} finally {
			lock.unlock();
		}
	}

	public void disconnect(boolean showErrorDialog) throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {			
			if (dbPool.isConnected()) {
				topFrame.setStatusText(Internal.I18N.getString("main.status.database.disconnect.label"));

				try {
					dbPool.disconnect();
				} catch (SQLException e) {
					LOG.error("Connection error: " + e.getMessage().trim());
					LOG.error("Terminating connection...");
					dbPool.forceDisconnect();

					if (showErrorDialog) {
						String text = Internal.I18N.getString("db.dialog.error.closeConn");
						Object[] args = new Object[]{ e.getMessage() };
						String result = MessageFormat.format(text, args);

						topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
					}
					
					throw e;
				}

				LOG.info("Disconnected from database.");
				topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			}
		} finally {
			lock.unlock();
		}
	}

	private void report() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();
			Workspace workspace = config.getProject().getDatabase().getWorkspaces().getOperationWorkspace();		
			if (!checkWorkspaceInput(workspace))
				return;

			topFrame.clearConsole();
			topFrame.setStatusText(Internal.I18N.getString("main.status.database.report.label"));

			LOG.info("Generating database report...");			

			final StatusDialog reportDialog = new StatusDialog(topFrame, 
					Internal.I18N.getString("db.dialog.report.window"), 
					Internal.I18N.getString("db.dialog.report.title"), 
					null,
					Internal.I18N.getString("db.dialog.report.details"), 
					true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					reportDialog.setLocationRelativeTo(getTopLevelAncestor());
					reportDialog.setVisible(true);
				}
			});

			reportDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							DBUtil.cancelOperation();
						}
					});
				}
			});

			String[] report = null;
			String dbSqlEx = null;
			try {
				// checking workspace... this should be improved in future...
				if (changeWorkspace(workspace)) {
					report = DBUtil.databaseReport(workspace);

					if (report != null) {
						for(String line : report) {
							if (line != null) {
								line = line.replaceAll("\\\\n", "\\\n");
								line = line.replaceAll("\\\\t", "\\\t");
								LOG.write(line);
							}
						}

						LOG.info("Database report successfully generated.");
					} else
						LOG.warn("Generation of database report aborted.");
				}

			} catch (SQLException sqlEx) {
				dbSqlEx = sqlEx.getMessage().trim();
				String text = Internal.I18N.getString("db.dialog.error.report");
				Object[] args = new Object[]{ dbSqlEx };
				String result = MessageFormat.format(text, args);

				topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
				LOG.error("SQL error: " + dbSqlEx);
			} finally {			
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reportDialog.dispose();
					}
				});

				topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private void boundingBox(FeatureClassMode featureClass) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();
			Database db = config.getProject().getDatabase();			
			Workspace workspace = db.getWorkspaces().getOperationWorkspace();
			if (!checkWorkspaceInput(workspace))
				return;

			topFrame.clearConsole();
			topFrame.setStatusText(Internal.I18N.getString("main.status.database.bbox.label"));

			LOG.info("Calculating bounding box...");			

			final StatusDialog bboxDialog = new StatusDialog(topFrame, 
					Internal.I18N.getString("db.dialog.bbox.window"), 
					Internal.I18N.getString("db.dialog.bbox.title"), 
					null,
					Internal.I18N.getString("db.dialog.bbox.details"), 
					true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					bboxDialog.setLocationRelativeTo(getTopLevelAncestor());
					bboxDialog.setVisible(true);
				}
			});

			bboxDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							DBUtil.cancelOperation();
						}
					});
				}
			});

			BoundingBox bbox = null;
			try {
				// checking workspace... this should be improved in future...
				if (changeWorkspace(workspace)) {
					bbox = DBUtil.calcBoundingBox(workspace, featureClass);
					if (bbox != null) {
						int dbSrid = dbPool.getActiveConnection().getMetaData().getSrid();
						int bboxSrid = db.getOperation().getBoundingBoxSRS().getSrid();

						if (db.getOperation().getBoundingBoxSRS().isSupported() && bboxSrid != dbSrid) {
							try {
								bbox = DBUtil.transformBBox(bbox, dbSrid, bboxSrid);
							} catch (SQLException e) {
								//
							}					
						}

						double xmin = bbox.getLowerCorner().getX();
						double ymin = bbox.getLowerCorner().getY();
						double xmax = bbox.getUpperCorner().getX();
						double ymax = bbox.getUpperCorner().getY();

						if (xmin != Double.MAX_VALUE && ymin != Double.MAX_VALUE &&
								ymin != -Double.MAX_VALUE && ymax != -Double.MAX_VALUE) {						
							LOG.info("Maximum bounding box for feature class " + featureClass + ':');
							LOG.info("Xmin = " + xmin + ", Ymin = " + ymin);
							LOG.info("Xmax = " + xmax + ", Ymax = " + ymax);
							LOG.info("Bounding box successfully calculated.");
						} else {
							LOG.warn("The bounding box could not be calculated.");
							LOG.warn("Either the database does not contain " + featureClass + " features or their ENVELOPE attribute is not set.");
						}

					} else
						LOG.warn("Calculation of bounding box aborted.");
				}
			} catch (SQLException sqlEx) {
				String sqlExMsg = sqlEx.getMessage().trim();
				String text = Internal.I18N.getString("db.dialog.error.bbox");
				Object[] args = new Object[]{ sqlExMsg };
				String result = MessageFormat.format(text, args);

				topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
				LOG.error("SQL error: " + sqlExMsg);
			} finally {		
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						bboxDialog.dispose();
					}
				});

				topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private boolean changeWorkspace(Workspace workspace) {		
		if (!workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.existsWorkspace(workspace);

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;

			if (!workspaceExists) {
				LOG.error("Database workspace " + name + " is not available.");
				return false;
			} else 
				LOG.info("Switching to database workspace " + name + '.');
		}

		return true;
	}

	private boolean checkWorkspaceInput(Workspace workspace) {
		if (!Util.checkWorkspaceTimestamp(workspace)) {
			topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.operation.incorrectData"), 
					Internal.I18N.getString("common.dialog.error.incorrectData.date"));
			return false;
		}

		return true;
	}

	public void loadSettings() {
		if (dbPool.isConnected()) {
			try {
				disconnect(true);
			} catch (SQLException e) {
				dbPool.forceDisconnect();
			}
		}

		databaseConfig = config.getProject().getDatabase();
		isSettingsLoaded = false;

		connCombo.removeAllItems();
		DBConnection dbConnection = databaseConfig.getActiveConnection();
		List<DBConnection> dbConnectionList = databaseConfig.getConnections();

		if (dbConnection == null) {
			if (dbConnectionList != null && !dbConnectionList.isEmpty())
				dbConnection = dbConnectionList.get(0);
			else {
				dbConnection = new DBConnection();
				dbConnection.setDescription(Internal.I18N.getString("db.label.newConnection"));
				databaseConfig.addConnection(dbConnection);
			}
		}

		Collections.sort(dbConnectionList);
		for (DBConnection conn : dbConnectionList)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(dbConnection);
		dbConnection.setInternalPassword(dbConnection.getPassword());

		workspaceText.setText(databaseConfig.getWorkspaces().getOperationWorkspace().getName());
		timestampText.setText(databaseConfig.getWorkspaces().getOperationWorkspace().getTimestamp());
		bboxComboBox.setSelectedItem(databaseConfig.getOperation().getBoundingBoxFeatureClass());

		srsComboBox.setSelectedItem(databaseConfig.getOperation().getBoundingBoxSRS());

		if (databaseConfig.getOperation().getExecute() == DBOperationMode.REPORT)
			dbReport.setSelected(true);
		else
			dbBBox.setSelected(true);

		setEnabledDBOperations(false);
		isSettingsLoaded = true;
	}

	public void setSettings() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		setDbConnection(dbConnection);

		String workspace = workspaceText.getText().trim();
		if (!workspace.equals(Internal.ORACLE_DEFAULT_WORKSPACE) && 
				(workspace.length() == 0 || workspace.toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)))
			workspaceText.setText(Internal.ORACLE_DEFAULT_WORKSPACE);

		databaseConfig.getWorkspaces().getOperationWorkspace().setName(workspaceText.getText());
		databaseConfig.getWorkspaces().getOperationWorkspace().setTimestamp(timestampText.getText());
		databaseConfig.getOperation().setExecute(dbReport.isSelected() ? DBOperationMode.REPORT : DBOperationMode.BBOX);
		databaseConfig.getOperation().setBoundingBoxFeatureClass((FeatureClassMode)bboxComboBox.getSelectedItem());
		databaseConfig.getOperation().setBoundingBoxSRS(srsComboBox.getSelectedItem());
	}

	private void setDbConnection(DBConnection dbConnection) {
		if (!descriptionText.getText().trim().equals(""))
			dbConnection.setDescription(descriptionText.getText());
		else
			descriptionText.setText(dbConnection.getDescription());

		dbConnection.setServer(serverText.getText().trim());	
		dbConnection.setPort(((Number)portText.getValue()).intValue());
		dbConnection.setSid(databaseText.getText());
		dbConnection.setUser(userText.getText());
		dbConnection.setInternalPassword(new String(passwordText.getPassword()));

		dbConnection.setSavePassword(passwordCheck.isSelected());
		if (passwordCheck.isSelected())
			dbConnection.setPassword(new String(passwordText.getPassword()));
		else
			dbConnection.setPassword("");
	}

	private void getDbConnection(DBConnection dbConnection) {
		descriptionText.setText(dbConnection.getDescription());
		serverText.setText(dbConnection.getServer());
		databaseText.setText(dbConnection.getSid());
		userText.setText(dbConnection.getUser());
		passwordCheck.setSelected(dbConnection.isSetSavePassword());

		if (passwordCheck.isSelected())
			passwordText.setText(dbConnection.getPassword());
		else
			passwordText.setText(dbConnection.getInternalPassword());

		Integer port = dbConnection.getPort();
		if (port == null || port == 0) {
			port = 1521;
			dbConnection.setPort(1521);
		}

		portText.setValue(port);		
	}

	private String getCopyOfDescription(DBConnection dbConnection) {
		// pattern: "connectionName - copy 1"
		// so to retrieve connectionName, " - copy*" has to be deleted...

		int nr = 0;
		String name = dbConnection.getDescription().replaceAll("\\s*-\\s*" + Internal.I18N.getString("db.label.copyConnection") + ".*$", "");
		String copy = name + " - " + Internal.I18N.getString("db.label.copyConnection");
		for (DBConnection conn : databaseConfig.getConnections()) 
			if (conn.getDescription().replaceAll("\\s*-\\s*" + Internal.I18N.getString("db.label.copyConnection") + ".*$", "").toLowerCase().equals(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return copy + " " + nr;
		else
			return copy;
	}

	private String getNewConnDescription() {
		int nr = 1;
		String name = Internal.I18N.getString("db.label.newConnection");
		for (DBConnection conn : databaseConfig.getConnections()) 
			if (conn.getDescription().toLowerCase().startsWith(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return name + " " + nr;
		else
			return name;
	}

	public boolean requestChange() {
		if (isModified()) {
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), Internal.I18N.getString("db.dialog.apply.msg"), 
					Internal.I18N.getString("db.dialog.apply.title"), JOptionPane.YES_NO_CANCEL_OPTION);
			if (res==JOptionPane.CANCEL_OPTION) 
				return false;
			else if (res==JOptionPane.YES_OPTION)
				apply();
			else
				getDbConnection((DBConnection)connCombo.getSelectedItem());
		}

		return true;
	}

	private boolean requestDelete() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		String text = Internal.I18N.getString("db.dialog.delete.msg");
		Object[] args = new Object[]{ dbConnection.getDescription() };
		String result = MessageFormat.format(text, args);

		int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), result, Internal.I18N.getString("db.dialog.delete.title"), JOptionPane.YES_NO_OPTION);
		return res==JOptionPane.YES_OPTION;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean isConnected = ((DatabaseConnectionStateEvent)event).isConnected();

		if (!isConnected)
			connectButton.setText(Internal.I18N.getString("db.button.connect"));
		else
			connectButton.setText(Internal.I18N.getString("db.button.disconnect"));

		connectButton.repaint();
		setEnabledDBOperations(isConnected);
	}

	private void setEnabledDBOperations(boolean enable) {
		((TitledBorder)row3.getBorder()).setTitleColor(enable ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		row3.repaint();

		infoButton.setEnabled(enable);
		workspaceLabel.setEnabled(enable);
		workspaceText.setEnabled(enable);
		timestampLabel.setEnabled(enable);
		timestampText.setEnabled(enable);
		dbReport.setEnabled(enable);
		dbBBox.setEnabled(enable);
		bboxComboBox.setEnabled(dbBBox.isSelected() && enable);
		featureClassLabel.setEnabled(dbBBox.isSelected() && enable);
		srsLabel.setEnabled(dbBBox.isSelected() && enable);
		srsComboBox.setEnabled(dbBBox.isSelected() && enable);
		executeButton.setEnabled(enable);
	}

	private void setEnabledFeatureClass() {
		bboxComboBox.setEnabled(dbBBox.isSelected());
		featureClassLabel.setEnabled(dbBBox.isSelected());
		srsLabel.setEnabled(dbBBox.isSelected());
		srsComboBox.setEnabled(dbBBox.isSelected());
	}

}
