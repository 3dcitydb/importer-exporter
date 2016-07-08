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
package org.citydb.modules.database.gui.operations;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.citydb.api.controller.ViewController;
import org.citydb.api.database.DatabaseType;
import org.citydb.api.event.global.DatabaseConnectionStateEvent;
import org.citydb.api.log.LogLevel;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.IndexStatusInfo;
import org.citydb.database.IndexStatusInfo.IndexInfoObject;
import org.citydb.database.IndexStatusInfo.IndexStatus;
import org.citydb.database.IndexStatusInfo.IndexType;
import org.citydb.gui.components.StatusDialog;
import org.citydb.log.Logger;
import org.citydb.util.gui.GuiUtil;

public class IndexOperation extends DatabaseOperationView {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final ViewController viewController;
	private final DatabaseConnectionPool dbConnectionPool;
	private final Config config;

	private JPanel component;
	private JButton activate;
	private JButton deactivate;
	private JButton query;
	private JButton tableStats;
	private JCheckBox spatial;
	private JCheckBox normal;

	private boolean isStatsSupported;
	
	public IndexOperation(Config config) {
		this.config = config;
		viewController = ObjectRegistry.getInstance().getViewController();
		dbConnectionPool = DatabaseConnectionPool.getInstance();

		init();
	}

	private void init() {
		component = new JPanel();
		component.setLayout(new GridBagLayout());

		activate = new JButton();
		deactivate = new JButton();
		query = new JButton();
		tableStats = new JButton();

		spatial = new JCheckBox();
		normal = new JCheckBox();
		spatial.setIconTextGap(10);
		normal.setIconTextGap(10);

		Box checkBox = Box.createVerticalBox();
		checkBox.add(spatial);
		checkBox.add(normal);

		Box buttonsPanel = Box.createHorizontalBox();
		buttonsPanel.add(activate);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(deactivate);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(query);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(tableStats);

		component.add(checkBox, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,5,5,0,5));
		component.add(buttonsPanel, GuiUtil.setConstraints(0,1,1,0,GridBagConstraints.NONE,10,5,5,5));
		
		activate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						if (spatial.isSelected() || normal.isSelected())
							createIndex();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		deactivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						if (spatial.isSelected() || normal.isSelected())
							dropIndex();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});

		query.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						if (spatial.isSelected() || normal.isSelected())
							queryStatus();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});
		
		tableStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						if (spatial.isSelected() || normal.isSelected())
							updateTableStatsOnColumn();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("db.label.operation.index");
	}

	@Override
	public Component getViewComponent() {
		return component;
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public DBOperationType getType() {
		return DBOperationType.INDEXES;
	}

	@Override
	public void doTranslation() {
		activate.setText(Language.I18N.getString("db.button.index.activate"));
		deactivate.setText(Language.I18N.getString("db.button.index.deactivate"));
		query.setText(Language.I18N.getString("db.button.index.query"));
		tableStats.setText("VACUUM");
		spatial.setText(Language.I18N.getString("db.label.operation.index.spatial"));
		normal.setText(Language.I18N.getString("db.label.operation.index.normal"));
	}

	@Override
	public void setEnabled(boolean enable) {
		activate.setEnabled(enable);
		deactivate.setEnabled(enable);
		query.setEnabled(enable);
		spatial.setEnabled(enable);
		normal.setEnabled(enable);
		tableStats.setEnabled(enable && isStatsSupported);
	}

	@Override
	public void loadSettings() {
		spatial.setSelected(config.getProject().getDatabase().getOperation().isSetSpatialIndex());
		normal.setSelected(config.getProject().getDatabase().getOperation().isSetNormalIndex());
	}

	@Override
	public void setSettings() {
		config.getProject().getDatabase().getOperation().setSpatialIndex(spatial.isSelected());
		config.getProject().getDatabase().getOperation().setNormalIndex(normal.isSelected());
	}

	private void createIndex() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.activate.index.label"));

			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.index.window"), 
					Language.I18N.getString("db.dialog.index.activate"), 
					null, 
					Language.I18N.getString("db.dialog.index.activate.detail"), 
					false);			

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dialog.setLocationRelativeTo(viewController.getTopFrame());
					dialog.setVisible(true);
				}
			});

			try {
				for (IndexType type : IndexType.values()) {
					IndexStatusInfo indexStatus = null;

					if (type == IndexType.SPATIAL && spatial.isSelected()) {
						LOG.all(LogLevel.INFO, "Activating spatial indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.all(LogLevel.INFO, "Activating normal indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createNormalIndexes();
					}

					if (indexStatus != null) {				
						for (IndexInfoObject index : indexStatus.getIndexObjects()) {							
							if (index.getStatus() != IndexStatus.VALID) {
								LOG.error("FAILED: " + index.toString());
								if (index.hasErrorMessage())
								LOG.error("Error cause: " + index.getErrorMessage());
							} else
								LOG.all(LogLevel.INFO, "SUCCESS: " + index.toString());
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				LOG.all(LogLevel.INFO, "Activating indexes successfully finished.");
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				String dbSqlEx = sqlEx.getMessage().trim();

				if (dbSqlEx.length() == 0) {
					LOG.warn("Activating indexes aborted.");
				} else  {
					String text = Language.I18N.getString("db.dialog.index.activate.error");
					Object[] args = new Object[]{ dbSqlEx };
					String result = MessageFormat.format(text, args);

					JOptionPane.showMessageDialog(
							viewController.getTopFrame(), 
							result, 
							Language.I18N.getString("common.dialog.error.db.title"),
							JOptionPane.ERROR_MESSAGE);

					LOG.error("Failed to activate indexes: " + dbSqlEx.trim());
				}
			} finally {
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private void dropIndex() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.deactivate.label"));

			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.index.window"), 
					Language.I18N.getString("db.dialog.index.deactivate"), 
					null, 
					Language.I18N.getString("db.dialog.index.deactivate.detail"), 
					false);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dialog.setLocationRelativeTo(viewController.getTopFrame());
					dialog.setVisible(true);
				}
			});

			try {
				for (IndexType type : IndexType.values()) {
					IndexStatusInfo indexStatus = null;

					if (type == IndexType.SPATIAL && spatial.isSelected()) {
						LOG.all(LogLevel.INFO, "Deactivating spatial indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().dropSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.all(LogLevel.INFO, "Deactivating normal indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().dropNormalIndexes();
					}

					if (indexStatus != null) {				
						for (IndexInfoObject index : indexStatus.getIndexObjects()) {							
							if (index.getStatus() != IndexStatus.DROPPED) {
								LOG.error("FAILED: " + index.toString());
								if (index.hasErrorMessage())
								LOG.error("Error cause: " + index.getErrorMessage());
							} else
								LOG.all(LogLevel.INFO, "SUCCESS: " + index.toString());
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				LOG.all(LogLevel.INFO, "Deactivating indexes successfully finished.");
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				String dbSqlEx = sqlEx.getMessage().trim();

				if (dbSqlEx.length() == 0) {
					LOG.warn("Deactivating indexes aborted.");
				} else  {
					String text = Language.I18N.getString("db.dialog.index.deactivate.error");
					Object[] args = new Object[]{ dbSqlEx };
					String result = MessageFormat.format(text, args);

					JOptionPane.showMessageDialog(
							viewController.getTopFrame(), 
							result, 
							Language.I18N.getString("common.dialog.error.db.title"),
							JOptionPane.ERROR_MESSAGE);

					LOG.error("Failed to deactivate indexes: " + dbSqlEx.trim());
				}
			} finally {
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	private void queryStatus() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.index.query"));

			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.index.query.window"), 
					Language.I18N.getString("db.dialog.index.query.title"), 
					null,
					null, 
					true);		

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dialog.setLocationRelativeTo(viewController.getTopFrame());
					dialog.setVisible(true);
				}
			});

			try {
				for (IndexType type : IndexType.values()) {
					IndexStatusInfo indexStatus = null;

					if (type == IndexType.SPATIAL && spatial.isSelected()) {
						LOG.all(LogLevel.INFO, "Checking spatial indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().getStatusSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.all(LogLevel.INFO, "Checking normal indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().getStatusNormalIndexes();
					}

					if (indexStatus != null) {
						for (IndexInfoObject index : indexStatus.getIndexObjects()) {
							LOG.all(LogLevel.INFO, (index.getStatus() == IndexStatus.VALID ? "ON" : "OFF") + ": " + index.toString());
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				LOG.all(LogLevel.INFO, "Querying index status successfully finished.");
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				String sqlExMsg = sqlEx.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.index.query.error");
				Object[] args = new Object[]{ sqlExMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Language.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				LOG.error("SQL error: " + sqlExMsg);

			} finally {
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}
	
	private void updateTableStatsOnColumn() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.index.tableStats"));
			
			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.index.tableStats.window"), 
					Language.I18N.getString("db.dialog.index.tableStats.title"), 
					null,
					Language.I18N.getString("db.dialog.index.tableStats.detail"), 
					true);		

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dialog.setLocationRelativeTo(viewController.getTopFrame());
					dialog.setVisible(true);
				}
			});


			dialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation();
						}
					});
				}
			});
			
			try {
				boolean statsUpdated = true;
				
				for (IndexType type : IndexType.values()) {
					if (statsUpdated) {
						if (type == IndexType.SPATIAL && spatial.isSelected()) {
							LOG.all(LogLevel.INFO, "Updating table statistics for columns with spatial index...");
							statsUpdated = dbConnectionPool.getActiveDatabaseAdapter().getUtil().updateTableStatsSpatialColumns();
						} else if (type == IndexType.NORMAL && normal.isSelected()) {
							LOG.all(LogLevel.INFO, "Updating table statistics for columns with normal index...");
							statsUpdated = dbConnectionPool.getActiveDatabaseAdapter().getUtil().updateTableStatsNormalColumns();
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				if (statsUpdated)
					LOG.all(LogLevel.INFO, "Table statistics successfully updated.");
				else {
					if (dbConnectionPool.getActiveDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS)
						LOG.warn("Updating table statistics aborted.");
					else
						LOG.warn("Updating table statistics not yet supported for connected DBMS.");
				}
					
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				String sqlExMsg = sqlEx.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.index.tableStats.error");
				Object[] args = new Object[]{ sqlExMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Language.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				LOG.error("SQL error: " + sqlExMsg);

			} finally {
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
		if (event.isConnected())
			isStatsSupported = dbConnectionPool.getActiveDatabaseAdapter().hasTableStatsSupport();
	}

}
