/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodIterator;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

public class DBTrafficArea extends AbstractFeatureExporter<AbstractTransportationObject> {
	private String transportationModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;

	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private Set<String> adeHookTables;

	public DBTrafficArea(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(AbstractTransportationObject.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.TRAFFIC_AREA.getName());
		transportationModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.TRANSPORTATION).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();

		table = new Table(TableEnum.TRAFFIC_AREA.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"), table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", transportationModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", transportationModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", transportationModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));
		if (projectionFilter.containsProperty("surfaceMaterial", transportationModule)) select.addProjection(table.getColumn("surface_material"), table.getColumn("surface_material_codespace"));
		if (projectionFilter.containsProperty("lod2MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod2_multi_surface_id"));
		if (projectionFilter.containsProperty("lod3MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod3_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4MultiSurface", transportationModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.TRAFFIC_AREA);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	@Override
	protected Collection<AbstractTransportationObject> doExport(long id, AbstractTransportationObject root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<AbstractTransportationObject> transportationObjects = new ArrayList<>();

			while (rs.next()) {
				long transportationObjectId = 0;
				AbstractTransportationObject transportationObject = null;
				FeatureType featureType = null;

				if (transportationObjectId == id && root != null) {
					transportationObject = root;
					featureType = rootType;
				} else {
					int objectClassId = rs.getInt("objectclass_id");
					featureType = exporter.getFeatureType(objectClassId);
					if (featureType == null)
						continue;

					// create transportation object
					transportationObject = exporter.createObject(objectClassId, AbstractTransportationObject.class);
					if (transportationObject == null) {
						exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, transportationObjectId) + " as transportation object.");
						continue;
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				cityObjectExporter.doExport(transportationObject, transportationObjectId, featureType, projectionFilter);

				boolean isTrafficArea = transportationObject instanceof TrafficArea;

				if (projectionFilter.containsProperty("class", transportationModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));

						if (isTrafficArea)
							((TrafficArea)transportationObject).setClazz(code);
						else
							((AuxiliaryTrafficArea)transportationObject).setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", transportationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));

						if (isTrafficArea)
							((TrafficArea)transportationObject).addFunction(function);
						else
							((AuxiliaryTrafficArea)transportationObject).addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", transportationModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));

						if (isTrafficArea)
							((TrafficArea)transportationObject).addUsage(usage);
						else
							((AuxiliaryTrafficArea)transportationObject).addUsage(usage);
					}
				}

				if (projectionFilter.containsProperty("surfaceMaterial", transportationModule)) {
					String clazz = rs.getString("surface_material");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
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
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, transportationObject, transportationObjectId, featureType, projectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(transportationObject))
					continue;

				transportationObjects.add(transportationObject);
			}

			return transportationObjects;
		}
	}

}
