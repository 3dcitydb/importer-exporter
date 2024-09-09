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
import org.citygml4j.model.citygml.building.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBRoom extends AbstractFeatureExporter<Room> {
    private final Set<Long> batches;
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBBuildingInstallation buildingInstallationExporter;
    private final DBThematicSurface thematicSurfaceExporter;
    private final DBOpening openingExporter;
    private final DBBuildingFurniture buildingFurnitureExporter;
    private final DBAddress addressExporter;

    private final int batchSize;
    private final String buildingModule;
    private final LodFilter lodFilter;
    private final AttributeValueSplitter valueSplitter;
    private final boolean hasObjectClassIdColumn;
    private final boolean useXLink;
    private final List<Table> roomADEHookTables;
    private List<Table> surfaceADEHookTables;
    private List<Table> openingADEHookTables;
    private List<Table> addressADEHookTables;

    public DBRoom(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(Room.class, connection, exporter);

        batches = new HashSet<>();
        batchSize = exporter.getFeatureBatchSize();
        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        buildingInstallationExporter = exporter.getExporter(DBBuildingInstallation.class);
        thematicSurfaceExporter = exporter.getExporter(DBThematicSurface.class);
        openingExporter = exporter.getExporter(DBOpening.class);
        buildingFurnitureExporter = exporter.getExporter(DBBuildingFurniture.class);
        addressExporter = exporter.getExporter(DBAddress.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        valueSplitter = exporter.getAttributeValueSplitter();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.ROOM.getName());
        buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
        useXLink = exporter.getInternalConfig().isExportFeatureReferences();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.ROOM.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"), table.getColumn("building_id"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", buildingModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", buildingModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", buildingModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (lodFilter.isEnabled(4)) {
            if (projectionFilter.containsProperty("lod4MultiSurface", buildingModule))
                select.addProjection(table.getColumn("lod4_multi_surface_id"));
            if (projectionFilter.containsProperty("lod4Solid", buildingModule))
                select.addProjection(table.getColumn("lod4_solid_id"));
            if (projectionFilter.containsProperty("boundedBy", buildingModule)) {
                CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.THEMATIC_SURFACE.getName());
                Table thematicSurface = new Table(TableEnum.THEMATIC_SURFACE.getName(), schema);
                thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
                        .addJoin(JoinFactory.left(thematicSurface, "room_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
                if (boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule)) {
                    CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.OPENING.getName());
                    Table opening = new Table(TableEnum.OPENING.getName(), schema);
                    Table openingToThemSurface = new Table(TableEnum.OPENING_TO_THEM_SURFACE.getName(), schema);
                    Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
                    openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
                            .addProjection(cityObject.getColumn("gmlid", "opgmlid"))
                            .addJoin(JoinFactory.left(openingToThemSurface, "thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
                            .addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("opening_id")))
                            .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
                    if (openingProjectionFilter.containsProperty("address", buildingModule)) {
                        Table address = new Table(TableEnum.ADDRESS.getName(), schema);
                        addressExporter.addProjection(select, address, "oa")
                                .addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
                        addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
                    }
                    openingADEHookTables = addJoinsToADEHookTables(TableEnum.OPENING, opening);
                }
                surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.THEMATIC_SURFACE, thematicSurface);
            }
            if (projectionFilter.containsProperty("roomInstallation", buildingModule)) {
                Table installation = new Table(TableEnum.BUILDING_INSTALLATION.getName(), schema);
                select.addProjection(new ColumnExpression(new Select()
                        .addProjection(installation.getColumn("id"))
                        .addSelection(ComparisonFactory.equalTo(installation.getColumn("room_id"), table.getColumn("id")))
                        .withFetch(new FetchToken(1)), "inid"));
            }
            if (projectionFilter.containsProperty("interiorFurniture", buildingModule)) {
                Table buildingFurniture = new Table(TableEnum.BUILDING_FURNITURE.getName(), schema);
                select.addProjection(new ColumnExpression(new Select()
                        .addProjection(buildingFurniture.getColumn("id"))
                        .addSelection(ComparisonFactory.equalTo(buildingFurniture.getColumn("room_id"), table.getColumn("id")))
                        .withFetch(new FetchToken(1)), "bfid"));
            }
        }
        roomADEHookTables = addJoinsToADEHookTables(TableEnum.ROOM, table);
    }

    private void addBatch(long id, Map<Long, Collection<Room>> rooms) throws CityGMLExportException, SQLException {
        batches.add(id);
        if (batches.size() == batchSize)
            executeBatch(rooms);
    }

    private void executeBatch(Map<Long, Collection<Room>> rooms) throws CityGMLExportException, SQLException {
        if (batches.isEmpty())
            return;

        try {
            PreparedStatement ps;
            if (batches.size() == 1) {
                ps = getOrCreateStatement("building_id");
                ps.setLong(1, batches.iterator().next());
            } else {
                ps = getOrCreateBulkStatement("building_id", batchSize);
                prepareBulkStatement(ps, batches.toArray(new Long[0]), batchSize);
            }

            try (ResultSet rs = ps.executeQuery()) {
                for (Map.Entry<Long, Room> entry : doExport(0, null, null, rs).entrySet()) {
                    Long buildingId = (Long) entry.getValue().getLocalProperty("building_id");
                    if (buildingId == null) {
                        exporter.logOrThrowErrorMessage("Failed to assign room with id " + entry.getKey() + " to a building.");
                        continue;
                    }

                    rooms.computeIfAbsent(buildingId, v -> new ArrayList<>()).add(entry.getValue());
                }
            }
        } finally {
            batches.clear();
        }
    }

    protected Collection<Room> doExport(long buildingId) throws CityGMLExportException, SQLException {
        return doExport(buildingId, null, null, getOrCreateStatement("building_id"));
    }

    protected Map<Long, Collection<Room>> doExport(Set<Long> buildingIds) throws CityGMLExportException, SQLException {
        if (buildingIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Collection<Room>> rooms = new HashMap<>();
        for (Long buildingId : buildingIds) {
            addBatch(buildingId, rooms);
        }

        executeBatch(rooms);
        return rooms;
    }

    @Override
    protected Collection<Room> doExport(long id, Room root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            return doExport(id, root, rootType, rs).values();
        }
    }

    private Map<Long, Room> doExport(long id, Room root, FeatureType rootType, ResultSet rs) throws CityGMLExportException, SQLException {
        long currentRoomId = 0;
        Room room = null;
        ProjectionFilter projectionFilter = null;
        Map<Long, Room> rooms = new HashMap<>();
        Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
        Map<Long, List<String>> adeHookTables = roomADEHookTables != null ? new HashMap<>() : null;

        long currentBoundarySurfaceId = 0;
        AbstractBoundarySurface boundarySurface = null;
        ProjectionFilter boundarySurfaceProjectionFilter = null;
        Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

        long currentOpeningId = 0;
        OpeningProperty openingProperty = null;
        ProjectionFilter openingProjectionFilter = null;
        Map<String, OpeningProperty> openingProperties = new HashMap<>();

        Set<Long> installations = new HashSet<>();
        Set<Long> buildingFurnitures = new HashSet<>();
        Set<String> addresses = new HashSet<>();

        while (rs.next()) {
            long roomId = rs.getLong("id");

            if (roomId != currentRoomId || room == null) {
                currentRoomId = roomId;

                room = rooms.get(roomId);
                if (room == null) {
                    FeatureType featureType;
                    if (roomId == id && root != null) {
                        room = root;
                        featureType = rootType;
                    } else {
                        if (hasObjectClassIdColumn) {
                            // create room object
                            int objectClassId = rs.getInt("objectclass_id");
                            room = exporter.createObject(objectClassId, Room.class);
                            if (room == null) {
                                exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, roomId) + " as room object.");
                                continue;
                            }

                            featureType = exporter.getFeatureType(objectClassId);
                        } else {
                            room = new Room();
                            featureType = exporter.getFeatureType(room);
                        }
                    }

                    // get projection filter
                    projectionFilter = exporter.getProjectionFilter(featureType);

                    // export city object information
                    cityObjectExporter.addBatch(room, roomId, featureType, projectionFilter);

                    if (projectionFilter.containsProperty("class", buildingModule)) {
                        String clazz = rs.getString("class");
                        if (!rs.wasNull()) {
                            Code code = new Code(clazz);
                            code.setCodeSpace(rs.getString("class_codespace"));
                            room.setClazz(code);
                        }
                    }

                    if (projectionFilter.containsProperty("function", buildingModule)) {
                        for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                            Code function = new Code(splitValue.result(0));
                            function.setCodeSpace(splitValue.result(1));
                            room.addFunction(function);
                        }
                    }

                    if (projectionFilter.containsProperty("usage", buildingModule)) {
                        for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                            Code usage = new Code(splitValue.result(0));
                            usage.setCodeSpace(splitValue.result(1));
                            room.addUsage(usage);
                        }
                    }

                    if (lodFilter.isEnabled(4)) {
                        // bldg:lod4MultiSurface
                        if (projectionFilter.containsProperty("lod4MultiSurface", buildingModule)) {
                            long geometryId = rs.getLong("lod4_multi_surface_id");
                            if (!rs.wasNull())
                                geometries.put(geometryId, new DefaultGeometrySetterHandler(room::setLod4MultiSurface));
                        }

                        // bldg:lod4Solid
                        if (projectionFilter.containsProperty("lod4Solid", buildingModule)) {
                            long geometryId = rs.getLong("lod4_solid_id");
                            if (!rs.wasNull())
                                geometries.put(geometryId, new DefaultGeometrySetterHandler(room::setLod4Solid));
                        }
                    }

                    // bldg:roomInstallation
                    if (lodFilter.isEnabled(4)
                            && projectionFilter.containsProperty("roomInstallation", buildingModule)) {
                        if (rs.getLong("inid") != 0) {
                            installations.add(roomId);
                        }
                    }

                    // bldg:interiorFurniture
                    if (lodFilter.isEnabled(4)
                            && projectionFilter.containsProperty("interiorFurniture", buildingModule)) {
                        if (rs.getLong("bfid") != 0) {
                            buildingFurnitures.add(roomId);
                        }
                    }

                    // get tables of ADE hook properties
                    if (roomADEHookTables != null) {
                        List<String> tables = retrieveADEHookTables(roomADEHookTables, rs);
                        if (tables != null) {
                            adeHookTables.put(roomId, tables);
                            room.setLocalProperty("type", featureType);
                        }
                    }

                    room.setLocalProperty("building_id", rs.getLong("building_id"));
                    room.setLocalProperty("projection", projectionFilter);
                    rooms.put(roomId, room);
                } else
                    projectionFilter = (ProjectionFilter) room.getLocalProperty("projection");
            }

            if (!lodFilter.isEnabled(4)
                    || !projectionFilter.containsProperty("boundedBy", buildingModule))
                continue;

            // bldg:boundedBy
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

                    room.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
                    boundarySurfaces.put(boundarySurfaceId, boundarySurface);
                } else
                    boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
            }

            // continue if openings shall not be exported
            if (!boundarySurfaceProjectionFilter.containsProperty("opening", buildingModule))
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
                    || openingProjectionFilter.containsProperty("address", buildingModule))) {
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
        for (Map.Entry<Long, Collection<AbstractCityObject>> entry : buildingInstallationExporter.doExportForRooms(installations).entrySet()) {
            room = rooms.get(entry.getKey());
            if (room != null) {
                for (AbstractCityObject installation : entry.getValue()) {
                    if (installation instanceof IntBuildingInstallation) {
                        room.addRoomInstallation(new IntBuildingInstallationProperty((IntBuildingInstallation) installation));
                    }
                }
            }
        }

        // export furniture
        for (Map.Entry<Long, Collection<BuildingFurniture>> entry : buildingFurnitureExporter.doExport(buildingFurnitures).entrySet()) {
            room = rooms.get(entry.getKey());
            if (room != null) {
                for (BuildingFurniture buildingFurniture : entry.getValue()) {
                    room.addInteriorFurniture(new InteriorFurnitureProperty(buildingFurniture));
                }
            }
        }

        // export postponed geometries
        for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
            geometryExporter.addBatch(entry.getKey(), entry.getValue());

        // delegate export of generic ADE properties
        if (adeHookTables != null) {
            for (Map.Entry<Long, List<String>> entry : adeHookTables.entrySet()) {
                long roomId = entry.getKey();
                room = rooms.get(roomId);
                exporter.delegateToADEExporter(entry.getValue(), room, roomId,
                        (FeatureType) room.getLocalProperty("type"),
                        (ProjectionFilter) room.getLocalProperty("projection"));
            }
        }

        return rooms;
    }
}
