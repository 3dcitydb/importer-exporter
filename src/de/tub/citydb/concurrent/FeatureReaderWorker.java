package de.tub.citydb.concurrent;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;

import org.xml.sax.SAXException;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.sax.SAXBuffer.SAXEvent;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.model.citygml.core.CityGMLBase;

public class FeatureReaderWorker implements Worker<Vector<SAXEvent>> {
	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<Vector<SAXEvent>> workQueue = null;
	private Vector<SAXEvent> firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final JAXBContext jaxbContext;
	private final WorkerPool<CityGMLBase> dbWorkerPool;
	private final CityGMLFactory cityGMLFactory;

	public FeatureReaderWorker(JAXBContext jaxbContext, WorkerPool<CityGMLBase> dbWorkerPool, CityGMLFactory cityGMLFactory) {
		this.jaxbContext = jaxbContext;
		this.dbWorkerPool = dbWorkerPool;
		this.cityGMLFactory = cityGMLFactory;
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

		try{
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

			for (SAXEvent saxEvent : work)
				saxEvent.send(unmarshallerHandler);

			JAXBElement<?> featureElem = (JAXBElement<?>)unmarshallerHandler.getResult();

			if (featureElem == null || featureElem.getValue() == null)
				return;
			
			CityGMLBase cityObject = cityGMLFactory.jaxb2cityGML(featureElem);
			if (cityObject != null)
				dbWorkerPool.addWork(cityObject);

		} catch (JAXBException jaxbE) {
			System.out.println(jaxbE);
		} catch (SAXException saxE) {
			System.out.println(saxE);
		} finally {
			runLock.unlock();
		}
	}
}
