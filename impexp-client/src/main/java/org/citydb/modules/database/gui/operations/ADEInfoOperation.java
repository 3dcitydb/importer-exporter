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

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBOperationType;
import org.citydb.database.connection.ADEMetadata;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.Metadata;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.modules.database.gui.util.ADEInfoRow;
import org.citydb.modules.database.gui.util.ADETableCellRenderer;
import org.citydb.modules.database.gui.util.ADETableModel;
import org.citydb.plugin.extension.view.ViewController;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ADEInfoOperation extends DatabaseOperationView {
    private final ReentrantLock mainLock = new ReentrantLock();
    private final Logger LOG = Logger.getInstance();
    private final DatabaseOperationsPanel parent;
    private final ViewController viewController;
    private final DatabaseConnectionPool dbConnectionPool;
    private final ADEExtensionManager adeManager;

    private JPanel component;
    private JTable adeTable;
    private ADETableModel adeTableModel;

    public ADEInfoOperation(DatabaseOperationsPanel parent) {
        this.parent = parent;

        viewController = parent.getViewController();
        dbConnectionPool = DatabaseConnectionPool.getInstance();
        adeManager = ADEExtensionManager.getInstance();

        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        adeTableModel = new ADETableModel();
        adeTableModel.addRow(ADEInfoRow.DUMMY);

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

        component.add(tablePanel, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.BOTH, 5, 5, 5, 5));
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
        adeTable.getColumnModel().getColumn(0).setHeaderValue("ADE Name");
        adeTable.getColumnModel().getColumn(1).setHeaderValue("Version");
        adeTable.getColumnModel().getColumn(2).setHeaderValue(Language.I18N.getString("main.tabbedPane.database"));
        adeTable.getColumnModel().getColumn(3).setHeaderValue("Importer/Exporter");
    }

    @Override
    public void setEnabled(boolean enable) {
        adeTable.getTableHeader().setEnabled(enable);
        adeTable.setEnabled(enable);
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
            adeTableModel.addRow(ADEInfoRow.DUMMY);
            adeTable.setRowSelectionAllowed(false);
        }

        adeTableModel.fireTableDataChanged();
    }
}
