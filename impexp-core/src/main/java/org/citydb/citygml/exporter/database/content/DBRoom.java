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
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.Room;
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

public class DBRoom extends AbstractFeatureExporter<Room> {
	private DBSurfaceGeometry geometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBuildingInstallation buildingInstallationExporter;
	private DBThematicSurface thematicSurfaceExporter;
	private DBBuildingFurniture buildingFurnitureExporter;

	private String buildingModule;
	private LodFilter lodFilter;
	private AttributeValueSplitter valueSplitter;
	private boolean hasObjectClassIdColumn;
	private Set<String> adeHookTables;

	public DBRoom(Connection connection, CityGMLExportManager exporter) throws CityGMLExportException, SQLException {
		super(Room.class, connection, exporter);

		CombinedProjectionFilter projectionFilter = exporter.getCombinedProjectionFilter(TableEnum.ROOM.getName());
		buildingModule = exporter.getTargetCityGMLVersion().getCityGMLModule(CityGMLModuleType.BUILDING).getNamespaceURI();
		lodFilter = exporter.getLodFilter();
		String schema = exporter.getDatabaseAdapter().getConnectionDetails().getSchema();
		hasObjectClassIdColumn = exporter.getDatabaseAdapter().getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0;

		table = new Table(TableEnum.ROOM.getName(), schema);
		select = new Select().addProjection(table.getColumn("id"));
		if (hasObjectClassIdColumn) select.addProjection(table.getColumn("objectclass_id"));
		if (projectionFilter.containsProperty("class", buildingModule)) select.addProjection(table.getColumn("class"), table.getColumn("class_codespace"));
		if (projectionFilter.containsProperty("function", buildingModule)) select.addProjection(table.getColumn("function"), table.getColumn("function_codespace"));
		if (projectionFilter.containsProperty("usage", buildingModule)) select.addProjection(table.getColumn("usage"), table.getColumn("usage_codespace"));		
		if (projectionFilter.containsProperty("lod4MultiSurface", buildingModule)) select.addProjection(table.getColumn("lod4_multi_surface_id"));
		if (projectionFilter.containsProperty("lod4Solid", buildingModule)) select.addProjection(table.getColumn("lod4_solid_id"));

		// add joins to ADE hook tables
		if (exporter.hasADESupport()) {
			adeHookTables = exporter.getADEHookTables(TableEnum.ROOM);			
			if (adeHookTables != null) addJoinsToADEHookTables(adeHookTables, table);
		}

		cityObjectExporter = exporter.getExporter(DBCityObject.class);
		buildingInstallationExporter = exporter.getExporter(DBBuildingInstallation.class);
		thematicSurfaceExporter = exporter.getExporter(DBThematicSurface.class);
		buildingFurnitureExporter = exporter.getExporter(DBBuildingFurniture.class);
		geometryExporter = exporter.getExporter(DBSurfaceGeometry.class);
		valueSplitter = exporter.getAttributeValueSplitter();
	}

	protected Collection<Room> doExport(AbstractBuilding parent, long parentId) throws CityGMLExportException, SQLException {
		return doExport(parentId, null, null, getOrCreateStatement("building_id"));
	}

	@Override
	protected Collection<Room> doExport(long id, Room root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException {
		ps.setLong(1, id);

		try (ResultSet rs = ps.executeQuery()) {
			List<Room> rooms = new ArrayList<>();

			while (rs.next()) {
				long roomId = rs.getLong("id");
				Room room = null;
				FeatureType featureType = null;

				if (roomId == id && root != null) {
					room = root;
					featureType = rootType;
				} else {
					if (hasObjectClassIdColumn) {
						int objectClassId = rs.getInt("objectclass_id");
						featureType = exporter.getFeatureType(objectClassId);
						if (featureType == null)
							continue;

						// create room object
						room = exporter.createObject(objectClassId, Room.class);
						if (room == null) {
							exporter.logOrThrowErrorMessage("Failed to instantiate " + exporter.getObjectSignature(featureType, roomId) + " as room object.");
							continue;
						}
					} else {
						room = new Room();
						featureType = exporter.getFeatureType(room);
					}
				}

				// get projection filter
				ProjectionFilter projectionFilter = exporter.getProjectionFilter(featureType);

				// export city object information
				cityObjectExporter.doExport(room, roomId, featureType, projectionFilter);

				if (projectionFilter.containsProperty("class", buildingModule)) {
					String clazz = rs.getString("class");
					if (!rs.wasNull()) {
						Code code = new Code(clazz);
						code.setCodeSpace(rs.getString("class_codespace"));
						room.setClazz(code);
					}
				}

				if (projectionFilter.containsProperty("function", buildingModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("function"), rs.getString("function_codespace"))) {
						Code function = new Code(splitValue.result(0));
						function.setCodeSpace(splitValue.result(1));
						room.addFunction(function);
					}
				}

				if (projectionFilter.containsProperty("usage", buildingModule)) {
					for (SplitValue splitValue : valueSplitter.split(rs.getString("usage"), rs.getString("usage_codespace"))) {
						Code usage = new Code(splitValue.result(0));
						usage.setCodeSpace(splitValue.result(1));
						room.addUsage(usage);
					}
				}

				if (lodFilter.isEnabled(4)) {
					// bldg:boundedBy
					if (projectionFilter.containsProperty("boundedBy", buildingModule)) {
						for (AbstractBoundarySurface boundarySurface : thematicSurfaceExporter.doExport(room, roomId))
							room.addBoundedBySurface(new BoundarySurfaceProperty(boundarySurface));
					}

					// bldg:roomInstallation
					for (AbstractCityObject installation : buildingInstallationExporter.doExport(room, roomId, projectionFilter)) {
						if (installation instanceof IntBuildingInstallation)
							room.addRoomInstallation(new IntBuildingInstallationProperty((IntBuildingInstallation)installation));
					}

					// bldg:interiorFurniture
					if (projectionFilter.containsProperty("interiorFurniture", buildingModule)) {
						for (BuildingFurniture furniture : buildingFurnitureExporter.doExport(room, roomId))
							room.addInteriorFurniture(new InteriorFurnitureProperty(furniture));
					}

					// bldg:lod4MultiSurface
					if (projectionFilter.containsProperty("lod4MultiSurface", buildingModule)) {					
						long surfaceGeometryId = rs.getLong("lod4_multi_surface_id");
						if (!rs.wasNull()) {
							SurfaceGeometry geometry = geometryExporter.doExport(surfaceGeometryId);
							if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
								MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
								if (geometry.isSetGeometry())
									multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getGeometry());
								else
									multiSurfaceProperty.setHref(geometry.getReference());

								room.setLod4MultiSurface(multiSurfaceProperty);
							}
						}
					}

					// bldg:lod4Solid
					if (projectionFilter.containsProperty("lod4Solid", buildingModule)) {					
						long solidGeometryId = rs.getLong("lod4_solid_id");
						if (!rs.wasNull()) {
							SurfaceGeometry geometry = geometryExporter.doExport(solidGeometryId);
							if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
								SolidProperty solidProperty = new SolidProperty();
								if (geometry.isSetGeometry())
									solidProperty.setSolid((AbstractSolid)geometry.getGeometry());
								else
									solidProperty.setHref(geometry.getReference());

								room.setLod4Solid(solidProperty);
							}
						}
					}
				}
				
				// delegate export of generic ADE properties
				if (adeHookTables != null) {
					List<String> adeHookTables = retrieveADEHookTables(this.adeHookTables, rs);
					if (adeHookTables != null)
						exporter.delegateToADEExporter(adeHookTables, room, roomId, featureType, projectionFilter);
				}

				// check whether lod filter is satisfied
				if (!exporter.satisfiesLodFilter(room))
					continue;

				rooms.add(room);
			}

			return rooms;
		}
	}

}
