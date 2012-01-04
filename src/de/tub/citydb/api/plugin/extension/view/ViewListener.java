package de.tub.citydb.api.plugin.extension.view;

import de.tub.citydb.api.event.global.ViewEvent;

public interface ViewListener {
	public void viewActivated(ViewEvent e);
	public void viewDeactivated(ViewEvent e);
}
