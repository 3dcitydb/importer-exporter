package de.tub.citydb.db.xlink.importer;

import java.sql.SQLException;
import java.util.HashMap;

import de.tub.citydb.db.temp.DBTempGTT;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.temp.model.DBTempTableBasic;
import de.tub.citydb.db.temp.model.DBTempTableDeprecatedMaterial;
import de.tub.citydb.db.temp.model.DBTempTableExternalFile;
import de.tub.citydb.db.temp.model.DBTempTableGroupToCityObject;
import de.tub.citydb.db.temp.model.DBTempTableLinearRing;
import de.tub.citydb.db.temp.model.DBTempTableSurfaceGeometry;
import de.tub.citydb.db.temp.model.DBTempTableTextureAssociation;
import de.tub.citydb.db.temp.model.DBTempTableTextureParam;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;

public class DBXlinkImporterManager {
	private final DBTempTableManager dbTempTableManager;
	private final EventDispatcher eventDispatcher;
	private HashMap<DBXlinkImporterEnum, DBXlinkImporter> dbImporterMap;

	public DBXlinkImporterManager(DBTempTableManager dbTempTableManager, EventDispatcher eventDispatcher) {
		this.dbTempTableManager = dbTempTableManager;
		this.eventDispatcher = eventDispatcher;

		dbImporterMap = new HashMap<DBXlinkImporterEnum, DBXlinkImporter>();
	}

	public DBXlinkImporter getDBImporterXlink(DBXlinkImporterEnum xlinkType) throws SQLException {
		DBXlinkImporter dbImporter = dbImporterMap.get(xlinkType);

		if (dbImporter == null) {
			// firstly create tmp table
			DBTempGTT tempTable = null;

			switch (xlinkType) {
			case SURFACE_GEOMETRY:
				tempTable = dbTempTableManager.createDecoupledGTT(DBTempTableSurfaceGeometry.getInstance());
				break;
			case LINEAR_RING:
				tempTable = dbTempTableManager.createGTT(DBTempTableLinearRing.getInstance());
				break;
			case XLINK_BASIC:
				tempTable = dbTempTableManager.createGTT(DBTempTableBasic.getInstance());
				break;
			case XLINK_TEXTUREPARAM:
				tempTable = dbTempTableManager.createGTT(DBTempTableTextureParam.getInstance());
				break;
			case XLINK_TEXTUREASSOCIATION:
				tempTable = dbTempTableManager.createGTT(DBTempTableTextureAssociation.getInstance());
				break;
			case EXTERNAL_FILE:
				tempTable = dbTempTableManager.createGTT(DBTempTableExternalFile.getInstance());
				break;
			case XLINK_DEPRECATED_MATERIAL:
				tempTable = dbTempTableManager.createGTT(DBTempTableDeprecatedMaterial.getInstance());
				break;
			case GROUP_TO_CITYOBJECT:
				tempTable = dbTempTableManager.createDecoupledGTT(DBTempTableGroupToCityObject.getInstance());
				break;
			}

			if (tempTable != null) {
				// initialize DBImporter
				switch (xlinkType) {
				case SURFACE_GEOMETRY:
					dbImporter = new DBXlinkImporterSurfaceGeometry(tempTable, this);
					break;
				case LINEAR_RING:
					dbImporter = new DBXlinkImporterLinearRing(tempTable, this);
					break;
				case XLINK_BASIC:
					dbImporter = new DBXlinkImporterBasic(tempTable, this);
					break;
				case XLINK_TEXTUREPARAM:
					dbImporter = new DBXlinkImporterTextureParam(tempTable, this);
					break;
				case XLINK_TEXTUREASSOCIATION:
					dbImporter = new DBXlinkImporterTextureAssociation(tempTable, this);
					break;
				case EXTERNAL_FILE:
					dbImporter = new DBXlinkImporterExternalFile(tempTable, this);
					break;
				case XLINK_DEPRECATED_MATERIAL:
					dbImporter = new DBXlinkImporterDeprecatedMaterial(tempTable, this);
					break;
				case GROUP_TO_CITYOBJECT:
					dbImporter = new DBXlinkImporterGroupToCityObject(tempTable, this);
					break;
				}

				if (dbImporter != null)
					dbImporterMap.put(xlinkType, dbImporter);
			}
		}

		return dbImporter;
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}

	public void executeBatch() throws SQLException {
		for (DBXlinkImporter dbImporter : dbImporterMap.values())
			dbImporter.executeBatch();
	}
}
