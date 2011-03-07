package de.tub.citydb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oracle.jdbc.OracleResultSet;
import oracle.jdbc.driver.OracleCallableStatement;
import oracle.jdbc.driver.OracleTypes;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.BoundingVolume;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.db.DBConnectionPool;

public class DBUtil {
	private static HashMap<String, DBUtil> instanceMap = new HashMap<String, DBUtil>();

	private final DBConnectionPool dbConnectionPool;
	private volatile boolean cancelled = false;

	// use for interuptable operations
	private OracleCallableStatement callableStmt;
	private Statement stmt;

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

		return dbInfo;
	}

	public String[] databaseReport(Workspace workspace) throws SQLException {
		String[] report = null;
		Connection conn = null;

		try {
			conn = getConnection();
			dbConnectionPool.changeWorkspace(conn, workspace);		

			callableStmt = (OracleCallableStatement)conn.prepareCall("{? = call geodb_stat.table_contents}");
			callableStmt.registerOutParameter(1, OracleTypes.ARRAY, "STRARRAY");
			callableStmt.executeUpdate();

			ARRAY result = callableStmt.getARRAY(1);

			if (!cancelled)
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

	public BoundingVolume calcBoundingBox(Workspace workspace, FeatureClassMode featureClass) throws SQLException {
		BoundingVolume bbox = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			dbConnectionPool.changeWorkspace(conn, workspace);	
			stmt = conn.createStatement();

			List<Integer> featureTypes = new ArrayList<Integer>();
			String query = "select sdo_aggr_mbr(ENVELOPE) from CITYOBJECT where ENVELOPE is not NULL";

			switch (featureClass) {
			case BUILDING:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.BUILDING));
				break;
			case CITYFURNITURE:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITYFURNITURE));
				break;
			case CITYOBJECTGROUP:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITYOBJECTGROUP));
				break;
			case GENERICCITYOBJECT:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.GENERICCITYOBJECT));
				break;
			case LANDUSE:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.LANDUSE));
				break;
			case RELIEFFEATURE:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RELIEFFEATURE));
				break;
			case TRANSPORTATION:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRANSPORTATIONCOMPLEX));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.ROAD));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RAILWAY));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRACK));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SQUARE));
				break;
			case VEGETATION:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.PLANTCOVER));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SOLITARYVEGETATIONOBJECT));
				break;
			case WATERBODY:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.WATERBODY));
				break;
			default:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.BUILDING));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITYFURNITURE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITYOBJECTGROUP));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.GENERICCITYOBJECT));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.LANDUSE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RELIEFFEATURE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRANSPORTATIONCOMPLEX));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.ROAD));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RAILWAY));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRACK));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SQUARE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.PLANTCOVER));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SOLITARYVEGETATIONOBJECT));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.WATERBODY));
			}

			if (!featureTypes.isEmpty()) 
				query += " and CLASS_ID in (" + Util.collection2string(featureTypes, ", ") +") ";

			rs = stmt.executeQuery(query);
			Point lowerCorner = new Point(Double.MAX_VALUE);
			Point upperCorner = new Point(-Double.MAX_VALUE);

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

			if (!cancelled)
				bbox = new BoundingVolume(lowerCorner, upperCorner);

		} catch (SQLException sqlEx) {
			if (!cancelled)
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

			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}

				rs = null;
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

		return bbox;
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

	public void cancelOperation() {	
		cancelled = true;

		try {
			if (callableStmt != null)
				callableStmt.cancel();

			if (stmt != null)
				stmt.cancel();
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
			if (rs.next()) {
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
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
				
				conn = null;
			}
		}

		return result;
	}

	public boolean isSrsSupported(int srid) throws SQLException {
		Connection conn = null;
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		boolean isSupported = false;

		try {
			conn = getConnection();
			psQuery = conn.prepareStatement("select count(*) from MDSYS.CS_SRS where srid = ?");

			psQuery.setInt(1, srid);

			rs = (OracleResultSet)psQuery.executeQuery();
			if (rs.next()) {
				isSupported = (rs.getInt(1) > 0);
			}

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
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
				
				conn = null;
			}
		}

		return isSupported;
	}

	public List<String> getAppearanceThemeList() throws SQLException {
		Connection conn = null;
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		ArrayList<String> appearanceThemes = new ArrayList<String>();

		try {
			conn = getConnection();
			psQuery = conn.prepareStatement("select distinct theme from appearance order by theme");
			rs = (OracleResultSet)psQuery.executeQuery();
			while (rs.next()) {
				appearanceThemes.add(rs.getString(1));
			}

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
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					throw sqlEx;
				}
				
				conn = null;
			}
		}

		return appearanceThemes;
	}
}
