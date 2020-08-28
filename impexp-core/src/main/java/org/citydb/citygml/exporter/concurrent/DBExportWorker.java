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
package org.citydb.citygml.exporter.concurrent;

import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.database.content.CityGMLExportManager;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.global.LogLevel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.GeometryCounterEvent;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.file.OutputFile;
import org.citydb.plugin.PluginException;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.export.CityGMLExportExtension;
import org.citydb.query.Query;
import org.citydb.util.CoreConstants;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DBExportWorker extends Worker<DBSplittingResult> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final Connection connection;
	private final CityGMLExportManager exporter;
	private final FeatureWriter featureWriter;
	private final List<CityGMLExportExtension> plugins;
	private final EventDispatcher eventDispatcher;
	private final Config config;

	private int globalAppearanceCounter = 0;
	private int topLevelFeatureCounter = 0;

	public DBExportWorker(OutputFile outputFile,
			Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			FeatureWriter featureWriter,
			WorkerPool<DBXlink> xlinkPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			Query query,
			Config config,
			EventDispatcher eventDispatcher) throws CityGMLExportException {
		this.connection = connection;
		this.featureWriter = featureWriter;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		exporter = new CityGMLExportManager(
				outputFile,
				connection,
				query,
				databaseAdapter,
				schemaMapping, 
				cityGMLBuilder,
				featureWriter,
				xlinkPool,
				uidCacheManager,
				cacheTableManager,
				config);

		plugins = PluginManager.getInstance().getExternalPlugins(CityGMLExportExtension.class);
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
					DBSplittingResult work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}

			eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, topLevelFeatureCounter, this));
			eventDispatcher.triggerEvent(new CounterEvent(CounterType.GLOBAL_APPEARANCE, globalAppearanceCounter, this));
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, topLevelFeatureCounter + globalAppearanceCounter, this));
			eventDispatcher.triggerEvent(new ObjectCounterEvent(exporter.getAndResetObjectCounter(), this));
			eventDispatcher.triggerEvent(new GeometryCounterEvent(exporter.getAndResetGeometryCounter(), this));
		} finally {
			try {
				exporter.close();
			} catch (CityGMLExportException | SQLException e) {
				//
			}

			try {
				connection.close();
			} catch (SQLException e) {
				//
			}

			eventDispatcher.removeEventHandler(this);
		}
	}

	private void doWork(DBSplittingResult work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;

			AbstractFeature feature = null;
			if (work.getObjectType().getObjectClassId() == MappingConstants.APPEARANCE_OBJECTCLASS_ID)
				feature = exporter.exportGlobalAppearance(work.getId());
			else {
				AbstractGML object = exporter.exportObject(work.getId(), work.getObjectType(), false);
				if (object instanceof AbstractFeature) {
					// execute batch export
					exporter.executeBatch();

					if (!exporter.isTiledExport() || !object.hasLocalProperty(CoreConstants.NOT_ON_TILE)) {
						feature = (AbstractFeature) object;

						// remove empty city objects and clean up appearances if LoDs are filtered
						if (!exporter.getLodFilter().preservesGeometry()) {
							exporter.cleanupCityObjects(feature);
							exporter.cleanupAppearances(feature);
						}

						// trigger export of textures if required
						if (exporter.isLazyTextureExport())
							exporter.triggerLazyTextureExport(feature);
					}
				}
			}

			if (feature != null) {
				// invoke export plugins
				if (!plugins.isEmpty()) {
					for (CityGMLExportExtension plugin : plugins) {
						feature = plugin.postprocess((AbstractFeature) feature);
						if (feature == null) {
							featureWriter.updateSequenceId(work.getSequenceId());
							return;
						}
					}
				}

				// write feature
				featureWriter.write((AbstractFeature) feature, work.getSequenceId());

				// register gml:id in cache
				if (config.getInternal().isRegisterGmlIdInCache() && feature.isSetId())
					exporter.putObjectUID(feature.getId(), work.getId(), work.getObjectType().getObjectClassId());
				
				// update export counter
				exporter.updateExportCounter(feature);
				if (feature instanceof Appearance) {
					if (++globalAppearanceCounter == 20) {
						eventDispatcher.triggerEvent(new CounterEvent(CounterType.GLOBAL_APPEARANCE, globalAppearanceCounter, this));
						eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, globalAppearanceCounter, this));
						globalAppearanceCounter = 0;
					}
				} else if (++topLevelFeatureCounter == 20) {
					eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, topLevelFeatureCounter, this));
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, topLevelFeatureCounter, this));
					topLevelFeatureCounter = 0;
				}
			} else
				featureWriter.updateSequenceId(work.getSequenceId());

		} catch (SQLException | CityGMLExportException | FeatureWriteException | PluginException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Aborting export due to errors.", LogLevel.WARN, e, eventChannel, this));
		} catch (Throwable e) {
			// this is to catch general exceptions that may occur during the export
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
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
