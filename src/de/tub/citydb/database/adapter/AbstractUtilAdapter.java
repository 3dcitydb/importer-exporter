package de.tub.citydb.database.adapter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.database.DatabaseUtil;
import de.tub.citydb.api.geometry.BoundingBox;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.database.DatabaseMetaDataImpl;
import de.tub.citydb.database.IndexStatusInfo;
import de.tub.citydb.database.IndexStatusInfo.IndexType;
import de.tub.citydb.util.Util;

public abstract class AbstractUtilAdapter implements DatabaseUtil {
	protected final AbstractDatabaseAdapter databaseAdapter;

	protected CallableStatement interruptableCallableStatement;
	protected Statement interruptableStatement;
	protected volatile boolean isInterrupted; 

	protected AbstractUtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}
	
	protected abstract void getDatabaseMetaData(DatabaseMetaDataImpl metaData, Connection connection) throws SQLException;
	protected abstract void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException;
	protected abstract String[] createDatabaseReport(Connection connection) throws SQLException;
	protected abstract BoundingBox calcBoundingBox(List<Integer> classIds, Connection connection) throws SQLException;
	protected abstract BoundingBox transformBBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException;
	protected abstract int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException;	
	protected abstract IndexStatusInfo manageIndexes(String operation, IndexType type, Connection connection) throws SQLException;
	
	public DatabaseMetaDataImpl getDatabaseInfo() throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnectionWithTimeout();

			// get vendor specific meta data
			DatabaseMetaData vendorMetaData = conn.getMetaData();			

			// get 3dcitydb specific meta data
			DatabaseMetaDataImpl metaData = new DatabaseMetaDataImpl();
			getDatabaseMetaData(metaData, conn);			
			metaData.setDatabaseProductName(vendorMetaData.getDatabaseProductName());
			metaData.setDatabaseProductVersion(vendorMetaData.getDatabaseProductVersion());
			metaData.setDatabaseMajorVersion(vendorMetaData.getDatabaseMajorVersion());
			metaData.setDatabaseMinorVersion(vendorMetaData.getDatabaseMinorVersion());

			return metaData;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
	}
	
	public void getSrsInfo(DatabaseSrs srs) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			getSrsInfo(srs, conn);			
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}

				conn = null;
			}
		}
	}
	
	public String[] createDatabaseReport(Workspace workspace) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);
			
			return createDatabaseReport(conn);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
	}
	
	public BoundingBox calcBoundingBox(Workspace workspace, FeatureClassMode featureClass) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);
			
			List<Integer> classIds = new ArrayList<Integer>();
			switch (featureClass) {
			case BUILDING:
				classIds.add(Util.cityObject2classId(CityGMLClass.BUILDING));
				break;
			case CITYFURNITURE:
				classIds.add(Util.cityObject2classId(CityGMLClass.CITY_FURNITURE));
				break;
			case CITYOBJECTGROUP:
				classIds.add(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP));
				break;
			case GENERICCITYOBJECT:
				classIds.add(Util.cityObject2classId(CityGMLClass.GENERIC_CITY_OBJECT));
				break;
			case LANDUSE:
				classIds.add(Util.cityObject2classId(CityGMLClass.LAND_USE));
				break;
			case RELIEFFEATURE:
				classIds.add(Util.cityObject2classId(CityGMLClass.RELIEF_FEATURE));
				break;
			case TRANSPORTATION:
				classIds.add(Util.cityObject2classId(CityGMLClass.TRANSPORTATION_COMPLEX));
				classIds.add(Util.cityObject2classId(CityGMLClass.ROAD));
				classIds.add(Util.cityObject2classId(CityGMLClass.RAILWAY));
				classIds.add(Util.cityObject2classId(CityGMLClass.TRACK));
				classIds.add(Util.cityObject2classId(CityGMLClass.SQUARE));
				break;
			case VEGETATION:
				classIds.add(Util.cityObject2classId(CityGMLClass.PLANT_COVER));
				classIds.add(Util.cityObject2classId(CityGMLClass.SOLITARY_VEGETATION_OBJECT));
				break;
			case WATERBODY:
				classIds.add(Util.cityObject2classId(CityGMLClass.WATER_BODY));
				break;
			default:
				classIds.add(Util.cityObject2classId(CityGMLClass.BUILDING));
				classIds.add(Util.cityObject2classId(CityGMLClass.CITY_FURNITURE));
				classIds.add(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP));
				classIds.add(Util.cityObject2classId(CityGMLClass.GENERIC_CITY_OBJECT));
				classIds.add(Util.cityObject2classId(CityGMLClass.LAND_USE));
				classIds.add(Util.cityObject2classId(CityGMLClass.RELIEF_FEATURE));
				classIds.add(Util.cityObject2classId(CityGMLClass.TRANSPORTATION_COMPLEX));
				classIds.add(Util.cityObject2classId(CityGMLClass.ROAD));
				classIds.add(Util.cityObject2classId(CityGMLClass.RAILWAY));
				classIds.add(Util.cityObject2classId(CityGMLClass.TRACK));
				classIds.add(Util.cityObject2classId(CityGMLClass.SQUARE));
				classIds.add(Util.cityObject2classId(CityGMLClass.PLANT_COVER));
				classIds.add(Util.cityObject2classId(CityGMLClass.SOLITARY_VEGETATION_OBJECT));
				classIds.add(Util.cityObject2classId(CityGMLClass.WATER_BODY));
			}

			return calcBoundingBox(classIds, conn);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
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
		String operation = type == IndexType.SPATIAL ? "geodb_idx.status_spatial_indexes" : "geodb_idx.status_normal_indexes";
		return manageIndexes(operation, type);
	}

	private IndexStatusInfo createIndexes(IndexType type) throws SQLException {
		String operation = type == IndexType.SPATIAL ? "geodb_idx.create_spatial_indexes" : "geodb_idx.create_normal_indexes";
		return manageIndexes(operation, type);
	}

	private IndexStatusInfo dropIndexes(IndexType type) throws SQLException {
		String operation = type == IndexType.SPATIAL ? "geodb_idx.drop_spatial_indexes" : "geodb_idx.drop_normal_indexes";
		return manageIndexes(operation, type);
	}

	private IndexStatusInfo manageIndexes(String operation, IndexType type) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			return manageIndexes(operation, type, conn);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
	}
	
	@Override
	public boolean isIndexEnabled(String tableName, String columnName) throws SQLException {
		boolean isIndexed = false;
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();

			interruptableCallableStatement = conn.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("geodb_idx.index_status") + "(?, ?)}");
			interruptableCallableStatement.setString(2, tableName);
			interruptableCallableStatement.setString(3, columnName);
			interruptableCallableStatement.registerOutParameter(1, Types.VARCHAR);
			interruptableCallableStatement.executeUpdate();

			isIndexed = interruptableCallableStatement.getString(1).equals("VALID");
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
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
			
			isInterrupted = false;
		}
		
		return isIndexed;
	}
	
	@Override
	public BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			return transformBBox(bbox, sourceSrs, targetSrs, conn);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
	}
	
	public int get2DSrid(DatabaseSrs srs) throws SQLException {
		if (!srs.is3D())
			return srs.getSrid();

		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			int srid = get2DSrid(srs, conn);
			
			if (srid > 0)
				return srid;
			else 
				throw new SQLException("Failed to discover 2D equivalent for the 3D SRID " + srs.getSrid());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
	}
	
	public List<String> getAppearanceThemeList(Workspace workspace) throws SQLException {
		final String THEME_UNKNOWN = "<unknown>";

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<String> appearanceThemes = new ArrayList<String>();

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);
						
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select distinct theme from appearance order by theme");
			
			while (rs.next()) {
				String thema = rs.getString(1);
				if (thema != null)
					appearanceThemes.add(rs.getString(1));
				else
					appearanceThemes.add(THEME_UNKNOWN);
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

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw e;
				}

				stmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}

				conn = null;
			}
		}

		return appearanceThemes;
	}
	
	public int getNumGlobalAppearances(Workspace workspace) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(id) from appearance where cityobject_id is null");

			if (rs.next()) 
				return rs.getInt(1);
			else
				throw new SQLException("Failed to discover number of global appearances.");

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

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}

				conn = null;
			}
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
			if (interruptableStatement != null)
				interruptableStatement.cancel();
		} catch (SQLException e) {
			//
		}
	}
}
