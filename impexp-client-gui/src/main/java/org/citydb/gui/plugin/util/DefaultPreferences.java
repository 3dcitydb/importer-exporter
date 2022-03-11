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

import org.citydb.gui.plugin.preferences.Preferences;
import org.citydb.gui.plugin.preferences.PreferencesEntry;

import java.util.Locale;

public class DefaultPreferences implements Preferences {
    protected final DefaultPreferencesEntry rootEntry;

    public DefaultPreferences(DefaultPreferencesEntry rootEntry) {
        this.rootEntry = rootEntry;
    }

    @Override
    public final DefaultPreferencesEntry getPreferencesEntry() {
        return rootEntry;
    }

    public void switchLocale(Locale locale) {
        switchLocale(rootEntry, locale);
    }

    private void switchLocale(DefaultPreferencesEntry entry, Locale locale) {
        entry.switchLocale(locale);
        for (PreferencesEntry child : entry.getChildEntries()) {
            switchLocale((DefaultPreferencesEntry) child, locale);
        }
    }

    public void loadSettings() {
        loadSettings(rootEntry);
    }

    private void loadSettings(DefaultPreferencesEntry entry) {
        entry.getViewComponent().loadSettings();
        for (PreferencesEntry child : entry.getChildEntries()) {
			loadSettings((DefaultPreferencesEntry) child);
		}
    }

    public void setSettings() {
        setSettings(rootEntry);
    }

    private void setSettings(DefaultPreferencesEntry entry) {
        entry.getViewComponent().setSettings();
        for (PreferencesEntry child : entry.getChildEntries()) {
			setSettings((DefaultPreferencesEntry) child);
		}
    }
}
