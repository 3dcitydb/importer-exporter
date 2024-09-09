/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.config.gui.visExporter;

import org.citydb.config.gui.components.SQLExportFilterComponent;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VisExportGuiType", propOrder = {
        "collapseFeatureVersionFilter",
        "collapseTilingFilter",
        "collapseAttributeFilter",
        "collapseSQLFilter",
        "collapseBoundingBoxFilter",
        "collapseFeatureTypeFilter",
        "showVisExportUnsupportedADEWarning",
        "sqlFilter"
})
public class VisExportGuiConfig {
    private boolean collapseFeatureVersionFilter = false;
    private boolean collapseTilingFilter = true;
    private boolean collapseAttributeFilter = true;
    private boolean collapseSQLFilter = true;
    private boolean collapseBoundingBoxFilter = true;
    private boolean collapseFeatureTypeFilter = true;
    private boolean showVisExportUnsupportedADEWarning = true;
    private SQLExportFilterComponent sqlFilter;

    public VisExportGuiConfig() {
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

    public boolean isShowVisExportUnsupportedADEWarning() {
        return showVisExportUnsupportedADEWarning;
    }

    public void setShowVisExportUnsupportedADEWarning(boolean showVisExportUnsupportedADEWarning) {
        this.showVisExportUnsupportedADEWarning = showVisExportUnsupportedADEWarning;
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
