/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.geometry.GeometryObject;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.database.connection.ADEMetadata;
import org.citydb.database.connection.DatabaseMetaData;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public abstract class AbstractUtilAdapter {
	protected final AbstractDatabaseAdapter databaseAdapter;
	protected final ConcurrentHashMap<Integer, DatabaseSrs> srsInfoMap;
	private final ConcurrentHashMap<Integer, CoordinateReferenceSystem> srsDefMap;

	protected CallableStatement interruptableCallableStatement;
	protected Statement interruptableStatement;
	protected volatile boolean isInterrupted;
	
	protected AbstractUtilAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
		srsInfoMap = new ConcurrentHashMap<>();
		srsDefMap = new ConcurrentHashMap<Integer, CoordinateReferenceSystem>();
	}
	
	protected abstract void getCityDBVersion(DatabaseMetaData metaData, Connection connection) throws SQLException;
	protected abstract void getDatabaseMetaData(DatabaseMetaData metaData, String schema, Connection connection) throws SQLException;
	protected abstract void getSrsInfo(DatabaseSrs srs, Connection connection) throws SQLException;
	protected abstract String[] createDatabaseReport(String schema, Connection connection) throws SQLException;
	protected abstract BoundingBox calcBoundingBox(String schema, List<Integer> classIds, Connection connection) throws SQLException;
	protected abstract BoundingBox createBoundingBoxes(List<Integer> classIds, boolean onlyIfNull, Connection connection) throws SQLException;
	@Deprecated protected abstract BoundingBox transformBoundingBox(BoundingBox bbox, DatabaseSrs sourceSrs, DatabaseSrs targetSrs, Connection connection) throws SQLException;
	protected abstract GeometryObject transform(GeometryObject geometry, DatabaseSrs targetSrs, Connection connection) throws SQLException;
	protected abstract int get2DSrid(DatabaseSrs srs, Connection connection) throws SQLException;	
	protected abstract IndexStatusInfo manageIndexes(String operation, IndexType type, String schema, Connection connection) throws SQLException;
	protected abstract boolean updateTableStats(IndexType type, String schema, Connection connection) throws SQLException;
	public abstract DatabaseSrs getWGS843D();
	
	public DatabaseMetaData getDatabaseInfo() throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();

			// get vendor specific meta data
			java.sql.DatabaseMetaData vendorMetaData = conn.getMetaData();			

			// get 3dcitydb specific meta data
			DatabaseMetaData metaData = new DatabaseMetaData();
			getCityDBVersion(metaData, conn);
			getDatabaseMetaData(metaData, databaseAdapter.getConnectionDetails().getSchema(), conn);
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
	
	public List<ADEMetadata> getADEInfo() {
		ArrayList<ADEMetadata> ades = new ArrayList<>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			stmt = conn.createStatement();
			StringBuilder query = new StringBuilder("select adeid, name, description, version, db_prefix from ")
					.append(databaseAdapter.getConnectionDetails().getSchema()).append(".ade");
			
			rs = stmt.executeQuery(query.toString());
			while (rs.next()) {
				ADEMetadata ade = new ADEMetadata();
				ade.setADEId(rs.getString(1));
				ade.setName(rs.getString(2));
				ade.setDescription(rs.getString(3));
				ade.setVersion(rs.getString(4));
				ade.setDBPrefix(rs.getString(5));
				ades.add(ade);
			}
		} catch (SQLException e) {
			// nothing to do
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					//
				}

				rs = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}

				stmt = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//
				}

				conn = null;
			}
		}
		
		return ades;
	}

	public String[] createDatabaseReport(Workspace workspace, String schema) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(true);			
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			return createDatabaseReport(schema, conn);
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

	public BoundingBox calcBoundingBox(Workspace workspace, String schema, List<Integer> objectClassIds) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(true);			
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			return calcBoundingBox(schema, objectClassIds, conn);
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

	public BoundingBox createBoundingBoxes(Workspace workspace, String schema, List<Integer> objectClassIds, boolean onlyIfNull) throws SQLException {
		BoundingBox bbox = null;
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(false);
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			try {
				bbox = createBoundingBoxes(objectClassIds, onlyIfNull, conn);
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
	
	public IndexStatusInfo dropSpatialIndexes(String schema) throws SQLException {
		return dropIndexes(IndexType.SPATIAL, schema);
	}

	public IndexStatusInfo dropNormalIndexes(String schema) throws SQLException {
		return dropIndexes(IndexType.NORMAL, schema);
	}

	public IndexStatusInfo createSpatialIndexes(String schema) throws SQLException {
		return createIndexes(IndexType.SPATIAL, schema);
	}

	public IndexStatusInfo createNormalIndexes(String schema) throws SQLException {
		return createIndexes(IndexType.NORMAL, schema);
	}

	public IndexStatusInfo getStatusSpatialIndexes(String schema) throws SQLException {
		return getIndexStatus(IndexType.SPATIAL, schema);
	}

	public IndexStatusInfo getStatusNormalIndexes(String schema) throws SQLException {
		return getIndexStatus(IndexType.NORMAL, schema);
	}

	public IndexStatusInfo getIndexStatus(IndexType type, String schema) throws SQLException {
		String operation = type == IndexType.SPATIAL ? "citydb_idx.status_spatial_indexes" : "citydb_idx.status_normal_indexes";
		return manageIndexes(operation, type, schema);
	}

	private IndexStatusInfo createIndexes(IndexType type, String schema) throws SQLException {
		String operation = type == IndexType.SPATIAL ? "citydb_idx.create_spatial_indexes" : "citydb_idx.create_normal_indexes";
		return manageIndexes(operation, type, schema);
	}

	private IndexStatusInfo dropIndexes(IndexType type, String schema) throws SQLException {
		String operation = type == IndexType.SPATIAL ? "citydb_idx.drop_spatial_indexes" : "citydb_idx.drop_normal_indexes";
		return manageIndexes(operation, type, schema);
	}

	private IndexStatusInfo manageIndexes(String operation, IndexType type, String schema) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(true);
			return manageIndexes(operation, type, schema, conn);
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

	public boolean updateTableStatsSpatialColumns(String schema) throws SQLException {
		return updateTableStats(IndexType.SPATIAL, schema);
	}

	public boolean updateTableStatsNormalColumns(String schema) throws SQLException {
		return updateTableStats(IndexType.NORMAL, schema);
	}

	private boolean updateTableStats(IndexType type, String schema) throws SQLException {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			conn.setAutoCommit(true);
			return updateTableStats(type, schema, conn);
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

	public boolean isIndexEnabled(String tableName, String columnName) throws SQLException {
		return isIndexEnabled(tableName, columnName, databaseAdapter.getSchemaManager().getDefaultSchema());
	}

	public boolean isIndexEnabled(String tableName, String columnName, String schema) throws SQLException {
		boolean isIndexed = false;
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();

			interruptableCallableStatement = conn.prepareCall("{? = call " + databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_idx.index_status") + "(?, ?, ?)}");

			interruptableCallableStatement.setString(2, tableName);
			interruptableCallableStatement.setString(3, columnName);
			interruptableCallableStatement.setString(4, schema);
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

	@Deprecated
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

	public List<String> getAppearanceThemeList(Workspace workspace, String schema) throws SQLException {
		final String THEME_UNKNOWN = "<unknown>";

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<String> appearanceThemes = new ArrayList<String>();

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			if (schema == null || schema.length() == 0)
				schema = databaseAdapter.getSchemaManager().getDefaultSchema();

			stmt = conn.createStatement();
			rs = stmt.executeQuery("select distinct theme from " + schema + ".appearance order by theme");

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

	public int getNumGlobalAppearances(Workspace workspace, String schema) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			if (databaseAdapter.hasVersioningSupport())
				databaseAdapter.getWorkspaceManager().gotoWorkspace(conn, workspace);

			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(id) from " + schema + ".appearance where cityobject_id is null");

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

	public CoordinateReferenceSystem decodeDatabaseSrs(DatabaseSrs srs) throws FactoryException {
		if (srsDefMap.containsKey(srs.getSrid()))
			return srsDefMap.get(srs.getSrid());

		CoordinateReferenceSystem tmp = null;

		try {
			tmp = CRS.decode("EPSG:" + srs.getSrid());
		} catch (FactoryException e) {
			// 
		}


		if (tmp == null) {
			try {
				tmp = CRS.decode(srs.getGMLSrsName());
			} catch (FactoryException e) {
				// 
			}
		}

		if (tmp == null) {
			if (srs.getWkText() != null)
				tmp = CRS.parseWKT(srs.getWkText());
			else 
				throw new FactoryException("Failed to load SRS information for reference system " + srs.getDescription());
		}

		srsDefMap.putIfAbsent(srs.getSrid(), tmp);
		return tmp;
	}
}
