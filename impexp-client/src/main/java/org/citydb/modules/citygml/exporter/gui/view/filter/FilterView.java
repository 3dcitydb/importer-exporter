package org.citydb.modules.citygml.exporter.gui.view.filter;

import org.citydb.config.Config;
import org.citydb.plugin.extension.view.View;

public abstract class FilterView extends View {
    final Config config;

    public abstract void doTranslation();
    public abstract void setEnabled(boolean enable);
    public abstract void loadSettings();
    public abstract void setSettings();

    public FilterView(Config config) {
        this.config = config;
    }
}
