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
package de.tub.citydb.modules.citygml.importer.concurrent;

import org.citygml4j.builder.jaxb.xml.io.reader.CityGMLChunk;
import org.citygml4j.model.citygml.CityGML;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerFactory;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;

public class FeatureReaderWorkerFactory implements WorkerFactory<CityGMLChunk> {
	private final WorkerPool<CityGML> dbWorkerPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public FeatureReaderWorkerFactory(WorkerPool<CityGML> dbWorkerPool,
			Config config,
			EventDispatcher eventDispatcher) {
		this.dbWorkerPool = dbWorkerPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<CityGMLChunk> createWorker() {
		return new FeatureReaderWorker(dbWorkerPool, config, eventDispatcher);
	}
}
