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
import de.tub.citydb.db.xlink.DBXlinkGroupToCityObject;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import de.tub.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import de.tub.citygml4j.model.gml.GeometryProperty;

public class DBCityObjectGroup implements DBImporter {
	private final Connection batchConn;
	private final Config config;
	private final DBImporterManager dbImporterManager;

	private PreparedStatement psCityObjectGroup;
	private DBCityObject cityObjectImporter;
	private DBSurfaceGeometry surfaceGeometryImporter;

	private String gmlNameDelimiter;
	private int batchCounter;

	public DBCityObjectGroup(Connection batchConn, Config config, DBImporterManager dbImporterManager) throws SQLException {
		this.batchConn = batchConn;
		this.config = config;
		this.dbImporterManager = dbImporterManager;

		init();
	}

	private void init() throws SQLException {
		gmlNameDelimiter = config.getInternal().getGmlNameDelimiter();

		psCityObjectGroup = batchConn.prepareStatement("insert into CITYOBJECTGROUP (ID, NAME, NAME_CODESPACE, DESCRIPTION, CLASS, FUNCTION, USAGE, " +
				"GEOMETRY, SURFACE_GEOMETRY_ID, PARENT_CITYOBJECT_ID) values " +
		"(?, ?, ?, ?, ?, ?, ?, null, ?, ?)");

		surfaceGeometryImporter = (DBSurfaceGeometry)dbImporterManager.getDBImporter(DBImporterEnum.SURFACE_GEOMETRY);
		cityObjectImporter = (DBCityObject)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECT);
	}

	public long insert(CityObjectGroup cityObjectGroup) throws SQLException {
		long cityObjectGroupId = dbImporterManager.getDBId(DBSequencerEnum.CITYOBJECT_SEQ);
		boolean success = false;

		if (cityObjectGroupId != 0)
			success = insert(cityObjectGroup, cityObjectGroupId);

		if (success)
			return cityObjectGroupId;
		else
			return 0;
	}

	private boolean insert(CityObjectGroup cityObjectGroup, long cityObjectGroupId) throws SQLException {
		// CityObject
		long cityObjectId = cityObjectImporter.insert(cityObjectGroup, cityObjectGroupId);
		if (cityObjectId == 0)
			return false;

		// CityObjectGroup
		// ID
		psCityObjectGroup.setLong(1, cityObjectId);

		// gml:name
		if (cityObjectGroup.getName() != null) {
			String[] dbGmlName = Util.gmlName2dbString(cityObjectGroup, gmlNameDelimiter);

			psCityObjectGroup.setString(2, dbGmlName[0]);
			psCityObjectGroup.setString(3, dbGmlName[1]);
		} else {
			psCityObjectGroup.setNull(2, Types.VARCHAR);
			psCityObjectGroup.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (cityObjectGroup.getDescription() != null) {
			String description = cityObjectGroup.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psCityObjectGroup.setString(4, description);
		} else {
			psCityObjectGroup.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (cityObjectGroup.getClazz() != null)
			psCityObjectGroup.setString(5, cityObjectGroup.getClazz().trim());
		else
			psCityObjectGroup.setNull(5, Types.VARCHAR);

		// citygml:function
		if (cityObjectGroup.getFunction() != null) {
			List<String> functionList = cityObjectGroup.getFunction();
			psCityObjectGroup.setString(6, Util.collection2string(functionList, " "));
		} else {
			psCityObjectGroup.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (cityObjectGroup.getUsage() != null) {
			List<String> usageList = cityObjectGroup.getUsage();
			psCityObjectGroup.setString(7, Util.collection2string(usageList, " "));
		} else {
			psCityObjectGroup.setNull(7, Types.VARCHAR);
		}

		// Geometry
		GeometryProperty geometryProperty = cityObjectGroup.getGeometry();
		long geometryId = 0;

		if (geometryProperty != null) {
			if (geometryProperty.getGeometry() != null) {
				geometryId = surfaceGeometryImporter.insert(geometryProperty.getGeometry(), cityObjectGroupId);
			} else {
				// xlink
				String href = geometryProperty.getHref();

				if (href != null && href.length() != 0) {
					DBXlinkBasic xlink = new DBXlinkBasic(
							cityObjectGroupId,
							DBTableEnum.CITYOBJECTGROUP,
							href,
							DBTableEnum.SURFACE_GEOMETRY
					);

					xlink.setAttrName("SURFACE_GEOMETRY_ID");
					dbImporterManager.propagateXlink(xlink);
				}
			}
		}

		if (geometryId != 0)
			psCityObjectGroup.setLong(8, geometryId);
		else
			psCityObjectGroup.setNull(8, 0);

		// parent
		psCityObjectGroup.setNull(9, 0);
		
		psCityObjectGroup.addBatch();
		if (++batchCounter == Internal.ORACLE_MAX_BATCH_SIZE)
			dbImporterManager.executeBatch(DBImporterEnum.CITYOBJECTGROUP);		
		
		// group parent
		if (cityObjectGroup.getParent() != null) {
			if (cityObjectGroup.getParent().getObject() != null) {
				System.out.println("Parser-Fehler beim Einlesen von parent-Element");
			} else {			
				// xlink
				String href = cityObjectGroup.getParent().getHref();

				if (href != null && href.length() != 0) {
					dbImporterManager.propagateXlink(new DBXlinkGroupToCityObject(
							cityObjectGroupId,
							href,
							true));
				}
			}
		} 

		// group member
		List<CityObjectGroupMember> groupMemberList = cityObjectGroup.getGroupMember();
		if (groupMemberList != null) {
			for (CityObjectGroupMember groupMember : groupMemberList) {
				if (groupMember.getObject() != null) {
					System.out.println("Parser-Fehler beim Einlesen von groupMember-Elementen");
				} else {
					// xlink
					String href = groupMember.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkGroupToCityObject xlink = new DBXlinkGroupToCityObject(
								cityObjectGroupId,
								href,
								false);
						
						xlink.setRole(groupMember.getRoleType());						
						dbImporterManager.propagateXlink(xlink);
					}
				}
			}
		}
		
		return true;
	}

	@Override
	public void executeBatch() throws SQLException {
		psCityObjectGroup.executeBatch();
		batchCounter = 0;
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITYOBJECTGROUP;
	}

}
