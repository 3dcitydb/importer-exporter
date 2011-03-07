package de.tub.citydb.concurrent;


public interface WorkerFactory<T> {
	Worker<T> getWorker();
}
