package de.tub.citydb.modules.database.gui.operations;

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

import de.tub.citydb.api.controller.ViewController;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBOperationType;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.gui.components.StatusDialog;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.database.DBUtil;
import de.tub.citydb.util.gui.GuiUtil;

public class ReportOperation implements DatabaseOperationView {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger LOG = Logger.getInstance();
	private final DatabaseOperationsPanel parent;
	private final ViewController viewController;

	private JPanel component;
	private JButton reportButton;

	public ReportOperation(DatabaseOperationsPanel parent) {
		this.parent = parent;
		viewController = ObjectRegistry.getInstance().getViewController();

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
		return Internal.I18N.getString("db.label.operation.report");
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
		reportButton.setText(Internal.I18N.getString("db.button.report"));
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
			viewController.setStatusText(Internal.I18N.getString("main.status.database.report.label"));

			LOG.info("Generating database report...");			

			final StatusDialog reportDialog = new StatusDialog(viewController.getTopFrame(), 
					Internal.I18N.getString("db.dialog.report.window"), 
					Internal.I18N.getString("db.dialog.report.title"), 
					null,
					Internal.I18N.getString("db.dialog.report.details"), 
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
							DBUtil.cancelOperation();
						}
					});
				}
			});

			String[] report = null;
			String dbSqlEx = null;
			try {
				if (parent.existsWorkspace()) {
					report = DBUtil.databaseReport(workspace);

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
				}

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
				String text = Internal.I18N.getString("db.dialog.error.report");
				Object[] args = new Object[]{ dbSqlEx };
				String result = MessageFormat.format(text, args);

				JOptionPane.showMessageDialog(
						viewController.getTopFrame(), 
						result, 
						Internal.I18N.getString("common.dialog.error.db.title"),
						JOptionPane.ERROR_MESSAGE);

				LOG.error("SQL error: " + dbSqlEx);
			} finally {			
				viewController.setStatusText(Internal.I18N.getString("main.status.ready.label"));
			}

		} finally {
			lock.unlock();
		}
	}

}
