package de.tub.citydb.database.adapter.postgis;

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

	protected enum BlobType {
		TEXTURE_IMAGE,
		LIBRARY_OBJECT
	}
	
	@Override
	public String getInteger() {
		return "INTEGER";
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
		return "NUMERIC";
	}

	@Override
	public String getNumeric(int precision) {
		return "NUMERIC(" + precision + ")";
	}

	@Override
	public String getNumeric(int precision, int scale) {
		return "NUMERIC(" + precision + "," + scale + ")";
	}

	@Override
	public String getReal() {
		return "REAL";
	}

	@Override
	public String getDoublePrecision() {
		return "DOUBLE PRECISION";
	}

	@Override
	public String getCharacter(int nrOfChars) {
		return "CHAR(" + nrOfChars + ")";
	}

	@Override
	public String getCharacterVarying(int nrOfChars) {
		return "VARCHAR(" + nrOfChars + ")";
	}

	@Override
	public String getCreateUnloggedTable(String tableName, String columns) {
		StringBuilder builder = new StringBuilder()
		.append("create unlogged table ")
		.append(tableName).append(" ")
		.append(columns);

		return builder.toString();
	}

	@Override
	public String getCreateUnloggedTableAsSelectFrom( String targetTableName, String sourceTableName) {
		StringBuilder builder = new StringBuilder()
		.append("create unlogged table ")
		.append(targetTableName).append(" ")
		.append("as select * from ")
		.append(sourceTableName);

		return builder.toString();
	}
	
	@Override
	public String getNextSequenceValue(DBSequencerEnum sequence) {
		return new StringBuilder("nextval('").append(getSequenceName(sequence)).append("')").toString();
	}
	
	@Override
	public String getNextSequenceValuesQuery(DBSequencerEnum sequence) {
		return new StringBuilder("select ")
		.append(resolveDatabaseOperationName("geodb_util.get_seq_values")).append("(")
		.append("'").append(getSequenceName(sequence)).append("'").append(",")
		.append("?").append(")").toString();
	}

	@Override
	protected String getSequenceName(DBSequencerEnum sequence) {
		switch (sequence) {
		case SURFACE_GEOMETRY_ID_SEQ:
			return "surface_geometry_id_seq";
		case CITYOBJECT_ID_SEQ:
			return "cityobject_id_seq";
		case SURFACE_DATA_ID_SEQ:
			return "surface_data_id_seq";
		case CITYOBJECT_GENERICATTRIB_ID_SEQ:
			return "cityobject_genericattrib_id_seq";
		case EXTERNAL_REFERENCE_ID_SEQ:
			return "external_reference_id_seq";
		case APPEARANCE_ID_SEQ:
			return "appearance_id_seq";
		case ADDRESS_ID_SEQ:
			return "address_id_seq";
		case IMPLICIT_GEOMETRY_ID_SEQ:
			return "implicit_geometry_id_seq";
		default:
			return null;
		}
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
		StringBuilder geometry = new StringBuilder()
		.append("ST_GeomFromEWKT('SRID=").append(bbox.getSrs().getSrid())
		.append(";POLYGON((")
		.append(bbox.getLowerLeftCorner().getX()).append(" ").append(bbox.getLowerLeftCorner().getY()).append(",")
		.append(bbox.getLowerLeftCorner().getX()).append(" ").append(bbox.getUpperRightCorner().getY()).append(",")
		.append(bbox.getUpperRightCorner().getX()).append(" ").append(bbox.getUpperRightCorner().getY()).append(",")
		.append(bbox.getUpperRightCorner().getX()).append(" ").append(bbox.getLowerLeftCorner().getY()).append(",")
		.append(bbox.getLowerLeftCorner().getX()).append(" ").append(bbox.getLowerLeftCorner().getY()).append("))')");

		StringBuilder predicate = new StringBuilder();

		if (!overlap)
			predicate.append("ST_CoveredBy(").append(attributeName).append(", ").append(geometry).append(") = 'TRUE'");
		else
			predicate.append('(').append(attributeName).append(" && ").append(geometry).append(')');

		return predicate.toString();
	}

	@Override
	public boolean spatialPredicateRequiresNoIndexHint() {
		return false;
	}

	@Override
	public String getHierarchicalGeometryQuery() {
		StringBuilder query = new StringBuilder()
		.append("WITH RECURSIVE geometry_rec (id, gmlid, gmlid_codespace, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry, level) ")
		.append("AS (SELECT sg.*, 1 AS level FROM surface_geometry sg WHERE sg.id=? UNION ALL ")
		.append("SELECT sg.*, g.level + 1 AS level FROM surface_geometry sg, geometry_rec g WHERE sg.parent_id=g.id) ")
		.append("SELECT * FROM geometry_rec");
		
		return query.toString();
	}
	
	@Override
	public String getTextureImageContentLength(String columName) {
		return new StringBuilder("length(").append(columName).append(")").toString();
	}

	@Override
	public TextureImageImportAdapter getTextureImageImportAdapter(Connection connection) throws SQLException {
		return new BlobImportAdapterImpl(connection, BlobType.TEXTURE_IMAGE);
	}

	@Override
	public TextureImageExportAdapter getTextureImageExportAdapter( Connection connection) {
		return new BlobExportAdapterImpl(connection, BlobType.TEXTURE_IMAGE);
	}

	@Override
	public BlobImportAdapter getBlobImportAdapter(Connection connection) throws SQLException {
		return new BlobImportAdapterImpl(connection, BlobType.LIBRARY_OBJECT);
	}

	@Override
	public BlobExportAdapter getBlobExportAdapter(Connection connection) {
		return new BlobExportAdapterImpl(connection, BlobType.LIBRARY_OBJECT);
	}

}
