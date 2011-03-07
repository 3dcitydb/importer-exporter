package de.tub.citydb.concurrent;

import javax.xml.bind.JAXBContext;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.core.CityGMLBase;

import de.tub.citydb.config.Config;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.sax.SAXBuffer;

public class FeatureReaderWorkerFactory implements WorkerFactory<SAXBuffer> {
	private final JAXBContext jaxbContext;
	private final WorkerPool<CityGMLBase> dbWorkerPool;
	private final CityGMLFactory cityGMLFactory;
	private final EventDispatcher eventDispatcher;
	private final Config config;

	public FeatureReaderWorkerFactory(JAXBContext jaxbContext, 
			WorkerPool<CityGMLBase> dbWorkerPool, 
			CityGMLFactory cityGMLFactory, 
			EventDispatcher eventDispatcher,
			Config config) {
		this.jaxbContext = jaxbContext;
		this.dbWorkerPool = dbWorkerPool;
		this.cityGMLFactory = cityGMLFactory;
		this.eventDispatcher = eventDispatcher;
		this.config = config;
	}

	@Override
	public Worker<SAXBuffer> getWorker() {
		return new FeatureReaderWorker(jaxbContext, dbWorkerPool, cityGMLFactory, eventDispatcher, config);
	}
}