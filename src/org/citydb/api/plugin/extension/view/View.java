/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.api.plugin.extension.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.citydb.api.event.global.ViewEvent;

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
