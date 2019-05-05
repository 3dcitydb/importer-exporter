/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.citygml.exporter.util;

import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.core.Address;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.util.walker.GMLWalker;

import java.util.HashMap;
import java.util.Map;

public class ExportCounter {
	private final SchemaMapping schemaMapping;
	private final Map<Integer, Long> objectCounter;
	private final Map<GMLClass, Long> geometryCounter;
	private final CounterWalker counterWalker;
	
	public ExportCounter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
		objectCounter = new HashMap<>();
		geometryCounter = new HashMap<>();
		counterWalker = new CounterWalker();
	}

	public void updateExportCounter(AbstractGML object) {
		object.accept(counterWalker);
	}

	private void updateObjectCounter(int objectClassId) {
		Long counter = objectCounter.get(objectClassId);
		if (counter == null)
			objectCounter.put(objectClassId, 1L);
		else
			objectCounter.put(objectClassId, counter + 1);		
	}

	private void updateGeometryCounter(GMLClass type) {
		Long counter = geometryCounter.get(type);
		if (counter == null)
			geometryCounter.put(type, 1L);
		else
			geometryCounter.put(type, counter + 1);
	}

	public Map<Integer, Long> getAndResetObjectCounter() {
		Map<Integer, Long> tmp = new HashMap<>(objectCounter);
		objectCounter.clear();
		return tmp;
	}

	public Map<GMLClass, Long> getAndResetGeometryCounter() {
		Map<GMLClass, Long> tmp = new HashMap<>(geometryCounter);
		geometryCounter.clear();
		return tmp;
	}

	private final class CounterWalker extends GMLWalker {
		public void visit(AbstractGML object) {
			AbstractObjectType<?> type = schemaMapping.getAbstractObjectType(Util.getObjectClassId(object.getClass()));
			if (type != null)
				updateObjectCounter(type.getObjectClassId());
			else if (object instanceof ImplicitGeometry)
				updateObjectCounter(MappingConstants.IMPLICIT_GEOMETRY_OBJECTCLASS_ID);
		}

		public void visit(AbstractSurfaceData surfaceData) {
			// do not count surface data
		}

		public void visit(Address address) {
			// do not count addresses
		}

		public void visit(AbstractGeometry geometry) {
			updateGeometryCounter(geometry.getGMLClass());
		}
	}
}
