/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.config.project.query.filter.tiling;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.common.BoundingBoxFilter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "AbstractTilingType", propOrder = {
        "extent",
        "rows",
        "columns"
})
public abstract class AbstractTiling implements BoundingBoxFilter {
    @XmlElement(required = true)
    private BoundingBox extent;
    @XmlElement(required = true, defaultValue = "1")
    private int rows = 1;
    @XmlElement(required = true, defaultValue = "1")
    private int columns = 1;

    public AbstractTiling() {
        extent = new BoundingBox();
    }

    public abstract AbstractTilingOptions getTilingOptions();
    public abstract boolean isSetTilingOptions();

    @Override
    public boolean isSetExtent() {
        return extent != null;
    }

    @Override
    public BoundingBox getExtent() {
        return extent;
    }

    @Override
    public void setExtent(BoundingBox extent) {
        this.extent = extent;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

}
