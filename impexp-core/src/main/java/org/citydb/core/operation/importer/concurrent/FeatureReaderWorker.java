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

import org.citydb.config.Config;
import org.citydb.config.project.global.LogLevel;
import org.citydb.util.concurrent.Worker;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.InterruptEvent;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.citygml4j.xml.io.reader.XMLChunk;

import java.util.concurrent.locks.ReentrantLock;

public class FeatureReaderWorker extends Worker<XMLChunk> {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;

	private final WorkerPool<CityGML> workerPool;
	private final EventDispatcher eventDispatcher;
	private final boolean useValidation;

	public FeatureReaderWorker(WorkerPool<CityGML> workerPool,
			Config config,
			EventDispatcher eventDispatcher) {
		this.workerPool = workerPool;
		this.eventDispatcher = eventDispatcher;

		useValidation = config.getImportConfig().getCityGMLOptions().getXMLValidation().isSetUseXMLValidation();
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
				XMLChunk work = workQueue.take();				
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}
	}

	private void doWork(XMLChunk work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			try {
				CityGML cityGML = work.unmarshal();
				if (!useValidation || work.hasPassedXMLValidation()) {
					workerPool.addWork(cityGML);
				}
			} catch (UnmarshalException e) {
				if (!useValidation || work.hasPassedXMLValidation()) {
					eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to unmarshal XML chunk.", LogLevel.ERROR, e, eventChannel, this));
				}
			} catch (MissingADESchemaException e) {
				eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to read an ADE XML Schema.", LogLevel.ERROR, e, eventChannel, this));
			} catch (Throwable e) {
				eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during parsing of input file.", LogLevel.ERROR, e, eventChannel, this));
			}
		} finally {
			runLock.unlock();
		}
	}

}
