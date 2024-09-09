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
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.tunnel.*;
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

public class DBTunnel extends AbstractFeatureExporter<AbstractTunnel> {
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBTunnelThematicSurface thematicSurfaceExporter;
    private final DBTunnelOpening openingExporter;
    private final DBTunnelInstallation tunnelInstallationExporter;
    private final DBTunnelHollowSpace hollowSpaceExporter;
    private final GMLConverter gmlConverter;

    private final String tunnelModule;
    private final LodFilter lodFilter;
    private final AttributeValueSplitter valueSplitter;
    private final boolean hasObjectClassIdColumn;
    private final boolean useXLink;
    private final List<Table> tunnelADEHookTables;
    private List<Table> surfaceADEHookTables;
    private List<Table> openingADEHookTables;

    public DBTunnel(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(AbstractTunnel.class, connection, exporter);

        thematicSurfaceExporter = exporter.getExporter(DBTunnelThematicSurface.class);
        openingExporter = exporter.getExporter(DBTunnelOpening.class);
        tunnelInstallationExporter = exporter.getExporter(DBTunnelInstallation.class);
        hollowSpaceExporter = exporter.getExporter(DBTunnelHollowSpace.class);
        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        gmlConverter = exporter.getGMLConverter();
        valueSplitter = exporter.getAttributeValueSplitter();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL.getName());
        tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
        useXLink = exporter.getInternalConfig().isExportFeatureReferences();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.TUNNEL.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"), table.getColumn("tunnel_parent_id"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", tunnelModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", tunnelModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", tunnelModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (projectionFilter.containsProperty("yearOfConstruction", tunnelModule))
            select.addProjection(table.getColumn("year_of_construction"));
        if (projectionFilter.containsProperty("yearOfDemolition", tunnelModule))
            select.addProjection(table.getColumn("year_of_demolition"));
        if (lodFilter.isEnabled(1)) {
            if (projectionFilter.containsProperty("lod1TerrainIntersection", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod1_terrain_intersection")));
            if (projectionFilter.containsProperty("lod1Solid", tunnelModule))
                select.addProjection(table.getColumn("lod1_solid_id"));
            if (projectionFilter.containsProperty("lod1MultiSurface", tunnelModule))
                select.addProjection(table.getColumn("lod1_multi_surface_id"));
        }
        if (lodFilter.isEnabled(2)) {
            if (projectionFilter.containsProperty("lod2TerrainIntersection", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_terrain_intersection")));
            if (projectionFilter.containsProperty("lod2MultiCurve", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod2_multi_curve")));
            if (projectionFilter.containsProperty("lod2Solid", tunnelModule))
                select.addProjection(table.getColumn("lod2_solid_id"));
            if (projectionFilter.containsProperty("lod2MultiSurface", tunnelModule))
                select.addProjection(table.getColumn("lod2_multi_surface_id"));
        }
        if (lodFilter.isEnabled(3)) {
            if (projectionFilter.containsProperty("lod3TerrainIntersection", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_terrain_intersection")));
            if (projectionFilter.containsProperty("lod3MultiCurve", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod3_multi_curve")));
            if (projectionFilter.containsProperty("lod3Solid", tunnelModule))
                select.addProjection(table.getColumn("lod3_solid_id"));
            if (projectionFilter.containsProperty("lod3MultiSurface", tunnelModule))
                select.addProjection(table.getColumn("lod3_multi_surface_id"));
        }
        if (lodFilter.isEnabled(4)) {
            if (projectionFilter.containsProperty("lod4TerrainIntersection", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_terrain_intersection")));
            if (projectionFilter.containsProperty("lod4MultiCurve", tunnelModule))
                select.addProjection(exporter.getGeometryColumn(table.getColumn("lod4_multi_curve")));
            if (projectionFilter.containsProperty("lod4Solid", tunnelModule))
                select.addProjection(table.getColumn("lod4_solid_id"));
            if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule))
                select.addProjection(table.getColumn("lod4_multi_surface_id"));
        }
        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                && projectionFilter.containsProperty("boundedBy", tunnelModule)) {
            CombinedProjectionFilter boundarySurfaceProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_THEMATIC_SURFACE.getName());
            Table thematicSurface = new Table(TableEnum.TUNNEL_THEMATIC_SURFACE.getName(), schema);
            thematicSurfaceExporter.addProjection(select, thematicSurface, boundarySurfaceProjectionFilter, "ts")
                    .addJoin(JoinFactory.left(thematicSurface, "tunnel_id", ComparisonName.EQUAL_TO, table.getColumn("id")));
            if (lodFilter.containsLodGreaterThanOrEuqalTo(3)
                    && boundarySurfaceProjectionFilter.containsProperty("opening", tunnelModule)) {
                CombinedProjectionFilter openingProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_OPENING.getName());
                Table opening = new Table(TableEnum.TUNNEL_OPENING.getName(), schema);
                Table openingToThemSurface = new Table(TableEnum.TUNNEL_OPEN_TO_THEM_SRF.getName(), schema);
                Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
                openingExporter.addProjection(select, opening, openingProjectionFilter, "op")
                        .addProjection(cityObject.getColumn("gmlid", "opgmlid"))
                        .addJoin(JoinFactory.left(openingToThemSurface, "tunnel_thematic_surface_id", ComparisonName.EQUAL_TO, thematicSurface.getColumn("id")))
                        .addJoin(JoinFactory.left(opening, "id", ComparisonName.EQUAL_TO, openingToThemSurface.getColumn("tunnel_opening_id")))
                        .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, opening.getColumn("id")));
                openingADEHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_OPENING, opening);
            }
            surfaceADEHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_THEMATIC_SURFACE, thematicSurface);
        }
        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                && (projectionFilter.containsProperty("outerTunnelInstallation", tunnelModule)
                || projectionFilter.containsProperty("interiorTunnelInstallation", tunnelModule))) {
            Table installation = new Table(TableEnum.TUNNEL_INSTALLATION.getName(), schema);
            select.addProjection(new ColumnExpression(new Select()
                    .addProjection(installation.getColumn("id"))
                    .addSelection(ComparisonFactory.equalTo(installation.getColumn("tunnel_id"), table.getColumn("id")))
                    .withFetch(new FetchToken(1)), "inid"));
        }
        if (lodFilter.isEnabled(4)
                && projectionFilter.containsProperty("interiorHollowSpace", tunnelModule)) {
            Table hollowSpace = new Table(TableEnum.TUNNEL_HOLLOW_SPACE.getName(), schema);
            select.addProjection(new ColumnExpression(new Select()
                    .addProjection(hollowSpace.getColumn("id"))
                    .addSelection(ComparisonFactory.equalTo(hollowSpace.getColumn("tunnel_id"), table.getColumn("id")))
                    .withFetch(new FetchToken(1)), "hsid"));
        }
        tunnelADEHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL, table);
    }

    @Override
    protected boolean doExport(AbstractTunnel object, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
        ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
        String column = projectionFilter.containsProperty("consistsOfTunnelPart", tunnelModule) ? "tunnel_root_id" : "id";
        return !doExport(id, object, featureType, getOrCreateStatement(column)).isEmpty();
    }

    @Override
    protected Collection<AbstractTunnel> doExport(long id, AbstractTunnel root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            long currentTunnelId = 0;
            AbstractTunnel tunnel = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, AbstractTunnel> tunnels = new HashMap<>();
            Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
            Map<Long, List<String>> adeHookTables = tunnelADEHookTables != null ? new HashMap<>() : null;

            long currentBoundarySurfaceId = 0;
            AbstractBoundarySurface boundarySurface = null;
            ProjectionFilter boundarySurfaceProjectionFilter = null;
            Map<Long, AbstractBoundarySurface> boundarySurfaces = new HashMap<>();

            long currentOpeningId = 0;
            OpeningProperty openingProperty = null;
            Map<String, OpeningProperty> openingProperties = new HashMap<>();

            Set<Long> installations = new HashSet<>();
            Set<Long> hollowSpaces = new HashSet<>();

            while (rs.next()) {
                long tunnelId = rs.getLong("id");

                if (tunnelId != currentTunnelId || tunnel == null) {
                    currentTunnelId = tunnelId;

                    tunnel = tunnels.get(tunnelId);
                    if (tunnel == null) {
                        FeatureType featureType;
                        if (tunnelId == id && root != null) {
                            tunnel = root;
                            featureType = rootType;
                        } else {
                            if (hasObjectClassIdColumn) {
                                // create tunnel object
                                int objectClassId = rs.getInt("objectclass_id");
                                tunnel = exporter.createObject(objectClassId, AbstractTunnel.class);
                                if (tunnel == null) {
                                    exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, tunnelId) + " as tunnel object.");
                                    continue;
                                }

                                featureType = exporter.getFeatureType(objectClassId);
                            } else {
                                tunnel = new TunnelPart();
                                featureType = exporter.getFeatureType(tunnel);
                            }
                        }

                        // get projection filter
                        projectionFilter = exporter.getProjectionFilter(featureType);

                        // export city object information
                        cityObjectExporter.addBatch(tunnel, tunnelId, featureType, projectionFilter);

                        if (projectionFilter.containsProperty("class", tunnelModule)) {
                            String clazz = rs.getString("class");
                            if (!rs.wasNull()) {
                                Code code = new Code(clazz);
                                code.setCodeSpace(rs.getString("class_codespace"));
                                tunnel.setClazz(code);
                            }
                        }

                        if (projectionFilter.containsProperty("function", tunnelModule)) {
                            for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                                Code function = new Code(splitValue.result(0));
                                function.setCodeSpace(splitValue.result(1));
                                tunnel.addFunction(function);
                            }
                        }

                        if (projectionFilter.containsProperty("usage", tunnelModule)) {
                            for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                                Code usage = new Code(splitValue.result(0));
                                usage.setCodeSpace(splitValue.result(1));
                                tunnel.addUsage(usage);
                            }
                        }

                        if (projectionFilter.containsProperty("yearOfConstruction", tunnelModule))
                            tunnel.setYearOfConstruction(rs.getObject("year_of_construction", LocalDate.class));

                        if (projectionFilter.containsProperty("yearOfDemolition", tunnelModule))
                            tunnel.setYearOfDemolition(rs.getObject("year_of_demolition", LocalDate.class));

                        // tun:lodXTerrainIntersectionCurve
                        LodIterator lodIterator = lodFilter.iterator(1, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "TerrainIntersection", tunnelModule))
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
                                            tunnel.setLod1TerrainIntersection(multiCurveProperty);
                                            break;
                                        case 2:
                                            tunnel.setLod2TerrainIntersection(multiCurveProperty);
                                            break;
                                        case 3:
                                            tunnel.setLod3TerrainIntersection(multiCurveProperty);
                                            break;
                                        case 4:
                                            tunnel.setLod4TerrainIntersection(multiCurveProperty);
                                            break;
                                    }
                                }
                            }
                        }

                        // tun:lodXMultiCurve
                        lodIterator = lodFilter.iterator(2, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "MultiCurve", tunnelModule))
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
                                            tunnel.setLod2MultiCurve(multiCurveProperty);
                                            break;
                                        case 3:
                                            tunnel.setLod3MultiCurve(multiCurveProperty);
                                            break;
                                        case 4:
                                            tunnel.setLod4MultiCurve(multiCurveProperty);
                                            break;
                                    }
                                }
                            }
                        }

                        // tun:lodXSolid
                        lodIterator = lodFilter.iterator(1, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "Solid", tunnelModule))
                                continue;

                            long geometryId = rs.getLong("lod" + lod + "_solid_id");
                            if (rs.wasNull())
                                continue;

                            switch (lod) {
                                case 1:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod1Solid));
                                    break;
                                case 2:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod2Solid));
                                    break;
                                case 3:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod3Solid));
                                    break;
                                case 4:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod4Solid));
                                    break;
                            }
                        }

                        // tun:lodXMultiSurface
                        lodIterator.reset();
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", tunnelModule))
                                continue;

                            long geometryId = rs.getLong("lod" + lod + "_multi_surface_id");
                            if (rs.wasNull())
                                continue;

                            switch (lod) {
                                case 1:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod1MultiSurface));
                                    break;
                                case 2:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod2MultiSurface));
                                    break;
                                case 3:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod3MultiSurface));
                                    break;
                                case 4:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(tunnel::setLod4MultiSurface));
                                    break;
                            }
                        }

                        // tun:outerTunnelInstallation and tun:interiorTunnelInstallation
                        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                                && (projectionFilter.containsProperty("outerTunnelInstallation", tunnelModule)
                                || projectionFilter.containsProperty("interiorTunnelInstallation", tunnelModule))) {
                            if (rs.getLong("inid") != 0) {
                                installations.add(tunnelId);
                            }
                        }

                        // tun:interiorHollowSpace
                        if (lodFilter.isEnabled(4)
                                && projectionFilter.containsProperty("interiorHollowSpace", tunnelModule)) {
                            if (rs.getLong("hsid") != 0) {
                                hollowSpaces.add(tunnelId);
                            }
                        }

                        // get tables of ADE hook properties
                        if (tunnelADEHookTables != null) {
                            List<String> tables = retrieveADEHookTables(this.tunnelADEHookTables, rs);
                            if (tables != null) {
                                adeHookTables.put(tunnelId, tables);
                                tunnel.setLocalProperty("type", featureType);
                            }
                        }

                        tunnel.setLocalProperty("parent", rs.getLong("tunnel_parent_id"));
                        tunnel.setLocalProperty("projection", projectionFilter);
                        tunnels.put(tunnelId, tunnel);
                    } else
                        projectionFilter = (ProjectionFilter) tunnel.getLocalProperty("projection");
                }

                if (!lodFilter.containsLodGreaterThanOrEuqalTo(2)
                        || !projectionFilter.containsProperty("boundedBy", tunnelModule))
                    continue;

                // tun:boundedBy
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

                        tunnel.getBoundedBySurface().add(new BoundarySurfaceProperty(boundarySurface));
                        boundarySurfaces.put(boundarySurfaceId, boundarySurface);
                    } else
                        boundarySurfaceProjectionFilter = (ProjectionFilter) boundarySurface.getLocalProperty("projection");
                }

                // continue if openings shall not be exported
                if (!lodFilter.containsLodGreaterThanOrEuqalTo(3)
                        || !boundarySurfaceProjectionFilter.containsProperty("opening", tunnelModule))
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
                            exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as tunnel opening object.");
                            continue;
                        }

                        if (generateNewGmlId)
                            opening.setId(exporter.generateFeatureGmlId(opening, gmlId));

                        openingProperty = new OpeningProperty(opening);
                        boundarySurface.getOpening().add(openingProperty);
                        openingProperties.put(key, openingProperty);
                    }
                }
            }

            // export installations
            for (Map.Entry<Long, Collection<AbstractCityObject>> entry : tunnelInstallationExporter.doExportForTunnels(installations).entrySet()) {
                tunnel = tunnels.get(entry.getKey());
                if (tunnel != null) {
                    for (AbstractCityObject installation : entry.getValue()) {
                        projectionFilter = (ProjectionFilter) tunnel.getLocalProperty("projection");
                        if (installation instanceof TunnelInstallation
                                && projectionFilter.containsProperty("outerTunnelInstallation", tunnelModule)) {
                            tunnel.addOuterTunnelInstallation(new TunnelInstallationProperty((TunnelInstallation) installation));
                        } else if (installation instanceof IntTunnelInstallation
                                && projectionFilter.containsProperty("interiorTunnelInstallation", tunnelModule)) {
                            tunnel.addInteriorTunnelInstallation(new IntTunnelInstallationProperty((IntTunnelInstallation) installation));
                        }
                    }
                }
            }

            // export hollow spaces
            for (Map.Entry<Long, Collection<HollowSpace>> entry : hollowSpaceExporter.doExport(hollowSpaces).entrySet()) {
                tunnel = tunnels.get(entry.getKey());
                if (tunnel != null) {
                    for (HollowSpace hollowSpace : entry.getValue()) {
                        tunnel.getInteriorHollowSpace().add(new InteriorHollowSpaceProperty(hollowSpace));
                    }
                }
            }

            // export postponed geometries
            for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
                geometryExporter.addBatch(entry.getKey(), entry.getValue());

            List<AbstractTunnel> result = new ArrayList<>();
            for (Entry<Long, AbstractTunnel> entry : tunnels.entrySet()) {
                tunnel = entry.getValue();
                long tunnelId = entry.getKey();
                long parentId = (Long) tunnel.getLocalProperty("parent");

                // delegate export of generic ADE properties
                if (adeHookTables != null) {
                    List<String> tables = adeHookTables.get(tunnelId);
                    if (tables != null) {
                        exporter.delegateToADEExporter(tables, tunnel, tunnelId,
                                (FeatureType) tunnel.getLocalProperty("type"),
                                (ProjectionFilter) tunnel.getLocalProperty("projection"));
                    }
                }

                // rebuild tunnel part hierarchy
                if (parentId == 0) {
                    result.add(tunnel);
                } else if (tunnel instanceof TunnelPart) {
                    AbstractTunnel parent = tunnels.get(parentId);
                    if (parent != null) {
                        projectionFilter = (ProjectionFilter) parent.getLocalProperty("projection");
                        if (projectionFilter.containsProperty("consistsOfTunnelPart", tunnelModule))
                            parent.addConsistsOfTunnelPart(new TunnelPartProperty((TunnelPart) tunnel));
                    }
                } else
                    exporter.logOrThrowErrorMessage("Expected " + exporter.getObjectSignature(exporter.getFeatureType(tunnel), tunnelId) + " to be a tunnel part.");
            }

            return result;
        }
    }
}
