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
package org.citydb.gui.components.dialog;

import org.citydb.config.i18n.Language;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ReadMeDialog extends JDialog {

    public ReadMeDialog(JFrame frame) {
        super(frame, Language.I18N.getString("menu.help.readMe.label"), true);
        initGUI();
    }

    private void initGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setLayout(new GridBagLayout());
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        {
            JLabel readMeHeader = new JLabel(Language.I18N.getString("menu.help.readMe.information"));

            JTextArea readMe = new JTextArea();
            readMe.setEditable(false);
            readMe.setBackground(UIManager.getColor("TextField.background"));
            readMe.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize()));
            readMe.setColumns(80);
            readMe.setRows(20);

            JScrollPane scroll = new JScrollPane(readMe);
            scroll.setAutoscrolls(true);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("/META-INF/README.txt"), StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }

                readMe.setText(builder.toString());
            } catch (Exception e) {
                readMe.setText("The README.txt file could not be found.\n\n" +
                        "Please refer to the README.txt file provided with the installation package.");
            }

            readMe.setCaretPosition(0);

            main.add(readMeHeader, GuiUtil.setConstraints(0, 0, 1, 0, GridBagConstraints.HORIZONTAL, 0, 0, 0, 0));
            main.add(scroll, GuiUtil.setConstraints(0, 1, 1, 1, GridBagConstraints.BOTH, 5, 0, 0, 0));

            PopupMenuDecorator.getInstance().decorate(readMe);
        }

        JButton button = new JButton(Language.I18N.getString("common.button.ok"));

        add(main, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 10, 10, 0, 10));
        add(button, GuiUtil.setConstraints(0, 3, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 15, 10, 10, 10));

        pack();

        button.addActionListener(e -> dispose());
    }
}
