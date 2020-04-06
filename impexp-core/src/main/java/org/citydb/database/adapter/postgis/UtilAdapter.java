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
package org.citydb.database.adapter.postgis;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.geometry.Position;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.DatabaseSrsType;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractUtilAdapter;
import org.citydb.database.adapter.IndexStatusInfo;
import org.citydb.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.database.connection.DatabaseMetaData;
import org.citydb.database.connection.DatabaseMetaData.Versioning;
import org.citydb.database.version.DatabaseVersion;
import org.citydb.util.Util;
import org.postgis.Geometry;
import org.postgis.PGbox2d;
import org.postgis.PGgeometry;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

public class UtilAdapter extends AbstractUtilAdapter {
    private final DatabaseSrs WGS843D_SRS = new DatabaseSrs(4326, "", "", "", DatabaseSrsType.GEOGRAPHIC2D, true);

    protected UtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
        super(databaseAdapter);
    }

    @Override
    protected void getCityDBVersion(DatabaseMetaData metaData, String schema, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select * from " +
                     databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_util.citydb_version") + "()")) {
            if (rs.next()) {
                String productVersion = rs.getString("VERSION");
                int major = rs.getInt("MAJOR_VERSION");
                int minor = rs.getInt("MINOR_VERSION");
                int revision = rs.getInt("MINOR_REVISION");
                metaData.setCityDBVersion(new DatabaseVersion(major, minor, revision, productVersion));
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve version information from the 3D City Database instance.", e);
        }
    }

    @Override
    protected void getDatabaseMetaData(DatabaseMetaData metaData, String schema, Connection connection) throws SQLException {
        StringBuilder query = new StringBuilder("select * from ").append(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_util.db_metadata"));
        boolean requiresSchema = metaData.getCityDBVersion().compareTo(4, 0, 0) < 0;
        query.append(requiresSchema ? "()" : "(?)");

		try (PreparedStatement psQuery = connection.prepareStatement(query.toString())) {
		    if (!requiresSchema)
		        psQuery.setString(1, databaseAdapter.getConnectionDetails().getSchema());
			
			try (ResultSet rs = psQuery.executeQuery()) {
				if (rs.next()) {
					DatabaseSrs srs = metaData.getReferenceSystem();
					srs.setSrid(rs.getInt("SCHEMA_SRID"));
					srs.setGMLSrsName(rs.getString("SCHEMA_GML_SRS_NAME"));
					srs.setDatabaseSrsName(rs.getString("COORD_REF_SYS_NAME"));
					srs.setType(getSrsType(rs.getString("COORD_REF_SYS_KIND")));
					srs.setWkText(rs.getString("WKTEXT"));
					srs.setSupported(true);

					metaData.setVersioning(Versioning.NOT_SUPPORTED);
				} else
					throw new SQLException("Failed to retrieve metadata information from database.");
			} catch (SQLException e) {
				throw new SQLException("No 3DCityDB instance found in given database schema.", e);
			}
		}
	}


    @Override
    protected void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select split_part(srtext, '\"', 2), split_part(srtext, '[', 1), srtext " +
                     "from spatial_ref_sys where srid = " +
                     srs.getSrid())) {
            if (rs.next()) {
                srs.setSupported(true);
                srs.setDatabaseSrsName(rs.getString(1));
                srs.setType(getSrsType(rs.getString(2)));
                srs.setWkText(rs.getString(3));
            } else {
                DatabaseSrs tmp = DatabaseSrs.createDefaultSrs();
                srs.setDatabaseSrsName(tmp.getDatabaseSrsName());
                srs.setType(tmp.getType());
                srs.setSupported(false);
            }
        }
    }

    @Override
    public void changeSrs(DatabaseSrs srs, boolean doTransform, String schema, Connection connection) throws SQLException {
        try {
            interruptableCallableStatement = connection.prepareCall("{call " +
                    databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.change_schema_srid") +
                    "(?, ?, ?, ?)}");

            interruptableCallableStatement.setInt(1, srs.getSrid());
            interruptableCallableStatement.setString(2, srs.getGMLSrsName());
            interruptableCallableStatement.setInt(3, doTransform ? 1 : 0);
            interruptableCallableStatement.setString(4, schema);
            interruptableCallableStatement.execute();
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
    }

    @Override
    protected String[] createDatabaseReport(String schema, Connection connection) throws SQLException {
        try {
            interruptableCallableStatement = connection.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_stat.table_contents") + "(?)}");
            interruptableCallableStatement.registerOutParameter(1, Types.ARRAY);
            interruptableCallableStatement.setString(2, schema);
            interruptableCallableStatement.executeUpdate();

            Array result = interruptableCallableStatement.getArray(1);
            return (String[]) result.getArray();
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

        return null;
    }

    @Override
    protected BoundingBox calcBoundingBox(String schema, List<Integer> objectClassIds, Connection connection) throws SQLException {
        StringBuilder query = new StringBuilder()
                .append("select ST_Extent(envelope) from ").append(schema)
                .append(".cityobject where envelope is not null");

        if (!objectClassIds.isEmpty())
            query.append(" and OBJECTCLASS_ID in (").append(Util.collection2string(objectClassIds, ", ")).append(") ");

        Position lowerCorner = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
        Position upperCorner = new Position(-Double.MAX_VALUE, -Double.MAX_VALUE);
        BoundingBox bbox = null;

        try {
            interruptablePreparedStatement = connection.prepareStatement(query.toString());

            try (ResultSet rs = interruptablePreparedStatement.executeQuery()) {
                if (rs.next()) {
                    PGbox2d geom = (PGbox2d) rs.getObject(1);
                    if (!rs.wasNull() && geom != null) {
                        lowerCorner.setX(geom.getLLB().x);
                        lowerCorner.setY(geom.getLLB().y);
                        upperCorner.setX(geom.getURT().x);
                        upperCorner.setY(geom.getURT().y);
                    }
                }

                if (!isInterrupted)
                    bbox = new BoundingBox(lowerCorner, upperCorner);
            }
        } catch (SQLException e) {
            if (!isInterrupted)
                throw e;
        } finally {
            if (interruptablePreparedStatement != null) {
                interruptablePreparedStatement.close();
                interruptablePreparedStatement = null;
            }

            isInterrupted = false;
        }

        return bbox;
    }

    @Override
    protected BoundingBox createBoundingBoxes(List<Integer> objectClassIds, boolean onlyIfNull, Connection connection) throws SQLException {
        BoundingBox bbox = null;

        try {
            for (Integer classId : objectClassIds) {
                String call = "{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_envelope.get_envelope_cityobjects") + "(?,1,?)}";
                interruptableCallableStatement = connection.prepareCall(call);
                interruptableCallableStatement.registerOutParameter(1, databaseAdapter.getGeometryConverter().getNullGeometryType());
                interruptableCallableStatement.setInt(2, classId);
                interruptableCallableStatement.setInt(3, onlyIfNull ? 1 : 0);
                interruptableCallableStatement.executeUpdate();

                Position lowerCorner = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
                Position upperCorner = new Position(-Double.MAX_VALUE, -Double.MAX_VALUE);

                Object geomObject = interruptableCallableStatement.getObject(1);
                if (geomObject instanceof PGgeometry) {
                    Geometry geom = ((PGgeometry) geomObject).getGeometry();
                    double xmin, ymin, xmax, ymax;

                    xmin = geom.getPoint(0).x;
                    ymin = geom.getPoint(0).y;
                    xmax = geom.getPoint(2).x;
                    ymax = geom.getPoint(2).y;

                    lowerCorner.setX(xmin);
                    lowerCorner.setY(ymin);
                    upperCorner.setX(xmax);
                    upperCorner.setY(ymax);
                }

                if (!isInterrupted) {
                    if (bbox == null)
                        bbox = new BoundingBox(lowerCorner, upperCorner);
                    else
                        bbox.update(lowerCorner, upperCorner);
                }
            }

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

        return bbox;
    }

    @Override
    protected IndexStatusInfo manageIndexes(String operation, IndexType type, String schema, Connection connection) throws SQLException {
        try {
            String call = "{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName(operation) + "(?)}";
            interruptableCallableStatement = connection.prepareCall(call);
            interruptableCallableStatement.registerOutParameter(1, Types.ARRAY);
            interruptableCallableStatement.setString(2, schema);
            interruptableCallableStatement.executeUpdate();

            Array result = interruptableCallableStatement.getArray(1);
            return IndexStatusInfo.createFromDatabaseQuery((String[]) result.getArray(), type);
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

        return null;
    }

    @Override
    protected boolean updateTableStats(IndexType type, String schema, Connection connection) throws SQLException {
        try (PreparedStatement pStmt = connection.prepareStatement("SELECT (obj).table_name, (obj).attribute_name " +
                "FROM " + schema + ".index_table WHERE (obj).type = ?")) {
            pStmt.setInt(1, type == IndexType.SPATIAL ? 1 : 0);

            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    String attributeName = rs.getString(2);

                    interruptablePreparedStatement = connection.prepareStatement("VACUUM ANALYZE " +
                            schema + "." + tableName + " (" + attributeName + ")");
                    interruptablePreparedStatement.executeUpdate();
                }

                return true;
            }
        } catch (SQLException e) {
            if (!isInterrupted)
                throw e;
        } finally {
            if (interruptablePreparedStatement != null) {
                interruptablePreparedStatement.close();
                interruptablePreparedStatement = null;
            }

            isInterrupted = false;
        }

        return false;
    }

    @Override
    protected BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException {
        BoundingBox result = new BoundingBox(bbox);
        int sourceSrid = sourceSrs.getSrid();
        int targetSrid = targetSrs.getSrid();

        StringBuilder boxGeom = new StringBuilder()
                .append("SRID=").append(sourceSrid).append(";POLYGON((")
                .append(bbox.getLowerCorner().getX()).append(" ").append(bbox.getLowerCorner().getY()).append(",")
                .append(bbox.getLowerCorner().getX()).append(" ").append(bbox.getUpperCorner().getY()).append(",")
                .append(bbox.getUpperCorner().getX()).append(" ").append(bbox.getUpperCorner().getY()).append(",")
                .append(bbox.getUpperCorner().getX()).append(" ").append(bbox.getLowerCorner().getY()).append(",")
                .append(bbox.getLowerCorner().getX()).append(" ").append(bbox.getLowerCorner().getY()).append("))");

        try (PreparedStatement psQuery = connection.prepareStatement("select ST_Transform(ST_GeomFromEWKT(?), " + targetSrid + ')')) {
            psQuery.setString(1, boxGeom.toString());

            try (ResultSet rs = psQuery.executeQuery()) {
                if (rs.next()) {
                    PGgeometry pgGeom = (PGgeometry) rs.getObject(1);
                    if (!rs.wasNull() && pgGeom != null) {
                        Geometry geom = pgGeom.getGeometry();
                        result.getLowerCorner().setX(geom.getPoint(0).x);
                        result.getLowerCorner().setY(geom.getPoint(0).y);
                        result.getUpperCorner().setX(geom.getPoint(2).x);
                        result.getUpperCorner().setY(geom.getPoint(2).y);
                        result.setSrs(targetSrs);
                    }
                }
            }

            return result;
        }
    }

    @Override
    protected GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs, Connection connection) throws SQLException {
        GeometryObject result = null;

        Object unconverted = databaseAdapter.getGeometryConverter().getDatabaseObject(geometry, connection);
        if (unconverted == null)
            return null;

        try (PreparedStatement psQuery = connection.prepareStatement("select ST_Transform(?, " + targetSrs.getSrid() + ')')) {
            psQuery.setObject(1, unconverted);

            try (ResultSet rs = psQuery.executeQuery()) {
                if (rs.next()) {
                    Object converted = rs.getObject(1);
                    if (!rs.wasNull() && converted != null)
                        result = databaseAdapter.getGeometryConverter().getGeometry(converted);
                }

                return result;
            }
        }
    }

    @Override
    protected int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException {
        return srs.getSrid();
    }

    @Override
    public DatabaseSrs getWGS843D() {
        return WGS843D_SRS;
    }

    @Override
    protected boolean containsGlobalAppearances(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select 1 from "
                     + databaseAdapter.getConnectionDetails().getSchema() + ".appearance" +
                     " where cityobject_id is null limit 1")) {
            return rs.next();
        }
    }

    @Override
    protected int cleanupGlobalAppearances(String schema, Connection connection) throws SQLException {
        int deleted = 0;

        try (PreparedStatement pStmt = connection.prepareStatement("SELECT "
                + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_delete.cleanup_appearances")
                + "()")) {
            try (ResultSet rs = pStmt.executeQuery()) {
                while (rs.next())
                    deleted++;
            }
        } catch (SQLException e) {
            if (!isInterrupted)
                throw e;
        } finally {
            if (interruptablePreparedStatement != null) {
                interruptablePreparedStatement.close();
                interruptablePreparedStatement = null;
            }

            isInterrupted = false;
        }

        return deleted;
    }

    private DatabaseSrsType getSrsType(String srsType) {
        if ("PROJCS".equals(srsType))
            return DatabaseSrsType.PROJECTED;
        else if ("GEOGCS".equals(srsType))
            return DatabaseSrsType.GEOGRAPHIC2D;
        else if ("GEOCCS".equals(srsType))
            return DatabaseSrsType.GEOCENTRIC;
        else if ("VERT_CS".equals(srsType))
            return DatabaseSrsType.VERTICAL;
        else if ("LOCAL_CS".equals(srsType))
            return DatabaseSrsType.ENGINEERING;
        else if ("COMPD_CS".equals(srsType))
            return DatabaseSrsType.COMPOUND;
        else if ("GEOGCS3D".equals(srsType))
            return DatabaseSrsType.GEOGRAPHIC3D;
        else
            return DatabaseSrsType.UNKNOWN;
    }

}
