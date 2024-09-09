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

@XmlRootElement(name = "multiPoint")
@XmlType(name = "MultiPointType", propOrder = {
        "points"
})
public class MultiPoint extends AbstractGeometry {
    @XmlElement(name = "point", required = true)
    private List<Point> points;

    public MultiPoint() {
        points = new ArrayList<>();
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @Override
    public BoundingBox toBoundingBox() {
        int dim = is3D() ? 3 : 2;
        BoundingBox bbox = new BoundingBox(new Position(Double.MAX_VALUE, dim), new Position(-Double.MAX_VALUE, dim));

        for (Point point : points)
            bbox.update(point.toBoundingBox());

        return bbox;
    }

    @Override
    public boolean is3D() {
        if (isValid()) {
            for (Point point : points) {
                if (!point.is3D())
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isValid() {
        if (points != null && !points.isEmpty()) {
            for (Point point : points) {
                if (!point.isValid())
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.MULTI_POINT;
    }

}
