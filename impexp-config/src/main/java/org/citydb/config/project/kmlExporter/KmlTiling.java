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

package org.citydb.config.project.kmlExporter;

import org.citydb.config.project.query.filter.tiling.AbstractTiling;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="KmlTilingType", propOrder={
        "tilingOptions"
})
public class KmlTiling extends AbstractTiling {
    @XmlAttribute(required = true)
    private KmlTilingMode mode = KmlTilingMode.NO_TILING;
    private KmlTilingOptions tilingOptions;

    public KmlTiling() {
        tilingOptions = new KmlTilingOptions();
    }

    public KmlTilingMode getMode() {
        return mode;
    }

    public void setMode(KmlTilingMode mode) {
        this.mode = mode;
    }

    @Override
    public KmlTilingOptions getTilingOptions() {
        return tilingOptions;
    }

    @Override
    public boolean isSetTilingOptions() {
        return tilingOptions != null;
    }

    public void setTilingOptions(KmlTilingOptions tilingOptions) {
        this.tilingOptions = tilingOptions;
    }

}
