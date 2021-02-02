package org.citydb.gui.modules.importer.preferences;

import org.citydb.config.i18n.Language;
import org.citydb.gui.modules.common.DefaultPreferencesEntry;
import org.citydb.gui.modules.common.NullComponent;

public class CityGMLOptionsPanel extends DefaultPreferencesEntry {

    public CityGMLOptionsPanel() {
        super(NullComponent.getInstance());
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("pref.tree.import.cityGMLOptions");
    }
}
