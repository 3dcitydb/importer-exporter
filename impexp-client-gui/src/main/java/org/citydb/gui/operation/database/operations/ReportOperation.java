/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.database.operations;

import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseOperationType;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.gui.components.dialog.StatusDialog;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportOperation extends DatabaseOperationView {
    private final ReentrantLock mainLock = new ReentrantLock();
    private final Logger log = Logger.getInstance();
    private final ViewController viewController;
    private final DatabaseConnectionPool dbConnectionPool;

    private JPanel component;
    private JButton reportButton;

    public ReportOperation(DatabaseOperationsPanel parent) {
        viewController = parent.getViewController();
        dbConnectionPool = DatabaseConnectionPool.getInstance();

        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        reportButton = new JButton();
        component.add(reportButton, GuiUtil.setConstraints(0, 0, 0, 0, GridBagConstraints.NONE, 15, 0, 10, 0));

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
    public DatabaseOperationType getType() {
        return DatabaseOperationType.REPORT;
    }

    @Override
    public void switchLocale(Locale locale) {
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
            viewController.clearConsole();
            viewController.setStatusText(Language.I18N.getString("main.status.database.report.label"));

            log.info("Generating database report...");

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
                String[] report = dbConnectionPool.getActiveDatabaseAdapter().getUtil().createDatabaseReport();
                Pattern pattern = Pattern.compile("^(#[^\\s\\\\]+)[^\\d]+(\\d+).*$");
                Matcher matcher = pattern.matcher("");

                if (report != null) {
                    for (String line : report) {
                        if (line != null) {
                            matcher.reset(line);
                            if (matcher.matches()) {
                                StringBuilder formatted = new StringBuilder(matcher.group(1));
                                while (formatted.length() < 30)
                                    formatted.append(' ');

                                line = formatted.append("  ").append(matcher.group(2)).toString();
                            }

                            log.print(line);
                        }
                    }

                    log.info("Database report successfully generated.");
                } else
                    log.warn("Generation of database report aborted.");

                SwingUtilities.invokeLater(reportDialog::dispose);

            } catch (SQLException e) {
                SwingUtilities.invokeLater(reportDialog::dispose);
                viewController.errorMessage(Language.I18N.getString("common.dialog.error.db.title"),
                        MessageFormat.format(Language.I18N.getString("db.dialog.error.report"), e.getMessage().trim()));

                log.error("Failed to generate the database report.", e);
            } finally {
                viewController.setStatusText(Language.I18N.getString("main.status.ready.label"));
            }

        } finally {
            lock.unlock();
        }
    }

}
