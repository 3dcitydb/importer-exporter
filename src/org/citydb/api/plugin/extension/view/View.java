/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
