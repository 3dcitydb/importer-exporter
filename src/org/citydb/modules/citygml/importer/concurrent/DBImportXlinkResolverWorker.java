/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.log.LogLevel;
import org.citydb.config.Config;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkEnum;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSolidGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureCoordList;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import org.citydb.modules.citygml.importer.database.xlink.resolver.DBXlinkResolverEnum;
import org.citydb.modules.citygml.importer.database.xlink.resolver.DBXlinkResolverManager;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkBasic;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkDeprecatedMaterial;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkGroupToCityObject;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkLibraryObject;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkSolidGeometry;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkSurfaceDataToTexImage;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkSurfaceGeometry;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTexCoordList;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTextureAssociation;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTextureImage;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTextureParam;
import org.citydb.modules.citygml.importer.database.xlink.resolver.XlinkWorldFile;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;
import org.citydb.modules.common.filter.ImportFilter;

public class DBImportXlinkResolverWorker extends Worker<DBXlink> implements EventHandler {
	private final Logger LOG = Logger.getInstance();
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final DatabaseConnectionPool dbPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final UIDCacheManager uidCacheManager;
	private final CacheTableManager cacheTableManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private Connection connection;
	private DBXlinkResolverManager xlinkResolverManager;
	private int updateCounter = 0;
	private int commitAfter = 20;

	public DBImportXlinkResolverWorker(DatabaseConnectionPool dbPool, 
			WorkerPool<DBXlink> tmpXlinkPool, 
			UIDCacheManager uidCacheManager, 
			CacheTableManager cacheTableManager, 
			ImportFilter importFilter, 
			Config config, 
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbPool = dbPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.uidCacheManager = uidCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		connection = dbPool.getConnection();
		connection.setAutoCommit(false);

		Database database = config.getProject().getDatabase();

		// try and change workspace for the connection if needed
		if (dbPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			Workspace workspace = database.getWorkspaces().getImportWorkspace();
			dbPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(connection, workspace);
		}

		Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= dbPool.getActiveDatabaseAdapter().getMaxBatchSize())
			commitAfter = commitAfterProp;

		xlinkResolverManager = new DBXlinkResolverManager(
				connection,
				dbPool.getActiveDatabaseAdapter(),
				tmpXlinkPool,
				uidCacheManager,
				cacheTableManager,
				importFilter,
				config,
				eventDispatcher);

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	@Override
	public void interrupt() {
		shouldRun = false;
		workerThread.interrupt();
	}

	@Override
	public void interruptIfIdle() {
		final ReentrantLock runLock = this.runLock;
		shouldRun = false;

		if (runLock.tryLock()) {
			try {
				workerThread.interrupt();
			} finally {
				runLock.unlock();
			}
		}
	}

	@Override
	public void run() {
		try {
			if (firstWork != null) {
				doWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					DBXlink work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}

			try {
				if (shouldWork) {
					xlinkResolverManager.executeBatch();
					connection.commit();
				}
			} catch (SQLException e) {
				try {
					connection.rollback();
				} catch (SQLException sql) {
					//
				}

				eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting import due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
			}

		} finally {
			try {
				xlinkResolverManager.close();
			} catch (SQLException e1) {
				//
			}

			try {
				connection.close();
			} catch (SQLException e) {
				//
			}

			connection = null;
			eventDispatcher.removeEventHandler(this);
		}
	}

	private void doWork(DBXlink work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;

			boolean success = false;
			DBXlinkEnum type = work.getXlinkType();

			switch (type) {
			case SURFACE_GEOMETRY:
				DBXlinkSurfaceGeometry surfaceGeometry = (DBXlinkSurfaceGeometry)work;
				XlinkSurfaceGeometry xlinkSurfaceGeometry = (XlinkSurfaceGeometry)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.SURFACE_GEOMETRY);
				if (xlinkSurfaceGeometry != null)
					success = xlinkSurfaceGeometry.insert(surfaceGeometry);

				break;
			case BASIC:
				DBXlinkBasic basic = (DBXlinkBasic)work;
				XlinkBasic xlinkBasic = (XlinkBasic)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.BASIC);
				if (xlinkBasic != null)
					success = xlinkBasic.insert(basic);

				break;
			case TEXTURE_COORD_LIST:
				DBXlinkTextureCoordList texCoord = (DBXlinkTextureCoordList)work;
				XlinkTexCoordList xlinkTexCoordList = (XlinkTexCoordList)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXCOORDLIST);
				if (xlinkTexCoordList != null)
					success = xlinkTexCoordList.insert(texCoord);

				break;					
			case TEXTUREPARAM:
				DBXlinkTextureParam textureParam = (DBXlinkTextureParam)work;
				DBXlinkTextureParamEnum subType = textureParam.getType();

				switch (subType) {
				case X3DMATERIAL:
				case GEOREFERENCEDTEXTURE:
				case TEXCOORDGEN:
					XlinkTextureParam xlinkTextureParam = (XlinkTextureParam)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXTUREPARAM);
					if (xlinkTextureParam != null)
						success = xlinkTextureParam.insert(textureParam);

					break;
				case UNDEFINED:
					// nothing to do
				}

				break;
			case TEXTUREASSOCIATION:
				DBXlinkTextureAssociation textureAssociation = (DBXlinkTextureAssociation)work;
				XlinkTextureAssociation xlinkTextureAssociation = (XlinkTextureAssociation)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.XLINK_TEXTUREASSOCIATION);
				if (xlinkTextureAssociation != null)
					success = xlinkTextureAssociation.insert(textureAssociation);

				break;
			case TEXTURE_FILE:
				DBXlinkTextureFile externalFile = (DBXlinkTextureFile)work;

				if (!externalFile.isWorldFile()) {
					XlinkTextureImage xlinkTextureImage = (XlinkTextureImage)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXTURE_IMAGE);
					if (xlinkTextureImage != null)
						xlinkTextureImage.insert(externalFile);
				} else {
					XlinkWorldFile xlinkWorldFile = (XlinkWorldFile)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.WORLD_FILE);
					if (xlinkWorldFile != null)
						xlinkWorldFile.insert(externalFile);
				}

				// we generate error messages within the modules, so no need for
				// a global warning
				success = true;
				break;
			case SURFACE_DATA_TO_TEX_IMAGE:
				DBXlinkSurfaceDataToTexImage surfData = (DBXlinkSurfaceDataToTexImage)work;
				XlinkSurfaceDataToTexImage xlinkSurfData = (XlinkSurfaceDataToTexImage)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.SURFACE_DATA_TO_TEX_IMAGE);
				if (xlinkSurfData != null)
					success = xlinkSurfData.insert(surfData);

				break;
			case LIBRARY_OBJECT:
				DBXlinkLibraryObject libObject = (DBXlinkLibraryObject)work;
				XlinkLibraryObject xlinkLibraryObject = (XlinkLibraryObject)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.LIBRARY_OBJECT);
				if (xlinkLibraryObject != null)
					success = xlinkLibraryObject.insert(libObject);

				break;
			case DEPRECATED_MATERIAL:
				DBXlinkDeprecatedMaterial depMaterial = (DBXlinkDeprecatedMaterial)work;
				XlinkDeprecatedMaterial xlinkDeprecatedMaterial = (XlinkDeprecatedMaterial)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.XLINK_DEPRECATED_MATERIAL);
				if (xlinkDeprecatedMaterial != null)
					success = xlinkDeprecatedMaterial.insert(depMaterial);

				break;
			case GROUP_TO_CITYOBJECT:
				DBXlinkGroupToCityObject groupMember = (DBXlinkGroupToCityObject)work;
				XlinkGroupToCityObject xlinkGroupToCityObject = (XlinkGroupToCityObject)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.GROUP_TO_CITYOBJECT);
				if (xlinkGroupToCityObject != null)
					success = xlinkGroupToCityObject.insert(groupMember);

				break;
			case SOLID_GEOMETRY:
				DBXlinkSolidGeometry solidGeometry = (DBXlinkSolidGeometry)work;
				XlinkSolidGeometry xlinkSolidGeometry = (XlinkSolidGeometry)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.SOLID_GEOMETRY);
				if (xlinkSolidGeometry != null)
					success = xlinkSolidGeometry.insert(solidGeometry);

				break;
			default:
				return;
			}

			if (!success) {
				LOG.error("Failed to resolve XLink reference '" + work.getGmlId() + "'.");
			} else
				updateCounter++;

			if (updateCounter == commitAfter) {
				xlinkResolverManager.executeBatch();
				connection.commit();
				updateCounter = 0;
			}

		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException sql) {
				//
			}

			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting import due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
		} catch (Exception e) {
			// this is to catch general exceptions that may occur during the import
			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.UNKNOWN_ERROR, "Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			runLock.unlock();
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldWork = false;
	}

}
