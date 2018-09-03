package org.citydb.gui.components.console;

import javax.swing.JTextPane;
import javax.swing.text.Style;
import java.awt.Dimension;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ConsoleTextPane extends JTextPane {
    private final int MAX_LINE_COUNT = 10000;
    private boolean lineWrap = false;

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

    public int getMaxLineCount() {
        return MAX_LINE_COUNT;
    }

    public PrintStream getConsolePrintStream(Style style) {
        Charset encoding = StandardCharsets.UTF_8;
        ConsoleOutputStream outputStream = new ConsoleOutputStream(this, encoding, style);

        PrintStream printStream;
        try {
            printStream = new PrintStream(outputStream, true, encoding.displayName()) {};
        } catch (UnsupportedEncodingException e) {
            printStream = new PrintStream(outputStream, true);
        }

        return printStream;
    }

    public PrintStream getConsolePrintStream() {
        return getConsolePrintStream(null);
    }
}
