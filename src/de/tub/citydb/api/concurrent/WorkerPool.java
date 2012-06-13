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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerPool<T> {
	private final ReentrantLock mainLock = new ReentrantLock();

	private final WorkQueue<T> workQueue;
	private final ConcurrentHashMap<Worker<T>, Object> workers;
	private final WorkerFactory<T> workerFactory;
	private static final Object DUMMY = new Object();

	private volatile int runState;
	private static final int RUNNING    = 0;
	private static final int SHUTDOWN   = 1;
	private static final int STOP       = 2;
	private static final int TERMINATED = 3;

	private ClassLoader contextClassLoader;
	private ClassLoader defaultClassLoader;

	private volatile int corePoolSize;
	private volatile int maximumPoolSize;
	private final int queueSize;
	private final boolean daemon;
	private int poolSize;

	// WorkQueue
	public static final class WorkQueue<E> {
		private final ReentrantLock lock;
		private final Condition notEmpty;
		private final Condition notFull;
		private final Condition empty;
		private final Condition flushed;

		private final E[] workItems;
		private int putIndex;
		private int takeIndex;
		private int count;
		private volatile boolean blockAndFlush;

		public WorkQueue(int capacity) {
			this(capacity, false);
		}

		@SuppressWarnings("unchecked")
		public WorkQueue(int capacity, boolean fair) {
			lock = new ReentrantLock(fair);
			notEmpty = lock.newCondition();
			notFull = lock.newCondition();
			empty = lock.newCondition();
			flushed = lock.newCondition();

			if (capacity <= 0)
				throw new IllegalArgumentException();

			workItems = (E[]) new Object[capacity];
		}

		final int inc(int i) {
			return (++i == workItems.length) ? 0 : i;
		}

		private void insert(E work) {
			workItems[putIndex] = work;
			putIndex = inc(putIndex);
			++count;
			notEmpty.signal();
		}

		private E extract() {
			final E[] workItems = this.workItems;
			E work = workItems[takeIndex];
			workItems[takeIndex] = null;
			takeIndex = inc(takeIndex);
			--count;
			notFull.signal();
			if (count == 0)
				empty.signalAll();
			return work;
		}

		private void removeAt(int i) {
			final E[] workItems = this.workItems;
			if (i == takeIndex) {
				workItems[i] = null;
				takeIndex = inc(takeIndex);
			} else {
				for (;;) {
					int nexti = inc(i);
					if (nexti != putIndex) {
						workItems[i] = workItems[nexti];
						i = nexti;
					} else {
						workItems[i] = null;
						putIndex = i;
						break;
					}
				}
			}

			--count;
			notFull.signal();
			if (count == 0)
				empty.signalAll();
		}

		public boolean offer(E work) {
			if (work == null)
				throw new NullPointerException();

			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				if (blockAndFlush)
					flushed.awaitUninterruptibly();

				if (count == workItems.length)
					return false;
				else {
					insert(work);
					return true;
				}
			} finally {
				lock.unlock();
			}
		}

		public boolean offer(E work, long timeout, TimeUnit unit) throws InterruptedException {
			if (work == null)
				throw new NullPointerException();

			long nanos = unit.toNanos(timeout);
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				if (blockAndFlush)
					flushed.awaitUninterruptibly();

				for (;;) {
					if (count != workItems.length) {
						insert(work);
						return true;
					}

					if (nanos <= 0)
						return false;

					try {
						nanos = notFull.awaitNanos(nanos);
					} catch (InterruptedException ie) {
						notFull.signal();
						throw ie;
					}
				}
			} finally {
				lock.unlock();
			}
		}

		public void put(E work) {
			if (work == null)
				throw new NullPointerException();

			final E[] workItems = this.workItems;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				if (blockAndFlush)
					flushed.awaitUninterruptibly();

				while (count == workItems.length)
					notFull.awaitUninterruptibly();

				insert(work);
			} finally {
				lock.unlock();
			}
		}

		public E poll() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				if (count == 0)
					return null;

				E work = extract();
				return work;
			} finally {
				lock.unlock();
			}
		}

		public E poll(long timeout, TimeUnit unit) throws InterruptedException {
			long nanos = unit.toNanos(timeout);
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					if (count != 0) {
						E work = extract();
						return work;
					}

					if (nanos <= 0)
						return null;

					try {
						nanos = notEmpty.awaitNanos(nanos);
					} catch (InterruptedException ie) {
						notEmpty.signal();
						throw ie;
					}
				}
			} finally {
				lock.unlock();
			}
		}

		public E take() throws InterruptedException {
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				try {
					while (count == 0)
						notEmpty.await();
				} catch (InterruptedException ie) {
					notEmpty.signal();
					throw ie;
				}

				E work = extract();
				return work;
			} finally {
				lock.unlock();
			}
		}

		public E peek() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return (count == 0) ? null : workItems[takeIndex];
			} finally {
				lock.unlock();
			}
		}

		public boolean remove(E work) {
			if (work == null)
				return false;
			final E[] workItems = this.workItems;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = takeIndex;
				int k = 0;
				for (;;) {
					if (k++ >= count)
						return false;
					if (work.equals(workItems[i])) {
						removeAt(i);
						return true;
					}

					i = inc(i);
				}
			} finally {
				lock.unlock();
			}
		}

		public int size() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return count;
			} finally {
				lock.unlock();
			}
		}

		public int remainingCapacity() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return workItems.length - count;
			} finally {
				lock.unlock();
			}
		}

		public boolean isEmpty() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return count == 0;
			} finally {
				lock.unlock();
			}
		}

		public void clear() {
			final E[] workItems = this.workItems;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = takeIndex;
				int k = count;
				while (k-- > 0) {
					workItems[i] = null;
					i = inc(i);
				}

				count = 0;
				putIndex = 0;
				takeIndex = 0;
				notFull.signalAll();
			} finally {
				lock.unlock();
			}
		}

		public int drainTo(Collection<? super E> collection) {
			if (collection == null)
				throw new NullPointerException();

			final E[] workItems = this.workItems;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = takeIndex;
				int n = 0;
				int max = count;

				while (n < max) {
					collection.add(workItems[i]);
					workItems[i] = null;
					i = inc(i);
					++n;
				}

				if (n > 0) {
					count = 0;
					putIndex = 0;
					takeIndex = 0;
					notFull.signalAll();
					empty.signalAll();
				}

				return n;
			} finally {
				lock.unlock();
			}
		}
	}

	// WorkerPool
	public WorkerPool(int corePoolSize,
			int maximumPoolSize,
			WorkerFactory<T> workerFactory,
			int queueSize,
			boolean fair,
			boolean daemon) {
		if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
			throw new IllegalArgumentException();

		if (workerFactory == null)
			throw new IllegalArgumentException("WorkerFactory may not be null.");

		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.daemon = daemon;
		this.workerFactory = workerFactory;
		this.defaultClassLoader = Thread.currentThread().getContextClassLoader();

		// setting up work queue
		this.queueSize = queueSize;
		workQueue = new WorkQueue<T>(queueSize, fair);

		// setting up the workers
		workers = new ConcurrentHashMap<Worker<T>, Object>(maximumPoolSize);
	}

	public WorkerPool(int corePoolSize,
			int maximumPoolSize,
			WorkerFactory<T> workerFactory,
			int queueSize,
			boolean fair) {
		this(corePoolSize, maximumPoolSize, workerFactory, queueSize, fair, true);
	}

	public WorkerPool(int corePoolSize,
			int maximumPoolSize,
			WorkerFactory<T> workerFactory,
			int queueSize) {
		this(corePoolSize, maximumPoolSize, workerFactory, queueSize, false);
	}
	
	public ClassLoader getDefaultContextClassLoader() {
		return defaultClassLoader;
	}

	public void setContextClassLoader(ClassLoader contextClassLoader) {
		this.contextClassLoader = contextClassLoader;
	}
	
	private boolean addWorker(T firstWork) {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();

		try {
			if (poolSize < maximumPoolSize && runState == RUNNING) {
				Worker<T> worker = workerFactory.createWorker();

				if (worker != null) {
					Thread workerThread = new Thread(worker);
					workerThread.setDaemon(daemon);
					if (contextClassLoader != null)
						workerThread.setContextClassLoader(contextClassLoader);

					worker.setWorkQueue(workQueue);
					worker.setThread(workerThread);
					if (firstWork != null)
						worker.setFirstWork(firstWork);

					workers.put(worker, DUMMY);
					workerThread.start();
					++poolSize;
					return true;
				}

			}

			return false;
		} finally {
			mainLock.unlock();
		}
	}

	@SuppressWarnings("unused")
	private boolean removeWorker(Worker<T> worker) {
		if (worker != null) {
			if (workers.remove(worker) == DUMMY) {
				--poolSize;
				return true;
			}
		}

		return false;
	}

	private void clearWorkers() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();

		try {
			workers.clear();
			poolSize = 0;
		} finally {
			mainLock.unlock();
		}
	}

	public void addWork(T work) {
		if (work == null)
			throw new NullPointerException();

		if (poolSize >= corePoolSize || !addIfUnderCorePoolSize(work)) {
			if (runState == RUNNING && !workQueue.offer(work)) {
				if (!addIfUnderMaximumPoolSize(work)) {
					if (runState == RUNNING)
						workQueue.put(work);
				}
			}
		}
	}

	public void addWorkAndWait(T work) {
		if (work == null)
			throw new NullPointerException();

		ReentrantLock mainLock = this.mainLock;
		ReentrantLock queueLock = workQueue.lock;
		mainLock.lock();
		try {
			addWork(work);
			queueLock.lock();
			try {
				while(!workQueue.isEmpty())
					workQueue.empty.await();
			} catch (InterruptedException ie) {
				// re-try
			}
		} finally {
			queueLock.unlock();
			mainLock.unlock();
		}
	}

	private boolean addIfUnderCorePoolSize(T work) {
		if (poolSize < corePoolSize && runState == RUNNING)
			return addWorker(work);

		return false;
	}

	private boolean addIfUnderMaximumPoolSize(T work) {
		if (poolSize < maximumPoolSize && runState == RUNNING)
			return addWorker(work);

		return false;
	}

	public boolean prestartCoreWorker() {
		return addIfUnderCorePoolSize(null);
	}

	public int prestartCoreWorkers() {
		int n = 0;
		while (addIfUnderCorePoolSize(null))
			++n;

		return n;
	}

	public void shutdown() {		
		if (runState >= SHUTDOWN)
			return;

		final ReentrantLock mainLock = this.mainLock;
		final ReentrantLock queueLock = workQueue.lock;
		mainLock.lock();
		try {
			if (runState < SHUTDOWN)
				runState = SHUTDOWN;

			// make sure we really can exit
			if (poolSize == 0)
				addWorker(null);

			queueLock.lock();
			try {
				while (!workQueue.isEmpty())
					workQueue.empty.await();

			} catch (InterruptedException ie) {
				// re-try
			}

			interruptWorkersIfIdle();
			runState = TERMINATED;
		} finally {
			queueLock.unlock();
			mainLock.unlock();
		}
	}

	public void shutdownAndWait() throws InterruptedException {
		if (runState >= SHUTDOWN)
			return;

		final ReentrantLock mainLock = this.mainLock;
		final ReentrantLock queueLock = workQueue.lock;
		mainLock.lock();
		try {
			if (runState < SHUTDOWN)
				runState = SHUTDOWN;

			// make sure we really can exit
			if (poolSize == 0)
				addWorker(null);

			queueLock.lock();
			try {
				while (!workQueue.isEmpty())
					workQueue.empty.await();

			} catch (InterruptedException ie) {
				// re-try
			}

			interruptWorkersIfIdle();
		} finally {
			queueLock.unlock();
			mainLock.unlock();
		}

		try {
			joinWorkerThreads();
		} catch (InterruptedException ie) {
			throw ie;
		} finally {
			runState = TERMINATED;
		}
	}

	public List<T> shutdownNow() {
		if (runState >= SHUTDOWN)
			return null;

		List<T> workList = drainWorkQueue();

		final ReentrantLock mainLock = this.mainLock;	
		mainLock.lock();
		try {
			if (runState < SHUTDOWN)
				runState = SHUTDOWN;
			
			interruptWorkers();
			
			runState = TERMINATED;
			clearWorkers();
			return workList;
		} finally {
			mainLock.unlock();
		}
	}

	public void join() throws InterruptedException {
		// joining can just be realized by stopping and
		// restarting threads...
		if (runState >= SHUTDOWN)
			return;

		final ReentrantLock mainLock = this.mainLock;
		final ReentrantLock queueLock = workQueue.lock;
		mainLock.lockInterruptibly();
		try {
			workQueue.blockAndFlush = true;

			// make sure we really can join
			if (poolSize == 0)
				addWorker(null);

			queueLock.lock();
			try {
				while (!workQueue.isEmpty())
					workQueue.empty.await();

			} catch (InterruptedException ie) {
				// re-try
			}

			workQueue.blockAndFlush = false;
			workQueue.flushed.signalAll();

			interruptWorkersIfIdle();
		} finally {
			queueLock.unlock();
			mainLock.unlock();
		}

		try {
			joinWorkerThreads();
		} catch (InterruptedException ie) {
			//
		} finally {
			clearWorkers();
			prestartCoreWorkers();
		}
	}

	public void awaitQueueEmpty() {
		final ReentrantLock mainLock = this.mainLock;
		final ReentrantLock queueLock = workQueue.lock;
		mainLock.lock();
		try {
			// make sure we do not wait forever
			if (poolSize == 0)
				addWorker(null);

			queueLock.lock();
			try {
				while (!workQueue.isEmpty())
					workQueue.empty.await();

			} catch (InterruptedException ie) {
				// re-try
			}

		} finally {
			queueLock.unlock();
			mainLock.unlock();
		}

	}

	private void joinWorkerThreads() throws InterruptedException {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker<T> worker : workers.keySet())
				worker.getThread().join();
		} finally {
			mainLock.unlock();
		}
	}

	private void interruptWorkersIfIdle() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker<T> worker : workers.keySet())
				worker.interruptIfIdle();
		} finally {
			mainLock.unlock();
		}
	}

	private void interruptWorkers() {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			for (Worker<T> worker : workers.keySet())
				worker.interrupt();
		} finally {
			mainLock.unlock();
		}
	}

	public List<T> drainWorkQueue() {
		List<T> workList = new ArrayList<T>();
		workQueue.drainTo(workList);

		return workList;
	}

	public WorkQueue<T> getWorkQueue() {
		return workQueue;
	}

	public WorkerFactory<T> getWorkerFactory() {
		return workerFactory;
	}

	public int getMaximumQueueSize() {
		return queueSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
			throw new IllegalArgumentException();

		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			int extra = this.maximumPoolSize - maximumPoolSize;
			this.maximumPoolSize = maximumPoolSize;

			if (extra > 0 && poolSize > maximumPoolSize) {
				HashSet<Worker<T>> remove = new HashSet<Worker<T>>();
				Iterator<Worker<T>> it = workers.keySet().iterator();
				while (it.hasNext() && extra-- > 0 && poolSize > maximumPoolSize) {
					Worker<T> worker = it.next();
					worker.interruptIfIdle();
					remove.add(worker);
					--poolSize;
				}

				for (Worker<T> worker : remove)
					workers.remove(worker);
			}
		} finally {
			mainLock.unlock();
		}
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0 || corePoolSize > maximumPoolSize)
			throw new IllegalArgumentException();

		ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			int extra = this.corePoolSize - corePoolSize;
			this.corePoolSize = corePoolSize;

			if (extra < 0) {
				int n = workQueue.size();
				while (extra++ < 0 && n-- > 0 && poolSize < corePoolSize)
					addWorker(null);
			} else if (extra > 0 && poolSize > corePoolSize) {
				HashSet<Worker<T>> remove = new HashSet<Worker<T>>();
				Iterator<Worker<T>> it = workers.keySet().iterator();
				while (it.hasNext() && extra-- > 0 && poolSize > corePoolSize && workQueue.remainingCapacity() == 0) {
					Worker<T> worker = it.next();
					worker.interruptIfIdle();
					remove.add(worker);
					--poolSize;
				}

				for (Worker<T> worker : remove)
					workers.remove(worker);
			}
		} finally {
			mainLock.unlock();
		}
	}

	public boolean isTerminated() {
		return runState == TERMINATED;
	}

	public boolean isTerminating() {
		int state = runState;
		return state == SHUTDOWN || state == STOP;
	}

	public int getPoolSize() {
		return poolSize;
	}

	protected void finalize() {
		shutdown();
	}
}
