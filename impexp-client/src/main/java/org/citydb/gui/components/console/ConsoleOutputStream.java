package org.citydb.gui.components.console;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.nio.charset.Charset;

public class ConsoleOutputStream extends FilterOutputStream {
    private final JTextPane textPane;
    private final Charset encoding;
    private final StyledDocument doc;
    private final Style style;
    private final int maxLineCount;

    ConsoleOutputStream(JTextPane textPane, Charset encoding, Style style, int maxLineCount) {
        super(new ByteArrayOutputStream());
        this.textPane = textPane;
        this.encoding = encoding;
        this.style = style;
        this.maxLineCount = maxLineCount;

        doc = textPane.getStyledDocument();
    }

    @Override
    public void write(final byte[] b) {
        write(new String(b, encoding), style);
    }

    @Override
    public void write(final byte b[], final int off, final int len) {
        write(new String(b, off, len, encoding), style);
    }

    private void write(String msg, Style style) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (textPane.getStyledDocument().getDefaultRootElement().getElementCount() > maxLineCount)
                    textPane.setText("...truncating console output after " + maxLineCount + " log messages...\n");

                if (textPane.getCaretPosition() != doc.getLength())
                    textPane.setCaretPosition(doc.getLength());

                doc.insertString(doc.getLength(), msg, style);
            } catch (Throwable e) {
                //
            }
        });
    }

}
