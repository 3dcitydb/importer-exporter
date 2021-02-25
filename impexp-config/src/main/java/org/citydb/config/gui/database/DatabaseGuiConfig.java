package org.citydb.config.gui.database;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DatabaseGuiType", propOrder = {
        "collapseFeatureVersionFilter",
        "showOutdatedDatabaseVersionWarning",
        "showUnsupportedADEWarning",
        "showChangeSridWarning"
})
public class DatabaseGuiConfig {
    private boolean collapseFeatureVersionFilter = false;
    private boolean showOutdatedDatabaseVersionWarning = true;
    private boolean showUnsupportedADEWarning = true;
    private boolean showChangeSridWarning = true;

    public boolean isCollapseFeatureVersionFilter() {
        return collapseFeatureVersionFilter;
    }

    public void setCollapseFeatureVersionFilter(boolean collapseFeatureVersionFilter) {
        this.collapseFeatureVersionFilter = collapseFeatureVersionFilter;
    }

    public boolean isShowOutdatedDatabaseVersionWarning() {
        return showOutdatedDatabaseVersionWarning;
    }

    public void setShowOutdatedDatabaseVersionWarning(boolean showOutdatedDatabaseVersionWarning) {
        this.showOutdatedDatabaseVersionWarning = showOutdatedDatabaseVersionWarning;
    }

    public boolean isShowUnsupportedADEWarning() {
        return showUnsupportedADEWarning;
    }

    public void setShowUnsupportedADEWarning(boolean showUnsupportedADEWarning) {
        this.showUnsupportedADEWarning = showUnsupportedADEWarning;
    }

    public boolean isShowChangeSridWarning() {
        return showChangeSridWarning;
    }

    public void setShowChangeSridWarning(boolean showChangeSridWarning) {
        this.showChangeSridWarning = showChangeSridWarning;
    }
}
