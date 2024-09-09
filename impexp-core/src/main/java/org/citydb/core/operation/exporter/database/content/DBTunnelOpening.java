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
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.lod.LodIterator;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.tunnel.AbstractOpening;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBTunnelOpening extends AbstractFeatureExporter<AbstractOpening> {
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final DBImplicitGeometry implicitGeometryExporter;

    private final String tunnelModule;
    private final LodFilter lodFilter;
    private final List<Table> adeHookTables;

    public DBTunnelOpening(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(AbstractOpening.class, connection, exporter);

        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TUNNEL_OPENING.getName());
        tunnelModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TUNNEL).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.TUNNEL_OPENING.getName(), schema);
        select = addProjection(new Select(), table, projectionFilter, "");
        adeHookTables = addJoinsToADEHookTables(TableEnum.TUNNEL_OPENING, table);
    }

    protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
        select.addProjection(table.getColumn("id", prefix + "id"), table.getColumn("objectclass_id", prefix + "objectclass_id"));
        if (lodFilter.isEnabled(3)) {
            if (projectionFilter.containsProperty("lod3MultiSurface", tunnelModule))
                select.addProjection(table.getColumn("lod3_multi_surface_id", prefix + "lod3_multi_surface_id"));
            if (projectionFilter.containsProperty("lod3ImplicitRepresentation", tunnelModule)) {
                select.addProjection(table.getColumn("lod3_implicit_rep_id", prefix + "lod3_implicit_rep_id"),
                        exporter.getGeometryColumn(table.getColumn("lod3_implicit_ref_point"), prefix + "lod3_implicit_ref_point"),
                        table.getColumn("lod3_implicit_transformation", prefix + "lod3_implicit_transformation"));
            }
        }
        if (lodFilter.isEnabled(4)) {
            if (projectionFilter.containsProperty("lod4MultiSurface", tunnelModule))
                select.addProjection(table.getColumn("lod4_multi_surface_id", prefix + "lod4_multi_surface_id"));
            if (projectionFilter.containsProperty("lod4ImplicitRepresentation", tunnelModule)) {
                select.addProjection(table.getColumn("lod4_implicit_rep_id", prefix + "lod4_implicit_rep_id"),
                        exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point"), prefix + "lod4_implicit_ref_point"),
                        table.getColumn("lod4_implicit_transformation", prefix + "lod4_implicit_transformation"));
            }
        }

        return select;
    }

    @Override
    protected Collection<AbstractOpening> doExport(long id, AbstractOpening root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            List<AbstractOpening> openings = new ArrayList<>();

            while (rs.next()) {
                long openingId = rs.getLong("id");
                AbstractOpening opening;
                FeatureType featureType;

                if (openingId == id && root != null) {
                    opening = root;
                    featureType = rootType;
                } else {
                    // create opening object
                    int objectClassId = rs.getInt("objectclass_id");
                    opening = exporter.createObject(objectClassId, AbstractOpening.class);
                    if (opening == null) {
                        exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, openingId) + " as tunnel opening object.");
                        continue;
                    }

                    featureType = exporter.getFeatureType(objectClassId);
                }

                // get projection filter
                ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

                doExport(opening, openingId, featureType, projectionFilter, "", adeHookTables, rs);
                openings.add(opening);
            }

            return openings;
        }
    }

    protected AbstractOpening doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
        AbstractOpening opening = null;
        if (featureType != null) {
            opening = exporter.createObject(featureType.getObjectClassId(), AbstractOpening.class);
            if (opening != null)
                doExport(opening, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
        }

        return opening;
    }

    private void doExport(AbstractOpening object, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
        // export city object information
        cityObjectExporter.addBatch(object, id, featureType, projectionFilter);

        LodIterator lodIterator = lodFilter.iterator(3, 4);
        while (lodIterator.hasNext()) {
            int lod = lodIterator.next();

            if (!projectionFilter.containsProperty("lod" + lod + "MultiSurface", tunnelModule))
                continue;

            long geometryId = rs.getLong(prefix + "lod" + lod + "_multi_surface_id");
            if (rs.wasNull())
                continue;

            switch (lod) {
                case 3:
                    geometryExporter.addBatch(geometryId, object::setLod3MultiSurface);
                    break;
                case 4:
                    geometryExporter.addBatch(geometryId, object::setLod4MultiSurface);
                    break;
            }
        }

        lodIterator.reset();
        while (lodIterator.hasNext()) {
            int lod = lodIterator.next();

            if (!projectionFilter.containsProperty("lod" + lod + "ImplicitRepresentation", tunnelModule))
                continue;

            // get implicit geometry details
            long implicitGeometryId = rs.getLong(prefix + "lod" + lod + "_implicit_rep_id");
            if (rs.wasNull())
                continue;

            GeometryObject referencePoint = null;
            Object referencePointObj = rs.getObject(prefix + "lod" + lod + "_implicit_ref_point");
            if (!rs.wasNull())
                referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

            String transformationMatrix = rs.getString(prefix + "lod" + lod + "_implicit_transformation");

            ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
            if (implicit != null) {
                ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
                implicitProperty.setObject(implicit);

                switch (lod) {
                    case 3:
                        object.setLod3ImplicitRepresentation(implicitProperty);
                        break;
                    case 4:
                        object.setLod4ImplicitRepresentation(implicitProperty);
                        break;
                }
            }
        }

        // delegate export of generic ADE properties
        if (adeHookTables != null) {
            List<String> tableNames = retrieveADEHookTables(adeHookTables, rs);
            if (tableNames != null)
                exporter.delegateToADEExporter(tableNames, object, id, featureType, projectionFilter);
        }
    }
}
