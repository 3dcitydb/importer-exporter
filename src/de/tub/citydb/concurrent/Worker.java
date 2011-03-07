package de.tub.citydb.concurrent;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;

public interface Worker<T> extends Runnable {
	public void setWorkQueue(WorkQueue<T> workQueue);
	public void setFirstWork(T firstWork);
	public void setThread(Thread workerThread);
	public Thread getThread();
	public void interruptIfIdle();
	public void interrupt();
}
