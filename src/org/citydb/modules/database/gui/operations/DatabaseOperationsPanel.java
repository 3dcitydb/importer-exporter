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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.citydb.api.controller.ViewController;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventHandler;
import org.citydb.api.event.global.DatabaseConnectionStateEvent;
import org.citydb.api.event.global.GlobalEvents;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.util.Util;
import org.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class DatabaseOperationsPanel extends JPanel implements EventHandler {
	private final Config config;

	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JTextField workspace;
	private JFormattedTextField timestamp;

	private DatabaseConnectionPool dbConnectionPool;
	private ViewController viewController;

	private JTabbedPane operationsTab;
	private DatabaseOperationView[] operations;

	public DatabaseOperationsPanel(Config config) {
		this.config = config;
		dbConnectionPool = DatabaseConnectionPool.getInstance();
		viewController = ObjectRegistry.getInstance().getViewController();
		ObjectRegistry.getInstance().getEventDispatcher().addEventHandler(GlobalEvents.DATABASE_CONNECTION_STATE, this);

		init();
	}

	private void init() {
		setLayout(new GridBagLayout());

		workspace = new JTextField();
		timestamp = new JFormattedTextField(new SimpleDateFormat("dd.MM.yyyy"));
		timestamp.setFocusLostBehavior(JFormattedTextField.COMMIT);
		timestamp.setColumns(10);
		workspaceLabel = new JLabel();
		timestampLabel = new JLabel();

		add(workspaceLabel, GuiUtil.setConstraints(0,0,0.0,0.0,GridBagConstraints.NONE,0,0,5,5));
		add(workspace, GuiUtil.setConstraints(1,0,1.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,5));
		add(timestampLabel, GuiUtil.setConstraints(2,0,0.0,0.0,GridBagConstraints.NONE,0,10,5,5));
		add(timestamp, GuiUtil.setConstraints(3,0,0.0,0.0,GridBagConstraints.HORIZONTAL,0,5,5,0));
		timestamp.setMinimumSize(timestamp.getPreferredSize());

		operationsTab = new JTabbedPane();
		GridBagConstraints c = GuiUtil.setConstraints(0,1,1.0,1.0,GridBagConstraints.BOTH,5,0,0,0);
		c.gridwidth = 4;
		add(operationsTab, c);

		operations = new DatabaseOperationView[3];
		operations[0] = new ReportOperation(this);
		operations[1] = new BoundingBoxOperation(this, config);
		operations[2] = new IndexOperation(config);

		for (int i = 0; i < operations.length; ++i)
			operationsTab.insertTab(null, operations[i].getIcon(), null, operations[i].getToolTip(), i);

		operationsTab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {			
				int index = operationsTab.getSelectedIndex();
				for (int i = 0; i < operationsTab.getTabCount(); ++i)
					operationsTab.setComponentAt(i, index == i ? operations[index].getViewComponent() : null);
			}
		});

		PopupMenuDecorator.getInstance().decorate(workspace, timestamp);
	}

	public void doTranslation() {
		workspaceLabel.setText(Language.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Language.I18N.getString("common.label.timestamp"));

		for (int i = 0; i < operations.length; ++i) {
			operationsTab.setTitleAt(i, operations[i].getLocalizedTitle());
			operations[i].doTranslation();
		}
	}

	public void loadSettings() {
		Database db = config.getProject().getDatabase();
		workspace.setText(db.getWorkspaces().getOperationWorkspace().getName());
		timestamp.setText(db.getWorkspaces().getOperationWorkspace().getTimestamp());

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
		Database db = config.getProject().getDatabase();

		db.getOperation().setLastUsed(operations[operationsTab.getSelectedIndex()].getType());
		db.getWorkspaces().getOperationWorkspace().setName(workspace.getText().trim());
		db.getWorkspaces().getOperationWorkspace().setTimestamp(timestamp.getText().trim());

		for (int i = 0; i < operations.length; ++i)
			operations[i].setSettings();
	}

	public void setEnabled(boolean enable) {		
		setEnabledWorkspace(enable);

		operationsTab.setEnabled(enable);
		for (int i = 0; i < operations.length; ++i)
			operations[i].setEnabled(enable);
	}
	
	public void setEnabledWorkspace(boolean enable) {
		if (enable && dbConnectionPool.isConnected() && !dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport())
			enable = false;
		
		workspaceLabel.setEnabled(enable);
		workspace.setEnabled(enable);
		timestampLabel.setEnabled(enable);
		timestamp.setEnabled(enable);
	}

	public boolean existsWorkspace() {		
		if (!dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getText())) {
			Workspace tmp = new Workspace(workspace.getText().trim(), timestamp.getText().trim());
			return dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().existsWorkspace(tmp, true);
		}

		return true;
	}

	public Workspace getWorkspace() {
		setSettings();
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getOperationWorkspace();

		if (!Util.checkWorkspaceTimestamp(workspace)) {
			JOptionPane.showMessageDialog(
					viewController.getTopFrame(), 
					Language.I18N.getString("common.dialog.error.incorrectData.date"), 
					Language.I18N.getString("db.dialog.error.operation.incorrectData"), 
					JOptionPane.ERROR_MESSAGE);

			return null;
		}

		return workspace;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		DatabaseConnectionStateEvent state = (DatabaseConnectionStateEvent)event;
		for (int i = 0; i < operations.length; ++i)
			operations[i].handleDatabaseConnectionStateEvent(state);
	}

}
