/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.config.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.modules.citygml.importer.util.AffineTransformer;

public class Internal {
	public static final String CODELIST_DELIMITER = "--/\\--";
	public static final String GEOMETRY_XLINK = "isXlink";
	public static final String GEOMETRY_ORIGINAL = "origGeom";
	public static final String GEOMETRY_INVALID = "geomInvalid";
	public static final String UNIQUE_TEXTURE_FILENAME_PREFIX = "tex_";
	public static final List<String> CITYDB_ACCEPT_VERSIONS = new ArrayList<String>(Arrays.asList(new String[]{"3.0.0"}));
	
	// path names and files
	public static final String USER_PATH = System.getProperty("user.home") + File.separator + "3dcitydb" + File.separator + "importer-exporter-3.0";	
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
	private String currentGmlIdCodespace = "";
	private AffineTransformer affineTransformer;

	// internal flags
	private boolean useInternalBBoxFilter = false;
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

	public boolean isUseInternalBBoxFilter() {
		return useInternalBBoxFilter;
	}

	public void setUseInternalBBoxFilter(boolean useInternalBBoxFilter) {
		this.useInternalBBoxFilter = useInternalBBoxFilter;
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
