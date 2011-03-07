package de.tub.citydb.concurrent;

import java.util.Vector;

import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.events.SAXEvent;

public class IOWriterWorkerFactory implements WorkerFactory<Vector<SAXEvent>> {
	private final SAXWriter saxWriter;

	public IOWriterWorkerFactory(SAXWriter saxWriter) {
		this.saxWriter = saxWriter;
	}

	@Override
	public Worker<Vector<SAXEvent>> getWorker() {
		return new IOWriterWorker(saxWriter);
	}
}
