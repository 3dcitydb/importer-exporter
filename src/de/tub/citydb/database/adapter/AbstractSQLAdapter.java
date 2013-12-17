package de.tub.citydb.database.adapter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import de.tub.citydb.api.geometry.BoundingBox;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

public abstract class AbstractSQLAdapter {
	protected final Logger LOG = Logger.getInstance();
	
	protected Properties databaseOperations;
	
	public abstract String getInteger();
	public abstract String getSmallInt();
	public abstract String getBigInt();
	public abstract String getNumeric();
	public abstract String getNumeric(int precision);	
	public abstract String getNumeric(int precision, int scale);
	public abstract String getReal();
	public abstract String getDoublePrecision();
	public abstract String getCharacter(int nrOfChars);
	public abstract String getCharacterVarying(int nrOfChars);
	public abstract String getCreateUnloggedTable(String tableName, String columns);
	public abstract String getCreateUnloggedTableAsSelectFrom(String targetTableName, String sourceTableName);
	public abstract String getUnloggedIndexProperty();
	
	public abstract boolean requiresPseudoTableInSelect();
	public abstract String getPseudoTableName();
	public abstract String getBoundingBoxPredicate(String attributeName, BoundingBox bbox, boolean overlap);
	public abstract boolean spatialPredicateRequiresNoIndexHint();
	public abstract String getHierarchicalGeometryQuery();
	public abstract String getTextureImageContentLength(String columName);
	public abstract String getNextSequenceValue(DBSequencerEnum sequence);
	public abstract String getNextSequenceValuesQuery(DBSequencerEnum sequence);
	protected abstract String getSequenceName(DBSequencerEnum sequence);
	
	public String getBoundingBoxPredicate(String attributeName, String tablePrefix, BoundingBox bbox, boolean overlap) {
		return getBoundingBoxPredicate(tablePrefix + '.' + attributeName, bbox, overlap);
	}

	public String getTextureImageContentLength(String columName, String tablePrefix) {
		return getTextureImageContentLength(tablePrefix + '.' + columName);
	}
	
	public abstract TextureImageImportAdapter getTextureImageImportAdapter(Connection connection) throws SQLException;
	public abstract TextureImageExportAdapter getTextureImageExportAdapter(Connection connection);
	public abstract BlobImportAdapter getBlobImportAdapter(Connection connection) throws SQLException;
	public abstract BlobExportAdapter getBlobExportAdapter(Connection connection);
	
	public String resolveDatabaseOperationName(String operation) {
		if (databaseOperations == null) {
			try {
				databaseOperations = new Properties();
				databaseOperations.load(getClass().getResourceAsStream("operations.properties"));
			} catch (IOException e) {
				throw new IllegalStateException("Failed to load operations properties file.", e);
			}
		}

		return databaseOperations.getProperty(operation);
	}
}
