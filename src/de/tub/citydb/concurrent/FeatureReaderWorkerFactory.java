package de.tub.citydb.concurrent;

import java.util.Vector;

import javax.xml.bind.JAXBContext;

import de.tub.citydb.sax.SAXBuffer.SAXEvent;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.model.citygml.core.CityGMLBase;

public class FeatureReaderWorkerFactory implements WorkerFactory<Vector<SAXEvent>> {
	private final JAXBContext jaxbContext;
	private final WorkerPool<CityGMLBase> dbWorkerPool;
	private final CityGMLFactory cityGMLFactory;

	public FeatureReaderWorkerFactory(JAXBContext jaxbContext, WorkerPool<CityGMLBase> dbWorkerPool, CityGMLFactory cityGMLFactory) {
		this.jaxbContext = jaxbContext;
		this.dbWorkerPool = dbWorkerPool;
		this.cityGMLFactory = cityGMLFactory;
	}

	@Override
	public Worker<Vector<SAXEvent>> getWorker() {
		return new FeatureReaderWorker(jaxbContext, dbWorkerPool, cityGMLFactory);
	}
}