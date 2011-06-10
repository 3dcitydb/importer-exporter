package de.tub.citydb.api.gui;

import javax.swing.JComboBox;

import de.tub.citydb.api.database.DatabaseSrs;

@SuppressWarnings("serial")
public abstract class DatabaseSrsComboBox extends JComboBox {
	
	@Override
	public DatabaseSrs getSelectedItem() {
		Object object = super.getSelectedItem();
		return (object instanceof DatabaseSrs) ? (DatabaseSrs)object : null;
	}
}
