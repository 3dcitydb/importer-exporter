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

package org.citydb.gui.operation.common.filter;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.citydb.config.gui.components.SQLExportFilterComponent;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.gui.components.popup.PopupMenuDecorator;
import org.citydb.gui.util.GuiUtil;
import org.citydb.gui.util.RSyntaxTextAreaHelper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class SQLFilterView extends FilterView<SelectOperator> {
    private final Supplier<SQLExportFilterComponent> sqlFilterComponentSupplier;

    private JPanel component;
    private RSyntaxTextArea sqlText;
    private RTextScrollPane scrollPane;
    private JButton addButton;
    private JButton removeButton;

    private int additionalRows;
    private int rowHeight;

    public SQLFilterView(Supplier<SQLExportFilterComponent> sqlFilterComponentSupplier) {
        this.sqlFilterComponentSupplier = sqlFilterComponentSupplier;
        init();
    }

    private void init() {
        component = new JPanel();
        component.setLayout(new GridBagLayout());

        addButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/add.svg"));
        removeButton = new JButton(new FlatSVGIcon("org/citydb/gui/icons/remove.svg"));

        sqlText = new RSyntaxTextArea("", 5, 1);
        sqlText.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        sqlText.setAutoIndentEnabled(true);
        sqlText.setHighlightCurrentLine(true);
        sqlText.setTabSize(2);
        rowHeight = sqlText.getPreferredSize().height / 5;
        scrollPane = new RTextScrollPane(sqlText) {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };

        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createEmptyBorder());
        toolBar.add(addButton);
        toolBar.add(removeButton);
        toolBar.setFloatable(false);
        toolBar.setOrientation(JToolBar.VERTICAL);

        component.add(scrollPane, GuiUtil.setConstraints(0, 0, 1, 1, GridBagConstraints.BOTH, 0, 0, 0, 0));
        component.add(toolBar, GuiUtil.setConstraints(1, 0, 0, 0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 5, 0, 0));

        addButton.addActionListener(e -> {
            Dimension size = scrollPane.getPreferredSize();
            size.height += rowHeight;
            additionalRows++;

            scrollPane.setPreferredSize(size);
            component.revalidate();

            if (!removeButton.isEnabled()) {
                removeButton.setEnabled(true);
            }
        });

        removeButton.addActionListener(e -> {
            if (additionalRows > 0) {
                Dimension size = scrollPane.getPreferredSize();
                size.height -= rowHeight;
                additionalRows--;

                scrollPane.setPreferredSize(size);
                component.revalidate();

                if (additionalRows == 0) {
                    removeButton.setEnabled(false);
                }
            }
        });

        RSyntaxTextAreaHelper.installDefaultTheme(sqlText);
        PopupMenuDecorator.getInstance().decorate(sqlText);
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
        return new FlatSVGIcon("org/citydb/gui/filter/sql.svg");
    }

    @Override
    public void doTranslation() {
        addButton.setToolTipText(Language.I18N.getString("filter.label.sql.increase"));
        removeButton.setToolTipText(Language.I18N.getString("filter.label.sql.decrease"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        scrollPane.getHorizontalScrollBar().setEnabled(enabled);
        scrollPane.getVerticalScrollBar().setEnabled(enabled);
        scrollPane.getViewport().getView().setEnabled(enabled);

        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled && additionalRows > 0);
    }

    @Override
    public void loadSettings(SelectOperator selectOperator) {
        additionalRows = sqlFilterComponentSupplier.get().getAdditionalRows();

        SwingUtilities.invokeLater(() -> {
            Dimension size = scrollPane.getPreferredSize();
            sqlText.setText(selectOperator.getValue());

            if (additionalRows > 0) {
                size.height += additionalRows * rowHeight;
            } else {
                additionalRows = 0;
            }

            scrollPane.setPreferredSize(size);
            component.revalidate();

            removeButton.setEnabled(addButton.isEnabled() && additionalRows > 0);
        });
    }

    @Override
    public SelectOperator toSettings() {
        SelectOperator selectOperator = new SelectOperator();
        String value = sqlText.getText().trim();
        if (!value.isEmpty()) {
            selectOperator.setValue(value.replaceAll(";", " "));
        }

        sqlFilterComponentSupplier.get().setAdditionalRows(additionalRows);
        return selectOperator;
    }
}
