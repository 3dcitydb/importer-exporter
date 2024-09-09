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
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.transportation.*;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBTransportationComplex extends AbstractFeatureExporter<TransportationComplex> {
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBTrafficArea trafficAreaExporer;
    private final GMLConverter gmlConverter;

    private final String transportationModule;
    private final LodFilter lodFilter;
    private final AttributeValueSplitter valueSplitter;
    private final List<Table> complexADEHookTables;
    private List<Table> trafficAreaADEHookTables;

    public DBTransportationComplex(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(TransportationComplex.class, connection, exporter);

        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        trafficAreaExporer = exporter.getExporter(DBTrafficArea.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        gmlConverter = exporter.getGMLConverter();
        valueSplitter = exporter.getAttributeValueSplitter();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TRANSPORTATION_COMPLEX.getName());
        transportationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.TRANSPORTATION_COMPLEX.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", transportationModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", transportationModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", transportationModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (lodFilter.isEnabled(0) && projectionFilter.containsProperty("lod0Network", transportationModule))
            select.addProjection(exporter.getGeometryColumn(table.getColumn("lod0_network")));
        if (lodFilter.isEnabled(1) && projectionFilter.containsProperty("lod1MultiSurface", transportationModule))
            select.addProjection(table.getColumn("lod1_multi_surface_id"));
        if (lodFilter.isEnabled(2) && projectionFilter.containsProperty("lod2MultiSurface", transportationModule))
            select.addProjection(table.getColumn("lod2_multi_surface_id"));
        if (lodFilter.isEnabled(3) && projectionFilter.containsProperty("lod3MultiSurface", transportationModule))
            select.addProjection(table.getColumn("lod3_multi_surface_id"));
        if (lodFilter.isEnabled(4) && projectionFilter.containsProperty("lod4MultiSurface", transportationModule))
            select.addProjection(table.getColumn("lod4_multi_surface_id"));
        if (lodFilter.containsLodGreaterThanOrEuqalTo(2)
                && (projectionFilter.containsProperty("trafficArea", transportationModule)
                || projectionFilter.containsProperty("auxiliaryTrafficArea", transportationModule))) {
            CombinedProjectionFilter trafficAreaProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TRAFFIC_AREA.getName());
            Table trafficArea = new Table(TableEnum.TRAFFIC_AREA.getName(), schema);
            trafficAreaExporer.addProjection(select, trafficArea, trafficAreaProjectionFilter, "ta")
                    .addJoin(JoinFactory.left(trafficArea, "transportation_complex_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addProjection(trafficArea.getColumn("id", "ta_id"), trafficArea.getColumn("objectclass_id", "ta_objectclass_id"));
            trafficAreaADEHookTables = addJoinsToADEHookTables(TableEnum.TRAFFIC_AREA, trafficArea);
        }
        complexADEHookTables = addJoinsToADEHookTables(TableEnum.TRANSPORTATION_COMPLEX, table);
    }

    @Override
    protected Collection<TransportationComplex> doExport(long id, TransportationComplex root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            long currentComplexId = 0;
            TransportationComplex complex = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, TransportationComplex> complexes = new HashMap<>();
            Map<Long, GeometrySetterHandler> geometries = new LinkedHashMap<>();
            Map<Long, List<String>> adeHookTables = complexADEHookTables != null ? new HashMap<>() : null;

            while (rs.next()) {
                long complexId = rs.getLong("id");

                if (complexId != currentComplexId || complex == null) {
                    currentComplexId = complexId;

                    complex = complexes.get(complexId);
                    if (complex == null) {
                        FeatureType featureType;
                        if (complexId == id && root != null) {
                            complex = root;
                            featureType = rootType;
                        } else {
                            // create transportation complex object
                            int objectClassId = rs.getInt("objectclass_id");
                            complex = exporter.createObject(objectClassId, TransportationComplex.class);
                            if (complex == null) {
                                exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, complexId) + " as transportation complex object.");
                                continue;
                            }

                            featureType = exporter.getFeatureType(objectClassId);
                        }

                        // get projection filter
                        projectionFilter = exporter.getProjectionFilter(featureType);

                        // export city object information
                        cityObjectExporter.addBatch(complex, complexId, featureType, projectionFilter);

                        if (projectionFilter.containsProperty("class", transportationModule)) {
                            String clazz = rs.getString("class");
                            if (!rs.wasNull()) {
                                Code code = new Code(clazz);
                                code.setCodeSpace(rs.getString("class_codespace"));
                                complex.setClazz(code);
                            }
                        }

                        if (projectionFilter.containsProperty("function", transportationModule)) {
                            for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                                Code function = new Code(splitValue.result(0));
                                function.setCodeSpace(splitValue.result(1));
                                complex.addFunction(function);
                            }
                        }

                        if (projectionFilter.containsProperty("usage", transportationModule)) {
                            for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                                Code usage = new Code(splitValue.result(0));
                                usage.setCodeSpace(splitValue.result(1));
                                complex.addUsage(usage);
                            }
                        }

                        if (lodFilter.isEnabled(0) && projectionFilter.containsProperty("lod0Network", transportationModule)) {
                            Object lod0NetworkObj = rs.getObject("lod0_network");
                            if (!rs.wasNull()) {
                                GeometryObject lod0Network = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(lod0NetworkObj);
                                if (lod0Network != null)
                                    complex.addLod0Network(gmlConverter.getPointOrCurveComplexProperty(lod0Network, false));
                            }
                        }

                        LodIterator lodIterator = lodFilter.iterator(1, 4);
                        while (lodIterator.hasNext()) {
                            int lod = lodIterator.next();

                            if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", transportationModule))
                                continue;

                            long geometryId = rs.getLong("lod" + lod + "_multi_surface_id");
                            if (rs.wasNull())
                                continue;

                            switch (lod) {
                                case 1:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(complex::setLod1MultiSurface));
                                    break;
                                case 2:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(complex::setLod2MultiSurface));
                                    break;
                                case 3:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(complex::setLod3MultiSurface));
                                    break;
                                case 4:
                                    geometries.put(geometryId, new DefaultGeometrySetterHandler(complex::setLod4MultiSurface));
                                    break;
                            }
                        }

                        // get tables of ADE hook properties
                        if (complexADEHookTables != null) {
                            List<String> tables = retrieveADEHookTables(complexADEHookTables, rs);
                            if (tables != null) {
                                adeHookTables.put(complexId, tables);
                                complex.setLocalProperty("type", featureType);
                            }
                        }

                        complex.setLocalProperty("projection", projectionFilter);
                        complexes.put(complexId, complex);
                    } else
                        projectionFilter = (ProjectionFilter) complex.getLocalProperty("projection");
                }

                // continue if traffic areas shall not be exported
                if (!lodFilter.containsLodGreaterThanOrEuqalTo(2)
                        || (!projectionFilter.containsProperty("trafficArea", transportationModule)
                        && !projectionFilter.containsProperty("auxiliaryTrafficArea", transportationModule)))
                    continue;

                long transportationObjectId = rs.getLong("taid");
                if (rs.wasNull())
                    continue;

                int objectClassId = rs.getInt("taobjectclass_id");

                // create new traffic area object
                FeatureType featureType = exporter.getFeatureType(objectClassId);
                AbstractTransportationObject transportationObject = trafficAreaExporer.doExport(transportationObjectId, featureType, "ta", trafficAreaADEHookTables, rs);
                if (transportationObject == null) {
                    exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, transportationObjectId) + " as transportation object.");
                    continue;
                }

                if (transportationObject instanceof TrafficArea) {
                    TrafficAreaProperty property = new TrafficAreaProperty((TrafficArea) transportationObject);
                    complex.addTrafficArea(property);
                } else {
                    AuxiliaryTrafficAreaProperty property = new AuxiliaryTrafficAreaProperty((AuxiliaryTrafficArea) transportationObject);
                    complex.addAuxiliaryTrafficArea(property);
                }
            }

            // export postponed geometries
            for (Map.Entry<Long, GeometrySetterHandler> entry : geometries.entrySet())
                geometryExporter.addBatch(entry.getKey(), entry.getValue());

            // delegate export of generic ADE properties
            if (adeHookTables != null) {
                for (Map.Entry<Long, List<String>> entry : adeHookTables.entrySet()) {
                    long complexId = entry.getKey();
                    complex = complexes.get(complexId);
                    exporter.delegateToADEExporter(entry.getValue(), complex, complexId,
                            (FeatureType) complex.getLocalProperty("type"),
                            (ProjectionFilter) complex.getLocalProperty("projection"));
                }
            }

            return complexes.values();
        }
    }
}
