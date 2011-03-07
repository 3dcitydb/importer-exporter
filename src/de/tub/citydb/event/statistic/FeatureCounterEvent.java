package de.tub.citydb.event.statistic;

import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class FeatureCounterEvent extends Event {
	private HashMap<CityGMLClass, Long> featureCounterMap;

	public FeatureCounterEvent(HashMap<CityGMLClass, Long> featureCounterMap) {
		super(EventType.FeatureCounter);
		this.featureCounterMap = featureCounterMap;
	}

	public HashMap<CityGMLClass, Long> getCounter() {
		return featureCounterMap;
	}

}
