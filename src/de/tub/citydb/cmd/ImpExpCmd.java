package de.tub.citydb.cmd;

import java.sql.SQLException;
import java.util.Set;

import javax.xml.bind.JAXBContext;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.DBVersioning;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.importer.ImpSchemaType;
import de.tub.citydb.config.project.importer.ImpXMLValidation;
import de.tub.citydb.controller.Exporter;
import de.tub.citydb.controller.Importer;
import de.tub.citydb.controller.XMLValidator;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.DBUtil;

public class ImpExpCmd {
	private final Logger LOG = Logger.getInstance();
	private JAXBContext jaxbCityGMLContext;
	private DBConnectionPool dbPool;
	private Config config;
	
	public ImpExpCmd(JAXBContext jaxbCityGMLContext,
			DBConnectionPool dbPool,
			Config config) {
		this.jaxbCityGMLContext = jaxbCityGMLContext;
		this.dbPool = dbPool;
		this.config = config;
	}
	
	public void doImport() {
		initDBPool();
		if (!config.getInternal().isDbIsConnected()) {
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
		
		ImpXMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		if (xmlValidation.getUseLocalSchemas().isSet()) {
			Set<ImpSchemaType> schemas = xmlValidation.getUseLocalSchemas().getSchemas();
			if (schemas.isEmpty())
				for (ImpSchemaType schema : ImpSchemaType.values())
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
		if (!config.getInternal().isDbIsConnected()) {
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
	
	private void initDBPool() {	
		// check active connection
		Database db = config.getProject().getDatabase();
		DBConnection conn = db.getActiveConnection();
		
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
			config.getInternal().setCurrentDbPassword(conn.getPassword());

		LOG.info("Connecting to database profile '" + conn.getDescription() + "'.");
		
		try {
			dbPool.init(config);
			DBUtil dbUtil = new DBUtil(dbPool);
			String[] dbInfo = dbUtil.getDatabaseInfo();
			Internal intConfig = config.getInternal();
			
			if (dbInfo != null) {
				if (dbInfo[0] != null)
					intConfig.setDbSrid(dbInfo[0]);
				else
					intConfig.setDbSrid("81989002");

				if (dbInfo[1] != null)
					intConfig.setDbSrsName(dbInfo[1]);
				else
					intConfig.setDbSrsName("urn:ogc:def:crs,crs:EPSG:6.12:3068,crs:EPSG:6.12:5783");
				
				if (dbInfo[2] != null)
					intConfig.setDbVersioning(DBVersioning.fromValue(dbInfo[2]));
				else
					intConfig.setDbVersioning(DBVersioning.OFF);
			}

			config.getInternal().setDbIsConnected(true);			
			LOG.info("Database connection established.");
			LOG.info("Database SRID: " + intConfig.getDbSrid());
			LOG.info("Database GML_SRS_Name: " + intConfig.getDbSrsName());
			LOG.info("Database versioning: " + intConfig.getDbVersioning());
		} catch (SQLException e) {
			LOG.error("Connection to database could not be established: " + e.getMessage());
			config.getInternal().setDbIsConnected(false);			
		}
	}
}
