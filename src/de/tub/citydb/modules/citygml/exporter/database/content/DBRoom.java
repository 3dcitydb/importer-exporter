/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.database.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.citygml4j.impl.citygml.building.InteriorRoomPropertyImpl;
import org.citygml4j.impl.citygml.building.RoomImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.aggregates.MultiSurfacePropertyImpl;
import org.citygml4j.impl.gml.geometry.primitives.SolidPropertyImpl;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

import de.tub.citydb.util.Util;

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
		psRoom = connection.prepareStatement("select * from ROOM where BUILDING_ID = ?");

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
				long roomId = rs.getLong("ID");
				Room room = new RoomImpl();

				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(room, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					room.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					room.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					room.setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());
					room.setUsage(Arrays.asList(usageList));
				}

				// boundarySurface
				// geometry objects of _BoundarySurface elements have to be referenced by lod4Solid 
				// So we first export all _BoundarySurfaces
				thematicSurfaceExporter.read(room, roomId);
				
				long lodGeometryId = rs.getLong("LOD4_GEOMETRY_ID");
				if (!rs.wasNull() && lodGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodGeometryId);

					if (geometry != null) {
						switch (geometry.getType()) {
						case COMPOSITE_SOLID:
						case SOLID:
							SolidProperty solidProperty = new SolidPropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
							else
								solidProperty.setHref(geometry.getTarget());

							room.setLod4Solid(solidProperty);
							break;
						case MULTI_SURFACE:
							MultiSurfaceProperty multiSurfaceProperty = new MultiSurfacePropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								multiSurfaceProperty.setMultiSurface((MultiSurface)geometry.getAbstractGeometry());
							else
								multiSurfaceProperty.setHref(geometry.getTarget());

							room.setLod4MultiSurface(multiSurfaceProperty);
							break;
						}
					}
				}

				// cityObject stuff
				cityObjectExporter.read(room, roomId);

				// intBuildingInstallation
				buildingInstallationExporter.read(room, roomId);

				// buildingFurniture
				buildingFurnitureExporter.read(room, roomId);

				InteriorRoomProperty roomProperty = new InteriorRoomPropertyImpl();
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
