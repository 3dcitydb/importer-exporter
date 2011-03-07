package de.tub.citydb.concurrent;

import java.sql.SQLException;
import java.util.Vector;

import javax.xml.bind.JAXBContext;

import org.citygml4j.factory.CityGMLFactory;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.DBGmlIdLookupServerManager;
import de.tub.citydb.db.exporter.DBSplittingResult;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.sax.events.SAXEvent;

public class DBExportWorkerFactory implements WorkerFactory<DBSplittingResult> {
	private final JAXBContext jaxbContext;
	private final DBConnectionPool dbConnectionPool;
	private final WorkerPool<Vector<SAXEvent>> ioWriterPool;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CityGMLFactory cityGMLFactory;
	private final ExportFilter exportFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportWorkerFactory(
			JAXBContext jaxbContext,
			DBConnectionPool dbConnectionPool,
			WorkerPool<Vector<SAXEvent>> ioWriterPool,
			WorkerPool<DBXlink> xlinkExporterPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CityGMLFactory cityGMLFactory,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.dbConnectionPool = dbConnectionPool;
		this.ioWriterPool = ioWriterPool;
		this.xlinkExporterPool = xlinkExporterPool;
		this.lookupServerManager = lookupServerManager;
		this.cityGMLFactory = cityGMLFactory;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBSplittingResult> getWorker() {
		DBExportWorker dbWorker = null;

		try {
			dbWorker = new DBExportWorker(
					jaxbContext,
					dbConnectionPool,
					ioWriterPool,
					xlinkExporterPool,
					lookupServerManager,
					cityGMLFactory,
					exportFilter,
					config,
					eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		}

		return dbWorker;
	}

}
