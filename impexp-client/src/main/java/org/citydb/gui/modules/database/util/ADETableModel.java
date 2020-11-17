/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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
package org.citydb.gui.modules.database.util;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ADETableModel extends AbstractTableModel {
    private final String[] columnNames = new String[]{"", "", "", ""};
    private final List<ADEInfoRow> data = new ArrayList<>();

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return ADEInfoRow.NO_ADES_ENTRY.getValueAt(columnIndex).getClass();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    public ADEInfoRow getRow(int rowIndex) {
        return rowIndex >= 0 && rowIndex < data.size() ? data.get(rowIndex) : ADEInfoRow.NO_ADES_ENTRY;
    }

    public void addRow(ADEInfoRow data) {
        int row = getRowCount();
        this.data.add(row, data);
        fireTableRowsInserted(row, row);
    }

    public boolean hasRows() {
        return !data.isEmpty();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).getValueAt(columnIndex);
    }

    public void reset() {
        int rowCount = getRowCount();
        data.clear();
        if (rowCount != 0) {
            fireTableRowsDeleted(0, rowCount - 1);
        }
    }
}
