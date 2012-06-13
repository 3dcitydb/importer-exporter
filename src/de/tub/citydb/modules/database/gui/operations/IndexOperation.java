/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.modules.database.gui.operations;

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

import de.tub.citydb.api.controller.ViewController;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBOperationType;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.database.IndexStatusInfo;
import de.tub.citydb.util.database.IndexStatusInfo.IndexInfoObject;
import de.tub.citydb.util.database.IndexStatusInfo.IndexStatus;
import de.tub.citydb.util.database.IndexStatusInfo.IndexType;
import de.tub.citydb.util.gui.GuiUtil;

public class IndexOperation extends DatabaseOperationView {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final ViewController viewController;
	private final Config config;

	private JPanel component;
	private JButton activate;
	private JButton deactivate;
	private JButton query;
	private JCheckBox spatial;
	private JCheckBox normal;

	public IndexOperation(Config config) {
		this.config = config;
		viewController = ObjectRegistry.getInstance().getViewController();

		init();
	}

	private void init() {
		component = new JPanel();
		component.setLayout(new GridBagLayout());

		activate = new JButton();
		deactivate = new JButton();
		query = new JButton();

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
	}

	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("db.label.operation.index");
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
		activate.setText(Internal.I18N.getString("db.button.index.activate"));
		deactivate.setText(Internal.I18N.getString("db.button.index.deactivate"));
		query.setText(Internal.I18N.getString("db.button.index.query"));
		spatial.setText(Internal.I18N.getString("db.label.operation.index.spatial"));
		normal.setText(Internal.I18N.getString("db.label.operation.index.normal"));
	}

	@Override
	public void setEnabled(boolean enable) {
		activate.setEnabled(enable);
		deactivate.setEnabled(enable);
		query.setEnabled(enable);
		spatial.setEnabled(enable);
		normal.setEnabled(enable);
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
			viewController.setStatusText(Internal.I18N.getString("main.status.database.activate.index.label"));

			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Internal.I18N.getString("db.dialog.index.window"), 
					Internal.I18N.getString("db.dialog.index.activate"), 
					null, 
					Internal.I18N.getString("db.dialog.index.activate.detail"), 
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
						indexStatus = DBUtil.createSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.all(LogLevel.INFO, "Activating normal indexes...");
						indexStatus = DBUtil.createNormalIndexes();
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
					String text = Internal.I18N.getString("db.dialog.index.activate.error");
					Object[] args = new Object[]{ dbSqlEx };
					String result = MessageFormat.format(text, args);

					JOptionPane.showMessageDialog(
							viewController.getTopFrame(), 
							result, 
							Internal.I18N.getString("common.dialog.error.db.title"),
							JOptionPane.ERROR_MESSAGE);

					LOG.error("Failed to activate indexes: " + dbSqlEx.trim());
				}
			} finally {
				viewController.setStatusText(Internal.I18N.getString("main.status.ready.label"));
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
			viewController.setStatusText(Internal.I18N.getString("main.status.database.deactivate.label"));

			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Internal.I18N.getString("db.dialog.index.window"), 
					Internal.I18N.getString("db.dialog.index.deactivate"), 
					null, 
					Internal.I18N.getString("db.dialog.index.deactivate.detail"), 
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
						indexStatus = DBUtil.dropSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.all(LogLevel.INFO, "Deactivating normal indexes...");
						indexStatus = DBUtil.dropNormalIndexes();
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
					String text = Internal.I18N.getString("db.dialog.index.deactivate.error");
					Object[] args = new Object[]{ dbSqlEx };
					String result = MessageFormat.format(text, args);

					JOptionPane.showMessageDialog(
							viewController.getTopFrame(), 
							result, 
							Internal.I18N.getString("common.dialog.error.db.title"),
							JOptionPane.ERROR_MESSAGE);

					LOG.error("Failed to deactivate indexes: " + dbSqlEx.trim());
				}
			} finally {
				viewController.setStatusText(Internal.I18N.getString("main.status.ready.label"));
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
			viewController.setStatusText(Internal.I18N.getString("main.status.database.index.query"));

			final StatusDialog dialog = new StatusDialog(viewController.getTopFrame(), 
					Internal.I18N.getString("db.dialog.index.query.window"), 
					Internal.I18N.getString("db.dialog.index.query.title"), 
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
						indexStatus = DBUtil.getStatusSpatialIndexes();
					} else if (type == IndexType.NORMAL && normal.isSelected()) {
						LOG.all(LogLevel.INFO, "Checking normal indexes...");
						indexStatus = DBUtil.getStatusNormalIndexes();
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
				String text = Internal.I18N.getString("db.dialog.index.query.error");
				Object[] args = new Object[]{ sqlExMsg };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Internal.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				LOG.error("SQL error: " + sqlExMsg);

			} finally {
				viewController.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

}
