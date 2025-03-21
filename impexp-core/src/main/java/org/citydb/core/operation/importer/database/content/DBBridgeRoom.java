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

import org.citydb.config.Config;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.common.xlink.DBXlinkBasic;
import org.citydb.core.operation.common.xlink.DBXlinkSurfaceGeometry;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.util.AttributeValueJoiner;
import org.citygml4j.model.citygml.bridge.*;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBBridgeRoom implements DBImporter {
    private final CityGMLImportManager importer;

    private PreparedStatement psRoom;
    private DBCityObject cityObjectImporter;
    private DBSurfaceGeometry surfaceGeometryImporter;
    private DBBridgeThematicSurface thematicSurfaceImporter;
    private DBBridgeFurniture bridgeFurnitureImporter;
    private DBBridgeInstallation bridgeInstallationImporter;
    private AttributeValueJoiner valueJoiner;
    private boolean hasObjectClassIdColumn;
    private int batchCounter;

    public DBBridgeRoom(Connection batchConn, Config config, CityGMLImportManager importer) throws CityGMLImportException, SQLException {
        this.importer = importer;

        String schema = importer.getDatabaseAdapter().getConnectionDetails().getSchema();
        hasObjectClassIdColumn = importer.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

        String stmt = "insert into " + schema + ".bridge_room (id, class, class_codespace, function, function_codespace, usage, usage_codespace, bridge_id, " +
                "lod4_multi_surface_id, lod4_solid_id" +
                (hasObjectClassIdColumn ? ", objectclass_id) " : ") ") +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
                (hasObjectClassIdColumn ? ", ?)" : ")");
        psRoom = batchConn.prepareStatement(stmt);

        surfaceGeometryImporter = importer.getImporter(DBSurfaceGeometry.class);
        cityObjectImporter = importer.getImporter(DBCityObject.class);
        thematicSurfaceImporter = importer.getImporter(DBBridgeThematicSurface.class);
        bridgeFurnitureImporter = importer.getImporter(DBBridgeFurniture.class);
        bridgeInstallationImporter = importer.getImporter(DBBridgeInstallation.class);
        valueJoiner = importer.getAttributeValueJoiner();
    }

    protected long doImport(BridgeRoom bridgeRoom) throws CityGMLImportException, SQLException {
        return doImport(bridgeRoom, 0);
    }

    public long doImport(BridgeRoom bridgeRoom, long bridgeId) throws CityGMLImportException, SQLException {
        FeatureType featureType = importer.getFeatureType(bridgeRoom);
        if (featureType == null)
            throw new SQLException("Failed to retrieve feature type.");

        // import city object information
        long bridgeRoomId = cityObjectImporter.doImport(bridgeRoom, featureType);

        // import bridge room information
        // primary id
        psRoom.setLong(1, bridgeRoomId);

        // brid:class
        if (bridgeRoom.isSetClazz() && bridgeRoom.getClazz().isSetValue()) {
            psRoom.setString(2, bridgeRoom.getClazz().getValue());
            psRoom.setString(3, bridgeRoom.getClazz().getCodeSpace());
        } else {
            psRoom.setNull(2, Types.VARCHAR);
            psRoom.setNull(3, Types.VARCHAR);
        }

        // brid:function
        if (bridgeRoom.isSetFunction()) {
            valueJoiner.join(bridgeRoom.getFunction(), Code::getValue, Code::getCodeSpace);
            psRoom.setString(4, valueJoiner.result(0));
            psRoom.setString(5, valueJoiner.result(1));
        } else {
            psRoom.setNull(4, Types.VARCHAR);
            psRoom.setNull(5, Types.VARCHAR);
        }

        // brid:usage
        if (bridgeRoom.isSetUsage()) {
            valueJoiner.join(bridgeRoom.getUsage(), Code::getValue, Code::getCodeSpace);
            psRoom.setString(6, valueJoiner.result(0));
            psRoom.setString(7, valueJoiner.result(1));
        } else {
            psRoom.setNull(6, Types.VARCHAR);
            psRoom.setNull(7, Types.VARCHAR);
        }

        // parent bridge id
        if (bridgeId != 0)
            psRoom.setLong(8, bridgeId);
        else
            psRoom.setNull(8, Types.NULL);

        // brid:lod4MultiSurface
        long geometryId = 0;

        if (bridgeRoom.isSetLod4MultiSurface()) {
            MultiSurfaceProperty multiSurfacePropery = bridgeRoom.getLod4MultiSurface();

            if (multiSurfacePropery.isSetMultiSurface()) {
                geometryId = surfaceGeometryImporter.doImport(multiSurfacePropery.getMultiSurface(), bridgeRoomId);
            } else {
                String href = multiSurfacePropery.getHref();
                if (href != null && href.length() != 0) {
                    importer.propagateXlink(new DBXlinkSurfaceGeometry(
                            TableEnum.BRIDGE_ROOM.getName(),
                            bridgeRoomId,
                            href,
                            "lod4_multi_surface_id"));
                }
            }
        }

        if (geometryId != 0)
            psRoom.setLong(9, geometryId);
        else
            psRoom.setNull(9, Types.NULL);

        // brid:lod4Solid
        geometryId = 0;

        if (bridgeRoom.isSetLod4Solid()) {
            SolidProperty solidProperty = bridgeRoom.getLod4Solid();

            if (solidProperty.isSetSolid()) {
                geometryId = surfaceGeometryImporter.doImport(solidProperty.getSolid(), bridgeRoomId);
            } else {
                String href = solidProperty.getHref();
                if (href != null && href.length() != 0) {
                    importer.propagateXlink(new DBXlinkSurfaceGeometry(
                            TableEnum.BRIDGE_ROOM.getName(),
                            bridgeRoomId,
                            href,
                            "lod4_solid_id"));
                }
            }
        }

        if (geometryId != 0)
            psRoom.setLong(10, geometryId);
        else
            psRoom.setNull(10, Types.NULL);

        // objectclass id
        if (hasObjectClassIdColumn)
            psRoom.setLong(11, featureType.getObjectClassId());

        psRoom.addBatch();
        if (++batchCounter == importer.getDatabaseAdapter().getMaxBatchSize())
            importer.executeBatch(TableEnum.BRIDGE_ROOM);

        // brid:boundedBy
        if (bridgeRoom.isSetBoundedBySurface()) {
            for (BoundarySurfaceProperty property : bridgeRoom.getBoundedBySurface()) {
                AbstractBoundarySurface boundarySurface = property.getBoundarySurface();

                if (boundarySurface != null) {
                    thematicSurfaceImporter.doImport(boundarySurface, bridgeRoom, bridgeRoomId);
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0) {
                        importer.propagateXlink(new DBXlinkBasic(
                                TableEnum.BRIDGE_THEMATIC_SURFACE.getName(),
                                href,
                                bridgeRoomId,
                                "bridge_room_id"));
                    }
                }
            }
        }

        // brid:bridgeRoomInstallation
        if (bridgeRoom.isSetBridgeRoomInstallation()) {
            for (IntBridgeInstallationProperty property : bridgeRoom.getBridgeRoomInstallation()) {
                IntBridgeInstallation installation = property.getIntBridgeInstallation();

                if (installation != null) {
                    bridgeInstallationImporter.doImport(installation, bridgeRoom, bridgeRoomId);
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0) {
                        importer.propagateXlink(new DBXlinkBasic(
                                TableEnum.BRIDGE_INSTALLATION.getName(),
                                href,
                                bridgeRoomId,
                                "bridge_room_id"));
                    }
                }
            }
        }

        // brid:interiorFurniture
        if (bridgeRoom.isSetInteriorFurniture()) {
            for (InteriorFurnitureProperty property : bridgeRoom.getInteriorFurniture()) {
                BridgeFurniture furniture = property.getBridgeFurniture();

                if (furniture != null) {
                    bridgeFurnitureImporter.doImport(furniture, bridgeRoomId);
                } else {
                    String href = property.getHref();
                    if (href != null && href.length() != 0) {
                        importer.propagateXlink(new DBXlinkBasic(
                                TableEnum.BRIDGE_FURNITURE.getName(),
                                href,
                                bridgeRoomId,
                                "bridge_room_id"));
                    }
                }
            }
        }

        // ADE-specific extensions
        if (importer.hasADESupport())
            importer.delegateToADEImporter(bridgeRoom, bridgeRoomId, featureType);

        return bridgeRoomId;
    }

    @Override
    public void executeBatch() throws CityGMLImportException, SQLException {
        if (batchCounter > 0) {
            psRoom.executeBatch();
            batchCounter = 0;
        }
    }

    @Override
    public void close() throws CityGMLImportException, SQLException {
        psRoom.close();
    }

}
