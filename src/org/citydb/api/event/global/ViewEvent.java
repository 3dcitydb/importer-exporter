/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
