/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.tub.citydb.concurrent.FeatureReaderWorkerFactory;
import de.tub.citydb.concurrent.SingleWorkerPool;
import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.importer.LocalXMLSchemaType;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogProgressBar;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.io.InputFileHandler;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.SAXErrorHandler;
import de.tub.citydb.sax.SAXNamespaceMapper;
import de.tub.citydb.sax.SAXSplitter;

public class XMLValidator implements EventListener {
	private final Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbContext;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private WorkerPool<SAXBuffer> featureWorkerPool;
	private SAXParserFactory factory;

	private FileInputStream fileIn;
	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private long xmlValidationErrorCounter;

	private int runState;
	private final int XML_VALIDATING = 1;

	public XMLValidator(JAXBContext jaxbContext, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
	}

	public boolean doProcess() {
		Internal intConfig = config.getInternal();

		// adding listeners
		eventDispatcher.addListener(EventType.Interrupt, this);
		eventDispatcher.addListener(EventType.Counter, this);
		
		// build list of files to be validated
		LOG.info("Creating list of CityGML files to be validated...");
		InputFileHandler fileHandler = new InputFileHandler(eventDispatcher);
		List<File> importFiles = fileHandler.getFiles(intConfig.getImportFileName().trim().split("\n"));

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
		config.getInternal().setUseXMLValidation(true);		
		if (shouldRun && xmlValidation.getUseLocalSchemas().isSet()) {
			LOG.info("Using local schema documents for XML validation.");

			for (LocalXMLSchemaType schema : xmlValidation.getUseLocalSchemas().getSchemas())
				if (schema != null)
					LOG.info("Using schema: " + schema.value());
		} else
			LOG.info("Using schema documents from xsi:schemaLocation attribute on root element.");

		while (shouldRun && fileCounter < importFiles.size()) {
			try {
				runState = XML_VALIDATING;

				File file = importFiles.get(fileCounter++);
				intConfig.setImportPath(file.getParent());

				eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName()));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("validate.dialog.validate.msg")));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles));
				
				// this worker pool parses the xml file and passes xml chunks to the dbworker pool
				featureWorkerPool = new SingleWorkerPool<SAXBuffer>(
						new FeatureReaderWorkerFactory(jaxbContext, null, null, eventDispatcher, config),
						1,
						false);

				// create a new XML parser
				XMLReader reader = null;
				try {
					reader = factory.newSAXParser().getXMLReader();
				} catch (SAXException saxE) {
					LOG.error("I/O error: " + saxE.getMessage());
					shouldRun = false;
					continue;
				} catch (ParserConfigurationException pcE) {
					LOG.error("I/O error: " + pcE.getMessage());
					shouldRun = false;
					continue;
				}

				// prepare a xml splitter
				SAXSplitter splitter = new SAXSplitter(featureWorkerPool, config, eventDispatcher);

				// prepare an xml errorHandler
				SAXErrorHandler errorHandler = new SAXErrorHandler();

				// prepare namespaceFilter used for mapping xml namespaces
				SAXNamespaceMapper nsMapper = new SAXNamespaceMapper(reader);
				nsMapper.setNamespaceMapping("http://www.citygml.org/citygml/0/3/0", "http://www.citygml.org/citygml/1/0/0");
				nsMapper.setNamespaceMapping("http://www.citygml.org/citygml/0/4/0", "http://www.citygml.org/citygml/1/0/0");

				// connect both components
				nsMapper.setContentHandler(splitter);
				nsMapper.setErrorHandler(errorHandler);

				// open stream on input file
				try {
					if (shouldRun)
						fileIn = new FileInputStream(file);
				} catch (FileNotFoundException e1) {
					LOG.error("I/O error: " + e1.getMessage());
					continue;
				}

				// prestart threads
				featureWorkerPool.prestartCoreWorkers();
				
				// ok, preparation done. inform user and  start parsing the input file
				try {
					if (shouldRun) {
						LOG.info("Validating document: " + file.toString());						
						nsMapper.parse(new InputSource(fileIn));
					}
				} catch (IOException ioE) {
					// we catch "Read error" and "Bad file descriptor" because we produce these ones when interrupting the import
					if (!(ioE.getMessage().equals("Read error") || ioE.getMessage().equals("Bad file descriptor"))) {
						LOG.error("I/O error: " + ioE.getMessage());
						xmlValidationErrorCounter++;
					}
				} catch (SAXException saxE) {
					xmlValidationErrorCounter++;
				}

				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("validate.dialog.finish.msg")));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));
				
				// we are done with parsing. so shutdown the workers
				// xlink pool is not shutdown because we need it afterwards
				try {
					featureWorkerPool.shutdownAndWait();
					if (fileIn != null)
						fileIn.close();

					eventDispatcher.join();
				} catch (InterruptedException ie) {
					//
				} catch (IOException e) {
					//
				}

				// show XML validation errors
				if (xmlValidationErrorCounter > 0)
					LOG.warn(xmlValidationErrorCounter + " error(s) encountered while validating the document.");
				else if (xmlValidationErrorCounter == 0 && shouldRun)
					LOG.info("The document does not contain invalid CityGML content.");

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
		if (e.getEventType() == EventType.Interrupt) {
			if (isInterrupted.compareAndSet(false, true)) {
				switch (((InterruptEvent)e).getInterruptType()) {
				case READ_SCHEMA_ERROR:
					xmlValidationErrorCounter++;
				case USER_ABORT:
					shouldRun = false;
					break;
				}

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);

				if (runState == XML_VALIDATING && fileIn != null)
					try {
						fileIn.close();
						fileIn = null;
					} catch (IOException ioE) {
						//
					}
			}
		}

		else if (e.getEventType() == EventType.Counter &&
				((CounterEvent)e).getType() == CounterType.XML_VALIDATION_ERROR) {
			xmlValidationErrorCounter += ((CounterEvent)e).getCounter();
		}
	}
}
