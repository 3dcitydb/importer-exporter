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
package org.citydb.core.query.filter.selection.operator.spatial;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.expression.Expression;

public class SpatialOperationFactory {

    public static BinarySpatialOperator bbox(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.BBOX, spatialDescription);
    }

    public static BinarySpatialOperator bbox(GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(null, SpatialOperatorName.BBOX, spatialDescription);
    }

    public static BinarySpatialOperator equals(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.EQUALS, spatialDescription);
    }

    public static BinarySpatialOperator disjoint(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.DISJOINT, spatialDescription);
    }

    public static BinarySpatialOperator touches(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.TOUCHES, spatialDescription);
    }

    public static BinarySpatialOperator within(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.WITHIN, spatialDescription);
    }

    public static BinarySpatialOperator overlaps(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.OVERLAPS, spatialDescription);
    }

    public static BinarySpatialOperator intersects(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.INTERSECTS, spatialDescription);
    }

    public static BinarySpatialOperator contains(Expression leftOperand, GeometryObject spatialDescription) throws FilterException {
        return new BinarySpatialOperator(leftOperand, SpatialOperatorName.CONTAINS, spatialDescription);
    }

    public static DistanceOperator dWithin(Expression leftOperand, GeometryObject spatialDescription, Distance distance) throws FilterException {
        return new DistanceOperator(leftOperand, SpatialOperatorName.DWITHIN, spatialDescription, distance);
    }

    public static DistanceOperator beyond(Expression leftOperand, GeometryObject spatialDescription, Distance distance) throws FilterException {
        return new DistanceOperator(leftOperand, SpatialOperatorName.BEYOND, spatialDescription, distance);
    }

}
