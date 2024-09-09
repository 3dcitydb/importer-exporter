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

import org.citydb.config.project.database.DatabaseSrs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "point")
@XmlType(name = "PointType", propOrder = {
        "pos"
})
public class Point extends AbstractGeometry {
    @XmlElement(name = "pos", required = true)
    private Position pos;

    public Point() {
        pos = new Position();
    }

    public Point(Position pos, DatabaseSrs srs) {
        this.pos = pos;
        setSrs(srs);
    }

    public Point(Double x, Double y, DatabaseSrs srs) {
        this(new Position(x, y), srs);
    }

    public Point(Double x, Double y, Double z, DatabaseSrs srs) {
        this(new Position(x, y, z), srs);
    }

    public Point(Position pos) {
        this(pos, null);
    }

    public Point(Double x, Double y, Double z) {
        this(new Position(x, y, z), null);
    }

    public Point(Double x, Double y) {
        this(new Position(x, y), null);
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public Double getX() {
        return pos.getX();
    }

    public boolean isSetX() {
        return pos.isSetX();
    }

    public void setX(Double x) {
        pos.setX(x);
    }

    public Double getY() {
        return pos.getY();
    }

    public boolean isSetY() {
        return pos.isSetY();
    }

    public void setY(Double y) {
        pos.setY(y);
    }

    public Double getZ() {
        return pos.getZ();
    }

    public boolean isSetZ() {
        return pos.isSetZ();
    }

    public void setZ(Double z) {
        pos.setZ(z);
    }

    @Override
    public BoundingBox toBoundingBox() {
        return new BoundingBox(pos, pos);
    }

    @Override
    public boolean is3D() {
        return isValid() && pos.is3D();
    }

    @Override
    public boolean isValid() {
        return pos.isValid();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.POINT;
    }

}
