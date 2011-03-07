package de.tub.citydb.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.log.Logger;

public class InputFileHandler implements EventListener {
	private final Logger LOG = Logger.getInstance();
	
	private volatile boolean shouldRun = true;
	
	public InputFileHandler(EventDispatcher eventDispatcher) {
		eventDispatcher.addListener(EventType.Interrupt, this);
	}
	
	public List<File> getFiles(String[] input) {
		List<File> files = new ArrayList<File>();

		for (String importFile : input) {
			if (!shouldRun)
				break;

			if (importFile == null)
				continue;

			String fileName = importFile.trim();
			if (fileName.length() == 0)
				continue;

			buildFileList(new File(fileName), files);
		}

		return files;
	}

	private void buildFileList(File file, List<File> files) {
		if (!shouldRun)
			return;

		if (!file.exists()) {
			LOG.error("Failed to find file '" + file.toString() + "'.");
			return;
		}

		if (!file.canRead()) {
			LOG.error("Failed to read file '" + file.toString() + "'.");
			return;
		}

		if (file.isFile()) {
			String name = file.getName();
			if (name == null || name.length() == 0) {
				LOG.error("Failed to read file '" + file.toString() + "'.");
				return;
			}

			files.add(new File(file.getAbsolutePath()));
		} else if (file.isDirectory()) {
			LOG.debug("Scanning directory '" + file.toString() + "'.");			
			for (File subFile : file.listFiles(new CityGMLFileFilter()))
				buildFileList(subFile, files);
		}

	}

	private class CityGMLFileFilter implements FileFilter {

		@Override
		public boolean accept(File pathname) {
			String fileName = pathname.getName().toUpperCase();
			return (pathname.isDirectory() || 
					fileName.endsWith("GML") ||
					fileName.endsWith("XML") ||
					fileName.endsWith("CITYGML"));
		}

	}
	
	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.Interrupt)
			shouldRun = false;
	}

}
