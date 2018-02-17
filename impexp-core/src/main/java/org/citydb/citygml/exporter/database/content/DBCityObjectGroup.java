/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupParent;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;

public class DBCityObjectGroup extends AbstractTypeExporter {
	private final Config config;

	private final PreparedStatement ps;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private GMLConverter gmlConverter;

	private String groupModule;
	private AttributeValueSplitter valueSplitter;
	private boolean hasObjectClassIdColumn;
	private Set<String> adeHookTables;

	public DBCityObjectGroup(Connection connection, CityGMLExportManager exporter, Config config) throws CityGMLExportException, SQLException {
		super(exporter);
		this.config = config;

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.CITYOBJECTGROUP.getName());
		groupModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.CITY_OBJECT_GROUP).getNamespaceURI();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.CITYOBJECTGROUP.getName());
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", groupModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", groupModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", groupModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("geometry", groupModule)) select.addProjection(table.getColumn("brep_id"), exporter.getGeometryColumn(table.getColumn("other_geom")));
		if (projectionFilter.containsProperty("parent", groupModule)) {
			Table cityObject = new Table(TableEnum.CITYOBJECT.getName());
			select.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, table.getColumn("parent_cityobject_id")))
			.addProjection(table.getColumn("parent_cityobject_id"), cityObject.getColumn("gmlid", "parent_gmlid"));
		}
		if (projectionFilter.containsProperty("groupMember", groupModule)) {
			Table cityObject = new Table(TableEnum.CITYOBJECT.getName());
			Table groupToCityObject = new Table(TableEnum.GROUP_TO_CITYOBJECT.getName());
			select.addJoin(JoinFactory.left(groupToCityObject, "cityobjectgroup_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addJoin(JoinFactory.left(cityObject, "id", ComparisonName.EQUAL_TO, groupToCityObject.getColumn("cityobject_id")))
			.addProjection(groupToCityObject.getColumn("cityobject_id"), groupToCityObject.getColumn("role"), cityObject.getColumn("gmlid", "member_gmlid"));
		}
		
		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.CITYOBJECTGROUP);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}

		select.addSelection(ComparisonFactory.equalTo(table.getColumn("id"), new PlaceHolder<>()));
		ps = connection.prepareStatement(select.toString());

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		surfaceGeometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
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
					boolean success = cityObjectExporter.doExport(cityObjectGroup, id, featureType, projectionFilter);
					if (!success)
						return false;
					
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
						Object geometryObj = rs.getObject("other_geom");
						if (geometryId != 0 || geometryObj != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = null;
							if (geometryId != 0) {
								SurfaceGeometry geometry = surfaceGeometryExporter.doExport(geometryId);
								if (geometry != null) {
									geometryProperty = new GeometryProperty<>();
									if (geometry.isSetGeometry())
										geometryProperty.setGeometry(geometry.getGeometry());
									else
										geometryProperty.setHref(geometry.getReference());
								}
							} else {
								GeometryObject geometry = exporter.getDatabaseAdapter().getGeometryConverter().getGeometry(geometryObj);
								if (geometry != null)
									geometryProperty = new GeometryProperty<AbstractGeometry>(gmlConverter.getPointOrCurveGeometry(geometry, true));
							}

							if (geometryProperty != null)
								cityObjectGroup.setGeometry(geometryProperty);
						}
					}

					if (projectionFilter.containsProperty("parent", groupModule)) {
						long parentId = rs.getLong("parent_cityobject_id");
						if (!rs.wasNull() && parentId != 0) {
							String gmlId = rs.getString("parent_gmlid");

							if (!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks()
									&& !exporter.lookupObjectUID(gmlId))
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

						if (!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks()
								&& !exporter.lookupObjectUID(gmlId))
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
