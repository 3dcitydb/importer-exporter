package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import de.tub.citydb.config.Config;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.implementation.gml._3_1_1.GeometryPropertyImpl;
import de.tub.citygml4j.implementation.gml._3_1_1.StringOrRefImpl;
import de.tub.citygml4j.model.citygml.building.BuildingFurniture;
import de.tub.citygml4j.model.citygml.building.BuildingModule;
import de.tub.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import de.tub.citygml4j.model.citygml.building.Room;
import de.tub.citygml4j.model.gml.GeometryProperty;
import de.tub.citygml4j.model.gml.StringOrRef;

public class DBBuildingFurniture implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuildingFurniture;
	private ResultSet rs;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;
	
	private String gmlNameDelimiter;
	
	public DBBuildingFurniture(Connection connection, CityGMLFactory cityGMLFactory, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();

		psBuildingFurniture = connection.prepareStatement("select * from BUILDING_FURNITURE where ROOM_ID = ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public void read(Room room, long parentId, BuildingModule bldgFactory) throws SQLException {
		psBuildingFurniture.setLong(1, parentId);
		rs = psBuildingFurniture.executeQuery();

		while (rs.next()) {
			long buildingFurnitureId = rs.getLong("ID");
			BuildingFurniture buildingFurniture = cityGMLFactory.createBuildingFurniture(bldgFactory);

			String gmlName = rs.getString("NAME");
			String gmlNameCodespace = rs.getString("NAME_CODESPACE");

			Util.dbGmlName2featureName(buildingFurniture, gmlName, gmlNameCodespace, gmlNameDelimiter);

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
					GeometryProperty geometryProperty = new GeometryPropertyImpl();

					if (geometry.getAbstractGeometry() != null)
						geometryProperty.setGeometry(geometry.getAbstractGeometry());
					else
						geometryProperty.setHref(geometry.getTarget());

					buildingFurniture.setLod4Geometry(geometryProperty);
				}
			}

			// cityObject stuff
			cityObjectExporter.read(buildingFurniture, buildingFurnitureId);

			InteriorFurnitureProperty buildingFurnitureProp = cityGMLFactory.createInteriorFurnitureProperty(bldgFactory);
			buildingFurnitureProp.setObject(buildingFurniture);
			room.addInteriorFurniture(buildingFurnitureProp);
		}
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BUILDING_FURNITURE;
	}

}
