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

import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.query.filter.lod.LodFilter;
import org.citydb.core.query.filter.lod.LodIterator;
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBWaterBoundarySurface extends AbstractFeatureExporter<AbstractWaterBoundarySurface> {
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;

    private final String waterBodyModule;
    private final LodFilter lodFilter;
    private final List<Table> adeHookTables;

    public DBWaterBoundarySurface(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(AbstractWaterBoundarySurface.class, connection, exporter);

        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.WATERBOUNDARY_SURFACE.getName());
        waterBodyModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.WATER_BODY).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.WATERBOUNDARY_SURFACE.getName(), schema);
        select = addProjection(new Select(), table, projectionFilter, "");
        adeHookTables = addJoinsToADEHookTables(TableEnum.WATERBOUNDARY_SURFACE, table);
    }

    protected Select addProjection(Select select, Table table, CombinedProjectionFilter projectionFilter, String prefix) {
        select.addProjection(table.getColumn("id", prefix + "id"), table.getColumn("objectclass_id", prefix + "objectclass_id"));
        if (projectionFilter.containsProperty("waterLevel", waterBodyModule))
            select.addProjection(table.getColumn("water_level", prefix + "water_level"), table.getColumn("water_level_codespace", prefix + "water_level_codespace"));
        if (lodFilter.isEnabled(2) && projectionFilter.containsProperty("lod2Surface", waterBodyModule))
            select.addProjection(table.getColumn("lod2_surface_id", prefix + "lod2_surface_id"));
        if (lodFilter.isEnabled(3) && projectionFilter.containsProperty("lod3Surface", waterBodyModule))
            select.addProjection(table.getColumn("lod3_surface_id", prefix + "lod3_surface_id"));
        if (lodFilter.isEnabled(4) && projectionFilter.containsProperty("lod4Surface", waterBodyModule))
            select.addProjection(table.getColumn("lod4_surface_id", prefix + "lod4_surface_id"));

        return select;
    }

    @Override
    protected Collection<AbstractWaterBoundarySurface> doExport(long id, AbstractWaterBoundarySurface root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            List<AbstractWaterBoundarySurface> waterBoundarySurfaces = new ArrayList<>();

            while (rs.next()) {
                long waterBoundarySurfaceId = rs.getLong("id");
                AbstractWaterBoundarySurface waterBoundarySurface;
                FeatureType featureType;

                if (waterBoundarySurfaceId == id & root != null) {
                    waterBoundarySurface = root;
                    featureType = rootType;
                } else {
                    // create water boundary surface object
                    int objectClassId = rs.getInt("objectclass_id");
                    waterBoundarySurface = exporter.createObject(objectClassId, AbstractWaterBoundarySurface.class);
                    if (waterBoundarySurface == null) {
                        exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, waterBoundarySurfaceId) + " as water boundary surface object.");
                        continue;
                    }

                    featureType = exporter.getFeatureType(objectClassId);
                }

                // get projection filter
                ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

                doExport(waterBoundarySurface, waterBoundarySurfaceId, featureType, projectionFilter, "", adeHookTables, rs);
                waterBoundarySurfaces.add(waterBoundarySurface);
            }

            return waterBoundarySurfaces;
        }
    }

    protected AbstractWaterBoundarySurface doExport(long id, FeatureType featureType, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
        AbstractWaterBoundarySurface waterBoundarySurface = null;
        if (featureType != null) {
            waterBoundarySurface = exporter.createObject(featureType.getObjectClassId(), AbstractWaterBoundarySurface.class);
            if (waterBoundarySurface != null)
                doExport(waterBoundarySurface, id, featureType, exporter.getProjectionFilter(featureType), prefix, adeHookTables, rs);
        }

        return waterBoundarySurface;
    }

    private void doExport(AbstractWaterBoundarySurface object, long id, FeatureType featureType, ProjectionFilter projectionFilter, String prefix, List<Table> adeHookTables, ResultSet rs) throws CityGMLExportException, SQLException {
        // export city object information
        cityObjectExporter.addBatch(object, id, featureType, projectionFilter);

        if (object instanceof WaterSurface
                && projectionFilter.containsProperty("waterLevel", waterBodyModule)) {
            String waterLevel = rs.getString(prefix + "water_level");
            if (!rs.wasNull()) {
                Code code = new Code(waterLevel);
                code.setCodeSpace(rs.getString(prefix + "water_level_codespace"));
                ((WaterSurface) object).setWaterLevel(code);
            }
        }

        LodIterator lodIterator = lodFilter.iterator(2, 4);
        while (lodIterator.hasNext()) {
            int lod = lodIterator.next();

            if (!projectionFilter.containsProperty("lod" + lod + "Surface", waterBodyModule))
                continue;

            long geometryId = rs.getLong(prefix + "lod" + lod + "_surface_id");
            if (rs.wasNull())
                continue;

            switch (lod) {
                case 2:
                    geometryExporter.addBatch(geometryId, object::setLod2Surface);
                    break;
                case 3:
                    geometryExporter.addBatch(geometryId, object::setLod3Surface);
                    break;
                case 4:
                    geometryExporter.addBatch(geometryId, object::setLod4Surface);
                    break;
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
