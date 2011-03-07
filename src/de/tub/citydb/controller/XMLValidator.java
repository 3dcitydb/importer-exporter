package de.tub.citydb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
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
import de.tub.citydb.config.project.importer.ImpSchemaType;
import de.tub.citydb.config.project.importer.ImpXMLValidation;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.XMLValidationErrorCounterEvent;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXErrorHandler;
import de.tub.citydb.sax.SAXNamespaceMapper;
import de.tub.citydb.sax.SAXSplitter;
import de.tub.citydb.sax.events.SAXEvent;

public class XMLValidator implements EventListener {
	private final Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbContext;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private WorkerPool<Vector<SAXEvent>> featureWorkerPool;
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

		String[] fileNames = intConfig.getImportFileName().trim().split("\n");
		int fileCounter = 0;

		// prepare XML validation 
		ImpXMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		config.getInternal().setUseXMLValidation(true);
		eventDispatcher.addListener(EventType.ValidationErrorCounter, this);		
		if (xmlValidation.getUseLocalSchemas().isSet()) {
			LOG.info("Using local schema documents for XML validation.");
			for (ImpSchemaType schema : xmlValidation.getUseLocalSchemas().getSchemas())
				if (schema != null)
					LOG.info("Reading schema: " + schema.value());
		} else
			LOG.info("Using schema documents from xsi:schemaLocation attribute on root element.");

		// adding listeners
		eventDispatcher.addListener(EventType.Interrupt, this);

		while (shouldRun && fileCounter < fileNames.length) {
			try {
				runState = XML_VALIDATING;

				String fileName = fileNames[fileCounter].trim();
				if (fileName.equals("")) {
					fileCounter++;
					continue;
				}

				File file = new File(fileName);
				if (!file.exists() || !file.canRead() || file.isDirectory()) {
					LOG.error("Failed to read file '" + fileName + "'.");
					fileCounter++;
					continue;
				}

				// set path variables
				File path = new File(file.getAbsolutePath());
				intConfig.setImportPath(path.getParent());

				// this worker pool parses the xml file and passes xml chunks to the dbworker pool
				featureWorkerPool = new SingleWorkerPool<Vector<SAXEvent>>(
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
					fileCounter++;
					continue;
				}

				// prestart threads
				featureWorkerPool.prestartCoreWorkers();
				
				// ok, preparation done. inform user and  start parsing the input file
				try {
					if (shouldRun) {
						LOG.info("Validating document: " + fileName);						
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
				fileCounter++;

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

		else if (e.getEventType() == EventType.ValidationErrorCounter) {
			xmlValidationErrorCounter += ((XMLValidationErrorCounterEvent)e).getCounter();
		}
	}
}
