package de.tub.citydb.db.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.gml.GeometryProperty;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.db.DBTableEnum;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.db.xlink.DBXlinkGroupToCityObject;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

public class DBCityObjectGroup implements DBImporter {
	private final Logger LOG = Logger.getInstance();
	
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
		String origGmlId = cityObjectGroup.getId();
		
		// CityObject
		long cityObjectId = cityObjectImporter.insert(cityObjectGroup, cityObjectGroupId);
		if (cityObjectId == 0)
			return false;

		// CityObjectGroup
		// ID
		psCityObjectGroup.setLong(1, cityObjectId);

		// gml:name
		if (cityObjectGroup.isSetName()) {
			String[] dbGmlName = Util.gmlName2dbString(cityObjectGroup, gmlNameDelimiter);

			psCityObjectGroup.setString(2, dbGmlName[0]);
			psCityObjectGroup.setString(3, dbGmlName[1]);
		} else {
			psCityObjectGroup.setNull(2, Types.VARCHAR);
			psCityObjectGroup.setNull(3, Types.VARCHAR);
		}

		// gml:description
		if (cityObjectGroup.isSetDescription()) {
			String description = cityObjectGroup.getDescription().getValue();

			if (description != null)
				description = description.trim();

			psCityObjectGroup.setString(4, description);
		} else {
			psCityObjectGroup.setNull(4, Types.VARCHAR);
		}

		// citygml:class
		if (cityObjectGroup.isSetClazz())
			psCityObjectGroup.setString(5, cityObjectGroup.getClazz().trim());
		else
			psCityObjectGroup.setNull(5, Types.VARCHAR);

		// citygml:function
		if (cityObjectGroup.isSetFunction()) {
			psCityObjectGroup.setString(6, Util.collection2string(cityObjectGroup.getFunction(), " "));
		} else {
			psCityObjectGroup.setNull(6, Types.VARCHAR);
		}

		// citygml:usage
		if (cityObjectGroup.isSetUsage()) {
			psCityObjectGroup.setString(7, Util.collection2string(cityObjectGroup.getUsage(), " "));
		} else {
			psCityObjectGroup.setNull(7, Types.VARCHAR);
		}

		// Geometry
		long geometryId = 0;

		if (cityObjectGroup.isSetGeometry()) {
			GeometryProperty geometryProperty = cityObjectGroup.getGeometry();

			if (geometryProperty.isSetGeometry()) {
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
		if (cityObjectGroup.isSetParent()) {
			if (cityObjectGroup.getParent().isSetObject()) {
				StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
						CityGMLClass.CITYOBJECTGROUP, 
						origGmlId));
				
				msg.append(": XML read error while parsing parent element.");
				LOG.error(msg.toString());
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
		if (cityObjectGroup.isSetGroupMember()) {
			for (CityObjectGroupMember groupMember : cityObjectGroup.getGroupMember()) {
				if (groupMember.isSetObject()) {
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							CityGMLClass.CITYOBJECTGROUP, 
							origGmlId));
					
					msg.append(": XML read error while parsing groupMember element.");
					LOG.error(msg.toString());
				} else {
					// xlink
					String href = groupMember.getHref();

					if (href != null && href.length() != 0) {
						DBXlinkGroupToCityObject xlink = new DBXlinkGroupToCityObject(
								cityObjectGroupId,
								href,
								false);
						
						xlink.setRole(groupMember.getGroupRole());						
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
	public void close() throws SQLException {
		psCityObjectGroup.close();
	}

	@Override
	public DBImporterEnum getDBImporterType() {
		return DBImporterEnum.CITYOBJECTGROUP;
	}

}
