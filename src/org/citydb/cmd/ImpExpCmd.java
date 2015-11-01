/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.cmd;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.exporter.ExportFilterConfig;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.exporter.controller.Exporter;
import org.citydb.modules.citygml.importer.controller.CityGMLImportException;
import org.citydb.modules.citygml.importer.controller.Importer;
import org.citydb.modules.citygml.importer.controller.XMLValidator;
import org.citydb.modules.kml.controller.KmlExporter;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;

public class ImpExpCmd {
	private final Logger LOG = Logger.getInstance();
	private final DatabaseConnectionPool dbPool;
	private JAXBBuilder cityGMLBuilder;
	private JAXBContext jaxbKmlContext;
	private JAXBContext jaxbColladaContext;
	private Config config;

	public ImpExpCmd(JAXBBuilder cityGMLBuilder, Config config) {
		this.cityGMLBuilder = cityGMLBuilder;
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
	}

	public ImpExpCmd(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
	}

	public void doImport(String importFiles) {
		// prepare list of files to be validated
		List<File> files = getFiles(importFiles, ";");
		if (files.size() == 0) {
			LOG.error("Invalid list of files to be imported");
			LOG.error("Aborting...");
			return;
		}
		
		initDBPool();
		if (!dbPool.isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database import...");

		config.getInternal().setImportFiles(files.toArray(new File[0]));
		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		Importer importer = new Importer(cityGMLBuilder, dbPool, config, eventDispatcher);
		
		boolean success = false;
		try {
			success = importer.doProcess();
		} catch (CityGMLImportException e) {
			LOG.error("Aborting due to an internal error: " + e.getMessage());
			success = false;
			
			Throwable cause = e.getCause();
			while (cause != null) {
				LOG.error("Cause: " + cause.getMessage());
				cause = cause.getCause();
			}
		}
		try {
			eventDispatcher.flushEvents();
		} catch (InterruptedException e) {
			//
		}

		if (success) {
			LOG.info("Database import successfully finished.");
		} else {
			LOG.warn("Database import aborted.");
		}
	}

	public void doValidate(String validateFiles) {
		// prepare list of files to be validated
		List<File> files = getFiles(validateFiles, ";");
		if (files.size() == 0) {
			LOG.error("Invalid list of files to be validated");
			LOG.error("Aborting...");
			return;
		}
		
		LOG.info("Initializing XML validation...");

		config.getInternal().setImportFiles(files.toArray(new File[0]));
		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		XMLValidator validator = new XMLValidator(config, eventDispatcher);
		boolean success = validator.doProcess();

		try {
			eventDispatcher.flushEvents();
		} catch (InterruptedException e) {
			//
		}

		if (success) {
			LOG.info("XML validation finished.");
		} else {
			LOG.warn("XML validation aborted.");
		}
	}

	public void doExport() {
		initDBPool();
		if (!dbPool.isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database export...");

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		Exporter exporter = new Exporter(cityGMLBuilder, dbPool, config, eventDispatcher);
		boolean success = exporter.doProcess();

		try {
			eventDispatcher.flushEvents();
		} catch (InterruptedException e) {
			//
		}
		
		if (success) {
			LOG.info("Database export successfully finished.");
		} else {
			LOG.warn("Database export aborted.");
		}
	}

	public void doKmlExport() {
		initDBPool();
		if (!dbPool.isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database export...");

		EventDispatcher eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		KmlExporter kmlExporter = new KmlExporter(jaxbKmlContext, jaxbColladaContext, dbPool, config, eventDispatcher);
		ExportFilterConfig filter = config.getProject().getKmlExporter().getFilter();
		if (filter.isSetComplexFilter() && filter.getComplexFilter().getTiledBoundingBox().isSet()) {
			try {
				kmlExporter.calculateRowsColumns();
			}
			catch (SQLException sqle) {
				String srsDescription = filter.getComplexFilter().getBoundingBox().getSrs() == null ?
										"": filter.getComplexFilter().getBoundingBox().getSrs().getDescription() + ": ";
				String message = sqle.getMessage().indexOf("\n") > -1? // cut ORA- stack traces
								 sqle.getMessage().substring(0, sqle.getMessage().indexOf("\n")): sqle.getMessage();
				LOG.error(srsDescription + message);
				LOG.warn("Database export aborted.");
				return;
			}
		}
		boolean success = kmlExporter.doProcess();

		try {
			eventDispatcher.flushEvents();
		} catch (InterruptedException e) {
			//
		}
		
		if (success) {
			LOG.info("Database export successfully finished.");
		} else {
			LOG.warn("Database export aborted.");
		}
	}

	private void initDBPool() {	
		// check active connection
		DBConnection conn = config.getProject().getDatabase().getActiveConnection();

		if (conn == null) {
			LOG.error("No valid database connection found in project settings.");
			return;
		}

		LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");
		conn.setInternalPassword(conn.getPassword());

		try {
			dbPool.connect(config);
			LOG.info("Database connection established.");
			dbPool.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();

			// log whether user-defined SRSs are supported
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) {
				if (refSys.isSupported())
					LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
				else
					LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
			}

		} catch (DatabaseConfigurationException | SQLException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
		} catch (DatabaseVersionException e) {
			LOG.error("Version '" + e.getUnsupportedVersion() + "' of the 3D City Database is not supported.");
			LOG.error("Supported versions are '" + Util.collection2string(config.getProject().getDatabase().getSupportedVersions(), ", ") + "'.");
			LOG.error("Connection to database could not be established.");
		}
	}
	
	private List<File> getFiles(String fileNames, String delim) {
		List<File> files = new ArrayList<File>();
		
		for (String part : fileNames.split(delim)) {
			if (part == null || part.trim().isEmpty())
				continue;

			File file = new File(part.trim());
			if (file.isDirectory()) {
				files.add(file);
				continue;
			}

			final String pathName = new File(file.getAbsolutePath()).getParent();
			final String fileName = file.getName().replace("?", ".?").replace("*", ".*?");

			file = new File(pathName);
			if (!file.exists()) {
				LOG.error("'" + file.toString() + "' does not exist");
				continue;
			}

			File[] wildcardList = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.matches(fileName));
				}
			});

			if (wildcardList != null && wildcardList.length != 0)
				files.addAll(Arrays.asList(wildcardList));
		}

		return files;
	}
}
