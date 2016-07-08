/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.citydb.util.Util;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBRoom implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Connection connection;

	private PreparedStatement psRoom;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBuildingInstallation buildingInstallationExporter;
	private DBThematicSurface thematicSurfaceExporter;
	private DBBuildingFurniture buildingFurnitureExporter;

	public DBRoom(Connection connection, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder query = new StringBuilder()
		.append("select ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD4_MULTI_SURFACE_ID, LOD4_SOLID_ID ")
		.append("from ROOM where BUILDING_ID = ?");
		psRoom = connection.prepareStatement(query.toString());

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		buildingInstallationExporter = (DBBuildingInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_INSTALLATION);
		thematicSurfaceExporter = (DBThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.THEMATIC_SURFACE);
		buildingFurnitureExporter = (DBBuildingFurniture)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_FURNITURE);
	}

	public void read(AbstractBuilding building, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psRoom.setLong(1, parentId);
			rs = psRoom.executeQuery();

			while (rs.next()) {
				long roomId = rs.getLong(1);
				Room room = new Room();

				String clazz = rs.getString(2);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(3));
					room.setClazz(code);
				}

				String function = rs.getString(4);
				String functionCodeSpace = rs.getString(5);
				if (function != null)
					room.setFunction(Util.string2codeList(function, functionCodeSpace));

				String usage = rs.getString(6);
				String usageCodeSpace = rs.getString(7);
				if (usage != null)
					room.setUsage(Util.string2codeList(usage, usageCodeSpace));

				// boundarySurface
				// geometry objects of _BoundarySurface elements have to be referenced by lod4Solid / lod4MultiSurface
				// So we first export all _BoundarySurfaces
				thematicSurfaceExporter.read(room, roomId);

				// lod4MultiSurface
				long multiSurfaceGeometryId = rs.getLong(8);
				if (!rs.wasNull() && multiSurfaceGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(multiSurfaceGeometryId);
					if (geometry != null && geometry.getType() == GMLClass.MULTI_SURFACE) {
						MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty();
						if (geometry.getAbstractGeometry() != null)
							multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
						else
							multiSurfaceProperty.setHref(geometry.getTarget());

						room.setLod4MultiSurface(multiSurfaceProperty);
					}
				}

				// lod4Solid
				long solidGeometryId = rs.getLong(9);
				if (!rs.wasNull() && solidGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(solidGeometryId);
					if (geometry != null && (geometry.getType() == GMLClass.SOLID || geometry.getType() == GMLClass.COMPOSITE_SOLID)) {
						SolidProperty solidProperty = new SolidProperty();
						if (geometry.getAbstractGeometry() != null)
							solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
						else
							solidProperty.setHref(geometry.getTarget());

						room.setLod4Solid(solidProperty);
					}
				}

				// cityObject stuff
				cityObjectExporter.read(room, roomId);

				// intBuildingInstallation
				buildingInstallationExporter.read(room, roomId);

				// buildingFurniture
				buildingFurnitureExporter.read(room, roomId);

				InteriorRoomProperty roomProperty = new InteriorRoomProperty();
				roomProperty.setObject(room);
				building.addInteriorRoom(roomProperty);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psRoom.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.ROOM;
	}

}
