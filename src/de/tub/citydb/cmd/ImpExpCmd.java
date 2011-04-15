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
package de.tub.citydb.cmd;

import java.sql.SQLException;

import javax.xml.bind.JAXBContext;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.log.LogLevelType;
import de.tub.citydb.api.log.Logger;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.modules.citygml.exporter.controller.Exporter;
import de.tub.citydb.modules.citygml.importer.controller.Importer;
import de.tub.citydb.modules.citygml.importer.controller.XMLValidator;
import de.tub.citydb.modules.kml.controller.KmlExporter;
import de.tub.citydb.util.DBUtil;

public class ImpExpCmd {
	private final Logger LOG = Logger.getInstance();
	private final DBConnectionPool dbPool;
	private JAXBBuilder cityGMLBuilder;
	private JAXBContext jaxbKmlContext;
	private JAXBContext jaxbColladaContext;
	private Config config;

	public ImpExpCmd(JAXBBuilder cityGMLBuilder, Config config) {
		this.cityGMLBuilder = cityGMLBuilder;
		this.config = config;
		dbPool = DBConnectionPool.getInstance();
	}

	public ImpExpCmd(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.config = config;
		dbPool = DBConnectionPool.getInstance();
	}

	public void doImport() {
		initDBPool();
		if (!dbPool.isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database import...");

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

	public void doValidate() {
		LOG.info("Initializing XML validation...");

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
		try {
			kmlExporter.calculateRowsColumnsAndDelta();
		}
		catch (SQLException sqle) {
			String srsDescription = filter.getComplexFilter().getBoundingBox().getSRS().getDescription();
			LOG.error(srsDescription + " " + sqle.getMessage());
			return;
		}
		boolean success = kmlExporter.doProcess();

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

		if (conn.getServer() == null || conn.getServer().trim().equals("")) {
			LOG.error("No database server configured in project settings.");
			return;
		}

		if (conn.getPort() == null || conn.getPort() == 0) {
			LOG.error("No valid database connection port configured in project settings.");
			return;
		}

		if (conn.getSid() == null || conn.getSid().trim().equals("")) {
			LOG.error("No valid database sid configured in project settings.");
			return;
		}

		if (conn.getUser() == null || conn.getUser().trim().equals("")) {
			LOG.error("No database user configured in project settings.");
			return;
		}

		if (conn.getPassword() == null || conn.getPassword().trim().equals("")) {
			LOG.error("No password for database user configured in project settings.");
			return;
		} else
			conn.setInternalPassword(conn.getPassword());

		LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");

		try {
			dbPool.connect(conn);
			
			LOG.info("Database connection established.");
			conn.getMetaData().toConsole(LogLevelType.INFO);
			
			// check whether user-defined SRSs are supported
			try {
				for (ReferenceSystem refSys: config.getProject().getDatabase().getReferenceSystems()) {
					boolean isSupported = DBUtil.isSrsSupported(refSys.getSrid());
					refSys.setSupported(isSupported);
					
					if (isSupported)
						LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
					else
						LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
				}
			} catch (SQLException sqlEx) {
				LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
			}

		} catch (DatabaseConfigurationException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
			dbPool.forceDisconnect();
		} catch (SQLException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
			dbPool.forceDisconnect();			
		} 
	}
}
