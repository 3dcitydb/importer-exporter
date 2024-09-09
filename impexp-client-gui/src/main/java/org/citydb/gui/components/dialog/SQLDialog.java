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
package org.citydb.gui.components.dialog;

import org.citydb.config.gui.exporter.ExportGuiConfig;
import org.citydb.config.i18n.Language;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.RSyntaxTextAreaHelper;
import org.citydb.util.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SQLDialog extends JDialog {
    private final Logger log = Logger.getInstance();
    private final String sql;

    public SQLDialog(String sql, JFrame frame) {
        super(frame, Language.I18N.getString("common.dialog.sql.title"), true);
        this.sql = sql;

        initGUI();
    }

    private void initGUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setLayout(new GridBagLayout());
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        {
            RSyntaxTextArea sqlText = new RSyntaxTextArea(sql);
            sqlText.setEditable(false);
            sqlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
            sqlText.setColumns(80);
            sqlText.setRows(20);
            sqlText.setCaretPosition(0);
            sqlText.setHighlightCurrentLine(false);

            RTextScrollPane scrollPane = new RTextScrollPane(sqlText);
            scrollPane.setAutoscrolls(true);

            main.add(scrollPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));

            RSyntaxTextAreaHelper.installDefaultTheme(sqlText);
            PopupMenuDecorator.getInstance().decorate(sqlText);
        }

        JButton save = new JButton(Language.I18N.getString("common.dialog.sql.button.saveFile"));
        JButton ok = new JButton(Language.I18N.getString("common.button.ok"));
        Box buttonsPanel = Box.createHorizontalBox();
        buttonsPanel.add(save);
        buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonsPanel.add(ok);

        add(main, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 10, 10, 0, 10));
        add(buttonsPanel, GuiUtil.setConstraints(0, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, 15, 10, 10, 10));

        pack();

        save.addActionListener(e -> saveFile());
        ok.addActionListener(e -> dispose());
    }

    private void saveFile() {
        ExportGuiConfig config = ObjectRegistry.getInstance().getConfig().getGuiConfig().getExportGuiConfig();
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(Language.I18N.getString("common.dialog.sql.saveAs.label"));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("SQL Files (*.sql)", "sql");
        chooser.addChoosableFileFilter(filter);
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        String savePath = config.getSQLFile();
        if (savePath != null) {
            chooser.setCurrentDirectory(new File(savePath));
        }

        int result = chooser.showSaveDialog(getOwner());
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().contains(".")) {
            file = new File(file + ".sql");
        }

        config.setSQLFile(chooser.getCurrentDirectory().getAbsolutePath());
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(sql);
            log.info("SQL successfully saved to file: '" + file.getAbsolutePath() + "'.");
        } catch (IOException e) {
            log.error("Failed to write SQL file.", e);
        }
    }
}
