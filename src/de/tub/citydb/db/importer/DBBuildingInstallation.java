package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.gml.GeometryProperty;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.util.Util;

public class DBBuildingInstallation implements DBImporter {
	private final Connection batchConn;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psBuildingInstallation;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;
	
	private int batchCounter;

	public DBBuildingInstallation(Connection batchConn, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {		
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
		if (buildingInstallation.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(buildingInstallation);

			psBuildingInstallation.setString(3, dbGmlName[0]);
			psBuildingInstallation.setString(4, dbGmlName[1]);
		} else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (buildingInstallation.isSetDescription()) {
			String description = buildingInstallation.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuildingInstallation.setString(5, description);
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
		}

		// citygml:class
		if (buildingInstallation.isSetClazz())
			psBuildingInstallation.setString(6, buildingInstallation.getClazz().trim());
		else
			psBuildingInstallation.setNull(6, Types.VARCHAR);

		// citygml:function
		if (buildingInstallation.isSetFunction()) {
			psBuildingInstallation.setString(7, Util.collection2string(buildingInstallation.getFunction(), " "));
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
		}

		// citygml:usage
		if (buildingInstallation.isSetUsage()) {
			psBuildingInstallation.setString(8, Util.collection2string(buildingInstallation.getUsage(), " "));
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
				if (geometryProperty.isSetGeometry()) {
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
		if (intBuildingInstallation.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(intBuildingInstallation);

			psBuildingInstallation.setString(3, dbGmlName[0]);
			psBuildingInstallation.setString(4, dbGmlName[1]);
		}  else {
			psBuildingInstallation.setNull(3, Types.VARCHAR);
			psBuildingInstallation.setNull(4, Types.VARCHAR);
		}

		// gml:description
		if (intBuildingInstallation.isSetDescription()) {
			String description = intBuildingInstallation.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psBuildingInstallation.setString(5, description);
		} else {
			psBuildingInstallation.setNull(5, Types.VARCHAR);
		}

		// citygml:class
		if (intBuildingInstallation.isSetClazz()) {
			psBuildingInstallation.setString(6, intBuildingInstallation.getClazz().trim());
		} else {
			psBuildingInstallation.setNull(6, Types.VARCHAR);
		}

		// citygml:function
		if (intBuildingInstallation.isSetFunction()) {
			psBuildingInstallation.setString(7, Util.collection2string(intBuildingInstallation.getFunction(), " "));
		} else {
			psBuildingInstallation.setNull(7, Types.VARCHAR);
		}

		// citygml:usage
		if (intBuildingInstallation.isSetUsage()) {
			psBuildingInstallation.setString(8, Util.collection2string(intBuildingInstallation.getUsage(), " "));
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

		long geometryId = 0;
		if (intBuildingInstallation.isSetLod4Geometry()) {
			GeometryProperty geometryProperty = intBuildingInstallation.getLod4Geometry();

			if (geometryProperty.isSetGeometry()) {
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
	public void close() throws SQLException {
		psBuildingInstallation.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.BUILDING_INSTALLATION;
	}

}
