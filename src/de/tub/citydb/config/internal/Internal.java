/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.internal;

import java.io.File;
import java.util.ResourceBundle;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.modules.citygml.importer.util.AffineTransformer;

public class Internal {
	public static final int ORACLE_MAX_BATCH_SIZE = 65535;
	public static final String ORACLE_DEFAULT_WORKSPACE = "LIVE";
	public static final String GML_NAME_DELIMITER = " --/\\-- ";
	public static final String GEOMETRY_XLINK = "isXlink";
	public static final String GEOMETRY_ORIGINAL = "origGeom";

	// language pack
	public static ResourceBundle I18N;
	
	// path names and files
	public static final String USER_PATH = System.getProperty("user.home") + File.separator + "3DCityDB-Importer-Exporter";	
	public static final String PLUGINS_PATH = System.getProperty("user.dir") + File.separator + "plugins";
	public static final String SRS_TEMPLATES_PATH = System.getProperty("user.dir") + File.separator + "templates" + File.separator + "CoordinateReferenceSystems";
	private String configPath =  USER_PATH + File.separator + "config";
	private String logPath = USER_PATH + File.separator + "log";
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

	public Internal() {
	}

	public String getCurrentGmlIdCodespace() {
		return currentGmlIdCodespace;
	}

	public void setCurrentGmlIdCodespace(String currentGmlIdCodespace) {
		this.currentGmlIdCodespace = currentGmlIdCodespace;
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

	public String getLogPath() {
		return logPath;
	}

	public String getCurrentLogPath() {
		return currentLogPath;
	}

	public void setCurrentLogPath(String currentLogPath) {
		this.currentLogPath = currentLogPath;
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

}
