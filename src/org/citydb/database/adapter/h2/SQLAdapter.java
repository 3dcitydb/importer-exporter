/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
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
package org.citydb.database.adapter.h2;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.api.geometry.BoundingBox;
import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobImportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

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
	public int getMaximumNumberOfItemsForInOperator() {
		// not required for cache tables
		return 0;
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
	public BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type) throws SQLException {
		// not required for cache tables
		return null;
	}

	@Override
	public BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type) {
		// not required for cache tables
		return null;
	}

}
