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
package org.citydb.api.event.global;

import org.citydb.api.event.Event;
import org.citydb.api.plugin.extension.view.View;

public class ViewEvent extends Event {
	public enum ViewState {
		VIEW_ACTIVATED,
		VIEW_DEACTIVATED
	}
	
	private final View view;
	private final ViewState viewState;
	
	public ViewEvent(View view, ViewState viewState, Object source) {
		super(GlobalEvents.VIEW_STATE, GLOBAL_CHANNEL, source);
		this.view = view;
		this.viewState = viewState;
	}
	
	public View getView() {
		return view;
	}

	public ViewState getViewState() {
		return viewState;
	}
	
}
