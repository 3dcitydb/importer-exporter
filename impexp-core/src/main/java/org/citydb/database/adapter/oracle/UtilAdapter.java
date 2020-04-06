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
import org.citydb.util.Util;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
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

					metaData.setVersioning(Versioning.valueOf(rs.getString("VERSIONING")));
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
            interruptableCallableStatement = connection.prepareCall("{call " +
                    databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_srs.change_schema_srid") +
                    "(?, ?, ?)}");

            interruptableCallableStatement.setInt(1, srs.getSrid());
            interruptableCallableStatement.setString(2, srs.getGMLSrsName());
            interruptableCallableStatement.setInt(3, doTransform ? 1 : 0);
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
            interruptableCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, schema + ".STRARRAY");
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
                .append("select sdo_aggr_mbr(").append(schema).append(".citydb_util.to_2d(ENVELOPE, (select srid from ")
                .append(schema).append(".database_srs))) from ").append(schema)
                .append(".CITYOBJECT where ENVELOPE is not NULL");

        if (!objectClassIds.isEmpty())
            query.append(" and OBJECTCLASS_ID in (").append(Util.collection2string(objectClassIds, ", ")).append(") ");

        Position lowerCorner = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
        Position upperCorner = new Position(-Double.MAX_VALUE, -Double.MAX_VALUE);
        BoundingBox bbox = null;

        try {
            interruptablePreparedStatement = connection.prepareStatement(query.toString());

            try (ResultSet rs = interruptablePreparedStatement.executeQuery()) {
                if (rs.next()) {
                    Struct struct = (Struct) rs.getObject(1);
                    if (!rs.wasNull() && struct != null) {
                        JGeometry jGeom = JGeometry.loadJS(struct);
                        double[] points = jGeom.getOrdinatesArray();

                        lowerCorner.setX(points[0]);
                        lowerCorner.setY(points[1]);
                        if (points.length >= 4) {
                            upperCorner.setX(points[2]);
                            upperCorner.setY(points[3]);
                        } else {
                            upperCorner.setX(points[0]);
                            upperCorner.setY(points[1]);
                        }
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
                interruptableCallableStatement.registerOutParameter(1, databaseAdapter.getGeometryConverter().getNullGeometryType(), databaseAdapter.getGeometryConverter().getNullGeometryTypeName());
                interruptableCallableStatement.setInt(2, classId);
                interruptableCallableStatement.setInt(3, onlyIfNull ? 1 : 0);
                interruptableCallableStatement.executeUpdate();

                Position lowerCorner = new Position(Double.MAX_VALUE, Double.MAX_VALUE);
                Position upperCorner = new Position(-Double.MAX_VALUE, -Double.MAX_VALUE);

                Object geomObject = interruptableCallableStatement.getObject(1);
                if (geomObject instanceof Struct) {
                    JGeometry jGeom = JGeometry.loadJS((Struct) geomObject);
                    double[] points = jGeom.getOrdinatesArray();
                    double xmin, ymin, xmax, ymax;

                    xmin = points[0];
                    ymin = points[1];
                    xmax = points[6];
                    ymax = points[7];

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
            interruptableCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, schema + ".STRARRAY");
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
        return false;
    }

    @Override
    protected BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException {
        BoundingBox result = new BoundingBox(bbox);
        int sourceSrid = get2DSrid(sourceSrs, connection);
        int targetSrid = get2DSrid(targetSrs, connection);

        try (PreparedStatement psQuery = connection.prepareStatement("select SDO_CS.TRANSFORM(MDSYS.SDO_GEOMETRY(2003, " +
                sourceSrid + ", NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 1), " +
                "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), " + targetSrid + ") from dual")) {
            psQuery.setDouble(1, bbox.getLowerCorner().getX());
            psQuery.setDouble(2, bbox.getLowerCorner().getY());
            psQuery.setDouble(3, bbox.getUpperCorner().getX());
            psQuery.setDouble(4, bbox.getUpperCorner().getY());

            try (ResultSet rs = psQuery.executeQuery()) {
                if (rs.next()) {
                    Struct struct = (Struct) rs.getObject(1);
                    if (!rs.wasNull() && struct != null) {
                        JGeometry geom = JGeometry.loadJS(struct);
                        double[] ordinatesArray = geom.getOrdinatesArray();

                        result.getLowerCorner().setX(ordinatesArray[0]);
                        result.getLowerCorner().setY(ordinatesArray[1]);
                        result.getUpperCorner().setX(ordinatesArray[2]);
                        result.getUpperCorner().setY(ordinatesArray[3]);
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
        if (sourceSrs.is3D() && !targetSrs.is3D())
            geometry.changeSrid(get2DSrid(sourceSrs, connection));
        else if (!sourceSrs.is3D() && targetSrs.is3D())
            targetSrid = get2DSrid(targetSrs, connection);

        Object unconverted = databaseAdapter.getGeometryConverter().getDatabaseObject(geometry, connection);
        if (unconverted == null)
            return null;

        try (PreparedStatement psQuery = connection.prepareStatement("select SDO_CS.TRANSFORM(?, " + targetSrid + ") from dual")) {
            psQuery.setObject(1, unconverted);

            try (ResultSet rs = psQuery.executeQuery()) {
                if (rs.next()) {
                    Object converted = rs.getObject(1);
                    if (!rs.wasNull() && converted != null)
                        result = databaseAdapter.getGeometryConverter().getGeometry(converted);
                }
            }

            return result;
        }
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
    protected int cleanupGlobalAppearances(String schema, Connection connection) throws SQLException {
        try {
            String call = "{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_delete.cleanup_appearances") + "()";
            interruptableCallableStatement = connection.prepareCall(call);
            interruptableCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, schema + ".ID_ARRAY");
            interruptableCallableStatement.execute();

            Array result = interruptableCallableStatement.getArray(1);
            return ((Object[]) result.getArray()).length;
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
