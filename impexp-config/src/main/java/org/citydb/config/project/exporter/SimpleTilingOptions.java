/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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

package org.citydb.config.project.exporter;

import org.citydb.config.project.query.filter.tiling.AbstractTilingOptions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CityGMLTilingOptionsType", propOrder = {
        "tilePath",
        "tilePathSuffix",
        "tileNameSuffix",
        "includeTileAsGenericAttribute",
        "genericAttributeValue"
})
public class SimpleTilingOptions extends AbstractTilingOptions {
    private String tilePath = "tile";
    private TileSuffixMode tilePathSuffix = TileSuffixMode.ROW_COLUMN;
    private TileNameSuffixMode tileNameSuffix = TileNameSuffixMode.NONE;
    @XmlElement(defaultValue = "false")
    private Boolean includeTileAsGenericAttribute = false;
    private TileSuffixMode genericAttributeValue = TileSuffixMode.XMIN_YMIN_XMAX_YMAX;

    public String getTilePath() {
        return tilePath;
    }

    public void setTilePath(String tilePath) {
        this.tilePath = tilePath;
    }

    public TileSuffixMode getTilePathSuffix() {
        return tilePathSuffix;
    }

    public void setTilePathSuffix(TileSuffixMode tilePathSuffix) {
        this.tilePathSuffix = tilePathSuffix;
    }

    public TileNameSuffixMode getTileNameSuffix() {
        return tileNameSuffix;
    }

    public void setTileNameSuffix(TileNameSuffixMode tileNameSuffix) {
        this.tileNameSuffix = tileNameSuffix;
    }

    public Boolean getIncludeTileAsGenericAttribute() {
        return includeTileAsGenericAttribute;
    }

    public boolean isIncludeTileAsGenericAttribute() {
        return includeTileAsGenericAttribute != null && includeTileAsGenericAttribute;
    }

    public void setIncludeTileAsGenericAttribute(Boolean includeTileAsGenericAttribute) {
        this.includeTileAsGenericAttribute = includeTileAsGenericAttribute;
    }

    public TileSuffixMode getGenericAttributeValue() {
        return genericAttributeValue;
    }

    public void setGenericAttributeValue(TileSuffixMode genericAttributeValue) {
        this.genericAttributeValue = genericAttributeValue;
    }

}
