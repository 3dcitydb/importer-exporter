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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.controller.LogController;
import org.citydb.api.event.Event;
import org.citydb.api.registry.ObjectRegistry;

public class WorkerPool<T> {
	private final ReentrantLock mainLock = new ReentrantLock();

	private final LogController log;
	private final WorkQueue<T> workQueue;
	private final ConcurrentHashMap<Worker<T>, Object> workers;
	private final WorkerFactory<T> workerFactory;
	private final String poolName;
	private final Object DUMMY = new Object();

	private volatile int runState;
	private final byte RUNNING    = 0;
	private final byte SHUTDOWN   = 1;
	private final byte STOP       = 2;
	private final byte TERMINATED = 3;

	private ClassLoader contextClassLoader;
	private ClassLoader defaultClassLoader;

	private PoolSizeAdaptationStrategy adaptationStrategy = PoolSizeAdaptationStrategy.AGGRESSIVE;
	private volatile int corePoolSize;
	private volatile int maximumPoolSize;
	private final int queueSize;
	private final boolean daemon;
	private int poolSize;
	private byte poolSizeAdaptationFailure;
	private byte threadNo;
	private Object eventSource;

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
	public WorkerPool(String poolName,
			int corePoolSize,
			int maximumPoolSize,
			PoolSizeAdaptationStrategy adaptationStrategy,
			WorkerFactory<T> workerFactory,
			int queueSize,
			boolean fair,
			boolean daemon) {
		if (corePoolSize <= 0)
			throw new IllegalArgumentException("Core pool size must be greater than zero.");

		if  (maximumPoolSize < corePoolSize)
			throw new IllegalArgumentException("Maximum pool size must be greater or equal to core pool size.");

		if (workerFactory == null)
			throw new IllegalArgumentException("WorkerFactory may not be null.");

		this.poolName = poolName;
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.adaptationStrategy = adaptationStrategy;
		this.daemon = daemon;
		this.workerFactory = workerFactory;
		this.defaultClassLoader = Thread.currentThread().getContextClassLoader();

		// setting up work queue and workers map
		this.queueSize = queueSize;
		workQueue = new WorkQueue<T>(queueSize, fair);
		workers = new ConcurrentHashMap<Worker<T>, Object>(maximumPoolSize);

		log = ObjectRegistry.getInstance().getLogController();
	}

	public WorkerPool(String poolName,
			int corePoolSize,
			int maximumPoolSize,
			PoolSizeAdaptationStrategy adaptationStrategy,
			WorkerFactory<T> workerFactory,
			int queueSize,
			boolean fair) {
		this(poolName, corePoolSize, maximumPoolSize, adaptationStrategy, workerFactory, queueSize, fair, true);
	}

	public WorkerPool(String poolName,
			int corePoolSize,
			int maximumPoolSize,
			PoolSizeAdaptationStrategy adaptationStrategy,
			WorkerFactory<T> workerFactory,
			int queueSize) {
		this(poolName, corePoolSize, maximumPoolSize, adaptationStrategy, workerFactory, queueSize, false);
	}

	public ClassLoader getDefaultContextClassLoader() {
		return defaultClassLoader;
	}

	public void setContextClassLoader(ClassLoader contextClassLoader) {
		this.contextClassLoader = contextClassLoader;
	}

	public void setEventSource(Object eventSource) {
		this.eventSource = eventSource;
	}

	private boolean addWorker(T firstWork) {
		final ReentrantLock mainLock = this.mainLock;
		mainLock.lock();

		try {
			if (poolSize < maximumPoolSize && runState == RUNNING) {
				Worker<T> worker = workerFactory.createWorker();

				if (worker != null) {
					Thread workerThread = new Thread(worker);
					workerThread.setName(poolName + " " + threadNo++);
					workerThread.setDaemon(daemon);
					if (contextClassLoader != null)
						workerThread.setContextClassLoader(contextClassLoader);

					// set context
					worker.workQueue = workQueue;
					worker.workerThread = workerThread;
					worker.eventChannel = eventSource != null ? eventSource : Event.GLOBAL_CHANNEL;
					if (firstWork != null)
						worker.firstWork = firstWork;

					workers.put(worker, DUMMY);
					workerThread.start();
					++poolSize;
					return true;
				}

				else {
					// adapt worker pool size according to chosen strategy
					if (++poolSizeAdaptationFailure == 3) {
						adaptationStrategy = PoolSizeAdaptationStrategy.AGGRESSIVE;
						log.info("[" + poolName + "] Failed three times to create worker. Falling back to " + PoolSizeAdaptationStrategy.AGGRESSIVE + " adaptation strategy.");
					}

					if (adaptationStrategy == PoolSizeAdaptationStrategy.NONE) {
						log.warn("[" + poolName + "] Not adapting pool size although creation of worker failed.");
					} else {
						switch (adaptationStrategy) {
						case AGGRESSIVE:
							if (poolSize < corePoolSize) {
								// remove all workers but one
								Iterator<Entry<Worker<T>, Object>> it = workers.entrySet().iterator();
								while (it.hasNext() && poolSize > 1) {
									it.next().getKey().interruptIfIdle();
									it.remove();
									--poolSize;
								}
								corePoolSize = poolSize;
							} else if (poolSize < maximumPoolSize)
								maximumPoolSize = poolSize;
							break;
						case STEPWISE:
							if (poolSize < corePoolSize && poolSize > 1) {
								// remove one worker
								Iterator<Entry<Worker<T>, Object>> it = workers.entrySet().iterator();
								it.next().getKey().interruptIfIdle();
								it.remove();
								--poolSize;
								--corePoolSize;
							} else if (poolSize < maximumPoolSize)
								--maximumPoolSize;
							break;
						default: 
							// nothing to do
						}

						if (corePoolSize > 0)
							log.info("[" + poolName + "] Adapting pool size to " + corePoolSize + " core worker(s) and " + maximumPoolSize + " maximum worker(s).");
					}
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
			}

			try {
				joinWorkerThreads();
			} catch (InterruptedException ie) {
				//
			}
			
			int poolSize = this.poolSize;
			clearWorkers();
			for (int i = 0; i < poolSize; i++)
				addWorker(null);
			
		} finally {
			mainLock.unlock();
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
				worker.workerThread.join();
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
				Iterator<Entry<Worker<T>, Object>> it = workers.entrySet().iterator();
				while (it.hasNext() && extra-- > 0 && poolSize > maximumPoolSize) {
					it.next().getKey().interruptIfIdle();
					it.remove();
					--poolSize;
				}
			}
		} finally {
			mainLock.unlock();
		}
	}

	public void setCorePoolSize(int corePoolSize) {
		if (corePoolSize < 0 || corePoolSize > maximumPoolSize)
			throw new IllegalArgumentException();

		ReentrantLock mainLock = this.mainLock;
		mainLock.lock();
		try {
			int extra = poolSize - corePoolSize;
			this.corePoolSize = corePoolSize;

			if (extra < 0) {
				int n = workQueue.size();
				while (extra++ < 0 && n-- > 0 && poolSize < corePoolSize)
					addWorker(null);
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

	public PoolSizeAdaptationStrategy getPoolSizeAdaptationStrategy() {
		return adaptationStrategy;
	}

	public void setPoolSizeAdaptationStrategy(PoolSizeAdaptationStrategy adaptationStrategy) {
		this.adaptationStrategy = adaptationStrategy;
	}

	public String getName() {
		return poolName;
	}

	protected void finalize() {
		shutdown();
	}
}
