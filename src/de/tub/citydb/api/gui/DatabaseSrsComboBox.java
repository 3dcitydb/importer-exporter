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
	
	@Override
	public DatabaseSrs getItemAt(int index) {
		Object object = super.getItemAt(index);
		return (object instanceof DatabaseSrs) ? (DatabaseSrs)object : null;
	}
	
	@Override
	public void addItem(Object anObject) {
		if (anObject instanceof DatabaseSrs)
			super.addItem(anObject);
	}

	@Override
	public void insertItemAt(Object anObject, int index) {
		if (anObject instanceof DatabaseSrs)
			super.insertItemAt(anObject, index);
	}
	
}
