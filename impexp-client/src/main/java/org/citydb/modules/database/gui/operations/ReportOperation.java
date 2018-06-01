/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.concurrent.locks.ReentrantLock;

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
		
		viewController = parent.getViewController();
		dbConnectionPool = DatabaseConnectionPool.getInstance();

		init();
	}

	private void init() {
		component = new JPanel();
		component.setLayout(new GridBagLayout());

		reportButton = new JButton();		
		component.add(reportButton, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,10,5,10,5));

		reportButton.addActionListener(e -> new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				doOperation();
				return null;
			}
		}.execute());
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

			reportDialog.getButton().addActionListener(e -> SwingUtilities.invokeLater(() ->
					dbConnectionPool.getActiveDatabaseAdapter().getUtil().interruptDatabaseOperation()));

			SwingUtilities.invokeLater(() -> {
				reportDialog.setLocationRelativeTo(viewController.getTopFrame());
				reportDialog.setVisible(true);
			});

			try {
				String[] report = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createDatabaseReport(workspace);
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

				SwingUtilities.invokeLater(reportDialog::dispose);

			} catch (SQLException sqlEx) {
				SwingUtilities.invokeLater(reportDialog::dispose);

				String dbSqlEx = sqlEx.getMessage().trim();
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
