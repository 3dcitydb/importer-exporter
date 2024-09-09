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
import org.citydb.core.query.filter.projection.CombinedProjectionFilter;
import org.citydb.core.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupParent;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBCityObjectGroup extends AbstractTypeExporter {
    private final PreparedStatement ps;
    private final DBSurfaceGeometry geometryExporter;
    private final DBCityObject cityObjectExporter;
    private final GMLConverter gmlConverter;

    private final String groupModule;
    private final AttributeValueSplitter valueSplitter;
    private final List<Table> adeHookTables;

    public DBCityObjectGroup(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
        super(exporter);

        cityObjectExporter = exporter.getExporter(DBCityObject.class);
        geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
        gmlConverter = exporter.getGMLConverter();
        valueSplitter = exporter.getAttributeValueSplitter();

        CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.CITYOBJECTGROUP.getName());
        groupModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.CITY_OBJECT_GROUP).getNamespaceURI();
        String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
        boolean hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

        table = new Table(TableEnum.CITYOBJECTGROUP.getName(), schema);
        select = new Select().addProjection(table.getColumn("id"));
        if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
        if (projectionFilter.containsProperty("class", groupModule))
            select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
        if (projectionFilter.containsProperty("function", groupModule))
            select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
        if (projectionFilter.containsProperty("usage", groupModule))
            select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
        if (projectionFilter.containsProperty("geometry", groupModule))
            select.addProjection(table.getColumn("brep_id"), exporter.getGeometryColumn(table.getColumn("other_geom")));
        if (projectionFilter.containsProperty("parent", groupModule)) {
            Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
            select.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, table.getColumn("parent_cityobject_id")))
                    .addProjection(table.getColumn("parent_cityobject_id"), cityObject.getColumn("gmlid", "parent_gmlid"));
        }
        if (projectionFilter.containsProperty("groupMember", groupModule)) {
            Table cityObject = new Table(TableEnum.CITYOBJECT.getName(), schema);
            Table groupToCityObject = new Table(TableEnum.GROUP_TO_CITYOBJECT.getName(), schema);
            select.addJoin(JoinFactory.left(groupToCityObject, "cityobjectgroup_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
                    .addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, groupToCityObject.getColumn("cityobject_id")))
                    .addProjection(groupToCityObject.getColumn("cityobject_id"), groupToCityObject.getColumn("role"), cityObject.getColumn("gmlid", "member_gmlid"));
        }
        adeHookTables = addJoinsToADEHookTables(TableEnum.CITYOBJECTGROUP, table);

        select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
        ps = connection.prepareStatement(select.toString());
    }

    protected boolean doExport(CityObjectGroup cityObjectGroup, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            boolean isInited = false;

            // get projection filter
            ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

            while (rs.next()) {
                if (!isInited) {
                    // export city object information
                    cityObjectExporter.addBatch(cityObjectGroup, id, featureType, projectionFilter);

                    if (projectionFilter.containsProperty("class", groupModule)) {
                        String clazz = rs.getString("class");
                        if (!rs.wasNull()) {
                            Code code = new Code(clazz);
                            code.setCodeSpace(rs.getString("class_codespace"));
                            cityObjectGroup.setClazz(code);
                        }
                    }

                    if (projectionFilter.containsProperty("function", groupModule)) {
                        for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
                            Code function = new Code(splitValue.result(0));
                            function.setCodeSpace(splitValue.result(1));
                            cityObjectGroup.addFunction(function);
                        }
                    }

                    if (projectionFilter.containsProperty("usage", groupModule)) {
                        for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
                            Code usage = new Code(splitValue.result(0));
                            usage.setCodeSpace(splitValue.result(1));
                            cityObjectGroup.addUsage(usage);
                        }
                    }

                    if (projectionFilter.containsProperty("geometry", groupModule)) {
                        long geometryId = rs.getLong("brep_id");
                        if (!rs.wasNull())
                            geometryExporter.addBatch(geometryId, (GeometrySetter.AbstractGeometry) cityObjectGroup::setGeometry);
                        else {
                            Object geometryObj = rs.getObject("other_geom");
                            if (!rs.wasNull()) {
                                GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
                                if (geometry != null) {
                                    GeometryProperty<AbstractGeometry> property = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
                                    cityObjectGroup.setGeometry(property);
                                }
                            }
                        }
                    }

                    if (projectionFilter.containsProperty("parent", groupModule)) {
                        long parentId = rs.getLong("parent_cityobject_id");
                        if (!rs.wasNull() && parentId != 0) {
                            String gmlId = rs.getString("parent_gmlid");

                            if (!exporter.getExportConfig().getCityObjectGroup().isExportMemberAsXLinks()
                                    && !exporter.lookupObjectId(gmlId))
                                continue;

                            if (gmlId != null) {
                                CityObjectGroupParent parent = new CityObjectGroupParent("#" + gmlId);
                                cityObjectGroup.setGroupParent(parent);
                            }
                        }
                    }

                    // delegate export of generic ADE properties
                    if (adeHookTables != null) {
                        List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
                        if (adeHookTables != null)
                            exporter.delegateToADEExporter(adeHookTables, cityObjectGroup, id, featureType, projectionFilter);
                    }

                    isInited = true;
                }

                if (projectionFilter.containsProperty("groupMember", groupModule)) {
                    long groupMemberId = rs.getLong("cityobject_id");
                    if (!rs.wasNull() && groupMemberId != 0) {
                        String gmlId = rs.getString("member_gmlid");

                        if (!exporter.getExportConfig().getCityObjectGroup().isExportMemberAsXLinks()
                                && !exporter.lookupObjectId(gmlId))
                            continue;

                        if (gmlId != null) {
                            CityObjectGroupMember groupMember = new CityObjectGroupMember("#" + gmlId);
                            groupMember.setGroupRole(rs.getString("role"));
                            cityObjectGroup.addGroupMember(groupMember);
                        }
                    }
                }
            }

            return isInited;
        }
    }

    @Override
    public void close() throws SQLException {
        ps.close();
    }

}
