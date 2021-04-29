/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.importer.database.xlink.importer;

import java.sql.SQLException;
import java.util.HashMap;

import org.citydb.citygml.common.cache.CacheTable;
import org.citydb.citygml.common.cache.CacheTableManager;
import org.citydb.citygml.common.cache.model.CacheTableModel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;

public class DBXlinkImporterManager {
	private final CacheTableManager cacheTableManager;
	private final EventDispatcher eventDispatcher;
	private HashMap<DBXlinkImporterEnum, DBXlinkImporter> dbImporterMap;

	public DBXlinkImporterManager(CacheTableManager cacheTableManager, EventDispatcher eventDispatcher) {
		this.cacheTableManager = cacheTableManager;
		this.eventDispatcher = eventDispatcher;

		dbImporterMap = new HashMap<DBXlinkImporterEnum, DBXlinkImporter>();
	}

	public DBXlinkImporter getDBImporterXlink(DBXlinkImporterEnum xlinkType) throws SQLException {
		DBXlinkImporter dbImporter = dbImporterMap.get(xlinkType);

		if (dbImporter == null) {
			// firstly create tmp table
			CacheTable tempTable = null;

			switch (xlinkType) {
			case SURFACE_GEOMETRY:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.SURFACE_GEOMETRY);
				break;
			case LINEAR_RING:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.LINEAR_RING);
				break;
			case XLINK_BASIC:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.BASIC);
				break;
			case XLINK_TEXTURE_COORD_LIST:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.TEXTURE_COORD_LIST);
				break;
			case XLINK_TEXTUREPARAM:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.TEXTUREPARAM);
				break;
			case TEXTUREASSOCIATION_TARGET:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.TEXTUREASSOCIATION_TARGET);
				break;
			case XLINK_TEXTUREASSOCIATION:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.TEXTUREASSOCIATION);
				break;
			case TEXTURE_FILE:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.TEXTURE_FILE);
				break;
			case SURFACE_DATA_TO_TEX_IMAGE:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.SURFACE_DATA_TO_TEX_IMAGE);
				break;
			case LIBRARY_OBJECT:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.LIBRARY_OBJECT);
				break;
			case XLINK_DEPRECATED_MATERIAL:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.DEPRECATED_MATERIAL);
				break;
			case GROUP_TO_CITYOBJECT:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.GROUP_TO_CITYOBJECT);
				break;
			case SOLID_GEOMETRY:
				tempTable = cacheTableManager.createCacheTable(CacheTableModel.SOLID_GEOMETRY);
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
				case XLINK_TEXTURE_COORD_LIST:
					dbImporter = new DBXlinkImporterTextureCoordList(tempTable, this);
					break;
				case XLINK_TEXTUREPARAM:
					dbImporter = new DBXlinkImporterTextureParam(tempTable, this);
					break;
				case TEXTUREASSOCIATION_TARGET:
					dbImporter = new DBXlinkImporterTextureAssociationTarget(tempTable, this);
					break;
				case XLINK_TEXTUREASSOCIATION:
					dbImporter = new DBXlinkImporterTextureAssociation(tempTable, this);
					break;
				case TEXTURE_FILE:
					dbImporter = new DBXlinkImporterTextureFile(tempTable, this);
					break;
				case SURFACE_DATA_TO_TEX_IMAGE:
					dbImporter = new DBXlinkImporterSurfaceDataToTexImage(tempTable, this);
					break;
				case LIBRARY_OBJECT:
					dbImporter = new DBXlinkImporterLibraryObject(tempTable, this);
					break;
				case XLINK_DEPRECATED_MATERIAL:
					dbImporter = new DBXlinkImporterDeprecatedMaterial(tempTable, this);
					break;
				case GROUP_TO_CITYOBJECT:
					dbImporter = new DBXlinkImporterGroupToCityObject(tempTable, this);
					break;
				case SOLID_GEOMETRY:
					dbImporter = new DBXlinkImporterSolidGeometry(tempTable, this);
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
	
	public AbstractDatabaseAdapter getCacheAdapter() {
		return cacheTableManager.getDatabaseAdapter();
	}

	public void executeBatch() throws SQLException {
		for (DBXlinkImporter dbImporter : dbImporterMap.values())
			dbImporter.executeBatch();
	}
	
	public void close() throws SQLException {
		for (DBXlinkImporter dbImporter : dbImporterMap.values())
			dbImporter.close();
	}
}
