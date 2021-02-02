/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.citygml.importer.database.xlink.resolver;

import org.citydb.citygml.common.cache.CacheTable;
import org.citydb.citygml.common.cache.CacheTableManager;
import org.citydb.citygml.common.cache.model.CacheTableModel;
import org.citydb.citygml.common.cache.IdCacheEntry;
import org.citydb.citygml.common.cache.IdCacheManager;
import org.citydb.citygml.common.cache.IdCacheType;
import org.citydb.citygml.common.xlink.DBXlink;
import org.citydb.citygml.importer.database.SequenceHelper;
import org.citydb.citygml.importer.util.ConcurrentLockManager;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.file.InputFile;
import org.citydb.registry.ObjectRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBXlinkResolverManager {
	private final ConcurrentLockManager lockManager = ConcurrentLockManager.getInstance(DBXlinkResolverManager.class);
	private final InputFile inputFile;
	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final CacheTableManager cacheTableManager;
	private final EventDispatcher eventDispatcher;

	private final Map<DBXlinkResolverEnum, DBXlinkResolver> resolvers;
	private final DBGmlIdResolver gmlIdResolver;
	private final SequenceHelper sequenceHelper;

	public DBXlinkResolverManager(
			InputFile inputFile,
			Connection batchConn,
			AbstractDatabaseAdapter databaseAdapter,
			WorkerPool<DBXlink> tmpXlinkPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.inputFile = inputFile;
		this.connection = batchConn;
		this.databaseAdapter = databaseAdapter;
		this.tmpXlinkPool = tmpXlinkPool;
		this.cacheTableManager = cacheTableManager;
		this.eventDispatcher = eventDispatcher;

		resolvers = new HashMap<>();
		gmlIdResolver = new DBGmlIdResolver(batchConn, databaseAdapter, idCacheManager);
		sequenceHelper = new SequenceHelper(batchConn, databaseAdapter, config);
     	schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
	}

	public DBXlinkResolver getDBXlinkResolver(DBXlinkResolverEnum dbResolverType) throws SQLException {
		DBXlinkResolver dbResolver = resolvers.get(dbResolverType);

		if (dbResolver == null) {
			// initialise writer
			switch (dbResolverType) {
			case SURFACE_GEOMETRY:
				CacheTable surfaceGeomHeapView = cacheTableManager.getCacheTable(CacheTableModel.SURFACE_GEOMETRY).getMirrorTable();
				if (surfaceGeomHeapView != null)
					dbResolver = new XlinkSurfaceGeometry(connection, surfaceGeomHeapView, this);

				break;
			case BASIC:
				dbResolver = new XlinkBasic(connection, this);
				break;
			case TEXCOORDLIST:
				CacheTable texCoords = cacheTableManager.getCacheTable(CacheTableModel.TEXTURE_COORD_LIST);
				CacheTable linearRings = cacheTableManager.getCacheTable(CacheTableModel.LINEAR_RING);
				if (texCoords != null && linearRings != null)
					dbResolver = new XlinkTexCoordList(connection, texCoords, linearRings, this);
				break;
			case TEXTUREPARAM:
				dbResolver = new XlinkTextureParam(connection, this);
				break;
			case XLINK_TEXTUREASSOCIATION:
				CacheTable texAssHeapView = cacheTableManager.getCacheTable(CacheTableModel.TEXTUREASSOCIATION_TARGET);
				if (texAssHeapView != null)
					dbResolver = new XlinkTextureAssociation(connection, texAssHeapView, this);
				break;
			case TEXTURE_IMAGE:
				dbResolver = new XlinkTextureImage(connection, this);
				break;
			case SURFACE_DATA_TO_TEX_IMAGE:
				dbResolver = new XlinkSurfaceDataToTexImage(connection, this);
				break;
			case LIBRARY_OBJECT:
				dbResolver = new XlinkLibraryObject(connection, this);
				break;
			case XLINK_DEPRECATED_MATERIAL:
				dbResolver = new XlinkDeprecatedMaterial(connection, this);
				break;
			case GROUP_TO_CITYOBJECT:
				CacheTable groupHeapView = cacheTableManager.getCacheTable(CacheTableModel.GROUP_TO_CITYOBJECT).getMirrorTable();
				if (groupHeapView != null)					
					dbResolver = new XlinkGroupToCityObject(connection, groupHeapView, this);

				break;
			case SOLID_GEOMETRY:
				dbResolver = new XlinkSolidGeometry(connection, this);
				break;
			}

			if (dbResolver != null)
				resolvers.put(dbResolverType, dbResolver);
		}

		return dbResolver;
	}

	public long getDBId(String sequence) throws SQLException {
		return sequenceHelper.getNextSequenceValue(sequence);
	}
	
	public IdCacheEntry getObjectId(String gmlId) {
		return gmlIdResolver.getDBId(gmlId, IdCacheType.OBJECT, false);
	}

	public IdCacheEntry getObjectId(String gmlId, boolean forceCityObjectDatabaseLookup) {
		return gmlIdResolver.getDBId(gmlId, IdCacheType.OBJECT, forceCityObjectDatabaseLookup);
	}	
	
	public IdCacheEntry getGeometryId(String gmlId) {
		return gmlIdResolver.getDBId(gmlId, IdCacheType.GEOMETRY, false);
	}
	
	public FeatureType getFeatureType(int objectClassId) {
		return schemaMapping.getFeatureType(objectClassId);
	}
	
	public ObjectType getObjectType(int objectClassId) {
		return schemaMapping.getObjectType(objectClassId);
	}
	
	public AbstractObjectType<?> getAbstractObjectType(int objectClassId) {
		AbstractObjectType<?> type = getFeatureType(objectClassId);
		if (type == null)
			type = getObjectType(objectClassId);
		
		return type;
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
	
	public InputStream openStream(String fileURI) throws IOException {
		try {
			return new URL(fileURI).openStream();
		} catch (MalformedURLException ignored) {
			//
		}

		Path file = null;
		try {
			file = Paths.get(fileURI);
		} catch (InvalidPathException ignored) {
			//
		}

		if (file == null || !file.isAbsolute())
			file = inputFile.resolve(fileURI);

		return Files.newInputStream(file);
	}
	
	public void executeBatch() throws SQLException {
		for (DBXlinkResolver resolver : resolvers.values()) {
			final ReentrantLock lock = lockManager.getLock(resolver.getClass().getName());
			lock.lock();
			try {
				resolver.executeBatch();
			} finally {
				lock.unlock();
			}
		}
	}

	void executeBatch(DBXlinkResolver resolver) throws SQLException {
		final ReentrantLock lock = lockManager.getLock(resolver.getClass().getName());
		lock.lock();
		try {
			resolver.executeBatch();
		} finally {
			lock.unlock();
		}
	}

	void executeBatchWithLock(PreparedStatement ps, DBXlinkResolver resolver) throws SQLException {
		final ReentrantLock lock = lockManager.getLock(resolver.getClass().getName());
		lock.lock();
		try {
			ps.executeBatch();
		} finally {
			lock.unlock();
		}
	}

	public void close() throws SQLException {
		gmlIdResolver.close();
		sequenceHelper.close();

		for (DBXlinkResolver dbResolver : resolvers.values())
			dbResolver.close();
	}
}
