package org.citydb.gui.plugin;

import org.citydb.core.plugin.extension.Extension;
import org.citydb.gui.plugin.view.ViewController;

import java.util.Locale;

public interface GuiExtension extends Extension {
    void initGuiExtension(ViewController viewController, Locale locale);
    void switchLocale(Locale locale);
}
