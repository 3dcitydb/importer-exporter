package org.citydb.citygml.exporter.util;

import java.util.HashMap;

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

public class ExportCounter {
	private final SchemaMapping schemaMapping;
	private final HashMap<Integer, Long> objectCounter;
	private final HashMap<GMLClass, Long> geometryCounter;
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
			objectCounter.put(objectClassId, 1l);
		else
			objectCounter.put(objectClassId, counter + 1);		
	}

	private void updateGeometryCounter(GMLClass type) {
		Long counter = geometryCounter.get(type);
		if (counter == null)
			geometryCounter.put(type, 1l);
		else
			geometryCounter.put(type, counter + 1);
	}

	public HashMap<Integer, Long> getAndResetObjectCounter() {
		HashMap<Integer, Long> tmp = new HashMap<>(objectCounter);
		objectCounter.clear();
		return tmp;
	}

	public HashMap<GMLClass, Long> getAndResetGeometryCounter() {
		HashMap<GMLClass, Long> tmp = new HashMap<>(geometryCounter);
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
