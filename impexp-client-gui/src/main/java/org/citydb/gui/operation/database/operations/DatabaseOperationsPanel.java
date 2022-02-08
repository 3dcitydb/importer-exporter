/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.util.GuiUtil;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.DatabaseConnectionStateEvent;
import org.citydb.util.event.global.EventType;
import org.citydb.util.log.Logger;

import javax.swing.*;
import java.awt.*;

public class DatabaseOperationsPanel extends JPanel implements EventHandler {
    private final Logger log = Logger.getInstance();
    private final Config config;
    private final DatabaseConnectionPool dbConnectionPool;
    private final ViewController viewController;
    private final JTabbedPane operationsTab;
    private final DatabaseOperationView[] operations;

    public DatabaseOperationsPanel(ViewController viewController, Config config) {
        this.config = config;
        this.viewController = viewController;

        dbConnectionPool = DatabaseConnectionPool.getInstance();
        ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

        setLayout(new GridBagLayout());
        operationsTab = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

		operations = new DatabaseOperationView[]{
				new ReportOperation(this),
				new BoundingBoxOperation(this, config),
				new IndexOperation(this, config),
				new SrsOperation(this, config),
				new ADEInfoOperation(this)
		};

        for (int i = 0; i < operations.length; ++i) {
        	operationsTab.insertTab(null, operations[i].getIcon(), null, operations[i].getToolTip(), i);
		}

        operationsTab.addChangeListener(e -> {
            int index = operationsTab.getSelectedIndex();
            for (int i = 0; i < operationsTab.getTabCount(); i++) {
            	operationsTab.setComponentAt(i, index == i ? operations[index].getViewComponent() : null);
			}
        });

		add(operationsTab, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
	}

    public void doTranslation() {
        for (int i = 0; i < operations.length; ++i) {
            operationsTab.setTitleAt(i, operations[i].getLocalizedTitle());
            operations[i].doTranslation();
        }
    }

    public void loadSettings() {
        DatabaseConfig databaseConfig = config.getDatabaseConfig();
        int index = 0;
        for (int i = 0; i < operations.length; ++i) {
            operations[i].loadSettings();
            if (operations[i].getType() == databaseConfig.getOperation().lastUsed()) {
            	index = i;
			}
        }

        operationsTab.setSelectedIndex(-1);
        operationsTab.setSelectedIndex(index);
    }

    public void setSettings() {
		config.getDatabaseConfig().getOperation().setLastUsed(operations[operationsTab.getSelectedIndex()].getType());
        for (DatabaseOperationView operation : operations) {
        	operation.setSettings();
		}
    }

    public void setEnabled(boolean enable) {
        operationsTab.setEnabled(enable);
        for (DatabaseOperationView operation : operations) {
        	operation.setEnabled(enable);
		}
    }

    protected ViewController getViewController() {
        return viewController;
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (operations != null) {
            for (DatabaseOperationView operation : operations) {
                if (operationsTab.getSelectedComponent() != operation.getViewComponent()) {
                    try {
                        SwingUtilities.updateComponentTreeUI(operation.getViewComponent());
                    } catch (Exception e) {
                        log.error("Failed to update UI for component '" + operation.getViewComponent() + "'.", e);
                    }
                }
            }
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent) event;
        for (DatabaseOperationView operation : operations) {
        	operation.handleDatabaseConnectionStateEvent(state);
		}
    }
}
