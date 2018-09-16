/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.citygml4j.model.gml.GML;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.module.gml.GMLCoreModule;

@XmlEnum
@XmlType(name = "geometryType")
public enum GeometryType {

	@XmlEnumValue("AbstractGeometry")
    ABSTRACT_GEOMETRY("AbstractGeometry", AbstractGeometry.class, "_Geometry"),
	@XmlEnumValue("Envelope")
    ENVELOPE("Envelope", Envelope.class, "Envelope"),
    @XmlEnumValue("Point")
    POINT("Point", Point.class, "Point"),
    @XmlEnumValue("AbstractSurface")
    ABSTRACT_SURFACE("AbstractSurface", AbstractSurface.class, "_Surface"),
    @XmlEnumValue("Polygon")
	POLYGON("Polygon", Polygon.class, "Polygon"),
	@XmlEnumValue("TriangulatedSurface")
	TRIANGULATED_SURFACE("TriangulatedSurface", TriangulatedSurface.class, "TriangulatedSurface"),
    @XmlEnumValue("AbstractSolid")
    ABSTRACT_SOLID("AbstractSolid", AbstractSolid.class, "_Solid"),
    @XmlEnumValue("GeometricComplex")
   	GEOMETRIC_COMPLEX("GeometricComplex", GeometricComplex.class, "GeometricComplex"),
   	@XmlEnumValue("MultiPoint")
	MULTI_POINT("MultiPoint", MultiPoint.class, "MultiPoint"),
	@XmlEnumValue("MultiCurve")
	MULTI_CURVE("MultiCurve", MultiCurve.class, "MultiCurve"),
	@XmlEnumValue("MultiSurface")
	MULTI_SURFACE("MultiSurface", MultiSurface.class, "MultiSurface"),
    @XmlEnumValue("MultiSolid")
	MULTI_SOLID("MultiSolid", MultiSolid.class, "MultiSolid");
    
    private final String value;
    private final Class<? extends GML> geometryClass;
    private final String elementName;

    GeometryType(String value, Class<? extends GML> geometryClass, String elementName) {
        this.value = value;
        this.geometryClass = geometryClass;
        this.elementName = elementName;
    }

    public String value() {
        return value;
    }

    public Class<? extends GML> getGeometryClass() {
		return geometryClass;
	}

	public QName getElementName() {
		return new QName(GMLCoreModule.v3_1_1.getNamespaceURI(), elementName);
	}

	public static GeometryType fromValue(String v) {
        for (GeometryType c: GeometryType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
