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
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.controller.Exporter;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.controller.Importer;
import org.citydb.citygml.importer.controller.XMLValidator;
import org.citydb.config.Config;
import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.connection.DatabaseConnectionWarning;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.modules.kml.controller.KmlExportException;
import org.citydb.modules.kml.controller.KmlExporter;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.module.citygml.CoreModule;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ImpExpCli {
	private final Logger log = Logger.getInstance();
	private final DatabaseConnectionPool dbPool;
	private final SchemaMapping schemaMapping;
	private CityGMLBuilder cityGMLBuilder;
	private JAXBContext jaxbKmlContext;
	private JAXBContext jaxbColladaContext;
	private Config config;

	public ImpExpCli(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
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

		log.info("Initializing XML validation...");

		config.getInternal().setImportFiles(files);
		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		XMLValidator validator = new XMLValidator(config, eventDispatcher);
		boolean success = validator.doProcess();

		try {
			eventDispatcher.flushEvents();
		} catch (InterruptedException e) {
			//
		}

		if (success)
			log.info("XML validation finished.");
		else
			log.warn("XML validation aborted.");

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

	private BoundingBox doCalcBBOX() {
		final ReentrantLock mainLock = new ReentrantLock();
		final ReentrantLock lock = mainLock;
		lock.lock();

		BoundingBox bbox = null;
		DatabaseConnectionPool dbConnectionPool = DatabaseConnectionPool.getInstance();
		try {
			if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport())
				return null;

			try {
				log.info("Calculate bounding box automatically from the database as prompted by user");
				// automatically calculate bounding box --> will overwrite bounding box defined in the config file
				dbConnectionPool = DatabaseConnectionPool.getInstance();
				FeatureType featureType = schemaMapping.getFeatureType(config.getProject().getDatabase().getOperation().getBoundingBoxTypeName());
				FeatureType cityObject = schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI());
				featureType = (featureType != null) ? featureType : cityObject;
				bbox = dbConnectionPool.getActiveDatabaseAdapter().getUtil().calcBoundingBox(null, Util.getObjectClassIds(featureType, cityObject, true, schemaMapping));

				if (bbox != null) {
					if (bbox.getLowerCorner().getX() != Double.MAX_VALUE &&
							bbox.getLowerCorner().getY() != Double.MAX_VALUE &&
							bbox.getUpperCorner().getX() != -Double.MAX_VALUE &&
							bbox.getUpperCorner().getY() != -Double.MAX_VALUE) {
						log.info("BBOX " + "[" + bbox.getLowerCorner().getX() + ", " + bbox.getLowerCorner().getY() + ", " + bbox.getLowerCorner().getZ() + "]"
								+ "[" + bbox.getUpperCorner().getX() + ", " + bbox.getUpperCorner().getY() + ", " + bbox.getUpperCorner().getZ() + "]");
						log.info("Bounding box for " + featureType + " features successfully calculated.");
					} else {
						log.warn("The bounding box could not be calculated.");
						log.warn("Either the database does not contain " + featureType + " features or their ENVELOPE attribute is not set.");
					}
				} else
					log.warn("Calculation of bounding box aborted.");
			} catch (SQLException sqlEx) {
				log.error("An SQL error occurred while calculating the bounding box: " + sqlEx.getMessage().trim());
				bbox = null;
			} finally {
				log.info("BBOX calculation done.");
			}
		} catch (Exception e) {
			log.error("An error occurred while calculating the bounding box: " + e.getMessage().trim());
		} finally {
			lock.unlock();
		}

		return bbox;
	}

	public boolean doKmlExport(String kmlExportFile, boolean calcBBOX) throws ImpExpException {
		setExportFile(kmlExportFile);

		initDBPool();
		if (!dbPool.isConnected())
			throw new ImpExpException("Connection to database could not be established.");

		log.info("Initializing database export...");

		if (calcBBOX) {
			config.getProject().getKmlExporter().getQuery().getBboxFilter().setExtent(doCalcBBOX());
		}

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		KmlExporter kmlExporter = new KmlExporter(jaxbKmlContext, jaxbColladaContext, schemaMapping, config, eventDispatcher);
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
			config.getInternal().setExportFile(new File(exportFile).toPath());
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
		conn.setInternalPassword(conn.getPassword());

		try {
			dbPool.connect(config);
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
