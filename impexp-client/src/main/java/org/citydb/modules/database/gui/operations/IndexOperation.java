/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseOperationType;
import org.citydb.config.project.database.DatabaseType;
import org.citydb.database.adapter.IndexStatusInfo;
import org.citydb.database.adapter.IndexStatusInfo.IndexInfoObject;
import org.citydb.database.adapter.IndexStatusInfo.IndexStatus;
import org.citydb.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

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

	public IndexOperation(DatabaseOperationsPanel parent, Config config) {
		this.config = config;
		
		viewController = parent.getViewController();
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
		component.add(buttonsPanel, GuiUtil.setConstraints(0,1,1,0,GridBagConstraints.NONE,10,5,10,5));
		
		activate.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				createIndex();
				return null;
			}
		}.execute());

		deactivate.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				dropIndex();
				return null;
			}
		}.execute());

		query.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				queryStatus();
				return null;
			}
		}.execute());

		tableStats.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				updateTableStatsOnColumn();
				return null;
			}
		}.execute());
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
	public DatabaseOperationType getType() {
		return DatabaseOperationType.INDEXES;
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
		spatial.setSelected(config.getDatabaseConfig().getOperation().isSetSpatialIndex());
		normal.setSelected(config.getDatabaseConfig().getOperation().isSetNormalIndex());
	}

	@Override
	public void setSettings() {
		config.getDatabaseConfig().getOperation().setSpatialIndex(spatial.isSelected());
		config.getDatabaseConfig().getOperation().setNormalIndex(normal.isSelected());
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
					true);

			dialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(
					() -> dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				dialog.setLocationRelativeTo(viewController.getTopFrame());
				dialog.setVisible(true);
			});

			try {
				for (IndexType type : IndexType.values()) {
					IndexStatusInfo indexStatus = null;

					if (type == IndexType.SPATIAL && spatial.isSelected()) {
						LOG.info("Activating spatial indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.info("Activating normal indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createNormalIndexes();
					}

					if (indexStatus != null) {				
						for (IndexInfoObject index : indexStatus.getIndexObjects()) {							
							if (index.getStatus() != IndexStatus.VALID) {
								LOG.error("FAILED: " + index.toString());
								if (index.hasErrorMessage())
								LOG.error("Error cause: " + index.getErrorMessage());
							} else
								LOG.info("SUCCESS: " + index.toString());
						}
					}
				}

				SwingUtilities.invokeLater(dialog::dispose);

				LOG.info("Activating indexes successfully finished.");
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(dialog::dispose);

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
					true);

			dialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(
					() -> dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				dialog.setLocationRelativeTo(viewController.getTopFrame());
				dialog.setVisible(true);
			});

			try {
				for (IndexType type : IndexType.values()) {
					IndexStatusInfo indexStatus = null;

					if (type == IndexType.SPATIAL && spatial.isSelected()) {
						LOG.info("Deactivating spatial indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().dropSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.info("Deactivating normal indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().dropNormalIndexes();
					}

					if (indexStatus != null) {				
						for (IndexInfoObject index : indexStatus.getIndexObjects()) {							
							if (index.getStatus() != IndexStatus.DROPPED) {
								LOG.error("FAILED: " + index.toString());
								if (index.hasErrorMessage())
								LOG.error("Error cause: " + index.getErrorMessage());
							} else
								LOG.info("SUCCESS: " + index.toString());
						}
					}
				}

				SwingUtilities.invokeLater(dialog::dispose);

				LOG.info("Deactivating indexes successfully finished.");
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(dialog::dispose);

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

			dialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(
					() -> dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				dialog.setLocationRelativeTo(viewController.getTopFrame());
				dialog.setVisible(true);
			});

			try {
				for (IndexType type : IndexType.values()) {
					IndexStatusInfo indexStatus = null;

					if (type == IndexType.SPATIAL && spatial.isSelected()) {
						LOG.info("Checking spatial indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().getStatusSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.info("Checking normal indexes...");
						indexStatus = dbConnectionPool.getActiveDatabaseAdapter().getUtil().getStatusNormalIndexes();
					}

					if (indexStatus != null) {
						for (IndexInfoObject index : indexStatus.getIndexObjects()) {
							LOG.info((index.getStatus() == IndexStatus.VALID ? "ON" : "OFF") + ": " + index.toString());
						}
					}
				}

				SwingUtilities.invokeLater(dialog::dispose);

				LOG.info("Querying index status successfully finished.");
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(dialog::dispose);

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

			dialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(() ->
					dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				dialog.setLocationRelativeTo(viewController.getTopFrame());
				dialog.setVisible(true);
			});
			
			try {
				boolean statsUpdated = true;
				
				for (IndexType type : IndexType.values()) {
					if (statsUpdated) {
						if (type == IndexType.SPATIAL && spatial.isSelected()) {
							LOG.info("Updating table statistics for columns with spatial index...");
							statsUpdated = dbConnectionPool.getActiveDatabaseAdapter().getUtil().updateTableStatsSpatialColumns();
						} else if (type == IndexType.NORMAL && normal.isSelected()) {
							LOG.info("Updating table statistics for columns with normal index...");
							statsUpdated = dbConnectionPool.getActiveDatabaseAdapter().getUtil().updateTableStatsNormalColumns();
						}
					}
				}

				SwingUtilities.invokeLater(dialog::dispose);

				if (statsUpdated)
					LOG.info("Table statistics successfully updated.");
				else {
					if (dbConnectionPool.getActiveDatabaseAdapter().getDatabaseType() == DatabaseType.POSTGIS)
						LOG.warn("Updating table statistics aborted.");
					else
						LOG.warn("Updating table statistics not yet supported for connected DBMS.");
				}
					
			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(dialog::dispose);

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
