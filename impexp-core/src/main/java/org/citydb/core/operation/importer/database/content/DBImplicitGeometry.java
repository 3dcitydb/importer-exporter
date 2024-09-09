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
package org.citydb.core.operation.importer.database.content;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.SequenceEnum;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.operation.common.xlink.DBXlinkLibraryObject;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.util.ConcurrentLockManager;
import org.citydb.core.operation.importer.util.ExternalFileChecker;
import org.citydb.core.operation.importer.util.GeometryConverter;
import org.citydb.core.util.Util;
import org.citydb.util.log.Logger;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import java.sql.*;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBImplicitGeometry implements DBImporter {
    private final ConcurrentLockManager lockManager = ConcurrentLockManager.getInstance(DBImplicitGeometry.class);
    private final Logger log = Logger.getInstance();
    private final Connection connection;
    private final CityGMLImportManager importer;

    private final PreparedStatement psImplicitGeometry;
    private final PreparedStatement psUpdateImplicitGeometry;
    private final DBSurfaceGeometry surfaceGeometryImporter;
    private final GeometryConverter geometryConverter;
    private final ExternalFileChecker externalFileChecker;

    private final int nullGeometryType;
    private final String nullGeometryTypeName;
    private final boolean hasGmlIdColumn;
    private final String schema;

    private PreparedStatement psRelativeGeometryLookup;
    private PreparedStatement psLibraryObjectLookup;
    private int batchCounter;

    public DBImplicitGeometry(Connection connection, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
        this.connection = connection;
        this.importer = importer;

        nullGeometryType = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryType();
        nullGeometryTypeName = importer.getDatabaseAdapter().getGeometryConverter().getNullGeometryTypeName();
        hasGmlIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 3, 0) >= 0;
        schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();

        String stmt = "insert into " + schema + ".implicit_geometry (id, reference_to_library, mime_type, " +
                "relative_brep_id, relative_other_geom" +
                (hasGmlIdColumn ? ", gmlid) " : ") ") +
                "values (?, ?, ?, ?, ?" +
                (hasGmlIdColumn ? ", ?)" : ")");
        psImplicitGeometry = connection.prepareStatement(stmt);

        stmt = "update " + schema + ".implicit_geometry set relative_brep_id = ?, relative_other_geom = ? " +
                "where id = ?";
        psUpdateImplicitGeometry = connection.prepareStatement(stmt);

        surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
        geometryConverter = importer.getGeometryConverter();
        externalFileChecker = importer.getExternalFileChecker();
    }

    protected long doImport(ImplicitGeometry implicitGeometry) throws CityGMLImportException, SQLException {
        AbstractGeometry relativeGeometry = null;
        String gmlId = null;

        if (implicitGeometry.isSetRelativeGMLGeometry()) {
            GeometryProperty<? extends AbstractGeometry> property = implicitGeometry.getRelativeGMLGeometry();
            if (property.isSetGeometry()) {
                relativeGeometry = property.getGeometry();
                if (!relativeGeometry.isSetId()) {
                    relativeGeometry.setId(importer.generateNewGmlId());
                }

                gmlId = relativeGeometry.getId();
            } else if (property.isSetHref()) {
                if (Util.isRemoteXlink(property.getHref())) {
                    importer.logOrThrowErrorMessage(importer.getObjectSignature(implicitGeometry) +
                            ": XLink reference '" + property.getHref() + "' to remote relative GML geometry is not supported.");
                    return 0;
                }

                gmlId = property.getHref().replaceAll("^#", "");
            }
        }

        String libraryObject = implicitGeometry.isSetLibraryObject() ?
                implicitGeometry.getLibraryObject().trim() :
                null;

        if (gmlId == null && libraryObject == null) {
            return 0;
        } else if (gmlId != null && libraryObject != null) {
            log.warn(importer.getObjectSignature(implicitGeometry) + ": Found both a relative geometry and a library object.");
            log.warn("The library object will not be imported.");
            libraryObject = null;
        }

        // we synchronize the processing of the implicit geometry
        // to avoid duplicate entries.
        final ReentrantLock lock = lockManager.getLock(gmlId != null ? gmlId : libraryObject);
        lock.lock();
        try {
            Map.Entry<Long, Boolean> lookup = gmlId != null ?
                    lookupRelativeGeometry(gmlId) :
                    lookupLibraryObject(libraryObject);

            long implicitGeometryId = lookup.getKey();
            boolean isGlobal = lookup.getValue();

            if (implicitGeometryId <= 0) {
                implicitGeometryId = importer.getNextSequenceValue(SequenceEnum.IMPLICIT_GEOMETRY_ID_SEQ.getName());
                psImplicitGeometry.setLong(1, implicitGeometryId);
                psImplicitGeometry.setString(2, libraryObject);
                psImplicitGeometry.setString(3, libraryObject != null
                        && implicitGeometry.isSetMimeType()
                        && implicitGeometry.getMimeType().isSetValue() ?
                        implicitGeometry.getMimeType().getValue() :
                        null);

                if (relativeGeometry != null) {
                    Map.Entry<Long, GeometryObject> result = importRelativeGeometry(relativeGeometry, implicitGeometry);
                    if (result.getKey() != 0) {
                        psImplicitGeometry.setLong(4, result.getKey());
                    } else {
                        psImplicitGeometry.setNull(4, Types.NULL);
                    }

                    if (result.getValue() != null) {
                        psImplicitGeometry.setObject(5, importer.getDatabaseAdapter()
                                .getGeometryConverter().getDatabaseObject(result.getValue(), connection));
                    } else {
                        psImplicitGeometry.setNull(5, nullGeometryType, nullGeometryTypeName);
                    }
                } else {
                    psImplicitGeometry.setNull(4, Types.NULL);
                    psImplicitGeometry.setNull(5, nullGeometryType, nullGeometryTypeName);
                }

                if (libraryObject != null) {
                    try {
                        Map.Entry<String, String> result = externalFileChecker.getFileInfo(libraryObject);
                        importer.propagateXlink(new DBXlinkLibraryObject(implicitGeometryId, result.getKey()));
                    } catch (Exception e) {
                        importer.logOrThrowErrorMessage("Failed to read library object file at '" + libraryObject + "'.", e);
                    }
                }

                if (hasGmlIdColumn) {
                    psImplicitGeometry.setString(6, gmlId);
                }

                if (gmlId != null) {
                    importer.putObjectId(gmlId, implicitGeometryId, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID);
                }

                importer.updateObjectCounter(implicitGeometry, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID, implicitGeometryId);
                psImplicitGeometry.addBatch();
                batchCounter++;
            } else if (!isGlobal && relativeGeometry != null) {
                psUpdateImplicitGeometry.setLong(3, implicitGeometryId);

                Map.Entry<Long, GeometryObject> result = importRelativeGeometry(relativeGeometry, implicitGeometry);
                if (result.getKey() != 0) {
                    psUpdateImplicitGeometry.setLong(1, result.getKey());
                } else {
                    psUpdateImplicitGeometry.setNull(1, Types.NULL);
                }

                if (result.getValue() != null) {
                    psUpdateImplicitGeometry.setObject(2, importer.getDatabaseAdapter()
                            .getGeometryConverter().getDatabaseObject(result.getValue(), connection));
                } else {
                    psUpdateImplicitGeometry.setNull(2, nullGeometryType, nullGeometryTypeName);
                }

                psUpdateImplicitGeometry.addBatch();
                ++batchCounter;
            }

            if (batchCounter > 0) {
                importer.executeBatch(TableEnum.IMPLICIT_GEOMETRY);
                connection.commit();
            }

            return implicitGeometryId;
        } finally {
            lockManager.releaseLock(gmlId != null ? gmlId : libraryObject);
            lock.unlock();
        }
    }

    private Map.Entry<Long, GeometryObject> importRelativeGeometry(AbstractGeometry relativeGeometry, ImplicitGeometry implicitGeometry) throws CityGMLImportException, SQLException {
        long geometryId = 0;
        GeometryObject geometryObject = null;

        if (importer.isSurfaceGeometry(relativeGeometry)) {
            geometryId = surfaceGeometryImporter.importImplicitGeometry(relativeGeometry);
        } else if (importer.isPointOrLineGeometry(relativeGeometry)) {
            geometryObject = geometryConverter.getPointOrCurveGeometry(relativeGeometry);
        } else {
            importer.logOrThrowUnsupportedGeometryMessage(implicitGeometry, relativeGeometry);
        }

        return new AbstractMap.SimpleEntry<>(geometryId, geometryObject);
    }

    private Map.Entry<Long, Boolean> lookupRelativeGeometry(String gmlId) throws SQLException {
        long implicitGeometryId = importer.getObjectId(gmlId);
        boolean isGlobal = false;

        if (implicitGeometryId < 0 && hasGmlIdColumn) {
            implicitGeometryId = importer.getObjectId("#globalGeometry#" + gmlId);
            if (implicitGeometryId < 0) {
                PreparedStatement ps = getOrCreateRelativeGeometryLookup();
                ps.setString(1, gmlId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        implicitGeometryId = rs.getLong(1);
                        importer.putObjectId("#globalGeometry#" + gmlId, implicitGeometryId, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID);
                    }
                }
            }

            isGlobal = true;
        }

        return new AbstractMap.SimpleEntry<>(implicitGeometryId, isGlobal);
    }

    private Map.Entry<Long, Boolean> lookupLibraryObject(String libraryObject) throws SQLException {
        String lookupKey = "#libraryObject#" + libraryObject;
        long implicitGeometryId = importer.getObjectId(lookupKey);

        if (implicitGeometryId < 0) {
            PreparedStatement ps = getOrCreateLibraryObjectLookup();
            ps.setString(1, libraryObject);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    implicitGeometryId = rs.getLong(1);
                    importer.putObjectId(lookupKey, implicitGeometryId, MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID);
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(implicitGeometryId, false);
    }

    private PreparedStatement getOrCreateRelativeGeometryLookup() throws SQLException {
        if (psRelativeGeometryLookup == null) {
            psRelativeGeometryLookup = connection.prepareStatement(
                    "select id from " + schema + ".implicit_geometry " +
                            "where gmlid = ? fetch first 1 rows only");
        }

        return psRelativeGeometryLookup;
    }

    private PreparedStatement getOrCreateLibraryObjectLookup() throws SQLException {
        if (psLibraryObjectLookup == null) {
            psLibraryObjectLookup = connection.prepareStatement(
                    "select id from " + schema + ".implicit_geometry " +
                            "where reference_to_library = ? fetch first 1 rows only");
        }

        return psLibraryObjectLookup;
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            psImplicitGeometry.executeBatch();
            psUpdateImplicitGeometry.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        psImplicitGeometry.close();
        psUpdateImplicitGeometry.close();

        if (psRelativeGeometryLookup != null) {
            psRelativeGeometryLookup.close();
        }

        if (psLibraryObjectLookup != null) {
            psLibraryObjectLookup.close();
        }
    }
}
