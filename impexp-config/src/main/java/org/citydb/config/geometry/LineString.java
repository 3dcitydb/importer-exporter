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
package org.citydb.config.geometry;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "lineString")
@XmlType(name = "LineStringType", propOrder = {
        "posList"
})
public class LineString extends AbstractGeometry {
    @XmlElement(required = true)
    private PositionList posList;

    public LineString() {
        posList = new PositionList();
    }

    public PositionList getPosList() {
        return posList;
    }

    public void setPosList(PositionList posList) {
        this.posList = posList;
    }

    @Override
    public BoundingBox toBoundingBox() {
        int dim = is3D() ? 3 : 2;
        BoundingBox bbox = new BoundingBox(new Position(Double.MAX_VALUE, dim), new Position(-Double.MAX_VALUE, dim));

        List<Double> coords = posList.getCoords();
        for (int i = 0; i < coords.size(); i += dim) {
            if (coords.get(i) < bbox.getLowerCorner().getX())
                bbox.getLowerCorner().setX(coords.get(i));
            else if (coords.get(i) > bbox.getUpperCorner().getX())
                bbox.getUpperCorner().setX(coords.get(i));

            if (coords.get(i + 1) < bbox.getLowerCorner().getY())
                bbox.getLowerCorner().setY(coords.get(i + 1));
            else if (coords.get(i + 1) > bbox.getUpperCorner().getY())
                bbox.getUpperCorner().setY(coords.get(i + 1));

            if (dim == 3) {
                if (coords.get(i + 2) < bbox.getLowerCorner().getZ())
                    bbox.getLowerCorner().setZ(coords.get(i + 2));
                else if (coords.get(i + 2) > bbox.getUpperCorner().getZ())
                    bbox.getUpperCorner().setZ(coords.get(i + 2));
            }
        }

        return bbox;
    }

    @Override
    public boolean is3D() {
        return isValid() && posList.getDimension() == 3;
    }

    @Override
    public boolean isValid() {
        return posList != null && posList.isValid();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.LINE_STRING;
    }

}
