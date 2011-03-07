package de.tub.citydb.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OracleConnection;
import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.DBGmlIdLookupServerManager;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.db.xlink.DBXlinkBasic;
import de.tub.citydb.db.xlink.DBXlinkDeprecatedMaterial;
import de.tub.citydb.db.xlink.DBXlinkEnum;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citydb.db.xlink.DBXlinkGroupToCityObject;
import de.tub.citydb.db.xlink.DBXlinkSurfaceGeometry;
import de.tub.citydb.db.xlink.DBXlinkTextureParam;
import de.tub.citydb.db.xlink.DBXlinkTextureParamEnum;
import de.tub.citydb.db.xlink.resolver.DBXlinkResolverEnum;
import de.tub.citydb.db.xlink.resolver.DBXlinkResolverManager;
import de.tub.citydb.db.xlink.resolver.XlinkBasic;
import de.tub.citydb.db.xlink.resolver.XlinkDeprecatedMaterial;
import de.tub.citydb.db.xlink.resolver.XlinkGroupToCityObject;
import de.tub.citydb.db.xlink.resolver.XlinkLibraryObject;
import de.tub.citydb.db.xlink.resolver.XlinkSurfaceGeometry;
import de.tub.citydb.db.xlink.resolver.XlinkTexCoordList;
import de.tub.citydb.db.xlink.resolver.XlinkTextureAssociation;
import de.tub.citydb.db.xlink.resolver.XlinkTextureImage;
import de.tub.citydb.db.xlink.resolver.XlinkTextureParam;
import de.tub.citydb.db.xlink.resolver.XlinkWorldFile;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.filter.ImportFilter;

public class DBImportXlinkResolverWorker implements Worker<DBXlink> {
	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<DBXlink> workQueue = null;
	private DBXlink firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final DBConnectionPool dbPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final DBTempTableManager dbTempTableManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private Connection batchConn;
	private Connection externalFileConn;
	private DBXlinkResolverManager xlinkResolverManager;
	private int updateCounter = 0;
	private int commitAfter = 20;

	public DBImportXlinkResolverWorker(DBConnectionPool dbPool, 
			WorkerPool<DBXlink> tmpXlinkPool, 
			DBGmlIdLookupServerManager lookupServerManager, 
			DBTempTableManager dbTempTableManager, 
			ImportFilter importFilter, 
			Config config, 
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbPool = dbPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.lookupServerManager = lookupServerManager;
		this.dbTempTableManager = dbTempTableManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		batchConn = dbPool.getConnection();
		batchConn.setAutoCommit(false);
		((OracleConnection)batchConn).setImplicitCachingEnabled(true);
		
		externalFileConn = dbPool.getConnection();
		externalFileConn.setAutoCommit(false);

		Database database = config.getProject().getDatabase();
		
		// try and change workspace for both connections if needed
		String workspace = database.getWorkspace().getImportWorkspace();
		dbPool.changeWorkspace(batchConn, workspace);
		dbPool.changeWorkspace(externalFileConn, workspace);
		
		Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= Internal.ORACLE_MAX_BATCH_SIZE)
			commitAfter = commitAfterProp;

		xlinkResolverManager = new DBXlinkResolverManager(
				batchConn,
				externalFileConn,
				tmpXlinkPool,
				lookupServerManager,
				dbTempTableManager,
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
			batchConn.commit();

		} catch (SQLException sqlEx) {
			LogMessageEvent log = new LogMessageEvent(
					"SQL-Fehler: " + sqlEx.getMessage(),
					LogMessageEnum.ERROR);
			eventDispatcher.triggerEvent(log);
			return;
		}

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
				externalFileConn.close();
			} catch (SQLException e) {
				//
			}

			externalFileConn = null;
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

				case EXTERNAL_FILE:
					DBXlinkExternalFile externalFile = (DBXlinkExternalFile)work;
					DBXlinkExternalFileEnum fileSubType = externalFile.getType();

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

					case LIBRARY_OBJECT:
						XlinkLibraryObject xlinkLibraryObject = (XlinkLibraryObject)xlinkResolverManager.getDBXlinkResolver(DBXlinkResolverEnum.LIBRARY_OBJECT);

						if (xlinkLibraryObject != null)
							xlinkLibraryObject.insert(externalFile);

						break;
					}

					// we generate error messages within the modules, so no need for
					// a global warning
					success = true;
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
					LogMessageEvent log = new LogMessageEvent(
							"Xlink-Verweis \"" + work.getGmlId() + "\" konnte nicht aufgelöst werden.",
							LogMessageEnum.ERROR);
					eventDispatcher.triggerEvent(log);
				} else
					updateCounter++;

			} catch (SQLException sqlEx) {
				LogMessageEvent log = new LogMessageEvent(
						"SQL-Fehler: " + sqlEx,
						LogMessageEnum.ERROR);
				eventDispatcher.triggerEvent(log);
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
				LogMessageEvent log = new LogMessageEvent(
						"SQL-Fehler: " + sqlEx,
						LogMessageEnum.ERROR);
				eventDispatcher.triggerEvent(log);
				return;
			}


		} finally {
			runLock.unlock();
		}
	}
}
