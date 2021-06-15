/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.writer;

import org.citydb.util.concurrent.Worker;
import org.citydb.config.project.global.LogLevel;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.InterruptEvent;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXWriter;
import org.xml.sax.SAXException;

import java.util.concurrent.locks.ReentrantLock;

public class XMLWriterWorker extends Worker<SAXEventBuffer> {
	private final ReentrantLock runLock = new ReentrantLock();	
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final SAXWriter saxWriter;
	private final EventDispatcher eventDispatcher;

	public XMLWriterWorker(SAXWriter saxWriter, EventDispatcher eventDispatcher) {
		this.saxWriter = saxWriter;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public void interrupt() {
		shouldRun = false;
	}

	@Override
	public void run() {
		if (firstWork != null) {
			doWork(firstWork);
			firstWork = null;
		}

		while (shouldRun) {
			try {
				SAXEventBuffer work = workQueue.take();
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}
	}

	private void doWork(SAXEventBuffer work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;
			
			work.send(saxWriter, true);
		} catch (SAXException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to write XML content.", LogLevel.ERROR, e, eventChannel, this));
			shouldWork = false;
		} finally {
			runLock.unlock();
		}
	}
	
}
