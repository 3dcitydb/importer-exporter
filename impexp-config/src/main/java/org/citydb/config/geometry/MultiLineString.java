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
package org.citydb.config.geometry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "multiLineString")
@XmlType(name = "MultiLineStringType", propOrder = {
        "lineStrings"
})
public class MultiLineString extends AbstractGeometry {
    @XmlElement(name = "lineString", required = true)
    private List<LineString> lineStrings;

    public MultiLineString() {
        lineStrings = new ArrayList<>();
    }

    public List<LineString> getLineStrings() {
        return lineStrings;
    }

    public void setLineStrings(List<LineString> lineStrings) {
        this.lineStrings = lineStrings;
    }

    @Override
    public BoundingBox toBoundingBox() {
        int dim = is3D() ? 3 : 2;
        BoundingBox bbox = new BoundingBox(new Position(Double.MAX_VALUE, dim), new Position(-Double.MAX_VALUE, dim));

        for (LineString lineString : lineStrings)
            bbox.update(lineString.toBoundingBox());

        return bbox;
    }

    @Override
    public boolean is3D() {
        if (isValid()) {
            for (LineString lineString : lineStrings) {
                if (!lineString.is3D())
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isValid() {
        if (lineStrings != null && !lineStrings.isEmpty()) {
            for (LineString lineString : lineStrings) {
                if (!lineString.isValid())
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_LINE_STRING;
    }

}
