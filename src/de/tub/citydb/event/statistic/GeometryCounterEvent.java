package de.tub.citydb.event.statistic;

import java.util.HashMap;

import org.citygml4j.model.gml.GMLClass;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class GeometryCounterEvent extends Event {
	private HashMap<GMLClass, Long> geometryCounterMap;

	public GeometryCounterEvent(HashMap<GMLClass, Long> geometryCounterMap) {
		super(EventType.GeometryCounter);
		this.geometryCounterMap = geometryCounterMap;
	}

	public HashMap<GMLClass, Long> getCounter() {
		return geometryCounterMap;
	}

}
