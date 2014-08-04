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
package org.citydb.modules.database.gui.operations;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.citydb.api.controller.ViewController;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.gui.components.StatusDialog;
import org.citydb.log.Logger;
import org.citydb.util.gui.GuiUtil;

public class ReportOperation extends DatabaseOperationView {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final DatabaseOperationsPanel parent;
	private final ViewController viewController;
	private final DatabaseConnectionPool dbConnectionPool;

	private JPanel component;
	private JButton reportButton;

	public ReportOperation(DatabaseOperationsPanel parent) {
		this.parent = parent;
		viewController = ObjectRegistry.getInstance().getViewController();
		dbConnectionPool = DatabaseConnectionPool.getInstance();

		init();
	}

	private void init() {
		component = new JPanel();
		component.setLayout(new GridBagLayout());

		reportButton = new JButton();		
		component.add(reportButton, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,0,5,0,5));

		reportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread thread = new Thread() {
					public void run() {
						doOperation();
					}
				};
				thread.setDaemon(true);
				thread.start();
			}
		});
	}

	@Override
	public String getLocalizedTitle() {
		return Language.I18N.getString("db.label.operation.report");
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
		return DBOperationType.REPORT;
	}

	@Override
	public void doTranslation() {
		reportButton.setText(Language.I18N.getString("db.button.report"));
	}

	@Override
	public void setEnabled(boolean enable) {
		reportButton.setEnabled(enable);
	}

	@Override
	public void loadSettings() {
		// nothing to do here...
	}

	@Override
	public void setSettings() {
		// nothing to do here...
	}

	private void doOperation() {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			Workspace workspace = parent.getWorkspace();
			if (workspace == null)
				return;

			viewController.clearConsole();
			viewController.setStatusText(Language.I18N.getString("main.status.database.report.label"));

			LOG.info("Generating database report...");			
			if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport() && !parent.existsWorkspace())
				return;

			final StatusDialog reportDialog = new StatusDialog(viewController.getTopFrame(), 
					Language.I18N.getString("db.dialog.report.window"), 
					Language.I18N.getString("db.dialog.report.title"), 
					null,
					Language.I18N.getString("db.dialog.report.details"), 
					true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					reportDialog.setLocationRelativeTo(viewController.getTopFrame());
					reportDialog.setVisible(true);
				}
			});

			reportDialog.getButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation();
						}
					});
				}
			});

			String[] report = null;
			String dbSqlEx = null;
			try {
				report = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createDatabaseReport(workspace);

				if (report != null) {
					for(String line : report) {
						if (line != null) {
							line = line.replaceAll("\\\\n", "\\\n");
							line = line.replaceAll("\\\\t", "\\\t");
							LOG.print(line);
						}
					}

					LOG.info("Database report successfully generated.");
				} else
					LOG.warn("Generation of database report aborted.");

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reportDialog.dispose();
					}
				});

			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reportDialog.dispose();
					}
				});

				dbSqlEx = sqlEx.getMessage().trim();
				String text = Language.I18N.getString("db.dialog.error.report");
				Object[] args = new Object[]{ dbSqlEx };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Language.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				LOG.error("SQL error: " + dbSqlEx);
			} finally {			
				viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

}
