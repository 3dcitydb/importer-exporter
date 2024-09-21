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
package org.citydb.core.operation.importer.util;

import org.citydb.config.project.global.LogLevel;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.util.Util;
import org.citydb.util.log.Logger;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.GML;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.util.child.ChildInfo;

import java.util.List;

public class GeometryValidator {
    private final Logger log = Logger.getInstance();
    private final boolean failOnError;
    private final SchemaMapping schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
    private final ChildInfo childInfo = new ChildInfo();

    public GeometryValidator(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public GeometryValidator() {
        this(false);
    }

    public boolean isValidCurve(List<Double> coordinates, GML curve) throws CityGMLImportException {
        if (coordinates == null
                || (curve instanceof AbstractCurve
                && ((AbstractCurve) curve).hasLocalProperty(CoreConstants.GEOMETRY_INVALID))) {
            return false;
        }

        // too few points
        if (coordinates.size() / 3 < 2) {
            logOrThrow(curve, getGeometrySignature(curve) + " has too few points.");
            return false;
        }

        return true;
    }

    public boolean isValidRing(List<Double> coordinates, AbstractRing ring) throws CityGMLImportException {
        if (coordinates == null || ring.hasLocalProperty(CoreConstants.GEOMETRY_INVALID)) {
            return false;
        }

        // check closedness
        closeRing(coordinates, ring);

        // too few points
        if (coordinates.size() / 3 < 4) {
            logOrThrow(ring, getGeometrySignature(ring) + " has too few points.");
            return false;
        }

        return true;
    }

    private void closeRing(List<Double> coordinates, AbstractRing ring) throws CityGMLImportException {
        if (coordinates.size() >= 9) {
            Double x = coordinates.get(0);
            Double y = coordinates.get(1);
            Double z = coordinates.get(2);

            int nrOfPoints = coordinates.size();
            if (!x.equals(coordinates.get(nrOfPoints - 3))
                    || !y.equals(coordinates.get(nrOfPoints - 2))
                    || !z.equals(coordinates.get(nrOfPoints - 1))) {

                logOrThrow(ring, "Fixed unclosed " + getGeometrySignature(ring) + " by appending its first point.",
                        LogLevel.DEBUG);

                // repair unclosed ring...
                coordinates.add(x);
                coordinates.add(y);
                coordinates.add(z);
            }
        }
    }

    private void logOrThrow(GML gml, String message) throws CityGMLImportException {
        logOrThrow(gml, message, LogLevel.ERROR);
    }

    private void logOrThrow(GML gml, String message, LogLevel level) throws CityGMLImportException {
        if (gml instanceof AbstractGeometry) {
            ((AbstractGeometry) gml).setLocalProperty(CoreConstants.GEOMETRY_INVALID, message);
        }

        AbstractCityObject topLevel = gml instanceof Child ?
                childInfo.getRootCityObject((Child) gml) :
                null;

        message = topLevel != null ?
                getObjectSignature(topLevel) + ": " + message :
                message;

        if (level != LogLevel.ERROR || !failOnError) {
            log.log(level, message);
        } else {
            throw new CityGMLImportException(message);
        }
    }

    private String getGeometrySignature(GML gml) {
        String signature = "gml:" + gml.getClass().getSimpleName();
        if (gml instanceof AbstractGeometry) {
            AbstractGeometry geometry = (AbstractGeometry) gml;
            String gmlId = geometry.hasLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) ?
                    (String) geometry.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) :
                    geometry.getId();

            if (gmlId != null) {
                signature += " '" + gmlId + "'";
            }
        }

        return signature;
    }

    private String getObjectSignature(AbstractGML object) {
        AbstractObjectType<?> type = object instanceof AbstractFeature ?
                schemaMapping.getFeatureType(Util.getObjectClassId(object.getClass())) :
                schemaMapping.getObjectType(Util.getObjectClassId(object.getClass()));
        String signature = type != null ?
                type.getSchema().getXMLPrefix() + ":" + type.getPath() :
                object.getClass().getSimpleName();

        String gmlId = object.hasLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) ?
                (String) object.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) :
                object.getId();

        signature += gmlId != null ?
                " '" + gmlId + "'" :
                " (unknown gml:id)";

        return signature;
    }
}
