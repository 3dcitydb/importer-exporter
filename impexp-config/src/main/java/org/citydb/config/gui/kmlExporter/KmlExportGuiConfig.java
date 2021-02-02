package org.citydb.config.gui.kmlExporter;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "KmlExportGuiType", propOrder = {
        "collapseTilingFilter",
        "collapseAttributeFilter",
        "collapseBoundingBoxFilter",
        "collapseFeatureTypeFilter",
        "showKmlExportUnsupportedADEWarning"
})
public class KmlExportGuiConfig {
    private boolean collapseTilingFilter = true;
    private boolean collapseAttributeFilter = true;
    private boolean collapseBoundingBoxFilter = true;
    private boolean collapseFeatureTypeFilter = true;
    private boolean showKmlExportUnsupportedADEWarning = true;

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
}
