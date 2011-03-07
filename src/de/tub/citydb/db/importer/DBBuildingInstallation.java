package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.building.BuildingInstallation;
import de.tub.citygml4j.model.citygml.building.IntBuildingInstallation;
import de.tub.citygml4j.model.gml.GeometryProperty;

public class DBBuildingInstallation implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBuildingInstallation;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	
	private String gmlNameDelimiter;
	private int batchCounter;

	public DBBuildingInstallation(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();
		
		psBuildingInstallation = batchConn.prepareStatement("insert into BUILDING_INSTALLATION (ID, IS_EXTERNAL, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, BUILDING_ID, ROOM_ID, LOD2_GEOMETRY_ID, LOD3_GEOMETRY_ID, LOD4_GEOMETRY_ID) values " +
				"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(BuildingInstallation buildingInstallation, CityGMLClass parent, long parentId) throws SQLException {
		long buildingInstallationId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (buildingInstallationId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(buildingInstallation, buildingInstallationId);

		// BuildingInstallation
		// ID
		psBuildingInstallation.setLong(1, buildingInstallationId);

		// IS_EXTERNAL
		psBuildingInstallation.setLong(2, 1);

		// gml:name
		if (buildingInstallation.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(buildingInstallation, gmlNameDelimiter);

			psBuildingInstallation.setString(3, dbGmlName[0]);
			psBuildingInstallation.setString(4, dbGmlName[1]);
		} else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (buildingInstallation.getDescription() != null) {
			String description = buildingInstallation.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuildingInstallation.setString(5, description);
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
		}

		// citygml:class
		if (buildingInstallation.getClazz() != null)
			psBuildingInstallation.setString(6, buildingInstallation.getClazz().trim());
		else
			psBuildingInstallation.setNull(6, Types.VARCHAR);

		// citygml:function
		if (buildingInstallation.getFunction() != null) {
			List<String> functionList = buildingInstallation.getFunction();
			psBuildingInstallation.setString(7, Util.collection2string(functionList, " "));
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
		}

		// citygml:usage
		if (buildingInstallation.getUsage() != null) {
			List<String> usageList = buildingInstallation.getUsage();
			psBuildingInstallation.setString(8, Util.collection2string(usageList, " "));
		} else {
			psBuildingInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		switch (parent) {
		case BUILDING:
		case BUILDINGPART:
			psBuildingInstallation.setLong(9, parentId);
			psBuildingInstallation.setNull(10, 0);
			break;
		case ROOM:
			psBuildingInstallation.setNull(9, 0);
			psBuildingInstallation.setLong(10, parentId);
			break;
		default:
			psBuildingInstallation.setNull(9, 0);
			psBuildingInstallation.setNull(10, 0);
		}

		// Geometry
		for (int lod = 2; lod < 5; lod++) {
			GeometryProperty geometryProperty = null;
			long geometryId = 0;

			switch (lod) {
			case 2:
				geometryProperty = buildingInstallation.getLod2Geometry();
				break;
			case 3:
				geometryProperty = buildingInstallation.getLod3Geometry();
				break;
			case 4:
				geometryProperty = buildingInstallation.getLod4Geometry();
				break;
			}

			if (geometryProperty != null) {
				if (geometryProperty.getGeometry() != null) {
					geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), buildingInstallationId);
				} else {
					// xlink
					String href = geometryProperty.getHref();

        			if (href != null && href.length() != 0) {
        				DBXlinkBasic xlink = new DBXlinkBasic(
        						buildingInstallationId,
        						DBTableEnum.BUILDING_INSTALLATION,
        						href,
        						DBTableEnum.SURFACE_GEOMETRY
        				);

        				xlink.setAttrName("LOD" + lod + "_GEOMETRY_ID");
        				dbImporterManager.propagateXlink(xlink);
        			}
				}
			}

			switch (lod) {
			case 2:
				if (geometryId != 0)
					psBuildingInstallation.setLong(11, geometryId);
				else
					psBuildingInstallation.setNull(11, 0);
				break;
			case 3:
				if (geometryId != 0)
					psBuildingInstallation.setLong(12, geometryId);
				else
					psBuildingInstallation.setNull(12, 0);
				break;
			case 4:
				if (geometryId != 0)
					psBuildingInstallation.setLong(13, geometryId);
				else
					psBuildingInstallation.setNull(13, 0);
				break;
			}
		}

		psBuildingInstallation.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING_INSTALLATION);
		
		return buildingInstallationId;
	}

	public long insert(IntBuildingInstallation intBuildingInstallation, CityGMLClass parent, long parentId) throws SQLException {
		long buildingInstallationId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		if (buildingInstallationId == 0)
			return 0;

		// CityObject
		cityObjectImporter.insert(intBuildingInstallation, buildingInstallationId);

		// IntBuildingInstallation
		// ID
		psBuildingInstallation.setLong(1, buildingInstallationId);

		// IS_EXTERNAL
		psBuildingInstallation.setLong(2, 0);

		// gml:name
		if (intBuildingInstallation.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(intBuildingInstallation, gmlNameDelimiter);

			psBuildingInstallation.setString(3, dbGmlName[0]);
			psBuildingInstallation.setString(4, dbGmlName[1]);
		}  else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (intBuildingInstallation.getDescription() != null) {
			String description = intBuildingInstallation.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuildingInstallation.setString(5, description);
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
		}

		// citygml:class
		if (intBuildingInstallation.getClazz() != null) {
			psBuildingInstallation.setString(6, intBuildingInstallation.getClazz().trim());
		} else {
			psBuildingInstallation.setNull(6, Types.VARCHAR);
		}

		// citygml:function
		if (intBuildingInstallation.getFunction() != null) {
			List<String> functionList = intBuildingInstallation.getFunction();
			psBuildingInstallation.setString(7, Util.collection2string(functionList, " "));
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
		}

		// citygml:usage
		if (intBuildingInstallation.getUsage() != null) {
			List<String> usageList = intBuildingInstallation.getUsage();
			psBuildingInstallation.setString(8, Util.collection2string(usageList, " "));
		} else {
			psBuildingInstallation.setNull(8, Types.VARCHAR);
		}

		// parentId
		switch (parent) {
		case BUILDING:
		case BUILDINGPART:
			psBuildingInstallation.setLong(9, parentId);
			psBuildingInstallation.setNull(10, 0);
			break;
		case ROOM:
			psBuildingInstallation.setNull(9, 0);
			psBuildingInstallation.setLong(10, parentId);
			break;
		default:
			psBuildingInstallation.setNull(9, 0);
			psBuildingInstallation.setNull(10, 0);
		}

		// Geometry
		psBuildingInstallation.setNull(11, 0);
		psBuildingInstallation.setNull(12, 0);

		GeometryProperty geometryProperty = intBuildingInstallation.getLod4Geometry();
		long geometryId = 0;
		if (geometryProperty != null) {
			if (geometryProperty.getGeometry() != null) {
				geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), buildingInstallationId);
			} else {
				// xlink
				String href = geometryProperty.getHref();

    			if (href != null && href.length() != 0) {
    				DBXlinkBasic xlink = new DBXlinkBasic(
    						buildingInstallationId,
    						DBTableEnum.BUILDING_INSTALLATION,
    						href,
    						DBTableEnum.SURFACE_GEOMETRY
    				);

    				xlink.setAttrName("LOD4_GEOMETRY_ID");
    				dbImporterManager.propagateXlink(xlink);
    			}
			}
		}
		if (geometryId != 0)
			psBuildingInstallation.setLong(13, geometryId);
		else
			psBuildingInstallation.setNull(13, 0);

		psBuildingInstallation.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.BUILDING_INSTALLATION);
		
		return buildingInstallationId;
	}

	@Override
	public void executeBatch() throws SQLException {
		psBuildingInstallation.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BUILDING_INSTALLATION;
	}

}
