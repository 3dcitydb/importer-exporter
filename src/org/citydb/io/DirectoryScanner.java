/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.citydb.log.Logger;

public class DirectoryScanner {
	private final Logger LOG = Logger.getInstance();

	private FilenameFilter filenameFilter;
	private volatile boolean shouldRun;
	private boolean isScanning;
	private boolean recursive;

	public DirectoryScanner() {
		filenameFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return true;
			}
		};
	}

	public DirectoryScanner(boolean recursive) {
		this();
		this.recursive = recursive;
	}

	public void addFilenameFilter(FilenameFilter filter) {
		this.filenameFilter = new CombinedFilenameFilter(this.filenameFilter, filter);
	}

	public void enableRecursiveDirectoryScan(boolean enable) {
		recursive = enable;
	}

	public List<File> getFiles(File baseDir) {
		List<File> files = new ArrayList<File>();

		if (baseDir != null && baseDir.exists()) {
			shouldRun = isScanning = true;

			if (baseDir.isFile() && baseDir.canRead())
				files.add(baseDir);
			else	
				buildFileList(baseDir, files, true);
			
			isScanning = false;
		}

		return files;
	}

	public List<File> getFiles(File[] baseDir) {
		shouldRun = isScanning = true;

		List<File> files = new ArrayList<File>();
		for (File file : baseDir) {
			if (!shouldRun)
				break;

			if (file == null || !file.exists())
				continue;

			if (file.isFile() && file.canRead()) {
				files.add(file);
				continue;
			}

			buildFileList(file, files, true);
		}

		isScanning = false;
		return files;
	}

	private void buildFileList(File file, List<File> files, boolean scanDir) {
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
			String filename = file.getName();
			if (filename.isEmpty()) {
				LOG.error("Failed to read file '" + file.toString() + "'.");
				return;
			}

			if (filenameFilter.accept(file.getParentFile(), filename))
				files.add(new File(file.getAbsolutePath()));
		} 

		else if (file.isDirectory() && scanDir) {
			LOG.debug("Scanning directory '" + file.toString() + "'.");

			File[] contents = file.listFiles();
			if (contents != null)
				for (File subFile : contents)
					buildFileList(subFile, files, recursive);
		}

	}

	public boolean isScanning() {
		return isScanning;
	}

	public void stopScanning() {
		shouldRun = false;
	}

	public static final class CityGMLFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			name = name.toUpperCase();
			return (name.endsWith(".GML") ||
					name.endsWith(".XML") ||
					name.endsWith(".CITYGML"));
		}
	}

	private class CombinedFilenameFilter implements FilenameFilter {
		private final FilenameFilter first;
		private final FilenameFilter second;

		CombinedFilenameFilter(FilenameFilter first, FilenameFilter second) {
			this.first = first;
			this.second = second;
		}

		public boolean accept(File dir, String name) {
			return first.accept(dir, name) && second.accept(dir, name);
		}
	}

}
