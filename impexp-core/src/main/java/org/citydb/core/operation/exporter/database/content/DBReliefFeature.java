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
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBReliefFeature extends AbstractFeatureExporter<ReliefFeature> {
    private final DBCityObject cityObjectExporter;
    private final DBReliefComponent componentExporter;

    private final String reliefModule;
    private final LodFilter lodFilter;
    private final boolean hasObjectClassIdColumn;
    private final boolean useXLink;

    private final List<Table> reliefADEHookTables;
    private final List<Table> componentADEHookTables;

    public DBReliefFeature(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(ReliefFeature.class, connection, exporter);

        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        componentExporter = exporter.getExporter(DBReliefComponent.class);

        CombinedProjectionFilter componentProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.RELIEF_COMPONENT.getName());
        reliefModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.RELIEF).getNamespaceURI();
        lodFilter = exporter.getLodFilter();
        hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
        useXLink = exporter.getInternalConfig().isExportFeatureReferences();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

        table = new Table(TableEnum.RELIEF_FEATURE.getName(), schema);
        Table reliefComponent = new Table(TableEnum.RELIEF_COMPONENT.getName(), schema);
        Table reliefFeatToRelComp = new Table(TableEnum.RELIEF_FEAT_TO_REL_COMP.getName(), schema);
        Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);

        select = new Select().addProjection(table.getColumn("id"), table.getColumn("lod"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        select.addJoin(JoinFactory.inner(reliefFeatToRelComp, "relief_feature_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                .addJoin(JoinFactory.inner(reliefComponent, "id", ComparisonName.EQUAL_TO, reliefFeatToRelComp.getColumn("relief_component_id")));
        componentExporter.addProjection(select, reliefComponent, componentProjectionFilter, "rc")
                .addProjection(cityObject.getColumn("gmlid", "rcgmlid"))
                .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, reliefComponent.getColumn("id")));
        componentADEHookTables = addJoinsToADEHookTables(TableEnum.RELIEF_COMPONENT, reliefComponent);
        reliefADEHookTables = addJoinsToADEHookTables(TableEnum.RELIEF_FEATURE, table);
    }

    @Override
    protected Collection<ReliefFeature> doExport(long id, ReliefFeature root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            long currentReliefFeatureId = 0;
            ReliefFeature reliefFeature = null;
            ProjectionFilter projectionFilter = null;
            Map<Long, ReliefFeature> reliefFeatures = new HashMap<>();

            while (rs.next()) {
                long reliefFeatureId = rs.getLong("id");

                if (reliefFeatureId != currentReliefFeatureId || reliefFeature == null) {
                    currentReliefFeatureId = reliefFeatureId;

                    reliefFeature = reliefFeatures.get(reliefFeatureId);
                    if (reliefFeature == null) {
                        FeatureType featureType;
                        if (reliefFeatureId == id & root != null) {
                            reliefFeature = root;
                            featureType = rootType;
                        } else {
                            if (hasObjectClassIdColumn) {
                                // create relief feature object
                                int objectClassId = rs.getInt("objectclass_id");
                                reliefFeature = exporter.createObject(objectClassId, ReliefFeature.class);
                                if (reliefFeature == null) {
                                    exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, reliefFeatureId) + " as relief feature object.");
                                    continue;
                                }

                                featureType = exporter.getFeatureType(objectClassId);
                            } else {
                                reliefFeature = new ReliefFeature();
                                featureType = exporter.getFeatureType(reliefFeature);
                            }
                        }

                        // get projection filter
                        projectionFilter = exporter.getProjectionFilter(featureType);

                        // export city object information
                        cityObjectExporter.addBatch(reliefFeature, reliefFeatureId, featureType, projectionFilter);

                        int lod = rs.getInt("lod");
                        if (!lodFilter.isEnabled(lod))
                            continue;

                        reliefFeature.setLod(lod);

                        // delegate export of generic ADE properties
                        if (reliefADEHookTables != null) {
                            List<String> adeHookTables = retrieveADEHookTables(reliefADEHookTables, rs);
                            if (adeHookTables != null)
                                exporter.delegateToADEExporter(adeHookTables, reliefFeature, reliefFeatureId, featureType, projectionFilter);
                        }

                        reliefFeature.setLocalProperty("projection", projectionFilter);
                        reliefFeatures.put(reliefFeatureId, reliefFeature);
                    } else
                        projectionFilter = (ProjectionFilter) reliefFeature.getLocalProperty("projection");
                }

                if (!projectionFilter.containsProperty("reliefComponent", reliefModule))
                    continue;

                long componentId = rs.getLong("rcid");
                if (rs.wasNull())
                    continue;

                int objectClassId = rs.getInt("rcobjectclass_id");

                // check whether we need an XLink
                String gmlId = rs.getString("rcgmlid");
                boolean generateNewGmlId = false;
                if (!rs.wasNull()) {
                    if (exporter.lookupAndPutObjectId(gmlId, componentId, objectClassId)) {
                        if (useXLink) {
                            ReliefComponentProperty property = new ReliefComponentProperty();
                            property.setHref("#" + gmlId);
                            reliefFeature.addReliefComponent(property);
                            continue;
                        } else
                            generateNewGmlId = true;
                    }
                }

                // create new relief component object
                FeatureType featureType = exporter.getFeatureType(objectClassId);
                AbstractReliefComponent component = componentExporter.doExport(componentId, featureType, "rc", componentADEHookTables, rs);
                if (component == null) {
                    exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, componentId) + " as relief component object.");
                    continue;
                }

                if (generateNewGmlId)
                    component.setId(exporter.generateFeatureGmlId(component, gmlId));

                reliefFeature.addReliefComponent(new ReliefComponentProperty(component));
            }

            return reliefFeatures.values();
        }
    }
}
