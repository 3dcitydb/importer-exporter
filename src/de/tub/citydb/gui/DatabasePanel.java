package de.tub.citydb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.DBVersioning;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.gui.components.DigitsOnlyDocument;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.DBUtil;

public class DatabasePanel extends JPanel implements PropertyChangeListener {
	private ReentrantLock mainLock = new ReentrantLock();
	private Logger LOG = Logger.getInstance();

	private JComboBox connCombo;
	private JTextField descriptionText;
	private JTextField serverText;
	private JTextField portText;
	private JTextField databaseText;
	private JTextField userText;
	private JTextField workspaceReportText;
	private JPasswordField passwordText;
	private JCheckBox passwordCheck;
	private JButton saveButton;
	private JButton newButton;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton reportButton;
	private JButton connectButton;

	private JPanel row2;
	private JPanel row3;
	private JLabel row1_1;
	private JLabel row2_1;
	private JLabel row2_2;
	private JLabel row2_3;
	private JLabel row2_4;
	private JLabel row2_5;
	private JLabel row2_6;
	private JLabel row3_1;

	private Config config;
	private Database databaseConfig;
	private ImpExpGui topFrame;

	public DatabasePanel(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;
		initGui();		
		config.getInternal().addPropertyChangeListener(this);
		databaseConfig = config.getProject().getDatabase();
	}

	private boolean isModified() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();	
		if (!descriptionText.getText().trim().equals(dbConnection.getDescription())) return true;
		if (!serverText.getText().equals(dbConnection.getServer())) return true;
		if (!userText.getText().equals(dbConnection.getUser())) return true;		
		if (!String.valueOf(passwordText.getPassword()).equals(config.getInternal().getCurrentDbPassword())) return true;
		if (!databaseText.getText().equals(dbConnection.getSid())) return true;
		if (passwordCheck.isSelected() != dbConnection.isSetSavePassword()) return true;		
		try {
			int port = Integer.valueOf(portText.getText());
			if (port != dbConnection.getPort()) return true;
		} catch (NumberFormatException nfe) {
			return true;
		}			
		return false;
	}

	private void initGui() {

		//gui-elemente anlegen
		connCombo = new JComboBox();
		descriptionText = new JTextField("");
		serverText = new JTextField("");
		portText = new JTextField(new DigitsOnlyDocument(5), "" , 5);
		databaseText = new JTextField("");
		userText = new JTextField("");
		workspaceReportText = new JTextField("");
		passwordText = new JPasswordField("");
		passwordCheck = new JCheckBox("");
		saveButton = new JButton("");
		newButton = new JButton("");
		copyButton = new JButton("");
		deleteButton = new JButton("");
		reportButton = new JButton("");
		connectButton = new JButton("");

		reportButton.setEnabled(false);
		connectButton.setEnabled(true);

		// Listener
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
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
						connect();
					}
				};
				thread.start();
			}
		});

		reportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						report();
					}
				};
				thread.start();
			}
		});

		connCombo.addItemListener( new ItemListener() {
			public void itemStateChanged( ItemEvent e ) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					selectConnection();
			}
		});

		//layout
		setLayout(new GridBagLayout());

		{
			JPanel row1 = new JPanel();
			add(row1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,10,5,5,5));
			row1.setLayout(new GridBagLayout());
			row1_1 = new JLabel();
			{
				row1.add(row1_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
				row1.add(connCombo, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			}
		}

		{
			row2 = new JPanel();
			add(row2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			row2.setBorder(BorderFactory.createTitledBorder(""));
			row2.setLayout(new GridBagLayout());
			row2_1 = new JLabel();
			row2_2 = new JLabel();
			row2_3 = new JLabel();
			row2_4 = new JLabel();
			row2_5 = new JLabel();
			row2_6 = new JLabel();
			passwordCheck.setIconTextGap(10);
			{
				row2.add(row2_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(descriptionText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(row2_2, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(userText, GuiUtil.setConstraints(1,1,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(row2_3, GuiUtil.setConstraints(0,2,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(passwordText, GuiUtil.setConstraints(1,2,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(passwordCheck, GuiUtil.setConstraints(1,3,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(row2_4, GuiUtil.setConstraints(0,4,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(serverText, GuiUtil.setConstraints(1,4,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(row2_5, GuiUtil.setConstraints(0,5,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				GridBagConstraints c = GuiUtil.setConstraints(1,5,0.0,0.0,GridBagConstraints.NONE,0,5,5,5);
				c.anchor = GridBagConstraints.WEST;
				row2.add(portText, c);
				row2.add(row2_6, GuiUtil.setConstraints(0,6,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row2.add(databaseText, GuiUtil.setConstraints(1,6,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				JPanel row2_buttons = new JPanel();
				JPanel row2_buttonsBox = new JPanel();
				GridBagConstraints constraint = new GridBagConstraints();
				constraint = GuiUtil.setConstraints(0,7,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5);
				constraint.gridwidth = 2;
				row2.add(row2_buttons,constraint);
				row2_buttons.setLayout(new GridBagLayout());
				row2_buttonsBox.setLayout(new GridBagLayout());
				row2_buttons.add(row2_buttonsBox, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,5,0,5));				
				row2_buttonsBox.add(saveButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,5,0,5));
				row2_buttonsBox.add(newButton, GuiUtil.setConstraints(1,0,0.0,0.0,GridBagConstraints.NONE,0,5,0,5));
				row2_buttonsBox.add(copyButton, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,5,0,5));
				row2_buttonsBox.add(deleteButton, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.NONE,0,5,0,5));
			}
		}
		{
			row3 = new JPanel();
			add(row3, GuiUtil.setConstraints(0,3,1.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			row3.setBorder(BorderFactory.createTitledBorder(""));
			row3.setLayout(new GridBagLayout());
			{
				row3_1 = new JLabel();
				row3.add(row3_1, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row3.add(workspaceReportText, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
				row3.add(reportButton, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.BOTH,0,5,5,5));
			}
		}
		{
			JPanel row4 = new JPanel();
			add(row4, GuiUtil.setConstraints(0,4,1.0,0.0,GridBagConstraints.BOTH,0,5,15,5));
			row4.setLayout(new GridBagLayout());
			{
				
				row4.add(connectButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,5,5,5,5));
			}
		}
		{
			JPanel row5 = new JPanel();
			add(row5, GuiUtil.setConstraints(0,5,1.0,1.0,GridBagConstraints.BOTH,20,5,15,5));
		}
		this.setEnabled(false);
	}

	public void doTranslation() {

		//internationalisierte Labels und Strings
		row2.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("db.border.connectionDetails")));
		row1_1.setText(ImpExpGui.labels.getString("db.label.connection"));
		row2_1.setText(ImpExpGui.labels.getString("db.label.description"));
		row2_2.setText(ImpExpGui.labels.getString("db.label.user"));
		row2_3.setText(ImpExpGui.labels.getString("db.label.password"));
		row2_4.setText(ImpExpGui.labels.getString("db.label.server"));
		row2_5.setText(ImpExpGui.labels.getString("db.label.port"));
		row2_6.setText(ImpExpGui.labels.getString("db.label.sid"));
		row3.setBorder(BorderFactory.createTitledBorder(ImpExpGui.labels.getString("db.border.databaseReport")));
		row3_1.setText(ImpExpGui.labels.getString("db.label.workspace"));
		passwordCheck.setText(ImpExpGui.labels.getString("db.label.passwordCheck"));
		saveButton.setText(ImpExpGui.labels.getString("db.button.save"));
		newButton.setText(ImpExpGui.labels.getString("db.button.new"));
		copyButton.setText(ImpExpGui.labels.getString("db.button.copy"));
		deleteButton.setText(ImpExpGui.labels.getString("db.button.delete"));
		reportButton.setText(ImpExpGui.labels.getString("db.button.report"));

		if (!config.getInternal().isDbIsConnected())
			connectButton.setText(ImpExpGui.labels.getString("db.button.connect"));
		else
			connectButton.setText(ImpExpGui.labels.getString("db.button.disconnect"));
	}

	private void selectConnection() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		databaseConfig.setActiveConnection(dbConnection);
		getDbConnection(dbConnection);
	}

	private void save() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		setDbConnection(dbConnection);

		List<DBConnection> connList = databaseConfig.getConnections();
		Collections.sort(connList);

		connCombo.removeAllItems();
		for (DBConnection conn : connList)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(dbConnection);
		topFrame.saveIniFile();
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

	public void connect() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			setSettings();
			Internal intConfig = config.getInternal();
			DBConnectionPool dbPool = topFrame.getDBPool();

			if (!intConfig.isDbIsConnected()) {
				DBConnection dbConnection = config.getProject().getDatabase().getActiveConnection();

				// check for valid input
				if (dbConnection.getServer().trim().equals("")) {
					topFrame.errorMessage("Verbindungsfehler", "Ihre Angaben zur Datenbankverbindung sind unvollständig.\n\nBitte geben Sie einen Server an.");
					return;
				}

				if (dbConnection.getPort() == null) {
					topFrame.errorMessage("Verbindungsfehler", "Ihre Angaben zur Datenbankverbindung sind unvollständig.\n\nBitte geben Sie einen gültigen Port an.");
					return;
				}

				if (dbConnection.getSid().trim().equals("")) {
					topFrame.errorMessage("Verbindungsfehler", "Ihre Angaben zur Datenbankverbindung sind unvollständig.\n\nBitte geben Sie eine SID an.");
					return;
				}

				if (dbConnection.getUser().trim().equals("")) {
					topFrame.errorMessage("Verbindungsfehler", "Ihre Angaben zur Datenbankverbindung sind unvollständig.\n\nBitte geben Sie einen Benutzernamen an.");
					return;
				}

				if (intConfig.getCurrentDbPassword().trim().equals("")) {
					topFrame.errorMessage("Verbindungsfehler", "Ihre Angaben zur Datenbankverbindung sind unvollständig.\n\nBitte geben Sie ein Passwort an.");
					return;
				}

				topFrame.getStatusText().setText(ImpExpGui.labels.getString("main.status.database.connect.label"));
				LOG.info("Verbinde zu Datenbank-Profil '" + dbConnection.getDescription().trim() + "'.");
				
				try {
					dbPool.init(config);
					DBUtil dbUtil = new DBUtil(dbPool);
					String[] dbInfo = dbUtil.getDatabaseInfo();

					if (dbInfo != null) {
						if (dbInfo[0] != null)
							intConfig.setDbSrid(dbInfo[0]);
						else
							intConfig.setDbSrid("81989002");

						if (dbInfo[1] != null)
							intConfig.setDbSrsName(dbInfo[1]);
						else
							intConfig.setDbSrsName("urn:ogc:def:crs,crs:EPSG:6.12:3068,crs:EPSG:6.12:5783");
						
						if (dbInfo[2] != null)
							intConfig.setDbVersioning(DBVersioning.fromValue(dbInfo[2]));
						else
							intConfig.setDbVersioning(DBVersioning.OFF);
					}

					intConfig.setDbIsConnected(true);
				} catch (SQLException sqlEx) {
					topFrame.errorMessage("Verbindungsfehler", "Es konnte keine Verbindung zur Datenbank hergestellt werden:\n\n" + sqlEx.getMessage());
					intConfig.setDbIsConnected(false);
					dbPool.forceClose();
					LOG.error("Verbindungsfehler: " + sqlEx.getMessage().trim());
				}

				if (!intConfig.isDbIsConnected()) {
					topFrame.getConnectText().setText(ImpExpGui.labels.getString("main.status.database.disconnected.label"));
				}
				else {
					topFrame.getConnectText().setText(ImpExpGui.labels.getString("main.status.database.connected.label"));
					connectButton.setText(ImpExpGui.labels.getString("db.button.disconnect"));
					LOG.info("Verbindung zur Datenbank hergestellt.");
					LOG.info("Datenbank SRID: " + intConfig.getDbSrid());
					LOG.info("Datenbank GML_SRS_Name: " + intConfig.getDbSrsName());
					LOG.info("Datenbank-Versionierung: " + intConfig.getDbVersioning());
				}
			}

			else {
				topFrame.getStatusText().setText(ImpExpGui.labels.getString("main.status.database.disconnect.label"));
				intConfig.setDbIsConnected(false);

				try {
					dbPool.close();
				} catch (SQLException sqlEx) {
					LOG.error("Verbindungsfehler: " + sqlEx.getMessage().trim());
					LOG.error("Erzwinge Verbindungsabbruch...");
					dbPool.forceClose();
					topFrame.errorMessage("Verbindungsfehler", "Die Verbindung zur Datenbank konnte nicht korrekt getrennt werden:\n\n" + sqlEx.getMessage());
				}

				topFrame.getConnectText().setText(ImpExpGui.labels.getString("main.status.database.disconnected.label"));
				connectButton.setText(ImpExpGui.labels.getString("db.button.connect"));
				LOG.info("Verbindung zur Datenbank getrennt.");
			}

			topFrame.getStatusText().setText(ImpExpGui.labels.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void report() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			topFrame.getConsoleText().setText("");
			topFrame.getStatusText().setText(ImpExpGui.labels.getString("main.status.database.report.label"));
			
			LOG.info("Erstelle Datenbankbericht...");			
			final DBUtil geodbReportReader;
			try {
				geodbReportReader = new DBUtil(topFrame.getDBPool());
			} catch (SQLException sqlEx) {
				topFrame.errorMessage("Datenbankfehler", "Datenbankbericht konnte nicht erstellt werden:\n\n" + sqlEx.getMessage());
				LOG.error("Datenbankfehler: " + sqlEx.getMessage().trim());
				return;
			}

			String[] msg = {"Das Erstellen des Reports kann je nach Datenbankgröße","sehr viel Zeit in Anspruch nehmen."};
			
			final StatusDialog reportDialog = new StatusDialog(topFrame, "Datenbankbericht", "Erstelle Datenbankbericht...", msg, true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					reportDialog.pack();
					reportDialog.setLocationRelativeTo(getTopLevelAncestor());
					reportDialog.setVisible(true);
				}
			});

			reportDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							reportDialog.setHeader("Abbruch - Bitte warten...");							
							geodbReportReader.cancelProcedure();
						}
					});
				}
			});
											
			String[] report = null;
			String dbSqlEx = null;
			try {
				// checking workspace... this should be improved in future...
				DBConnectionPool dbPool = topFrame.getDBPool();
				String workspace = workspaceReportText.getText();
				if (workspace != null) {
					workspace = workspace.trim();

					if (!workspace.toUpperCase().equals("LIVE")) {
						boolean workspaceExists = dbPool.checkWorkspace(workspace);

						if (!workspaceExists) {
							LOG.error("Der Datenbank-Workspace '" + workspace + "' ist nicht verfügbar.");
							return;
						} else {
							LOG.info("Der Datenbank-Workspace '" + workspace + "' wird gewählt.");
						}
					}
				}
				
				report = geodbReportReader.databaseReport(workspace);

				if (report != null) {
					System.out.println();

					for(String line : report) {
						if (line != null) {
							line = line.replaceAll("\\\\n", "\\\n");
							line = line.replaceAll("\\\\t", "\\\t");
							System.out.println(line);
						}
					}

					System.out.println();	
				}

			} catch (SQLException sqlEx) {
				dbSqlEx = sqlEx.getMessage();
			} finally {			
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reportDialog.dispose();
					}
				});

				if (report != null)  {
					LOG.info("Datenbankbericht erfolgreich erstellt.");
				} else {

					if (dbSqlEx == null) {
						LOG.warn("Erstellung des Datenbankberichts abgebrochen.");
					} else  {
						topFrame.errorMessage("Datenbankfehler", "Datenbankbericht konnte nicht erstellt werden:\n\n" + dbSqlEx);
						LOG.error("Datenbankfehler: " + dbSqlEx.trim());
					}
				}
				
				topFrame.getStatusText().setText(ImpExpGui.labels.getString("main.status.ready.label"));
			}
			
		} finally {
			lock.unlock();
		}
	}

	public void loadSettings() {		
		connCombo.removeAllItems();
		workspaceReportText.setText(databaseConfig.getWorkspace().getReportWorkspace());
		
		DBConnection dbConnection = databaseConfig.getActiveConnection();
		List<DBConnection> dbConnectionList = databaseConfig.getConnections();

		if (dbConnection == null) {
			if (dbConnectionList != null && !dbConnectionList.isEmpty())
				dbConnection = dbConnectionList.get(0);
			else {
				dbConnection = new DBConnection();
				dbConnection.setDescription(ImpExpGui.labels.getString("db.label.newConnection"));
				databaseConfig.addConnection(dbConnection);
			}
		}

		Collections.sort(dbConnectionList);
		for (DBConnection conn : dbConnectionList)
			connCombo.addItem(conn);

		connCombo.setSelectedItem(dbConnection);
		getDbConnection(dbConnection);
	}

	public void setSettings() {
		databaseConfig.getWorkspace().setReportWorkspace(workspaceReportText.getText());
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		setDbConnection(dbConnection);	
	}

	public void setDbConnection(DBConnection dbConnection) {
		if (!descriptionText.getText().trim().equals(""))
			dbConnection.setDescription(descriptionText.getText());
		else
			descriptionText.setText(dbConnection.getDescription());

		dbConnection.setServer(serverText.getText());
		try {
			dbConnection.setPort(Integer.valueOf(portText.getText()));
		} catch (NumberFormatException nfe) {
			dbConnection.setPort(null);
		}		
		dbConnection.setSid(databaseText.getText());
		dbConnection.setUser(userText.getText());
		config.getInternal().setCurrentDbPassword(new String(passwordText.getPassword()));

		dbConnection.setSavePassword(passwordCheck.isSelected());
		if (passwordCheck.isSelected())
			dbConnection.setPassword(new String(passwordText.getPassword()));
		else
			dbConnection.setPassword("");
	}

	public void getDbConnection(DBConnection dbConnection) {
		descriptionText.setText(dbConnection.getDescription());
		serverText.setText(dbConnection.getServer());
		databaseText.setText(dbConnection.getSid());
		userText.setText(dbConnection.getUser());
		passwordCheck.setSelected(dbConnection.isSetSavePassword());
		passwordText.setText(dbConnection.getPassword());
		config.getInternal().setCurrentDbPassword(dbConnection.getPassword());

		String port = String.valueOf(dbConnection.getPort());
		if (port == null || port.equals("") || port.equals("null")) {
			port = "1521";
			dbConnection.setPort(1521);
		}

		portText.setText(port);		
	}

	private String getCopyOfDescription(DBConnection dbConnection) {
		// pattern: "connectionName - copy 1"
		// so to retrieve connectionName, " - copy*" has to be deleted...

		int nr = 0;
		String name = dbConnection.getDescription().replaceAll("\\s*-\\s*" + ImpExpGui.labels.getString("db.label.copyConnection") + ".*$", "");
		String copy = name + " - " + ImpExpGui.labels.getString("db.label.copyConnection");
		for (DBConnection conn : databaseConfig.getConnections()) 
			if (conn.getDescription().replaceAll("\\s*-\\s*" + ImpExpGui.labels.getString("db.label.copyConnection") + ".*$", "").toLowerCase().equals(name.toLowerCase()))
				nr++;

		if (nr > 1)
			return copy + " " + nr;
		else
			return copy;
	}

	private String getNewConnDescription() {
		int nr = 1;
		String name = ImpExpGui.labels.getString("db.label.newConnection");
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
			int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), ImpExpGui.labels.getString("db.dialog.save.msg"), ImpExpGui.labels.getString("db.dialog.save.title"), JOptionPane.YES_NO_CANCEL_OPTION);
			if (res==JOptionPane.CANCEL_OPTION) 
				return false;
			else if (res==JOptionPane.YES_OPTION)
				save();
			else
				getDbConnection((DBConnection)connCombo.getSelectedItem());
		}

		return true;
	}

	public boolean requestDelete() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		String text = ImpExpGui.labels.getString("db.dialog.delete.msg");
		Object[] args = new Object[]{ dbConnection.getDescription() };
		String result = MessageFormat.format(text, args);

		int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), result, ImpExpGui.labels.getString("db.dialog.delete.title"), JOptionPane.YES_NO_OPTION);
		return res==JOptionPane.YES_OPTION;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("internal.dbIsConnected"))
			reportButton.setEnabled((Boolean)evt.getNewValue());
	}
}
