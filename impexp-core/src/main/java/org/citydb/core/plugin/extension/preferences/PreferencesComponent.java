package org.citydb.core.plugin.extension.preferences;

import javax.swing.*;
import java.awt.*;

public class PreferencesComponent extends JPanel {
    private boolean isScrollable = true;

    public PreferencesComponent(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public PreferencesComponent(LayoutManager layout) {
        super(layout);
    }

    public PreferencesComponent(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public PreferencesComponent() {
        super();
    }

    public boolean isScrollable() {
        return isScrollable;
    }

    public void setScrollable(boolean scrollable) {
        isScrollable = scrollable;
    }
}
