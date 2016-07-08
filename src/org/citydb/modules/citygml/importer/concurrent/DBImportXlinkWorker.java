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

import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.log.LogLevel;
import org.citydb.config.Config;
import org.citydb.config.project.database.Database;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkBasic;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkGroupToCityObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkLinearRing;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSolidGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkSurfaceGeometry;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociation;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureCoordList;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import org.citydb.modules.citygml.common.database.xlink.DBXlinkTextureParam;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterBasic;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterDeprecatedMaterial;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterEnum;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterGroupToCityObject;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterLibraryObject;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterLinearRing;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterManager;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterSolidGeometry;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterSurfaceDataToTexImage;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterSurfaceGeometry;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterTextureAssociation;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterTextureAssociationTarget;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterTextureCoordList;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterTextureFile;
import org.citydb.modules.citygml.importer.database.xlink.importer.DBXlinkImporterTextureParam;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;

public class DBImportXlinkWorker extends Worker<DBXlink> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;
	
	private final Config config;
	private DBXlinkImporterManager dbXlinkManager;
	private final EventDispatcher eventDispatcher;
	private int updateCounter = 0;
	private int commitAfter = 1000;

	public DBImportXlinkWorker(DatabaseConnectionPool dbPool,
			CacheTableManager cacheTableManager, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		dbXlinkManager = new DBXlinkImporterManager(cacheTableManager, eventDispatcher);

		init(dbPool);		
	}

	private void init(DatabaseConnectionPool dbPool) {
		Database database = config.getProject().getDatabase();

		Integer commitAfterProp = database.getUpdateBatching().getTempBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0 && commitAfterProp <= dbPool.getActiveDatabaseAdapter().getMaxBatchSize())
			commitAfter = commitAfterProp;

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
			if (shouldWork)
				dbXlinkManager.executeBatch();
		} catch (SQLException e) {
			eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting import due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
		} finally {
			try {
				dbXlinkManager.close();
			} catch (SQLException e) {
				//
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

			switch (work.getXlinkType()) {
			case SURFACE_GEOMETRY:
				DBXlinkSurfaceGeometry xlinkSurfaceGeometry = (DBXlinkSurfaceGeometry)work;
				DBXlinkImporterSurfaceGeometry dbSurfaceGeometry = (DBXlinkImporterSurfaceGeometry)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.SURFACE_GEOMETRY);
				if (dbSurfaceGeometry != null)
					success = dbSurfaceGeometry.insert(xlinkSurfaceGeometry);

				break;
			case LINEAR_RING:
				DBXlinkLinearRing xlinkLinearRing = (DBXlinkLinearRing)work;
				DBXlinkImporterLinearRing dbLinearRing = (DBXlinkImporterLinearRing)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.LINEAR_RING);
				if (dbLinearRing != null)
					success = dbLinearRing.insert(xlinkLinearRing);

				break;
			case BASIC:
				DBXlinkBasic xlinkBasic = (DBXlinkBasic)work;
				DBXlinkImporterBasic dbBasic = (DBXlinkImporterBasic)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.XLINK_BASIC);
				if (dbBasic != null)
					success = dbBasic.insert(xlinkBasic);

				break;
			case TEXTURE_COORD_LIST:
				DBXlinkTextureCoordList xlinkTexCoord = (DBXlinkTextureCoordList)work;
				DBXlinkImporterTextureCoordList dbTexCoord = (DBXlinkImporterTextureCoordList)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.XLINK_TEXTURE_COORD_LIST);
				if (dbTexCoord != null)
					success = dbTexCoord.insert(xlinkTexCoord);

				break;
			case TEXTUREPARAM:
				DBXlinkTextureParam xlinkAppearance = (DBXlinkTextureParam)work;
				DBXlinkImporterTextureParam dbAppearance = (DBXlinkImporterTextureParam)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.XLINK_TEXTUREPARAM);
				if (dbAppearance != null)
					success = dbAppearance.insert(xlinkAppearance);

				break;
			case TEXTUREASSOCIATION:
				DBXlinkTextureAssociation xlinkTextureAss = (DBXlinkTextureAssociation)work;
				DBXlinkImporterTextureAssociation dbTexAss = (DBXlinkImporterTextureAssociation)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.XLINK_TEXTUREASSOCIATION);
				if (dbTexAss != null)
					success = dbTexAss.insert(xlinkTextureAss);

				break;
			case TEXTUREASSOCIATION_TARGET:
				DBXlinkTextureAssociationTarget xlinkTextureAssTarget = (DBXlinkTextureAssociationTarget)work;
				DBXlinkImporterTextureAssociationTarget dbTexAssTarget = (DBXlinkImporterTextureAssociationTarget)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.TEXTUREASSOCIATION_TARGET);
				if (dbTexAssTarget != null)
					success = dbTexAssTarget.insert(xlinkTextureAssTarget);

				break;
			case TEXTURE_FILE:
				DBXlinkTextureFile xlinkFile = (DBXlinkTextureFile)work;
				DBXlinkImporterTextureFile dbFile = (DBXlinkImporterTextureFile)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.TEXTURE_FILE);
				if (dbFile != null)
					success = dbFile.insert(xlinkFile);

				break;
			case SURFACE_DATA_TO_TEX_IMAGE:
				DBXlinkSurfaceDataToTexImage xlinkSurfData = (DBXlinkSurfaceDataToTexImage)work;
				DBXlinkImporterSurfaceDataToTexImage dbSurfData = (DBXlinkImporterSurfaceDataToTexImage)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.SURFACE_DATA_TO_TEX_IMAGE);
				if (dbSurfData != null)
					success = dbSurfData.insert(xlinkSurfData);

				break;
			case LIBRARY_OBJECT:
				DBXlinkLibraryObject xlinkLibraryObject = (DBXlinkLibraryObject)work;
				DBXlinkImporterLibraryObject dbLibraryObject = (DBXlinkImporterLibraryObject)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.LIBRARY_OBJECT);
				if (dbLibraryObject != null)
					success = dbLibraryObject.insert(xlinkLibraryObject);

				break;
			case DEPRECATED_MATERIAL:
				DBXlinkDeprecatedMaterial xlinkDeprecatedMaterial = (DBXlinkDeprecatedMaterial)work;
				DBXlinkImporterDeprecatedMaterial dbDeprectatedMaterial = (DBXlinkImporterDeprecatedMaterial)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.XLINK_DEPRECATED_MATERIAL);
				if (dbDeprectatedMaterial != null)
					success = dbDeprectatedMaterial.insert(xlinkDeprecatedMaterial);

				break;
			case GROUP_TO_CITYOBJECT:
				DBXlinkGroupToCityObject xlinkGroupToCityObject = (DBXlinkGroupToCityObject)work;
				DBXlinkImporterGroupToCityObject dbGroup = (DBXlinkImporterGroupToCityObject)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.GROUP_TO_CITYOBJECT);
				if (dbGroup != null)
					success = dbGroup.insert(xlinkGroupToCityObject);

				break;
			case SOLID_GEOMETRY:
				DBXlinkSolidGeometry xlinkSolidGeometry = (DBXlinkSolidGeometry)work;
				DBXlinkImporterSolidGeometry solidGeometry = (DBXlinkImporterSolidGeometry)dbXlinkManager.getDBImporterXlink(DBXlinkImporterEnum.SOLID_GEOMETRY);
				if (solidGeometry != null)
					success = solidGeometry.insert(xlinkSolidGeometry);

				break;
			}

			if (success)
				updateCounter++;

			if (updateCounter == commitAfter) {
				dbXlinkManager.executeBatch();
				updateCounter = 0;
			}

		} catch (SQLException e) {
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
