package org.citydb.gui.components.common;

import javax.swing.*;
import java.awt.*;

public class AlphaButton extends JButton {

    public AlphaButton() {
        super();
    }

    public AlphaButton(Icon icon) {
        super(icon);
    }

    public AlphaButton(String text) {
        super(text);
    }

    public AlphaButton(String text, Icon icon) {
        super(text, icon);
    }

    public AlphaButton(Action a) {
        super(a);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor( getBackground() );
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    @Override
    public boolean isOpaque() {
        return false;
    }
}
