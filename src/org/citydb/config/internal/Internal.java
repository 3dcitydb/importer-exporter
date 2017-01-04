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
package org.citydb.config.internal;

import java.io.File;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.modules.citygml.importer.util.AffineTransformer;

public class Internal {	
	public static final String CODELIST_DELIMITER = "--/\\--";
	public static final String GEOMETRY_XLINK = "isXlink";
	public static final String GEOMETRY_ORIGINAL = "origGeom";
	public static final String GEOMETRY_INVALID = "geomInvalid";
	public static final String UNIQUE_TEXTURE_FILENAME_PREFIX = "tex_";
	
	// path names and files
	public static final String USER_PATH = System.getProperty("user.home") + File.separator + "3dcitydb" + File.separator + "importer-exporter";	
	public static final String PLUGINS_PATH = System.getProperty("user.dir") + File.separator + "plugins";
	public static final String SRS_TEMPLATES_PATH = System.getProperty("user.dir") + File.separator + "templates" + File.separator + "CoordinateReferenceSystems";
	public static final String DEFAULT_LOG_PATH = USER_PATH + File.separator + "log";
	public static final String DEFAULT_IMPORT_LOG_PATH = DEFAULT_LOG_PATH + File.separator + "imported-features";
	
	private String configPath =  USER_PATH + File.separator + "config";
	private String currentLogPath = "";
	private String configProject = "project.xml";
	private String configGui = "gui.xml";
	private String importPath = "";
	private File[] importFiles;
	private File currentImportFile;
	private String exportPath = "";
	private String exportFileName = "";
	private String exportTextureFilePath = "";

	// database related settings
	private DatabaseSrs exportTargetSRS;

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

	public DatabaseSrs getExportTargetSRS() {
		return exportTargetSRS;
	}

	public void setExportTargetSRS(DatabaseSrs exportTargetSRS) {
		this.exportTargetSRS = exportTargetSRS;
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

	public String getConfigProject() {
		return configProject;
	}

	public void setConfigProject(String configProject) {
		this.configProject = configProject;
	}

	public String getConfigGui() {
		return configGui;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
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
