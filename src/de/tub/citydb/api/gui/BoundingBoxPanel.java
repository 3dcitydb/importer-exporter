package de.tub.citydb.api.gui;

import javax.swing.JPanel;

import de.tub.citydb.api.config.BoundingBox;

@SuppressWarnings("serial")
public abstract class BoundingBoxPanel extends JPanel {
	public abstract BoundingBox getBoundingBox();
	public abstract void setBoundingBox(BoundingBox boundingBox);
}
