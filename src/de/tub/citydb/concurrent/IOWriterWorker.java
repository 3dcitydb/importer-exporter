package de.tub.citydb.concurrent;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.xml.sax.SAXException;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.SAXBuffer.SAXEvent;

public class IOWriterWorker implements Worker<Vector<SAXEvent>> {
	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<Vector<SAXEvent>> workQueue = null;
	private Vector<SAXEvent> firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final SAXWriter saxWriter;

	public IOWriterWorker(SAXWriter saxWriter) {
		this.saxWriter = saxWriter;
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
	public void setFirstWork(Vector<SAXEvent> firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<Vector<SAXEvent>> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
    	if (firstWork != null) {
    		doWork(firstWork);
    		firstWork = null;
    	}

    	while (shouldRun) {
			try {
				Vector<SAXEvent> work = workQueue.take();
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
    	}
	}

	private void doWork(Vector<SAXEvent> work) {
		final ReentrantLock runLock = this.runLock;
        runLock.lock();

        try {
        	for (SAXEvent event : work)
        		event.send(saxWriter);

        	saxWriter.flush();
        } catch (SAXException saxE) {
        	System.out.println(saxE);
        	return;
        } catch (IOException ioE) {
        	System.out.println(ioE);
        	return;
        } finally {
        	runLock.unlock();
        }
	}
}
