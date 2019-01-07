/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.gui.components.console;

import org.citydb.config.gui.style.LogLevelStyle;
import org.citydb.config.project.global.LogLevel;
import org.citydb.gui.util.GuiUtil;
import org.citydb.log.ConsoleLogger;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
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

        // add default styles for each log level
        for (LogLevel level : LogLevel.values())
            context.addStyle(level.value(), null);

        out = getStyledPrintStream(context.getStyle(LogLevel.INFO.value()));
        err = getStyledPrintStream(context.getStyle(LogLevel.ERROR.value()));
    }

    public Style getStyle(LogLevel level) {
        return context.getStyle(level.value());
    }

    public void setStyle(LogLevel level, Style style) {
        context.addStyle(level.value(), style);
    }

    public void applyLogLevelStyle(LogLevel level, LogLevelStyle logLevelStyle) {
        Style style = context.getStyle(level.value());

        Color foreground = GuiUtil.hexToColor(logLevelStyle.getForeground());
        if (foreground != null)
            StyleConstants.setForeground(style, foreground);
        else
            style.removeAttribute(StyleConstants.Foreground);

        Color background = GuiUtil.hexToColor(logLevelStyle.getBackground());
        if (background != null)
            StyleConstants.setBackground(style, background);
        else
            style.removeAttribute(StyleConstants.Background);
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

                doc.insertString(doc.getLength(), msg + "\n", style);
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
