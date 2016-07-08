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
package org.citydb.api.event;

import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.controller.LogController;
import org.citydb.api.registry.ObjectRegistry;

public class EventWorker extends Worker<Event> {
	private final LogController LOG;
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	
	private final EventDispatcher eventDispatcher;

	public EventWorker(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
		LOG = ObjectRegistry.getInstance().getLogController();
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
				Event work = workQueue.take();
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}
	}

	private void doWork(Event work) {
		ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			eventDispatcher.propagate(work);
		} catch (Exception e) {
			LOG.error("Internal message bus error: " + e.getMessage());
		} finally {
			runLock.unlock();
		}
	}
}
