package de.tub.citydb.event.validation;

import java.net.URL;
import java.util.Set;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventType;

public class SchemaLocationEvent extends Event {
	private Set<URL> schemaLocationURLs;
	
	public SchemaLocationEvent(Set<URL> schemaLocationURLs) {
		super(EventType.SchemaLocation);
		this.schemaLocationURLs = schemaLocationURLs;
	}

	public Set<URL> getSchemaLocationURLs() {
		return schemaLocationURLs;
	}

}
