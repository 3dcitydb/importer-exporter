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
package org.citydb.core.ade.importer;

import org.citydb.config.project.importer.ImportConfig;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.ObjectType;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.util.AttributeValueJoiner;
import org.citydb.core.operation.importer.util.GeometryConverter;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.AbstractGeometry;

import java.sql.SQLException;

public interface CityGMLImportHelper {
    long importObject(AbstractGML object) throws CityGMLImportException, SQLException;

    long importObject(AbstractGML object, ForeignKeys foreignKeys) throws CityGMLImportException, SQLException;

    long importGlobalAppearance(Appearance appearance) throws CityGMLImportException, SQLException;

    long importSurfaceGeometry(AbstractGeometry surfaceGeometry, long cityObjectId) throws CityGMLImportException, SQLException;

    long importImplicitGeometry(ImplicitGeometry implicitGeometry) throws CityGMLImportException, SQLException;

    GeometryConverter getGeometryConverter();

    String convertImplicitGeometryTransformationMatrix(TransformationMatrix4x4 matrix);

    boolean isSurfaceGeometry(AbstractGeometry abstractGeometry);

    boolean isPointOrLineGeometry(AbstractGeometry abstractGeometry);

    AbstractDatabaseAdapter getDatabaseAdapter();

    void executeBatch(String tableName) throws CityGMLImportException, SQLException;

    void executeBatch(AbstractObjectType<?> type) throws CityGMLImportException, SQLException;

    String getTableNameWithSchema(String tableName);

    long getNextSequenceValue(String sequence) throws SQLException;

    AttributeValueJoiner getAttributeValueJoiner();

    boolean isFailOnError();

    ImportConfig getImportConfig();

    void logOrThrowUnsupportedXLinkMessage(AbstractGML from, Class<? extends AbstractGML> to, String xlink) throws CityGMLImportException;

    void logOrThrowUnsupportedGeometryMessage(AbstractGML from, AbstractGeometry geometry) throws CityGMLImportException;

    void logOrThrowErrorMessage(String message) throws CityGMLImportException;

    void logOrThrowErrorMessage(String message, Throwable cause) throws CityGMLImportException;

    String getObjectSignature(AbstractGML object);

    int getObjectClassId(AbstractGML object);

    FeatureType getFeatureType(AbstractFeature feature);

    ObjectType getObjectType(AbstractGML object);

    AbstractObjectType<?> getAbstractObjectType(AbstractGML object);

    void propagateObjectXlink(String table, long objectId, String xlink, String propertyColumn);

    void propagateObjectXlink(String intermediateTable, long objectId, String fromColumn, String xlink, String toColumn);

    void propagateReverseObjectXlink(String toTable, String gmlId, long objectId, String propertyColumn);

    void propagateSurfaceGeometryXlink(String xlink, String table, long objectId, String propertyColumn);
}
