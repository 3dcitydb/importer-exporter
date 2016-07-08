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
package org.citydb.api.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public abstract class DefaultWorkerImpl<T> extends Worker<T> {
	private final ReentrantLock mainLock = new ReentrantLock();
	private volatile boolean shouldRun = true;

	@Override
	public void interrupt() {
		shouldRun = false;
		workerThread.interrupt();
	}

	@Override
	public void interruptIfIdle() {
		final ReentrantLock runLock = this.mainLock;
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
		try {
			if (firstWork != null) {
				lockAndDoWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					T work = workQueue.take();
					lockAndDoWork(work);					
				} catch (InterruptedException ie) {
					// re-check state
				}
			}
		} finally {
			shutdown();
		}
	}
	
	private void lockAndDoWork(T work) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		
		try {
			doWork(work);
		} finally {
			lock.unlock();
		}
	}

	public abstract void doWork(T work);
	public abstract void shutdown();
}
