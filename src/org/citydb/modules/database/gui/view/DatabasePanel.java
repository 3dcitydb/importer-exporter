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
package org.citydb.modules.database.gui.view;

import java.awt.BorderLayout;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.citydb.api.controller.DatabaseController;
import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseConnectionWarning;
import org.citydb.api.database.DatabaseConnectionWarning.ConnectionWarningType;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseType;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventHandler;
import org.citydb.api.event.global.DatabaseConnectionStateEvent;
import org.citydb.api.event.global.GlobalEvents;
import org.citydb.api.event.global.ViewEvent;
import org.citydb.api.log.LogLevel;
import org.citydb.api.plugin.extension.view.ViewListener;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.Database;
import org.citydb.database.ConnectionStateEnum;
import org.citydb.database.ConnectionViewHandler;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.log.Logger;
import org.citydb.modules.database.gui.operations.DatabaseOperationsPanel;
import org.citydb.util.Util;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class DatabasePanel extends JPanel implements ConnectionViewHandler, EventHandler, ViewListener {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final ImpExpGui topFrame;
	private final DatabaseController databaseController;

	private JComboBox<DBConnection> connCombo;
	private JTextField descriptionText;
	private JComboBox<DatabaseType> databaseTypeCombo;
	private JTextField serverText;
	private JFormattedTextField portText;
	private JTextField databaseText;
	private JTextField userText;
	private JPasswordField passwordText;
	private JCheckBox passwordCheck;
	private JButton applyButton;
	private JButton newButton;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton connectButton;
	private JButton infoButton;	

	private JPanel connectionDetails;
	private JPanel connectionButtons;
	private JPanel operations;

	private JLabel connLabel;
	private JLabel descriptionLabel;
	private JLabel databaseTypeLabel;
	private JLabel userLabel;
	private JLabel passwordLabel;
	private JLabel serverLabel;
	private JLabel portLabel;
	private JLabel databaseLabel;

	private DatabaseOperationsPanel operationsPanel;

	private Config config;
	private Database databaseConfig;
	private boolean isSettingsLoaded;

	public DatabasePanel(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;
		databaseController = ObjectRegistry.getInstance().getDatabaseController();

		initGui();		
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);
	}

	private boolean isModified() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();	
		if (!descriptionText.getText().trim().equals(dbConnection.getDescription())) return true;
		if (databaseTypeCombo.getSelectedItem() != dbConnection.getDatabaseType()) return true;
		if (!serverText.getText().equals(dbConnection.getServer())) return true;
		if (!userText.getText().equals(dbConnection.getUser())) return true;		
		if (!String.valueOf(passwordText.getPassword()).equals(dbConnection.getInternalPassword())) return true;
		if (!databaseText.getText().equals(dbConnection.getSid())) return true;
		if (passwordCheck.isSelected() != dbConnection.isSetSavePassword()) return true;		
		if (portText.getValue() != null && ((Number)portText.getValue()).intValue() != dbConnection.getPort()) return true;

		return false;
	}

	private void initGui() {
		connCombo = new JComboBox<DBConnection>();
		descriptionText = new JTextField();
		databaseTypeCombo = new JComboBox<DatabaseType>();
		serverText = new JTextField();
		DecimalFormat df = new DecimalFormat("#####");
		df.setMaximumIntegerDigits(5);
		df.setMinimumIntegerDigits(1);
		portText = new JFormattedTextField(df);
		portText.setColumns(5);
		databaseText = new JTextField();
		userText = new JTextField();
		passwordText = new JPasswordField();
		passwordCheck = new JCheckBox();

		applyButton = new JButton();
		newButton = new JButton();
		copyButton = new JButton();
		deleteButton = new JButton();
		connectButton = new JButton();
		infoButton = new JButton();		

		PopupMenuDecorator.getInstance().decorate(descriptionText, serverText, portText, databaseText, userText, passwordText);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

		JPanel view = new JPanel();
		view.setLayout(new GridBagLayout());

		JPanel chooserPanel = new JPanel();
		view.add(chooserPanel, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
		chooserPanel.setLayout(new GridBagLayout());
		connLabel = new JLabel();

		chooserPanel.add(connLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
		chooserPanel.add(connCombo, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));

		connectionDetails = new JPanel();
		view.add(connectionDetails, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.setBorder(BorderFactory.createTitledBorder(""));
		connectionDetails.setLayout(new GridBagLayout());
		descriptionLabel = new JLabel();
		databaseTypeLabel = new JLabel();
		userLabel = new JLabel();
		passwordLabel = new JLabel();
		serverLabel = new JLabel();
		portLabel = new JLabel();
		databaseLabel = new JLabel();
		passwordCheck.setIconTextGap(10);

		connectionDetails.add(descriptionLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(descriptionText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(userLabel, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(userText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(passwordLabel, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(passwordText, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(passwordCheck, GuiUtil.setConstraints(1,3,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(databaseTypeLabel, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(databaseTypeCombo, GuiUtil.setConstraints(1,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));		
		connectionDetails.add(serverLabel, GuiUtil.setConstraints(0,5,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(serverText, GuiUtil.setConstraints(1,5,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(portLabel, GuiUtil.setConstraints(0,6,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

		connectionDetails.add(portText, GuiUtil.setConstraints(1,6,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,0,5,5,5));
		portText.setMinimumSize(portText.getPreferredSize());

		connectionDetails.add(databaseLabel, GuiUtil.setConstraints(0,7,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(databaseText, GuiUtil.setConstraints(1,7,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

		connectionButtons = new JPanel();
		connectionDetails.add(connectionButtons, GuiUtil.setConstraints(2,0,1,7,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionButtons.setLayout(new GridBagLayout());
		connectionButtons.add(applyButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
		connectionButtons.add(newButton, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
		connectionButtons.add(copyButton, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
		connectionButtons.add(deleteButton, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,0,0,0));
		connectionDetails.add(connectButton, GuiUtil.setConstraints(0,8,3,1,0.0,0.0,GridBagConstraints.NONE,10,5,5,5));
		connectionDetails.add(infoButton, GuiUtil.setConstraints(2,8,0.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

		operations = new JPanel();
		view.add(operations, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
		operations.setBorder(BorderFactory.createTitledBorder(""));
		operations.setLayout(new GridBagLayout());

		operationsPanel = new DatabaseOperationsPanel(config);
		operations.add(operationsPanel, GuiUtil.setConstraints(0,0,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));

		view.add(Box.createVerticalGlue(), GuiUtil.setConstraints(0,3,1.0,1.0,GridBagConstraints.BOTH,5,5,5,5));

		scrollPane.setViewportView(view);
		setLayout(new BorderLayout());
		add(scrollPane);

		// influence focus behavior
		applyButton.setFocusable(false);
		newButton.setFocusable(false);
		copyButton.setFocusable(false);
		deleteButton.setFocusable(false);
		infoButton.setFocusable(false);

		for (DatabaseType type : DatabaseType.values())
			databaseTypeCombo.addItem(type);

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
						if (!databaseController.isConnected()) {
							try {
								connect(true);
							} catch (DatabaseConfigurationException e) {
								//
							} catch (DatabaseVersionException e) {
								//
							} catch (SQLException e) {
								//
							}
						} else {
							disconnect();
						}
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				topFrame.clearConsole();
				LOG.all(LogLevel.INFO, "Connected to database profile '" + databaseController.getActiveDatabaseAdapter().getConnectionDetails().getDescription() + "'.");
				databaseController.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();
			}
		});

		connCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					selectConnection();
			}
		});

		databaseTypeCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					switch ((DatabaseType)e.getItem()) {
					case ORACLE:
						portText.setValue(1521);
						break;
					case POSTGIS:
						portText.setValue(5432);
						break;
					}
				}
			}
		});
	}

	public void doTranslation() {
		((TitledBorder)connectionDetails.getBorder()).setTitle(Language.I18N.getString("db.border.connectionDetails"));
		connLabel.setText(Language.I18N.getString("db.label.connection"));
		descriptionLabel.setText(Language.I18N.getString("db.label.description"));
		databaseTypeLabel.setText(Language.I18N.getString("db.label.databaseType"));
		userLabel.setText(Language.I18N.getString("common.label.username"));
		passwordLabel.setText(Language.I18N.getString("common.label.password"));
		serverLabel.setText(Language.I18N.getString("common.label.server"));
		portLabel.setText(Language.I18N.getString("common.label.port"));
		databaseLabel.setText(Language.I18N.getString("db.label.sid"));
		passwordCheck.setText(Language.I18N.getString("common.label.passwordCheck"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
		newButton.setText(Language.I18N.getString("db.button.new"));
		copyButton.setText(Language.I18N.getString("db.button.copy"));
		deleteButton.setText(Language.I18N.getString("db.button.delete"));
		infoButton.setText(Language.I18N.getString("db.button.info"));

		((TitledBorder)operations.getBorder()).setTitle(Language.I18N.getString("db.border.databaseOperations"));
		operationsPanel.doTranslation();

		if (!databaseController.isConnected())
			connectButton.setText(Language.I18N.getString("db.button.connect"));
		else
			connectButton.setText(Language.I18N.getString("db.button.disconnect"));
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

	public void connect(boolean showErrorDialog) throws DatabaseConfigurationException, DatabaseVersionException, SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			databaseController.connect(true);
		} finally {
			lock.unlock();
		}
	}

	public void disconnect() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			databaseController.disconnect();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void commitConnectionDetails() {
		setSettings();
	}

	@Override
	public void printConnectionState(ConnectionStateEnum state) {
		DBConnection conn = config.getProject().getDatabase().getActiveConnection();			

		switch (state) {
		case INIT_CONNECT:
			topFrame.setStatusText(Language.I18N.getString("main.status.database.connect.label"));
			LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");
			break;
		case FINISH_CONNECT:
			if (databaseController.isConnected()) {
				LOG.info("Database connection established.");
				databaseController.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();

				// log whether user-defined SRSs are supported
				for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) {
					if (refSys.isSupported())
						LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
					else
						LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
				}					
			}

			topFrame.setStatusText(Language.I18N.getString("main.status.ready.label"));	
			break;
		case INIT_DISCONNECT:
			topFrame.setStatusText(Language.I18N.getString("main.status.database.disconnect.label"));
			break;
		case FINISH_DISCONNECT:
			LOG.info("Disconnected from database.");
			topFrame.setStatusText(Language.I18N.getString("main.status.ready.label"));
			break;
		}
	}

	@Override
	public void printError(DatabaseConfigurationException e, boolean showErrorDialog) {
		if (showErrorDialog)
			topFrame.errorMessage(Language.I18N.getString("db.dialog.error.conn.title"), e.getMessage());
		else
			LOG.error(e.getMessage());

		LOG.error("Connection to database could not be established.");
		topFrame.setStatusText(Language.I18N.getString("main.status.ready.label"));
	}

	@Override
	public void printError(DatabaseVersionException e, boolean showErrorDialog) {
		if (showErrorDialog)
			topFrame.errorMessage(Language.I18N.getString("db.dialog.error.version.title"), e.getFormattedMessage());
		else {
			LOG.error(e.getMessage());
			LOG.error("Supported versions are '" + Util.collection2string(e.getSupportedVersions(), ", ") + "'.");
		}

		LOG.error("Connection to database could not be established.");
		topFrame.setStatusText(Language.I18N.getString("main.status.ready.label"));
	}

	@Override
	public void printError(SQLException e, boolean showErrorDialog) {
		if (showErrorDialog) {
			String text = Language.I18N.getString("db.dialog.error.openConn");
			Object[] args = new Object[]{ e.getMessage() };
			String result = MessageFormat.format(text, args);					

			topFrame.errorMessage(Language.I18N.getString("common.dialog.error.db.title"), result);
		} else if (e.getMessage() != null)
			LOG.error(e.getMessage());

		LOG.error("Connection to database could not be established.");
		if (LOG.getDefaultConsoleLogLevel() == LogLevel.DEBUG) {
			LOG.debug("Check the following stack trace for details:");
			e.printStackTrace();
		}

		topFrame.setStatusText(Language.I18N.getString("main.status.ready.label"));	
	}

	@Override
	public void printWarning(DatabaseConnectionWarning warning, boolean showWarningDialog) {
		if (showWarningDialog) {
			if (warning.getType() == ConnectionWarningType.OUTDATED_DATABASE_VERSION) {
				if (config.getGui().isShowOutdatedDatabaseVersionWarning()) {
					JPanel confirmPanel = new JPanel(new GridBagLayout());
					JCheckBox confirmDialogNoShow = new JCheckBox(Language.I18N.getString("common.dialog.msg.noShow"));
					confirmDialogNoShow.setIconTextGap(10);
					confirmPanel.add(new JLabel(warning.getFormattedMessage()), GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,0,0,0,0));
					confirmPanel.add(confirmDialogNoShow, GuiUtil.setConstraints(0,2,1.0,0.0,GridBagConstraints.BOTH,10,0,0,0));

					JOptionPane.showMessageDialog(topFrame, confirmPanel, Language.I18N.getString("db.dialog.warn.title"), JOptionPane.WARNING_MESSAGE);	
					config.getGui().setShowOutdatedDatabaseVersionWarning(!confirmDialogNoShow.isSelected());
				}
			}

			else {
				String text = Language.I18N.getString("db.dialog.warn.general");
				Object[] args = new Object[]{ warning.getMessage() };
				String result = MessageFormat.format(text, args);

				topFrame.warnMessage(Language.I18N.getString("db.dialog.warn.title"), result);
			}
		}

		LOG.warn(warning.getMessage());
	}

	public void loadSettings() {
		if (databaseController.isConnected())
			disconnect();

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
				dbConnection.setDescription(Language.I18N.getString("db.label.newConnection"));
				databaseConfig.addConnection(dbConnection);
			}
		}

		Collections.sort(dbConnectionList);
		for (DBConnection conn : dbConnectionList)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(dbConnection);
		dbConnection.setInternalPassword(dbConnection.getPassword());

		operationsPanel.loadSettings();

		setEnabledDBOperations(false);
		isSettingsLoaded = true;
	}

	public void setSettings() {
		setDbConnection((DBConnection)connCombo.getSelectedItem());
		operationsPanel.setSettings();
	}

	private void setDbConnection(DBConnection dbConnection) {
		String description = descriptionText.getText().trim();		
		if (description.length() > 0) {
			boolean repaint = dbConnection == databaseConfig.getActiveConnection() && !description.equals(dbConnection.getDescription());
			dbConnection.setDescription(description);
			if (repaint) 
				connCombo.repaint();			
		} else
			descriptionText.setText(dbConnection.getDescription());

		dbConnection.setDatabaseType((DatabaseType)databaseTypeCombo.getSelectedItem());
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
		databaseTypeCombo.setSelectedItem(dbConnection.getDatabaseType());
		serverText.setText(dbConnection.getServer());
		databaseText.setText(dbConnection.getSid());
		userText.setText(dbConnection.getUser());
		passwordCheck.setSelected(dbConnection.isSetSavePassword());

		if (passwordCheck.isSelected())
			passwordText.setText(dbConnection.getPassword());
		else
			passwordText.setText(dbConnection.getInternalPassword());

		if (dbConnection.getInternalPassword() == null)
			dbConnection.setInternalPassword(dbConnection.getPassword());

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
		String name = dbConnection.getDescription().replaceAll("\\s*-\\s*" + Language.I18N.getString("db.label.copyConnection") + ".*$", "");
		String copy = name + " - " + Language.I18N.getString("db.label.copyConnection");
		for (DBConnection conn : databaseConfig.getConnections()) 
			if (conn.getDescription().replaceAll("\\s*-\\s*" + Language.I18N.getString("db.label.copyConnection") + ".*$", "").toLowerCase().equals(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return copy + " " + nr;
		else
			return copy;
	}

	private String getNewConnDescription() {
		int nr = 1;
		String name = Language.I18N.getString("db.label.newConnection");
		for (DBConnection conn : databaseConfig.getConnections()) 
			if (conn.getDescription().toLowerCase().startsWith(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return name + " " + nr;
		else
			return name;
	}

	private boolean requestChange() {
		if (isModified()) {
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), Language.I18N.getString("db.dialog.apply.msg"), 
					Language.I18N.getString("db.dialog.apply.title"), JOptionPane.YES_NO_CANCEL_OPTION);
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
		String text = Language.I18N.getString("db.dialog.delete.msg");
		Object[] args = new Object[]{ dbConnection.getDescription() };
		String result = MessageFormat.format(text, args);

		int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), result, Language.I18N.getString("db.dialog.delete.title"), JOptionPane.YES_NO_OPTION);
		return res==JOptionPane.YES_OPTION;
	}

	private void setEnabledDBOperations(boolean enable) {
		((TitledBorder)operations.getBorder()).setTitleColor(enable ? 
				UIManager.getColor("TitledBorder.titleColor"):
					UIManager.getColor("Label.disabledForeground"));
		operations.repaint();

		infoButton.setEnabled(enable);		
		operationsPanel.setEnabled(enable);
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean isConnected = ((DatabaseConnectionStateEvent)event).isConnected();

		if (!isConnected)
			connectButton.setText(Language.I18N.getString("db.button.connect"));
		else
			connectButton.setText(Language.I18N.getString("db.button.disconnect"));

		connectButton.repaint();
		setEnabledDBOperations(isConnected);
	}

	@Override
	public void viewActivated(ViewEvent e) {
		// nothing to do here
	}

	@Override
	public void viewDeactivated(ViewEvent e) {
		if (isModified())
			setDbConnection((DBConnection)connCombo.getSelectedItem());
	}

}
