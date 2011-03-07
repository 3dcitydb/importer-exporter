package de.tub.citydb.config.internal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Internal {
	public static final int ORACLE_MAX_BATCH_SIZE = 65535;
	
	private String currentGmlIdCodespace = "";
	private String currentDbPassword = "";
	private String dbSrsName = "urn:ogc:def:crs,crs:EPSG:6.12:3068,crs:EPSG:6.12:5783";
	private String dbSrid = "81989002";
	private DBVersioning dbVersioning = DBVersioning.OFF;
	private String gmlNameDelimiter = " --/\\-- ";
	private String exportPath = "";
	private String exportTextureFilePath = "";
	private String importPath = "";
	private String exportFileName = "";
	private String importFileName = "";
	private String locale = "de";
	private String configPath = "config";
	private String configProject = "project.xml";
	private String configGui = "gui.xml";
	private boolean dbIsConnected = false;

	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	public Internal() {
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

	public String getDbSrsName() {
		return dbSrsName;
	}

	public void setDbSrsName(String dbSrsName) {
		this.dbSrsName = dbSrsName;
	}

	public String getDbSrid() {
		return dbSrid;
	}

	public void setDbSrid(String dbSrid) {
		this.dbSrid = dbSrid;
	}

	public DBVersioning getDbVersioning() {
		return dbVersioning;
	}

	public void setDbVersioning(DBVersioning dbVersioning) {
		this.dbVersioning = dbVersioning;
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

	public String getImportFileName() {
		return importFileName;
	}

	public void setImportFileName(String importFileName) {
		this.importFileName = importFileName;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean isDbIsConnected() {
		return dbIsConnected;
	}

	public void setDbIsConnected(boolean dbIsConnected) {
		boolean oldDbIsConnected = this.dbIsConnected;
		this.dbIsConnected = dbIsConnected;
		changes.firePropertyChange("internal.dbIsConnected", oldDbIsConnected, dbIsConnected);
	}

	public String getExportTextureFilePath() {
		return exportTextureFilePath;
	}

	public void setExportTextureFilePath(String exportTextureFilePath) {
		this.exportTextureFilePath = exportTextureFilePath;
	}

	public String getGmlNameDelimiter() {
		return gmlNameDelimiter;
	}

	public void setGmlNameDelimiter(String gmlNameDelimiter) {
		this.gmlNameDelimiter = gmlNameDelimiter;
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

	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

}
