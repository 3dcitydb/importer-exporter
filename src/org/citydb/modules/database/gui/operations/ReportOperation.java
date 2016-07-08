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
