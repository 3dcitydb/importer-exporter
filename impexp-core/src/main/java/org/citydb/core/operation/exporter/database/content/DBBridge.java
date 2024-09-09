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

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AttributeValueSplitter;
import org.citydb.core.operation.exporter.util.DefaultGeometrySetterHandler;
import org.citydb.core.operation.exporter.util.GeometrySetterHandler;
import org.citydb.core.operation.exporter.util.SplitValue;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.lod.LodIterator;
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
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;

public class DBBridge extends AbstractFeatureExporter<AbstractBridge> {
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBBridgeThematicSurface thematicSurfaceExporter;
    private final DBBridgeOpening openingExporter;
    private final DBBridgeInstallation bridgeInstallationExporter;
    private final DBBridgeConstrElement bridgeConstrElemExporter;
    private final DBBridgeRoom bridgeRoomExporter;
    private final DBAddress addressExporter;
    private final GMLConverter gmlConverter;

    private final String bridgeModule;
    private final LodFilter lodFilter;
    private final AttributeValueSplitter valueSplitter;
    private final boolean hasObjectClassIdColumn;
    private final boolean useXLink;
    private final List<Table> bridgeADEHookTables;
    private List<Table> addressADEHookTables;
    private List<Table> surfaceADEHookTables;
    private List<Table> openingADEHookTables;
    private List<Table> openingAddressADEHookTables;

    public DBBridge(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(AbstractBridge.class, connection, exporter);

        thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
        openingExporter = exporter.getExporter(DBBridgeOpening.class);
        bridgeInstallationExporter = exporter.getExporter(DBBridgeInstallation.class);
        bridgeConstrElemExporter = exporter.getExporter(DBBridgeConstrElement.class);
        bridgeRoomExporter = exporter.getExporter(DBBridgeRoom.class);
        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        addressExporter = exporter.getExporter(DBAddress.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        gmlConverter = exporter.getGMLConverter();
        valueSplitter = exporter.getAttributeValueSplitter();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE.getName());
        bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
        useXLink = exporter.getInternalConfig().isExportFeatureReferences();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.BRIDGE.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"), table.getColumn("bridge_parent_id"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", bridgeModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", bridgeModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", bridgeModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (projectionFilter.containsProperty("yearOfConstruction", bridgeModule))
            select.addProjection(table.getColumn("year_of_construction"));
        if (projectionFilter.containsProperty("yearOfDemolition", bridgeModule))
            select.addProjection(table.getColumn("year_of_demolition"));
        if (projectionFilter.containsProperty("isMovable", bridgeModule))
            select.addProjection(table.getColumn("is_movable"));
        if (lodFilter.isEnabled(1)) {
            if (projectionFilter.containsProperty("lod1TerrainIntersection", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
            if (projectionFilter.containsProperty("lod1Solid", bridgeModule))
                select.addProjection(table.getColumn("lod1_solid_id"));
            if (projectionFilter.containsProperty("lod1MultiSurface", bridgeModule))
                select.addProjection(table.getColumn("lod1_multi_surface_id"));
        }
        if (lodFilter.isEnabled(2)) {
            if (projectionFilter.containsProperty("lod2TerrainIntersection", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
            if (projectionFilter.containsProperty("lod2MultiCurve", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
            if (projectionFilter.containsProperty("lod2Solid", bridgeModule))
                select.addProjection(table.getColumn("lod2_solid_id"));
            if (projectionFilter.containsProperty("lod2MultiSurface", bridgeModule))
                select.addProjection(table.getColumn("lod2_multi_surface_id"));
        }
        if (lodFilter.isEnabled(3)) {
            if (projectionFilter.containsProperty("lod3TerrainIntersection", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
            if (projectionFilter.containsProperty("lod3MultiCurve", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
            if (projectionFilter.containsProperty("lod3Solid", bridgeModule))
                select.addProjection(table.getColumn("lod3_solid_id"));
            if (projectionFilter.containsProperty("lod3MultiSurface", bridgeModule))
                select.addProjection(table.getColumn("lod3_multi_surface_id"));
        }
        if (lodFilter.isEnabled(4)) {
            if (projectionFilter.containsProperty("lod4TerrainIntersection", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
            if (projectionFilter.containsProperty("lod4MultiCurve", bridgeModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
            if (projectionFilter.containsProperty("lod4Solid", bridgeModule))
                select.addProjection(table.getColumn("lod4_solid_id"));
            if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule))
                select.addProjection(table.getColumn("lod4_multi_surface_id"));
        }
        if (projectionFilter.containsProperty("address", bridgeModule)) {
            Table address = new Table(TableEnum.ADDRESS.getName(), schema);
            Table addressToBridge = new Table(TableEnum.ADDRESS_TO_BRIDGE.getName(), schema);
            addressExporter.addProjection(select, address, "ba")
                    .addJoin(JoinFactory.left(addressToBridge, "bridge_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addJoin(JoinFactory.left(address, "id", ComparisonName.EQUAL_TO, addressToBridge.getColumn("address_id")));
            addressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, address);
        }
        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                && projectionFilter.containsProperty("boundedBy", bridgeModule)) {
            CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_THEMATIC_SURFACE.getName());
            Table thematicSurface = new Table(TableEnum.BRIDGE_THEMATIC_SURFACE.getName(), schema);
            thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
                    .addJoin(JoinFactory.left(thematicSurface, "bridge_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
            if (lodFilter.containsLodGreaterThanOrEuqalTo(3)
                    && boundarySurfaceProjectionFilter.containsProperty("opening", bridgeModule)) {
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
                    Table openingAddress = new Table(TableEnum.ADDRESS.getName(), schema);
                    addressExporter.addProjection(select, openingAddress, "oa")
                            .addJoin(JoinFactory.left(openingAddress, "id", ComparisonName.EQUAL_TO, opening.getColumn("address_id")));
                    openingAddressADEHookTables = addJoinsToADEHookTables(TableEnum.ADDRESS, openingAddress);
                }
                openingADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_OPENING, opening);
            }
            surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE_THEMATIC_SURFACE, thematicSurface);
        }
        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                && (projectionFilter.containsProperty("outerBridgeInstallation", bridgeModule)
                || projectionFilter.containsProperty("interiorBridgeInstallation", bridgeModule))) {
            Table installation = new Table(TableEnum.BRIDGE_INSTALLATION.getName(), schema);
            select.addProjection(new ColumnExpression(new Select()
                    .addProjection(installation.getColumn("id"))
                    .addSelection(ComparisonFactory.equalTo(installation.getColumn("bridge_id"), table.getColumn("id")))
                    .withFetch(new FetchToken(1)), "inid"));
        }
        if (lodFilter.containsLodGreaterThanOrEuqalTo(1)
                && projectionFilter.containsProperty("outerBridgeConstruction", bridgeModule)) {
            Table constructionElement = new Table(TableEnum.BRIDGE_CONSTR_ELEMENT.getName(), schema);
            select.addProjection(new ColumnExpression(new Select()
                    .addProjection(constructionElement.getColumn("id"))
                    .addSelection(ComparisonFactory.equalTo(constructionElement.getColumn("bridge_id"), table.getColumn("id")))
                    .withFetch(new FetchToken(1)), "ceid"));
        }
        if (lodFilter.isEnabled(4)
                && projectionFilter.containsProperty("interiorBridgeRoom", bridgeModule)) {
            Table bridgeRoom = new Table(TableEnum.BRIDGE_ROOM.getName(), schema);
            select.addProjection(new ColumnExpression(new Select()
                    .addProjection(bridgeRoom.getColumn("id"))
                    .addSelection(ComparisonFactory.equalTo(bridgeRoom.getColumn("bridge_id"), table.getColumn("id")))
                    .withFetch(new FetchToken(1)), "roid"));
        }
        bridgeADEHookTables = addJoinsToADEHookTables(TableEnum.BRIDGE, table);
    }

    @Override
    protected boolean doExport(AbstractBridge object, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
        ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
        String column = projectionFilter.containsProperty("consistsOfBridgePart", bridgeModule) ? "bridge_root_id" : "id";
        return !doExport(id, object, featureType, getOrCreateStatement(column)).isEmpty();
    }

    @Override
    protected Collection<AbstractBridge> doExport(long id, AbstractBridge root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            long currentBridgeId = 0;
            AbstractBridge bridge = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, AbstractBridge> bridges = new HashMap<>();
            Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
            Map<Long, List<String>> adeHookTables = bridgeADEHookTables != null ? new HashMap<>() : null;

            long currentBoundarySurfaceId = 0;
            AbstractBoundarySurface boundarySurface = null;
            ProjectionFilter boundarySurfaceProjectionFilter = null;
            Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

            long currentOpeningId = 0;
            OpeningProperty openingProperty = null;
            ProjectionFilter openingProjectionFilter = null;
            Map<String, OpeningProperty> openingProperties = new HashMap<>();

            Set<Long> installations = new HashSet<>();
            Set<Long> elements = new HashSet<>();
            Set<Long> bridgeRooms = new HashSet<>();
            Set<Long> bridgeAddresses = new HashSet<>();
            Set<String> openingAddresses = new HashSet<>();

            while (rs.next()) {
                long bridgeId = rs.getLong("id");

                if (bridgeId != currentBridgeId || bridge == null) {
                    currentBridgeId = bridgeId;

                    bridge = bridges.get(bridgeId);
                    if (bridge == null) {
                        FeatureType featureType;
                        if (bridgeId == id & root != null) {
                            bridge = root;
                            featureType = rootType;
                        } else {
                            if (hasObjectClassIdColumn) {
                                // create bridge object
                                int objectClassId = rs.getInt("objectclass_id");
                                bridge = exporter.createObject(objectClassId, AbstractBridge.class);
                                if (bridge == null) {
                                    exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, bridgeId) + " as bridge object.");
                                    continue;
                                }

                                featureType = exporter.getFeatureType(objectClassId);
                            } else {
                                bridge = new BridgePart();
                                featureType = exporter.getFeatureType(bridge);
                            }
                        }

                        // get projection filter
                        projectionFilter = exporter.getProjectionFilter(featureType);

                        // export city object information
                        cityObjectExporter.addBatch(bridge, bridgeId, featureType, projectionFilter);

                        if (projectionFilter.containsProperty("class", bridgeModule)) {
                            String clazz = rs.getString("class");
                            if (!rs.wasNull()) {
                                Code code = new Code(clazz);
                                code.setCodeSpace(rs.getString("class_codespace"));
                                bridge.setClazz(code);
                            }
                        }

                        if (projectionFilter.containsProperty("function", bridgeModule)) {
                            for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                                Code function = new Code(splitValue.result(0));
                                function.setCodeSpace(splitValue.result(1));
                                bridge.addFunction(function);
                            }
                        }

                        if (projectionFilter.containsProperty("usage", bridgeModule)) {
                            for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                                Code usage = new Code(splitValue.result(0));
                                usage.setCodeSpace(splitValue.result(1));
                                bridge.addUsage(usage);
                            }
                        }

                        if (projectionFilter.containsProperty("yearOfConstruction", bridgeModule))
                            bridge.setYearOfConstruction(rs.getObject("year_of_construction", LocalDate.class));

                        if (projectionFilter.containsProperty("yearOfDemolition", bridgeModule))
                            bridge.setYearOfDemolition(rs.getObject("year_of_demolition", LocalDate.class));

                        if (projectionFilter.containsProperty("isMovable", bridgeModule)) {
                            boolean isMovable = rs.getBoolean("is_movable");
                            if (!rs.wasNull())
                                bridge.setIsMovable(isMovable);
                        }

                        // brid:lodXTerrainIntersectionCurve
                        LodIterator lodIterator = lodFilter.iterator(1, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "TerrainIntersection", bridgeModule))
                                continue;

                            Object terrainIntersectionObj = rs.getObject("lod" + lod + "_terrain_intersection");
                            if (rs.wasNull())
                                continue;

                            GeometryObject terrainIntersection = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(terrainIntersectionObj);
                            if (terrainIntersection != null) {
                                MultiCurveProperty multiCurveProperty = gmlConverter.getMultiCurveProperty(terrainIntersection, false);
                                if (multiCurveProperty != null) {
                                    switch (lod) {
                                        case 1:
                                            bridge.setLod1TerrainIntersection(multiCurveProperty);
                                            break;
                                        case 2:
                                            bridge.setLod2TerrainIntersection(multiCurveProperty);
                                            break;
                                        case 3:
                                            bridge.setLod3TerrainIntersection(multiCurveProperty);
                                            break;
                                        case 4:
                                            bridge.setLod4TerrainIntersection(multiCurveProperty);
                                            break;
                                    }
                                }
                            }
                        }

                        // brid:lodXMultiCurve
                        lodIterator = lodFilter.iterator(2, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "MultiCurve", bridgeModule))
                                continue;

                            Object multiCurveObj = rs.getObject("lod" + lod + "_multi_curve");
                            if (rs.wasNull())
                                continue;

                            GeometryObject multiCurve = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(multiCurveObj);
                            if (multiCurve != null) {
                                MultiCurveProperty multiCurveProperty = gmlConverter.getMultiCurveProperty(multiCurve, false);
                                if (multiCurveProperty != null) {
                                    switch (lod) {
                                        case 2:
                                            bridge.setLod2MultiCurve(multiCurveProperty);
                                            break;
                                        case 3:
                                            bridge.setLod3MultiCurve(multiCurveProperty);
                                            break;
                                        case 4:
                                            bridge.setLod4MultiCurve(multiCurveProperty);
                                            break;
                                    }
                                }
                            }
                        }

                        // brid:lodXSolid
                        lodIterator = lodFilter.iterator(1, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "Solid", bridgeModule))
                                continue;

                            long geometryId = rs.getLong("lod" + lod + "_solid_id");
                            if (rs.wasNull())
                                continue;

                            switch (lod) {
                                case 1:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod1Solid));
                                    break;
                                case 2:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod2Solid));
                                    break;
                                case 3:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod3Solid));
                                    break;
                                case 4:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod4Solid));
                                    break;
                            }
                        }

                        // brid:lodXMultiSurface
                        lodIterator.reset();
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", bridgeModule))
                                continue;

                            long geometryId = rs.getLong("lod" + lod + "_multi_surface_id");
                            if (rs.wasNull())
                                continue;

                            switch (lod) {
                                case 1:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod1MultiSurface));
                                    break;
                                case 2:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod2MultiSurface));
                                    break;
                                case 3:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod3MultiSurface));
                                    break;
                                case 4:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(bridge::setLod4MultiSurface));
                                    break;
                            }
                        }

                        // brid:outerBridgeInstallation and bldg:interiorBridgeInstallation
                        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                                && (projectionFilter.containsProperty("outerBridgeInstallation", bridgeModule)
                                || projectionFilter.containsProperty("interiorBridgeInstallation", bridgeModule))) {
                            if (rs.getLong("inid") != 0) {
                                installations.add(bridgeId);
                            }
                        }

                        // brid:outerBridgeConstruction
                        if (lodFilter.containsLodGreaterThanOrEuqalTo(1) &&
                                projectionFilter.containsProperty("outerBridgeConstruction", bridgeModule)) {
                            if (rs.getLong("ceid") != 0) {
                                elements.add(bridgeId);
                            }
                        }

                        // brid:interiorBridgeRoom
                        if (lodFilter.isEnabled(4) &&
                                projectionFilter.containsProperty("interiorBridgeRoom", bridgeModule)) {
                            if (rs.getLong("roid") != 0) {
                                bridgeRooms.add(bridgeId);
                            }
                        }

                        // get tables of ADE hook properties
                        if (bridgeADEHookTables != null) {
                            List<String> tables = retrieveADEHookTables(bridgeADEHookTables, rs);
                            if (tables != null) {
                                adeHookTables.put(bridgeId, tables);
                                bridge.setLocalProperty("type", featureType);
                            }
                        }

                        bridge.setLocalProperty("parent", rs.getLong("bridge_parent_id"));
                        bridge.setLocalProperty("projection", projectionFilter);
                        bridges.put(bridgeId, bridge);
                    } else
                        projectionFilter = (ProjectionFilter) bridge.getLocalProperty("projection");
                }

                // brid:address
                if (projectionFilter.containsProperty("address", bridgeModule)) {
                    long addressId = rs.getLong("baid");
                    if (!rs.wasNull() && bridgeAddresses.add(addressId)) {
                        AddressProperty addressProperty = addressExporter.doExport(addressId, "ba", addressADEHookTables, rs);
                        if (addressProperty != null)
                            bridge.addAddress(addressProperty);
                    }
                }

                if (!lodFilter.containsLodGreaterThanOrEuqalTo(2)
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

                        bridge.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
                        boundarySurfaces.put(boundarySurfaceId, boundarySurface);
                    } else
                        boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
                }

                // continue if openings shall not be exported
                if (!lodFilter.containsLodGreaterThanOrEuqalTo(3)
                        || !boundarySurfaceProjectionFilter.containsProperty("opening", bridgeModule))
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
                            exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as bridge opening object.");
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
                        && openingProjectionFilter.containsProperty("address", bridgeModule)) {
                    long openingAddressId = rs.getLong("oaid");
                    if (!rs.wasNull() && openingAddresses.add(currentOpeningId + "_" + openingAddressId)) {
                        AddressProperty addressProperty = addressExporter.doExport(openingAddressId, "oa", openingAddressADEHookTables, rs);
                        if (addressProperty != null) {
                            Door door = (Door) openingProperty.getOpening();
                            door.addAddress(addressProperty);
                        }
                    }
                }
            }

            // export installations
            for (Map.Entry<Long, Collection<AbstractCityObject>> entry : bridgeInstallationExporter.doExportForBridges(installations).entrySet()) {
                bridge = bridges.get(entry.getKey());
                if (bridge != null) {
                    for (AbstractCityObject installation : entry.getValue()) {
                        projectionFilter = (ProjectionFilter) bridge.getLocalProperty("projection");
                        if (installation instanceof BridgeInstallation
                                && projectionFilter.containsProperty("outerBridgeInstallation", bridgeModule)) {
                            bridge.addOuterBridgeInstallation(new BridgeInstallationProperty((BridgeInstallation) installation));
                        } else if (installation instanceof IntBridgeInstallation
                                && projectionFilter.containsProperty("interiorBridgeInstallation", bridgeModule)) {
                            bridge.addInteriorBridgeInstallation(new IntBridgeInstallationProperty((IntBridgeInstallation) installation));
                        }
                    }
                }
            }

            // export construction elements
            for (Map.Entry<Long, Collection<BridgeConstructionElement>> entry : bridgeConstrElemExporter.doExport(elements).entrySet()) {
                bridge = bridges.get(entry.getKey());
                if (bridge != null) {
                    for (BridgeConstructionElement element : entry.getValue()) {
                        bridge.getOuterBridgeConstructionElement().add(new BridgeConstructionElementProperty(element));
                    }
                }
            }

            // export bridge rooms
            for (Map.Entry<Long, Collection<BridgeRoom>> entry : bridgeRoomExporter.doExport(bridgeRooms).entrySet()) {
                bridge = bridges.get(entry.getKey());
                if (bridge != null) {
                    for (BridgeRoom bridgeRoom : entry.getValue()) {
                        bridge.getInteriorBridgeRoom().add(new InteriorBridgeRoomProperty(bridgeRoom));
                    }
                }
            }

            // export postponed geometries
            for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
                geometryExporter.addBatch(entry.getKey(), entry.getValue());

            List<AbstractBridge> result = new ArrayList<>();
            for (Entry<Long, AbstractBridge> entry : bridges.entrySet()) {
                bridge = entry.getValue();
                long bridgeId = entry.getKey();
                long parentId = (Long) bridge.getLocalProperty("parent");

                // delegate export of generic ADE properties
                if (adeHookTables != null) {
                    List<String> tables = adeHookTables.get(bridgeId);
                    if (tables != null) {
                        exporter.delegateToADEExporter(tables, bridge, bridgeId,
                                (FeatureType) bridge.getLocalProperty("type"),
                                (ProjectionFilter) bridge.getLocalProperty("projection"));
                    }
                }

                // rebuild bridge part hierarchy
                if (parentId == 0) {
                    result.add(bridge);
                } else if (bridge instanceof BridgePart) {
                    AbstractBridge parent = bridges.get(parentId);
                    if (parent != null) {
                        projectionFilter = (ProjectionFilter) parent.getLocalProperty("projection");
                        if (projectionFilter.containsProperty("consistsOfBridgePart", bridgeModule))
                            parent.addConsistsOfBridgePart(new BridgePartProperty((BridgePart) bridge));
                    }
                } else
                    exporter.logOrThrowErrorMessage("Expected " + exporter.getObjectSignature(exporter.getFeatureType(bridge), bridgeId) + " to be a bridge part.");
            }

            return result;
        }
    }
}
