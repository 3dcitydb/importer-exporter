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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;

public class DBTransportationComplex extends AbstractFeatureExporter<TransportationComplex> {
	private String transportationModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;

	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private GMLConverter gmlConverter;
	private Set<String> complexADEHookTables;
	private Set<String> objectADEHookTables;

	public DBTransportationComplex(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(TransportationComplex.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TRANSPORTATION_COMPLEX.getName());
		CombinedProjectionFilter trafficAreaProjectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TRAFFIC_AREA.getName());
		transportationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.TRANSPORTATION_COMPLEX.getName(), schema);
		Table trafficArea = new Table(TableEnum.TRAFFIC_AREA.getName(), schema);

		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", transportationModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", transportationModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", transportationModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("lod0Network", transportationModule)) select.addProjection(exporter.getGeometryColumn(table.getColumn("lod0_network")));
		if (projectionFilter.containsProperty("lod1MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod1_multi_surface_id"));
		if (projectionFilter.containsProperty("lod2MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("trafficArea", transportationModule)
				|| projectionFilter.containsProperty("auxiliaryTrafficArea", transportationModule)) {
			select.addJoin(JoinFactory.left(trafficArea, "transportation_complex_id", ComparisonName.EQUAL_TO, table.getColumn("id")))
			.addProjection(trafficArea.getColumn("id", "ta_id"), trafficArea.getColumn("objectclass_id", "ta_objectclass_id"));
			if (trafficAreaProjectionFilter.containsProperty("class", transportationModule)) select.addProjection(trafficArea.getColumn("class", "ta_class"), trafficArea.getColumn("class_codespace", "ta_class_codespace"));
			if (trafficAreaProjectionFilter.containsProperty("function", transportationModule)) select.addProjection(trafficArea.getColumn("function", "ta_function"), trafficArea.getColumn("function_codespace", "ta_function_codespace"));
			if (trafficAreaProjectionFilter.containsProperty("usage", transportationModule)) select.addProjection(trafficArea.getColumn("usage", "ta_usage"), trafficArea.getColumn("usage_codespace", "ta_usage_codespace"));
			if (trafficAreaProjectionFilter.containsProperty("surfaceMaterial", transportationModule)) select.addProjection(trafficArea.getColumn("surface_material"), trafficArea.getColumn("surface_material_codespace"));
			if (trafficAreaProjectionFilter.containsProperty("lod2MultiSurface", transportationModule)) select.addProjection(trafficArea.getColumn("lod2_multi_surface_id", "ta_lod2_multi_surface_id"));
			if (trafficAreaProjectionFilter.containsProperty("lod3MultiSurface", transportationModule)) select.addProjection(trafficArea.getColumn("lod3_multi_surface_id", "ta_lod3_multi_surface_id"));
			if (trafficAreaProjectionFilter.containsProperty("lod4MultiSurface", transportationModule)) select.addProjection(trafficArea.getColumn("lod4_multi_surface_id", "ta_lod4_multi_surface_id"));
		}

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			complexADEHookTables = exporter.getADEHookTables(TableEnum.TRANSPORTATION_COMPLEX);
			objectADEHookTables = exporter.getADEHookTables(TableEnum.TRAFFIC_AREA);			
			if (complexADEHookTables != null) addJoinsToADEHookTables(complexADEHookTables, table);
			if (objectADEHookTables != null) addJoinsToADEHookTables(objectADEHookTables, trafficArea);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	@Override
	protected Collection<TransportationComplex> doExport(long id, TransportationComplex root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			long currentComplexId = 0;
			TransportationComplex complex = null;
			ProjectionFilter projectionFilter = null;
			HashMap<Long, TransportationComplex> complexes = new HashMap<>();

			while (rs.next()) {
				long complexId = rs.getLong("id");

				if (complexId != currentComplexId) {
					currentComplexId = complexId;

					complex = complexes.get(complexId);
					if (complex == null) {
						FeatureType featureType = null;						
						if (complexId == id && root != null) {
							complex = root;
							featureType = rootType;
						} else {
							int objectClassId = rs.getInt("objectclass_id");
							featureType = exporter.getFeatureType(objectClassId);
							if (featureType == null)
								continue;

							// create transportation complex object
							complex = exporter.createObject(objectClassId, TransportationComplex.class);
							if (complex == null) {
								exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, complexId) + " as transportation complex object.");
								continue;
							}
						}

						// get projection filter
						projectionFilter = exporter.getProjectionFilter(featureType);

						// export city object information
						boolean success = cityObjectExporter.doExport(complex, complexId, featureType, projectionFilter);
						if (!success) {
							if (complex == root)
								return Collections.emptyList();
							else if (featureType.isSetTopLevel())
								continue;
						}

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

							if (!projectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiSurface").toString(), transportationModule))
								continue;

							long surfaceGeometryId = rs.getLong(new StringBuilder("lod").append(lod).append("_multi_surface_id").toString());
							if (rs.wasNull())
								continue;

							SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
								if (geometry.isSetGeometry())
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getReference());

								switch (lod) {
								case 1:
									complex.setLod1MultiSurface(multiSurfaceProperty);
									break;
								case 2:
									complex.setLod2MultiSurface(multiSurfaceProperty);
									break;
								case 3:
									complex.setLod3MultiSurface(multiSurfaceProperty);
									break;
								case 4:
									complex.setLod4MultiSurface(multiSurfaceProperty);
									break;
								}
							}
						}
						
						// delegate export of generic ADE properties
						if (complexADEHookTables != null) {
							List<String> adeHookTables = retrieveADEHookTables(complexADEHookTables, rs);
							if (adeHookTables != null)
								exporter.delegateToADEExporter(adeHookTables, complex, complexId, featureType, projectionFilter);
						}

						complex.setLocalProperty("projection", projectionFilter);
						complexes.put(complexId, complex);
					} else
						projectionFilter = (ProjectionFilter)complex.getLocalProperty("projection");
				}

				// continue if traffic areas shall not be exported
				if (!lodFilter.containsLodGreaterThanOrEuqalTo(2)
						|| (!projectionFilter.containsProperty("trafficArea", transportationModule) 
						&& !projectionFilter.containsProperty("auxiliaryTrafficArea", transportationModule)))
					continue;

				long transportationObjectId = rs.getLong("ta_id");
				if (rs.wasNull())
					continue;

				// create new traffic area object
				int objectClassId = rs.getInt("ta_objectclass_id");
				AbstractTransportationObject transportationObject = exporter.createObject(objectClassId, AbstractTransportationObject.class);
				if (transportationObject == null) {
					exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(objectClassId, transportationObjectId) + " as transportation object.");
					continue;
				}

				// get projection filter
				FeatureType transportationObjectType = exporter.getFeatureType(objectClassId);
				ProjectionFilter transportationObjectProjectionFilter = exporter.getProjectionFilter(transportationObjectType);

				// cityobject stuff
				cityObjectExporter.doExport(transportationObject, transportationObjectId, transportationObjectType, transportationObjectProjectionFilter);

				boolean isTrafficArea = transportationObject instanceof TrafficArea;

				if (transportationObjectProjectionFilter.containsProperty("class", transportationModule)) {
					String clazz = rs.getString("ta_class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("ta_class_codespace"));

						if (isTrafficArea)
							((TrafficArea)transportationObject).setClazz(code);
						else
							((AuxiliaryTrafficArea)transportationObject).setClazz(code);
					}
				}

				if (transportationObjectProjectionFilter.containsProperty("function", transportationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("ta_function"), rs.getString("ta_function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));

						if (isTrafficArea)
							((TrafficArea)transportationObject).addFunction(function);
						else
							((AuxiliaryTrafficArea)transportationObject).addFunction(function);
					}
				}

				if (transportationObjectProjectionFilter.containsProperty("usage", transportationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("ta_usage"), rs.getString("ta_usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));

						if (isTrafficArea)
							((TrafficArea)transportationObject).addUsage(usage);
						else
							((AuxiliaryTrafficArea)transportationObject).addUsage(usage);
					}
				}

				if (transportationObjectProjectionFilter.containsProperty("surfaceMaterial", transportationModule)) {
					String surfaceMaterial = rs.getString("surface_material");
					if (!rs.wasNull()) {
						Code code = new Code(surfaceMaterial);
						code.setCodeSpace(rs.getString("surface_material_codespace"));

						if (isTrafficArea)
							((TrafficArea)transportationObject).setSurfaceMaterial(code);
						else
							((AuxiliaryTrafficArea)transportationObject).setSurfaceMaterial(code);
					}
				}

				LodIterator lodIterator = lodFilter.iterator(2, 4);
				while (lodIterator.hasNext()) {
					int lod = lodIterator.next();

					if (!transportationObjectProjectionFilter.containsProperty(new StringBuilder("lod").append(lod).append("MultiSurface").toString(), transportationModule))
						continue;

					long surfaceGeometryId = rs.getLong(new StringBuilder("ta_lod").append(lod).append("_multi_surface_id").toString());
					if (rs.wasNull())
						continue;

					SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.isSetGeometry())
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getReference());

						switch (lod) {
						case 2:
							if (isTrafficArea)
								((TrafficArea)transportationObject).setLod2MultiSurface(multiSurfaceProperty);
							else
								((AuxiliaryTrafficArea)transportationObject).setLod2MultiSurface(multiSurfaceProperty);
							break;
						case 3:
							if (isTrafficArea)
								((TrafficArea)transportationObject).setLod3MultiSurface(multiSurfaceProperty);
							else
								((AuxiliaryTrafficArea)transportationObject).setLod3MultiSurface(multiSurfaceProperty);
							break;
						case 4:
							if (isTrafficArea)
								((TrafficArea)transportationObject).setLod4MultiSurface(multiSurfaceProperty);
							else
								((AuxiliaryTrafficArea)transportationObject).setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (objectADEHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(objectADEHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, transportationObject, transportationObjectId, transportationObjectType, transportationObjectProjectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(transportationObject))
					continue;

				if (transportationObject instanceof TrafficArea) {
					TrafficAreaProperty property = new TrafficAreaProperty((TrafficArea)transportationObject);
					complex.addTrafficArea(property);
				} else {
					AuxiliaryTrafficAreaProperty property  = new AuxiliaryTrafficAreaProperty((AuxiliaryTrafficArea)transportationObject);
					complex.addAuxiliaryTrafficArea(property);
				}
			}

			return complexes.values();
		}
	}

}
