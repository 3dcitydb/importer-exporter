package de.tub.citydb.database.adapter.oracle;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import oracle.jdbc.OracleTypes;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.database.DatabaseSrsType;
import de.tub.citydb.api.geometry.BoundingBox;
import de.tub.citydb.api.geometry.BoundingBoxCorner;
import de.tub.citydb.database.DatabaseMetaDataImpl;
import de.tub.citydb.database.IndexStatusInfo;
import de.tub.citydb.database.DatabaseMetaDataImpl.Versioning;
import de.tub.citydb.database.IndexStatusInfo.IndexType;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;
import de.tub.citydb.database.adapter.AbstractUtilAdapter;
import de.tub.citydb.util.Util;

public class UtilsAdapter extends AbstractUtilAdapter {

	protected UtilsAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}

	@Override
	protected void getDatabaseMetaData(DatabaseMetaDataImpl metaData, Connection connection) throws SQLException {
		Statement statement = null;
		ResultSet rs = null;

		try {
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from table(" + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("geodb_util.db_metadata") + ")");
			if (rs.next()) {
				DatabaseSrs srs = metaData.getReferenceSystem();
				srs.setSrid(rs.getInt("SRID"));
				srs.setGMLSrsName(rs.getString("GML_SRS_NAME"));
				srs.setDatabaseSrsName(rs.getString("COORD_REF_SYS_NAME"));
				srs.setType(getSrsType(rs.getString("COORD_REF_SYS_KIND")));
				srs.setSupported(true);				

				metaData.setVersioning(Versioning.valueOf(rs.getString("VERSIONING")));
			} else
				throw new SQLException("Failed to retrieve metadata information from database.");
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw e;
				}

				rs = null;
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					throw e;
				}

				statement = null;
			}
		}
	}

	@Override
	protected void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException {
		Statement statement = null;
		ResultSet rs = null;

		try {
			statement = connection.createStatement();
			rs = statement.executeQuery("select coord_ref_sys_name, coord_ref_sys_kind from sdo_coord_ref_sys where srid = " + srs.getSrid());

			if (rs.next()) {
				srs.setSupported(true);
				srs.setDatabaseSrsName(rs.getString(1));
				srs.setType(getSrsType(rs.getString(2)));
			} else {
				DatabaseSrs tmp = DatabaseSrs.createDefaultSrs();
				srs.setDatabaseSrsName(tmp.getDatabaseSrsName());
				srs.setType(tmp.getType());
				srs.setSupported(false);
			}

		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw e;
				}

				rs = null;
			}

			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					throw e;
				}

				statement = null;
			}
		}
	}

	@Override
	protected String[] createDatabaseReport(Connection connection) throws SQLException {
		try {
			interruptableCallableStatement = connection.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("geodb_stat.table_contents") + "()}");
			interruptableCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, "STRARRAY");
			interruptableCallableStatement.executeUpdate();

			Array result = interruptableCallableStatement.getArray(1);
			return (String[])result.getArray();
		} catch (SQLException e) {
			if (!isInterrupted)
				throw e;
		} finally {
			if (interruptableCallableStatement != null) {
				try {
					interruptableCallableStatement.close();
				} catch (SQLException e) {
					throw e;
				}

				interruptableCallableStatement = null;
			}

			isInterrupted = false;
		}

		return null;
	}

	@Override
	protected BoundingBox calcBoundingBox(List<Integer> classIds, Connection connection) throws SQLException {
		BoundingBox bbox = null;
		ResultSet rs = null;

		try {		
			String query = "select sdo_aggr_mbr(geodb_util.to_2d(ENVELOPE, (select srid from database_srs))) from CITYOBJECT where ENVELOPE is not NULL";
			if (!classIds.isEmpty()) 
				query += " and CLASS_ID in (" + Util.collection2string(classIds, ", ") +") ";

			BoundingBoxCorner lowerCorner = new BoundingBoxCorner(Double.MAX_VALUE);
			BoundingBoxCorner upperCorner = new BoundingBoxCorner(-Double.MAX_VALUE);

			interruptableStatement = connection.createStatement();
			rs = interruptableStatement.executeQuery(query);

			if (rs.next()) {
				STRUCT struct = (STRUCT)rs.getObject(1);
				if (!rs.wasNull() && struct != null) {
					JGeometry jGeom = JGeometry.load(struct);
					int dim = jGeom.getDimensions();	
					if (dim == 2 || dim == 3) {
						double[] points = jGeom.getOrdinatesArray();
						double xmin, ymin, xmax, ymax;
						xmin = ymin = Double.MAX_VALUE;
						xmax = ymax = -Double.MAX_VALUE;

						if (dim == 2) {
							xmin = points[0];
							ymin = points[1];
							xmax = points[2];
							ymax = points[3];
						} else if (dim == 3) {
							xmin = points[0];
							ymin = points[1];
							xmax = points[3];
							ymax = points[4];
						}

						lowerCorner.setX(xmin);
						lowerCorner.setY(ymin);
						upperCorner.setX(xmax);
						upperCorner.setY(ymax);	
					}		
				}
			}

			if (!isInterrupted)
				bbox = new BoundingBox(lowerCorner, upperCorner);

		} catch (SQLException e) {
			if (!isInterrupted)
				throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw e;
				}

				rs = null;
			}

			if (interruptableStatement != null) {
				try {
					interruptableStatement.close();
				} catch (SQLException e) {
					throw e;
				}

				interruptableStatement = null;
			}

			isInterrupted = false;
		}

		return bbox;
	}

	@Override
	protected IndexStatusInfo manageIndexes(String operation, IndexType type, Connection connection) throws SQLException {
		try {
			String call = "{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName(operation) + "()}";
			interruptableCallableStatement = connection.prepareCall(call);
			interruptableCallableStatement.registerOutParameter(1, OracleTypes.ARRAY, "STRARRAY");
			interruptableCallableStatement.executeUpdate();

			Array result = interruptableCallableStatement.getArray(1);
			return IndexStatusInfo.createFromDatabaseQuery((String[])result.getArray(), type);
		} catch (SQLException e) {
			if (!isInterrupted)
				throw e;
		} finally {
			if (interruptableCallableStatement != null) {
				try {
					interruptableCallableStatement.close();
				} catch (SQLException e) {
					throw e;
				}

				interruptableCallableStatement = null;
			}

			isInterrupted = false;
		}

		return null;
	}

	@Override
	protected BoundingBox transformBBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException {
		BoundingBox result = new BoundingBox(bbox);
		PreparedStatement psQuery = null;
		ResultSet rs = null;

		try {
			int sourceSrid = get2DSrid(sourceSrs, connection);
			int targetSrid = get2DSrid(targetSrs, connection);

			StringBuilder query = new StringBuilder()
			.append("select SDO_CS.TRANSFORM(MDSYS.SDO_GEOMETRY(2003, ").append(sourceSrid)
			.append(", NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 1), ")
			.append("MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), ").append(targetSrid).append(") from dual");

			psQuery = connection.prepareStatement(query.toString());
			psQuery.setDouble(1, bbox.getLowerLeftCorner().getX());
			psQuery.setDouble(2, bbox.getLowerLeftCorner().getY());
			psQuery.setDouble(3, bbox.getUpperRightCorner().getX());
			psQuery.setDouble(4, bbox.getUpperRightCorner().getY());

			rs = psQuery.executeQuery();
			if (rs.next()) {
				STRUCT struct = (STRUCT)rs.getObject(1); 
				if (!rs.wasNull() && struct != null) {
					JGeometry geom = JGeometry.load(struct);
					double[] ordinatesArray = geom.getOrdinatesArray();

					result.getLowerLeftCorner().setX(ordinatesArray[0]);
					result.getLowerLeftCorner().setY(ordinatesArray[1]);
					result.getUpperRightCorner().setX(ordinatesArray[2]);
					result.getUpperRightCorner().setY(ordinatesArray[3]);
					result.setSrs(targetSrs);
				}
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw e;
				}

				rs = null;
			}

			if (psQuery != null) {
				try {
					psQuery.close();
				} catch (SQLException e) {
					throw e;
				}

				psQuery = null;
			}
		}

		return result;
	}

	@Override
	protected int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException {
		if (!srs.is3D())
			return srs.getSrid();

		ResultSet rs = null;
		Statement stmt = null;

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(srs.getType() == DatabaseSrsType.GEOGRAPHIC3D ? 
					"select min(crs2d.srid) from sdo_coord_ref_sys crs3d, sdo_coord_ref_sys crs2d where crs3d.srid = "
					+ srs.getSrid() + " and crs2d.coord_ref_sys_kind = 'GEOGRAPHIC2D' and crs3d.datum_id = crs2d.datum_id" :
						"select cmpd_horiz_srid from sdo_coord_ref_sys where srid = " + srs.getSrid());

			return rs.next() ? rs.getInt(1) : -1;

		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw e;
				}

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw e;
				}

				stmt = null;
			}
		}
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
