package de.tub.citydb.api.plugin.extension.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import de.tub.citydb.api.event.global.ViewEvent;

public abstract class View {
	private List<ViewListener> viewListeners;

	public abstract String getLocalizedTitle();
	public abstract Component getViewComponent();
	public abstract String getToolTip();
	public abstract Icon getIcon();

	public final void addViewListener(ViewListener listener) {
		if (viewListeners == null)
			viewListeners = new ArrayList<ViewListener>();

		viewListeners.add(listener);
	}

	public final boolean removeViewListener(ViewListener listener) {
		return viewListeners != null ? viewListeners.remove(listener) : false;
	}

	public final void fireViewEvent(final ViewEvent e) {
		if (viewListeners != null) {
			for (final ViewListener listener : viewListeners) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						switch (e.getViewState()) {
						case VIEW_ACTIVATED:
							listener.viewActivated(e);
							break;
						case VIEW_DEACTIVATED:
							listener.viewDeactivated(e);
							break;
						}
					}
				});
			}
		}
	}

}
