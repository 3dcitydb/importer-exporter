/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import org.citydb.citygml.importer.util.AffineTransformer;

import java.io.File;

public class Internal {	
	public static final String DEFAULT_DELIMITER = "--/\\--";
	public static final String OBJECT_ORIGINAL_GMLID = "origGMLId";
	public static final String IS_TOP_LEVEL = "isTopLevel";
	public static final String GEOMETRY_XLINK = "isXlink";
	public static final String GEOMETRY_ORIGINAL = "origGeom";
	public static final String GEOMETRY_INVALID = "geomInvalid";
	public static final String TEXTURE_IMAGE_XLINK = "textureXlink";
	public static final String FOREIGN_KEYS_SET = "foreignKeys";
	public static final String EXPORT_STUB = "exportStub";
	public static final String UNIQUE_TEXTURE_FILENAME_PREFIX = "tex_";
	
	private String currentLogPath = "";
	private String importPath = "";
	private File[] importFiles;
	private File currentImportFile;
	private String exportPath = "";
	private String exportFileName = "";
	private String exportTextureFilePath = "";

	// internal variables
	private String currentGmlIdCodespace = null;
	private AffineTransformer affineTransformer;

	// internal flags
	private boolean transformCoordinates = false;
	private boolean exportGlobalAppearances = false;
	private boolean registerGmlIdInCache = false;

	public Internal() {
	}

	public String getExportPath() {
		return exportPath;
	}

	public void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}

	public String getImportPath() {
		return importPath;
	}

	public void setImportPath(String importPath) {
		this.importPath = importPath;
	}

	public String getExportFileName() {
		return exportFileName;
	}

	public void setExportFileName(String exportFileName) {
		this.exportFileName = exportFileName;
	}

	public File[] getImportFiles() {
		return importFiles;
	}

	public void setImportFiles(File[] importFiles) {
		this.importFiles = importFiles;
	}

	public File getCurrentImportFile() {
		return currentImportFile;
	}

	public void setCurrentImportFile(File currentImportFile) {
		this.currentImportFile = currentImportFile;
	}

	public String getExportTextureFilePath() {
		return exportTextureFilePath;
	}

	public void setExportTextureFilePath(String exportTextureFilePath) {
		this.exportTextureFilePath = exportTextureFilePath;
	}

	public boolean isTransformCoordinates() {
		return transformCoordinates;
	}

	public void setTransformCoordinates(boolean transformCoordinates) {
		this.transformCoordinates = transformCoordinates;
	}

	public String getCurrentLogPath() {
		return currentLogPath;
	}

	public void setCurrentLogPath(String currentLogPath) {
		this.currentLogPath = currentLogPath;
	}

	public String getCurrentGmlIdCodespace() {
		return currentGmlIdCodespace;
	}

	public void setCurrentGmlIdCodespace(String currentGmlIdCodespace) {
		this.currentGmlIdCodespace = currentGmlIdCodespace;
	}
	
	public AffineTransformer getAffineTransformer() {
		return affineTransformer;
	}

	public void setAffineTransformer(AffineTransformer affineTransformer) {
		this.affineTransformer = affineTransformer;
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
