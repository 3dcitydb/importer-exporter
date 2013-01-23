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
package de.tub.citydb.plugins.matching_merging.gui.preferences;

import de.tub.citydb.api.plugin.extension.preferences.PreferencesEntry;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesEvent;

public class DefaultPreferencesEntry extends PreferencesEntry {
	public AbstractPreferencesComponent component;
	
	public DefaultPreferencesEntry(AbstractPreferencesComponent component) {
		this.component = component;
	}
	
	@Override
	public boolean isModified() {
		return component.isModified();
	}

	@Override
	public boolean handleEvent(PreferencesEvent event) {
		switch (event) {
		case APPLY_SETTINGS:
			component.setSettings();
			break;
		case RESTORE_SETTINGS:
			component.loadSettings();
			break;
		case SET_DEFAULT_SETTINGS:
			component.resetSettings();
			break;
		}
		
		return true;
	}

	@Override
	public String getLocalizedTitle() {
		return component.getTitle();
	}

	@Override
	public final AbstractPreferencesComponent getViewComponent() {
		return component;
	}
	
	@Override
	public final void addChildEntry(PreferencesEntry child) {
		if (!(child instanceof DefaultPreferencesEntry))
			throw new IllegalArgumentException("Only DefaultPreferencesEntry instances are allowed as child entries.");
		
		super.addChildEntry(child);
	}

	public void switchLocale() {
		component.switchLocale();
	}

}
