package de.tub.citydb.concurrent;

import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.SAXWriter;

public class IOWriterWorkerFactory implements WorkerFactory<SAXBuffer> {
	private final SAXWriter saxWriter;

	public IOWriterWorkerFactory(SAXWriter saxWriter) {
		this.saxWriter = saxWriter;
	}

	@Override
	public Worker<SAXBuffer> getWorker() {
		return new IOWriterWorker(saxWriter);
	}
}
