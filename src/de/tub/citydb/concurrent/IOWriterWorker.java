package de.tub.citydb.concurrent;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.xml.sax.SAXException;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.events.SAXEvent;

public class IOWriterWorker implements Worker<SAXBuffer> {
	private final Logger LOG = Logger.getInstance();
	
	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<SAXBuffer> workQueue = null;
	private SAXBuffer firstWork;
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
	public void setFirstWork(SAXBuffer firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<SAXBuffer> workQueue) {
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
				SAXBuffer work = workQueue.take();
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
    	}
	}

	private void doWork(SAXBuffer work) {
		final ReentrantLock runLock = this.runLock;
        runLock.lock();

        try {
        	SAXEvent event = work.getFirstEvent();
        	while (event != null) {
				event.send(saxWriter);
				work.removeFirstEvent();
				event = event.next();
        	}

        	saxWriter.flush();
        } catch (SAXException saxE) {
        	LOG.error("XML error: " + saxE.getMessage());
        	return;
        } catch (IOException ioE) {
        	LOG.error("I/O error: " + ioE.getMessage());
        	return;
        } finally {
        	runLock.unlock();
        }
	}
}
