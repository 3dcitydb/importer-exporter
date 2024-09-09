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

@XmlRootElement(name = "envelope")
@XmlType(name = "BoundingBoxType", propOrder = {
        "lowerCorner",
        "upperCorner"
})
public class BoundingBox extends AbstractGeometry {
    @XmlElement(required = true)
    private Position lowerCorner;
    @XmlElement(required = true)
    private Position upperCorner;

    public BoundingBox() {
        lowerCorner = new Position();
        upperCorner = new Position();
    }

    public BoundingBox(Position lowerCorner, Position upperCorner) {
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
    }

    public BoundingBox(Position lowerCorner, Position upperCorner, DatabaseSrs srs) {
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
        setSrs(srs);
    }

    public BoundingBox(BoundingBox other) {
        copyFrom(other);
    }

    public Position getLowerCorner() {
        return lowerCorner;
    }

    public void setLowerCorner(Position lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public Position getUpperCorner() {
        return upperCorner;
    }

    public void setUpperCorner(Position upperCorner) {
        this.upperCorner = upperCorner;
    }

    public void update(Position lowerCorner, Position upperCorner) {
        if (lowerCorner.getX() < this.lowerCorner.getX())
            this.lowerCorner.setX(lowerCorner.getX());

        if (lowerCorner.getY() < this.lowerCorner.getY())
            this.lowerCorner.setY(lowerCorner.getY());

        if (upperCorner.getX() > this.upperCorner.getX())
            this.upperCorner.setX(upperCorner.getX());

        if (upperCorner.getY() > this.upperCorner.getY())
            this.upperCorner.setY(upperCorner.getY());

        if (is3D()) {
            if (lowerCorner.isSetZ() && lowerCorner.getZ() < this.lowerCorner.getZ())
                this.lowerCorner.setZ(lowerCorner.getZ());

            if (upperCorner.isSetZ() && upperCorner.getZ() > this.upperCorner.getZ())
                this.upperCorner.setZ(upperCorner.getZ());
        }
    }

    public void update(BoundingBox other) {
        update(other.lowerCorner, other.upperCorner);
    }

    public void copyFrom(BoundingBox other) {
        setSrs(other.getSrs());

        if (!other.is3D()) {
            lowerCorner = new Position(other.getLowerCorner().getX(), other.getLowerCorner().getY());
            upperCorner = new Position(other.getUpperCorner().getX(), other.getUpperCorner().getY());
        } else {
            lowerCorner = new Position(other.getLowerCorner().getX(), other.getLowerCorner().getY(), other.getLowerCorner().getZ());
            upperCorner = new Position(other.getUpperCorner().getX(), other.getUpperCorner().getY(), other.getUpperCorner().getZ());
        }
    }

    @Override
    public BoundingBox toBoundingBox() {
        return this;
    }

    @Override
    public boolean is3D() {
        return isValid() && lowerCorner.is3D() && upperCorner.is3D();
    }

    @Override
    public boolean isValid() {
        return lowerCorner != null && lowerCorner.isValid()
                && upperCorner != null && upperCorner.isValid();
    }

    @Override
    public GeometryType getGeometryType() {
        return GeometryType.ENVELOPE;
    }

}
