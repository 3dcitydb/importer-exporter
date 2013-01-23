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
package de.tub.citydb.modules.citygml.importer.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import oracle.ucp.jdbc.ValidConnection;
import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFileEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParamEnum;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.DBXlinkResolverEnum;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.DBXlinkResolverManager;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkBasic;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkDeprecatedMaterial;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkGroupToCityObject;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkLibraryObject;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkSurfaceGeometry;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTexCoordList;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTextureAssociation;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTextureImage;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkTextureParam;
import de.tub.citydb.modules.citygml.importer.database.xlink.resolver.XlinkWorldFile;
import de.tub.citydb.modules.common.filter.ImportFilter;

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
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CacheManager cacheManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private Connection batchConn;
	private Connection externalFileConn;
	private DBXlinkResolverManager xlinkResolverManager;
	private int updateCounter = 0;
	private int commitAfter = 20;

	public DBImportXlinkResolverWorker(DatabaseConnectionPool dbPool, 
			WorkerPool<DBXlink> tmpXlinkPool, 
			DBGmlIdLookupServerManager lookupServerManager, 
			CacheManager cacheManager, 
			ImportFilter importFilter, 
			Config config, 
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbPool = dbPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.lookupServerManager = lookupServerManager;
		this.cacheManager = cacheManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		batchConn = dbPool.getConnection();
		batchConn.setAutoCommit(false);

		externalFileConn = dbPool.getConnection();
		externalFileConn.setAutoCommit(false);
		
		Database database = config.getProject().getDatabase();

		// try and change workspace for both connections if needed
		Workspace workspace = database.getWorkspaces().getImportWorkspace();
		dbPool.gotoWorkspace(batchConn, workspace);
		dbPool.gotoWorkspace(externalFileConn, workspace);

		Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= Internal.ORACLE_MAX_BATCH_SIZE)
			commitAfter = commitAfterProp;

		xlinkResolverManager = new DBXlinkResolverManager(
				batchConn,
				externalFileConn,
				tmpXlinkPool,
				lookupServerManager,
				cacheManager,
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
			if (firstWork != null && shouldRun) {
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
				batchConn.commit();
			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}
			
			try {
				xlinkResolverManager.close();
			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}
			
		} finally {
			if (batchConn != null) {
				try {
					batchConn.close();
				} catch (SQLException e) {
					//
				}

				batchConn = null;
			}

			if (externalFileConn != null) {
				try {
					((ValidConnection)externalFileConn).setInvalid();
					externalFileConn.close();
				} catch (SQLException e) {
					//
				}

				externalFileConn = null;
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

				case TEXTUREPARAM:
					DBXlinkTextureParam textureParam = (DBXlinkTextureParam)work;
					DBXlinkTextureParamEnum subType = textureParam.getType();

					switch (subType) {
					case TEXCOORDLIST:
						XlinkTexCoordList xlinkTexCoordList = (XlinkTexCoordList)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXCOORDLIST);
						if (xlinkTexCoordList != null)
							success = xlinkTexCoordList.insert(textureParam);

						break;

					case XLINK_TEXTUREASSOCIATION:
						XlinkTextureAssociation xlinkTextureAssociation = (XlinkTextureAssociation)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.XLINK_TEXTUREASSOCIATION);
						if (xlinkTextureAssociation != null)
							success = xlinkTextureAssociation.insert(textureParam);

						break;
					case X3DMATERIAL:
					case GEOREFERENCEDTEXTURE:
					case TEXCOORDGEN:
						XlinkTextureParam xlinkTextureParam = (XlinkTextureParam)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXTUREPARAM);
						if (xlinkTextureParam != null)
							success = xlinkTextureParam.insert(textureParam);

						break;
					}

					break;

				case TEXTURE_FILE:
					DBXlinkTextureFile externalFile = (DBXlinkTextureFile)work;
					DBXlinkTextureFileEnum fileSubType = externalFile.getType();

					switch (fileSubType) {
					case TEXTURE_IMAGE:
						XlinkTextureImage xlinkTextureImage = (XlinkTextureImage)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.TEXTURE_IMAGE);

						if (xlinkTextureImage != null)
							xlinkTextureImage.insert(externalFile);

						break;

					case WORLD_FILE:
						XlinkWorldFile xlinkWorldFile = (XlinkWorldFile)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.WORLD_FILE);

						if (xlinkWorldFile != null)
							xlinkWorldFile.insert(externalFile);

						break;
					}

					// we generate error messages within the modules, so no need for
					// a global warning
					success = true;
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
					batchConn.commit();

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
