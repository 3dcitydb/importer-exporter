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
package de.tub.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.cache.HeapCacheTable;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.GmlIdEntry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencer;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;
import de.tub.citydb.modules.common.filter.ImportFilter;

public class DBXlinkResolverManager {
	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final CacheManager dbTempTableManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private HashMap<DBXlinkResolverEnum, DBXlinkResolver> dbWriterMap;
	private DBGmlIdResolver dbGmlIdResolver;
	private DBSequencer dbSequencer;

	public DBXlinkResolverManager(
			Connection batchConn,
			AbstractDatabaseAdapter databaseAdapter,
			WorkerPool<DBXlink> tmpXlinkPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CacheManager dbTempTableManager,
			ImportFilter importFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.connection = batchConn;
		this.databaseAdapter = databaseAdapter;
		this.tmpXlinkPool = tmpXlinkPool;
		this.dbTempTableManager = dbTempTableManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbWriterMap = new HashMap<DBXlinkResolverEnum, DBXlinkResolver>();
		dbGmlIdResolver = new DBGmlIdResolver(batchConn, lookupServerManager, config);
		dbSequencer = new DBSequencer(batchConn, databaseAdapter);
	}

	public DBXlinkResolver getDBXlinkResolver(DBXlinkResolverEnum dbResolverType) throws SQLException {
		DBXlinkResolver dbResolver = dbWriterMap.get(dbResolverType);

		if (dbResolver == null) {
			// initialise DBWriter
			switch (dbResolverType) {
			case SURFACE_GEOMETRY:
				HeapCacheTable surfaceGeomHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.SURFACE_GEOMETRY);
				if (surfaceGeomHeapView != null)
					dbResolver = new XlinkSurfaceGeometry(connection, surfaceGeomHeapView, config, this);

				break;
			case BASIC:
				dbResolver = new XlinkBasic(connection, this);
				break;
			case TEXCOORDLIST:
				HeapCacheTable textureParamHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.TEXTUREPARAM);
				HeapCacheTable linearRingHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.LINEAR_RING);				
				if (textureParamHeapView != null && linearRingHeapView != null)
					dbResolver = new XlinkTexCoordList(connection,
							textureParamHeapView,
							linearRingHeapView, this);
				break;
			case TEXTUREPARAM:
				dbResolver = new XlinkTextureParam(connection, this);
				break;
			case XLINK_TEXTUREASSOCIATION:
				HeapCacheTable texAssHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION);
				if (texAssHeapView != null)
					dbResolver = new XlinkTextureAssociation(connection, texAssHeapView, this);
				break;
			case TEXTURE_IMAGE:
				dbResolver = new XlinkTextureImage(connection, config, this);
				break;
			case LIBRARY_OBJECT:
				dbResolver = new XlinkLibraryObject(connection, config, this);
				break;
			case WORLD_FILE:
				dbResolver = new XlinkWorldFile(connection, config, this);
				break;
			case XLINK_DEPRECATED_MATERIAL:
				dbResolver = new XlinkDeprecatedMaterial(connection, this);
				break;
			case GROUP_TO_CITYOBJECT:
				HeapCacheTable groupHeapView = dbTempTableManager.getDerivedHeapCacheTable(CacheTableModelEnum.GROUP_TO_CITYOBJECT);
				if (groupHeapView != null)					
					dbResolver = new XlinkGroupToCityObject(connection, groupHeapView, importFilter, this);

				break;
			}

			if (dbResolver != null)
				dbWriterMap.put(dbResolverType, dbResolver);
		}

		return dbResolver;
	}

	public long getDBId(DBSequencerEnum sequence) throws SQLException {
		return dbSequencer.getDBId(sequence);
	}

	public GmlIdEntry getDBId(String gmlId, CityGMLClass type) {
		return getDBId(gmlId, type, false);
	}

	public GmlIdEntry getDBId(String gmlId, CityGMLClass type, boolean forceCityObjectDatabaseLookup) {
		return dbGmlIdResolver.getDBId(gmlId, type, forceCityObjectDatabaseLookup);
	}

	public void propagateXlink(DBXlink xlink) {
		tmpXlinkPool.addWork(xlink);
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}
	
	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	public void executeBatch() throws SQLException {
		for (DBXlinkResolver dbResolver : dbWriterMap.values())
			dbResolver.executeBatch();
	}

	public void close() throws SQLException {
		dbGmlIdResolver.close();
		dbSequencer.close();
		
		for (DBXlinkResolver dbResolver : dbWriterMap.values())
			dbResolver.close();
	}
}
