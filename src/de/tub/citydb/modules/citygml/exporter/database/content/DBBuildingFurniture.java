/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

import org.citygml4j.impl.citygml.building.BuildingFurnitureImpl;
import org.citygml4j.impl.citygml.building.InteriorFurniturePropertyImpl;
import org.citygml4j.impl.gml.base.StringOrRefImpl;
import org.citygml4j.impl.gml.geometry.GeometryPropertyImpl;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.util.Util;

public class DBBuildingFurniture implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuildingFurniture;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;

	private boolean transformCoords;

	public DBBuildingFurniture(Connection connection, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {		
			psBuildingFurniture = connection.prepareStatement("select * from BUILDING_FURNITURE where ROOM_ID = ?");
		} else {
			int srid = config.getInternal().getExportTargetSRS().getSrid();
			
			psBuildingFurniture = connection.prepareStatement("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
					"ROOM_ID, LOD4_GEOMETRY_ID, LOD4_IMPLICIT_REP_ID, " +
					"geodb_util.transform_or_null(LOD4_IMPLICIT_REF_POINT, " + srid + ") AS LOD4_IMPLICIT_REF_POINT, " +
			"LOD4_IMPLICIT_TRANSFORMATION from BUILDING_FURNITURE where ROOM_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public void read(Room room, long parentId) throws SQLException {
		ResultSet rs = null;

		try {
			psBuildingFurniture.setLong(1, parentId);
			rs = psBuildingFurniture.executeQuery();

			while (rs.next()) {
				long buildingFurnitureId = rs.getLong("ID");
				BuildingFurniture buildingFurniture = new BuildingFurnitureImpl();

				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(buildingFurniture, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);
					buildingFurniture.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					buildingFurniture.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());
					buildingFurniture.setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());
					buildingFurniture.setUsage(Arrays.asList(usageList));
				}

				long lodGeometryId = rs.getLong("LOD4_GEOMETRY_ID");
				if (!rs.wasNull() && lodGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodGeometryId);

					if (geometry != null) {
						GeometryProperty<AbstractGeometry> geometryProperty = new GeometryPropertyImpl<AbstractGeometry>();

						if (geometry.getAbstractGeometry() != null)
							geometryProperty.setGeometry(geometry.getAbstractGeometry());
						else
							geometryProperty.setHref(geometry.getTarget());

						buildingFurniture.setLod4Geometry(geometryProperty);
					}
				}

				// cityObject stuff
				cityObjectExporter.read(buildingFurniture, buildingFurnitureId);

				InteriorFurnitureProperty buildingFurnitureProp = new InteriorFurniturePropertyImpl();
				buildingFurnitureProp.setObject(buildingFurniture);
				room.addInteriorFurniture(buildingFurnitureProp);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psBuildingFurniture.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BUILDING_FURNITURE;
	}

}
