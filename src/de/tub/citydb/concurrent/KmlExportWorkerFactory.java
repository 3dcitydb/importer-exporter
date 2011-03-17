package de.tub.citydb.concurrent;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.JAXBContext;

import net.opengis.kml._2.ObjectFactory;

import org.citygml4j.factory.CityGMLFactory;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.kmlExporter.ColladaBundle;
import de.tub.citydb.db.kmlExporter.KmlSplittingResult;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.sax.SAXBuffer;

public class KmlExportWorkerFactory implements WorkerFactory<KmlSplittingResult> {
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DBConnectionPool dbConnectionPool;
	private final WorkerPool<SAXBuffer> ioWriterPool;
	private final ObjectFactory kmlFactory;
	private final CityGMLFactory cityGMLFactory;
	private final ConcurrentLinkedQueue<ColladaBundle> buildingQueue;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public KmlExportWorkerFactory(
			JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DBConnectionPool dbConnectionPool,
			WorkerPool<SAXBuffer> ioWriterPool,
			ObjectFactory kmlFactory,
			CityGMLFactory cityGMLFactory,
			ConcurrentLinkedQueue<ColladaBundle> buildingQueue,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbConnectionPool = dbConnectionPool;
		this.ioWriterPool = ioWriterPool;
		this.kmlFactory = kmlFactory;
		this.cityGMLFactory = cityGMLFactory;
		this.buildingQueue = buildingQueue;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<KmlSplittingResult> getWorker() {
		KmlExportWorker kmlWorker = null;

		try {
			kmlWorker = new KmlExportWorker(
					jaxbKmlContext,
					jaxbColladaContext,
					dbConnectionPool,
					ioWriterPool,
					kmlFactory,
					cityGMLFactory,
					buildingQueue,
					config,
					eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		}

		return kmlWorker;
	}

}
