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

package org.citydb.operation.exporter.util;

import org.citydb.operation.exporter.database.content.SurfaceGeometry;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygonProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolidProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;

public class DefaultGeometrySetterHandler implements GeometrySetterHandler {
    private final GeometrySetter<?> setter;

    public DefaultGeometrySetterHandler(GeometrySetter.AbstractGeometry setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.Surface setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.CompositeSurface setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.MultiSurface setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.Polygon setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.MultiPolygon setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.Solid setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.CompositeSolid setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.MultiSolid setter) {
        this.setter = setter;
    }

    public DefaultGeometrySetterHandler(GeometrySetter.Tin setter) {
        this.setter = setter;
    }

    @Override
    public void handle(SurfaceGeometry geometry) {
        if (this.setter instanceof GeometrySetter.MultiSurface) {
            GeometrySetter.MultiSurface setter = (GeometrySetter.MultiSurface) this.setter;
            setter.set(geometry.fill(new MultiSurfaceProperty()));
        } else if (this.setter instanceof GeometrySetter.Solid) {
            GeometrySetter.Solid setter = (GeometrySetter.Solid) this.setter;
            setter.set(geometry.fill(new SolidProperty()));
        } else if (this.setter instanceof GeometrySetter.Surface) {
            GeometrySetter.Surface setter = (GeometrySetter.Surface) this.setter;
            setter.set(geometry.fill(new SurfaceProperty()));
        } else if (this.setter instanceof GeometrySetter.CompositeSurface) {
            GeometrySetter.CompositeSurface setter = (GeometrySetter.CompositeSurface) this.setter;
            setter.set(geometry.fill(new CompositeSurfaceProperty()));
        } else if (this.setter instanceof GeometrySetter.Polygon) {
            GeometrySetter.Polygon setter = (GeometrySetter.Polygon) this.setter;
            setter.set(geometry.fill(new PolygonProperty()));
        } else if (this.setter instanceof GeometrySetter.MultiPolygon) {
            GeometrySetter.MultiPolygon setter = (GeometrySetter.MultiPolygon) this.setter;
            setter.set(geometry.fill(new MultiPolygonProperty()));
        } else if (this.setter instanceof GeometrySetter.CompositeSolid) {
            GeometrySetter.CompositeSolid setter = (GeometrySetter.CompositeSolid) this.setter;
            setter.set(geometry.fill(new CompositeSolidProperty()));
        } else if (this.setter instanceof GeometrySetter.MultiSolid) {
            GeometrySetter.MultiSolid setter = (GeometrySetter.MultiSolid) this.setter;
            setter.set(geometry.fill(new MultiSolidProperty()));
        } else if (this.setter instanceof GeometrySetter.Tin) {
            GeometrySetter.Tin setter = (GeometrySetter.Tin) this.setter;
            setter.set(geometry.fill(new TinProperty()));
        } else if (this.setter instanceof GeometrySetter.AbstractGeometry) {
            GeometrySetter.AbstractGeometry setter = (GeometrySetter.AbstractGeometry) this.setter;
            setter.set(geometry.fill(new GeometryProperty<>()));
        }
    }
}
