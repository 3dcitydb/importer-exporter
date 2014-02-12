package de.tub.citydb.database.adapter.h2;

import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.api.geometry.BoundingBox;
import de.tub.citydb.database.adapter.AbstractSQLAdapter;
import de.tub.citydb.database.adapter.BlobExportAdapter;
import de.tub.citydb.database.adapter.BlobImportAdapter;
import de.tub.citydb.database.adapter.TextureImageExportAdapter;
import de.tub.citydb.database.adapter.TextureImageImportAdapter;
import de.tub.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

public class SQLAdapter extends AbstractSQLAdapter {

	@Override
	public String getInteger() {
		return "BIGINT";
	}

	@Override
	public String getSmallInt() {
		return "SMALLINT";
	}

	@Override
	public String getBigInt() {
		return "BIGINT";
	}

	@Override
	public String getNumeric() {
		return "BIGINT";
	}

	@Override
	public String getNumeric(int precision) {
		if (precision <= 3)
			return "SMALLINT";

		return "BIGINT";
	}

	@Override
	public String getNumeric(int precision, int scale) {
		if (precision == 1 && scale == 0)
			return "TINYINT";

		return "DECIMAL";
	}

	@Override
	public String getReal() {
		return "REAL";
	}

	@Override
	public String getDoublePrecision() {
		return "DOUBLE";
	}

	@Override
	public String getCharacter(int nrOfChars) {
		return "VARCHAR(" + nrOfChars + ")";
	}

	@Override
	public String getCharacterVarying(int nrOfChars) {
		return "VARCHAR(" + nrOfChars + ")";
	}

	@Override
	public String getPolygon2D() {
		return "GEOMETRY";
	}

	@Override
	public String getCreateUnloggedTable(String tableName, String columns) {
		StringBuilder builder = new StringBuilder()
		.append("create table ")
		.append(tableName).append(" ")
		.append(columns);

		return builder.toString();
	}

	@Override
	public String getCreateUnloggedTableAsSelectFrom(String targetTableName, String sourceTableName) {
		StringBuilder builder = new StringBuilder()
		.append("create table ")
		.append(targetTableName).append(" ")
		.append("as select * from ")
		.append(sourceTableName);

		return builder.toString();
	}

	@Override
	public String getUnloggedIndexProperty() {
		return "";
	}

	@Override
	public boolean requiresPseudoTableInSelect() {
		return false;
	}

	@Override
	public String getPseudoTableName() {
		return "";
	}

	@Override
	public String getBoundingBoxPredicate(String attributeName, BoundingBox bbox, boolean overlap) {
		// not required for cache tables
		return "";
	}

	@Override
	public boolean spatialPredicateRequiresNoIndexHint() {
		return false;
	}

	@Override
	public String getHierarchicalGeometryQuery() {
		// not required for cache tables
		return "";
	}

	@Override
	public String getTextureImageContentLength(String columName) {
		// not required for cache tables
		return "";
	}

	@Override
	public String getNextSequenceValue(DBSequencerEnum sequence) {
		// not required for cache tables
		return "";
	}

	@Override
	public String getCurrentSequenceValue(DBSequencerEnum sequence) {
		// not required for cache tables
		return "";
	}

	@Override
	public String getNextSequenceValuesQuery(DBSequencerEnum sequence) {
		// not required for cache tables
		return "";
	}

	@Override
	protected String getSequenceName(DBSequencerEnum sequence) {
		// not required for cache tables
		return "";
	}

	@Override
	public TextureImageImportAdapter getTextureImageImportAdapter(Connection connection) throws SQLException {
		// not required for cache tables
		return null;
	}

	@Override
	public TextureImageExportAdapter getTextureImageExportAdapter(Connection connection) {
		// not required for cache tables
		return null;
	}

	@Override
	public BlobImportAdapter getBlobImportAdapter(Connection connection) throws SQLException {
		// not required for cache tables
		return null;
	}

	@Override
	public BlobExportAdapter getBlobExportAdapter(Connection connection) {
		// not required for cache tables
		return null;
	}

}
