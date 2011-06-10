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
