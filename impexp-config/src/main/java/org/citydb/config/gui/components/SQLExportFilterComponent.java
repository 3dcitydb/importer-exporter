package org.citydb.config.gui.components;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="SQLExportFilterComponentType", propOrder={
        "additionalRows"
})
public class SQLExportFilterComponent {
    private int additionalRows;

    public int getAdditionalRows() {
        return additionalRows;
    }

    public void setAdditionalRows(Integer additionalRows) {
        if (additionalRows >= 0)
            this.additionalRows = additionalRows;
    }
}
