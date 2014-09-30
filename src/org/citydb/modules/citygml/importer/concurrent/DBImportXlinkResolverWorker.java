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
package org.citydb.modules.citygml.importer.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.concurrent.WorkerPool.WorkQueue;
import org.citydb.api.event.EventDispatcher;
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
import org.citydb.modules.common.filter.ImportFilter;

public class DBImportXlinkResolverWorker implements Worker<DBXlink> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<DBXlink> workQueue = null;
	private DBXlink firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
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
	}

	@Override
	public Thread getThread() {
		return workerThread;
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
	public void setFirstWork(DBXlink firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<DBXlink> workQueue) {
		this.workQueue = workQueue;
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
				xlinkResolverManager.executeBatch();
				connection.commit();
			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}

			try {
				xlinkResolverManager.close();
			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}

		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}

				connection = null;
			}
		}
	}

	private void doWork(DBXlink work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {

			try {
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

			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
				return;
			}

			try {
				if (updateCounter == commitAfter) {
					xlinkResolverManager.executeBatch();
					connection.commit();

					updateCounter = 0;
				}
			} catch (SQLException sqlEx) {
				// uh, batch update did not work. this is serious...
				LOG.error("SQL error: " + sqlEx.getMessage());
				return;
			}


		} finally {
			runLock.unlock();
		}
	}
}
