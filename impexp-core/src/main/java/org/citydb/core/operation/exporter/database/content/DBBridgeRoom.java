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
package org.citydb.core.operation.exporter.database.content;

import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.operation.exporter.util.DefaultGeometrySetterHandler;
import org.citydb.core.operation.exporter.util.GeometrySetterHandler;
import org.citydb.core.operation.exporter.util.SplitValue;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.FetchToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.projection.ColumnExpression;
import org.citygml4j.model.citygml.bridge.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBBridgeRoom extends AbstractFeatureExporter<BridgeRoom> {
    private final Set<Long> batches;
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBBridgeInstallation bridgeInstallationExporter;
    private final DBBridgeThematicSurface thematicSurfaceExporter;
    private final DBBridgeOpening openingExporter;
    private final DBBridgeFurniture bridgeFurnitureExporter;
    private final DBAddress addressExporter;

    private final int batchSize;
    private final String bridgeModule;
    private final LodFilter lodFilter;
    private final AttributeValueSplitter valueSplitter;
    private final boolean hasObjectClassIdColumn;
    private final boolean useXLink;
    private final List<Table> bridgeRoomADEHookTables;
    private List<Table> surfaceADEHookTables;
    private List<Table> openingADEHookTables;
    private List<Table> addressADEHookTables;

    public DBBridgeRoom(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(BridgeRoom.class, connection, exporter);

        batches = new HashSet<>();
        batchSize = exporter.getFeatureBatchSize();
        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        bridgeInstallationExporter = exporter.getExporter(DBBridgeInstallation.class);
        thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
        openingExporter = exporter.getExporter(DBBridgeOpening.class);
        bridgeFurnitureExporter = exporter.getExporter(DBBridgeFurniture.class);
        addressExporter = exporter.getExporter(DBAddress.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        valueSplitter = exporter.getAttributeValueSplitter();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_ROOM.getName());
        bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
        useXLink = exporter.getInternalConfig().isExportFeatureReferences();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.BRIDGE_ROOM.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"), table.getColumn("bridge_id"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", bridgeModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", bridgeModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", bridgeModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (lodFilter.isEnabled(4)) {
            if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule))
                select.addProjection(table.getColumn("lod4_multi_surface_id"));
            if (projectionFilter.containsProperty("lod4Solid", bridgeModule))
                select.addProjection(table.getColumn("lod4_solid_id"));
            if (projectionFilter.containsProperty("boundedBy", bridgeModule)) {
                CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_THEMATIC_SURFACE.getName());
                Table thematicSurface = new Table(TableEnum.BRIDGE_THEMATIC_SURFACE.getName(), schema);
                thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
                        .addJoin(JoinFactory.left(thematicSurface, "bridge_room_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
                if (boundarySurfaceProjectionFilter.containsProperty("opening", bridgeModule)) {
                    CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_OPENING.getName());
                    Table opening = new Table(TableEnum.BRIDGE_OPENING.getName(), schema);
                    Table openingToThemSurface = new Table(TableEnum.BRIDGE_OPEN_TO_THEM_SRF.getName(), schema);
                    Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
                    openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
                            .addProjection(cityObject.getColumn("gmlid", "opgmlid"))
                            .addJoin(JoinFactory.left(openingToThemSurface, "bridge_thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
                            .addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("bridge_opening_id")))
                            .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
                    if (openingProjectionFilter.containsProperty("address", bridgeModule)) {
                        Table address = new Table(TableEnum.ADDRESS.getName(), schema);
                        addressExporter.addProjection(select, address, "oa")
                                .addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
                        addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
                    }
                    openingADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_OPENING, opening);
                }
                surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_THEMATIC_SURFACE, thematicSurface);
            }
            if (projectionFilter.containsProperty("bridgeRoomInstallation", bridgeModule)) {
                Table installation = new Table(TableEnum.BRIDGE_INSTALLATION.getName(), schema);
                select.addProjection(new ColumnExpression(new Select()
                        .addProjection(installation.getColumn("id"))
                        .addSelection(ComparisonFactory.equalTo(installation.getColumn("bridge_room_id"), table.getColumn("id")))
                        .withFetch(new FetchToken(1)), "inid"));
            }
            if (projectionFilter.containsProperty("interiorFurniture", bridgeModule)) {
                Table bridgeFurniture = new Table(TableEnum.BRIDGE_FURNITURE.getName(), schema);
                select.addProjection(new ColumnExpression(new Select()
                        .addProjection(bridgeFurniture.getColumn("id"))
                        .addSelection(ComparisonFactory.equalTo(bridgeFurniture.getColumn("bridge_room_id"), table.getColumn("id")))
                        .withFetch(new FetchToken(1)), "bfid"));
            }
        }
        bridgeRoomADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_ROOM, table);
    }

    private void addBatch(long id, Map<Long, Collection<BridgeRoom>> bridgeRooms) throws CityGMLExportException, SQLException {
        batches.add(id);
        if (batches.size() == batchSize)
            executeBatch(bridgeRooms);
    }

    private void executeBatch(Map<Long, Collection<BridgeRoom>> bridgeRooms) throws CityGMLExportException, SQLException {
        if (batches.isEmpty())
            return;

        try {
            PreparedStatement ps;
            if (batches.size() == 1) {
                ps = getOrCreateStatement("bridge_id");
                ps.setLong(1, batches.iterator().next());
            } else {
                ps = getOrCreateBulkStatement("bridge_id", batchSize);
                prepareBulkStatement(ps, batches.toArray(new Long[0]), batchSize);
            }

            try (ResultSet rs = ps.executeQuery()) {
                for (Map.Entry<Long, BridgeRoom> entry : doExport(0, null, null, rs).entrySet()) {
                    Long bridgeId = (Long) entry.getValue().getLocalProperty("bridge_id");
                    if (bridgeId == null) {
                        exporter.logOrThrowErrorMessage("Failed to assign bridge room with id " + entry.getKey() + " to a building.");
                        continue;
                    }

                    bridgeRooms.computeIfAbsent(bridgeId, v -> new ArrayList<>()).add(entry.getValue());
                }
            }
        } finally {
            batches.clear();
        }
    }

    protected Collection<BridgeRoom> doExport(long bridgeId) throws CityGMLExportException, SQLException {
        return doExport(bridgeId, null, null, getOrCreateStatement("bridge_id"));
    }

    protected Map<Long, Collection<BridgeRoom>> doExport(Set<Long> bridgeIds) throws CityGMLExportException, SQLException {
        if (bridgeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Collection<BridgeRoom>> bridgeRooms = new HashMap<>();
        for (Long bridgeId : bridgeIds) {
            addBatch(bridgeId, bridgeRooms);
        }

        executeBatch(bridgeRooms);
        return bridgeRooms;
    }

    @Override
    protected Collection<BridgeRoom> doExport(long id, BridgeRoom root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            return doExport(id, root, rootType, rs).values();
        }
    }

    private Map<Long, BridgeRoom> doExport(long id, BridgeRoom root, FeatureType rootType, ResultSet rs) throws CityGMLExportException, SQLException {
        long currentBridgeRoomId = 0;
        BridgeRoom bridgeRoom = null;
        ProjectionFilter projectionFilter = null;
        Map<Long, BridgeRoom> bridgeRooms = new HashMap<>();
        Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
        Map<Long, List<String>> adeHookTables = bridgeRoomADEHookTables != null ? new HashMap<>() : null;

        long currentBoundarySurfaceId = 0;
        AbstractBoundarySurface boundarySurface = null;
        ProjectionFilter boundarySurfaceProjectionFilter = null;
        Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

        long currentOpeningId = 0;
        OpeningProperty openingProperty = null;
        ProjectionFilter openingProjectionFilter = null;
        Map<String, OpeningProperty> openingProperties = new HashMap<>();

        Set<Long> installations = new HashSet<>();
        Set<Long> bridgeFurnitures = new HashSet<>();
        Set<String> addresses = new HashSet<>();

        while (rs.next()) {
            long bridgeRoomId = rs.getLong("id");

            if (bridgeRoomId != currentBridgeRoomId || bridgeRoom == null) {
                currentBridgeRoomId = bridgeRoomId;

                bridgeRoom = bridgeRooms.get(bridgeRoomId);
                if (bridgeRoom == null) {
                    FeatureType featureType;
                    if (bridgeRoomId == id && root != null) {
                        bridgeRoom = root;
                        featureType = rootType;
                    } else {
                        if (hasObjectClassIdColumn) {
                            // create bridge room object
                            int objectClassId = rs.getInt("objectclass_id");
                            bridgeRoom = exporter.createObject(objectClassId, BridgeRoom.class);
                            if (bridgeRoom == null) {
                                exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, bridgeRoomId) + " as bridge room object.");
                                continue;
                            }

                            featureType = exporter.getFeatureType(objectClassId);
                        } else {
                            bridgeRoom = new BridgeRoom();
                            featureType = exporter.getFeatureType(bridgeRoom);
                        }
                    }

                    // get projection filter
                    projectionFilter = exporter.getProjectionFilter(featureType);

                    // export city object information
                    cityObjectExporter.addBatch(bridgeRoom, bridgeRoomId, featureType, projectionFilter);

                    if (projectionFilter.containsProperty("class", bridgeModule)) {
                        String clazz = rs.getString("class");
                        if (!rs.wasNull()) {
                            Code code = new Code(clazz);
                            code.setCodeSpace(rs.getString("class_codespace"));
                            bridgeRoom.setClazz(code);
                        }
                    }

                    if (projectionFilter.containsProperty("function", bridgeModule)) {
                        for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                            Code function = new Code(splitValue.result(0));
                            function.setCodeSpace(splitValue.result(1));
                            bridgeRoom.addFunction(function);
                        }
                    }

                    if (projectionFilter.containsProperty("usage", bridgeModule)) {
                        for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                            Code usage = new Code(splitValue.result(0));
                            usage.setCodeSpace(splitValue.result(1));
                            bridgeRoom.addUsage(usage);
                        }
                    }

                    if (lodFilter.isEnabled(4)) {
                        // brid:lod4MultiSurface
                        if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) {
                            long geometryId = rs.getLong("lod4_multi_surface_id");
                            if (!rs.wasNull())
                                geometries.put(geometryId, new DefaultGeometrySetterHandler(bridgeRoom::setLod4MultiSurface));
                        }

                        // brid:lod4Solid
                        if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) {
                            long geometryId = rs.getLong("lod4_solid_id");
                            if (!rs.wasNull())
                                geometries.put(geometryId, new DefaultGeometrySetterHandler(bridgeRoom::setLod4Solid));
                        }
                    }

                    // brid:bridgeRoomInstallation
                    if (lodFilter.isEnabled(4)
                            && projectionFilter.containsProperty("bridgeRoomInstallation", bridgeModule)) {
                        if (rs.getLong("inid") != 0) {
                            installations.add(bridgeRoomId);
                        }
                    }

                    // brid:interiorFurniture
                    if (lodFilter.isEnabled(4)
                            && projectionFilter.containsProperty("interiorFurniture", bridgeModule)) {
                        if (rs.getLong("bfid") != 0) {
                            bridgeFurnitures.add(bridgeRoomId);
                        }
                    }

                    // get tables of ADE hook properties
                    if (bridgeRoomADEHookTables != null) {
                        List<String> tables = retrieveADEHookTables(bridgeRoomADEHookTables, rs);
                        if (tables != null) {
                            adeHookTables.put(bridgeRoomId, tables);
                            bridgeRoom.setLocalProperty("type", featureType);
                        }
                    }

                    bridgeRoom.setLocalProperty("bridge_id", rs.getLong("bridge_id"));
                    bridgeRoom.setLocalProperty("projection", projectionFilter);
                    bridgeRooms.put(bridgeRoomId, bridgeRoom);
                } else
                    projectionFilter = (ProjectionFilter) bridgeRoom.getLocalProperty("projection");
            }

            if (!lodFilter.isEnabled(4)
                    || !projectionFilter.containsProperty("boundedBy", bridgeModule))
                continue;

            // brid:boundedBy
            long boundarySurfaceId = rs.getLong("tsid");
            if (rs.wasNull())
                continue;

            if (boundarySurfaceId != currentBoundarySurfaceId || boundarySurface == null) {
                currentBoundarySurfaceId = boundarySurfaceId;
                currentOpeningId = 0;

                boundarySurface = boundarySurfaces.get(boundarySurfaceId);
                if (boundarySurface == null) {
                    int objectClassId = rs.getInt("tsobjectclass_id");
                    FeatureType featureType = exporter.getFeatureType(objectClassId);

                    boundarySurface = thematicSurfaceExporter.doExport(boundarySurfaceId, featureType, "ts", surfaceADEHookTables, rs);
                    if (boundarySurface == null) {
                        exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, boundarySurfaceId) + " as boundary surface object.");
                        continue;
                    }

                    // get projection filter
                    boundarySurfaceProjectionFilter = exporter.getProjectionFilter(featureType);
                    boundarySurface.setLocalProperty("projection", boundarySurfaceProjectionFilter);

                    bridgeRoom.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
                    boundarySurfaces.put(boundarySurfaceId, boundarySurface);
                } else
                    boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
            }

            // continue if openings shall not be exported
            if (!boundarySurfaceProjectionFilter.containsProperty("opening", bridgeModule))
                continue;

            long openingId = rs.getLong("opid");
            if (rs.wasNull())
                continue;

            if (openingId != currentOpeningId || openingProperty == null) {
                currentOpeningId = openingId;
                String key = currentBoundarySurfaceId + "_" + openingId;

                openingProperty = openingProperties.get(key);
                if (openingProperty == null) {
                    int objectClassId = rs.getInt("opobjectclass_id");

                    // check whether we need an XLink
                    String gmlId = rs.getString("opgmlid");
                    boolean generateNewGmlId = false;
                    if (!rs.wasNull()) {
                        if (exporter.lookupAndPutObjectId(gmlId, openingId, objectClassId)) {
                            if (useXLink) {
                                openingProperty = new OpeningProperty();
                                openingProperty.setHref("#" + gmlId);
                                boundarySurface.addOpening(openingProperty);
                                openingProperties.put(key, openingProperty);
                                continue;
                            } else
                                generateNewGmlId = true;
                        }
                    }

                    // create new opening object
                    FeatureType featureType = exporter.getFeatureType(objectClassId);
                    AbstractOpening opening = openingExporter.doExport(openingId, featureType, "op", openingADEHookTables, rs);
                    if (opening == null) {
                        exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as opening object.");
                        continue;
                    }

                    if (generateNewGmlId)
                        opening.setId(exporter.generateFeatureGmlId(opening, gmlId));

                    // get projection filter
                    openingProjectionFilter = exporter.getProjectionFilter(featureType);
                    opening.setLocalProperty("projection", openingProjectionFilter);

                    openingProperty = new OpeningProperty(opening);
                    boundarySurface.getOpening().add(openingProperty);
                    openingProperties.put(key, openingProperty);
                } else if (openingProperty.isSetOpening())
                    openingProjectionFilter = (ProjectionFilter) openingProperty.getOpening().getLocalProperty("projection");
            }

            if (openingProperty.getOpening() instanceof Door
                    && (openingProjectionFilter == null
                    || openingProjectionFilter.containsProperty("address", bridgeModule))) {
                long addressId = rs.getLong("oaid");
                if (!rs.wasNull() && addresses.add(currentOpeningId + "_" + addressId)) {
                    AddressProperty addressProperty = addressExporter.doExport(addressId, "oa", addressADEHookTables, rs);
                    if (addressProperty != null) {
                        Door door = (Door) openingProperty.getOpening();
                        door.addAddress(addressProperty);
                    }
                }
            }
        }

        // export installations
        for (Map.Entry<Long, Collection<AbstractCityObject>> entry : bridgeInstallationExporter.doExportForBridgeRooms(installations).entrySet()) {
            bridgeRoom = bridgeRooms.get(entry.getKey());
            if (bridgeRoom != null) {
                for (AbstractCityObject installation : entry.getValue()) {
                    if (installation instanceof IntBridgeInstallation) {
                        bridgeRoom.addBridgeRoomInstallation(new IntBridgeInstallationProperty((IntBridgeInstallation) installation));
                    }
                }
            }
        }

        // export furniture
        for (Map.Entry<Long, Collection<BridgeFurniture>> entry : bridgeFurnitureExporter.doExport(bridgeFurnitures).entrySet()) {
            bridgeRoom = bridgeRooms.get(entry.getKey());
            if (bridgeRoom != null) {
                for (BridgeFurniture bridgeFurniture : entry.getValue()) {
                    bridgeRoom.addInteriorFurniture(new InteriorFurnitureProperty(bridgeFurniture));
                }
            }
        }

        // export postponed geometries
        for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
            geometryExporter.addBatch(entry.getKey(), entry.getValue());

        // delegate export of generic ADE properties
        if (adeHookTables != null) {
            for (Map.Entry<Long, List<String>> entry : adeHookTables.entrySet()) {
                long bridgeRoomId = entry.getKey();
                bridgeRoom = bridgeRooms.get(bridgeRoomId);
                exporter.delegateToADEExporter(entry.getValue(), bridgeRoom, bridgeRoomId,
                        (FeatureType) bridgeRoom.getLocalProperty("type"),
                        (ProjectionFilter) bridgeRoom.getLocalProperty("projection"));
            }
        }

        return bridgeRooms;
    }
}
