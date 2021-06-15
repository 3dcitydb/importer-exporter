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
package org.citydb.core.operation.importer.concurrent;

import org.citydb.core.concurrent.Worker;
import org.citydb.core.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.global.LogLevel;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.event.Event;
import org.citydb.core.event.EventDispatcher;
import org.citydb.core.event.EventHandler;
import org.citydb.core.event.global.EventType;
import org.citydb.core.event.global.InterruptEvent;
import org.citydb.core.file.InputFile;
import org.citydb.core.log.Logger;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.common.xlink.DBXlinkBasic;
import org.citydb.core.operation.common.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.core.operation.common.xlink.DBXlinkEnum;
import org.citydb.core.operation.common.xlink.DBXlinkGroupToCityObject;
import org.citydb.core.operation.common.xlink.DBXlinkLibraryObject;
import org.citydb.core.operation.common.xlink.DBXlinkSolidGeometry;
import org.citydb.core.operation.common.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.core.operation.common.xlink.DBXlinkSurfaceGeometry;
import org.citydb.core.operation.common.xlink.DBXlinkTextureAssociation;
import org.citydb.core.operation.common.xlink.DBXlinkTextureCoordList;
import org.citydb.core.operation.common.xlink.DBXlinkTextureFile;
import org.citydb.core.operation.common.xlink.DBXlinkTextureParam;
import org.citydb.core.operation.common.xlink.DBXlinkTextureParamEnum;
import org.citydb.core.operation.importer.database.xlink.resolver.DBXlinkResolverEnum;
import org.citydb.core.operation.importer.database.xlink.resolver.DBXlinkResolverManager;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkBasic;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkDeprecatedMaterial;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkGroupToCityObject;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkLibraryObject;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkSolidGeometry;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkSurfaceDataToTexImage;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkSurfaceGeometry;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkTexCoordList;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkTextureAssociation;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkTextureImage;
import org.citydb.core.operation.importer.database.xlink.resolver.XlinkTextureParam;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class DBImportXlinkResolverWorker extends Worker<DBXlink> implements EventHandler {
	private final Logger log = Logger.getInstance();
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final Connection connection;
	private final boolean isManagedTransaction;
	private final DBXlinkResolverManager xlinkResolverManager;
	private final EventDispatcher eventDispatcher;

	private int updateCounter = 0;
	private int commitAfter;

	public DBImportXlinkResolverWorker(InputFile inputFile,
			Connection connection,
			boolean isManagedTransaction,
			AbstractDatabaseAdapter databaseAdapter,
			WorkerPool<DBXlink> tmpXlinkPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.connection = connection;
		this.isManagedTransaction = isManagedTransaction;
		this.eventDispatcher = eventDispatcher;

		commitAfter = config.getDatabaseConfig().getImportBatching().getFeatureBatchSize();
		if (commitAfter > databaseAdapter.getMaxBatchSize())
			commitAfter = databaseAdapter.getMaxBatchSize();

		xlinkResolverManager = new DBXlinkResolverManager(
				inputFile,
				connection,
				databaseAdapter,
				tmpXlinkPool,
				idCacheManager,
				cacheTableManager,
				config,
				eventDispatcher);

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	@Override
	public void interrupt() {
		shouldRun = false;
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
					if (!isManagedTransaction)
						connection.commit();
				}
			} catch (Throwable e) {
				try {
					connection.rollback();
				} catch (SQLException sql) {
					//
				}

				eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during XLink resolving.", LogLevel.ERROR, e, eventChannel, this));
			}
		} finally {
			try {
				xlinkResolverManager.close();
			} catch (SQLException e1) {
				//
			}

			if (!isManagedTransaction) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}
			}

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
				XlinkTextureImage xlinkTextureImage = (XlinkTextureImage) xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXTURE_IMAGE);
				if (xlinkTextureImage != null)
					xlinkTextureImage.insert(externalFile);

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
				log.error("Failed to resolve XLink reference '" + work.getGmlId() + "'.");
			} else
				updateCounter++;

			if (updateCounter == commitAfter) {
				xlinkResolverManager.executeBatch();
				if (!isManagedTransaction)
					connection.commit();

				updateCounter = 0;
			}

		} catch (Throwable e) {
			try {
				connection.rollback();
			} catch (SQLException sql) {
				//
			}

			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during XLink resolving.", LogLevel.WARN, e, eventChannel, this));
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
