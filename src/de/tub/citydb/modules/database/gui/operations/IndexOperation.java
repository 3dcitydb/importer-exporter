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
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBOperationType;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.database.DBUtil.DBIndexType;
import de.tub.citydb.util.gui.GuiUtil;

public class IndexOperation implements DatabaseOperationView {
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
				for (DBIndexType type : DBIndexType.values()) {
					String[] queryResult = null;

					if (type == DBIndexType.SPATIAL && spatial.isSelected()) {
						LOG.info("Activating spatial indexes...");
						queryResult = DBUtil.createSpatialIndexes();
					} else if (type == DBIndexType.NORMAL && normal.isSelected()) {
						LOG.info("Activating normal indexes...");
						queryResult = DBUtil.createNormalIndexes();
					}

					if (queryResult != null) {				
						for (String line : queryResult) {				
							String[] parts = line.split(":");

							if (!parts[4].equals("VALID")) {
								LOG.error("FAILED: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
								String errMsg = DBUtil.errorMessage(parts[3]);
								LOG.error("Error cause: " + errMsg);
							} else
								LOG.info("SUCCESS: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				LOG.info("Activating indexes successfully finished.");
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
				for (DBIndexType type : DBIndexType.values()) {
					String[] queryResult = null;

					if (type == DBIndexType.SPATIAL && spatial.isSelected()) {
						LOG.info("Deactivating spatial indexes...");
						queryResult = DBUtil.dropSpatialIndexes();
					} else if (type == DBIndexType.NORMAL && normal.isSelected()) {
						LOG.info("Deactivating normal indexes...");
						queryResult = DBUtil.dropNormalIndexes();
					}

					if (queryResult != null) {
						for (String line : queryResult) {
							String[] parts = line.split(":");

							if (!parts[4].equals("DROPPED")) {
								LOG.error("FAILED: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
								String errMsg = DBUtil.errorMessage(parts[3]);
								LOG.error("Error cause: " + errMsg);
							} else
								LOG.info("SUCCESS: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				LOG.info("Deactivating indexes successfully finished.");
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
				for (DBIndexType type : DBIndexType.values()) {
					String[] queryResult = null;

					if (type == DBIndexType.SPATIAL && spatial.isSelected()) {
						LOG.info("Checking spatial indexes...");
						queryResult = DBUtil.getStatusSpatialIndexes();
					} else if (type == DBIndexType.NORMAL && normal.isSelected()) {
						LOG.info("Checking normal indexes...");
						queryResult = DBUtil.getStatusNormalIndexes();
					}

					if (queryResult != null) {
						for (String line : queryResult) {
							String[] parts = line.split(":");

							if (parts[3].equals("VALID"))
								LOG.info("ON: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
							else
								LOG.info("OFF: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
						}
					}
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						dialog.dispose();
					}
				});

				LOG.info("Querying index status successfully finished.");
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
