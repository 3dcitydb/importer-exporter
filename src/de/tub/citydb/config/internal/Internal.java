/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ResourceBundle;

import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.ReferenceSystem;

public class Internal {
	public static final int ORACLE_MAX_BATCH_SIZE = 65535;
	public static final String GML_NAME_DELIMITER = " --/\\-- ";
	public static final ReferenceSystem DEFAULT_DB_REF_SYS =  new ReferenceSystem("", 0, "n/a", "", true);
	public static ResourceBundle I18N;
	
	// path names and files
	private String userDir = System.getProperty("user.home") + File.separator + "3DCityDB-Importer-Exporter";	
	private String configPath =  userDir + File.separator + "config";
	private String configProject = "project.xml";
	private String configGui = "gui.xml";
	private String importPath = "";
	private String importFileName = "";
	private String currentImportFileName = "";
	private String exportPath = "";
	private String exportFileName = "";
	private String exportTextureFilePath = "";
	private String logPath = userDir + File.separator + "log";
	private String currentLogPath = "";

	// database related settings
	private DBConnection openConnection;
	private ReferenceSystem exportTargetSRS;
	private String currentDbPassword = "";
	
	// internal variables
	private String currentGmlIdCodespace = "";
	private String locale = "de";

	// internal flags
	private boolean isShuttingDown = false;
	private boolean isConnected = false;
	private boolean useXMLValidation = false;
	private boolean useInternalBBoxFilter = false;
	private boolean transformCoordinates = false;
	
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
	
	public Internal() {
	}

	public String getUserDir() {
		return userDir;
	}

	public void setUserDir(String userDir) {
		this.userDir = userDir;
	}

	public String getCurrentGmlIdCodespace() {
		return currentGmlIdCodespace;
	}

	public void setCurrentGmlIdCodespace(String currentGmlIdCodespace) {
		this.currentGmlIdCodespace = currentGmlIdCodespace;
	}

	public String getCurrentDbPassword() {
		return currentDbPassword;
	}

	public void setCurrentDbPassword(String currentDbPassword) {
		this.currentDbPassword = currentDbPassword;
	}
	
	public void setOpenConnection(DBConnection openedConnection) {
		this.openConnection = openedConnection;
		isConnected = true;
		changes.firePropertyChange("database.isConnected", false, true);
	}
	
	public void unsetOpenConnection() {
		openConnection = null;
		isConnected = false;
		changes.firePropertyChange("database.isConnected", true, false);
	}

	public boolean isConnected() {
		return isConnected;
	}

	public DBConnection getOpenConnection() {
		return openConnection;
	}

	public String getExportPath() {
		return exportPath;
	}

	public void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}

	public ReferenceSystem getExportTargetSRS() {
		return exportTargetSRS;
	}

	public void setExportTargetSRS(ReferenceSystem exportTargetSRS) {
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

	public String getImportFileName() {
		return importFileName;
	}

	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}

	public String getCurrentImportFileName() {
		return currentImportFileName;
	}

	public void setCurrentImportFileName(String currentImportFileName) {
		this.currentImportFileName = currentImportFileName;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
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

	public void setConfigGui(String configGui) {
		this.configGui = configGui;
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public boolean isShuttingDown() {
		return isShuttingDown;
	}

	public void setShuttingDown(boolean isShuttingDown) {
		this.isShuttingDown = isShuttingDown;
	}

	public boolean isUseXMLValidation() {
		return useXMLValidation;
	}

	public void setUseXMLValidation(boolean useXMLValidation) {
		this.useXMLValidation = useXMLValidation;
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
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

}
