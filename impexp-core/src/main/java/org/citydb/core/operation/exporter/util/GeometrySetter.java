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

package org.citydb.core.operation.exporter.util;

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

@FunctionalInterface
public interface GeometrySetter<T extends GeometryProperty<?>> {
    void set(T property);

    interface AbstractGeometry extends GeometrySetter<GeometryProperty<?>> {
    }

    interface Surface extends GeometrySetter<SurfaceProperty> {
    }

    interface CompositeSurface extends GeometrySetter<CompositeSurfaceProperty> {
    }

    interface MultiSurface extends GeometrySetter<MultiSurfaceProperty> {
    }

    interface Polygon extends GeometrySetter<PolygonProperty> {
    }

    interface MultiPolygon extends GeometrySetter<MultiPolygonProperty> {
    }

    interface Solid extends GeometrySetter<SolidProperty> {
    }

    interface CompositeSolid extends GeometrySetter<CompositeSolidProperty> {
    }

    interface MultiSolid extends GeometrySetter<MultiSolidProperty> {
    }

    interface Tin extends GeometrySetter<TinProperty> {
    }
}
