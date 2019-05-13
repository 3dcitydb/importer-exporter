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

import org.citydb.citygml.common.database.cache.CacheTable;
import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.cache.model.CacheTableModel;
import org.citydb.citygml.common.database.uid.UIDCacheEntry;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.importer.database.SequenceHelper;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.file.InputFile;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
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
import java.sql.SQLException;
import java.util.HashMap;

public class DBXlinkResolverManager {
	private final InputFile inputFile;
	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final CacheTableManager cacheTableManager;
	private final EventDispatcher eventDispatcher;

	private HashMap<DBXlinkResolverEnum, DBXlinkResolver> dbWriterMap;
	private DBGmlIdResolver dbGmlIdResolver;
	private SequenceHelper sequenceHelper;

	public DBXlinkResolverManager(
			InputFile inputFile,
			Connection batchConn,
			AbstractDatabaseAdapter databaseAdapter,
			WorkerPool<DBXlink> tmpXlinkPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.inputFile = inputFile;
		this.connection = batchConn;
		this.databaseAdapter = databaseAdapter;
		this.tmpXlinkPool = tmpXlinkPool;
		this.cacheTableManager = cacheTableManager;
		this.eventDispatcher = eventDispatcher;

		dbWriterMap = new HashMap<>();
		dbGmlIdResolver = new DBGmlIdResolver(batchConn, databaseAdapter, uidCacheManager);
		sequenceHelper = new SequenceHelper(batchConn, databaseAdapter, config);

     	schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
	}

	public DBXlinkResolver getDBXlinkResolver(DBXlinkResolverEnum dbResolverType) throws SQLException {
		DBXlinkResolver dbResolver = dbWriterMap.get(dbResolverType);

		if (dbResolver == null) {
			// initialise DBWriter
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
				dbWriterMap.put(dbResolverType, dbResolver);
		}

		return dbResolver;
	}

	public long getDBId(String sequence) throws SQLException {
		return sequenceHelper.getNextSequenceValue(sequence);
	}
	
	public UIDCacheEntry getObjectId(String gmlId) {
		return dbGmlIdResolver.getDBId(gmlId, UIDCacheType.OBJECT, false);
	}

	public UIDCacheEntry getObjectId(String gmlId, boolean forceCityObjectDatabaseLookup) {
		return dbGmlIdResolver.getDBId(gmlId, UIDCacheType.OBJECT, forceCityObjectDatabaseLookup);
	}	
	
	public UIDCacheEntry getGeometryId(String gmlId) {
		return dbGmlIdResolver.getDBId(gmlId, UIDCacheType.GEOMETRY, false);
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
		for (DBXlinkResolver dbResolver : dbWriterMap.values())
			dbResolver.executeBatch();
	}

	public void close() throws SQLException {
		dbGmlIdResolver.close();
		sequenceHelper.close();

		for (DBXlinkResolver dbResolver : dbWriterMap.values())
			dbResolver.close();
	}
}
