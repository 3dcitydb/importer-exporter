package org.citydb.core.plugin.extension;

import org.citydb.core.plugin.extension.view.ViewController;

import java.util.Locale;

public interface GuiExtension extends Extension {
    void initGuiExtension(ViewController viewController, Locale locale);
    void switchLocale(Locale locale);
}
