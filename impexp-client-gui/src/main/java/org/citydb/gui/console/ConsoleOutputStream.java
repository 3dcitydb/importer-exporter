/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.gui.console;

import javax.swing.*;
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
