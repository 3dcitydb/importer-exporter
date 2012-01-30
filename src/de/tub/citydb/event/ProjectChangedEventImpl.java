package de.tub.citydb.event;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.event.global.ProjectChangedEvent;

public final class ProjectChangedEventImpl extends Event implements ProjectChangedEvent {

	public ProjectChangedEventImpl(Object source) {
		super(GlobalEvents.PROJECT_CHANGED, source);
	}
	
}
