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
package de.tub.citydb.modules.citygml.importer.controller;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.xml.io.reader.CityGMLChunk;
import org.citygml4j.builder.jaxb.xml.io.reader.JAXBChunkReader;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.FeatureReadMode;

import de.tub.citydb.api.concurrent.SingleWorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.io.DirectoryScanner;
import de.tub.citydb.io.DirectoryScanner.CityGMLFilenameFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.importer.concurrent.FeatureReaderWorkerFactory;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.EventType;
import de.tub.citydb.modules.common.event.InterruptEvent;
import de.tub.citydb.modules.common.event.StatusDialogMessage;
import de.tub.citydb.modules.common.event.StatusDialogProgressBar;
import de.tub.citydb.modules.common.event.StatusDialogTitle;

public class XMLValidator implements EventHandler {
	private final Logger LOG = Logger.getInstance();

	private final JAXBBuilder jaxbBuilder;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private WorkerPool<CityGMLChunk> featureWorkerPool;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private DirectoryScanner directoryScanner;
	private long xmlValidationErrorCounter;

	private int runState;
	private final int PREPARING = 1;
	private final int VALIDATING = 2;

	public XMLValidator(JAXBBuilder jaxbBuilder, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.jaxbBuilder = jaxbBuilder;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() {
		runState = PREPARING;

		// adding listeners
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// worker pool settings 
		de.tub.citydb.config.project.system.System system = config.getProject().getImporter().getSystem();
		int maxThreads = system.getThreadPool().getDefaultPool().getMaxThreads();
		int queueSize = maxThreads * 2;

		Internal intConfig = config.getInternal();

		// build list of files to be validated
		LOG.info("Creating list of CityGML files to be validated...");
		directoryScanner = new DirectoryScanner(true);
		directoryScanner.addFilenameFilter(new CityGMLFilenameFilter());
		List<File> importFiles = directoryScanner.getFiles(intConfig.getImportFiles());

		if (!shouldRun)
			return true;

		if (importFiles.size() == 0) {
			LOG.warn("Failed to find CityGML files at the specified locations.");
			return false;
		}

		int fileCounter = 0;
		int remainingFiles = importFiles.size();
		LOG.info("List of import files successfully created.");
		LOG.info(remainingFiles + " file(s) will be validated.");

		// prepare XML validation
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		ValidationHandler validationHandler = new ValidationHandler();
		validationHandler.allErrors = !xmlValidation.isSetReportOneErrorPerFeature();

		// prepare CityGML input factory
		CityGMLInputFactory in = null;
		try {
			in = jaxbBuilder.createCityGMLInputFactory();
			in.setProperty(CityGMLInputFactory.USE_VALIDATION, true);
			in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
			in.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
			in.setValidationEventHandler(validationHandler);
		} catch (CityGMLReadException e) {
			LOG.error("Failed to initialize CityGML parser. Aborting.");
			return false;
		}

		runState = VALIDATING;

		while (shouldRun && fileCounter < importFiles.size()) {
			try {
				File file = importFiles.get(fileCounter++);
				intConfig.setImportPath(file.getParent());

				eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("validate.dialog.validate.msg"), this));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles, this));

				// this worker pool parses the xml file and passes xml chunks to the dbworker pool
				featureWorkerPool = new SingleWorkerPool<CityGMLChunk>(
						new FeatureReaderWorkerFactory(null, config, eventDispatcher),
						queueSize,
						false);

				// prestart threads
				featureWorkerPool.prestartCoreWorkers();

				// ok, preparation done. inform user and  start parsing the input file
				JAXBChunkReader reader = null;
				boolean containsCityGML = false;

				try {
					reader = (JAXBChunkReader)in.createCityGMLReader(file);					
					LOG.info("Validating document: " + file.toString());						

					containsCityGML = reader.hasNextChunk();

					// iterate through chunks and validate
					while (shouldRun && reader.hasNextChunk()) {
						CityGMLChunk chunk = reader.nextChunk();
						featureWorkerPool.addWork(chunk);
					}						
				} catch (CityGMLReadException e) {
					LOG.error("Fatal CityGML parser error: " + e.getCause().getMessage());
					continue;
				}

				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("validate.dialog.finish.msg"), this));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));

				// we are done with parsing. so shutdown the workers
				try {
					featureWorkerPool.shutdownAndWait();
					eventDispatcher.flushEvents();				
				} catch (InterruptedException ie) {
					//
				}

				try {
					reader.close();
				} catch (CityGMLReadException e) {
					//
				}

				// show XML validation errors
				if (xmlValidationErrorCounter > 0)
					LOG.warn(xmlValidationErrorCounter + " error(s) encountered while validating the document.");
				else if (xmlValidationErrorCounter == 0 && shouldRun) {
					if (!containsCityGML)
						LOG.info("The document does not contain any CityGML elements.");
					else 
						LOG.info("The CityGML elements contained in the document are valid.");
				}

				xmlValidationErrorCounter = 0;
			} finally {
				// clean up
				if (featureWorkerPool != null && !featureWorkerPool.isTerminated())
					featureWorkerPool.shutdownNow();

				// set to null
				featureWorkerPool = null;
			}			
		} 	

		return shouldRun;
	}

	// react on events we are receiving via the eventDispatcher
	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				switch (((InterruptEvent)e).getInterruptType()) {
				case ADE_SCHEMA_READ_ERROR:
				case USER_ABORT:
					shouldRun = false;
					break;
				}

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);

				if (runState == PREPARING && directoryScanner != null)
					directoryScanner.stopScanning();
			}
		}
	}

	private final class ValidationHandler implements ValidationEventHandler {
		boolean allErrors = false;

		@Override
		public boolean handleEvent(ValidationEvent event) {
			if (!event.getMessage().startsWith("cvc"))
				return true;

			StringBuilder msg = new StringBuilder();
			LogLevel type;

			switch (event.getSeverity()) {
			case ValidationEvent.FATAL_ERROR:
			case ValidationEvent.ERROR:
				msg.append("Invalid content");
				type = LogLevel.ERROR;
				break;
			case ValidationEvent.WARNING:
				msg.append("Warning");
				type = LogLevel.WARN;
				break;
			default:
				return allErrors;
			}

			if (event.getLocator() != null) {
				msg.append(" at [")
				.append(event.getLocator().getLineNumber())
				.append(", ")
				.append(event.getLocator().getColumnNumber())
				.append("]");
			}

			msg.append(": ");
			msg.append(event.getMessage());
			LOG.log(type, msg.toString());

			xmlValidationErrorCounter++;
			return allErrors;
		}

	}
}
