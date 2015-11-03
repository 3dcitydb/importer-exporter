/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
import java.util.List;

import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseUtil;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.general.FeatureClassMode;
import org.citydb.database.DatabaseMetaDataImpl;
import org.citydb.database.IndexStatusInfo;
import org.citydb.database.IndexStatusInfo.IndexType;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;

public abstract class AbstractUtilAdapter implements DatabaseUtil {
	protected final AbstractDatabaseAdapter databaseAdapter;

	protected CallableStatement interruptableCallableStatement;
	protected Statement interruptableStatement;
	protected volatile boolean isInterrupted; 

	protected AbstractUtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}
	
	protected abstract void getCityDBVersion(DatabaseMetaDataImpl metaData, Connection connection) throws SQLException;
	protected abstract void getDatabaseMetaData(DatabaseMetaDataImpl metaData, Connection connection) throws SQLException;
	protected abstract void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException;
	protected abstract String[] createDatabaseReport(Connection connection) throws SQLException;
	protected abstract BoundingBox calcBoundingBox(List<Integer> classIds, Connection connection) throws SQLException;
	protected abstract BoundingBox transformBBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException;
	protected abstract int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException;	
	protected abstract IndexStatusInfo manageIndexes(String operation, IndexType type, Connection connection) throws SQLException;
	protected abstract void updateTableStats(IndexType type, Connection connection) throws SQLException;
	
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
	
	public boolean updateEnvelopes(Workspace workspace, FeatureClassMode featureClass, boolean onlyIfNull) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);
			
			List<Integer> classIds = getClassIds(featureClass, false);
			try {
				for (Integer classId : classIds) {
					String call = "{call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_envelope.set_envelope_cityobjects") + "(?,?)}";
					interruptableCallableStatement = conn.prepareCall(call);
					interruptableCallableStatement.setInt(1, classId);
					interruptableCallableStatement.setInt(2, onlyIfNull ? 1 : 0);
					interruptableCallableStatement.executeUpdate();
				}
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

			return true;
			
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
			
			List<Integer> classIds = getClassIds(featureClass, true);

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
	
	private List<Integer> getClassIds(FeatureClassMode featureClass, boolean setDefault) {
		
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
			if (setDefault) {
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
			else
				classIds.add(0); // UNDEFINED
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
	
	public void updateTableStatsSpatialColumns() throws SQLException {
		updateTableStats(IndexType.SPATIAL);
	}

	public void updateTableStatsNormalColumns() throws SQLException {
		updateTableStats(IndexType.NORMAL);
	}
	
	private void updateTableStats(IndexType type) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			updateTableStats(type, conn);
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
