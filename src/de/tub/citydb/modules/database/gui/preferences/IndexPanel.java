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
package de.tub.citydb.modules.database.gui.preferences;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.gui.preferences.AbstractPreferencesComponent;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.database.DBUtil.DB_INDEX_TYPE;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class IndexPanel extends AbstractPreferencesComponent implements EventHandler {	
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();

	private JPanel block1;
	private JPanel block2;
	private JButton impSIDeactivate;
	private JButton impSIActivate;
	private JButton impNIDeactivate;
	private JButton impNIActivate;

	private ImpExpGui topFrame;

	public IndexPanel(Config config, ImpExpGui topFrame) {
		super(config);
		this.topFrame = topFrame;

		initGui();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);
	}

	@Override
	public boolean isModified() {
		return false;
	}

	private void initGui() {
		impSIDeactivate = new JButton();
		impSIActivate = new JButton();
		impSIDeactivate.setEnabled(false);
		impSIActivate.setEnabled(false);

		impNIDeactivate = new JButton();
		impNIActivate = new JButton();
		impNIDeactivate.setEnabled(false);
		impNIActivate.setEnabled(false);		

		setLayout(new GridBagLayout());
		{
			block1 = new JPanel();
			add(block1, GuiUtil.setConstraints(0,0,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block1.setBorder(BorderFactory.createTitledBorder(""));
			block1.setLayout(new GridBagLayout());
			{
				block1.add(impSIDeactivate, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				block1.add(impSIActivate, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			}

			block2 = new JPanel();
			add(block2, GuiUtil.setConstraints(0,1,1.0,0.0,GridBagConstraints.BOTH,5,0,5,0));
			block2.setBorder(BorderFactory.createTitledBorder(""));
			block2.setLayout(new GridBagLayout());
			{
				block2.add(impNIDeactivate, GuiUtil.setConstraints(0,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
				block2.add(impNIActivate, GuiUtil.setConstraints(1,0,0.0,1.0,GridBagConstraints.BOTH,5,5,5,5));
			}
		}	

		impSIDeactivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						dropIndex(DB_INDEX_TYPE.SPATIAL);
					}
				};
				thread.start();
			}
		});

		impNIDeactivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						dropIndex(DB_INDEX_TYPE.NORMAL);
					}
				};
				thread.start();
			}
		});

		impSIActivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						createIndex(DB_INDEX_TYPE.SPATIAL);
					}
				};
				thread.start();
			}
		});

		impNIActivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						createIndex(DB_INDEX_TYPE.NORMAL);
					}
				};
				thread.start();
			}
		});
	}

	@Override
	public void doTranslation() {
		block1.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.db.index.spatial.border.manual")));
		impSIDeactivate.setText(Internal.I18N.getString("pref.db.index.spatial.button.deactivate"));
		impSIActivate.setText(Internal.I18N.getString("pref.db.index.spatial.button.activate"));

		block2.setBorder(BorderFactory.createTitledBorder(Internal.I18N.getString("pref.db.index.normal.border.manual")));
		impNIDeactivate.setText(Internal.I18N.getString("pref.db.index.normal.button.deactivate"));
		impNIActivate.setText(Internal.I18N.getString("pref.db.index.normal.button.activate"));

		if (!DBConnectionPool.getInstance().isConnected()) {
			Color color = UIManager.getColor("Label.disabledForeground");
			((TitledBorder)block1.getBorder()).setTitleColor(color);
			block1.repaint();
			((TitledBorder)block2.getBorder()).setTitleColor(color);
			block2.repaint();
		}
	}

	private void dropIndex(DB_INDEX_TYPE type) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			String statusTextKey, logStartMsg, statusWindowTitle, statusTitle, logSuccess, logFail;

			if (type == DB_INDEX_TYPE.SPATIAL) {
				statusTextKey =	"main.status.database.deactivate.spatial.label";
				logStartMsg = "Deactivating spatial indexes...";
				statusWindowTitle = Internal.I18N.getString("pref.db.index.spatial.dialog.window");
				statusTitle = Internal.I18N.getString("pref.db.index.spatial.dialog.deactivate");
				logSuccess = "Deactivating spatial indexes successfully finished.";
				logFail = "Deactivating spatial indexes aborted.";
			} else {
				statusTextKey = "main.status.database.deactivate.normal.label";
				logStartMsg = "Deactivating normal indexes...";
				statusWindowTitle = Internal.I18N.getString("pref.db.index.normal.dialog.window");
				statusTitle = Internal.I18N.getString("pref.db.index.normal.dialog.deactivate");
				logSuccess = "Deactivating normal indexes successfully finished.";
				logFail = "Deactivating normal indexes aborted.";
			}			

			String statusDetails = Internal.I18N.getString("pref.db.index.deactivate.detail");

			topFrame.clearConsole();
			topFrame.setStatusText(Internal.I18N.getString(statusTextKey));

			LOG.info(logStartMsg);

			final StatusDialog reportDialog = new StatusDialog(topFrame, 
					statusWindowTitle, 
					statusTitle, 
					null, 
					statusDetails, 
					false);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					reportDialog.setLocationRelativeTo(getTopLevelAncestor());
					reportDialog.setVisible(true);
				}
			});

			String[] report = null;
			String dbSqlEx = null;
			try {
				if (type == DB_INDEX_TYPE.SPATIAL)
					report = DBUtil.dropSpatialIndexes();
				else 
					report = DBUtil.dropNormalIndexes();

				if (report != null) {

					for (String line : report) {
						String[] parts = line.split(":");

						if (!parts[4].equals("DROPPED")) {
							LOG.error("FAILED: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
							String errMsg = DBUtil.errorMessage(parts[3]);
							LOG.error("Error cause: " + errMsg);
						} else
							LOG.info("SUCCESS: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
					}
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
					LOG.info(logSuccess);
				} else {

					if (dbSqlEx == null) {
						LOG.warn(logFail);
					} else  {
						String text = Internal.I18N.getString("pref.db.index.deactivate.error");
						Object[] args = new Object[]{ dbSqlEx };
						String result = MessageFormat.format(text, args);

						topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
						LOG.error("Failed to deactivate indexes: " + dbSqlEx.trim());
					}
				}
			}

			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	private void createIndex(DB_INDEX_TYPE type) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			String statusTextKey, logStartMsg, statusWindowsTitle, statusTitle, logSuccess, logFail;

			if (type == DB_INDEX_TYPE.SPATIAL) {
				statusTextKey =	"main.status.database.activate.spatial.label";
				logStartMsg = "Activating spatial indexes...";
				statusWindowsTitle = Internal.I18N.getString("pref.db.index.spatial.dialog.window");
				statusTitle = Internal.I18N.getString("pref.db.index.spatial.dialog.activate");
				logSuccess = "Activating spatial indexes successfully finished.";
				logFail = "Activating spatial indexes aborted.";
			} else {
				statusTextKey = "main.status.database.activate.normal.label";
				logStartMsg = "Activating normal indexes...";
				statusWindowsTitle = Internal.I18N.getString("pref.db.index.normal.dialog.window");
				statusTitle = Internal.I18N.getString("pref.db.index.normal.dialog.activate");
				logSuccess = "Activating normal indexes successfully finished.";
				logFail = "Activating normal indexes aborted.";
			}			

			String statusDetails = Internal.I18N.getString("pref.db.index.activate.detail");

			topFrame.clearConsole();
			topFrame.setStatusText(Internal.I18N.getString(statusTextKey));

			LOG.info(logStartMsg);

			final StatusDialog reportDialog = new StatusDialog(topFrame, 
					statusWindowsTitle, 
					statusTitle, 
					null, 
					statusDetails, 
					false);			

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					reportDialog.setLocationRelativeTo(getTopLevelAncestor());
					reportDialog.setVisible(true);
				}
			});

			String[] report = null;
			String dbSqlEx = null;
			try {
				if (type == DB_INDEX_TYPE.SPATIAL)
					report = DBUtil.createSpatialIndexes();
				else 
					report = DBUtil.createNormalIndexes();

				if (report != null) {				
					for (String line : report) {				
						String[] parts = line.split(":");

						if (!parts[4].equals("VALID")) {
							LOG.error("FAILED: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
							String errMsg = DBUtil.errorMessage(parts[3]);
							LOG.error("Error cause: " + errMsg);
						} else
							LOG.info("SUCCESS: " + parts[0] + " on " + parts[1] + "(" + parts[2] + ")");
					}
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
					LOG.info(logSuccess);
				} else {

					if (dbSqlEx == null) {
						LOG.warn(logFail);
					} else  {
						String text = Internal.I18N.getString("pref.db.index.activate.error");
						Object[] args = new Object[]{ dbSqlEx };
						String result = MessageFormat.format(text, args);

						topFrame.errorMessage(Internal.I18N.getString("common.dialog.error.db.title"), result);
						LOG.error("Failed to activate indexes: " + dbSqlEx.trim());
					}
				}
			}

			topFrame.setStatusText(Internal.I18N.getString("main.status.ready.label"));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void loadSettings() {
		// nothing to do here
	}

	@Override
	public void setSettings() {
		// nothing to do here
	}

	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.db.index");
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		boolean isConnected = ((DatabaseConnectionStateEvent)event).isConnected();
		Color color = isConnected ? UIManager.getColor("TitledBorder.titleColor") : UIManager.getColor("Label.disabledForeground");

		((TitledBorder)block1.getBorder()).setTitleColor(color);
		block1.repaint();

		((TitledBorder)block2.getBorder()).setTitleColor(color);
		block2.repaint();

		impSIActivate.setEnabled(isConnected);
		impSIDeactivate.setEnabled(isConnected);
		impNIActivate.setEnabled(isConnected);
		impNIDeactivate.setEnabled(isConnected);
	}
}
