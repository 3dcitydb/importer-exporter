/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.database.adapter;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.database.connection.ADEMetadata;
import org.citydb.database.connection.DatabaseMetaData;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SchemaMappingException;
import org.citydb.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.database.schema.util.SchemaMappingUtil;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.xml.bind.JAXBException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractUtilAdapter {
    protected final AbstractDatabaseAdapter databaseAdapter;
    protected final ConcurrentHashMap<Integer, DatabaseSrs> srsInfoMap;
    private final ConcurrentHashMap<Integer, CoordinateReferenceSystem> srsDefMap;

    protected CallableStatement interruptableCallableStatement;
    protected PreparedStatement interruptablePreparedStatement;
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
    protected abstract BoundingBox calcBoundingBox(String schema, List<Integer> classIds, Connection connection) throws SQLException;
    protected abstract BoundingBox createBoundingBoxes(List<Integer> classIds, boolean onlyIfNull, Connection connection) throws SQLException;
    @Deprecated protected abstract BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException;
    protected abstract GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs, Connection connection) throws SQLException;
    protected abstract int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException;
    protected abstract IndexStatusInfo manageIndexes(String operation, IndexType type, String schema, Connection connection) throws SQLException;
    protected abstract boolean updateTableStats(IndexType type, String schema, Connection connection) throws SQLException;
    protected abstract boolean containsGlobalAppearances(Connection connection) throws SQLException;
    protected abstract int cleanupGlobalAppearances(String schema, Connection connection) throws SQLException;
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
                try (PreparedStatement ps  = conn.prepareStatement("update " +
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

    public String[] createDatabaseReport(Workspace workspace) throws SQLException {
        String schema = databaseAdapter.getConnectionDetails().getSchema();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            if (databaseAdapter.hasVersioningSupport())
                databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

            return createDatabaseReport(schema, conn);
        }
    }

    public BoundingBox calcBoundingBox(Workspace workspace, List<Integer> objectClassIds) throws SQLException {
        String schema = databaseAdapter.getConnectionDetails().getSchema();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            if (databaseAdapter.hasVersioningSupport())
                databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

            return calcBoundingBox(schema, objectClassIds, conn);
        }
    }

    public BoundingBox createBoundingBoxes(Workspace workspace, List<Integer> objectClassIds, boolean onlyIfNull) throws SQLException {
        BoundingBox bbox = null;

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            conn.setAutoCommit(false);
            if (databaseAdapter.hasVersioningSupport())
                databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

            try {
                bbox = createBoundingBoxes(objectClassIds, onlyIfNull, conn);
                conn.commit();
                return bbox;
            } catch (SQLException e) {
                conn.rollback();
                if (!isInterrupted)
                    throw e;
            }
        }

        return bbox;
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
            interruptableCallableStatement = conn.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_idx.index_status") + "(?, ?, ?)}");

            interruptableCallableStatement.setString(2, tableName);
            interruptableCallableStatement.setString(3, columnName);
            interruptableCallableStatement.setString(4, schema);
            interruptableCallableStatement.registerOutParameter(1, Types.VARCHAR);
            interruptableCallableStatement.executeUpdate();

            isIndexed = interruptableCallableStatement.getString(1).equals("VALID");
        } catch (SQLException e) {
            if (!isInterrupted)
                throw e;
        } finally {
            if (interruptableCallableStatement != null) {
                interruptableCallableStatement.close();
                interruptableCallableStatement = null;
            }

            isInterrupted = false;
        }

        return isIndexed;
    }

    @Deprecated
    public BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return transformBoundingBox(bbox, sourceSrs, targetSrs, conn);
        }
    }

    public GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return transform(geometry, targetSrs, conn);
        }
    }

    public int get2DSrid(DatabaseSrs srs) throws SQLException {
        if (!srs.is3D())
            return srs.getSrid();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            return get2DSrid(srs, conn);
        }
    }

    public List<String> getAppearanceThemeList(Workspace workspace) throws SQLException {
        final String THEME_UNKNOWN = "<unknown>";
        ArrayList<String> appearanceThemes = new ArrayList<>();

        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            if (databaseAdapter.hasVersioningSupport())
                databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("select distinct theme from " +
                         databaseAdapter.getConnectionDetails().getSchema() +
                         ".appearance order by theme")) {
                while (rs.next()) {
                    String thema = rs.getString(1);
                    if (thema != null)
                        appearanceThemes.add(rs.getString(1));
                    else
                        appearanceThemes.add(THEME_UNKNOWN);
                }
            }

            return appearanceThemes;
        }
    }

    public boolean containsGlobalAppearances(Workspace workspace) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            if (databaseAdapter.hasVersioningSupport())
                databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

            return containsGlobalAppearances(conn);
        }
    }

    public int cleanupGlobalAppearances(Workspace workspace, String schema) throws SQLException {
        try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
            if (databaseAdapter.hasVersioningSupport())
                databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

            return cleanupGlobalAppearances(schema, conn);
        }
    }

    public void interruptDatabaseOperation() {
        isInterrupted = true;

        try {
            if (interruptableCallableStatement != null)
                interruptableCallableStatement.cancel();
        } catch (SQLException e) {
            //
        }

        try {
            if (interruptablePreparedStatement != null)
                interruptablePreparedStatement.cancel();
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
