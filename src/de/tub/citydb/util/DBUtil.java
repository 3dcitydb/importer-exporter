package de.tub.citydb.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import oracle.jdbc.driver.OracleCallableStatement;
import oracle.jdbc.driver.OracleTypes;
import oracle.sql.ARRAY;
import de.tub.citydb.db.DBConnectionPool;

public class DBUtil {
	private static DBUtil instance = null;
	
	private final DBConnectionPool dbConnectionPool;
	private OracleCallableStatement callableStmt;
	private volatile boolean cancelled = false;

	private DBUtil(DBConnectionPool dbConnectionPool) {
		this.dbConnectionPool = dbConnectionPool;
	}

	public static synchronized DBUtil getInstance(DBConnectionPool dbPool) {
		if (instance == null)
			instance = new DBUtil(dbPool);
		
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
}
