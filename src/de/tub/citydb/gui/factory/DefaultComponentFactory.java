package de.tub.citydb.gui.factory;

import de.tub.citydb.api.gui.ComponentFactory;
import de.tub.citydb.api.gui.DatabaseSrsComboBox;
import de.tub.citydb.api.gui.StandardEditingPopupMenuDecorator;
import de.tub.citydb.config.Config;

public class DefaultComponentFactory implements ComponentFactory {
	private static DefaultComponentFactory instance;
	private final Config config;
	
	private DefaultComponentFactory(Config config) {
		this.config = config;
	}
	
	public static synchronized DefaultComponentFactory getInstance(Config config) {
		if (instance == null)
			instance = new DefaultComponentFactory(config);
		
		return instance;
	}
	
	@Override
	public DatabaseSrsComboBox createDatabaseSrsComboBox() {
		return SrsComboBoxFactory.getInstance(config).createSrsComboBox(true);
	}

	@Override
	public StandardEditingPopupMenuDecorator createPopupMenuDecorator() {
		return PopupMenuDecorator.getInstance();
	}

}
