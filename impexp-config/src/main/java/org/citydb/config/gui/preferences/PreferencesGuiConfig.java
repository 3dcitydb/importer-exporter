package org.citydb.config.gui.preferences;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "PreferencesGuiType", propOrder = {
        "showPreferencesConfirmDialog"
})
public class PreferencesGuiConfig {
    private boolean showPreferencesConfirmDialog = true;

    public boolean isShowPreferencesConfirmDialog() {
        return showPreferencesConfirmDialog;
    }

    public void setShowPreferencesConfirmDialog(boolean showPreferencesConfirmDialog) {
        this.showPreferencesConfirmDialog = showPreferencesConfirmDialog;
    }
}
