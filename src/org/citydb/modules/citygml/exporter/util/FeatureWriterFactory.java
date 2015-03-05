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
package org.citydb.modules.citygml.exporter.util;

import org.citydb.api.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXEventBuffer;

public class FeatureWriterFactory implements FeatureProcessorFactory {
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final JAXBBuilder jaxbBuilder;
	private final Config config;
	
	public FeatureWriterFactory(WorkerPool<SAXEventBuffer> ioWriterPool, JAXBBuilder jaxbBuilder, Config config) {
		this.ioWriterPool = ioWriterPool;
		this.jaxbBuilder = jaxbBuilder;
		this.config = config;
	}

	@Override
	public FeatureProcessor createFeatureProcessor() {
		return new FeatureWriter(ioWriterPool, jaxbBuilder, config);
	}

}
