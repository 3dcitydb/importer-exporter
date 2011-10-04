package de.tub.citydb.api.gui;

public interface ComponentFactory {
	public DatabaseSrsComboBox createDatabaseSrsComboBox();
	public StandardEditingPopupMenuDecorator createPopupMenuDecorator();
	public BoundingBoxPanel createBoundingBoxPanel();
}
