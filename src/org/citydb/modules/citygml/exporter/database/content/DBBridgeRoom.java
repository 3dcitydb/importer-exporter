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
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.InteriorBridgeRoomProperty;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;

public class DBBridgeRoom implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Connection connection;

	private PreparedStatement psBridgeRoom;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBridgeInstallation bridgeInstallationExporter;
	private DBBridgeThematicSurface thematicSurfaceExporter;
	private DBBridgeFurniture bridgeFurnitureExporter;

	public DBBridgeRoom(Connection connection, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		StringBuilder query = new StringBuilder()
		.append("select ID, CLASS, CLASS_CODESPACE, FUNCTION, FUNCTION_CODESPACE, USAGE, USAGE_CODESPACE, ")
		.append("LOD4_MULTI_SURFACE_ID, LOD4_SOLID_ID ")
		.append("from BRIDGE_ROOM where BRIDGE_ID = ?");
		psBridgeRoom = connection.prepareStatement(query.toString());

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		bridgeInstallationExporter = (DBBridgeInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_INSTALLATION);
		thematicSurfaceExporter = (DBBridgeThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_THEMATIC_SURFACE);
		bridgeFurnitureExporter = (DBBridgeFurniture)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE_FURNITURE);
	}

	public void read(AbstractBridge bridge, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psBridgeRoom.setLong(1, parentId);
			rs = psBridgeRoom.executeQuery();

			while (rs.next()) {
				long roomId = rs.getLong(1);
				BridgeRoom bridgeRoom = new BridgeRoom();

				String clazz = rs.getString(2);
				if (clazz != null) {
					Code code = new Code(clazz);
					code.setCodeSpace(rs.getString(3));
					bridgeRoom.setClazz(code);
				}

				String function = rs.getString(4);
				String functionCodeSpace = rs.getString(5);
				if (function != null)
					bridgeRoom.setFunction(Util.string2codeList(function, functionCodeSpace));

				String usage = rs.getString(6);
				String usageCodeSpace = rs.getString(7);
				if (usage != null)
					bridgeRoom.setUsage(Util.string2codeList(usage, usageCodeSpace));

				// boundarySurface
				// geometry objects of _BoundarySurface elements have to be referenced by lod4Solid / lod4MultiSurface
				// So we first export all _BoundarySurfaces
				thematicSurfaceExporter.read(bridgeRoom, roomId);

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

						bridgeRoom.setLod4MultiSurface(multiSurfaceProperty);
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

						bridgeRoom.setLod4Solid(solidProperty);
					}
				}

				// cityObject stuff
				cityObjectExporter.read(bridgeRoom, roomId);

				// intBridgeInstallation
				bridgeInstallationExporter.read(bridgeRoom, roomId);

				// bridgeFurniture
				bridgeFurnitureExporter.read(bridgeRoom, roomId);

				InteriorBridgeRoomProperty roomProperty = new InteriorBridgeRoomProperty();
				roomProperty.setObject(bridgeRoom);
				bridge.addInteriorBridgeRoom(roomProperty);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psBridgeRoom.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BRIDGE_ROOM;
	}

}
