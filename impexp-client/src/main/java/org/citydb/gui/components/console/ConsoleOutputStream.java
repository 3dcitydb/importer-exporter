package org.citydb.gui.components.console;

import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.nio.charset.Charset;

public class ConsoleOutputStream extends FilterOutputStream {
    private final ConsoleTextPane textPane;
    private final Charset encoding;
    private final StyledDocument doc;
    private final Style style;

    ConsoleOutputStream(ConsoleTextPane textPane, Charset encoding, Style style) {
        super(new ByteArrayOutputStream());
        this.textPane = textPane;
        this.encoding = encoding;
        this.style = style;

        doc = textPane.getStyledDocument();

        DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    @Override
    public void write(final byte[] b) {
        try {
            doc.insertString(doc.getLength(), new String(b, encoding), style);
            checkLineCount();
        } catch (Throwable e) {
            //
        }
    }

    @Override
    public void write(final byte b[], final int off, final int len) {
        try {
            doc.insertString(doc.getLength(), new String(b, off, len, encoding), style);
            checkLineCount();
        } catch (Throwable e) {
            //
        }
    }

    private void checkLineCount() {
        if (textPane.getStyledDocument().getDefaultRootElement().getElementCount() > textPane.getMaxLineCount())
            textPane.setText("...truncating console output after " + textPane.getMaxLineCount() + " log messages...\n");
    }
}
