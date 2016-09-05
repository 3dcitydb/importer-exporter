/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseUtil;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.api.geometry.GeometryObject;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.general.FeatureClassMode;
import org.citydb.database.DatabaseMetaDataImpl;
import org.citydb.database.IndexStatusInfo;
import org.citydb.database.IndexStatusInfo.IndexType;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

public abstract class AbstractUtilAdapter implements DatabaseUtil {
	protected final AbstractDatabaseAdapter databaseAdapter;
	protected final ConcurrentHashMap<Integer, DatabaseSrs> srsInfoMap;

	protected CallableStatement interruptableCallableStatement;
	protected Statement interruptableStatement;
	protected volatile boolean isInterrupted;

	protected AbstractUtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
		srsInfoMap = new ConcurrentHashMap<>();
	}
	
	protected abstract void getCityDBVersion(DatabaseMetaDataImpl metaData, Connection connection) throws SQLException;
	protected abstract void getDatabaseMetaData(DatabaseMetaDataImpl metaData, Connection connection) throws SQLException;
	protected abstract void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException;
	protected abstract String[] createDatabaseReport(Connection connection) throws SQLException;
	protected abstract BoundingBox calcBoundingBox(List<Integer> classIds, Connection connection) throws SQLException;
	protected abstract BoundingBox createBoundingBoxes(List<Integer> classIds, boolean onlyIfNull, Connection connection) throws SQLException;
	protected abstract BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException;
	protected abstract GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs, Connection connection) throws SQLException;
	protected abstract int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException;	
	protected abstract IndexStatusInfo manageIndexes(String operation, IndexType type, Connection connection) throws SQLException;
	protected abstract boolean updateTableStats(IndexType type, Connection connection) throws SQLException;
	public abstract DatabaseSrs getWGS843D();
	
	public DatabaseMetaDataImpl getDatabaseInfo() throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnectionWithTimeout();

			// get vendor specific meta data
			DatabaseMetaData vendorMetaData = conn.getMetaData();			

			// get 3dcitydb specific meta data
			DatabaseMetaDataImpl metaData = new DatabaseMetaDataImpl();
			getCityDBVersion(metaData, conn);
			getDatabaseMetaData(metaData, conn);
			metaData.setDatabaseProductName(vendorMetaData.getDatabaseProductName());
			metaData.setDatabaseProductVersion(vendorMetaData.getDatabaseProductVersion());
			metaData.setDatabaseMajorVersion(vendorMetaData.getDatabaseMajorVersion());
			metaData.setDatabaseMinorVersion(vendorMetaData.getDatabaseMinorVersion());
			
			// put database srs info on internal map
			srsInfoMap.put(metaData.getReferenceSystem().getSrid(), metaData.getReferenceSystem());

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
			
			// put database srs info on internal map
			srsInfoMap.put(srs.getSrid(), srs);
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
			conn.setAutoCommit(true);			
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
			conn.setAutoCommit(true);			
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			return calcBoundingBox(getClassIds(featureClass), conn);
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
	
	public BoundingBox createBoundingBoxes(Workspace workspace, FeatureClassMode featureClass, boolean onlyIfNull) throws SQLException {
		BoundingBox bbox = null;
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(false);
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);
			
			try {
				List<Integer> classIds = null;
				if (featureClass == FeatureClassMode.CITYOBJECT)
					classIds = Arrays.asList(new Integer[]{0});
				else
					classIds = getClassIds(featureClass);
				
				bbox = createBoundingBoxes(classIds, onlyIfNull, conn);
				conn.commit();
				return bbox;				
			} catch (SQLException e) {
				conn.rollback();
				if (!isInterrupted)
					throw e;
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
		
		return bbox;
	}
	
	private List<Integer> getClassIds(FeatureClassMode featureClass) {
		List<Integer> classIds = new ArrayList<Integer>();
		switch (featureClass) {
		case BUILDING:
			classIds.add(Util.cityObject2classId(CityGMLClass.BUILDING));
			break;
		case BRIDGE:
			classIds.add(Util.cityObject2classId(CityGMLClass.BRIDGE));
			break;
		case TUNNEL:
			classIds.add(Util.cityObject2classId(CityGMLClass.TUNNEL));
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
			classIds.add(Util.cityObject2classId(CityGMLClass.BRIDGE));
			classIds.add(Util.cityObject2classId(CityGMLClass.TUNNEL));
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
		
		return classIds;
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
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(true);
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
	
	public boolean updateTableStatsSpatialColumns() throws SQLException {
		return updateTableStats(IndexType.SPATIAL);
	}

	public boolean updateTableStatsNormalColumns() throws SQLException {
		return updateTableStats(IndexType.NORMAL);
	}
	
	private boolean updateTableStats(IndexType type) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(true);
			return updateTableStats(type, conn);
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

			interruptableCallableStatement = conn.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_idx.index_status") + "(?, ?)}");
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
			return transformBoundingBox(bbox, sourceSrs, targetSrs, conn);
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
	public GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();		
			return transform(geometry, targetSrs, conn);
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
			return get2DSrid(srs, conn);
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
