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
package org.citydb.config.project.query.filter.tiling;

import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.config.project.kmlExporter.KmlTilingOptions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "TilingType", propOrder = {
        "tilingOptions"
})
public class Tiling extends AbstractTiling {
    @XmlElements({
            @XmlElement(name = "tilingOptions", type = SimpleTilingOptions.class),
            @XmlElement(name = "kmlTilingOptions", type = KmlTilingOptions.class)
    })
    private AbstractTilingOptions tilingOptions;

    @Override
    public AbstractTilingOptions getTilingOptions() {
        return tilingOptions;
    }

    @Override
    public boolean isSetTilingOptions() {
        return tilingOptions != null;
    }

    public void setTilingOptions(AbstractTilingOptions tilingOptions) {
        this.tilingOptions = tilingOptions;
    }

}
