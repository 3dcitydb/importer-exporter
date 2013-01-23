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
package de.tub.citydb.modules.database.gui.view;

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

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.event.global.ViewEvent;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.plugin.extension.view.ViewListener;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.database.ConnectionStateEnum;
import de.tub.citydb.database.ConnectionViewHandler;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.database.gui.operations.DatabaseOperationsPanel;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class DatabasePanel extends JPanel implements ConnectionViewHandler, EventHandler, ViewListener {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final ImpExpGui topFrame;
	private final DatabaseController databaseController;

	private JComboBox connCombo;
	private JTextField descriptionText;
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
		connectionDetails.add(serverLabel, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(serverText, GuiUtil.setConstraints(1,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(portLabel, GuiUtil.setConstraints(0,5,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

		connectionDetails.add(portText, GuiUtil.setConstraints(1,5,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,0,5,5,5));
		portText.setMinimumSize(portText.getPreferredSize());

		connectionDetails.add(databaseLabel, GuiUtil.setConstraints(0,6,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionDetails.add(databaseText, GuiUtil.setConstraints(1,6,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));

		connectionButtons = new JPanel();
		connectionDetails.add(connectionButtons, GuiUtil.setConstraints(2,0,1,6,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
		connectionButtons.setLayout(new GridBagLayout());
		connectionButtons.add(applyButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
		connectionButtons.add(newButton, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
		connectionButtons.add(copyButton, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));
		connectionButtons.add(deleteButton, GuiUtil.setConstraints(0,3,0.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL,5,0,0,0));
		connectionDetails.add(connectButton, GuiUtil.setConstraints(0,7,3,1,0.0,0.0,GridBagConstraints.NONE,10,5,5,5));
		connectionDetails.add(infoButton, GuiUtil.setConstraints(2,7,0.0,0.0,GridBagConstraints.HORIZONTAL,5,5,0,5));

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

		// influence focus policy
		connectionDetails.setFocusCycleRoot(false);
		connectionButtons.setFocusCycleRoot(true);

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
				thread.setDaemon(true);
				thread.start();
			}
		});

		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				topFrame.clearConsole();
				LOG.all(LogLevel.INFO, "Connected to database profile '" + databaseController.getActiveConnectionDetails().getDescription() + "'.");
				databaseController.getActiveConnectionMetaData().printToConsole();
			}
		});

		connCombo.addItemListener(new ItemListener() {
			public void itemStateChanged( ItemEvent e ) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					selectConnection();
			}
		});
	}

	public void doTranslation() {
		((TitledBorder)connectionDetails.getBorder()).setTitle(Internal.I18N.getString("db.border.connectionDetails"));
		connLabel.setText(Internal.I18N.getString("db.label.connection"));
		descriptionLabel.setText(Internal.I18N.getString("db.label.description"));
		userLabel.setText(Internal.I18N.getString("common.label.username"));
		passwordLabel.setText(Internal.I18N.getString("common.label.password"));
		serverLabel.setText(Internal.I18N.getString("common.label.server"));
		portLabel.setText(Internal.I18N.getString("common.label.port"));
		databaseLabel.setText(Internal.I18N.getString("db.label.sid"));
		passwordCheck.setText(Internal.I18N.getString("common.label.passwordCheck"));
		applyButton.setText(Internal.I18N.getString("common.button.apply"));
		newButton.setText(Internal.I18N.getString("db.button.new"));
		copyButton.setText(Internal.I18N.getString("db.button.copy"));
		deleteButton.setText(Internal.I18N.getString("db.button.delete"));
		infoButton.setText(Internal.I18N.getString("db.button.info"));

		((TitledBorder)operations.getBorder()).setTitle(Internal.I18N.getString("db.border.databaseOperations"));
		operationsPanel.doTranslation();

		if (!databaseController.isConnected())
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
			databaseController.connect(true);
		} finally {
			lock.unlock();
		}
	}

	public void disconnect(boolean showErrorDialog) throws SQLException {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			databaseController.disconnect(true);
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
			topFrame.setStatusText(Internal.I18N.getString("main.status.database.connect.label"));
			LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");
			break;
		case FINISH_CONNECT:
			if (databaseController.isConnected()) {
				LOG.info("Database connection established.");
				databaseController.getActiveConnectionMetaData().printToConsole();

				// log whether user-defined SRSs are supported
				for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) {
					if (refSys.isSupported())
						LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
					else
						LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
				}					
			}

			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));	
			break;
		case INIT_DISCONNECT:
			topFrame.setStatusText(Internal.I18N.getString("main.status.database.disconnect.label"));
			break;
		case FINISH_DISCONNECT:
			LOG.info("Disconnected from database.");
			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			break;
		}
	}

	@Override
	public void printError(ConnectionStateEnum state, DatabaseConfigurationException e, boolean showErrorDialog) {
		switch (state) {
		case CONNECT_ERROR:
			if (showErrorDialog)
				topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"), e.getMessage());				

			LOG.error("Connection to database could not be established.");
			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			break;
		}
	}

	@Override
	public void printError(ConnectionStateEnum state, SQLException e, boolean showErrorDialog) {
		switch (state) {
		case CONNECT_ERROR:
			if (showErrorDialog) {
				String text = Internal.I18N.getString("db.dialog.error.openConn");
				Object[] args = new Object[]{ e.getMessage() };
				String result = MessageFormat.format(text, args);					

				topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));	
				topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
			}

			LOG.error("Connection to database could not be established.");
			if (LOG.getDefaultConsoleLogLevel() == LogLevel.DEBUG) {
				LOG.debug("Check the following stack trace for details:");
				e.printStackTrace();
			}

			break;
		case DISCONNECT_ERROR:
			LOG.error("Connection error: " + e.getMessage().trim());
			LOG.error("Terminating connection...");
			databaseController.forceDisconnect();

			if (showErrorDialog) {
				String text = Internal.I18N.getString("db.dialog.error.closeConn");
				Object[] args = new Object[]{ e.getMessage() };
				String result = MessageFormat.format(text, args);

				topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
			}

			break;
		}
	}

	public void loadSettings() {
		if (databaseController.isConnected()) {
			try {
				disconnect(true);
			} catch (SQLException e) {
				databaseController.forceDisconnect();
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
		if (!description.equals("")) {
			boolean repaint = dbConnection == databaseConfig.getActiveConnection() && !description.equals(dbConnection.getDescription());
			dbConnection.setDescription(description);
			if (repaint) 
				connCombo.repaint();			
		} else
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

	private boolean requestChange() {
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
			connectButton.setText(Internal.I18N.getString("db.button.connect"));
		else
			connectButton.setText(Internal.I18N.getString("db.button.disconnect"));

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
