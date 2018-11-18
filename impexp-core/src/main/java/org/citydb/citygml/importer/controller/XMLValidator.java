/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.importer.controller;

import org.apache.tika.exception.TikaException;
import org.citydb.citygml.importer.file.DirectoryScanner;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.internal.ArchiveInputFile;
import org.citydb.config.internal.InputFile;
import org.citydb.config.internal.InputFileType;
import org.citydb.config.internal.Internal;
import org.citydb.config.project.global.LogLevel;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.log.Logger;
import org.citydb.util.Util;
import org.citygml4j.xml.schema.SchemaHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class XMLValidator implements EventHandler {
	private final Logger log = Logger.getInstance();

	private final Config config;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private DirectoryScanner directoryScanner;
	private boolean reportAllErrors;
	private InputStream inputStream;
	
	public XMLValidator(Config config, EventDispatcher eventDispatcher) {
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() {
		// adding listeners
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
		Internal internalConfig = config.getInternal();

		// build list of files to be validated
		List<InputFile> importFiles;
		try {
			log.info("Creating list of CityGML files to be validated...");
			directoryScanner = new DirectoryScanner(true);
			importFiles = directoryScanner.listFiles(internalConfig.getImportFiles(), directoryScanner.getDefaultFileEndings());
			if (importFiles.isEmpty()) {
				log.warn("Failed to find CityGML files at the specified locations.");
				return false;
			}
		} catch (TikaException | IOException e) {
			log.error("Fatal error while searching for CityGML files.");
			return false;
		}

		if (!shouldRun)
			return false;

		int fileCounter = 0;
		int remainingFiles = importFiles.size();
		log.info("List of files to be validated successfully created.");
		log.info(remainingFiles + " file(s) will be validated.");

		// prepare XML validation
		reportAllErrors = !config.getProject().getImporter().getXMLValidation().isSetReportOneErrorPerFeature();
		Validator validator;
		ValidationErrorHandler errorHandler = new ValidationErrorHandler();
		try {
			SchemaHandler schemaHandler = SchemaHandler.newInstance();
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);		
			Schema schema = schemaFactory.newSchema(schemaHandler.getSchemaSources());	
			validator = schema.newValidator();
		} catch (SAXException e) {
			log.error("Failed to create CityGML schema context: " + e.getMessage());
			return false;
		}

		long start = System.currentTimeMillis();
		
		while (shouldRun && fileCounter < importFiles.size()) {
			try (InputFile file = importFiles.get(fileCounter++)) {
				Path contentFile = file.getType() != InputFileType.ARCHIVE ?
						file.getFile() : Paths.get(file.getFile().toString(), ((ArchiveInputFile) file).getContentFile());

				eventDispatcher.triggerEvent(new StatusDialogTitle(contentFile.getFileName().toString(), this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("validate.dialog.validate.msg"), this));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles, this));

				// ok, preparation done. inform user and start validating the input file
				log.info("Validating file: " + contentFile.toString());

				validator.reset();
				validator.setErrorHandler(errorHandler);
				errorHandler.reset();

				inputStream = file.openStream();
				validator.validate(new StreamSource(inputStream));
			} catch (SAXException | IOException e) {
				if (!errorHandler.isAborted && shouldRun)
					log.error("Failed to validate CityGML file: " + e.getMessage());
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException ignored) {
						//
					}
				}
			}

			eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("validate.dialog.finish.msg"), this));

			// show XML validation errors
			if (errorHandler.errors > 0)
				log.warn(errorHandler.errors + " error(s) reported while validating the document.");
			else if (errorHandler.errors == 0 && shouldRun)
				log.info("The CityGML file is valid.");
		}
		
		if (shouldRun)
			log.info("Total validation time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.INTERRUPT) {
			shouldRun = false;
			InterruptEvent interruptEvent = (InterruptEvent)e;

			if (interruptEvent.getCause() != null) {
				Throwable cause = interruptEvent.getCause();
				log.error("An error occurred: " + cause.getMessage());
				while ((cause = cause.getCause()) != null)
					log.error("Cause: " + cause.getMessage());
			}
			
			String log = interruptEvent.getLogMessage();
			if (log != null)
				this.log.log(interruptEvent.getLogLevelType(), log);
			
			if (directoryScanner != null)
				directoryScanner.cancel();

			if (inputStream != null)
				inputStream.close();
		}
	}

	private final class ValidationErrorHandler implements ErrorHandler {
		int errors;
		boolean isAborted;

		public void reset() {
			errors = 0;
			isAborted = false;
		}

		@Override
		public void warning(SAXParseException e) throws SAXException {
			write(e, "Warning", LogLevel.WARN);
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			reportAllErrors = false;
			write(e, "Invalid content", LogLevel.ERROR);
		}

		@Override
		public void error(SAXParseException e) throws SAXException {
			write(e, "Invalid content", LogLevel.ERROR);
		}

		public void write(SAXParseException e, String prefix, LogLevel level) throws SAXException {
			if (!isAborted) {
				log.log(level, prefix + " at " + '[' + e.getLineNumber() + ',' + e.getColumnNumber() + "]: " + e.getMessage());
				errors++;
				if (!reportAllErrors) {
					isAborted = true;
					throw new SAXException();
				}
			}
		}
	}

}
