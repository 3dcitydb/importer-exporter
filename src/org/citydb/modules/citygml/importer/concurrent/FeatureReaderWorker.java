/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.concurrent;

import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.log.LogLevel;
import org.citydb.config.Config;
import org.citydb.log.Logger;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.citygml4j.xml.io.reader.XMLChunk;

public class FeatureReaderWorker extends Worker<XMLChunk> {
	private final Logger LOG = Logger.getInstance();
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;

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
	public void run() {
		if (firstWork != null) {
			doWork(firstWork);
			firstWork = null;
		}

		while (shouldRun) {
			try {
				XMLChunk work = workQueue.take();				
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}
	}

	private void doWork(XMLChunk work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			try {
				CityGML cityGML = work.unmarshal();
				if (!useValidation || work.hasPassedXMLValidation())
					dbWorkerPool.addWork(cityGML);
			} catch (UnmarshalException e) {
				if (!useValidation || work.hasPassedXMLValidation()) {
					StringBuilder msg = new StringBuilder();				
					msg.append("Failed to unmarshal XML chunk: ").append(e.getMessage());			
					LOG.error(msg.toString());
				}
			} catch (MissingADESchemaException e) {
				eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.ADE_SCHEMA_READ_ERROR, "Failed to read an ADE XML Schema.", LogLevel.ERROR, e, eventChannel, this));
			} catch (Exception e) {
				// this is to catch general exceptions that may occur during the import
				eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.UNKNOWN_ERROR, "Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
			}
		} finally {
			runLock.unlock();
		}
	}

}
