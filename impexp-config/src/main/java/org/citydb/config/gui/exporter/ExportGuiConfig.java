package org.citydb.config.gui.exporter;

import org.citydb.config.gui.components.SQLExportFilterComponent;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ExportGuiType", propOrder = {
        "collapseFeatureVersionFilter",
        "collapseAttributeFilter",
        "collapseSQLFilter",
        "collapseLodFilter",
        "collapseCounterFilter",
        "collapseBoundingBoxFilter",
        "collapseFeatureTypeFilter",
        "showCityJSONTilingWarning",
        "sqlFilter"
})
public class ExportGuiConfig {
    private boolean collapseFeatureVersionFilter = true;
    private boolean collapseAttributeFilter = true;
    private boolean collapseSQLFilter = true;
    private boolean collapseLodFilter = true;
    private boolean collapseCounterFilter = true;
    private boolean collapseBoundingBoxFilter = true;
    private boolean collapseFeatureTypeFilter = true;
    private boolean showCityJSONTilingWarning = true;
    private SQLExportFilterComponent sqlFilter;

    public ExportGuiConfig() {
        sqlFilter = new SQLExportFilterComponent();
    }

    public boolean isCollapseFeatureVersionFilter() {
        return collapseFeatureVersionFilter;
    }

    public void setCollapseFeatureVersionFilter(boolean collapseFeatureVersionFilter) {
        this.collapseFeatureVersionFilter = collapseFeatureVersionFilter;
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

    public boolean isCollapseLodFilter() {
        return collapseLodFilter;
    }

    public void setCollapseLodFilter(boolean collapseLodFilter) {
        this.collapseLodFilter = collapseLodFilter;
    }

    public boolean isCollapseCounterFilter() {
        return collapseCounterFilter;
    }

    public void setCollapseCounterFilter(boolean collapseCounterFilter) {
        this.collapseCounterFilter = collapseCounterFilter;
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

    public boolean isShowCityJSONTilingWarning() {
        return showCityJSONTilingWarning;
    }

    public void setShowCityJSONTilingWarning(boolean showCityJSONTilingWarning) {
        this.showCityJSONTilingWarning = showCityJSONTilingWarning;
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
