/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.validator.controller;

import org.apache.tika.exception.TikaException;
import org.citydb.citygml.validator.ValidationException;
import org.citydb.citygml.validator.reader.ValidatorFactory;
import org.citydb.citygml.validator.reader.ValidatorFactoryBuilder;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.file.FileType;
import org.citydb.file.InputFile;
import org.citydb.file.input.AbstractArchiveInputFile;
import org.citydb.file.input.DirectoryScanner;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Validator implements EventHandler {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private volatile boolean shouldRun = true;
	private DirectoryScanner directoryScanner;

	public Validator() {
		config = ObjectRegistry.getInstance().getConfig();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
	}

	public boolean doValidate(List<Path> inputFiles) throws ValidationException {
		if (inputFiles == null || inputFiles.isEmpty()) {
			throw new ValidationException("No input file(s) provided.");
		}

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		try {
			return process(inputFiles);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			eventDispatcher.removeEventHandler(this);
		}
	}

	private boolean process(List<Path> inputFiles) throws ValidationException {
		// build list of files to be validated
		List<InputFile> files;
		try {
			log.info("Creating list of CityGML files to be validated...");
			directoryScanner = new DirectoryScanner(true);
			files = directoryScanner.listFiles(inputFiles);
			if (files.isEmpty()) {
				log.warn("Failed to find CityGML files at the specified locations.");
				return false;
			}
		} catch (TikaException | IOException e) {
			throw new ValidationException("Fatal error while searching for CityGML files.", e);
		}

		if (!shouldRun)
			return false;

		int fileCounter = 0;
		int remainingFiles = files.size();
		log.info("List of files to be validated successfully created.");
		log.info(remainingFiles + " file(s) will be validated.");

		// create reader factory builder
		ValidatorFactoryBuilder builder = new ValidatorFactoryBuilder();

		long start = System.currentTimeMillis();
		
		while (shouldRun && fileCounter < files.size()) {
			try (InputFile file = files.get(fileCounter++)) {
				Path contentFile = file.getType() != FileType.ARCHIVE ?
						file.getFile() : Paths.get(file.getFile().toString(), ((AbstractArchiveInputFile) file).getContentFile());

				eventDispatcher.triggerEvent(new StatusDialogTitle(contentFile.getFileName().toString(), this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("validate.dialog.validate.msg"), this));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles, this));

				ValidatorFactory validatorFactory;
				try {
					validatorFactory = builder.buildFactory(file, config);
				} catch (ValidationException e) {
					throw new ValidationException("Failed to validate input file '" + contentFile + "'.", e);
				}

				// ok, preparation done. inform user and start validating the input file
				log.info("Validating file: " + contentFile.toString());
				try (org.citydb.citygml.validator.reader.Validator validator = validatorFactory.createValidator()) {
					validator.validate(file);

					// show XML validation errors
					if (validator.getValidationErrors() > 0)
						log.warn(validator.getValidationErrors() + " error(s) encountered while validating the document.");
					else if (shouldRun)
						log.info("The input file is valid.");
				}

				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("validate.dialog.finish.msg"), this));
			} catch (ValidationException e) {
				throw e;
			} catch (IOException e) {
				throw new ValidationException("Failed to validate input file.", e);
			} catch (Throwable e) {
				throw new ValidationException("An unexpected error occurred.", e);
			}
		}
		
		if (shouldRun)
			log.info("Total validation time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (isInterrupted.compareAndSet(false, true)) {
			shouldRun = false;
			InterruptEvent interruptEvent = (InterruptEvent)e;
			if (interruptEvent.getCause() != null)
				log.error("An error occurred.", interruptEvent.getCause());
			
			String log = interruptEvent.getLogMessage();
			if (log != null)
				this.log.log(interruptEvent.getLogLevelType(), log);
			
			if (directoryScanner != null)
				directoryScanner.cancel();
		}
	}

}
