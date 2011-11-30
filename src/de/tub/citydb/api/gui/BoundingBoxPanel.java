package de.tub.citydb.api.gui;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class BoundingBoxPanel extends JPanel {
	public abstract BoundingBox getBoundingBox();
	public abstract void setBoundingBox(BoundingBox boundingBox);
	public abstract void clearBoundingBox();
	public abstract DatabaseSrsComboBox getSrsComboBox();
	public abstract void setEditable(boolean editable);
	public abstract void showMapButton(boolean show);
	public abstract void showCopyBoundingBoxButton(boolean show);
	public abstract void showPasteBoundingBoxButton(boolean show);
}
