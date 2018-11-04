package org.citydb.modules.citygml.exporter.gui.view.filter;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.exporter.SimpleQuery;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.gui.util.GuiUtil;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class SQLFilterView extends FilterView {
    private JPanel component;
    private JTextArea sqlText;
    private JScrollPane scrollPane;
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

        addButton = new JButton("+");
        removeButton = new JButton("-");
        removeButton.setPreferredSize(addButton.getPreferredSize());

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        buttons.add(addButton, GuiUtil.setConstraints(0,0,1,0,GridBagConstraints.HORIZONTAL,0,0,5,0));
        buttons.add(removeButton, GuiUtil.setConstraints(0,1,1,0,GridBagConstraints.HORIZONTAL,0,0,0,0));

        sqlText = new JTextArea("", 5, 1);
        sqlText.setTabSize(2);
        rowHeight = sqlText.getFont().getSize() + 5;

        scrollPane = new JScrollPane(sqlText);

        component.add(scrollPane, GuiUtil.setConstraints(0,0,1,1,GridBagConstraints.BOTH,10,5,10,5));
        component.add(buttons, GuiUtil.setConstraints(1,0,0,0,GridBagConstraints.NORTH,GridBagConstraints.NONE,10,5,10,5));

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
    }

    @Override
    public void doTranslation() {

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
        SimpleQuery query = config.getProject().getExporter().getSimpleQuery();

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
        SimpleQuery query = config.getProject().getExporter().getSimpleQuery();

        SelectOperator sql = query.getSelectionFilter().getSQLFilter();
        sql.reset();
        if (!sqlText.getText().trim().isEmpty()) {
            String value = sqlText.getText().trim().replaceAll(";", " ");
            sql.setValue(value);
        }

        config.getGui().getSQLExportFilterComponent().setAdditionalRows(additionalRows);
    }
}
