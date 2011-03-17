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
package de.tub.citydb.concurrent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.core.CityGMLBase;
import org.xml.sax.SAXException;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.importer.LocalXMLSchemaType;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEnum;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.validation.SchemaLocationEvent;
import de.tub.citydb.jaxb.JAXBValidationEventHandler;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.events.Locatable;
import de.tub.citydb.sax.events.SAXEvent;

public class FeatureReaderWorker implements Worker<SAXBuffer> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<SAXBuffer> workQueue = null;
	private SAXBuffer firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final JAXBContext jaxbContext;
	private final WorkerPool<CityGMLBase> dbWorkerPool;
	private final CityGMLFactory cityGMLFactory;
	private final EventDispatcher eventDispatcher;
	private final Config config;

	// XML validation
	private XMLValidation xmlValidation;
	private FeatureReader featureReader;

	public FeatureReaderWorker(JAXBContext jaxbContext, 
			WorkerPool<CityGMLBase> dbWorkerPool, 
			CityGMLFactory cityGMLFactory,
			EventDispatcher eventDispatcher,
			Config config) {
		this.jaxbContext = jaxbContext;
		this.dbWorkerPool = dbWorkerPool;
		this.cityGMLFactory = cityGMLFactory;
		this.eventDispatcher = eventDispatcher;
		this.config = config;

		init();
	}

	private void init() {
		if (config.getInternal().isUseXMLValidation()) {
			xmlValidation = config.getProject().getImporter().getXMLValidation();	

			featureReader = new ValidatingFeatureReader();
			ValidatingFeatureReader validatingFeatureReader = (ValidatingFeatureReader)featureReader;

			// choose how to obtain schema documents
			if (xmlValidation.getUseLocalSchemas().isSet())
				validatingFeatureReader.handleLocalSchemaLocation();
			else
				eventDispatcher.addListener(EventType.SchemaLocation, validatingFeatureReader);
		} else
			featureReader = new NonValidatingFeatureReader();
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

		try{
			featureReader.read(work);
		} finally {
			runLock.unlock();
		}
	}

	private abstract class FeatureReader {
		public abstract void read(SAXBuffer work);

		protected void forwardResult(JAXBElement<?> featureElem) {
			CityGMLBase cityObject = null;

			if (featureElem.getValue() instanceof org.citygml4j.jaxb.citygml._0_4.AppearancePropertyType) {
				org.citygml4j.jaxb.citygml._0_4.AppearancePropertyType appProp = (org.citygml4j.jaxb.citygml._0_4.AppearancePropertyType)featureElem.getValue();
				if (appProp.isSetAppearance())
					cityObject = new org.citygml4j.impl.jaxb.citygml.appearance._0_4.AppearanceImpl(appProp.getAppearance());
			} else if (featureElem.getValue() instanceof org.citygml4j.jaxb.citygml.app._1.AppearancePropertyType) {
				org.citygml4j.jaxb.citygml.app._1.AppearancePropertyType appProp = (org.citygml4j.jaxb.citygml.app._1.AppearancePropertyType)featureElem.getValue();
				if (appProp.isSetAppearance())
					cityObject = new org.citygml4j.impl.jaxb.citygml.appearance._1.AppearanceImpl(appProp.getAppearance());
			} else			
				cityObject = cityGMLFactory.jaxb2cityGML(featureElem);

			if (cityObject != null)
				dbWorkerPool.addWork(cityObject);
		}
	}

	private final class ValidatingFeatureReader extends FeatureReader implements EventListener {
		private Schema schema;
		private JAXBValidationEventHandler validationEventHandler;
		private boolean forwardResult;

		private ValidatingFeatureReader() {
			validationEventHandler = new JAXBValidationEventHandler(eventDispatcher, !xmlValidation.isSetReportOneErrorPerFeature());
			forwardResult = (dbWorkerPool != null && cityGMLFactory != null);
		}

		@Override
		public void read(SAXBuffer work) {
			if (schema == null)
				return;

			try{
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				unmarshaller.setSchema(schema);

				validationEventHandler.reset();
				unmarshaller.setEventHandler(validationEventHandler);
				UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

				SAXEvent event = work.getFirstEvent();
				do {
					if (event instanceof Locatable) {
						validationEventHandler.setLineNumber(((Locatable)event).getLocation().getLineNumber());
						validationEventHandler.setColumnNumber(((Locatable)event).getLocation().getColumnNumber());
					}

					event.send(unmarshallerHandler);
					work.removeFirstEvent();
				} while ((event = event.next()) != null);

				JAXBElement<?> featureElem = (JAXBElement<?>)unmarshallerHandler.getResult();
				unmarshallerHandler = null;

				if (featureElem == null || featureElem.getValue() == null)
					return;

				if (forwardResult && !validationEventHandler.hasEvents())
					forwardResult(featureElem);

			} catch (JAXBException jaxbE) {
				LOG.error(jaxbE.getMessage());
			} catch (SAXException saxE) {
				//
			}
		}

		private void handleLocalSchemaLocation() {
			Set<LocalXMLSchemaType> schemas = xmlValidation.getUseLocalSchemas().getSchemas();
			List<String> schemaLocations = new ArrayList<String>();

			for (LocalXMLSchemaType schema : schemas) {
				if (schema != null) {
					URL schemaURL = null;

					switch (schema) {
					case CityGML_v1_0_0:
						schemaURL = FeatureReaderWorker.class.getResource("/resources/schemas/CityGML/1.0.0/baseProfile.xsd");
						break;
					case CityGML_v0_4_0:
						schemaURL = FeatureReaderWorker.class.getResource("/resources/schemas/CityGML/0.4.0/CityGML.xsd");
						break;
					}

					if (schemaURL != null)
						schemaLocations.add(schemaURL.toString());
				}
			}

			if (!schemaLocations.isEmpty()) {
				Source[] sources =  new Source[schemaLocations.size()];
				int i = 0;

				for (String schemaLocation : schemaLocations)
					sources[i++] = new StreamSource(schemaLocation);

				initSchema(sources);
			}	
		}

		@Override
		public void handleEvent(Event e) throws Exception {
			if (e.getEventType() == EventType.SchemaLocation) {
				Set<URL> schemaLocationURLs = ((SchemaLocationEvent)e).getSchemaLocationURLs();

				Source[] sources =  new Source[schemaLocationURLs.size()];
				int i = 0;

				for (URL schemaLocationURL : schemaLocationURLs)
					sources[i++] = new StreamSource(schemaLocationURL.toString());

				initSchema(sources);
			}
		}

		private void initSchema(Source[] sources) {
			try {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				schema = schemaFactory.newSchema(sources);
			} catch (SAXException saxE) {
				eventDispatcher.triggerEvent(new InterruptEvent(InterruptEnum.READ_SCHEMA_ERROR, 
						"XML error: " + saxE.getMessage(), 
						LogLevelType.ERROR));
			}	
		}

	}

	private final class NonValidatingFeatureReader extends FeatureReader {

		@Override
		public void read(SAXBuffer work) {		
			try{
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();

				SAXEvent event = work.getFirstEvent();
				do {
					event.send(unmarshallerHandler);
					work.removeFirstEvent();
				} while ((event = event.next()) != null);

				JAXBElement<?> featureElem = (JAXBElement<?>)unmarshallerHandler.getResult();
				unmarshallerHandler = null;

				if (featureElem == null || featureElem.getValue() == null)
					return;

				forwardResult(featureElem);

			} catch (JAXBException jaxbE) {
				LOG.error(jaxbE.getMessage());
			} catch (SAXException saxE) {
				LOG.error("XML error: " + saxE.getMessage());
			}
		}		
	}
}
