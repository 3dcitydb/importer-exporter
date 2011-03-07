package de.tub.citydb.db.exporter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.impl.jaxb.gml._3_1_1.GeometryPropertyImpl;
import org.citygml4j.impl.jaxb.gml._3_1_1.StringOrRefImpl;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.BuildingModule;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.GeometryProperty;
import org.citygml4j.model.gml.StringOrRef;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.util.Util;

public class DBBuildingFurniture implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Config config;
	private final Connection connection;

	private PreparedStatement psBuildingFurniture;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectExporter;

	private String gmlNameDelimiter;
	private boolean transformCoords;

	public DBBuildingFurniture(Connection connection, CityGMLFactory cityGMLFactory, Config config, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.config = config;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		transformCoords = config.getInternal().isTransformCoordinates();

		if (!transformCoords) {		
			psBuildingFurniture = connection.prepareStatement("select * from BUILDING_FURNITURE where ROOM_ID = ?");
		} else {
			ReferenceSystem targetSRS = config.getInternal().getExportTargetSRS();
			
			psBuildingFurniture = connection.prepareStatement("select NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
					"ROOM_ID, LOD4_GEOMETRY_ID, LOD4_IMPLICIT_REP_ID, " +
					"geodb_util.transform_or_null(LOD4_IMPLICIT_REF_POINT, " + targetSRS.getSrid() + ") AS LOD4_IMPLICIT_REF_POINT, " +
			"LOD4_IMPLICIT_TRANSFORMATION from BUILDING_FURNITURE where ROOM_ID = ?");
		}

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectExporter = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public void read(Room room, long parentId, BuildingModule bldg) throws SQLException {
		ResultSet rs = null;

		try {
			psBuildingFurniture.setLong(1, parentId);
			rs = psBuildingFurniture.executeQuery();

			while (rs.next()) {
				long buildingFurnitureId = rs.getLong("ID");
				BuildingFurniture buildingFurniture = cityGMLFactory.createBuildingFurniture(bldg);

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

				InteriorFurnitureProperty buildingFurnitureProp = cityGMLFactory.createInteriorFurnitureProperty(bldg);
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
