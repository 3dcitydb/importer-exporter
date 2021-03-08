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
package org.citydb.database.adapter.oracle;

import oracle.jdbc.OracleTypes;
import oracle.spatial.geometry.JGeometry;
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

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;

public class UtilAdapter extends AbstractUtilAdapter {
    private final DatabaseSrs WGS843D_SRS = new DatabaseSrs(4979, "", "", "", DatabaseSrsType.GEOGRAPHIC3D, true);
    private final ConcurrentHashMap<Integer, Integer> srs2DMap;

    protected UtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
        super(databaseAdapter);
        srs2DMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void getCityDBVersion(DatabaseMetaData metaData, String schema, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select * from table(" + schema + "." +
                     databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_util.citydb_version") + ")")) {
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
        StringBuilder query = new StringBuilder("select * from table(").append(schema).append(".")
                .append(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_util.db_metadata"));
        boolean requiresSchema = metaData.getCityDBVersion().compareTo(4, 0, 0) < 0;
        query.append(requiresSchema ? "())" : "(?))");

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
					srs.setWkText(fixWKT(rs.getString("WKTEXT")));
					srs.setSupported(true);

					metaData.setVersioning(Versioning.fromValue(rs.getString("VERSIONING")));
				} else
					throw new SQLException("Failed to retrieve metadata information from database.");
			} catch (SQLException e) {
				throw new SQLException("No 3DCityDB instance found in database schema '" + schema + "'.", e);
			}
		}
	}


    @Override
    protected void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("select ref.coord_ref_sys_name, ref.coord_ref_sys_kind, " +
                     "nvl(srs.wktext3d, srs.wktext) " +
                     "from sdo_coord_ref_sys ref, cs_srs srs where srs.srid=ref.srid and ref.srid = " +
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
            interruptibleCallableStatement = connection.prepareCall("{call " +
                    databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.change_schema_srid") +
                    "(?, ?, ?)}");

            interruptibleCallableStatement.setInt(1, srs.getSrid());
            interruptibleCallableStatement.setString(2, srs.getGMLSrsName());
            interruptibleCallableStatement.setInt(3, doTransform ? 1 : 0);
            interruptibleCallableStatement.execute();
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
    }

    @Override
    protected String[] createDatabaseReport(String schema, Connection connection) throws SQLException {
        try {
            interruptibleCallableStatement = connection.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_stat.table_contents") + "(?)}");
            interruptibleCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, schema + ".STRARRAY");
            interruptibleCallableStatement.setString(2, schema);
            interruptibleCallableStatement.executeUpdate();

            Array result = interruptibleCallableStatement.getArray(1);
            return (String[]) result.getArray();
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

        return null;
    }

    @Override
    public BoundingBox createBoundingBox(String schema, long objectId, boolean onlyIfNull, Connection connection) throws SQLException {
        BoundingBox bbox = null;

        try (PreparedStatement pStmt = connection.prepareStatement("SELECT envelope FROM " + schema + ".cityobject WHERE id = ?")) {
            pStmt.setLong(1, objectId);

            try (ResultSet rs = pStmt.executeQuery()) {
                if (rs.next()) {
                    Object geomObject = rs.getObject(1);

                    if (rs.wasNull() || !onlyIfNull) {
                        try (CallableStatement cStmt = connection.prepareCall("{? = call " +
                                databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_envelope.get_envelope_cityobject") + "(?,1)}")) {
                            cStmt.registerOutParameter(1, databaseAdapter.getGeometryConverter().getNullGeometryType(), databaseAdapter.getGeometryConverter().getNullGeometryTypeName());
                            cStmt.setObject(2, objectId, Types.INTEGER);
                            cStmt.executeUpdate();
                            geomObject = cStmt.getObject(1);
                        }
                    }

                    if (geomObject instanceof Struct) {
                        JGeometry jGeom = JGeometry.loadJS((Struct) geomObject);
                        double[] points = jGeom.getOrdinatesArray();

                        double xMin = points[0];
                        double yMin = points[1];
                        double xMax = points[6];
                        double yMax = points[7];

                        bbox = new BoundingBox(new Position(xMin, yMin), new Position(xMax, yMax));
                        bbox.setSrs(databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid());
                    }
                }
            }
        }

        return bbox;
    }

    @Override
    protected IndexStatusInfo manageIndexes(String operation, IndexType type, String schema, Connection connection) throws SQLException {
        try {
            String call = "{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName(operation) + "(?)}";
            interruptibleCallableStatement = connection.prepareCall(call);
            interruptibleCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, schema + ".STRARRAY");
            interruptibleCallableStatement.setString(2, schema);
            interruptibleCallableStatement.executeUpdate();

            Array result = interruptibleCallableStatement.getArray(1);
            return IndexStatusInfo.createFromDatabaseQuery((String[]) result.getArray(), type);
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

        return null;
    }

    @Override
    protected boolean updateTableStats(IndexType type, String schema, Connection connection) throws SQLException {
        return false;
    }

    @Override
    protected GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs, Connection connection) throws SQLException {
        // get source srs
        DatabaseSrs sourceSrs = srsInfoMap.get(geometry.getSrid());
        if (sourceSrs == null) {
            sourceSrs = DatabaseSrs.createDefaultSrs();
            sourceSrs.setSrid(geometry.getSrid());
            getSrsInfo(sourceSrs);
        }

        // get target srid
        int targetSrid = targetSrs.getSrid();

        // change srids if required
        if (sourceSrs.is3D() && !targetSrs.is3D()) {
            geometry.changeSrid(get2DSrid(sourceSrs, connection));
        } else if (!sourceSrs.is3D() && targetSrs.is3D()) {
            targetSrid = get2DSrid(targetSrs, connection);
        }

        Object unconverted = databaseAdapter.getGeometryConverter().getDatabaseObject(geometry, connection);
        if (unconverted != null) {
            try (PreparedStatement psQuery = connection.prepareStatement(
                    "select SDO_CS.TRANSFORM(?, " + targetSrid + ") from dual")) {
                psQuery.setObject(1, unconverted);

                try (ResultSet rs = psQuery.executeQuery()) {
                    if (rs.next()) {
                        Object converted = rs.getObject(1);
                        if (!rs.wasNull()) {
                            return databaseAdapter.getGeometryConverter().getGeometry(converted);
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException {
        if (!srs.is3D())
            return srs.getSrid();

        Integer srid = srs2DMap.get(srs.getSrid());
        if (srid != null)
            return srid;

        String query = srs.getType() == DatabaseSrsType.GEOGRAPHIC3D ?
                "select min(crs2d.srid) from sdo_coord_ref_sys crs3d, sdo_coord_ref_sys crs2d where crs3d.srid = "
                        + srs.getSrid() + " and crs2d.coord_ref_sys_kind = 'GEOGRAPHIC2D' and crs3d.datum_id = crs2d.datum_id" :
                "select cmpd_horiz_srid from sdo_coord_ref_sys where srid = " + srs.getSrid();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int result = 0;
            if (rs.next())
                result = rs.getInt(1);

            if (result == 0)
                throw new SQLException("Failed to discover 2D equivalent for the 3D SRID " + srs.getSrid() + '.');

            // put 2d srid on internal map
            srs2DMap.put(srs.getSrid(), result);

            return result;
        }
    }

    private String fixWKT(String wkText) {
        if (wkText.contains("\"Cassini\""))
            wkText = wkText.replaceAll("\"Cassini\"", "\"Cassini-Soldner\"");

        return wkText;
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
                     " where cityobject_id is null and rownum = 1")) {
            return rs.next();
        }
    }

    @Override
    public int cleanupGlobalAppearances(String schema, Connection connection) throws SQLException {
        try {
            String call = "{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_delete.cleanup_appearances") + "()";
            interruptibleCallableStatement = connection.prepareCall(call);
            interruptibleCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, schema + ".ID_ARRAY");
            interruptibleCallableStatement.execute();

            Array result = interruptibleCallableStatement.getArray(1);
            return ((Object[]) result.getArray()).length;
        } catch (SQLException e) {
            connection.rollback();
            if (!isInterrupted)
                throw e;
        } finally {
            if (interruptibleCallableStatement != null) {
                interruptibleCallableStatement.close();
                interruptibleCallableStatement = null;
            }

            isInterrupted = false;
        }

        return 0;
    }

    private DatabaseSrsType getSrsType(String srsType) {
        if ("PROJECTED".equals(srsType))
            return DatabaseSrsType.PROJECTED;
        else if ("GEOGRAPHIC2D".equals(srsType))
            return DatabaseSrsType.GEOGRAPHIC2D;
        else if ("GEOCENTRIC".equals(srsType))
            return DatabaseSrsType.GEOCENTRIC;
        else if ("VERTICAL".equals(srsType))
            return DatabaseSrsType.VERTICAL;
        else if ("ENGINEERING".equals(srsType))
            return DatabaseSrsType.ENGINEERING;
        else if ("COMPOUND".equals(srsType))
            return DatabaseSrsType.COMPOUND;
        else if ("GEOGENTRIC".equals(srsType))
            return DatabaseSrsType.GEOGENTRIC;
        else if ("GEOGRAPHIC3D".equals(srsType))
            return DatabaseSrsType.GEOGRAPHIC3D;
        else
            return DatabaseSrsType.UNKNOWN;
    }

}
