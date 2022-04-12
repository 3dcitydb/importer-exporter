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
package org.citydb.core.operation.exporter.concurrent;

import org.citydb.config.Config;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Point;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.exporter.FeatureEnvelopeMode;
import org.citydb.config.project.global.LogLevel;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.util.AffineTransformer;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.database.content.CityGMLExportManager;
import org.citydb.core.operation.exporter.database.content.DBSplittingResult;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriter;
import org.citydb.core.query.Query;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.tiling.Tile;
import org.citydb.core.util.CoreConstants;
import org.citydb.util.concurrent.Worker;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.util.bbox.BoundingBoxOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class DBExportWorker extends Worker<DBSplittingResult> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final Connection connection;
	private final CityGMLExportManager exporter;
	private final FeatureWriter featureWriter;
	private final EventDispatcher eventDispatcher;
	private final InternalConfig internalConfig;
	private final boolean replaceIds;
	private final boolean useTiling;
	private final boolean calculateExtent;
	private final BoundingBoxOptions bboxOptions;

	private Tile activeTile;
	private DatabaseSrs targetSrs;
	private int globalAppearanceCounter = 0;
	private int topLevelFeatureCounter = 0;

	public DBExportWorker(Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			FeatureWriter featureWriter,
			WorkerPool<DBXlink> xlinkPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager,
			Query query,
			AffineTransformer affineTransformer,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) throws CityGMLExportException {
		this.connection = connection;
		this.featureWriter = featureWriter;
		this.eventDispatcher = eventDispatcher;
		this.internalConfig = internalConfig;

		replaceIds = config.getExportConfig().getResourceId().isReplaceWithUUIDs();

		useTiling = query.isSetTiling();
		if (useTiling) {
			activeTile = query.getTiling().getActiveTile();
			targetSrs = query.getTargetSrs();
		}

		calculateExtent = query.isSetLodFilter() && !query.getLodFilter().preservesGeometry();
		bboxOptions = calculateExtent ? BoundingBoxOptions.defaults()
				.assignResultToFeatures(config.getExportConfig().getGeneralOptions().getEnvelope().getFeatureMode() == FeatureEnvelopeMode.ALL)
				: null;

		exporter = new CityGMLExportManager(
				connection,
				query,
				databaseAdapter,
				schemaMapping,
				cityGMLBuilder,
				featureWriter,
				xlinkPool,
				idCacheManager,
				cacheTableManager,
				affineTransformer,
				internalConfig,
				config);

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
			if (work.getObjectType().getObjectClassId() == MappingConstants.APPEARANCE_OBJECTCLASS_ID) {
				feature = exporter.exportGlobalAppearance(work.getId());
				if (feature != null && ++globalAppearanceCounter == 20) {
					eventDispatcher.triggerEvent(new CounterEvent(CounterType.GLOBAL_APPEARANCE, globalAppearanceCounter, this));
					eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, globalAppearanceCounter, this));
					globalAppearanceCounter = 0;
				}
			} else {
				if (!useTiling || isOnTile(work.getEnvelope())) {
					AbstractGML object = exporter.exportObject(work.getId(), work.getObjectType());
					if (object instanceof AbstractFeature) {
						feature = (AbstractFeature) object;

						if (calculateExtent && feature.isSetBoundedBy()) {
							BoundingShape boundedBy = feature.calcBoundedBy(bboxOptions);
							boundedBy.getEnvelope().setSrsName(feature.getBoundedBy().getEnvelope().getSrsName());
							feature.setBoundedBy(boundedBy);
						}

						if (++topLevelFeatureCounter == 20) {
							eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, topLevelFeatureCounter, this));
							eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, topLevelFeatureCounter, this));
							topLevelFeatureCounter = 0;
						}
					}
				}
			}

			if (feature != null) {
				// write feature
				featureWriter.write(feature, work.getSequenceId());

				// register gml:id in cache
				if (internalConfig.isRegisterGmlIdInCache() && feature.isSetId()) {
					exporter.putObjectId(feature.getId(), work.getId(), work.getObjectType().getObjectClassId());

					if (replaceIds) {
						// also cache the original gml:id
						String originalId = (String) feature.getLocalProperty(CoreConstants.OBJECT_ORIGINAL_GMLID);
						if (originalId != null) {
							exporter.putObjectId(originalId, work.getId(), work.getObjectType().getObjectClassId());
						}
					}
				}
				
				// update export counter
				exporter.updateExportCounter(feature);
			} else
				featureWriter.updateSequenceId(work.getSequenceId());

		} catch (Throwable e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during export of " +
					exporter.getObjectSignature(work.getObjectType(), work.getId()) + ".", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			runLock.unlock();
		}
	}

	private boolean isOnTile(Object envelope) throws FilterException, SQLException {
		// check whether feature is on active tile
		if (envelope != null) {
			GeometryObject geometryObject = exporter.getDatabaseAdapter().getGeometryConverter().getEnvelope(envelope);
			double[] coordinates = geometryObject.getCoordinates(0);

			return activeTile.isOnTile(new Point(
					(coordinates[0] + coordinates[3]) / 2.0,
					(coordinates[1] + coordinates[4]) / 2.0,
					targetSrs),
					exporter.getDatabaseAdapter());
		} else
			return false;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldWork = false;
	}
}
