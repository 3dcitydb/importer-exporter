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
package de.tub.citydb.modules.citygml.importer.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBException;

import org.citygml4j.builder.jaxb.xml.io.reader.CityGMLChunk;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.xml.sax.SAXException;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.common.event.InterruptEnum;
import de.tub.citydb.modules.common.event.InterruptEvent;

public class FeatureReaderWorker implements Worker<CityGMLChunk> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<CityGMLChunk> workQueue = null;
	private CityGMLChunk firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final WorkerPool<CityGML> dbWorkerPool;
	private final EventDispatcher eventDispatcher;
	private final boolean useValidation;

	public FeatureReaderWorker(WorkerPool<CityGML> dbWorkerPool,
			Config config,
			EventDispatcher eventDispatcher) {
		this.dbWorkerPool = dbWorkerPool;
		this.eventDispatcher = eventDispatcher;

		useValidation = config.getProject().getImporter().getXMLValidation().isSetUseXMLValidation();
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
	public void setFirstWork(CityGMLChunk firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<CityGMLChunk> workQueue) {
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
				CityGMLChunk work = workQueue.take();				
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}
	}

	private void doWork(CityGMLChunk work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			try {
				CityGML cityGML = work.unmarshal();
				if (dbWorkerPool != null && (!useValidation || work.hasPassedXMLValidation()))
					dbWorkerPool.addWork(cityGML);
			} catch (JAXBException e) {
				StringBuilder msg = new StringBuilder();				
				msg.append("Failed to unmarshal XML chunk");

				if (work.getFirstStartElement() != null && work.getFirstStartElement().getLocation() != null) {
					msg.append(" at [")
					.append(work.getFirstStartElement().getLocation().getLineNumber())
					.append(", ")
					.append(work.getFirstStartElement().getLocation().getColumnNumber())
					.append("]");
				}
				
				msg.append(": ");
				msg.append(e.getMessage());
				LOG.error(msg.toString());
			} catch (SAXException e) {
				//
			} catch (MissingADESchemaException e) {
				LOG.error(e.getMessage());				
				eventDispatcher.triggerEvent(new InterruptEvent(InterruptEnum.ADE_SCHEMA_READ_ERROR, this));
			}
		} finally {
			runLock.unlock();
		}
	}

}
