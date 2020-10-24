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

package org.citydb.modules.citygml.exporter.gui.view.filter;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.gui.factory.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;

public class SQLFilterView extends FilterView {
    private JPanel component;
    private RSyntaxTextArea sqlText;
    private RTextScrollPane scrollPane;
    private JButton addButton;
    private JButton removeButton;

    private int additionalRows;
    private int rowHeight;

    public SQLFilterView(Config config) {
        super(config);
        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        addButton = new JButton();
        ImageIcon add = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/add.png"));
        addButton.setIcon(add);
        addButton.setMargin(new Insets(0, 0, 0, 0));

        removeButton = new JButton();
        ImageIcon remove = new ImageIcon(getClass().getResource("/org/citydb/gui/images/common/remove.png"));
        removeButton.setIcon(remove);
        removeButton.setMargin(new Insets(0, 0, 0, 0));

        sqlText = new RSyntaxTextArea("", 5, 1);
        try (InputStream in = getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml")) {
            Theme.load(in).apply(sqlText);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize SQL editor.", e);
        }

        sqlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        sqlText.setAutoIndentEnabled(true);
        sqlText.setHighlightCurrentLine(true);
        sqlText.setTabSize(2);
        rowHeight = sqlText.getFont().getSize() + 5;
        scrollPane = new RTextScrollPane(sqlText);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        buttons.add(addButton, GuiUtil.setConstraints(0,0,0,0,GridBagConstraints.NONE,0,5,5,5));
        buttons.add(removeButton, GuiUtil.setConstraints(0,1,0,0,GridBagConstraints.NONE,0,5,0,5));

        component.add(scrollPane, GuiUtil.setConstraints(0,0,1,1,GridBagConstraints.BOTH,10,5,10,0));
        component.add(buttons, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NORTH,GridBagConstraints.NONE,10,0,10,0));

        addButton.addActionListener(e -> {
            Dimension size = scrollPane.getPreferredSize();
            size.height += rowHeight;
            additionalRows++;

            scrollPane.setPreferredSize(size);
            component.revalidate();
            component.repaint();

            if (!removeButton.isEnabled())
                removeButton.setEnabled(true);
        });

        removeButton.addActionListener(e -> {
            if (additionalRows > 0) {
                Dimension size = scrollPane.getPreferredSize();
                size.height -= rowHeight;
                additionalRows--;

                scrollPane.setPreferredSize(size);
                component.revalidate();
                component.repaint();

                if (additionalRows == 0)
                    removeButton.setEnabled(false);
            }
        });

        PopupMenuDecorator.getInstance().decorate(sqlText);
    }

    @Override
    public void doTranslation() {
        addButton.setToolTipText(Language.I18N.getString("filter.label.sql.increase"));
        removeButton.setToolTipText(Language.I18N.getString("filter.label.sql.decrease"));
    }

    @Override
    public void setEnabled(boolean enable) {
        scrollPane.getHorizontalScrollBar().setEnabled(enable);
        scrollPane.getVerticalScrollBar().setEnabled(enable);
        scrollPane.getViewport().getView().setEnabled(enable);

        addButton.setEnabled(enable);
        removeButton.setEnabled(enable && additionalRows > 0);
    }

    @Override
    public String getLocalizedTitle() {
        return Language.I18N.getString("filter.border.sql");
    }

    @Override
    public Component getViewComponent() {
        return component;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void loadSettings() {
        SimpleQuery query = config.getExportConfig().getSimpleQuery();

        SelectOperator sql = query.getSelectionFilter().getSQLFilter();
        sqlText.setText(sql.getValue());

        additionalRows = config.getGui().getSQLExportFilterComponent().getAdditionalRows();
        SwingUtilities.invokeLater(() -> {
            if (additionalRows > 0) {
                Dimension size = scrollPane.getPreferredSize();
                size.height += additionalRows * rowHeight;

                scrollPane.setPreferredSize(size);
                component.revalidate();
                component.repaint();
            } else
                additionalRows = 0;

            removeButton.setEnabled(additionalRows > 0);
        });
    }

    @Override
    public void setSettings() {
        SimpleQuery query = config.getExportConfig().getSimpleQuery();

        SelectOperator sql = query.getSelectionFilter().getSQLFilter();
        sql.reset();
        String value = sqlText.getText().trim();
        if (!value.isEmpty())
            sql.setValue(value.replaceAll(";", " "));

        config.getGui().getSQLExportFilterComponent().setAdditionalRows(additionalRows);
    }
}
