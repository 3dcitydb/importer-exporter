/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.cmd;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.exporter.controller.Exporter;
import de.tub.citydb.modules.citygml.importer.controller.Importer;
import de.tub.citydb.modules.citygml.importer.controller.XMLValidator;
import de.tub.citydb.modules.kml.controller.KmlExporter;

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
		boolean success = importer.doProcess();

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
		XMLValidator validator = new XMLValidator(cityGMLBuilder, config, eventDispatcher);
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
		if (filter.isSetComplexFilter()) {
			try {
				kmlExporter.calculateRowsColumnsAndDelta();
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
			dbPool.getActiveConnectionMetaData().printToConsole();

			// log whether user-defined SRSs are supported
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems()) {
				if (refSys.isSupported())
					LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
				else
					LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
			}

		} catch (DatabaseConfigurationException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
			dbPool.forceDisconnect();
		} catch (SQLException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
			dbPool.forceDisconnect();			
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
