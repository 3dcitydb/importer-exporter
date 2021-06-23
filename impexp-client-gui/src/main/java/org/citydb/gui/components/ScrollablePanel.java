package org.citydb.gui.components;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;

public class ScrollablePanel extends JPanel implements Scrollable {
    private final boolean tracksViewportWidth;
    private final boolean tracksViewportHeight;

    public ScrollablePanel(boolean tracksViewportWidth, boolean tracksViewportHeight) {
        this.tracksViewportWidth = tracksViewportWidth;
        this.tracksViewportHeight = tracksViewportHeight;
    }

    public ScrollablePanel() {
        this(false, false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        int increment;
        switch (orientation) {
            case SwingConstants.VERTICAL:
                increment = visibleRect.height / 10;
                break;
            case SwingConstants.HORIZONTAL:
                increment = visibleRect.width / 10;
                break;
            default:
                increment = 10;
        }

        return UIScale.scale(increment);
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return tracksViewportWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return tracksViewportHeight;
    }
}
