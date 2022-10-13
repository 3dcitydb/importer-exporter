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
package org.citydb.gui.operation.database.view;

import com.formdev.flatlaf.extras.components.FlatComboBox;
import org.citydb.config.Config;
import org.citydb.config.gui.database.DatabaseGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.*;
import org.citydb.core.database.DatabaseController;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.DatabaseAdapterFactory;
import org.citydb.core.database.connection.ConnectionState;
import org.citydb.core.database.connection.ConnectionViewHandler;
import org.citydb.core.database.connection.DatabaseConnectionWarning;
import org.citydb.core.database.connection.DatabaseConnectionWarning.ConnectionWarningType;
import org.citydb.core.database.version.DatabaseVersionException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.DatePicker;
import org.citydb.gui.components.ScrollablePanel;
import org.citydb.gui.components.TitledPanel;
import org.citydb.gui.components.dialog.ConfirmationCheckDialog;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.operation.database.operations.DatabaseOperationsPanel;
import org.citydb.gui.plugin.util.DefaultViewComponent;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.plugin.view.ViewEvent;
import org.citydb.gui.plugin.view.ViewListener;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.DatabaseConnectionStateEvent;
import org.citydb.util.event.global.EventType;
import org.citydb.util.log.Logger;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

public class DatabasePanel extends DefaultViewComponent implements ConnectionViewHandler, EventHandler, ViewListener {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
	private final ViewController viewController;
	private final DatabaseController databaseController;
	private final Config config;

	private JComboBox<DatabaseConnection> connCombo;
	private JTextField descriptionText;
	private JComboBox<DatabaseType> databaseTypeCombo;
	private JTextField serverText;
	private JFormattedTextField portText;
	private JTextField databaseText;
	private FlatComboBox<String> schemaCombo;
	private FlatComboBox<String> workspaceCombo;
	private DatePicker timestamp;
	private JTextField userText;
	private JPasswordField passwordText;
	private JCheckBox passwordCheck;

	private JButton applyButton;
	private JButton newButton;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton connectButton;
	private JButton infoButton;
	private JButton schemaButton;
	private JButton workspaceButton;

    private TitledPanel connectionDetails;
    private JPanel workspacePanel;

	private JLabel connLabel;
	private JLabel descriptionLabel;
	private JLabel databaseTypeLabel;
	private JLabel userLabel;
	private JLabel passwordLabel;
	private JLabel serverLabel;
	private JLabel portLabel;
	private JLabel databaseLabel;
	private JLabel schemaLabel;
	private JLabel workspaceLabel;
	private JLabel timestampLabel;

	private DatabaseOperationsPanel operationsPanel;

	private DatabaseConfig databaseConfig;
	private boolean isSettingsLoaded;

	public DatabasePanel(ViewController viewController, Config config) {
		this.viewController = viewController;
		this.config = config;

		databaseController = ObjectRegistry.getInstance().getDatabaseController();

		initGui();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);
	}

	private boolean isModified() {
		DatabaseConnection databaseConnection = (DatabaseConnection) connCombo.getSelectedItem();
		if (!descriptionText.getText().trim().equals(databaseConnection.getDescription())) return true;
		if (databaseTypeCombo.getSelectedItem() != databaseConnection.getDatabaseType()) return true;
		if (!serverText.getText().equals(databaseConnection.getServer())) return true;
		if (!userText.getText().equals(databaseConnection.getUser())) return true;
		if (!String.valueOf(passwordText.getPassword()).equals(databaseConnection.getPassword())) return true;
		if (!databaseText.getText().equals(databaseConnection.getSid())) return true;
		if (passwordCheck.isSelected() != databaseConnection.isSetSavePassword()) return true;
		if (portText.getValue() != null && ((Number) portText.getValue()).intValue() != databaseConnection.getPort()) return true;

		String schema = getValue(schemaCombo);
		if (schema != null && !schema.equals(databaseConnection.getSchema())) return true;
		if (schema == null && databaseConnection.getSchema() != null) return true;

		if (databaseConnection.getDatabaseType() == DatabaseType.ORACLE) {
			String workspace = getValue(workspaceCombo);
			if (!databaseConnection.isSetWorkspace() && (workspace != null || timestamp.getDate() != null)) return true;
			if (databaseConnection.isSetWorkspace()) {
				if (workspace == null || !workspace.equals(databaseConnection.getWorkspace().getName())) return true;
				if (timestamp.getDate() == null) {
					if (databaseConnection.getWorkspace().getTimestamp() != null) return true;
				} else {
					if (!timestamp.getDate().equals(databaseConnection.getWorkspace().getTimestamp())) return true;
				}
			}
		}

		return false;
	}

	private void initGui() {
        connLabel = new JLabel();
        descriptionLabel = new JLabel();
        databaseTypeLabel = new JLabel();
        userLabel = new JLabel();
        passwordLabel = new JLabel();
        serverLabel = new JLabel();
        portLabel = new JLabel();
        databaseLabel = new JLabel();
        schemaLabel = new JLabel();
        workspaceLabel = new JLabel();
        timestampLabel = new JLabel();

		connCombo = new JComboBox<>();
		descriptionText = new JTextField();
		databaseTypeCombo = new JComboBox<>();
		serverText = new JTextField();

		NumberFormatter format = new NumberFormatter(new DecimalFormat("#"));
		format.setMaximum(99999);
		format.setMinimum(0);
		portText = new JFormattedTextField(format);
		portText.setColumns(10);

		databaseText = new JTextField();
		databaseText = new JTextField();
		userText = new JTextField();
		passwordCheck = new JCheckBox();
		passwordText = new JPasswordField();
		passwordText.putClientProperty("FlatLaf.style", "showRevealButton: true");

		schemaCombo = new FlatComboBox<>();
		schemaCombo.setEditable(true);
		workspaceCombo = new FlatComboBox<>();
		workspaceCombo.setEditable(true);
		timestamp = new DatePicker();

		applyButton = new JButton();
		newButton = new JButton();
		copyButton = new JButton();
		deleteButton = new JButton();
		connectButton = new JButton();
		infoButton = new JButton();
		schemaButton = new JButton();
		workspaceButton = new JButton();

		PopupMenuDecorator.getInstance().decorate(descriptionText, serverText, portText, databaseText,
				userText, passwordText, (JTextField) schemaCombo.getEditor().getEditorComponent(),
				(JTextField) workspaceCombo.getEditor().getEditorComponent());

		JPanel chooserPanel = new JPanel();
		chooserPanel.setLayout(new GridBagLayout());

        chooserPanel.add(connLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
        chooserPanel.add(connCombo, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));

        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());

        content.add(descriptionLabel, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
        content.add(descriptionText, GuiUtil.setConstraints(1, 0, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
        content.add(userLabel, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
        content.add(userText, GuiUtil.setConstraints(1, 1, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
        content.add(passwordLabel, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
        content.add(passwordText, GuiUtil.setConstraints(1, 2, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
        content.add(passwordCheck, GuiUtil.setConstraints(1, 3, 0, 0, GridBagConstraints.BOTH, 0, 5, 15, 0));
        content.add(databaseTypeLabel, GuiUtil.setConstraints(0, 4, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
        content.add(databaseTypeCombo, GuiUtil.setConstraints(1, 4, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
        content.add(serverLabel, GuiUtil.setConstraints(0, 5, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));

		JPanel serverPanel = new JPanel();
		serverPanel.setLayout(new GridBagLayout());
        serverPanel.add(serverText, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));
        serverPanel.add(portLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.BOTH, 0, 10, 0, 5));
        serverPanel.add(portText, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));
		portText.setMinimumSize(portText.getPreferredSize());

        content.add(serverPanel, GuiUtil.setConstraints(1, 5, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
        content.add(databaseLabel, GuiUtil.setConstraints(0, 6, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
        content.add(databaseText, GuiUtil.setConstraints(1, 6, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));
        content.add(schemaLabel, GuiUtil.setConstraints(0, 7, 0, 0, GridBagConstraints.BOTH, 0, 0, 5, 5));
		content.add(workspaceLabel, GuiUtil.setConstraints(0, 8, 0, 0, GridBagConstraints.BOTH, 0, 0, 0, 5));

		JPanel schemaPanel = new JPanel();
		schemaPanel.setLayout(new GridBagLayout());
        schemaPanel.add(schemaCombo, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
        schemaPanel.add(schemaButton, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
        content.add(schemaPanel, GuiUtil.setConstraints(1, 7, 1, 0, GridBagConstraints.BOTH, 0, 5, 5, 0));

		workspacePanel = new JPanel();
		workspacePanel.setLayout(new GridBagLayout());
		workspacePanel.add(workspaceCombo, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 5));
		workspacePanel.add(timestampLabel, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 10, 0, 5));
		workspacePanel.add(timestamp, GuiUtil.setConstraints(2, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 5));
		workspacePanel.add(workspaceButton, GuiUtil.setConstraints(3, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 5, 0, 0));
		content.add(workspacePanel, GuiUtil.setConstraints(1, 8, 1, 0, GridBagConstraints.BOTH, 0, 5, 0, 0));

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        buttons.add(applyButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
        buttons.add(newButton, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
        buttons.add(copyButton, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
        buttons.add(deleteButton, GuiUtil.setConstraints(0, 3, 0, 0, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));
        buttons.add(infoButton, GuiUtil.setConstraints(0, 4, 0, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 5, 0, 0, 0));

        content.add(buttons, GuiUtil.setConstraints(2, 0, 1, 8, 0, 0, GridBagConstraints.BOTH, 0, 20, 0, 0));

		connectionDetails = new TitledPanel().build(content);
		operationsPanel = new DatabaseOperationsPanel(viewController, config);
        JPanel view = new ScrollablePanel(true, false);
        view.setLayout(new GridBagLayout());
        view.add(chooserPanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 15, 10, 15, 10));
        view.add(connectionDetails, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.BOTH, 0, 10, 0, 10));
		view.add(connectButton, GuiUtil.setConstraints(0, 2, 0, 0, GridBagConstraints.NONE, 0, 10, 15, 10));
        view.add(operationsPanel, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.BOTH, 10, 10, 0, 10));

        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());
		add(scrollPane);

		// influence focus behavior
		applyButton.setFocusable(false);
		newButton.setFocusable(false);
		copyButton.setFocusable(false);
		deleteButton.setFocusable(false);
		infoButton.setFocusable(false);
		schemaButton.setFocusable(false);
		workspaceButton.setFocusable(false);

		for (DatabaseType type : DatabaseType.values())
			databaseTypeCombo.addItem(type);

		applyButton.addActionListener(e -> apply());

		newButton.addActionListener(e -> {
			if (requestChange())
				newConn();
		});

		copyButton.addActionListener(e -> {
			if (requestChange())
				copy();
		});

		deleteButton.addActionListener(e -> {
			if (requestDelete())
				delete();
		});

		connectButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				if (!databaseController.isConnected()) {
					connect();
				} else {
					disconnect();
				}

				return null;
			}
		}.execute());

		infoButton.addActionListener(e -> {
			viewController.clearConsole();
			log.info("Connected to database profile '" + databaseController.getActiveDatabaseAdapter().getConnectionDetails().getDescription() + "'.");
			databaseController.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();
		});

		schemaButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {
				getValues(schemaCombo, DatabasePanel.this::fetchSchemas);
				return null;
			}
		}.execute());

		workspaceButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() throws Exception {
				getValues(workspaceCombo, DatabasePanel.this::fetchWorkspaces);
				return null;
			}
		}.execute());

		connCombo.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
				selectConnection();
		});

		databaseTypeCombo.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (e.getItem() == DatabaseType.ORACLE) {
					portText.setValue(1521);
					setWorkspaceVisible(true);
				} else {
					portText.setValue(5432);
					setWorkspaceVisible(false);
				}
			}
		});
	}

	@Override
	public void switchLocale(Locale locale) {
        connectionDetails.setTitle(Language.I18N.getString("db.border.connectionDetails"));
		connLabel.setText(Language.I18N.getString("db.label.connection"));
		descriptionLabel.setText(Language.I18N.getString("db.label.description"));
		databaseTypeLabel.setText(Language.I18N.getString("db.label.databaseType"));
		userLabel.setText(Language.I18N.getString("common.label.username"));
		passwordLabel.setText(Language.I18N.getString("common.label.password"));
		serverLabel.setText(Language.I18N.getString("common.label.server"));
		portLabel.setText(Language.I18N.getString("common.label.port"));
		databaseLabel.setText(Language.I18N.getString("db.label.sid"));
		schemaLabel.setText(Language.I18N.getString("common.label.schema"));
		schemaCombo.setPlaceholderText(Language.I18N.getString("common.label.schema.prompt"));
		passwordCheck.setText(Language.I18N.getString("common.label.passwordCheck"));
		applyButton.setText(Language.I18N.getString("common.button.apply"));
		newButton.setText(Language.I18N.getString("db.button.new"));
		copyButton.setText(Language.I18N.getString("db.button.copy"));
		deleteButton.setText(Language.I18N.getString("db.button.delete"));
		infoButton.setText(Language.I18N.getString("db.button.info"));
		schemaButton.setText(Language.I18N.getString("common.button.query"));
		workspaceLabel.setText(Language.I18N.getString("common.label.workspace"));
		workspaceCombo.setPlaceholderText(Language.I18N.getString("common.label.workspace.prompt"));
		timestampLabel.setText(Language.I18N.getString("common.label.timestamp"));
		workspaceButton.setText(Language.I18N.getString("common.button.query"));
		operationsPanel.switchLocale(locale);

        connectButton.setText(Language.I18N.getString(!databaseController.isConnected() ?
				"db.button.connect" :
				"db.button.disconnect"));
	}

	private void selectConnection() {
		if (isSettingsLoaded)
			setDbConnection(databaseConfig.getActiveConnection());

		DatabaseConnection databaseConnection = (DatabaseConnection)connCombo.getSelectedItem();
		databaseConfig.setActiveConnection(databaseConnection);
		getDbConnection(databaseConnection);
	}

	private void apply() {
		DatabaseConnection databaseConnection = (DatabaseConnection)connCombo.getSelectedItem();
		setDbConnection(databaseConnection);

		List<DatabaseConnection> connList = databaseConfig.getConnections();
		Collections.sort(connList);

		connCombo.removeAllItems();
		for (DatabaseConnection conn : connList)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(databaseConnection);
		log.info("Settings successfully applied.");
	}

	private void copy() {
		DatabaseConnection source = (DatabaseConnection)connCombo.getSelectedItem();
		DatabaseConnection databaseConnection = new DatabaseConnection();
		setDbConnection(databaseConnection);
		databaseConnection.setDescription(getCopyOfDescription(source));

		databaseConfig.getConnections().add(0, databaseConnection);
		connCombo.insertItemAt(databaseConnection, 0);
		connCombo.setSelectedItem(databaseConnection);
	}

	private void newConn() {
		DatabaseConnection databaseConnection = new DatabaseConnection();
		databaseConnection.setDescription(getNewConnDescription());

		databaseConfig.getConnections().add(0, databaseConnection);
		connCombo.insertItemAt(databaseConnection, 0);
		connCombo.setSelectedItem(databaseConnection);
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

	public void connect() {
		final ReentrantLock lock = this.mainLock;
		if (lock.tryLock()) {
			try {
				databaseController.connect();
			} finally {
				lock.unlock();
			}
		}
	}

	public void disconnect() {
		final ReentrantLock lock = this.mainLock;
		if (lock.tryLock()) {
			try {
				databaseController.disconnect();
			} finally {
				lock.unlock();
			}
		}
	}

	private List<String> fetchSchemas(AbstractDatabaseAdapter adapter, DatabaseConnection connection) {
		try {
			return adapter.getSchemaManager().fetchSchemasFromDatabase(connection);
		} catch (SQLException e) {
			showError(e);
			return null;
		}
	}

	private List<String> fetchWorkspaces(AbstractDatabaseAdapter adapter, DatabaseConnection connection) {
		try {
			return adapter.getWorkspaceManager().fetchWorkspacesFromDatabase(connection);
		} catch (SQLException e) {
			showError(e);
			return null;
		}
	}

	@Override
	public void commitConnectionDetails() {
		setSettings();
	}

	@Override
	public void showConnectionStatus(ConnectionState state) {
		switch (state) {
			case INIT_CONNECT:
				viewController.setStatusText(Language.I18N.getString("main.status.database.connect.label"));
				break;
			case INIT_DISCONNECT:
				viewController.setStatusText(Language.I18N.getString("main.status.database.disconnect.label"));
				break;
			case FINISH_CONNECT:
			case FINISH_DISCONNECT:
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
				break;
		}
	}

	@Override
	public void showError(DatabaseConfigurationException e) {
		String message;
		switch (e.getErrorCode()) {
			case MISSING_DB_USERNAME:
				message = Language.I18N.getString("db.dialog.error.conn.user");
				break;
			case MISSING_DB_HOSTNAME:
				message = Language.I18N.getString("db.dialog.error.conn.server");
				break;
			case MISSING_DB_PORT:
				message = Language.I18N.getString("db.dialog.error.conn.port");
				break;
			case MISSING_DB_NAME:
				message = Language.I18N.getString("db.dialog.error.conn.sid");
				break;
			case EMPTY_DB_SCHEMA:
				message = Language.I18N.getString("db.dialog.error.conn.emptySchema");
				break;
			case INVALID_DB_WORKSPACE:
				message = Language.I18N.getString("db.dialog.error.conn.invalidWorkspace");
				break;
			default:
				message = e.getMessage();
		}

		viewController.errorMessage(Language.I18N.getString("db.dialog.error.conn.title"), message);
	}

	@Override
	public void showError(DatabaseVersionException e) {
		viewController.errorMessage(Language.I18N.getString("db.dialog.error.version.title"), e.getFormattedMessage());
	}

	@Override
	public void showError(SQLException e) {
		String text = Language.I18N.getString("db.dialog.error.openConn");
		viewController.errorMessage(Language.I18N.getString("common.dialog.error.db.title"),
				MessageFormat.format(text, e.getMessage().replaceAll("\\n", "")));
	}

	@Override
	public boolean showWarning(DatabaseConnectionWarning warning) {
		int option = JOptionPane.OK_OPTION;

		if (!(warning.getType() instanceof ConnectionWarningType)) {
			String text = Language.I18N.getString("db.dialog.warn.general");
			option = viewController.warnMessage(Language.I18N.getString("db.dialog.warn.title"), MessageFormat.format(text, warning.getMessage()));
		} else {
			DatabaseGuiConfig guiConfig = config.getGuiConfig().getDatabaseGuiConfig();
			boolean showWarning = false;
			switch ((ConnectionWarningType) warning.getType()) {
				case OUTDATED_DATABASE_VERSION:
					showWarning = guiConfig.isShowOutdatedDatabaseVersionWarning();
					break;
				case UNSUPPORTED_ADE:
					showWarning = guiConfig.isShowUnsupportedADEWarning();
					break;
			}

			if (showWarning) {
				ConfirmationCheckDialog dialog = ConfirmationCheckDialog.defaults()
						.withParentComponent(viewController.getTopFrame())
						.withOptionType(JOptionPane.OK_CANCEL_OPTION)
						.withMessageType(JOptionPane.WARNING_MESSAGE)
						.withTitle(Language.I18N.getString("db.dialog.warn.title"))
						.addMessage(warning.getFormattedMessage());

				option = dialog.show();
				if (!dialog.keepShowingDialog()) {
					switch ((ConnectionWarningType) warning.getType()) {
						case OUTDATED_DATABASE_VERSION:
							guiConfig.setShowOutdatedDatabaseVersionWarning(false);
							break;
						case UNSUPPORTED_ADE:
							guiConfig.setShowUnsupportedADEWarning(false);
							break;
					}
				}
			}
		}

		return option == JOptionPane.OK_OPTION;
	}

	@Override
	public void loadSettings() {
		if (databaseController.isConnected())
			disconnect();

		databaseConfig = config.getDatabaseConfig();
		isSettingsLoaded = false;

		connCombo.removeAllItems();
		DatabaseConnection databaseConnection = databaseConfig.getActiveConnection();
		List<DatabaseConnection> connections = databaseConfig.getConnections();

		if (databaseConnection == null) {
			if (!connections.isEmpty())
				databaseConnection = connections.get(0);
			else {
				databaseConnection = new DatabaseConnection();
				databaseConnection.setDescription(Language.I18N.getString("db.label.newConnection"));
				databaseConfig.addConnection(databaseConnection);
			}
		}

		Collections.sort(connections);
		for (DatabaseConnection conn : connections)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(databaseConnection);
		setWorkspaceVisible(databaseTypeCombo.getSelectedItem() == DatabaseType.ORACLE);
		operationsPanel.loadSettings();
		setEnabledDBOperations(false);
		isSettingsLoaded = true;
	}

	@Override
	public void setSettings() {
		setDbConnection((DatabaseConnection)connCombo.getSelectedItem());
		operationsPanel.setSettings();
	}

	private void setDbConnection(DatabaseConnection databaseConnection) {
		String description = descriptionText.getText().trim();
		if (description.length() > 0) {
			boolean repaint = databaseConnection == databaseConfig.getActiveConnection() && !description.equals(databaseConnection.getDescription());
			databaseConnection.setDescription(description);
			if (repaint)
				connCombo.repaint();
		} else
			descriptionText.setText(databaseConnection.getDescription());

		databaseConnection.setDatabaseType((DatabaseType) databaseTypeCombo.getSelectedItem());
		databaseConnection.setServer(serverText.getText());
		databaseConnection.setPort(((Number) portText.getValue()).intValue());
		databaseConnection.setSid(databaseText.getText());
		databaseConnection.setSchema(getValue(schemaCombo));
		databaseConnection.setUser(userText.getText());
		databaseConnection.setPassword(String.valueOf(passwordText.getPassword()));
		databaseConnection.setSavePassword(passwordCheck.isSelected());

		String workspace = getValue(workspaceCombo);
		if (databaseTypeCombo.getSelectedItem() == DatabaseType.ORACLE
				&& (workspace != null
				|| timestamp.getDate() != null)) {
			databaseConnection.setWorkspace(new Workspace(workspace, timestamp.getDate()));
		} else {
			databaseConnection.setWorkspace(null);
		}
	}

	private void getDbConnection(DatabaseConnection databaseConnection) {
		descriptionText.setText(databaseConnection.getDescription());
		databaseTypeCombo.setSelectedItem(databaseConnection.getDatabaseType());
		serverText.setText(databaseConnection.getServer());
		databaseText.setText(databaseConnection.getSid());
		userText.setText(databaseConnection.getUser());
		passwordText.setText(databaseConnection.getPassword());
		passwordCheck.setSelected(databaseConnection.isSetSavePassword());
		schemaCombo.removeAllItems();
		schemaCombo.setSelectedItem(databaseConnection.getSchema());

		if (databaseConnection.getPort() == null || databaseConnection.getPort() == 0) {
			databaseConnection.setPort(5432);
		}

		portText.setValue(databaseConnection.getPort());

		if (databaseConnection.getDatabaseType() == DatabaseType.ORACLE) {
			workspaceCombo.removeAllItems();
			if (databaseConnection.isSetWorkspace()) {
				Workspace workspace = databaseConnection.getWorkspace();
				workspaceCombo.setSelectedItem(workspace.getName());
				timestamp.setDate(workspace.getTimestamp());
			} else {
				timestamp.setDate(null);
			}
		}
	}

	private String getCopyOfDescription(DatabaseConnection databaseConnection) {
		// pattern: "connectionName - copy 1"
		// so to retrieve connectionName, " - copy*" has to be deleted...

		int nr = 0;
		String name = databaseConnection.getDescription().replaceAll("\\s*-\\s*" + Language.I18N.getString("db.label.copyConnection") + ".*$", "");
		String copy = name + " - " + Language.I18N.getString("db.label.copyConnection");
		for (DatabaseConnection conn : databaseConfig.getConnections())
			if (conn.getDescription().replaceAll("\\s*-\\s*" + Language.I18N.getString("db.label.copyConnection") + ".*$", "").equalsIgnoreCase(name))
				nr++;

		if (nr > 1)
			return copy + " " + nr;
		else
			return copy;
	}

	private String getNewConnDescription() {
		int nr = 1;
		String name = Language.I18N.getString("db.label.newConnection");
		for (DatabaseConnection conn : databaseConfig.getConnections())
			if (conn.getDescription().toLowerCase().startsWith(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return name + " " + nr;
		else
			return name;
	}

	private boolean requestChange() {
		if (isModified()) {
			int option = viewController.showOptionDialog(Language.I18N.getString("db.dialog.apply.title"),
					Language.I18N.getString("db.dialog.apply.msg"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			switch (option) {
				case JOptionPane.CANCEL_OPTION:
					return false;
				case JOptionPane.YES_OPTION:
					apply();
					break;
				default:
					getDbConnection((DatabaseConnection) connCombo.getSelectedItem());
					break;
			}
		}

		return true;
	}

	private boolean requestDelete() {
		DatabaseConnection databaseConnection = (DatabaseConnection) connCombo.getSelectedItem();
		return viewController.showOptionDialog(Language.I18N.getString("db.dialog.delete.title"),
				MessageFormat.format(Language.I18N.getString("db.dialog.delete.msg"), databaseConnection.getDescription()),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
	}

	private void setEnabledDBOperations(boolean enable) {
		infoButton.setEnabled(enable);
		operationsPanel.setEnabled(enable);
	}

	private void setWorkspaceVisible(boolean visible) {
		workspaceLabel.setVisible(visible);
		workspacePanel.setVisible(visible);
	}

	private String getValue(JComboBox<String> comboBox) {
		String value = (String) (comboBox.getSelectedIndex() != -1 ?
				comboBox.getSelectedItem() :
				comboBox.getEditor().getItem());

		value = value != null && value.trim().isEmpty() ? null : value;
		if (value == null && comboBox.getSelectedItem() != null) {
			comboBox.setSelectedItem(null);
		}

		return value;
	}

	private void getValues(JComboBox<String> comboBox, BiFunction<AbstractDatabaseAdapter, DatabaseConnection, List<String>> function) {
		final ReentrantLock lock = this.mainLock;
		if (lock.tryLock()) {
			try {
				commitConnectionDetails();
				DatabaseConnection connection = (DatabaseConnection) connCombo.getSelectedItem();
				connection.validate();

				AbstractDatabaseAdapter adapter = DatabaseAdapterFactory.getInstance().createDatabaseAdapter(connection.getDatabaseType());
				List<String> values = function.apply(adapter, connection);
				if (values != null && !values.isEmpty()) {
					String value = (String) comboBox.getSelectedItem();
					comboBox.removeAllItems();
					values.forEach(comboBox::addItem);
					comboBox.setSelectedItem(value);
					comboBox.setPopupVisible(true);
				}
			} catch (DatabaseConfigurationException e) {
				showError(e);
			} finally {
				lock.unlock();
			}
		}
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
			setDbConnection((DatabaseConnection)connCombo.getSelectedItem());
	}

}
