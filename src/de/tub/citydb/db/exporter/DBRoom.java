package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.impl.jaxb.gml._3_1_1.MultiSurfacePropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.SolidPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BuildingModule;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.AbstractSolid;
import org.citygml4j.model.gml.MultiSurface;
import org.citygml4j.model.gml.MultiSurfaceProperty;
import org.citygml4j.model.gml.SolidProperty;
import org.citygml4j.model.gml.StringOrRef;

import de.tub.citydb.config.Config;
import de.tub.citydb.util.Util;

public class DBRoom implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psRoom;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	private DBBuildingInstallation buildingInstallationExporter;
	private DBThematicSurface thematicSurfaceExporter;
	private DBBuildingFurniture buildingFurnitureExporter;

	private String gmlNameDelimiter;

	public DBRoom(Connection connection, CityGMLFactory cityGMLFactory, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();

		psRoom = connection.prepareStatement("select * from ROOM where BUILDING_ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
		buildingInstallationExporter = (DBBuildingInstallation)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_INSTALLATION);
		thematicSurfaceExporter = (DBThematicSurface)dbExporterManager.getDBExporter(DBExporterEnum.THEMATIC_SURFACE);
		buildingFurnitureExporter = (DBBuildingFurniture)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING_FURNITURE);
	}

	public void read(AbstractBuilding building, long parentId, BuildingModule bldg) throws SQLException {
		ResultSet rs = null;

		try {
			psRoom.setLong(1, parentId);
			rs = psRoom.executeQuery();

			while (rs.next()) {
				long roomId = rs.getLong("ID");
				Room room = cityGMLFactory.createRoom(bldg);

				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(room, gmlName, gmlNameCodespace, gmlNameDelimiter);

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
				thematicSurfaceExporter.read(room, roomId, bldg);
				
				long lodGeometryId = rs.getLong("LOD4_GEOMETRY_ID");
				if (!rs.wasNull() && lodGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodGeometryId);

					if (geometry != null) {
						switch (geometry.getType()) {
						case COMPOSITESOLID:
						case SOLID:
							SolidProperty solidProperty = new SolidPropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								solidProperty.setSolid((AbstractSolid)geometry.getAbstractGeometry());
							else
								solidProperty.setHref(geometry.getTarget());

							room.setLod4Solid(solidProperty);
							break;
						case MULTISURFACE:
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
				buildingInstallationExporter.read(room, roomId, bldg);

				// buildingFurniture
				buildingFurnitureExporter.read(room, roomId, bldg);

				InteriorRoomProperty roomProperty = cityGMLFactory.createInteriorRoomProperty(bldg);
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
