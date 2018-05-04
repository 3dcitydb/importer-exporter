package org.citydb.modules.database.gui.util;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ADETableCellRenderer implements TableCellRenderer {
    private final TableCellRenderer defaultRenderer;

    public ADETableCellRenderer(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setEnabled(table != null && table.isEnabled());
        return c;
    }
}
