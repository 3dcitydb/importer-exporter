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
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingModule;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.gml.GeometryProperty;
import org.citygml4j.model.gml.StringOrRef;

import de.tub.citydb.util.Util;

public class DBBuildingInstallation implements DBExporter {
	private final DBExporterManager dbExporterManager;
	private final CityGMLFactory cityGMLFactory;
	private final Connection connection;

	private PreparedStatement psBuildingInstallation;
	private PreparedStatement psRoomInstallation;

	private DBSurfaceGeometry surfaceGeometryExporter;
	private DBCityObject cityObjectReader;

	public DBBuildingInstallation(Connection connection, CityGMLFactory cityGMLFactory, DBExporterManager dbExporterManager) throws SQLException {
		this.connection = connection;
		this.cityGMLFactory = cityGMLFactory;
		this.dbExporterManager = dbExporterManager;

		init();
	}

	private void init() throws SQLException {
		psBuildingInstallation = connection.prepareStatement("select * from BUILDING_INSTALLATION where BUILDING_ID = ?");
		psRoomInstallation = connection.prepareStatement("select * from BUILDING_INSTALLATION where ROOM_ID =  ?");

		surfaceGeometryExporter = (DBSurfaceGeometry)dbExporterManager.getDBExporter(DBExporterEnum.SURFACE_GEOMETRY);
		cityObjectReader = (DBCityObject)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECT);
	}

	public void read(AbstractBuilding building, long parentId, BuildingModule bldg) throws SQLException {
		ResultSet rs = null;

		try {
			psBuildingInstallation.setLong(1, parentId);
			rs = psBuildingInstallation.executeQuery();

			while (rs.next()) {
				long installationId = rs.getLong("ID");
				int isExternal = rs.getInt("IS_EXTERNAL");

				BuildingInstallation buildingInstallation = null;
				IntBuildingInstallation intBuildingInstallation = null;

				if (isExternal == 1)
					buildingInstallation = cityGMLFactory.createBuildingInstallation(bldg);
				else
					intBuildingInstallation = cityGMLFactory.createIntBuildingInstallation(bldg);

				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				if (buildingInstallation != null)
					Util.dbGmlName2featureName(buildingInstallation, gmlName, gmlNameCodespace);
				else
					Util.dbGmlName2featureName(intBuildingInstallation, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);

					if (buildingInstallation != null)
						buildingInstallation.setDescription(stringOrRef);
					else
						intBuildingInstallation.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					if (buildingInstallation != null)
						buildingInstallation.setClazz(clazz);
					else
						intBuildingInstallation.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());

					if (buildingInstallation != null)
						buildingInstallation.setFunction(Arrays.asList(functionList));
					else
						intBuildingInstallation.setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());

					if (buildingInstallation != null)
						buildingInstallation.setUsage(Arrays.asList(usageList));
					else
						intBuildingInstallation.setUsage(Arrays.asList(usageList));
				}

				for (int lod = 2; lod < 5 ; lod++) {
					long lodSurfaceGeometryId = rs.getLong("LOD" + lod + "_GEOMETRY_ID");

					if (!rs.wasNull() && lodSurfaceGeometryId != 0) {
						DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodSurfaceGeometryId);

						if (geometry != null) {
							GeometryProperty geometryProperty = new GeometryPropertyImpl();

							if (geometry.getAbstractGeometry() != null)
								geometryProperty.setGeometry(geometry.getAbstractGeometry());
							else
								geometryProperty.setHref(geometry.getTarget());

							switch (lod) {
							case 2:
								if (buildingInstallation != null)
									buildingInstallation.setLod2Geometry(geometryProperty);
								break;
							case 3:
								if (buildingInstallation != null)
									buildingInstallation.setLod3Geometry(geometryProperty);
								break;
							case 4:
								if (buildingInstallation != null)
									buildingInstallation.setLod4Geometry(geometryProperty);
								else
									intBuildingInstallation.setLod4Geometry(geometryProperty);
								break;
							}
						}
					}
				}

				if (buildingInstallation != null) {
					cityObjectReader.read(buildingInstallation, installationId);

					BuildingInstallationProperty buildInstProp = cityGMLFactory.createBuildingInstallationProperty(bldg);
					buildInstProp.setObject(buildingInstallation);
					building.addOuterBuildingInstallation(buildInstProp);
				} else {
					cityObjectReader.read(intBuildingInstallation, installationId);

					IntBuildingInstallationProperty intInstProp = cityGMLFactory.createIntBuildingInstallationProperty(bldg);
					intInstProp.setObject(intBuildingInstallation);
					building.addInteriorBuildingInstallation(intInstProp);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	public void read(Room room, long parentId, BuildingModule bldg) throws SQLException {
		ResultSet rs = null;

		try {
			psRoomInstallation.setLong(1, parentId);
			rs = psRoomInstallation.executeQuery();

			while (rs.next()) {
				long installationId = rs.getLong("ID");
				IntBuildingInstallation intBuildingInstallation = cityGMLFactory.createIntBuildingInstallation(bldg);

				String gmlName = rs.getString("NAME");
				String gmlNameCodespace = rs.getString("NAME_CODESPACE");

				Util.dbGmlName2featureName(intBuildingInstallation, gmlName, gmlNameCodespace);

				String description = rs.getString("DESCRIPTION");
				if (description != null) {
					StringOrRef stringOrRef = new StringOrRefImpl();
					stringOrRef.setValue(description);

					intBuildingInstallation.setDescription(stringOrRef);
				}

				String clazz = rs.getString("CLASS");
				if (clazz != null) {
					intBuildingInstallation.setClazz(clazz);
				}

				String function = rs.getString("FUNCTION");
				if (function != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] functionList = p.split(function.trim());

					intBuildingInstallation.setFunction(Arrays.asList(functionList));
				}

				String usage = rs.getString("USAGE");
				if (usage != null) {
					Pattern p = Pattern.compile("\\s+");
					String[] usageList = p.split(usage.trim());

					intBuildingInstallation.setUsage(Arrays.asList(usageList));
				}

				long lodSurfaceGeometryId = rs.getLong("LOD4_GEOMETRY_ID");
				if (!rs.wasNull() && lodSurfaceGeometryId != 0) {
					DBSurfaceGeometryResult geometry = surfaceGeometryExporter.read(lodSurfaceGeometryId);

					if (geometry != null) {
						GeometryProperty geometryProperty = new GeometryPropertyImpl();

						if (geometry.getAbstractGeometry() != null)
							geometryProperty.setGeometry(geometry.getAbstractGeometry());
						else
							geometryProperty.setHref(geometry.getTarget());

						intBuildingInstallation.setLod4Geometry(geometryProperty);
					}
				}

				cityObjectReader.read(intBuildingInstallation, installationId);

				IntBuildingInstallationProperty intInstProp = cityGMLFactory.createIntBuildingInstallationProperty(bldg);
				intInstProp.setObject(intBuildingInstallation);
				room.addRoomInstallation(intInstProp);
			}
		} finally {
			if (rs != null)
				rs.close();
		}
	}

	@Override
	public void close() throws SQLException {
		psBuildingInstallation.close();
		psRoomInstallation.close();
	}

	@Override
	public DBExporterEnum getDBExporterType() {
		return DBExporterEnum.BUILDING_INSTALLATION;
	}

}
