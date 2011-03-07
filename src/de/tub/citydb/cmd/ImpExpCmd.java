package de.tub.citydb.cmd;

import java.sql.SQLException;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.importer.LocalXMLSchemaType;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.controller.Exporter;
import de.tub.citydb.controller.Importer;
import de.tub.citydb.controller.KmlExporter;
import de.tub.citydb.controller.XMLValidator;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.DBUtil;

public class ImpExpCmd {
	private final Logger LOG = Logger.getInstance();
	private JAXBContext jaxbCityGMLContext;
	private JAXBContext jaxbKmlContext;
	private JAXBContext jaxbColladaContext;
	private DBConnectionPool dbPool;
	private Config config;

	public ImpExpCmd(JAXBContext jaxbCityGMLContext,
			DBConnectionPool dbPool,
			Config config) {
		this.jaxbCityGMLContext = jaxbCityGMLContext;
		this.dbPool = dbPool;
		this.config = config;
	}

	public ImpExpCmd(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DBConnectionPool dbPool,
			Config config) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbPool = dbPool;
		this.config = config;
	}

	public void doImport() {
		initDBPool();
		if (!config.getInternal().isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database import...");

		EventDispatcher eventDispatcher = new EventDispatcher();
		Importer importer = new Importer(jaxbCityGMLContext, dbPool, config, eventDispatcher);
		boolean success = importer.doProcess();

		try {
			eventDispatcher.shutdownAndWait();
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

		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		if (xmlValidation.getUseLocalSchemas().isSet()) {
			Set<LocalXMLSchemaType> schemas = xmlValidation.getUseLocalSchemas().getSchemas();
			if (schemas.isEmpty())
				for (LocalXMLSchemaType schema : LocalXMLSchemaType.values())
					schemas.add(schema);
		}

		EventDispatcher eventDispatcher = new EventDispatcher();
		XMLValidator validator = new XMLValidator(jaxbCityGMLContext, config, eventDispatcher);
		boolean success = validator.doProcess();

		try {
			eventDispatcher.shutdownAndWait();
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
		if (!config.getInternal().isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database export...");

		EventDispatcher eventDispatcher = new EventDispatcher();
		Exporter exporter = new Exporter(jaxbCityGMLContext, dbPool, config, eventDispatcher);
		boolean success = exporter.doProcess();
		eventDispatcher.shutdown();

		if (success) {
			LOG.info("Database export successfully finished.");
		} else {
			LOG.warn("Database export aborted.");
		}
	}

	public void doKmlExport() {
		initDBPool();
		if (!config.getInternal().isConnected()) {
			LOG.error("Aborting...");
			return;
		}

		LOG.info("Initializing database export...");

		EventDispatcher eventDispatcher = new EventDispatcher();
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
		eventDispatcher.shutdown();

		if (success) {
			LOG.info("Database export successfully finished.");
		} else {
			LOG.warn("Database export aborted.");
		}
	}

	private void initDBPool() {	
		// check active connection
		DBConnection conn = config.getProject().getDatabase().getActiveConnection();
		Internal intConfig = config.getInternal();
		
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
			intConfig.setCurrentDbPassword(conn.getPassword());

		LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");

		try {
			dbPool.init();
			
			LOG.info("Database connection established.");
			LOG.info("Database SRID: " + ReferenceSystem.SAME_AS_IN_DB.getSrid());
			LOG.info("Database GML_SRS_Name: " + ReferenceSystem.SAME_AS_IN_DB.getSrsName());
			LOG.info("Database versioning: " + conn.getVersioning());
			
			// check whether user-defined SRSs are supported
			try {
				DBUtil dbUtil = DBUtil.getInstance(dbPool);
				
				for (ReferenceSystem refSys: config.getProject().getDatabase().getReferenceSystems()) {
					boolean isSupported = dbUtil.isSrsSupported(refSys.getSrid());
					refSys.setSupported(isSupported);
					
					if (isSupported)
						LOG.debug("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") supported.");
					else
						LOG.warn("Reference system '" + refSys.getDescription() + "' (SRID: " + refSys.getSrid() + ") NOT supported.");
				}
			} catch (SQLException sqlEx) {
				LOG.error("Error while checking user-defined SRSs: " + sqlEx.getMessage().trim());
			}

		} catch (SQLException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
			intConfig.unsetOpenConnection();			
		}
	}
}
