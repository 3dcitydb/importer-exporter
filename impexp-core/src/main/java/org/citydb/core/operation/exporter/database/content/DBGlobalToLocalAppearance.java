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
import org.citydb.core.database.schema.TableEnum;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.util.AppearanceRemover;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.sql.AppearanceFilterBuilder;
import org.citydb.core.util.CoreConstants;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
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
    private final boolean replaceIds;
    private final boolean handleImplicitGeometries;
    private final AppearanceRemover appearanceRemover;
    private final Table textureParam;

    private CopyBuilder copyBuilder;
    private ChildInfo childInfo;

    private enum GeometryType {
        GEOMETRY,
        IMPLICIT_GEOMETRY
    }

    public DBGlobalToLocalAppearance(Connection connection, Query query, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
        super(Mode.GLOBAL_TO_LOCAL, null, exporter, config);
        this.connection = connection;

        replaceIds = config.getExportConfig().getResourceId().isReplaceWithUUIDs();
        handleImplicitGeometries = exporter.getInternalConfig().getOutputFormat() == OutputFormat.CITYGML;
        if (handleImplicitGeometries) {
            copyBuilder = new DeepCopyBuilder();
            childInfo = new ChildInfo();
        }

        appearanceRemover = new AppearanceRemover();
        select = new Select(select).setDistinct(true)
                .addSelection(ComparisonFactory.isNull(table.getColumn("cityobject_id")));

        textureParam = select.getInvolvedTables().stream()
                .filter(table -> TableEnum.TEXTUREPARAM.getName().equals(table.getName()))
                .findFirst().orElse(null);
        if (textureParam == null) {
            throw new CityGMLExportException("Failed to build global-to-local appearance query.");
        }

        // add appearance theme filter
        if (query.isSetAppearanceFilter()) {
            try {
                PredicateToken predicate = new AppearanceFilterBuilder(exporter.getDatabaseAdapter())
                        .buildAppearanceFilter(query.getAppearanceFilter(), table.getColumn("theme"));
                select.addSelection(predicate);
            } catch (QueryBuildException e) {
                throw new CityGMLExportException("Failed to build appearance filter.", e);
            }
        }
    }

    protected Collection<Appearance> doExport(AbstractCityObject cityObject) throws CityGMLExportException, SQLException {
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

                    String target = replaceIds ?
                            (String) geometry.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID) :
                            geometry.getId();

                    targets.computeIfAbsent(type, v -> new HashSet<>()).add("#" + target);
                }
            }
        });

        if (!surfaceGeometryIds.isEmpty()) {
            Select select = new Select(this.select)
                    .addSelection(ComparisonFactory.in(textureParam.getColumn("surface_geometry_id"),
                            new LiteralList(surfaceGeometryIds.toArray(new Long[0]))));

            try (PreparedStatement stmt = exporter.getDatabaseAdapter().getSQLAdapter().prepareStatement(select, connection);
                 ResultSet rs = stmt.executeQuery()) {
                Map<Long, Appearance> appearances = doExport(rs);
                if (!appearances.isEmpty()) {
                    return postprocess(appearances, targets, cityObject);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Appearance> postprocess(Map<Long, Appearance> appearances, Map<GeometryType, Set<String>> targets, AbstractCityObject cityObject) {
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
                if (replaceIds) {
                    entry.getValue().accept(exporter.getIdReplacer());
                }

                cityObject.addAppearance(new AppearanceProperty(entry.getValue()));
            }
        }

        if (replaceIds) {
            globalAppearances.forEach(appearance -> appearance.accept(exporter.getIdReplacer()));
        }

        return globalAppearances;
    }

    @Override
    public void close() throws SQLException {
        // nothing to do
    }
}
