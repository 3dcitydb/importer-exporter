package de.tub.citydb.modules.database.gui.operations;

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

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.controller.ViewController;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.gui.factory.PopupMenuDecorator;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;
import de.tub.citydb.util.gui.GuiUtil;

@SuppressWarnings("serial")
public class DatabaseOperationsPanel extends JPanel implements EventHandler {
	private final Logger LOG = Logger.getInstance();
	private final Config config;
	
	private JLabel workspaceLabel;
	private JLabel timestampLabel;
	private JTextField workspace;
	private JFormattedTextField timestamp;
	
	private DatabaseController databaseController;
	private ViewController viewController;

	private JTabbedPane operationsTab;
	private DatabaseOperationView[] operations;
	
	public DatabaseOperationsPanel(Config config) {
		this.config = config;
		databaseController = ObjectRegistry.getInstance().getDatabaseController();
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
		workspaceLabel.setText(Internal.I18N.getString("common.label.workspace"));
		timestampLabel.setText(Internal.I18N.getString("common.label.timestamp"));

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
		
		String workspaceText = workspace.getText().trim();
		if (!workspaceText.equals(Internal.ORACLE_DEFAULT_WORKSPACE) && 
				(workspaceText.length() == 0 || workspaceText.toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)))
			workspace.setText(Internal.ORACLE_DEFAULT_WORKSPACE);

		db.getOperation().setLastUsed(operations[operationsTab.getSelectedIndex()].getType());
		db.getWorkspaces().getOperationWorkspace().setName(workspace.getText());
		db.getWorkspaces().getOperationWorkspace().setTimestamp(timestamp.getText());
		
		for (int i = 0; i < operations.length; ++i)
			operations[i].setSettings();
	}
	
	public void setEnabled(boolean enable) {
		workspaceLabel.setEnabled(enable);
		workspace.setEnabled(enable);
		timestampLabel.setEnabled(enable);
		timestamp.setEnabled(enable);
		
		operationsTab.setEnabled(enable);
		for (int i = 0; i < operations.length; ++i)
			operations[i].setEnabled(enable);
	}
	
	public boolean existsWorkspace() {		
		if (!workspace.getText().trim().toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)) {
			boolean workspaceExists = databaseController.existsWorkspace(workspace.getText());

			String name = "'" + workspace.getText().trim() + "'";
			String time = timestamp.getText();
			if (time.trim().length() > 0)
				name += " at timestamp " + time;

			if (!workspaceExists) {
				LOG.error("Database workspace " + name + " is not available.");
				return false;
			} else 
				LOG.info("Switching to database workspace " + name + '.');
		}

		return true;
	}

	public Workspace getWorkspace() {
		setSettings();
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getOperationWorkspace();
		
		if (!Util.checkWorkspaceTimestamp(workspace)) {
			JOptionPane.showMessageDialog(
					viewController.getTopFrame(), 
					Internal.I18N.getString("common.dialog.error.incorrectData.date"), 
					Internal.I18N.getString("db.dialog.error.operation.incorrectData"), 
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
