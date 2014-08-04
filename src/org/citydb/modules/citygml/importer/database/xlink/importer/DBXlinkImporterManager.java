/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package org.citydb.modules.citygml.importer.database.xlink.importer;

import java.sql.SQLException;
import java.util.HashMap;

import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;

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
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.SURFACE_GEOMETRY);
				break;
			case LINEAR_RING:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.LINEAR_RING);
				break;
			case XLINK_BASIC:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.BASIC);
				break;
			case XLINK_TEXTURE_COORD_LIST:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.TEXTURE_COORD_LIST);
				break;
			case XLINK_TEXTUREPARAM:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.TEXTUREPARAM);
				break;
			case TEXTUREASSOCIATION_TARGET:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION_TARGET);
				break;
			case XLINK_TEXTUREASSOCIATION:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION);
				break;
			case TEXTURE_FILE:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.TEXTURE_FILE);
				break;
			case SURFACE_DATA_TO_TEX_IMAGE:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.SURFACE_DATA_TO_TEX_IMAGE);
				break;
			case LIBRARY_OBJECT:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.LIBRARY_OBJECT);
				break;
			case XLINK_DEPRECATED_MATERIAL:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.DEPRECATED_MATERIAL);
				break;
			case GROUP_TO_CITYOBJECT:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.GROUP_TO_CITYOBJECT);
				break;
			case SOLID_GEOMETRY:
				tempTable = cacheTableManager.createCacheTable(CacheTableModelEnum.SOLID_GEOMETRY);
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
