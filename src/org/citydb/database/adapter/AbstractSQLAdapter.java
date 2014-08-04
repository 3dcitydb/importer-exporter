package org.citydb.database.adapter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.citydb.api.geometry.BoundingBox;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

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
	public abstract String getPolygon2D();
	public abstract String getCreateUnloggedTable(String tableName, String columns);
	public abstract String getCreateUnloggedTableAsSelectFrom(String targetTableName, String sourceTableName);
	public abstract String getUnloggedIndexProperty();
	
	public abstract boolean requiresPseudoTableInSelect();
	public abstract String getPseudoTableName();
	public abstract String getBoundingBoxPredicate(String attributeName, BoundingBox bbox, boolean overlap);
	public abstract boolean spatialPredicateRequiresNoIndexHint();
	public abstract String getHierarchicalGeometryQuery();
	public abstract String getNextSequenceValue(DBSequencerEnum sequence);
	public abstract String getCurrentSequenceValue(DBSequencerEnum sequence);
	public abstract String getNextSequenceValuesQuery(DBSequencerEnum sequence);
	
	public String getBoundingBoxPredicate(String attributeName, String tablePrefix, BoundingBox bbox, boolean overlap) {
		return getBoundingBoxPredicate(tablePrefix + '.' + attributeName, bbox, overlap);
	}
	
	public abstract BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type) throws SQLException;
	public abstract BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type);
	
	protected String getSequenceName(DBSequencerEnum sequence) {
		switch (sequence) {
		case SURFACE_GEOMETRY_ID_SEQ:
			return "SURFACE_GEOMETRY_SEQ";
		case CITYOBJECT_ID_SEQ:
			return "CITYOBJECT_SEQ";
		case SURFACE_DATA_ID_SEQ:
			return "SURFACE_DATA_SEQ";
		case CITYOBJECT_GENERICATTRIB_ID_SEQ:
			return "CITYOBJECT_GENERICATT_SEQ";
		case EXTERNAL_REFERENCE_ID_SEQ:
			return "EXTERNAL_REF_SEQ";
		case APPEARANCE_ID_SEQ:
			return "APPEARANCE_SEQ";
		case TEX_IMAGE_ID_SEQ:
			return "TEX_IMAGE_SEQ";
		case ADDRESS_ID_SEQ:
			return "ADDRESS_SEQ";
		case IMPLICIT_GEOMETRY_ID_SEQ:
			return "IMPLICIT_GEOMETRY_SEQ";
		case RASTER_REL_GEORASTER_ID_SEQ:
			return "RASTER_REL_GEORASTER_SEQ";
		default:
			return null;
		}
	}
	
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
