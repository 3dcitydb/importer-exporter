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
