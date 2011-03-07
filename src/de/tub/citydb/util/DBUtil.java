package de.tub.citydb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleCallableStatement;
import oracle.jdbc.driver.OracleTypes;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.BoundingVolume;

import de.tub.citydb.db.DBConnectionPool;

public class DBUtil {
	private static HashMap<String, DBUtil> instanceMap = new HashMap<String, DBUtil>();

	private final DBConnectionPool dbConnectionPool;
	private OracleCallableStatement callableStmt;
	private volatile boolean cancelled = false;

	private DBUtil(DBConnectionPool dbConnectionPool) {
		this.dbConnectionPool = dbConnectionPool;
	}

	public static synchronized DBUtil getInstance(DBConnectionPool dbPool) {
		DBUtil instance = instanceMap.get(dbPool.getCacheName());
		if (instance == null) {
			instance = new DBUtil(dbPool);
			instanceMap.put(dbPool.getCacheName(), instance);
		}	

		return instance;
	}

	public String[] getDatabaseInfo() throws SQLException {
		String[] dbInfo = null;
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = getConnection();
			callableStmt = (OracleCallableStatement)conn.prepareCall("{call geodb_util.db_info(?, ?, ?)}");
			callableStmt.registerOutParameter(1, Types.VARCHAR);
			callableStmt.registerOutParameter(2, Types.VARCHAR);
			callableStmt.registerOutParameter(3, Types.VARCHAR);
			callableStmt.executeUpdate();

			dbInfo = new String[3];
			dbInfo[0] = callableStmt.getString(1);
			dbInfo[1] = callableStmt.getString(2);
			dbInfo[2] = callableStmt.getString(3);

		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				stmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
			}
		}

		return dbInfo;
	}

	public String[] databaseReport(String workspace) throws SQLException {
		String[] report = null;
		Connection conn = null;

		try {
			conn = getConnection();
			dbConnectionPool.changeWorkspace(conn, workspace);		

			callableStmt = (OracleCallableStatement)conn.prepareCall("{? = call geodb_stat.table_contents}");
			callableStmt.registerOutParameter(1, OracleTypes.ARRAY, "STRARRAY");
			callableStmt.executeUpdate();

			ARRAY result = callableStmt.getARRAY(1);
			report = (String[])result.getArray();

		} catch (SQLException sqlEx) {
			if (!cancelled)
				throw sqlEx;
		} finally {
			if (callableStmt != null) {
				try {
					callableStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				callableStmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
			}

			cancelled = false;
		}

		return report;
	}

	private String[] dropIndexes(DB_INDEX_TYPE type) throws SQLException {
		String[] report = null;
		Connection conn = null;

		String call = type == DB_INDEX_TYPE.SPATIAL ? 
				"{? = call geodb_idx.drop_spatial_indexes}" : 
					"{? = call geodb_idx.drop_normal_indexes}";

		try {
			conn = getConnection();
			callableStmt = (OracleCallableStatement)conn.prepareCall(call);
			callableStmt.registerOutParameter(1, OracleTypes.ARRAY, "STRARRAY");
			callableStmt.executeUpdate();

			ARRAY result = callableStmt.getARRAY(1);
			report = (String[])result.getArray();

		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (callableStmt != null) {
				try {
					callableStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				callableStmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
			}
		}

		return report;
	}

	private String[] createIndexes(DB_INDEX_TYPE type) throws SQLException {
		String[] report = null;
		Connection conn = null;

		String call = type == DB_INDEX_TYPE.SPATIAL ? 
				"{? = call geodb_idx.create_spatial_indexes}" : 
					"{? = call geodb_idx.create_normal_indexes}";

		try {
			conn = getConnection();
			callableStmt = (OracleCallableStatement)conn.prepareCall(call);
			callableStmt.registerOutParameter(1, OracleTypes.ARRAY, "STRARRAY");
			callableStmt.executeUpdate();

			ARRAY result = callableStmt.getARRAY(1);
			report = (String[])result.getArray();

		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (callableStmt != null) {
				try {
					callableStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				callableStmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
			}
		}

		return report;
	}

	public boolean isIndexed(String tableName, String columnName) throws SQLException {
		Connection conn = null;
		boolean isIndexed = false;

		try {
			conn = getConnection();
			callableStmt = (OracleCallableStatement)conn.prepareCall("{? = call geodb_idx.index_status(?, ?)}");
			callableStmt.setString(2, tableName);
			callableStmt.setString(3, columnName);
			callableStmt.registerOutParameter(1, Types.VARCHAR);
			callableStmt.executeUpdate();

			isIndexed = callableStmt.getString(1).equals("VALID");

		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (callableStmt != null) {
				try {
					callableStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				callableStmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
			}
		}

		return isIndexed;
	}

	public String[] dropSpatialIndexes() throws SQLException {
		return dropIndexes(DB_INDEX_TYPE.SPATIAL);
	}

	public String[] dropNormalIndexes() throws SQLException {
		return dropIndexes(DB_INDEX_TYPE.NORMAL);
	}

	public String[] createSpatialIndexes() throws SQLException {
		return createIndexes(DB_INDEX_TYPE.SPATIAL);
	}

	public String[] createNormalIndexes() throws SQLException {
		return createIndexes(DB_INDEX_TYPE.NORMAL);
	}

	public String errorMessage(String errorCode) throws SQLException {
		String errorMessage = null;
		Connection conn = null;

		try {
			conn = getConnection();
			callableStmt = (OracleCallableStatement)conn.prepareCall("{? = call geodb_util.error_msg(?)}");
			callableStmt.setString(2, errorCode);
			callableStmt.registerOutParameter(1, Types.VARCHAR);
			callableStmt.executeUpdate();

			errorMessage = callableStmt.getString(1);

		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (callableStmt != null) {
				try {
					callableStmt.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				callableStmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
			}
		}

		return errorMessage;
	}

	private Connection getConnection() throws SQLException {
		Connection conn = dbConnectionPool.getConnection();	
		return conn;
	}

	public void cancelProcedure() {	
		cancelled = true;

		try {
			if (callableStmt != null)
				callableStmt.cancel();

		} catch (SQLException sqlEx) {
			//
		}
	}

	public enum DB_INDEX_TYPE {
		SPATIAL,
		NORMAL
	}

	public BoundingVolume transformBBox(BoundingVolume bbox, int sourceSrid, int targetSrid) throws SQLException {
		BoundingVolume result = bbox.clone();
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		Connection conn = null;

		try {
			conn = getConnection();
			psQuery = conn.prepareStatement("select SDO_CS.TRANSFORM(MDSYS.SDO_GEOMETRY(2003, " + sourceSrid +
					", NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 1003, 1), " +
					"MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), " + targetSrid + ") from dual");

			psQuery.setDouble(1, bbox.getLowerCorner().getX());
			psQuery.setDouble(2, bbox.getLowerCorner().getY());
			psQuery.setDouble(3, bbox.getUpperCorner().getX());
			psQuery.setDouble(4, bbox.getUpperCorner().getY());

			rs = (OracleResultSet)psQuery.executeQuery();
			while (rs.next()) {
				STRUCT struct = (STRUCT)rs.getObject(1); 
				if (!rs.wasNull() && struct != null) {
					JGeometry geom = JGeometry.load(struct);
					double[] ordinatesArray = geom.getOrdinatesArray();
					result.getLowerCorner().setX(ordinatesArray[0]);
					result.getLowerCorner().setY(ordinatesArray[1]);
					result.getUpperCorner().setX(ordinatesArray[2]);
					result.getUpperCorner().setY(ordinatesArray[3]);
				}
			}

			rs.close();
		} catch (SQLException sqlEx) {
			throw sqlEx;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
			}

			if (psQuery != null) {
				try {
					psQuery.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				psQuery = null;
			}
		}

		return result;
	}
}
