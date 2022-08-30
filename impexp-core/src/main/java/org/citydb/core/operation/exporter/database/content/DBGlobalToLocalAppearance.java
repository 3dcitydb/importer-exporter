/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

import org.citydb.config.Config;
import org.citydb.config.project.exporter.OutputFormat;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AppearanceRemover;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.sql.AppearanceFilterBuilder;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.LiteralSelectExpression;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.util.child.ChildInfo;
import org.citygml4j.util.walker.GMLWalker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DBGlobalToLocalAppearance extends AbstractAppearanceExporter {
    private final Connection connection;
    private final PreparedStatement psBulk;
    private final PreparedStatement psSelect;
    private final Select appearanceQuery;
    private final Table textureParam;

    private final boolean handleImplicitGeometries;
    private final AppearanceRemover appearanceRemover;
    private final Map<Long, AbstractCityObject> batches;
    private final int batchSize;

    private CopyBuilder copyBuilder;
    private ChildInfo childInfo;

    private enum GeometryType {
        GEOMETRY,
        IMPLICIT_GEOMETRY
    }

    public DBGlobalToLocalAppearance(Connection connection, Query query, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
        super(false, null, exporter, config);
        this.connection = connection;

        handleImplicitGeometries = exporter.getInternalConfig().getOutputFormat() == OutputFormat.CITYGML;
        if (handleImplicitGeometries) {
            copyBuilder = new DeepCopyBuilder();
            childInfo = new ChildInfo();
        }

        appearanceRemover = new AppearanceRemover();
        batches = new LinkedHashMap<>();
        batchSize = exporter.getFeatureBatchSize();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        Table appearance = new Table("appearance", schema);
        Table appearToSurfaceData = new Table("appear_to_surface_data", schema);
        Table surfaceData = new Table("surface_data", schema);
        textureParam = new Table("textureparam", schema);

        appearanceQuery = new Select().setDistinct(true)
                .addProjection(appearance.getColumn(MappingConstants.ID))
                .addJoin(JoinFactory.inner(appearToSurfaceData, "appearance_id", ComparisonName.EQUAL_TO, appearance.getColumn(MappingConstants.ID)))
                .addJoin(JoinFactory.inner(surfaceData, MappingConstants.ID, ComparisonName.EQUAL_TO, appearToSurfaceData.getColumn("surface_data_id")))
                .addJoin(JoinFactory.inner(textureParam, "surface_data_id", ComparisonName.EQUAL_TO, surfaceData.getColumn(MappingConstants.ID)))
                .addSelection(ComparisonFactory.isNull(appearance.getColumn("cityobject_id")));

        // add appearance theme filter
        if (query.isSetAppearanceFilter()) {
            try {
                PredicateToken predicate = new AppearanceFilterBuilder(exporter.getDatabaseAdapter())
                        .buildAppearanceFilter(query.getAppearanceFilter(), appearance.getColumn("theme"));
                appearanceQuery.addSelection(predicate);
            } catch (QueryBuildException e) {
                throw new CityGMLExportException("Failed to build appearance filter.", e);
            }
        }

        String placeHolders = String.join(",", Collections.nCopies(batchSize, "?"));
        psBulk = connection.prepareStatement(new Select(select)
                .addSelection(ComparisonFactory.in(table.getColumn("id"), new LiteralSelectExpression(placeHolders))).toString());

        psSelect = connection.prepareStatement(new Select(select)
                .addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>())).toString());
    }

    protected Collection<Appearance> doExport(AbstractCityObject cityObject) throws CityGMLExportException, SQLException {
        List<Appearance> unconverted = new ArrayList<>();
        Set<Long> surfaceGeometryIds = new HashSet<>();
        Map<GeometryType, Set<String>> targets = new EnumMap<>(GeometryType.class);

        cityObject.accept(new GMLWalker() {
            @Override
            public void visit(AbstractGeometry geometry) {
                Long id = (Long) geometry.getLocalProperty("global_app_cache_id");
                if (id != null) {
                    surfaceGeometryIds.add(id);

                    GeometryType type = handleImplicitGeometries
                            && childInfo.getParentCityGML(geometry, ImplicitGeometry.class) != null ?
                            GeometryType.IMPLICIT_GEOMETRY :
                            GeometryType.GEOMETRY;

                    targets.computeIfAbsent(type, v -> new HashSet<>()).add("#" + geometry.getId());
                }
            }
        });

        if (!surfaceGeometryIds.isEmpty()) {
            Select select = new Select(appearanceQuery)
                    .addSelection(ComparisonFactory.in(textureParam.getColumn("surface_geometry_id"),
                            new LiteralList(surfaceGeometryIds.toArray(new Long[0]))));
            try (PreparedStatement stmt = exporter.getDatabaseAdapter().getSQLAdapter().prepareStatement(select, connection);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.put(rs.getLong(1), cityObject);
                    if (batches.size() == batchSize) {
                        unconverted.addAll(executeBatch(targets));
                    }
                }

                if (!batches.isEmpty()) {
                    unconverted.addAll(executeBatch(targets));
                }
            }
        }

        return unconverted;
    }

    private List<Appearance> executeBatch(Map<GeometryType, Set<String>> targets) throws CityGMLExportException, SQLException {
        if (!batches.isEmpty()) {
            try {
                Map<Long, Appearance> appearances;
                if (batches.size() == 1) {
                    psSelect.setLong(1, batches.entrySet().iterator().next().getKey());
                    try (ResultSet rs = psSelect.executeQuery()) {
                        appearances = doExport(rs);
                    }
                } else {
                    int i = 1;
                    Long[] ids = batches.keySet().toArray(new Long[0]);
                    for (int j = 0; j < batchSize; j++) {
                        psBulk.setLong(i + j, j < ids.length ? ids[j] : 0);
                    }

                    try (ResultSet rs = psBulk.executeQuery()) {
                        appearances = doExport(rs);
                    }
                }

                if (!appearances.isEmpty()) {
                    return postprocess(appearances, targets);
                }
            } finally {
                batches.clear();
            }
        }

        return Collections.emptyList();
    }

    private List<Appearance> postprocess(Map<Long, Appearance> appearances, Map<GeometryType, Set<String>> targets) {
        List<Appearance> globalAppearances = new ArrayList<>();
        Set<String> implicitGeometryTargets = targets.get(GeometryType.IMPLICIT_GEOMETRY);
        if (implicitGeometryTargets != null) {
            appearances.values().stream()
                    .map(appearance -> (Appearance) appearance.copy(copyBuilder))
                    .forEach(globalAppearances::add);
            appearanceRemover.cleanupAppearances(globalAppearances, implicitGeometryTargets);
        }

        Set<String> geometryTargets = targets.get(GeometryType.GEOMETRY);
        if (geometryTargets != null) {
            appearanceRemover.cleanupAppearances(appearances.values(), geometryTargets);
            for (Map.Entry<Long, Appearance> entry : appearances.entrySet()) {
                AbstractCityObject cityObject = batches.get(entry.getKey());
                cityObject.addAppearance(new AppearanceProperty(entry.getValue()));
            }
        }

        return globalAppearances;
    }

    @Override
    public void close() throws SQLException {
        psBulk.close();
        psSelect.close();
    }
}
