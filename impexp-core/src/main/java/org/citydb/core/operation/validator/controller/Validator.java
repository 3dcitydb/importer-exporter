/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.validator.controller;

import org.apache.tika.exception.TikaException;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.core.file.FileType;
import org.citydb.core.file.InputFile;
import org.citydb.core.file.input.AbstractArchiveInputFile;
import org.citydb.core.file.input.DirectoryScanner;
import org.citydb.core.operation.validator.ValidationException;
import org.citydb.core.operation.validator.reader.ValidatorFactory;
import org.citydb.core.operation.validator.reader.ValidatorFactoryBuilder;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;
import org.citydb.util.log.Logger;

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
	private final Object eventChannel = new Object();

	private volatile boolean shouldRun = true;
	private boolean logTotalProcessingTime = true;
	private DirectoryScanner directoryScanner;
	private ValidationException exception;
	private int invalidFiles;

	public Validator() {
		config = ObjectRegistry.getInstance().getConfig();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
	}

	public Validator logTotalProcessingTime(boolean logTotalProcessingTime) {
		this.logTotalProcessingTime = logTotalProcessingTime;
		return this;
	}

	public Object getEventChannel() {
		return eventChannel;
	}

	public boolean doValidate(List<Path> inputFiles) throws ValidationException {
		if (inputFiles == null || inputFiles.isEmpty()) {
			throw new ValidationException("No input file(s) provided.");
		}

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		long start = System.currentTimeMillis();
		boolean success;
		try {
			success = process(inputFiles);
		} catch (ValidationException e) {
			throw e;
		} catch (Throwable e) {
			throw new ValidationException("An unexpected error occurred.", e);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			eventDispatcher.removeEventHandler(this);
		}

		if (logTotalProcessingTime && success) {
			log.info("Total validation time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");
		}

		return success;
	}

	private boolean process(List<Path> inputFiles) throws ValidationException {
		invalidFiles = 0;

		// build list of files to be validated
		List<InputFile> files;
		try {
			log.info("Creating list of files to be validated...");
			directoryScanner = new DirectoryScanner(true);
			files = directoryScanner.listFiles(inputFiles);
			if (files.isEmpty()) {
				log.warn("Failed to find files at the specified locations.");
				return false;
			}
		} catch (TikaException | IOException e) {
			throw new ValidationException("Fatal error while searching for files.", e);
		}

		if (!shouldRun)
			return false;

		int fileCounter = 0;
		int remainingFiles = files.size();
		log.info("List of files to be validated successfully created.");
		log.info(remainingFiles + " file(s) will be validated.");

		// create reader factory builder
		ValidatorFactoryBuilder builder = new ValidatorFactoryBuilder();

		while (shouldRun && fileCounter < files.size()) {
			try (InputFile file = files.get(fileCounter++)) {
				Path contentFile = file.getType() != FileType.ARCHIVE ?
						file.getFile() :
						Paths.get(file.getFile().toString(), ((AbstractArchiveInputFile) file).getContentFile());

				eventDispatcher.triggerEvent(new StatusDialogTitle(contentFile.getFileName().toString()));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("validate.dialog.validate.msg")));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles));

				ValidatorFactory validatorFactory;
				try {
					validatorFactory = builder.buildFactory(file, config);
				} catch (ValidationException e) {
					throw new ValidationException("Failed to validate input file '" + contentFile + "'.", e);
				}

				log.info("Validating file: " + contentFile);

				try (org.citydb.core.operation.validator.reader.Validator validator = validatorFactory.createValidator()) {
					validator.validate(file);

					if (shouldRun) {
						if (validator.getValidationErrors() == 0) {
							log.info("The file is valid.");
						} else {
							log.warn("The file is invalid. Found " + validator.getValidationErrors() + " error(s).");
							invalidFiles++;
						}
					}
				}

				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("validate.dialog.finish.msg")));
			} catch (IOException e) {
				throw new ValidationException("Failed to validate input file.", e);
			}
		}

		if (shouldRun && files.size() > 1) {
			if (invalidFiles == 0) {
				log.info("All files were successfully validated.");
			} else {
				log.warn("Validation failed for " + invalidFiles + " out of " + files.size() + " input file(s).");
			}
		}
		
		if (exception != null) {
			throw exception;
		}

		return shouldRun;
	}

	public int getNumberOfInvalidFiles() {
		return invalidFiles;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (isInterrupted.compareAndSet(false, true)) {
			shouldRun = false;
			InterruptEvent event = (InterruptEvent) e;

			if (event.getChannel() == eventChannel) {
				log.log(event.getLogLevelType(), event.getLogMessage());
				if (event.getCause() != null) {
					exception = new ValidationException("Aborting validation due to errors.", event.getCause());
				}
			}

			if (directoryScanner != null) {
				directoryScanner.cancel();
			}
		}
	}
}
