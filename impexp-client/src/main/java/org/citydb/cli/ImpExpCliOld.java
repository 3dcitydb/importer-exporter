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
package org.citydb.cli;

import org.citydb.ImpExpException;
import org.citydb.citygml.deleter.CityGMLDeleteException;
import org.citydb.citygml.deleter.controller.Deleter;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.controller.Exporter;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.controller.Importer;
import org.citydb.citygml.validator.ValidationException;
import org.citydb.citygml.validator.controller.Validator;
import org.citydb.config.Config;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.connection.DatabaseConnectionWarning;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.modules.kml.controller.KmlExportException;
import org.citydb.modules.kml.controller.KmlExporter;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.ClientConstants;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImpExpCliOld {
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionPool dbPool;
	private final SchemaMapping schemaMapping;
	private CityGMLBuilder cityGMLBuilder;
	private Config config;

	public ImpExpCliOld(Config config) {
		this.config = config;

		dbPool = DatabaseConnectionPool.getInstance();
		cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
	}

	public boolean doImport(String importFiles) throws ImpExpException {
		// prepare list of files to be validated
		List<Path> files = getFiles(importFiles);
		if (files.size() == 0)
			throw new ImpExpException("Invalid list of files to be imported.");

		initDBPool();
		if (!dbPool.isConnected())
			throw new ImpExpException("Connection to database could not be established.");

		log.info("Initializing database import...");

		config.getInternal().setImportFiles(files);
		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		Importer importer = new Importer(cityGMLBuilder, schemaMapping, config, eventDispatcher);
		boolean success = false;

		try {
			success = importer.doProcess();
		} catch (CityGMLImportException e) {
			throw new ImpExpException("CityGML import failed due to an internal error.", e);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			dbPool.disconnect();
		}

		if (success)
			log.info("Database import successfully finished.");
		else
			log.warn("Database import aborted.");

		return success;
	}

	public boolean doValidate(String validateFiles) throws ImpExpException {
		// prepare list of files to be validated
		List<Path> files = getFiles(validateFiles);
		if (files.size() == 0)
			throw new ImpExpException("Invalid list of files to be validated.");

		log.info("Initializing data validation...");

		config.getInternal().setImportFiles(files);
		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		Validator validator = new Validator(config, eventDispatcher);
		boolean success = false;

		try {
			success = validator.doProcess();
		} catch (ValidationException e) {
			throw new ImpExpException("Data validation failed due to an internal error.", e);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}
		}

		if (success)
			log.info("Data validation finished.");
		else
			log.warn("Data validation aborted.");

		return success;
	}

	public boolean doExport(String exportFile) throws ImpExpException {
		setExportFile(exportFile);

		initDBPool();
		if (!dbPool.isConnected())
			throw new ImpExpException("Connection to database could not be established.");

		log.info("Initializing database export...");

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		Exporter exporter = new Exporter(cityGMLBuilder, schemaMapping, config, eventDispatcher);
		boolean success = false;

		try {
			success = exporter.doProcess();
		} catch (CityGMLExportException e) {
			throw new ImpExpException("CityGML export failed due to an internal error.", e);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			dbPool.disconnect();
		}

		if (success)
			log.info("Database export successfully finished.");
		else
			log.warn("Database export aborted.");

		return success;
	}

	public boolean doDelete() throws ImpExpException {
		initDBPool();
		if (!dbPool.isConnected())
			throw new ImpExpException("Connection to database could not be established.");

		log.info("Initializing database delete...");

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		Deleter deleter = new Deleter(config, schemaMapping, eventDispatcher);
		boolean success = false;

		try {
			success = deleter.doProcess();
		} catch (CityGMLDeleteException e) {
			throw new ImpExpException("CityGML delete failed due to an internal error.", e);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			dbPool.disconnect();
		}

		if (success)
			log.info("Database delete successfully finished.");
		else
			log.warn("Database delete aborted.");

		return success;
	}
	
	public boolean doKmlExport(String kmlExportFile) throws ImpExpException {
		setExportFile(kmlExportFile);

		initDBPool();
		if (!dbPool.isConnected())
			throw new ImpExpException("Connection to database could not be established.");

		log.info("Initializing database export...");

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		KmlExporter kmlExporter = new KmlExporter(schemaMapping, config, eventDispatcher);
		boolean success = false;
		
		try {
			success = kmlExporter.doProcess();
		} catch (KmlExportException e) {
			throw new ImpExpException("KML/COLLADA/glTF export failed due to an internal error.", e);
		} finally {
			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			dbPool.disconnect();
		}

		if (success)
			log.info("Database export successfully finished.");
		else
			log.warn("Database export aborted.");

		return success;
	}

	private void setExportFile(String exportFile) throws ImpExpException {
		try {
			config.getInternal().setExportFile(ClientConstants.WORKING_DIR.resolve(exportFile));
		} catch (InvalidPathException e) {
			throw new ImpExpException("'" + exportFile + "' is not a valid file.", e);
		}
	}

	public boolean doTestConnection() throws ImpExpException {
		initDBPool();
		if (!dbPool.isConnected())
			throw new ImpExpException("Connection to database could not be established");

		dbPool.disconnect();
		return true;
	}

	private void initDBPool() throws ImpExpException {
		// check active connection
		DBConnection conn = config.getProject().getDatabase().getActiveConnection();
		if (conn == null)
			throw new ImpExpException("No valid database connection found in project settings.");

		log.info("Connecting to database profile '" + conn.getDescription() + "'.");

		try {
			dbPool.connect(conn);
			log.info("Database connection established.");
			dbPool.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();

			// log unsupported user-defined SRSs
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) {
				if (!refSys.isSupported())
					log.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") is not supported.");
			}

			// print connection warnings
			List<DatabaseConnectionWarning> warnings = dbPool.getActiveDatabaseAdapter().getConnectionWarnings();
			if (!warnings.isEmpty()) {
				for (DatabaseConnectionWarning warning : warnings)
					log.warn(warning.getMessage());
			}
		} catch (DatabaseConfigurationException | SQLException e) {
			throw new ImpExpException("Connection to database could not be established.", e);
		} catch (DatabaseVersionException e) {
			log.error(e.getMessage());
			log.error("Supported versions are '" + Util.collection2string(e.getSupportedVersions(), ", ") + "'.");
			throw new ImpExpException("Connection to database could not be established.");
		}
	}

	private List<Path> getFiles(String fileNames) {
		List<Path> files = new ArrayList<>();

		for (String part : fileNames.split(";")) {
			if (part == null || part.trim().isEmpty())
				continue;

			File file = new File(part.trim());
			if (file.isDirectory()) {
				files.add(file.toPath());
				continue;
			}

			final String pathName = new File(file.getAbsolutePath()).getParent();
			final String fileName = file.getName().replace("?", ".?").replace("*", ".*?");

			file = new File(pathName);
			if (!file.exists()) {
				log.error("'" + file.toString() + "' does not exist");
				continue;
			}

			File[] wildcardList = file.listFiles((dir, name) -> (name.matches(fileName)));

			if (wildcardList != null && wildcardList.length != 0) {
				for (File item : wildcardList)
					files.add(item.toPath());
			}
		}

		return files;
	}
}
