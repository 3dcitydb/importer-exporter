/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.database.xlink.resolver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.modules.citygml.common.database.cache.CacheTable;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citydb.modules.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.importer.database.content.DBSequencer;
import org.citydb.modules.citygml.importer.database.content.DBSequencerEnum;
import org.citydb.modules.common.filter.ImportFilter;
import org.citygml4j.model.citygml.CityGMLClass;

public class DBXlinkResolverManager {
	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final CacheTableManager cacheTableManager;
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
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			ImportFilter importFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.connection = batchConn;
		this.databaseAdapter = databaseAdapter;
		this.tmpXlinkPool = tmpXlinkPool;
		this.cacheTableManager = cacheTableManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbWriterMap = new HashMap<DBXlinkResolverEnum, DBXlinkResolver>();
		dbGmlIdResolver = new DBGmlIdResolver(batchConn, uidCacheManager);
		dbSequencer = new DBSequencer(batchConn, databaseAdapter);
	}

	public DBXlinkResolver getDBXlinkResolver(DBXlinkResolverEnum dbResolverType) throws SQLException {
		DBXlinkResolver dbResolver = dbWriterMap.get(dbResolverType);

		if (dbResolver == null) {
			// initialise DBWriter
			switch (dbResolverType) {
			case SURFACE_GEOMETRY:
				CacheTable surfaceGeomHeapView = cacheTableManager.getCacheTable(CacheTableModelEnum.SURFACE_GEOMETRY).getMirrorTable();
				if (surfaceGeomHeapView != null)
					dbResolver = new XlinkSurfaceGeometry(connection, surfaceGeomHeapView, this);

				break;
			case BASIC:
				dbResolver = new XlinkBasic(connection, this);
				break;
			case TEXCOORDLIST:
				CacheTable texCoords = cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTURE_COORD_LIST);
				CacheTable linearRings = cacheTableManager.getCacheTable(CacheTableModelEnum.LINEAR_RING);
				if (texCoords != null && linearRings != null)
					dbResolver = new XlinkTexCoordList(connection, texCoords, linearRings, this);
				break;
			case TEXTUREPARAM:
				dbResolver = new XlinkTextureParam(connection, this);
				break;
			case XLINK_TEXTUREASSOCIATION:
				CacheTable texAssHeapView = cacheTableManager.getCacheTable(CacheTableModelEnum.TEXTUREASSOCIATION_TARGET);
				if (texAssHeapView != null)
					dbResolver = new XlinkTextureAssociation(connection, texAssHeapView, this);
				break;
			case TEXTURE_IMAGE:
				dbResolver = new XlinkTextureImage(connection, config, this);
				break;
			case SURFACE_DATA_TO_TEX_IMAGE:
				dbResolver = new XlinkSurfaceDataToTexImage(connection, this);
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
				CacheTable groupHeapView = cacheTableManager.getCacheTable(CacheTableModelEnum.GROUP_TO_CITYOBJECT).getMirrorTable();
				if (groupHeapView != null)					
					dbResolver = new XlinkGroupToCityObject(connection, groupHeapView, importFilter, this);

				break;
			case SOLID_GEOMETRY:
				dbResolver = new XlinkSolidGeometry(connection, this);
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

	public UIDCacheEntry getDBId(String gmlId, CityGMLClass type) {
		return getDBId(gmlId, type, false);
	}

	public UIDCacheEntry getDBId(String gmlId, CityGMLClass type, boolean forceCityObjectDatabaseLookup) {
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

	public AbstractDatabaseAdapter getCacheAdapter() {
		return cacheTableManager.getDatabaseAdapter();
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
