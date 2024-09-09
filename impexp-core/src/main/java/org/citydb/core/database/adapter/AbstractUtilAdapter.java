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
package org.citydb.core.database.adapter;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.visExporter.VisExportConfig;
import org.citydb.core.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.core.database.connection.ADEMetadata;
import org.citydb.core.database.connection.DatabaseMetaData;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.mapping.SchemaMappingException;
import org.citydb.core.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.core.database.schema.util.SchemaMappingUtil;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.sql.BuildProperties;
import org.citydb.core.query.builder.sql.SQLQueryBuilder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;

import javax.xml.bind.JAXBException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractUtilAdapter {
    protected final AbstractDatabaseAdapter databaseAdapter;
    protected final ConcurrentHashMap<Integer, DatabaseSrs> srsInfoMap;
    private final ConcurrentHashMap<Integer, CoordinateReferenceSystem> srsDefMap;

    protected CallableStatement interruptibleCallableStatement;
    protected PreparedStatement interruptiblePreparedStatement;
    protected volatile boolean isInterrupted;

    protected AbstractUtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        srsInfoMap = new ConcurrentHashMap<>();
        srsDefMap = new ConcurrentHashMap<>();
    }

    protected abstract void getCityDBVersion(DatabaseMetaData metaData, String schema, Connection connection) throws SQLException;

    protected abstract void getDatabaseMetaData(DatabaseMetaData metaData, String schema, Connection connection) throws SQLException;

    protected abstract void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException;

    protected abstract void changeSrs(DatabaseSrs srs, boolean doTransform, String schema, Connection connection) throws SQLException;

    protected abstract String[] createDatabaseReport(String schema, Connection connection) throws SQLException;

    protected abstract GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs, Connection connection) throws SQLException;

    protected abstract int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException;

    protected abstract IndexStatusInfo manageIndexes(String operation, IndexType type, String schema, Connection connection) throws SQLException;

    protected abstract boolean updateTableStats(IndexType type, String schema, Connection connection) throws SQLException;

    protected abstract boolean containsGlobalAppearances(Connection connection) throws SQLException;

    public abstract int cleanupGlobalAppearances(String schema, Connection connection) throws SQLException;

    public abstract BoundingBox createBoundingBox(String schema, long objectId, boolean onlyIfNull, Connection connection) throws SQLException;

    public abstract DatabaseSrs getWGS843D();

    public DatabaseMetaData getDatabaseInfo(String schema) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            // get vendor specific meta data
            java.sql.DatabaseMetaData vendorMetaData = conn.getMetaData();

            // get 3dcitydb specific meta data
            DatabaseMetaData metaData = new DatabaseMetaData(databaseAdapter.getConnectionDetails());
            getCityDBVersion(metaData, schema, conn);
            getDatabaseMetaData(metaData, schema, conn);
            metaData.setDatabaseProductName(vendorMetaData.getDatabaseProductName());
            metaData.setDatabaseProductVersion(vendorMetaData.getDatabaseProductVersion());
            metaData.setDatabaseMajorVersion(vendorMetaData.getDatabaseMajorVersion());
            metaData.setDatabaseMinorVersion(vendorMetaData.getDatabaseMinorVersion());

            // put database srs info on internal map
            srsInfoMap.put(metaData.getReferenceSystem().getSrid(), metaData.getReferenceSystem());

            return metaData;
        }
    }

    public void getSrsInfo(DatabaseSrs srs) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            getSrsInfo(srs, conn);

            // put database srs info on internal map
            srsInfoMap.put(srs.getSrid(), srs);
        }
    }

    public void changeSrs(DatabaseSrs srs, boolean doTransform, String schema) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            if (srs.getSrid() != databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid()) {
                changeSrs(srs, doTransform, schema, conn);
            } else {
                try (PreparedStatement ps = conn.prepareStatement("update " +
                        databaseAdapter.getConnectionDetails().getSchema() + ".database_srs set gml_srs_name = ?")) {
                    ps.setString(1, srs.getGMLSrsName());
                    ps.execute();
                }
            }
        }
    }

    public List<ADEMetadata> getADEInfo() {
        ArrayList<ADEMetadata> ades = new ArrayList<>();

        try (Connection conn = databaseAdapter.connectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select adeid, name, description, version, db_prefix from " +
                     databaseAdapter.getConnectionDetails().getSchema() + ".ade")) {
            while (rs.next()) {
                ADEMetadata ade = new ADEMetadata();
                ade.setADEId(rs.getString(1));
                ade.setName(rs.getString(2));
                ade.setDescription(rs.getString(3));
                ade.setVersion(rs.getString(4));
                ade.setDBPrefix(rs.getString(5));
                ades.add(ade);
            }
        } catch (SQLException e) {
            // nothing to do
        }

        return ades;
    }

    public SchemaMapping getADESchemaMapping(String adeId, SchemaMapping schemaMapping) throws SQLException, JAXBException, SchemaMappingException, SchemaMappingValidationException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select xml_schemamapping_file from " +
                     databaseAdapter.getConnectionDetails().getSchema() + ".ade where adeid = '" + adeId + "'")) {
            return rs.next() ? SchemaMappingUtil.getInstance().unmarshal(schemaMapping, rs.getString(1)) : null;
        }
    }

    public String[] createDatabaseReport() throws SQLException {
        String schema = databaseAdapter.getConnectionDetails().getSchema();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return createDatabaseReport(schema, conn);
        }
    }

    public BoundingBox calcBoundingBox(Query query, SchemaMapping schemaMapping) throws SQLException, QueryBuildException {
        Position lowerCorner = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
        Position upperCorner = new Position(-Double.MAX_VALUE, -Double.MAX_VALUE);
        int srid = databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid();
        BoundingBox bbox = null;

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            SQLQueryBuilder builder = new SQLQueryBuilder(
                    schemaMapping,
                    databaseAdapter,
                    BuildProperties.defaults().addProjectionColumn(MappingConstants.ENVELOPE));

            Table table = new Table(builder.buildQuery(query));
            Select select = new Select().addProjection(databaseAdapter.getSQLAdapter()
                    .getAggregateExtentFunction(table.getColumn(MappingConstants.ENVELOPE)));

            try {
                interruptiblePreparedStatement = databaseAdapter.getSQLAdapter().prepareStatement(select, conn);
                try (ResultSet rs = interruptiblePreparedStatement.executeQuery()) {
                    if (rs.next()) {
                        Object extentObj = rs.getObject(1);
                        if (!rs.wasNull()) {
                            GeometryObject extent = databaseAdapter.getGeometryConverter().getEnvelope(extentObj);

                            DatabaseSrs targetSrs = query.getTargetSrs();
                            if (targetSrs != null
                                    && targetSrs.isSupported()
                                    && extent.getSrid() != targetSrs.getSrid()) {
                                try {
                                    extent = transform(extent, targetSrs).toEnvelope();
                                } catch (SQLException e) {
                                    //
                                }
                            }

                            srid = extent.getSrid();
                            double[] coordinates = extent.getCoordinates(0);
                            lowerCorner.setX(coordinates[0]);
                            lowerCorner.setY(coordinates[1]);
                            upperCorner.setX(coordinates[3]);
                            upperCorner.setY(coordinates[4]);
                        }
                    }

                    if (!isInterrupted) {
                        bbox = new BoundingBox(lowerCorner, upperCorner);
                        bbox.setSrs(srid);
                    }
                }
            } catch (SQLException e) {
                if (!isInterrupted)
                    throw e;
            } finally {
                if (interruptiblePreparedStatement != null) {
                    interruptiblePreparedStatement.close();
                    interruptiblePreparedStatement = null;
                }

                isInterrupted = false;
            }
        }

        return bbox;
    }

    public BoundingBox createBoundingBox(String schema, long objectId, boolean onlyIfNull) throws SQLException {
        try (Connection connection = databaseAdapter.connectionPool.getConnection()) {
            return createBoundingBox(schema, objectId, onlyIfNull, connection);
        }
    }

    public IndexStatusInfo dropSpatialIndexes() throws SQLException {
        return dropIndexes(IndexType.SPATIAL);
    }

    public IndexStatusInfo dropNormalIndexes() throws SQLException {
        return dropIndexes(IndexType.NORMAL);
    }

    public IndexStatusInfo createSpatialIndexes() throws SQLException {
        return createIndexes(IndexType.SPATIAL);
    }

    public IndexStatusInfo createNormalIndexes() throws SQLException {
        return createIndexes(IndexType.NORMAL);
    }

    public IndexStatusInfo getStatusSpatialIndexes() throws SQLException {
        return getIndexStatus(IndexType.SPATIAL);
    }

    public IndexStatusInfo getStatusNormalIndexes() throws SQLException {
        return getIndexStatus(IndexType.NORMAL);
    }

    public IndexStatusInfo getIndexStatus(IndexType type) throws SQLException {
        String operation = type == IndexType.SPATIAL ? "citydb_idx.status_spatial_indexes" : "citydb_idx.status_normal_indexes";
        return manageIndexes(operation, type);
    }

    private IndexStatusInfo createIndexes(IndexType type) throws SQLException {
        String operation = type == IndexType.SPATIAL ? "citydb_idx.create_spatial_indexes" : "citydb_idx.create_normal_indexes";
        return manageIndexes(operation, type);
    }

    private IndexStatusInfo dropIndexes(IndexType type) throws SQLException {
        String operation = type == IndexType.SPATIAL ? "citydb_idx.drop_spatial_indexes" : "citydb_idx.drop_normal_indexes";
        return manageIndexes(operation, type);
    }

    private IndexStatusInfo manageIndexes(String operation, IndexType type) throws SQLException {
        String schema = databaseAdapter.getConnectionDetails().getSchema();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return manageIndexes(operation, type, schema, conn);
        }
    }

    public boolean updateTableStatsSpatialColumns() throws SQLException {
        return updateTableStats(IndexType.SPATIAL);
    }

    public boolean updateTableStatsNormalColumns() throws SQLException {
        return updateTableStats(IndexType.NORMAL);
    }

    private boolean updateTableStats(IndexType type) throws SQLException {
        String schema = databaseAdapter.getConnectionDetails().getSchema();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return updateTableStats(type, schema, conn);
        }
    }

    public boolean isIndexEnabled(String tableName, String columnName) throws SQLException {
        String schema = databaseAdapter.getConnectionDetails().getSchema();
        boolean isIndexed = false;

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            interruptibleCallableStatement = conn.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_idx.index_status") + "(?, ?, ?)}");

            interruptibleCallableStatement.setString(2, tableName);
            interruptibleCallableStatement.setString(3, columnName);
            interruptibleCallableStatement.setString(4, schema);
            interruptibleCallableStatement.registerOutParameter(1, Types.VARCHAR);
            interruptibleCallableStatement.executeUpdate();

            isIndexed = interruptibleCallableStatement.getString(1).equals("VALID");
        } catch (SQLException e) {
            if (!isInterrupted)
                throw e;
        } finally {
            if (interruptibleCallableStatement != null) {
                interruptibleCallableStatement.close();
                interruptibleCallableStatement = null;
            }

            isInterrupted = false;
        }

        return isIndexed;
    }

    public BoundingBox transform2D(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs) throws SQLException {
        return transform(bbox, 2, sourceSrs, targetSrs);
    }

    public BoundingBox transform(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs) throws SQLException {
        return transform(bbox, bbox.is3D() ? 3 : 2, sourceSrs, targetSrs);
    }

    public BoundingBox transform(BoundingBox bbox, int dimension, DatabaseSrs sourceSrs, DatabaseSrs targetSrs) throws SQLException {
        GeometryObject geometryObject = GeometryObject.createEnvelope(bbox, dimension, sourceSrs.getSrid());
        GeometryObject transformed = transform(geometryObject, targetSrs).toEnvelope();

        // create new bounding box from transformed envelope
        double[] coordinates = transformed.getCoordinates(0);
        if (dimension == 2) {
            return new BoundingBox(
                    new Position(coordinates[0], coordinates[1]),
                    new Position(coordinates[2], coordinates[3]),
                    targetSrs
            );
        } else {
            return new BoundingBox(
                    new Position(coordinates[0], coordinates[1], coordinates[2]),
                    new Position(coordinates[3], coordinates[4], coordinates[5]),
                    targetSrs
            );
        }
    }

    public GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            GeometryObject transformed = transform(geometry, targetSrs, conn);
            if (transformed == null) {
                throw new SQLException("Failed to transform " + geometry.getGeometryType() + " geometry from " +
                        "source SRID " + geometry.getSrid() + " to target SRID " + targetSrs.getSrid() + ".");
            }

            return transformed;
        }
    }

    public int get2DSrid(DatabaseSrs srs) throws SQLException {
        if (!srs.is3D())
            return srs.getSrid();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return get2DSrid(srs, conn);
        }
    }

    public List<String> getAppearanceThemeList() throws SQLException {
        ArrayList<String> appearanceThemes = new ArrayList<>();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("select distinct theme from " +
                         databaseAdapter.getConnectionDetails().getSchema() +
                         ".appearance order by theme")) {
                while (rs.next()) {
                    String thema = rs.getString(1);
                    if (thema != null)
                        appearanceThemes.add(rs.getString(1));
                    else
                        appearanceThemes.add(VisExportConfig.THEME_NULL);
                }
            }

            return appearanceThemes;
        }
    }

    public boolean containsGlobalAppearances() throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return containsGlobalAppearances(conn);
        }
    }

    public int cleanupGlobalAppearances(String schema) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return cleanupGlobalAppearances(schema, conn);
        }
    }

    public void interruptDatabaseOperation() {
        isInterrupted = true;

        try {
            if (interruptibleCallableStatement != null)
                interruptibleCallableStatement.cancel();
        } catch (SQLException e) {
            //
        }

        try {
            if (interruptiblePreparedStatement != null)
                interruptiblePreparedStatement.cancel();
        } catch (SQLException e) {
            //
        }
    }

    public CoordinateReferenceSystem decodeDatabaseSrs(DatabaseSrs srs) throws FactoryException {
        if (srsDefMap.containsKey(srs.getSrid()))
            return srsDefMap.get(srs.getSrid());

        CoordinateReferenceSystem tmp = null;

        try {
            tmp = CRS.decode("EPSG:" + srs.getSrid());
        } catch (FactoryException e) {
            //
        }

        if (tmp == null) {
            try {
                tmp = CRS.decode(srs.getGMLSrsName());
            } catch (FactoryException e) {
                //
            }
        }

        if (tmp == null) {
            if (srs.getWkText() != null)
                tmp = CRS.parseWKT(srs.getWkText());
            else
                throw new FactoryException("Failed to load SRS information for reference system " + srs.getDescription());
        }

        srsDefMap.putIfAbsent(srs.getSrid(), tmp);
        return tmp;
    }
}
