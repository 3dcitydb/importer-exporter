package org.citydb.config.gui.kmlExporter;

import org.citydb.config.gui.components.SQLExportFilterComponent;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "KmlExportGuiType", propOrder = {
        "collapseFeatureVersionFilter",
        "collapseTilingFilter",
        "collapseAttributeFilter",
        "collapseSQLFilter",
        "collapseBoundingBoxFilter",
        "collapseFeatureTypeFilter",
        "showKmlExportUnsupportedADEWarning",
        "sqlFilter"
})
public class KmlExportGuiConfig {
    private boolean collapseFeatureVersionFilter = false;
    private boolean collapseTilingFilter = true;
    private boolean collapseAttributeFilter = true;
    private boolean collapseSQLFilter = true;
    private boolean collapseBoundingBoxFilter = true;
    private boolean collapseFeatureTypeFilter = true;
    private boolean showKmlExportUnsupportedADEWarning = true;
    private SQLExportFilterComponent sqlFilter;

    public KmlExportGuiConfig() {
        sqlFilter = new SQLExportFilterComponent();
    }

    public boolean isCollapseFeatureVersionFilter() {
        return collapseFeatureVersionFilter;
    }

    public void setCollapseFeatureVersionFilter(boolean collapseFeatureVersionFilter) {
        this.collapseFeatureVersionFilter = collapseFeatureVersionFilter;
    }

    public boolean isCollapseTilingFilter() {
        return collapseTilingFilter;
    }

    public void setCollapseTilingFilter(boolean collapseTilingFilter) {
        this.collapseTilingFilter = collapseTilingFilter;
    }

    public boolean isCollapseAttributeFilter() {
        return collapseAttributeFilter;
    }

    public void setCollapseAttributeFilter(boolean collapseAttributeFilter) {
        this.collapseAttributeFilter = collapseAttributeFilter;
    }

    public boolean isCollapseSQLFilter() {
        return collapseSQLFilter;
    }

    public void setCollapseSQLFilter(boolean collapseSQLFilter) {
        this.collapseSQLFilter = collapseSQLFilter;
    }

    public boolean isCollapseBoundingBoxFilter() {
        return collapseBoundingBoxFilter;
    }

    public void setCollapseBoundingBoxFilter(boolean collapseBoundingBoxFilter) {
        this.collapseBoundingBoxFilter = collapseBoundingBoxFilter;
    }

    public boolean isCollapseFeatureTypeFilter() {
        return collapseFeatureTypeFilter;
    }

    public void setCollapseFeatureTypeFilter(boolean collapseFeatureTypeFilter) {
        this.collapseFeatureTypeFilter = collapseFeatureTypeFilter;
    }

    public boolean isShowKmlExportUnsupportedADEWarning() {
        return showKmlExportUnsupportedADEWarning;
    }

    public void setShowKmlExportUnsupportedADEWarning(boolean showKmlExportUnsupportedADEWarning) {
        this.showKmlExportUnsupportedADEWarning = showKmlExportUnsupportedADEWarning;
    }

    public SQLExportFilterComponent getSQLExportFilterComponent() {
        return sqlFilter;
    }

    public void setSQLExportFilterComponent(SQLExportFilterComponent sqlExportFilter) {
        if (sqlExportFilter != null) {
            this.sqlFilter = sqlExportFilter;
        }
    }
}
