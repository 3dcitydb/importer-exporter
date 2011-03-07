package de.tub.citydb.event;

import de.tub.citydb.concurrent.Worker;
import de.tub.citydb.concurrent.WorkerFactory;

public class EventWorkerFactory implements WorkerFactory<Event> {
	private final EventDispatcher eventDispatcher;

	public EventWorkerFactory(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<Event> getWorker() {
		return new EventWorker(eventDispatcher);
	}

}
