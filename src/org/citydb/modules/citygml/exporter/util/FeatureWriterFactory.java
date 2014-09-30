package org.citydb.modules.citygml.exporter.util;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXEventBuffer;

public class FeatureWriterFactory implements FeatureProcessorFactory<FeatureWriter> {
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final JAXBBuilder jaxbBuilder;
	private final Config config;
	
	public FeatureWriterFactory(WorkerPool<SAXEventBuffer> ioWriterPool, JAXBBuilder jaxbBuilder, Config config) {
		this.ioWriterPool = ioWriterPool;
		this.jaxbBuilder = jaxbBuilder;
		this.config = config;
	}

	@Override
	public FeatureWriter createFeatureProcessor() {
		return new FeatureWriter(ioWriterPool, jaxbBuilder, config);
	}

}
