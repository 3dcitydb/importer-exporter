/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.config.internal;

import java.nio.file.Path;
import java.util.List;

public class Internal {
	public static boolean IS_GUI_MODE = false;

	private List<Path> importFiles;
	private Path exportFile;
	private String exportTextureURI;

	// internal variables
	private String currentGmlIdCodespace = null;

	// internal flags
	private boolean transformCoordinates = false;
	private boolean exportGlobalAppearances = false;
	private boolean registerGmlIdInCache = false;

	public List<Path> getImportFiles() {
		return importFiles;
	}

	public void setImportFiles(List<Path> importFiles) {
		this.importFiles = importFiles;
	}

	public Path getExportFile() {
		return exportFile;
	}

	public void setExportFile(Path exportFile) {
		this.exportFile = exportFile;
	}

	public String getExportTextureURI() {
		return exportTextureURI;
	}

	public void setExportTextureURI(String exportTextureURI) {
		this.exportTextureURI = exportTextureURI;
	}

	public boolean isTransformCoordinates() {
		return transformCoordinates;
	}

	public void setTransformCoordinates(boolean transformCoordinates) {
		this.transformCoordinates = transformCoordinates;
	}

	public String getCurrentGmlIdCodespace() {
		return currentGmlIdCodespace;
	}

	public void setCurrentGmlIdCodespace(String currentGmlIdCodespace) {
		if (currentGmlIdCodespace != null && !currentGmlIdCodespace.trim().isEmpty())
			this.currentGmlIdCodespace = currentGmlIdCodespace.trim();
	}
	
	public boolean isExportGlobalAppearances() {
		return exportGlobalAppearances;
	}

	public void setExportGlobalAppearances(boolean exportGlobalAppearances) {
		this.exportGlobalAppearances = exportGlobalAppearances;
	}
	
	public boolean isRegisterGmlIdInCache() {
		return registerGmlIdInCache;
	}

	public void setRegisterGmlIdInCache(boolean registerGmlIdInCache) {
		this.registerGmlIdInCache = registerGmlIdInCache;
	}
}
