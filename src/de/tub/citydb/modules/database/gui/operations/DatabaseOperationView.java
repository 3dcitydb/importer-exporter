package de.tub.citydb.modules.database.gui.operations;

import de.tub.citydb.api.event.global.DatabaseConnectionStateEvent;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.config.project.database.DBOperationType;

public abstract class DatabaseOperationView extends View {
	public abstract DBOperationType getType();
	public abstract void doTranslation();
	public abstract void setEnabled(boolean enable);
	public abstract void loadSettings();
	public abstract void setSettings();
	
	public void handleDatabaseConnectionStateEvent(DatabaseConnectionStateEvent event) {
		// nothing to do per default
	}
}
