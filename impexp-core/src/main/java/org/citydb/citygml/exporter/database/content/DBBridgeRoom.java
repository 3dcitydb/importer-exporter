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
import org.citydb.query.filter.projection.CombinedProjectionFilter;
import org.citydb.query.filter.projection.ProjectionFilter;
import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;

public class DBBridgeRoom extends AbstractFeatureExporter<BridgeRoom> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBridgeInstallation bridgeInstallationExporter;
	private DBBridgeThematicSurface thematicSurfaceExporter;
	private DBBridgeFurniture bridgeFurnitureExporter;

	private String bridgeModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;
	private boolean hasObjectClassIdColumn;
	private Set<String> adeHookTables;

	public DBBridgeRoom(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(BridgeRoom.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.BRIDGE_ROOM.getName());
		bridgeModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BRIDGE).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.BRIDGE_ROOM.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", bridgeModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", bridgeModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", bridgeModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));		
		if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) select.addProjection(table.getColumn("lod4_solid_id"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.BRIDGE_ROOM);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}
		
		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		bridgeInstallationExporter = exporter.getExporter(DBBridgeInstallation.class);
		thematicSurfaceExporter = exporter.getExporter(DBBridgeThematicSurface.class);
		bridgeFurnitureExporter = exporter.getExporter(DBBridgeFurniture.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Collection<BridgeRoom> doExport(AbstractBridge parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("bridge_id"));
	}
	
	@Override
	protected Collection<BridgeRoom> doExport(long id, BridgeRoom root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<BridgeRoom> bridgeRooms = new ArrayList<>();

			while (rs.next()) {
				long bridgeRoomId = rs.getLong("id");
				BridgeRoom bridgeRoom = null;
				FeatureType featureType = null;
				
				if (bridgeRoomId == id && root != null) {
					bridgeRoom = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create bridge room object
						bridgeRoom = exporter.createObject(objectClassId, BridgeRoom.class);
						if (bridgeRoom == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, bridgeRoomId) + " as bridge room object.");
							continue;
						}
					} else {
						bridgeRoom = new BridgeRoom();
						featureType = exporter.getFeatureType(bridgeRoom);
					}
				}
				
				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);
				
				// export city object information
				cityObjectExporter.doExport(bridgeRoom, bridgeRoomId, featureType, projectionFilter);

				if (projectionFilter.containsProperty("class", bridgeModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						bridgeRoom.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						bridgeRoom.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", bridgeModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						bridgeRoom.addUsage(usage);
					}
				}

				if (lodFilter.isEnabled(4)) {
					// brid:boundedBy
					if (projectionFilter.containsProperty("boundedBy", bridgeModule)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(bridgeRoom, bridgeRoomId))
							bridgeRoom.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					}

					// brid:bridgeRoomInstallation
					for (AbstractCityObject installation : bridgeInstallationExporter.doExport(bridgeRoom, bridgeRoomId, projectionFilter)) {
						if (installation instanceof IntBridgeInstallation)
							bridgeRoom.addBridgeRoomInstallation(new IntBridgeInstallationProperty((IntBridgeInstallation)installation));
					}

					// brid:interiorFurniture
					if (projectionFilter.containsProperty("interiorFurniture", bridgeModule)) {
						for (BridgeFurniture furniture : bridgeFurnitureExporter.doExport(bridgeRoom, bridgeRoomId))
							bridgeRoom.addInteriorFurniture(new InteriorFurnitureProperty(furniture));
					}
					
					// brid:lod4MultiSurface
					if (projectionFilter.containsProperty("lod4MultiSurface", bridgeModule)) {					
						long surfaceGeometryId = rs.getLong("lod4_multi_surface_id");
						if (!rs.wasNull()) {
							SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
								if (geometry.isSetGeometry())
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getReference());

								bridgeRoom.setLod4MultiSurface(multiSurfaceProperty);
							}
						}
					}

					// brid:lod4Solid
					if (projectionFilter.containsProperty("lod4Solid", bridgeModule)) {					
						long solidGeometryId = rs.getLong("lod4_solid_id");
						if (!rs.wasNull()) {
							SurfaceGeometry geometry = geometryExporter.doExport(solidGeometryId);
							if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
								SolidProperty solidProperty = new SolidProperty();
								if (geometry.isSetGeometry())
									solidProperty.setSolid((AbstractSolid)geometry.getGeometry());
								else
									solidProperty.setHref(geometry.getReference());

								bridgeRoom.setLod4Solid(solidProperty);
							}
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, bridgeRoom, bridgeRoomId, featureType, projectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(bridgeRoom))
					continue;

				bridgeRooms.add(bridgeRoom);
			}
			
			return bridgeRooms;
		}
	}

}
