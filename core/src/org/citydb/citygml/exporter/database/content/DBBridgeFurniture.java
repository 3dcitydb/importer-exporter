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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.util.AttributeValueSplitter;
import org.citydb.citygml.exporter.util.AttributeValueSplitter.SplitValue;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.schema.TableEnum;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import vcs.sqlbuilder.schema.Table;
import vcs.sqlbuilder.select.Select;

public class DBBridgeFurniture extends AbstractFeatureExporter<BridgeFurniture> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBImplicitGeometry implicitGeometryExporter;
	private GMLConverter gmlConverter;

	private String bridgeModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;
	private boolean hasObjectClassIdColumn;
	private Set<String> adeHookTables;

	public DBBridgeFurniture(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(BridgeFurniture.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_FURNITURE.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.BRIDGE_FURNITURE.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));		
		if (projectionFilter.containsProperty("lod4Geometry", bridgeModule)) select.addProjection(table.getColumn("lod4_brep_id"), exporter.getGeometryColumn(table.getColumn("lod4_other_geom")));
		if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule))
			select.addProjection(table.getColumn("lod4_implicit_rep_id"), exporter.getGeometryColumn(table.getColumn("lod4_implicit_ref_point")), table.getColumn("lod4_implicit_transformation"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.BRIDGE_FURNITURE);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}
		
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		implicitGeometryExporter = exporter.getExporter(DBImplicitGeometry.class);
		gmlConverter = exporter.getGMLConverter();
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Collection<BridgeFurniture> doExport(BridgeRoom parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("bridge_room_id"));
	}
	
	@Override
	protected Collection<BridgeFurniture> doExport(long id, BridgeFurniture root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<BridgeFurniture> bridgeFurnitures = new ArrayList<>();
			
			while (rs.next()) {
				long bridgeFurnitureId = rs.getLong("id");
				BridgeFurniture bridgeFurniture = null;
				FeatureType featureType = null;
				
				if (bridgeFurnitureId == id && root != null) {
					bridgeFurniture = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create bridge furniture object
						bridgeFurniture = exporter.createObject(objectClassId, BridgeFurniture.class);
						if (bridgeFurniture == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, bridgeFurnitureId) + " as bridge furniture object.");
							continue;
						}
					} else {
						bridgeFurniture = new BridgeFurniture();
						featureType = exporter.getFeatureType(bridgeFurniture);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectExporter.doExport(bridgeFurniture, bridgeFurnitureId, featureType, projectionFilter);
				
				if (projectionFilter.containsProperty("class", bridgeModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						bridgeFurniture.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						bridgeFurniture.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						bridgeFurniture.addUsage(usage);
					}
				}
				

				if (lodFilter.isEnabled(4)) {
					if (projectionFilter.containsProperty("lod4Geometry", bridgeModule)) {					
						long geometryId = rs.getLong("lod4_brep_id");
						Object geometryObj = rs.getObject("lod4_other_geom");
						if (geometryId != 0 || geometryObj != null) {
							GeometryProperty<AbstractGeometry> geometryProperty = null;
							if (geometryId != 0) {
								SurfaceGeometry geometry = geometryExporter.doExport(geometryId);
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
									geometryProperty = new GeometryProperty<>(gmlConverter.getPointOrCurveGeometry(geometry, true));
							}

							if (geometryProperty != null)
								bridgeFurniture.setLod4Geometry(geometryProperty);
						}
					}

					if (projectionFilter.containsProperty("lod4ImplicitRepresentation", bridgeModule)) {					
						long implicitGeometryId = rs.getLong("lod4_implicit_rep_id");
						if (!rs.wasNull()) {
							GeometryObject referencePoint = null;
							Object referencePointObj = rs.getObject("lod4_implicit_ref_point");
							if (!rs.wasNull())
								referencePoint = exporter.getDatabaseAdapter().getGeometryConverter().getPoint(referencePointObj);

							String transformationMatrix = rs.getString("lod4_implicit_transformation");

							ImplicitGeometry implicit = implicitGeometryExporter.doExport(implicitGeometryId, referencePoint, transformationMatrix);
							if (implicit != null) {
								ImplicitRepresentationProperty implicitProperty = new ImplicitRepresentationProperty();
								implicitProperty.setObject(implicit);
								bridgeFurniture.setLod4ImplicitRepresentation(implicitProperty);
							}
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, bridgeFurniture, bridgeFurnitureId, featureType, projectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(bridgeFurniture))
					continue;				

				bridgeFurnitures.add(bridgeFurniture);
			}
			
			return bridgeFurnitures;
		}
	}

}
