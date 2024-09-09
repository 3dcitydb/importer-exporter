/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.gui.components.popup;

import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.TileTokenValue;

import javax.swing.*;

public class AddTokenMenu extends JPopupMenu {
    private JTextField textField;

    public static AddTokenMenu newInstance() {
        return new AddTokenMenu();
    }

    private AddTokenMenu() {
        JMenuItem row = new JMenuItem(Language.I18N.getString("pref.export.tiling.label.row"));
        JMenuItem column = new JMenuItem(Language.I18N.getString("pref.export.tiling.label.column"));
        JMenuItem xmin = new JMenuItem("<html>x<sub>min</sub></html>");
        JMenuItem ymin = new JMenuItem("<html>y<sub>min</sub></html>");
        JMenuItem xmax = new JMenuItem("<html>x<sub>max</sub></html>");
        JMenuItem ymax = new JMenuItem("<html>y<sub>max</sub></html>");

        add(row);
        add(column);
        addSeparator();
        add(xmin);
        add(ymin);
        add(xmax);
        add(ymax);

        row.addActionListener(e -> addToken(TileTokenValue.ROW_TOKEN));
        column.addActionListener(e -> addToken(TileTokenValue.COLUMN_TOKEN));
        xmin.addActionListener(e -> addToken(TileTokenValue.X_MIN_TOKEN));
        ymin.addActionListener(e -> addToken(TileTokenValue.Y_MIN_TOKEN));
        xmax.addActionListener(e -> addToken(TileTokenValue.X_MAX_TOKEN));
        ymax.addActionListener(e -> addToken(TileTokenValue.Y_MAX_TOKEN));
    }

    public AddTokenMenu withTarget(JTextField textField) {
        this.textField = textField;
        return this;
    }

    private void addToken(String token) {
        if (textField != null) {
            String text = textField.getText();
            int dot = textField.getCaretPosition();
            int start = textField.getSelectionStart();
            int end = textField.getSelectionEnd();

            if (start != dot || end != dot) {
                text = text.substring(0, start) + text.substring(end);
                dot = start;
            }

            textField.setText(text.substring(0, dot) + token + text.substring(dot));
            textField.setCaretPosition(dot + token.length());
        }
    }
}
