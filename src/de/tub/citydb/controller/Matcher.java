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
package de.tub.citydb.controller;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.matching.Matching;
import de.tub.citydb.config.project.matching.MergeConfig;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogProgressBar;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.DBUtil;
import de.tub.citydb.util.Util;

public class Matcher implements EventListener {
	private final Logger LOG = Logger.getInstance();	

	private final DBConnectionPool dbPool;	
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	public Matcher(DBConnectionPool dbPool, Config config, EventDispatcher eventDispatcher) {
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	private boolean checkWorkspace(Workspace workspace) {
		if (shouldRun && !workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.checkWorkspace(workspace);

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp().trim();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;

			if (!workspaceExists) {
				LOG.error("Database workspace " + name + " is not available.");
				return false;
			} else 
				LOG.info("Switching to database workspace " + name + '.');
		}

		return true;
	}

	public boolean match() {
		// checking workspace... this should be improved in future...
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace();
		if (!checkWorkspace(workspace))
			return false;

		Matching matching = config.getProject().getMatching();
		eventDispatcher.addListener(EventType.Interrupt, this);

		int masterLODProjection = matching.getMasterBuildings().getLodProjection();
		int candLODProjection = matching.getCandidateBuildings().getLodProjection();
		double masterOverlap = matching.getMasterBuildings().getOverlap();
		double candOverlap = matching.getCandidateBuildings().getOverlap();
		double tolerance = matching.getMatchConfig().getTolerance();
		String lineage = matching.getMatchConfig().getLineage();

		// check whether spatial indexes are enabled
		LOG.info("Checking for spatial indexes on geometry columns of involved tables...");
		DBUtil dbUtil = DBUtil.getInstance(dbPool);
		try {
			if (!dbUtil.isIndexed("CITYOBJECT", "ENVELOPE") || 
					!dbUtil.isIndexed("SURFACE_GEOMETRY", "GEOMETRY")) {
				LOG.error("Spatial indexes are not activated.");
				LOG.error("Please use the preferences tab to activate the spatial indexes.");
				return false;
			}

		} catch (SQLException e) {
			LOG.error("Failed to retrieve status of spatial indexes: " + e.getMessage());
			return false;
		}

		LOG.info("Using lineage '" + lineage + "' to identify candidate buildings.");
		LOG.info("Using buildings having a different lineage as master buildings.");
		LOG.info("Starting matching process.");

		Connection conn = null;
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;
		int relevantOverlaps = 0;

		try {
			conn = dbPool.getConnection();
			dbPool.changeWorkspace(conn, workspace);
			conn.setAutoCommit(true);

			eventDispatcher.triggerEvent(new StatusDialogTitle(Internal.I18N.getString("match.match.dialog.process")));
			stmt = conn.createStatement();				

			if (shouldRun) {
				LOG.info("Identifying candidate buildings.");				
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.identifyCand")));

				cstmt = conn.prepareCall("{CALL geodb_match.collect_cand_building(?, ?)}");
				cstmt.setInt(1, candLODProjection); //LOD candidate
				cstmt.setString(2, lineage); //lineage
				cstmt.executeUpdate();

				int candidates = 0;
				result = stmt.executeQuery("select count(*) from match_tmp_building");
				if (result.next())
					candidates = result.getInt(1);

				if (candidates == 0) {
					LOG.info("There are no candidates to be merged.");
					return false;
				}

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(1, 9));
			}

			if (shouldRun) {
				LOG.info("Fetching LOD " + candLODProjection + " geometries of candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.collectGeomCand")));		

				cstmt = conn.prepareCall("{CALL geodb_match.collect_geometry(?)}");
				cstmt.setInt(1, candLODProjection); //Cand LOD
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(2, 9));
			}

			if (shouldRun) {
				LOG.info("Rectifying geometries of candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.rectGeomCand")));		

				cstmt = conn.prepareCall("{CALL geodb_match.rectify_geometry(?)}");
				cstmt.setDouble(1, tolerance); //tolerance
				cstmt.executeUpdate();

				int geometries = 0;
				result = stmt.executeQuery("select count(*) from match_collect_geom");
				if (result.next())
					geometries = result.getInt(1);

				if (geometries == 0) {
					LOG.info("There are no LOD " + candLODProjection + " representations of candidate buildings.");
					return false;
				}

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(3, 9));
			}

			if (shouldRun) {
				LOG.info("Computing 2D projection of candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.unionCand")));		

				cstmt = conn.prepareCall("{CALL geodb_match.aggregate_geometry(?, ?, ?)}");
				cstmt.setString(1, "MATCH_CAND_PROJECTED");
				cstmt.setDouble(2, tolerance); //tolerance
				cstmt.setInt(3, 1); //aggregate building
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(4, 9));
			}

			if (shouldRun) {
				LOG.info("Identifying master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.identifyMaster")));				

				cstmt = conn.prepareCall("{CALL geodb_match.collect_master_building(?, ?)}");
				cstmt.setInt(1, masterLODProjection); //Master LOD
				cstmt.setString(2, lineage); //lineage
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(5, 9));

				int master = 0;
				result = stmt.executeQuery("select count(*) from match_tmp_building");
				if (result.next())
					master = result.getInt(1);

				if (master == 0) {
					LOG.info("There are no master buildings for matching.");
					return false;
				}
			}

			if (shouldRun) {
				LOG.info("Fetching LOD " + masterLODProjection + " geometries of master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.collectGeomMaster")));				

				cstmt = conn.prepareCall("{CALL geodb_match.collect_geometry(?)}");
				cstmt.setInt(1, masterLODProjection); //Master LOD
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(6, 9));
			}

			if (shouldRun) {
				LOG.info("Rectifying geometries of master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.rectGeomMaster")));				

				cstmt = conn.prepareCall("{CALL geodb_match.rectify_geometry(?)}");
				cstmt.setDouble(1, tolerance); //tolerance
				cstmt.executeUpdate();

				int geometries = 0;
				result = stmt.executeQuery("select count(*) from match_collect_geom");
				if (result.next())
					geometries = result.getInt(1);

				if (geometries == 0) {
					LOG.info("There are no LOD " + masterLODProjection + " representations of master buildings.");
					return false;
				}

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(7, 9));
			}

			if (shouldRun) {
				LOG.info("Computing 2D projection of master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.unionMaster")));				

				cstmt = conn.prepareCall("{CALL geodb_match.aggregate_geometry(?, ?, ?)}");
				cstmt.setString(1, "MATCH_MASTER_PROJECTED"); 
				cstmt.setDouble(2, tolerance); //tolerance
				cstmt.setInt(3, 1); //aggregate buildings
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(8, 10));
			}

			if (shouldRun) {
				LOG.info("Computing overlaps between candidate and master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.match.dialog.overlap")));				

				cstmt = conn.prepareCall("{CALL geodb_match.join_cand_master(?, ?, ?, ?)}"); 
				cstmt.setInt(1, candLODProjection); //LOD candidate
				cstmt.setString(2, lineage); //lineage
				cstmt.setInt(3, masterLODProjection); //Master LOD
				cstmt.setDouble(4, tolerance); //tolerance
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(9, 10));

				relevantOverlaps = calcRelevantMatches(candOverlap, masterOverlap, conn);
				if (relevantOverlaps < 0)
					return false;
			}

			return shouldRun;
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while processing matching: " + sqlEx.getMessage());
			return false;
		} finally {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(10, 10));
			eventDispatcher.triggerEvent(new CounterEvent(CounterType.RELEVANT_MATCHES, relevantOverlaps));

			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					//
				}

				cstmt = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}

				stmt = null;
			}

			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					//
				}

				result = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//
				}
			}

			try {
				dbPool.refresh();
			} catch (SQLException e) {
				//
			}
		}
	}

	public boolean calcRelevantMatches() {		
		LOG.info("Adapting minimum overlap thresholds.");

		Connection conn = null;
		Matching matching = config.getProject().getMatching();
		int relevantOverlaps = 0;
		double masterOverlap = matching.getMasterBuildings().getOverlap();
		double candOverlap = matching.getCandidateBuildings().getOverlap();

		try {
			conn = dbPool.getConnection();
			conn.setAutoCommit(true);
			
			eventDispatcher.triggerEvent(new StatusDialogTitle(Internal.I18N.getString("match.overlap.dialog.process")));
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(1, 2));

			relevantOverlaps = calcRelevantMatches(candOverlap, masterOverlap, conn);
			return relevantOverlaps > 0;
		} catch (SQLException e) {
			LOG.error("SQL error while processing relevant matches: " + e.getMessage());
			return false;
		} finally {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(2, 2));
			eventDispatcher.triggerEvent(new CounterEvent(CounterType.RELEVANT_MATCHES, relevantOverlaps));

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//
				}
			}

			try {
				dbPool.refresh();
			} catch (SQLException e) {
				//
			}
		}
	}

	private int calcRelevantMatches(double candOverlap, double masterOverlap, Connection conn) throws SQLException {
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;
		
		try {
			stmt = conn.createStatement();
			
			int overlaps = 0;
			int relevantMatches = 0;
			
			float minCandOverlap = 0f;
			float maxCandOverlap = 0f;
			float minMasterOverlap = 0f;
			float maxMasterOverlap = 0f;

			result = stmt.executeQuery("select count(*), min(area1_cov_by_area2), max(area1_cov_by_area2), min(area2_cov_by_area1), max(area2_cov_by_area1) from match_overlap_all");
			if (result.next()) {
				overlaps = result.getInt(1);
				minCandOverlap = (int)(result.getFloat(2) * 10000) / 100f;
				maxCandOverlap = (int)(result.getFloat(3) * 10000) / 100f;
				minMasterOverlap = (int)(result.getFloat(4) * 10000) / 100f;
				maxMasterOverlap = (int)(result.getFloat(5) * 10000) / 100f;
			}

			if (overlaps == 0) {
				LOG.info("No overlaps between candidates and master buildings found.");
				return -1;
			}

			LOG.info(overlaps + " overlap(s) found.");
			LOG.info("Overlap of candidate buildings: min=" + minCandOverlap + "%, max=" + maxCandOverlap + "%");
			LOG.info("Overlap of master buildings: min=" + minMasterOverlap + "%, max=" + maxMasterOverlap + "%");

			LOG.info("Searching for 1:1 matches within the specified overlap thresholds.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.overlap.dialog.identifyMatch")));

			cstmt = conn.prepareCall("{CALL geodb_match.create_relevant_matches(?, ?)}");
			cstmt.setDouble(1, candOverlap); //delta1
			cstmt.setDouble(2, masterOverlap); //delta2
			cstmt.executeUpdate();

			result = stmt.executeQuery("select count(*) from match_overlap_relevant");
			if (result.next()) 
				relevantMatches = result.getInt(1);

			float percentage = (int)((float)relevantMatches/(overlaps > 0 ? overlaps : 1) * 10000) / 100f;				
			LOG.info(relevantMatches + " relevant candidate match(es) found (" + Math.round(percentage*100)/100.0 + "%).");

			LOG.info("Check the following tables for further information:");
			LOG.info("MATCH_CAND_PROJECTED (2D projection of candidate buildings)");
			LOG.info("MATCH_MASTER_PROJECTED (2D projection of master buildings)");
			LOG.info("MATCH_OVERLAP_ALL (all overlaps between candidate and master buildings)");
			LOG.info("MATCH_OVERLAP_RELEVANT (1:1 matches between candidate and master buildings)");
			
			return relevantMatches;
		} finally {
			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					//
				}

				cstmt = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}

				stmt = null;
			}

			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					//
				}

				result = null;
			}
		}
	}

	public boolean merge() {
		// checking workspace... this should be improved in future...
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace();
		if (!checkWorkspace(workspace))
			return false;

		Matching matching = config.getProject().getMatching();
		MergeConfig mergeConfig = config.getProject().getMatching().getMergeConfig();

		int masterLODGeometry = matching.getMasterBuildings().getLodGeometry();
		int candLODGeometry = matching.getCandidateBuildings().getLodGeometry();					
		String lineage = mergeConfig.getLineage();

		int gmlNameModeInt = 0;
		if (mergeConfig.isGmlNameModeIgnore()) 
			gmlNameModeInt = 1;
		else if (mergeConfig.isGmlNameModeReplace()) 
			gmlNameModeInt = 2;

		int deleteModeInt = 0;
		if (mergeConfig.isDeleteModeDelAll()) 
			deleteModeInt = 1;
		else if (mergeConfig.isDeleteModeRename()) 
			deleteModeInt = 2;

		LOG.info("Starting merging process.");

		Connection conn = null;
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;

		try {
			conn = dbPool.getConnection();
			dbPool.changeWorkspace(conn, workspace);
			conn.setAutoCommit(true);

			eventDispatcher.triggerEvent(new StatusDialogTitle(Internal.I18N.getString("match.merge.dialog.process")));

			stmt = conn.createStatement();
			int candidates = 0;
			result = stmt.executeQuery("select count(*) from match_overlap_relevant");
			if (result.next())
				candidates = result.getInt(1);

			if (candidates == 0) {
				LOG.info("There are no candidates to be merged." );
				return true;
			}

			LOG.info("Fetching LOD " + candLODGeometry + " geometries of the matched candidate buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.collectGeomCand")));				

			cstmt = conn.prepareCall("{CALL geodb_merge.collect_all_geometry(?)}");
			cstmt.setInt(1, candLODGeometry); //candidate LOD
			cstmt.executeUpdate();

			cstmt = conn.prepareCall("{CALL geodb_merge.remove_geometry_from_cand(?)}");
			cstmt.setInt(1, candLODGeometry); //candidate LOD
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(1, 6));
			LOG.info("Moving appearances to master buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.moveApp")));				

			cstmt = conn.prepareCall("{CALL geodb_merge.create_and_put_container(?, ?, ?)}");
			cstmt.setInt(1, masterLODGeometry); //master LOD
			cstmt.setInt(2, gmlNameModeInt); //GmlNameMode
			cstmt.setString(3, Internal.GML_NAME_DELIMITER); //delimiter
			cstmt.executeUpdate();

			cstmt = conn.prepareCall("{CALL geodb_merge.move_appearance()}");
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(2, 6));
			LOG.info("Moving geometries to LOD " + masterLODGeometry + " of master buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.moveGeom")));				

			cstmt = conn.prepareCall("{CALL geodb_merge.move_geometry()}");
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(3, 6));
			LOG.info("Removing geometries from candidate buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.delGeom")));				

			cstmt = conn.prepareCall("{CALL geodb_merge.delete_head_of_merge_geometry()}");
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(4, 6));

			if (deleteModeInt == 1) {
				LOG.info("Deleting matched candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.delCand")));				

				cstmt = conn.prepareCall("{CALL geodb_merge.delete_relevant_candidates()}");
				cstmt.executeUpdate();
			} else if (deleteModeInt == 2) {
				LOG.info("Changing lineage of matched candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.changeCandLineage")));				

				cstmt = conn.prepareCall("{CALL geodb_merge.update_lineage(?)}");
				cstmt.setString(1, lineage); //lineage for renaming
				cstmt.executeUpdate();
			}

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(5, 6));

			int cityObjects = 0;
			result = stmt.executeQuery("select count(*) from merge_collect_geom");			
			if (result.next()) 
				cityObjects = result.getInt(1);

			if (cityObjects > 0) {
				LOG.info("Cleaning matching results.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("match.merge.dialog.clear")));				

				cstmt = conn.prepareCall("{CALL geodb_match.clear_matching_tables()}");
				cstmt.executeUpdate();				
			}

			LOG.info(cityObjects + " city object(s) were affected by moving geometries and appearances.");
			return true;
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while processing merging: " + sqlEx.getMessage());
			return false;
		} finally {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(6, 6));			

			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					//
				}

				cstmt = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}

				stmt = null;
			}

			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					//
				}

				result = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//
				}
			}

			try {
				dbPool.refresh();
			} catch (SQLException e) {
				//
			}
		}
	}

	public boolean delete() {
		// checking workspace... this should be improved in future...
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getMatchingWorkspace();
		if (!checkWorkspace(workspace))
			return false;

		String lineage = config.getProject().getMatching().getDeleteBuildingsByLineage().getLineage().trim();

		Connection conn = null;
		Statement stmt = null;
		ResultSet result = null;
		CallableStatement cstmt = null;

		LOG.info("Deleting buildings with lineage '" + lineage + "'.");

		try {
			conn = dbPool.getConnection();
			dbPool.changeWorkspace(conn, workspace);
			conn.setAutoCommit(true);

			stmt = conn.createStatement();
			result = stmt.executeQuery("select count(*) from cityobject where class_id= " + Util.cityObject2classId(CityGMLClass.BUILDING) + " and lineage='" + lineage+"'");
			if (!result.next()) 
				throw new SQLException("Could not select building tuples with lineage '" + lineage + '\'');

			int bldgCount = result.getInt(1);
			LOG.info("Deleting " + bldgCount + " building(s).");

			if (bldgCount > 0) {
				cstmt = conn.prepareCall("{CALL geodb_delete_by_lineage.delete_buildings(?)}");
				cstmt.setString(1, lineage);
				cstmt.executeUpdate();
			}

			return true;
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while deleting buildings: " + sqlEx.getMessage());
			return false;
		} finally {
			if (cstmt != null) {
				try {
					cstmt.close();
				} catch (SQLException e) {
					//
				}

				cstmt = null;
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}

				stmt = null;
			}

			if (result != null) {
				try {
					result.close();
				} catch (SQLException e) {
					//
				}

				result = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//
				}
			}

			try {
				dbPool.refresh();
			} catch (SQLException e) {
				//
			}
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.Interrupt) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);
			}
		}
	}

}

