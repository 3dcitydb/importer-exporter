/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.api.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;

public abstract class DefaultWorkerImpl<T> implements Worker<T> {
	private final ReentrantLock mainLock = new ReentrantLock();

	// instance members needed for interaction with WorkerPool
	private WorkQueue<T> workQueue;
	private Thread workerThread;
	private T firstWork;

	private volatile boolean shouldRun = true;

	@Override
	public void setWorkQueue(WorkQueue<T> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
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
	public void setFirstWork(T firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void run() {
		try {
			if (firstWork != null && shouldRun) {
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
