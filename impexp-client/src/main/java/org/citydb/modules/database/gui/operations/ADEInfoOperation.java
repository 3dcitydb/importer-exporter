/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.database.connection.ADEMetadata;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.Metadata;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SchemaMappingException;
import org.citydb.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.database.schema.util.SchemaMappingUtil;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.modules.database.gui.util.ADEInfoDialog;
import org.citydb.modules.database.gui.util.ADEInfoRow;
import org.citydb.modules.database.gui.util.ADETableCellRenderer;
import org.citydb.modules.database.gui.util.ADETableModel;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ADEInfoOperation extends DatabaseOperationView {
    private final ReentrantLock mainLock = new ReentrantLock();
    private final Logger log = Logger.getInstance();
    private final ViewController viewController;
    private final DatabaseConnectionPool dbConnectionPool;
    private final ADEExtensionManager adeManager;

    private JPanel component;
    private JTable adeTable;
    private ADETableModel adeTableModel;
    private JButton infoButton;

    public ADEInfoOperation(DatabaseOperationsPanel parent) {
        viewController = parent.getViewController();
        dbConnectionPool = DatabaseConnectionPool.getInstance();
        adeManager = ADEExtensionManager.getInstance();

        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        adeTableModel = new ADETableModel();
        adeTableModel.addRow(ADEInfoRow.NO_ADES_ENTRY);

        adeTable = new JTable(adeTableModel);
        adeTable.setRowHeight(ADEInfoRow.getDefaultRowHeight());
        adeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        adeTable.getTableHeader().setDefaultRenderer(new ADETableCellRenderer(adeTable.getTableHeader().getDefaultRenderer()));
        for (int i = 0; i < adeTable.getColumnModel().getColumnCount(); i++)
            adeTable.getColumnModel().getColumn(i).setCellRenderer(
                    new ADETableCellRenderer(adeTable.getDefaultRenderer(adeTableModel.getColumnClass(i))));

        adeTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        adeTable.getColumnModel().getColumn(1).setPreferredWidth(5);
        adeTable.getColumnModel().getColumn(2).setPreferredWidth(5);
        adeTable.getColumnModel().getColumn(3).setPreferredWidth(5);

        Box tablePanel = Box.createVerticalBox();
        tablePanel.add(adeTable.getTableHeader());
        tablePanel.add(adeTable);

        infoButton = new JButton();

        component.add(tablePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 10, 5, 5, 5));
        component.add(infoButton, GuiUtil.setConstraints(0, 1, 0, 0, GridBagConstraints.NONE, 10, 5, 10, 5));

        adeTable.getSelectionModel().addListSelectionListener(l -> {
            if (!infoButton.isEnabled()) {
                if (adeTableModel.getRow(adeTable.getSelectedRow()) != ADEInfoRow.NO_ADES_ENTRY)
                    infoButton.setEnabled(true);
            }
        });

        ActionListener infoListener = l -> {
            ADEInfoRow adeInfo = adeTableModel.getRow(adeTable.getSelectedRow());
            if (adeInfo != ADEInfoRow.NO_ADES_ENTRY) {
                new SwingWorker<Void, Void>() {
                    protected Void doInBackground() {
                        displayADEInfo(adeInfo);
                        return null;
                    }
                }.execute();
            }
        };

        infoButton.addActionListener(infoListener);

        adeTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2)
                    infoListener.actionPerformed(new ActionEvent(adeTable, 1, "Info"));
            }
        });
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("db.label.operation.ade");
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
        return DBOperationType.ADE;
    }

    @Override
    public void doTranslation() {
        adeTable.getColumnModel().getColumn(0).setHeaderValue(Language.I18N.getString("db.dialog.ade.label.name"));
        adeTable.getColumnModel().getColumn(1).setHeaderValue(Language.I18N.getString("db.dialog.ade.label.version"));
        adeTable.getColumnModel().getColumn(2).setHeaderValue(Language.I18N.getString("main.tabbedPane.database"));
        adeTable.getColumnModel().getColumn(3).setHeaderValue("Importer/Exporter");
        infoButton.setText(Language.I18N.getString("db.button.info"));
        ADEInfoRow.NO_ADES_ENTRY.setName(Language.I18N.getString("db.dialog.ade.label.notAvailable"));
    }

    @Override
    public void setEnabled(boolean enable) {
        adeTable.getTableHeader().setEnabled(enable);
        adeTable.setEnabled(enable);
        infoButton.setEnabled(false);
    }

    @Override
    public void loadSettings() {
        // nothing to do here...
    }

    @Override
    public void setSettings() {
        // nothing to do here...
    }

    @Override
    public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
        adeTableModel.reset();

        if (event.isConnected()) {
            Map<String, ADEInfoRow> adeInfoRows = new HashMap<>();

            for (ADEMetadata metadata : dbConnectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getRegisteredADEs()) {
                ADEInfoRow adeInfoRow = new ADEInfoRow(metadata.getADEId(), metadata.getName(), metadata.getVersion(), true, false);
                adeInfoRows.put(metadata.getADEId(), adeInfoRow);
            }

            for (ADEExtension adeExtension : adeManager.getExtensions()) {
                ADEInfoRow adeInfoRow = adeInfoRows.get(adeExtension.getId());
                if (adeInfoRow != null)
                    adeInfoRow.setImpexpSupport(adeExtension.isEnabled());
                else {
                    Metadata metadata = adeExtension.getMetadata();
                    adeInfoRow = new ADEInfoRow(adeExtension.getId(), metadata.getName(), metadata.getVersion(), false, true);
                    adeInfoRows.put(adeExtension.getId(), adeInfoRow);
                }
            }

            for (ADEInfoRow adeInfoRow : adeInfoRows.values())
                adeTableModel.addRow(adeInfoRow);

            adeTable.setRowSelectionAllowed(true);
        }

        if (!adeTableModel.hasRows()) {
            adeTableModel.addRow(ADEInfoRow.NO_ADES_ENTRY);
            adeTable.setRowSelectionAllowed(false);
        }

        adeTableModel.fireTableDataChanged();
    }

    private void displayADEInfo(ADEInfoRow adeInfo) {
        final ReentrantLock lock = this.mainLock;
        lock.lock();

        try {
            SchemaMapping rootSchema = ObjectRegistry.getInstance().getSchemaMapping();
            SchemaMapping adeSchema = null;

            try {
                if (adeInfo.hasImpexpSupport()) {
                    ADEExtension adeExtension = adeManager.getExtensionById(adeInfo.getId());
                    if (adeExtension != null)
                        adeSchema = SchemaMappingUtil.getInstance().unmarshal(rootSchema, adeExtension.getSchemaMappingFile().toFile());
                } else if (adeInfo.hasDatabaseSupport()) {
                    log.info("Loading ADE information from database...");
                    adeSchema = dbConnectionPool.getActiveDatabaseAdapter().getUtil().getADESchemaMapping(adeInfo.getId(), rootSchema);
                }

            } catch (SQLException | JAXBException | SchemaMappingException | SchemaMappingValidationException e) {
                log.error("Failed to retrieve ADE information for '" + adeInfo.getName() + "'.");
                log.error(e.getClass().getTypeName() + ": " + e.getMessage());
                return;
            }

            if (adeSchema == null) {
                log.error("Failed to retrieve ADE information for '" + adeInfo.getName() + "'.");
                return;
            }

            ADEInfoDialog dialog = new ADEInfoDialog(adeInfo, adeSchema, rootSchema, viewController.getTopFrame());
            SwingUtilities.invokeLater(() -> {
                dialog.setLocationRelativeTo(viewController.getTopFrame());
                dialog.setVisible(true);
            });

        } finally {
            lock.unlock();
        }
    }
}
