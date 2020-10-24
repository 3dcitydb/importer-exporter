/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.Event;
import org.citydb.event.EventHandler;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.event.global.EventType;
import org.citydb.gui.components.common.DatePicker;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.registry.ObjectRegistry;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class DatabaseOperationsPanel extends JPanel implements EventHandler {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final DatabaseConnectionPool dbConnectionPool;
	private final ViewController viewController;

	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JXTextField workspace;
	private DatePicker datePicker;

	private JTabbedPane operationsTab;
	private DatabaseOperationView[] operations;

	public DatabaseOperationsPanel(ViewController viewController, Config config) {
		this.config = config;
		this.viewController = viewController;

		dbConnectionPool = DatabaseConnectionPool.getInstance();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(EventType.DATABASE_CONNECTION_STATE, this);

		init();
	}

	private void init() {
		setLayout(new GridBagLayout());

		workspace = new JXTextField();
		workspace.setPromptForeground(Color.LIGHT_GRAY);
		workspace.setFocusBehavior(FocusBehavior.SHOW_PROMPT);
		datePicker = new DatePicker();
		workspaceLabel = new JLabel();
		timestampLabel = new JLabel();

		add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.BOTH,0,0,5,5));
		add(workspace, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
		add(datePicker, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,0));

		operationsTab = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		add(operationsTab, GuiUtil.setConstraints(0,2,4,1,1.0,1.0,GridBagConstraints.BOTH,5,0,0,0));

		operations = new DatabaseOperationView[]{
				new ReportOperation(this),
				new BoundingBoxOperation(this, config),
				new IndexOperation(this, config),
				new SrsOperation(this, config),
				new ADEInfoOperation(this)
		};

		for (int i = 0; i < operations.length; ++i)
			operationsTab.insertTab(null, operations[i].getIcon(), null, operations[i].getToolTip(), i);

		operationsTab.addChangeListener(e -> {
			int index = operationsTab.getSelectedIndex();
			for (int i = 0; i < operationsTab.getTabCount(); i++)
				operationsTab.setComponentAt(i, index == i ? operations[index].getViewComponent() : null);
		});

		PopupMenuDecorator.getInstance().decorate(workspace, datePicker.getEditor());
	}

	public void doTranslation() {
		workspaceLabel.setText(Language.I18N.getString("common.label.workspace"));
		workspace.setPrompt(Language.I18N.getString("common.label.workspace.prompt"));
		timestampLabel.setText(Language.I18N.getString("common.label.timestamp"));
		
		for (int i = 0; i < operations.length; ++i) {
			operationsTab.setTitleAt(i, operations[i].getLocalizedTitle());
			operations[i].doTranslation();
		}
	}

	public void loadSettings() {
		DatabaseConfig db = config.getDatabaseConfig();
		workspace.setText(db.getWorkspaces().getOperationWorkspace().getName());
		datePicker.setDate(db.getWorkspaces().getOperationWorkspace().getTimestamp());

		int index = 0;
		for (int i = 0; i < operations.length; ++i) {
			operations[i].loadSettings();			
			if (operations[i].getType() == db.getOperation().lastUsed())
				index = i;
		}

		operationsTab.setSelectedIndex(-1);
		operationsTab.setSelectedIndex(index);
	}

	public void setSettings() {
		DatabaseConfig db = config.getDatabaseConfig();
		db.getOperation().setLastUsed(operations[operationsTab.getSelectedIndex()].getType());
		db.getWorkspaces().getOperationWorkspace().setName(workspace.getText());
		db.getWorkspaces().getOperationWorkspace().setTimestamp(datePicker.getDate());

		for (DatabaseOperationView operation : operations)
			operation.setSettings();
	}

	public void setEnabled(boolean enable) {		
		setEnabledWorkspace(enable);

		operationsTab.setEnabled(enable);
		for (DatabaseOperationView operation : operations)
			operation.setEnabled(enable);
	}
	
	public void setEnabledWorkspace(boolean enable) {
		if (enable && dbConnectionPool.isConnected() && !dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport())
			enable = false;
		
		workspaceLabel.setEnabled(enable);
		workspace.setEnabled(enable);
		timestampLabel.setEnabled(enable);
		datePicker.setEnabled(enable);
	}
	
	public boolean checkWorkspace() {
		if (!dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getText())) {
			try {
				Workspace tmp = new Workspace(workspace.getText().trim(), datePicker.getDate());
				log.info("Switching to database workspace " + tmp + ".");
				dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().checkWorkspace(tmp);
			} catch (SQLException e) {
				log.error(e.getMessage(), e.getCause());
				return false;
			}
		}

		return true;
	}
	
	public Workspace getWorkspace() {
		setSettings();
		return config.getDatabaseConfig().getWorkspaces().getOperationWorkspace();
	}

	protected ViewController getViewController() {
		return viewController;
	}
	
	@Override
	public void handleEvent(Event event) throws Exception {
		DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent)event;
		for (DatabaseOperationView operation : operations)
			operation.handleDatabaseConnectionStateEvent(state);
	}

}
