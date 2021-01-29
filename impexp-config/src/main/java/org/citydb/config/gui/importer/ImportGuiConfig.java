package org.citydb.config.gui.importer;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportGuiType", propOrder = {
        "collapseAttributeFilter",
        "collapseCounterFilter",
        "collapseBoundingBoxFilter",
        "collapseFeatureTypeFilter"
})
public class ImportGuiConfig {
    private boolean collapseAttributeFilter = true;
    private boolean collapseCounterFilter = true;
    private boolean collapseBoundingBoxFilter = true;
    private boolean collapseFeatureTypeFilter = true;

    public boolean isCollapseAttributeFilter() {
        return collapseAttributeFilter;
    }

    public void setCollapseAttributeFilter(boolean collapseAttributeFilter) {
        this.collapseAttributeFilter = collapseAttributeFilter;
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
}
