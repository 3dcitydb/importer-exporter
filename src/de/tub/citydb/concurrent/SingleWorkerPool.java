package de.tub.citydb.concurrent;

public class SingleWorkerPool<T> extends WorkerPool<T> {

	public SingleWorkerPool(WorkerFactory<T> workerFactory,
							int queueSize,
							boolean fair) {
		super(1, 1, workerFactory, queueSize, fair);
	}

	public SingleWorkerPool(WorkerFactory<T> workerFactory,
							int queueSize) {
		super(1, 1, workerFactory, queueSize, false);
	}
}
