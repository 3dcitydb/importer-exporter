/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.RasterRelief;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBReliefFeature extends AbstractFeatureExporter<ReliefFeature> {
	private GMLConverter gmlConverter;
	private final String reliefModule;
	private final LodFilter lodFilter;
	private final boolean hasObjectClassIdColumn;
	private final boolean useXLink;
	private final DBCityObject cityObjectExporter;

	private DBSurfaceGeometry geometryExporter;
	private Set<String> reliefADEHookTables;
	private Set<String> componentADEHookTables;

	public DBReliefFeature(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(ReliefFeature.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.RELIEF_FEATURE.getName());
		CombinedProjectionFilter componentProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.RELIEF_COMPONENT.getName());
		reliefModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.RELIEF).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;
		useXLink = exporter.getExportConfig().getXlink().getFeature().isModeXLink();

		table = new Table(TableEnum.RELIEF_FEATURE.getName(), schema);
		Table reliefComponent = new Table(TableEnum.RELIEF_COMPONENT.getName(), schema);

		select = new Select().addProjection(table.getColumn("id"), table.getColumn("lod", "rf_lod"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("reliefComponent", reliefModule)) {
			Table reliefFeatToRelComp = new Table(TableEnum.RELIEF_FEAT_TO_REL_COMP.getName(), schema);
			Table tinRelief = new Table(TableEnum.TIN_RELIEF.getName(), schema);
			Table massPointRelief = new Table(TableEnum.MASSPOINT_RELIEF.getName(), schema);
			Table breakLineRelief = new Table(TableEnum.BREAKLINE_RELIEF.getName(), schema);
			select.addJoin(JoinFactory.inner(reliefFeatToRelComp, "relief_feature_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addJoin(JoinFactory.inner(reliefComponent, "id", ComparisonName.EQUAL_TO, reliefFeatToRelComp.getColumn("relief_component_id")))
			.addJoin(JoinFactory.left(tinRelief, "id", ComparisonName.EQUAL_TO, reliefComponent.getColumn("id")))
			.addJoin(JoinFactory.left(massPointRelief, "id", ComparisonName.EQUAL_TO, reliefComponent.getColumn("id")))
			.addJoin(JoinFactory.left(breakLineRelief, "id", ComparisonName.EQUAL_TO, reliefComponent.getColumn("id")))
			.addProjection(reliefComponent.getColumn("id", "rc_id"), reliefComponent.getColumn("objectclass_id", "rc_objectclass_id"), reliefComponent.getColumn("lod", "rc_lod"));
			if (componentProjectionFilter.containsProperty("extent", reliefModule)) select.addProjection(reliefComponent.getColumn("extent"));
			if (componentProjectionFilter.containsProperty("tin", reliefModule)) select.addProjection(tinRelief.getColumn("max_length"), tinRelief.getColumn("max_length_unit"),
					exporter.getGeometryColumn(tinRelief.getColumn("stop_lines")), exporter.getGeometryColumn(tinRelief.getColumn("break_lines")),
					exporter.getGeometryColumn(tinRelief.getColumn("control_points")), tinRelief.getColumn("surface_geometry_id"));
			if (componentProjectionFilter.containsProperty("reliefPoints", reliefModule)) select.addProjection(exporter.getGeometryColumn(massPointRelief.getColumn("relief_points")));
			if (componentProjectionFilter.containsProperty("ridgeOrValleyLines", reliefModule)) select.addProjection(exporter.getGeometryColumn(breakLineRelief.getColumn("ridge_or_valley_lines")));
			if (componentProjectionFilter.containsProperty("breaklines", reliefModule)) select.addProjection(exporter.getGeometryColumn(breakLineRelief.getColumn("break_lines")));

			geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
			gmlConverter = exporter.getGMLConverter();
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			reliefADEHookTables = exporter.getADEHookTables(TableEnum.BRIDGE);
			componentADEHookTables = exporter.getADEHookTables(TableEnum.RELIEF_COMPONENT);			
			if (reliefADEHookTables != null) addJoinsToADEHookTables(reliefADEHookTables, table);
			if (componentADEHookTables != null) addJoinsToADEHookTables(componentADEHookTables, reliefComponent);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
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
								int objectClassId = rs.getInt("objectclass_id");
								featureType = exporter.getFeatureType(objectClassId);
								if (featureType == null)
									continue;

								// create relief feature object
								reliefFeature = exporter.createObject(featureType.getObjectClassId(), ReliefFeature.class);						
								if (reliefFeature == null) {
									exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, reliefFeatureId) + " as relief feature object.");
									continue;
								}
							} else {
								reliefFeature = new ReliefFeature();
								featureType = exporter.getFeatureType(reliefFeature);
							}
						}

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						boolean success = cityObjectExporter.doExport(reliefFeature, reliefFeatureId, featureType, projectionFilter);
						if (!success) {
							if (reliefFeature == root)
								return Collections.emptyList();
							else if (featureType.isSetTopLevel())
								continue;
						}

						int lod = rs.getInt("rf_lod");
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
						projectionFilter = (ProjectionFilter)reliefFeature.getLocalProperty("projection");
				}

				if (!projectionFilter.containsProperty("reliefComponent", reliefModule))
					continue;

				long componentId = rs.getLong("rc_id");
				if (rs.wasNull())
					continue;

				// create new relief component object
				int objectClassId = rs.getInt("rc_objectclass_id");
				AbstractReliefComponent component = exporter.createObject(objectClassId, AbstractReliefComponent.class);
				if (component == null) {
					exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, componentId) + " as relief component object.");
					continue;
				}

				// get projection filter
				FeatureType componentType = exporter.getFeatureType(objectClassId);
				ProjectionFilter componentProjectionFilter = exporter.getProjectionFilter(componentType);

				// export city object information
				cityObjectExporter.doExport(component, componentId, componentType, componentProjectionFilter);

				if (component.isSetId()) {
					// process xlink
					if (exporter.lookupAndPutObjectUID(component.getId(), componentId, objectClassId)) {
						if (useXLink) {
							ReliefComponentProperty property = new ReliefComponentProperty();
							property.setHref("#" + component.getId());
							reliefFeature.addReliefComponent(property);
							continue;
						} else
							component.setId(exporter.generateNewGmlId(component));
					}
				}

				component.setLod(rs.getInt("rc_lod"));

				if (componentProjectionFilter.containsProperty("extent", reliefModule)) {
					Object extentObj = rs.getObject("extent");
					if (!rs.wasNull()) {
						GeometryObject extent = exporter.getDatabaseAdapter().getGeometryConverter().getPolygon(extentObj);
						if (extent != null)
							component.setExtent(gmlConverter.getPolygonProperty(extent, false));
					}
				}

				// retrieve further content according to the component types
				if (component instanceof TINRelief && componentProjectionFilter.containsProperty("tin", reliefModule)) {
					TINRelief tinRelief = (TINRelief)component;

					// create gml:TriangulatedSurface
					TriangulatedSurface triangulatedSurface = null;
					long surfaceGeometryId = rs.getLong("surface_geometry_id");
					if (!rs.wasNull()) {
						SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
						if (geometry != null && geometry.getType() == GMLClass.TRIANGULATED_SURFACE && geometry.isSetGeometry()) 
							triangulatedSurface = (TriangulatedSurface)geometry.getGeometry();					
					}

					// triangle patches are mandatory
					if (triangulatedSurface == null)
						continue;

					Double maxLength = rs.getDouble("max_length");
					if (rs.wasNull())
						maxLength = null;

					GeometryObject stopLines = null;
					Object stopLinesObj = rs.getObject("stop_lines");
					if (!rs.wasNull())
						stopLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(stopLinesObj);

					GeometryObject breakLines = null;
					Object breakLinesObj = rs.getObject("break_lines");
					if (!rs.wasNull())
						breakLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);

					GeometryObject controlPoints = null;
					Object controlPointsObj = rs.getObject("control_points");
					if (!rs.wasNull())
						controlPoints = exporter.getDatabaseAdapter().getGeometryConverter().getMultiPoint(controlPointsObj);

					// check whether we deal with gml:Tin instead
					if (maxLength != null || stopLines != null || breakLines != null || controlPoints != null) {
						// control points are mandatory
						if (controlPoints == null)
							continue;

						TrianglePatchArrayProperty patches = triangulatedSurface.getTrianglePatches();
						triangulatedSurface = new Tin();
						triangulatedSurface.setTrianglePatches(patches);

						Tin tin = (Tin)triangulatedSurface;
						if (maxLength != null) {
							Length length = new Length(maxLength);
							length.setUom(rs.getString("max_length_unit"));
							tin.setMaxLength(length);
						}

						if (stopLines != null) {
							List<LineStringSegmentArrayProperty> property = gmlConverter.getListOfLineStringSegmentArrayProperty(stopLines, false);
							if (property != null)
								tin.setStopLines(property);
						}

						if (breakLines != null) {
							List<LineStringSegmentArrayProperty> property = gmlConverter.getListOfLineStringSegmentArrayProperty(breakLines, false);
							if (property != null)
								tin.setBreakLines(property);
						}

						tin.setControlPoint(gmlConverter.getControlPoint(controlPoints, false));
					}

					tinRelief.setTin(new TinProperty(triangulatedSurface));
				}

				else if (component instanceof MassPointRelief && componentProjectionFilter.containsProperty("reliefPoints", reliefModule)) {
					MassPointRelief massPointRelief = (MassPointRelief)component;

					Object reliefPointsObj = rs.getObject("relief_points");
					if (!rs.wasNull()) {
						GeometryObject reliefPoints = exporter.getDatabaseAdapter().getGeometryConverter().getMultiPoint(reliefPointsObj);
						if (reliefPoints != null)
							massPointRelief.setReliefPoints(gmlConverter.getMultiPointProperty(reliefPoints, false));
					}
				}

				else if (component instanceof BreaklineRelief) {
					BreaklineRelief breaklineRelief = (BreaklineRelief)component;

					if (componentProjectionFilter.containsProperty("ridgeOrValleyLines", reliefModule)) {
						Object ridgeOrValleyLinesObj = rs.getObject("ridge_or_valley_lines");
						if (!rs.wasNull()) {
							GeometryObject ridgeOrValleyLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(ridgeOrValleyLinesObj);
							if (ridgeOrValleyLines != null)					
								breaklineRelief.setRidgeOrValleyLines(gmlConverter.getMultiCurveProperty(ridgeOrValleyLines, false));
						}
					}

					if (componentProjectionFilter.containsProperty("breaklines", reliefModule)) {
						Object breakLinesObj = rs.getObject("break_lines");
						if (!rs.wasNull()) {
							GeometryObject breakLines = exporter.getDatabaseAdapter().getGeometryConverter().getMultiCurve(breakLinesObj);
							if (breakLines != null)					
								breaklineRelief.setBreaklines(gmlConverter.getMultiCurveProperty(breakLines, false));
						}
					}
				}

				else if (component instanceof RasterRelief)
					exporter.logOrThrowErrorMessage(exporter.getObjectSignature(componentType, componentId) + ": Raster reliefs are not supported.");
				
				// delegate export of generic ADE properties
				if (componentADEHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(componentADEHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, component, componentId, componentType, componentProjectionFilter);
				}

				ReliefComponentProperty property = new ReliefComponentProperty(component);
				reliefFeature.addReliefComponent(property);
			}

			return reliefFeatures.values();
		}
	}

}
