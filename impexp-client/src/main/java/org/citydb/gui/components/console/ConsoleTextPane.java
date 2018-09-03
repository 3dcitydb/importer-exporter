package org.citydb.gui.components.console;

import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
import java.awt.Dimension;

public class ConsoleTextPane extends JTextPane {
    private boolean lineWrap = false;

    public ConsoleTextPane() {
        DefaultCaret caret = (DefaultCaret) getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        if (!lineWrap)
            return getUI().getPreferredSize(this).width <= getParent().getSize().width;
        else
            return super.getScrollableTracksViewportWidth();
    };

    @Override
    public Dimension getPreferredSize() {
        if (!lineWrap)
            return getUI().getPreferredSize(this);
        else
            return super.getPreferredSize();
    };

    public void setLineWrap(boolean lineWrap) {
        this.lineWrap = lineWrap;
    }

}
