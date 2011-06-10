/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.util.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleTypes;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

import org.citygml4j.geometry.BoundingBox;
import org.citygml4j.geometry.Point;
import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.api.log.Logger;
import de.tub.citydb.config.project.database.DBMetaData;
import de.tub.citydb.config.project.database.DBMetaData.Versioning;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.general.FeatureClassMode;
import de.tub.citydb.database.DBConnectionPool;
import de.tub.citydb.util.Util;

public class DBUtil {
	private static final DBConnectionPool dbConnectionPool = DBConnectionPool.getInstance();
	private static volatile boolean cancelled = false;

	// use for interuptable operations
	private static OracleCallableStatement callableStmt;
	private static Statement stmt;

	public static DBMetaData getDatabaseInfo() throws SQLException {
		DBMetaData metaData = new DBMetaData();
		Connection conn = null;
		ResultSet rs = null;

		try {
			conn = dbConnectionPool.getConnection();

			// get vendor specific meta data
			DatabaseMetaData dbMetaData = conn.getMetaData();			

			// get 3dcitydb specific meta data
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from table(geodb_util.db_info)");
			if (rs.next()) {
				metaData.setSrid(rs.getInt("SRID"));
				metaData.setSrsName(rs.getString("GML_SRS_NAME"));
				metaData.setReferenceSystemName(rs.getString("COORD_REF_SYS_NAME"));
				metaData.setReferenceSystem3D(rs.getBoolean("IS_COORD_REF_SYS_3D"));
				metaData.setVersioning(Versioning.valueOf(rs.getString("VERSIONING")));
			} else
				throw new SQLException("Failed to retrieve metadata information from database.");

			metaData.setDatabaseProductName(dbMetaData.getDatabaseProductName());
			metaData.setDatabaseProductVersion(dbMetaData.getDatabaseProductVersion());
			metaData.setDatabaseMajorVersion(dbMetaData.getDatabaseMajorVersion());
			metaData.setDatabaseMinorVersion(dbMetaData.getDatabaseMinorVersion());

			return metaData;

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
	}

	public static String[] databaseReport(Workspace workspace) throws SQLException {
		String[] report = null;
		Connection conn = null;

		try {
			conn = dbConnectionPool.getConnection();
			dbConnectionPool.gotoWorkspace(conn, workspace);		

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

	public static BoundingBox calcBoundingBox(Workspace workspace, FeatureClassMode featureClass) throws SQLException {
		BoundingBox bbox = null;
		Connection conn = null;
		ResultSet rs = null;

		try {
			conn = dbConnectionPool.getConnection();
			dbConnectionPool.gotoWorkspace(conn, workspace);	
			stmt = conn.createStatement();

			List<Integer> featureTypes = new ArrayList<Integer>();
			String query = "select sdo_aggr_mbr(ENVELOPE) from CITYOBJECT where ENVELOPE is not NULL";

			switch (featureClass) {
			case BUILDING:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.BUILDING));
				break;
			case CITYFURNITURE:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITY_FURNITURE));
				break;
			case CITYOBJECTGROUP:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP));
				break;
			case GENERICCITYOBJECT:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.GENERIC_CITY_OBJECT));
				break;
			case LANDUSE:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.LAND_USE));
				break;
			case RELIEFFEATURE:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RELIEF_FEATURE));
				break;
			case TRANSPORTATION:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRANSPORTATION_COMPLEX));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.ROAD));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RAILWAY));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRACK));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SQUARE));
				break;
			case VEGETATION:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.PLANT_COVER));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SOLITARY_VEGETATION_OBJECT));
				break;
			case WATERBODY:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.WATER_BODY));
				break;
			default:
				featureTypes.add(Util.cityObject2classId(CityGMLClass.BUILDING));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITY_FURNITURE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.CITY_OBJECT_GROUP));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.GENERIC_CITY_OBJECT));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.LAND_USE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RELIEF_FEATURE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRANSPORTATION_COMPLEX));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.ROAD));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.RAILWAY));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.TRACK));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SQUARE));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.PLANT_COVER));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.SOLITARY_VEGETATION_OBJECT));
				featureTypes.add(Util.cityObject2classId(CityGMLClass.WATER_BODY));
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
				bbox = new BoundingBox(lowerCorner, upperCorner);

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

	private static String[] dropIndexes(DB_INDEX_TYPE type) throws SQLException {
		String[] report = null;
		Connection conn = null;

		String call = type == DB_INDEX_TYPE.SPATIAL ? 
				"{? = call geodb_idx.drop_spatial_indexes}" : 
					"{? = call geodb_idx.drop_normal_indexes}";

		try {
			conn = dbConnectionPool.getConnection();
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

	private static String[] createIndexes(DB_INDEX_TYPE type) throws SQLException {
		String[] report = null;
		Connection conn = null;

		String call = type == DB_INDEX_TYPE.SPATIAL ? 
				"{? = call geodb_idx.create_spatial_indexes}" : 
					"{? = call geodb_idx.create_normal_indexes}";

		try {
			conn = dbConnectionPool.getConnection();
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

	public static boolean isIndexed(String tableName, String columnName) throws SQLException {
		Connection conn = null;
		boolean isIndexed = false;

		try {
			conn = dbConnectionPool.getConnection();
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

	public static String[] dropSpatialIndexes() throws SQLException {
		return dropIndexes(DB_INDEX_TYPE.SPATIAL);
	}

	public static String[] dropNormalIndexes() throws SQLException {
		return dropIndexes(DB_INDEX_TYPE.NORMAL);
	}

	public static String[] createSpatialIndexes() throws SQLException {
		return createIndexes(DB_INDEX_TYPE.SPATIAL);
	}

	public static String[] createNormalIndexes() throws SQLException {
		return createIndexes(DB_INDEX_TYPE.NORMAL);
	}

	public static String errorMessage(String errorCode) throws SQLException {
		String errorMessage = null;
		Connection conn = null;

		try {
			conn = dbConnectionPool.getConnection();
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

	public static void cancelOperation() {	
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

	public static enum DB_INDEX_TYPE {
		SPATIAL,
		NORMAL
	}

	public static BoundingBox transformBBox(BoundingBox bbox, int sourceSrid, int targetSrid) throws SQLException {
		BoundingBox result = new BoundingBox(bbox);
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		Connection conn = null;

		try {
			conn = dbConnectionPool.getConnection();
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

	public static boolean isSrsSupported(int srid) throws SQLException {
		Connection conn = null;
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		boolean isSupported = false;

		try {
			conn = dbConnectionPool.getConnection();
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

	public static List<String> getAppearanceThemeList(Workspace workspace) throws SQLException {
		Connection conn = null;
		PreparedStatement psQuery = null;
		OracleResultSet rs = null;
		ArrayList<String> appearanceThemes = new ArrayList<String>();

		try {
			boolean workspaceExists = dbConnectionPool.existsWorkspace(workspace);

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp().trim();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;

			if (!workspaceExists) {
				Logger.getInstance().error("Database workspace " + name + " is not available.");
			} 

			conn = dbConnectionPool.getConnection();
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
