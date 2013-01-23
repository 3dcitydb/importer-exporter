/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
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
