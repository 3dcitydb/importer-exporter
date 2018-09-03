package org.citydb.gui.components.console;

import org.citydb.config.project.global.LogLevel;
import org.citydb.log.ConsoleLogger;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class StyledConsoleLogger implements ConsoleLogger {
    private final int MAX_LINE_COUNT = 10000;
    private final JTextPane textPane;
    private final StyledDocument doc;
    private final Charset encoding;

    private final StyleContext context;
    private final PrintStream out;
    private final PrintStream err;

    public StyledConsoleLogger(JTextPane textPane, Charset encoding) {
        this.textPane = textPane;
        this.encoding = encoding;

        doc = textPane.getStyledDocument();
        context = new StyleContext();

        Style info = context.addStyle(LogLevel.INFO.value(), null);

        Style debug = context.addStyle(LogLevel.DEBUG.value(), null);
        StyleConstants.setForeground(debug, new Color(0, 0, 238));

        Style warn = context.addStyle(LogLevel.WARN.value(), null);
        StyleConstants.setForeground(warn, new Color(166, 111, 0));

        Style error = context.addStyle(LogLevel.ERROR.value(), null);
        StyleConstants.setForeground(error, new Color(205, 0, 0));

        out = getStyledPrintStream(info);
        err = getStyledPrintStream(error);
    }

    public Style getStyle(LogLevel level) {
        return context.getStyle(level.value());
    }

    public void setStyle(LogLevel level, Style style) {
        context.addStyle(level.value(), style);
    }

    @Override
    public void log(LogLevel level, String msg) {
        log(msg, context.getStyle(level.value()));
    }

    @Override
    public void log(String msg) {
        log(msg, context.getStyle(StyleContext.DEFAULT_STYLE));
    }

    private void log(String msg, Style style) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (textPane.getStyledDocument().getDefaultRootElement().getElementCount() > MAX_LINE_COUNT)
                    textPane.setText("...truncating console output after " + MAX_LINE_COUNT + " log messages...\n");

                if (textPane.getCaretPosition() != doc.getLength())
                    textPane.setCaretPosition(doc.getLength());

                doc.insertString(doc.getLength(), new String(msg.getBytes(), encoding) + "\n", style);
            } catch (Throwable e) {
                //
            }
        });
    }

    @Override
    public PrintStream out() {
        return out;
    }

    @Override
    public PrintStream err() {
        return err;
    }

    private PrintStream getStyledPrintStream(Style style) {
        ConsoleOutputStream outputStream = new ConsoleOutputStream(textPane, encoding, style, MAX_LINE_COUNT);

        PrintStream printStream;
        try {
            printStream = new PrintStream(outputStream, true, encoding.displayName()) {};
        } catch (UnsupportedEncodingException e) {
            printStream = new PrintStream(outputStream, true);
        }

        return printStream;
    }
}
