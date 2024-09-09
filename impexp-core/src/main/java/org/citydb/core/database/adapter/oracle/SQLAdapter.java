/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.core.database.adapter.oracle;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.adapter.*;
import org.citydb.core.query.filter.selection.operator.spatial.SpatialOperatorName;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.expression.StringLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLAdapter extends AbstractSQLAdapter {

    protected SQLAdapter(AbstractDatabaseAdapter databaseAdapter) {
        super(databaseAdapter);
    }

    @Override
    public String getInteger() {
        return "NUMBER";
    }

    @Override
    public String getSmallInt() {
        return "NUMBER";
    }

    @Override
    public String getBigInt() {
        return "NUMBER";
    }

    @Override
    public String getNumeric() {
        return "NUMBER";
    }

    @Override
    public String getNumeric(int precision) {
        return "NUMBER(" + precision + ")";
    }

    @Override
    public String getNumeric(int precision, int scale) {
        return "NUMBER(" + precision + "," + scale + ")";
    }

    @Override
    public String getReal() {
        return "BINARY_FLOAT";
    }

    @Override
    public String getDoublePrecision() {
        return "BINARY_DOUBLE";
    }

    @Override
    public String getCharacter(int nrOfChars) {
        return "CHAR(" + nrOfChars + ")";
    }

    @Override
    public String getCharacterVarying(int nrOfChars) {
        return "VARCHAR2(" + nrOfChars + ")";
    }

    @Override
    public String getPolygon2D() {
        return "MDSYS.SDO_GEOMETRY";
    }

    @Override
    public String getCreateUnloggedTable(String tableName, String columns) {
        return "create table " + tableName + " " + columns + " nologging";
    }

    @Override
    public String getCreateUnloggedTableAsSelect(String tableName, String select) {
        return "create table " + tableName + " nologging as " + select;
    }

    @Override
    public String getNextSequenceValue(String sequence) {
        return databaseAdapter.getConnectionDetails().getSchema() + "." + sequence + ".nextval";
    }

    @Override
    public String getCurrentSequenceValue(String sequence) {
        return databaseAdapter.getConnectionDetails().getSchema() + "." + sequence + ".currval";
    }

    @Override
    public String getNextSequenceValuesQuery(String sequence) {
        boolean requiresSchema = databaseAdapter.getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) < 0;
        StringBuilder query = new StringBuilder("select * from table(")
                .append(resolveDatabaseOperationName("citydb_util.get_seq_values")).append("(");

        if (requiresSchema) {
            query.append("'").append(sequence).append("'").append(",")
                    .append("?,")
                    .append("'").append(databaseAdapter.getConnectionDetails().getSchema()).append("'");
        } else {
            query.append("'").append(databaseAdapter.getConnectionDetails().getSchema()).append(".")
                    .append(sequence).append("'").append(",")
                    .append("?");
        }

        return query.append("))").toString();
    }

    @Override
    public String getUnloggedIndexProperty() {
        return "nologging";
    }

    @Override
    public boolean requiresPseudoTableInSelect() {
        return true;
    }

    @Override
    public String getPseudoTableName() {
        return "dual";
    }

    @Override
    public int getMaximumNumberOfItemsForInOperator() {
        return 1000;
    }

    @Override
    public boolean spatialPredicateRequiresNoIndexHint() {
        return databaseAdapter.getConnectionMetaData().getDatabaseMajorVersion() == 11;
    }

    @Override
    public boolean supportsFetchFirstClause() {
        return databaseAdapter.getConnectionMetaData().getDatabaseMajorVersion() > 11;
    }

    @Override
    public String getHierarchicalGeometryQuery() {
        return "select sg.id, sg.gmlid, sg.parent_id, sg.root_id, sg.is_solid, sg.is_composite, sg.is_triangulated, " +
                "sg.is_xlink, sg.is_reverse, sg.geometry, sg.implicit_geometry, sg.solid_geometry, sg.cityobject_id, " +
                "level from " + databaseAdapter.getConnectionDetails().getSchema() +
                ".surface_geometry sg start with sg.id=? connect by prior sg.id=sg.parent_id";
    }

    @Override
    public BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type) throws SQLException {
        return new BlobImportAdapter(connection, type, databaseAdapter.getConnectionDetails().getSchema());
    }

    @Override
    public BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type) {
        return new BlobExportAdapter(connection, type, databaseAdapter.getConnectionDetails().getSchema());
    }

    @Override
    public PredicateToken getBinarySpatialPredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, boolean negate) {
        PlaceHolder<GeometryObject> geometryLiteral = new PlaceHolder<>(geometry);
        StringLiteral trueLiteral = new StringLiteral("TRUE");

        switch (operator) {
            case BBOX:
            case INTERSECTS:
                return ComparisonFactory.equalTo(new Function("SDO_ANYINTERACT", targetColumn, geometryLiteral), trueLiteral, negate);
            case EQUALS:
                return ComparisonFactory.equalTo(new Function("SDO_EQUALS", targetColumn, geometryLiteral), trueLiteral, negate);
            case DISJOINT:
                return ComparisonFactory.equalTo(new Function("SDO_ANYINTERACT", targetColumn, geometryLiteral), trueLiteral, !negate);
            case TOUCHES:
                return ComparisonFactory.equalTo(new Function("SDO_TOUCH", targetColumn, geometryLiteral), trueLiteral, !negate);
            case WITHIN:
                return ComparisonFactory.equalTo(new Function("SDO_RELATE", targetColumn, geometryLiteral, new StringLiteral("mask=inside+coveredby")), trueLiteral, negate);
            case OVERLAPS:
                return ComparisonFactory.equalTo(new Function("SDO_OVERLAPS", targetColumn, geometryLiteral), trueLiteral, negate);
            case CONTAINS:
                return ComparisonFactory.equalTo(new Function("SDO_RELATE", targetColumn, geometryLiteral, new StringLiteral("mask=contains+covers")), trueLiteral, negate);
            default:
                break;
        }

        return null;
    }

    @Override
    public PredicateToken getDistancePredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, double distance, boolean negate) {
        PlaceHolder<GeometryObject> placeHolder = new PlaceHolder<>(geometry);
        StringLiteral trueLiteral = new StringLiteral("TRUE");
        StringLiteral distanceLiteral = new StringLiteral("distance = " + distance);

        switch (operator) {
            case DWITHIN:
                return ComparisonFactory.equalTo(new Function("SDO_WITHIN_DISTANCE", targetColumn, placeHolder, distanceLiteral), trueLiteral, negate);
            case BEYOND:
                return ComparisonFactory.equalTo(new Function("SDO_WITHIN_DISTANCE", targetColumn, placeHolder, distanceLiteral), trueLiteral, !negate);
            default:
                break;
        }

        return null;
    }

    @Override
    public Function getAggregateExtentFunction(Column envelope) {
        return new Function("sdo_aggr_mbr",
                new Function(databaseAdapter.getConnectionDetails().getSchema() + ".citydb_util.to_2d",
                        envelope, new IntegerLiteral(databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid())));
    }

}
