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
package org.citydb.writer;

import org.citydb.concurrent.Worker;
import org.citydb.config.project.global.LogLevel;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.InterruptEvent;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONChunkWriter;
import org.citygml4j.builder.cityjson.json.io.writer.CityJSONWriteException;
import org.citygml4j.cityjson.feature.AbstractCityObjectType;

import java.util.concurrent.locks.ReentrantLock;

public class CityJSONWriterWorker extends Worker<AbstractCityObjectType> {
	private final ReentrantLock runLock = new ReentrantLock();	
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final CityJSONChunkWriter writer;
	private final EventDispatcher eventDispatcher;

	public CityJSONWriterWorker(CityJSONChunkWriter writer, EventDispatcher eventDispatcher) {
		this.writer = writer;
		this.eventDispatcher = eventDispatcher;
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
				AbstractCityObjectType work = workQueue.take();
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}
	}

	private void doWork(AbstractCityObjectType work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;
			
			writer.writeCityObject(work);
		} catch (CityJSONWriteException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to write CityJSON content.", LogLevel.ERROR, e, eventChannel, this));
			shouldWork = false;
		} finally {
			runLock.unlock();
		}
	}
	
}
