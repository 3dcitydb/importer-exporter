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
import org.citydb.core.operation.exporter.util.GeometrySetter;
import org.citydb.core.operation.exporter.util.SplitValue;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBBuildingFurniture extends AbstractFeatureExporter<BuildingFurniture> {
    private final Set<Long> batches;
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBImplicitGeometry implicitGeometryExporter;
    private final GMLConverter gmlConverter;

    private final int batchSize;
    private final String buildingModule;
    private final LodFilter lodFilter;
    private final AttributeValueSplitter valueSplitter;
    private final boolean hasObjectClassIdColumn;
    private final List<Table> adeHookTables;

    public DBBuildingFurniture(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(BuildingFurniture.class, connection, exporter);

        batches = new HashSet<>();
        batchSize = exporter.getFeatureBatchSize();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BUILDING_FURNITURE.getName());
        buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.BUILDING_FURNITURE.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"), table.getColumn("room_id"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", buildingModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", buildingModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", buildingModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (lodFilter.isEnabled(4)) {
            if (projectionFilter.containsProperty("lod4Geometry", buildingModule))
                select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
            if (projectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule)) {
                select.addProjection(table.getColumn("lod4_implicit_rep_id"),
                        exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")),
                        table.getColumn("lod4_implicit_transformation"));
            }
        }
        adeHookTables = addJoinsToADEHookTables(TableEnum.BUILDING_FURNITURE, table);

        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
        gmlConverter = exporter.getGMLConverter();
        valueSplitter = exporter.getAttributeValueSplitter();
    }

    private void addBatch(long id, Map<Long, Collection<BuildingFurniture>> buildingFurnitures) throws CityGMLExportException, SQLException {
        batches.add(id);
        if (batches.size() == batchSize)
            executeBatch(buildingFurnitures);
    }

    private void executeBatch(Map<Long, Collection<BuildingFurniture>> buildingFurnitures) throws CityGMLExportException, SQLException {
        if (batches.isEmpty())
            return;

        try {
            PreparedStatement ps;
            if (batches.size() == 1) {
                ps = getOrCreateStatement("room_id");
                ps.setLong(1, batches.iterator().next());
            } else {
                ps = getOrCreateBulkStatement("room_id", batchSize);
                prepareBulkStatement(ps, batches.toArray(new Long[0]), batchSize);
            }

            try (ResultSet rs = ps.executeQuery()) {
                for (Map.Entry<Long, BuildingFurniture> entry : doExport(0, null, null, rs).entrySet()) {
                    Long roomId = (Long) entry.getValue().getLocalProperty("room_id");
                    if (roomId == null) {
                        exporter.logOrThrowErrorMessage("Failed to assign furniture with id " + entry.getKey() + " to a building room.");
                        continue;
                    }

                    buildingFurnitures.computeIfAbsent(roomId, v -> new ArrayList<>()).add(entry.getValue());
                }
            }
        } finally {
            batches.clear();
        }
    }

    protected Collection<BuildingFurniture> doExport(long roomId) throws CityGMLExportException, SQLException {
        return doExport(roomId, null, null, getOrCreateStatement("room_id"));
    }

    protected Map<Long, Collection<BuildingFurniture>> doExport(Set<Long> roomIds) throws CityGMLExportException, SQLException {
        if (roomIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Collection<BuildingFurniture>> buildingFurnitures = new HashMap<>();
        for (Long roomId : roomIds) {
            addBatch(roomId, buildingFurnitures);
        }

        executeBatch(buildingFurnitures);
        return buildingFurnitures;
    }

    @Override
    protected Collection<BuildingFurniture> doExport(long id, BuildingFurniture root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            return doExport(id, root, rootType, rs).values();
        }
    }

    private Map<Long, BuildingFurniture> doExport(long id, BuildingFurniture root, FeatureType rootType, ResultSet rs) throws CityGMLExportException, SQLException {
        Map<Long, BuildingFurniture> buildingFurnitures = new HashMap<>();

        while (rs.next()) {
            long buildingFurnitureId = rs.getLong("id");
            BuildingFurniture buildingFurniture;
            FeatureType featureType;

            if (buildingFurnitureId == id && root != null) {
                buildingFurniture = root;
                featureType = rootType;
            } else {
                if (hasObjectClassIdColumn) {
                    // create building furniture object
                    int objectClassId = rs.getInt("objectclass_id");
                    buildingFurniture = exporter.createObject(objectClassId, BuildingFurniture.class);
                    if (buildingFurniture == null) {
                        exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, buildingFurnitureId) + " as building furniture object.");
                        continue;
                    }

                    featureType = exporter.getFeatureType(objectClassId);
                } else {
                    buildingFurniture = new BuildingFurniture();
                    featureType = exporter.getFeatureType(buildingFurniture);
                }
            }

            // get projection filter
            ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

            // export city object information
            cityObjectExporter.addBatch(buildingFurniture, id, featureType, projectionFilter);

            if (projectionFilter.containsProperty("class", buildingModule)) {
                String clazz = rs.getString("class");
                if (!rs.wasNull()) {
                    Code code = new Code(clazz);
                    code.setCodeSpace(rs.getString("class_codespace"));
                    buildingFurniture.setClazz(code);
                }
            }

            if (projectionFilter.containsProperty("function", buildingModule)) {
                for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                    Code function = new Code(splitValue.result(0));
                    function.setCodeSpace(splitValue.result(1));
                    buildingFurniture.addFunction(function);
                }
            }

            if (projectionFilter.containsProperty("usage", buildingModule)) {
                for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                    Code usage = new Code(splitValue.result(0));
                    usage.setCodeSpace(splitValue.result(1));
                    buildingFurniture.addUsage(usage);
                }
            }

            if (lodFilter.isEnabled(4)) {
                if (projectionFilter.containsProperty("lod4Geometry", buildingModule)) {
                    long geometryId = rs.getLong("lod4_brep_id");
                    if (!rs.wasNull())
                        geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) buildingFurniture::setLod4Geometry);
                    else {
                        Object geometryObj = rs.getObject("lod4_other_geom");
                        if (!rs.wasNull()) {
                            GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
                            if (geometry != null) {
                                GeometryProperty<AbstractGeometry> property = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
                                buildingFurniture.setLod4Geometry(property);
                            }
                        }
                    }
                }

                if (projectionFilter.containsProperty("lod4ImplicitRepresentation", buildingModule)) {
                    long implicitGeometryId = rs.getLong("lod4_implicit_rep_id");
                    if (!rs.wasNull()) {
                        GeometryObject referencePoint = null;
                        Object referencePointObj = rs.getObject("lod4_implicit_ref_point");
                        if (!rs.wasNull())
                            referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

                        String transformationMatrix = rs.getString("lod4_implicit_transformation");

                        ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
                        if (implicit != null) {
                            ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
                            implicitProperty.setObject(implicit);
                            buildingFurniture.setLod4ImplicitRepresentation(implicitProperty);
                        }
                    }
                }
            }

            // delegate export of generic ADE properties
            if (adeHookTables != null) {
                List<String> tableNames = retrieveADEHookTables(adeHookTables, rs);
                if (tableNames != null)
                    exporter.delegateToADEExporter(tableNames, buildingFurniture, id, featureType, projectionFilter);
            }

            buildingFurniture.setLocalProperty("room_id", rs.getLong("room_id"));
            buildingFurnitures.put(buildingFurnitureId, buildingFurniture);
        }

        return buildingFurnitures;
    }
}
