package de.tub.citydb.event;

import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.concurrent.Worker;
import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.log.Logger;

public class EventWorker implements Worker<Event> {
	private final Logger LOG = Logger.getInstance();
	
	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<Event> workQueue = null;
	private Event firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final EventDispatcher eventDispatcher;

	public EventWorker(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
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
	public void setFirstWork(Event firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<Event> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
		if (firstWork != null && shouldRun) {
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
