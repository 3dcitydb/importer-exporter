package de.tub.citydb.modules.database.gui.operations;

import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.config.project.database.DBOperationType;

public interface DatabaseOperationView extends View {
	public DBOperationType getType();
	public void doTranslation();
	public void setEnabled(boolean enable);
	public void loadSettings();
	public void setSettings();
}
