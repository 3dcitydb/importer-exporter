/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.plugin.util;

import org.citydb.gui.plugin.preferences.PreferencesEntry;
import org.citydb.gui.plugin.preferences.PreferencesEvent;

import java.util.Locale;

public class DefaultPreferencesEntry extends PreferencesEntry {
    private final DefaultPreferencesComponent component;

    public DefaultPreferencesEntry(DefaultPreferencesComponent component) {
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
        return component.getLocalizedTitle();
    }

    @Override
    public final DefaultPreferencesComponent getViewComponent() {
        return component;
    }

    @Override
    public final void addChildEntry(PreferencesEntry child) {
        throw new IllegalArgumentException("Only DefaultPreferencesEntry instances are allowed as children.");
    }

	public final void addChildEntry(DefaultPreferencesEntry child) {
		super.addChildEntry(child);
	}

    public void switchLocale(Locale locale) {
        component.switchLocale(locale);
    }
}
