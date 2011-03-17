package de.tub.citydb.gui.panel.db;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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

import org.citygml4j.geometry.BoundingVolume;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.DBOperationMode;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.SrsComboBoxManager;
import de.tub.citydb.gui.components.SrsComboBoxManager.SrsComboBox;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.gui.util.GuiUtil;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.DBUtil;
import de.tub.citydb.util.Util;

@SuppressWarnings("serial")
public class DatabasePanel extends JPanel implements PropertyChangeListener {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final ImpExpGui topFrame;

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
	private SrsComboBoxManager srsComboBoxManager;

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

	public DatabasePanel(Config config, ImpExpGui topFrame) {
		this.config = config;
		this.topFrame = topFrame;
		initGui();		
		databaseConfig = config.getProject().getDatabase();
		config.getInternal().addPropertyChangeListener(this);
	}

	private boolean isModified() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();	
		if (!descriptionText.getText().trim().equals(dbConnection.getDescription())) return true;
		if (!serverText.getText().equals(dbConnection.getServer())) return true;
		if (!userText.getText().equals(dbConnection.getUser())) return true;		
		if (!String.valueOf(passwordText.getPassword()).equals(config.getInternal().getCurrentDbPassword())) return true;
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

		srsComboBoxManager = SrsComboBoxManager.getInstance(config);
		srsComboBox = srsComboBoxManager.getSrsComboBox(true);

		portText.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (portText.getValue() != null) {
					if (((Number)portText.getValue()).intValue() < 0)
						portText.setValue(1521);
				}
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

		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				topFrame.getConsoleText().setText("");
				DBConnection conn = config.getInternal().getOpenConnection();
				LOG.info("Connected to database profile '" + conn.getDescription() + "'.");
				conn.getMetaData().toConsole(LogLevelType.INFO);
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
				row2_buttons.add(newButton, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,0,0,0));
				row2_buttons.add(copyButton, GuiUtil.setConstraints(0,1,0.0,0.0,GridBagConstraints.HORIZONTAL,5,0,0,0));

				c = GuiUtil.setConstraints(0,2,0.0,1.0,GridBagConstraints.HORIZONTAL,5,0,0,0);
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

		this.setEnabled(false);
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
		srsComboBox.doTranslation();

		if (!config.getInternal().isConnected())
			connectButton.setText(Internal.I18N.getString("db.button.connect"));
		else
			connectButton.setText(Internal.I18N.getString("db.button.disconnect"));
	}

	private void selectConnection() {
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
		if (topFrame.saveProjectSettings()) 
			LOG.info("Settings successfully saved in config file '" + 
					config.getInternal().getConfigPath() + File.separator + config.getInternal().getConfigProject() + "'.");
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
			DBConnection conn = config.getProject().getDatabase().getActiveConnection();
			DBConnectionPool dbPool = topFrame.getDBPool();

			if (!intConfig.isConnected()) {
				// check for valid input
				if (conn.getUser().trim().equals("")) {
					topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"),
							Internal.I18N.getString("db.dialog.error.conn.user"));
					return;
				}

				if (intConfig.getCurrentDbPassword().trim().equals("")) {
					topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"),
							Internal.I18N.getString("db.dialog.error.conn.pass"));
					return;
				}

				if (conn.getServer().trim().equals("")) {
					topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"),
							Internal.I18N.getString("db.dialog.error.conn.server"));
					return;
				}

				if (conn.getPort() == null) {
					topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"),
							Internal.I18N.getString("db.dialog.error.conn.port"));
					return;
				}

				if (conn.getSid().trim().equals("")) {
					topFrame.errorMessage(Internal.I18N.getString("db.dialog.error.conn.title"),
							Internal.I18N.getString("db.dialog.error.conn.sid"));
					return;
				}			

				topFrame.getStatusText().setText(Internal.I18N.getString("main.status.database.connect.label"));
				LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");

				try {
					dbPool.init();
				} catch (SQLException sqlEx) {
					String text = Internal.I18N.getString("db.dialog.error.openConn");
					Object[] args = new Object[]{ sqlEx.getMessage() };
					String result = MessageFormat.format(text, args);					

					topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
					intConfig.unsetOpenConnection();
					dbPool.forceClose();
					LOG.error("Connection to database could not be established: " + sqlEx.getMessage().trim());
				}

				if (intConfig.isConnected()) {
					LOG.info("Database connection established.");
					conn.getMetaData().toConsole(LogLevelType.INFO);

					// check whether user-defined SRSs are supported
					try {
						boolean updateComboBoxes = false;					
						DBUtil dbUtil = DBUtil.getInstance(dbPool);

						for (ReferenceSystem refSys: config.getProject().getDatabase().getReferenceSystems()) {
							boolean wasSupported = refSys.isSupported();
							boolean isSupported = dbUtil.isSrsSupported(refSys.getSrid());
							refSys.setSupported(isSupported);

							if (!updateComboBoxes && wasSupported != isSupported)
								updateComboBoxes = true;

							if (isSupported)
								LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
							else
								LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
						}

						if (updateComboBoxes)
							SrsComboBoxManager.getInstance(config).updateAll(false);

					} catch (SQLException sqlEx) {
						LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
					}
				}
			}

			else {
				topFrame.getStatusText().setText(Internal.I18N.getString("main.status.database.disconnect.label"));

				try {
					dbPool.close();
				} catch (SQLException sqlEx) {
					LOG.error("Connection error: " + sqlEx.getMessage().trim());
					LOG.error("Terminating connection...");
					dbPool.forceClose();

					String text = Internal.I18N.getString("db.dialog.error.closeConn");
					Object[] args = new Object[]{ sqlEx.getMessage() };
					String result = MessageFormat.format(text, args);

					topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
				}

				LOG.info("Disconnected from database.");
			}

			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
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

			topFrame.getConsoleText().setText("");
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.database.report.label"));

			LOG.info("Generating database report...");			
			final DBUtil geodbReportReader = DBUtil.getInstance(topFrame.getDBPool());

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
							geodbReportReader.cancelOperation();
						}
					});
				}
			});

			String[] report = null;
			String dbSqlEx = null;
			try {
				// checking workspace... this should be improved in future...
				DBConnectionPool dbPool = topFrame.getDBPool();

				if (changeWorkspace(workspace, dbPool)) {
					report = geodbReportReader.databaseReport(workspace);

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

				topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
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

			topFrame.getConsoleText().setText("");
			topFrame.getStatusText().setText(Internal.I18N.getString("main.status.database.bbox.label"));

			LOG.info("Calculating bounding box...");			
			final DBUtil geodbBBox = DBUtil.getInstance(topFrame.getDBPool());

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
							geodbBBox.cancelOperation();
						}
					});
				}
			});

			BoundingVolume bbox = null;
			try {
				// checking workspace... this should be improved in future...
				DBConnectionPool dbPool = topFrame.getDBPool();

				if (changeWorkspace(workspace, dbPool)) {
					bbox = geodbBBox.calcBoundingBox(workspace, featureClass);
					if (bbox != null) {
						int dbSrid = config.getInternal().getOpenConnection().getMetaData().getSrid();
						int bboxSrid = db.getOperation().getBoundingBoxSRS().getSrid();
						
						if (db.getOperation().getBoundingBoxSRS().isSupported() && bboxSrid != dbSrid) {
							try {
								bbox = geodbBBox.transformBBox(bbox, dbSrid, bboxSrid);
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

				topFrame.getStatusText().setText(Internal.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private boolean changeWorkspace(Workspace workspace, DBConnectionPool dbPool) {		
		if (!workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.checkWorkspace(workspace);

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
		databaseConfig = config.getProject().getDatabase();

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
		getDbConnection(dbConnection);

		workspaceText.setText(databaseConfig.getWorkspaces().getOperationWorkspace().getName());
		timestampText.setText(databaseConfig.getWorkspaces().getOperationWorkspace().getTimestamp());
		bboxComboBox.setSelectedItem(databaseConfig.getOperation().getBoundingBoxFeatureClass());

		srsComboBox.updateContent();
		srsComboBox.setSelectedItem(databaseConfig.getOperation().getBoundingBoxSRS());

		if (databaseConfig.getOperation().getExecute() == DBOperationMode.REPORT)
			dbReport.setSelected(true);
		else
			dbBBox.setSelected(true);

		setEnabledDBOperations(false);
	}

	public void setSettings() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		setDbConnection(dbConnection);

		if (workspaceText.getText().trim().length() == 0)
			workspaceText.setText("LIVE");

		databaseConfig.getWorkspaces().getOperationWorkspace().setName(workspaceText.getText());
		databaseConfig.getWorkspaces().getOperationWorkspace().setTimestamp(timestampText.getText());
		databaseConfig.getOperation().setExecute(dbReport.isSelected() ? DBOperationMode.REPORT : DBOperationMode.BBOX);
		databaseConfig.getOperation().setBoundingBoxFeatureClass((FeatureClassMode)bboxComboBox.getSelectedItem());
		databaseConfig.getOperation().setBoundingBoxSRS(srsComboBox.getSelectedItem());
	}

	public void setDbConnection(DBConnection dbConnection) {
		if (!descriptionText.getText().trim().equals(""))
			dbConnection.setDescription(descriptionText.getText());
		else
			descriptionText.setText(dbConnection.getDescription());

		dbConnection.setServer(serverText.getText());	
		dbConnection.setPort(((Number)portText.getValue()).intValue());
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

	public boolean requestDelete() {
		DBConnection dbConnection = (DBConnection)connCombo.getSelectedItem();
		String text = Internal.I18N.getString("db.dialog.delete.msg");
		Object[] args = new Object[]{ dbConnection.getDescription() };
		String result = MessageFormat.format(text, args);

		int res = JOptionPane.showConfirmDialog(getTopLevelAncestor(), result, Internal.I18N.getString("db.dialog.delete.title"), JOptionPane.YES_NO_OPTION);
		return res==JOptionPane.YES_OPTION;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("database.isConnected")) {
			boolean isConnected = (Boolean)evt.getNewValue();

			if (!isConnected)
				connectButton.setText(Internal.I18N.getString("db.button.connect"));
			else
				connectButton.setText(Internal.I18N.getString("db.button.disconnect"));

			setEnabledDBOperations(isConnected);
		}
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
