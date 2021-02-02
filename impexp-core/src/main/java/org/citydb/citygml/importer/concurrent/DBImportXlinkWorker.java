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
package org.citydb.citygml.importer.concurrent;

import org.citydb.citygml.common.cache.CacheTableManager;
import org.citydb.citygml.common.xlink.DBXlink;
import org.citydb.citygml.common.xlink.DBXlinkBasic;
import org.citydb.citygml.common.xlink.DBXlinkDeprecatedMaterial;
import org.citydb.citygml.common.xlink.DBXlinkGroupToCityObject;
import org.citydb.citygml.common.xlink.DBXlinkLibraryObject;
import org.citydb.citygml.common.xlink.DBXlinkLinearRing;
import org.citydb.citygml.common.xlink.DBXlinkSolidGeometry;
import org.citydb.citygml.common.xlink.DBXlinkSurfaceDataToTexImage;
import org.citydb.citygml.common.xlink.DBXlinkSurfaceGeometry;
import org.citydb.citygml.common.xlink.DBXlinkTextureAssociation;
import org.citydb.citygml.common.xlink.DBXlinkTextureAssociationTarget;
import org.citydb.citygml.common.xlink.DBXlinkTextureCoordList;
import org.citydb.citygml.common.xlink.DBXlinkTextureFile;
import org.citydb.citygml.common.xlink.DBXlinkTextureParam;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterBasic;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterDeprecatedMaterial;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterEnum;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterGroupToCityObject;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterLibraryObject;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterLinearRing;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterManager;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterSolidGeometry;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterSurfaceDataToTexImage;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterSurfaceGeometry;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterTextureAssociation;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterTextureAssociationTarget;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterTextureCoordList;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterTextureFile;
import org.citydb.citygml.importer.database.xlink.importer.DBXlinkImporterTextureParam;
import org.citydb.concurrent.Worker;
import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.global.LogLevel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;

import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class DBImportXlinkWorker extends Worker<DBXlink> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;
	
	private final DBXlinkImporterManager dbXlinkManager;
	private final EventDispatcher eventDispatcher;
	private int updateCounter = 0;
	private int commitAfter;

	public DBImportXlinkWorker(CacheTableManager cacheTableManager, Config config, EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
		dbXlinkManager = new DBXlinkImporterManager(cacheTableManager, eventDispatcher);

		AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		DatabaseConfig databaseConfig = config.getDatabaseConfig();

		commitAfter = databaseConfig.getImportBatching().getTempBatchSize();
		if (commitAfter > databaseAdapter.getMaxBatchSize())
			commitAfter = databaseAdapter.getMaxBatchSize();

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	@Override
	public void interrupt() {
		shouldRun = false;
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
		} catch (Throwable e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during import.", LogLevel.ERROR, e, eventChannel, this));
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

		} catch (Throwable e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during import.", LogLevel.ERROR, e, eventChannel, this));
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
