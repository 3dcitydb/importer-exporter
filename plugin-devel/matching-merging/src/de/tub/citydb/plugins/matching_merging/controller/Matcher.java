/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.plugins.matching_merging.controller;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.controller.LogController;
import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.event.EventHandler;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.plugins.matching_merging.PluginImpl;
import de.tub.citydb.plugins.matching_merging.config.ConfigImpl;
import de.tub.citydb.plugins.matching_merging.config.Merging;
import de.tub.citydb.plugins.matching_merging.config.Workspace;
import de.tub.citydb.plugins.matching_merging.events.CounterEvent;
import de.tub.citydb.plugins.matching_merging.events.EventType;
import de.tub.citydb.plugins.matching_merging.events.InterruptEvent;
import de.tub.citydb.plugins.matching_merging.events.StatusDialogMessage;
import de.tub.citydb.plugins.matching_merging.events.StatusDialogProgressBar;
import de.tub.citydb.plugins.matching_merging.events.StatusDialogTitle;
import de.tub.citydb.plugins.matching_merging.util.Util;

public class Matcher implements EventHandler {
	private final LogController logController;	

	private final DatabaseController databaseController;	
	private final PluginImpl plugin;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	public Matcher(PluginImpl plugin) {
		this.plugin = plugin;		
		databaseController = ObjectRegistry.getInstance().getDatabaseController();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		logController = ObjectRegistry.getInstance().getLogController();
	}
	
	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	private boolean checkWorkspace(Workspace workspace) {
		if (shouldRun && !workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = databaseController.existsWorkspace(workspace.getName());

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp().trim();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;

			if (!workspaceExists) {
				logController.error("Database workspace " + name + " is not available.");
				return false;
			} else 
				logController.info("Switching to database workspace " + name + '.');
		}

		return true;
	}

	public boolean match() {
		// checking workspace... this should be improved in future...
		Workspace workspace = plugin.getConfig().getWorkspace();
		if (!checkWorkspace(workspace))
			return false;

		ConfigImpl matching = plugin.getConfig();
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		int masterLODProjection = matching.getMasterBuildings().getLodProjection();
		int candLODProjection = matching.getCandidateBuildings().getLodProjection();
		double masterOverlap = matching.getMasterBuildings().getOverlap();
		double candOverlap = matching.getCandidateBuildings().getOverlap();
		double tolerance = matching.getMatching().getTolerance();
		String lineage = matching.getMatching().getLineage();

		// check whether spatial indexes are enabled
		logController.info("Checking for spatial indexes on geometry columns of involved tables...");
		try {
			if (!databaseController.isIndexEnabled("CITYOBJECT", "ENVELOPE") || 
					!databaseController.isIndexEnabled("SURFACE_GEOMETRY", "GEOMETRY")) {
				logController.error("Spatial indexes are not activated.");
				logController.error("Please use the preferences tab to activate the spatial indexes.");
				return false;
			}

		} catch (SQLException e) {
			logController.error("Failed to retrieve status of spatial indexes: " + e.getMessage());
			return false;
		}

		logController.info("Using lineage '" + lineage + "' to identify candidate buildings.");
		logController.info("Using buildings having a different lineage as master buildings.");
		logController.info("Starting matching process.");

		Connection conn = null;
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;
		int relevantOverlaps = 0;

		try {
			conn = databaseController.getConnection();
			databaseController.gotoWorkspace(conn, workspace.getName(), workspace.getTimestamp());
			conn.setAutoCommit(true);

			eventDispatcher.triggerEvent(new StatusDialogTitle(Util.I18N.getString("match.match.dialog.process"), this));
			stmt = conn.createStatement();				

			if (shouldRun) {
				logController.info("Identifying candidate buildings.");				
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.identifyCand"), this));

				cstmt = conn.prepareCall("{CALL geodb_match.collect_cand_building(?, ?)}");
				cstmt.setInt(1, candLODProjection); //LOD candidate
				cstmt.setString(2, lineage); //lineage
				cstmt.executeUpdate();

				int candidates = 0;
				result = stmt.executeQuery("select count(*) from match_tmp_building");
				if (result.next())
					candidates = result.getInt(1);

				if (candidates == 0) {
					logController.info("There are no candidates to be merged.");
					return false;
				}

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(1, 9, this));
			}
			
			if (shouldRun) {
				logController.info("Fetching LOD " + candLODProjection + " geometries of candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.collectGeomCand"), this));		

				cstmt = conn.prepareCall("{CALL geodb_match.collect_geometry(?)}");
				cstmt.setInt(1, candLODProjection); //Cand LOD
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(2, 9, this));
			}

			if (shouldRun) {
				logController.info("Rectifying geometries of candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.rectGeomCand"), this));		

				cstmt = conn.prepareCall("{CALL geodb_match.rectify_geometry(?)}");
				cstmt.setDouble(1, tolerance); //tolerance
				cstmt.executeUpdate();

				int geometries = 0;
				result = stmt.executeQuery("select count(*) from match_collect_geom");
				if (result.next())
					geometries = result.getInt(1);

				if (geometries == 0) {
					logController.info("There are no LOD " + candLODProjection + " representations of candidate buildings.");
					return false;
				}

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(3, 9, this));
			}

			if (shouldRun) {
				logController.info("Computing 2D projection of candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.unionCand"), this));		

				cstmt = conn.prepareCall("{CALL geodb_match.aggregate_geometry(?, ?, ?)}");
				cstmt.setString(1, "MATCH_CAND_PROJECTED");
				cstmt.setDouble(2, tolerance); //tolerance
				cstmt.setInt(3, 1); //aggregate building
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(4, 9, this));
			}

			if (shouldRun) {
				logController.info("Identifying master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.identifyMaster"), this));				

				cstmt = conn.prepareCall("{CALL geodb_match.collect_master_building(?, ?)}");
				cstmt.setInt(1, masterLODProjection); //Master LOD
				cstmt.setString(2, lineage); //lineage
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(5, 9, this));

				int master = 0;
				result = stmt.executeQuery("select count(*) from match_tmp_building");
				if (result.next())
					master = result.getInt(1);

				if (master == 0) {
					logController.info("There are no master buildings for matching.");
					return false;
				}
			}

			if (shouldRun) {
				logController.info("Fetching LOD " + masterLODProjection + " geometries of master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.collectGeomMaster"), this));				

				cstmt = conn.prepareCall("{CALL geodb_match.collect_geometry(?)}");
				cstmt.setInt(1, masterLODProjection); //Master LOD
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(6, 9, this));
			}

			if (shouldRun) {
				logController.info("Rectifying geometries of master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.rectGeomMaster"), this));				

				cstmt = conn.prepareCall("{CALL geodb_match.rectify_geometry(?)}");
				cstmt.setDouble(1, tolerance); //tolerance
				cstmt.executeUpdate();

				int geometries = 0;
				result = stmt.executeQuery("select count(*) from match_collect_geom");
				if (result.next())
					geometries = result.getInt(1);

				if (geometries == 0) {
					logController.info("There are no LOD " + masterLODProjection + " representations of master buildings.");
					return false;
				}

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(7, 9, this));
			}

			if (shouldRun) {
				logController.info("Computing 2D projection of master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.unionMaster"), this));				

				cstmt = conn.prepareCall("{CALL geodb_match.aggregate_geometry(?, ?, ?)}");
				cstmt.setString(1, "MATCH_MASTER_PROJECTED"); 
				cstmt.setDouble(2, tolerance); //tolerance
				cstmt.setInt(3, 1); //aggregate buildings
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(8, 10, this));
			}

			if (shouldRun) {
				logController.info("Computing overlaps between candidate and master buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.match.dialog.overlap"), this));				

				cstmt = conn.prepareCall("{CALL geodb_match.join_cand_master(?, ?, ?, ?)}"); 
				cstmt.setInt(1, candLODProjection); //LOD candidate
				cstmt.setString(2, lineage); //lineage
				cstmt.setInt(3, masterLODProjection); //Master LOD
				cstmt.setDouble(4, tolerance); //tolerance
				cstmt.executeUpdate();

				eventDispatcher.triggerEvent(new StatusDialogProgressBar(9, 10, this));

				relevantOverlaps = calcRelevantMatches(candOverlap, masterOverlap, conn);
				if (relevantOverlaps < 0)
					return false;
			}

			return shouldRun;
		} catch (SQLException sqlEx) {
			logController.error("SQL error while processing matching: " + sqlEx.getMessage());
			return false;
		} finally {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(10, 10, this));
			eventDispatcher.triggerEvent(new CounterEvent(relevantOverlaps, this));

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
		}
	}

	public boolean calcRelevantMatches() {		
		logController.info("Adapting minimum overlap thresholds.");

		Connection conn = null;
		ConfigImpl config = plugin.getConfig();
		int relevantOverlaps = 0;
		double masterOverlap = config.getMasterBuildings().getOverlap();
		double candOverlap = config.getCandidateBuildings().getOverlap();

		try {
			conn = databaseController.getConnection();
			conn.setAutoCommit(true);

			eventDispatcher.triggerEvent(new StatusDialogTitle(Util.I18N.getString("match.overlap.dialog.process"), this));
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(1, 2, this));

			relevantOverlaps = calcRelevantMatches(candOverlap, masterOverlap, conn);
			return relevantOverlaps > 0;
		} catch (SQLException e) {
			logController.error("SQL error while processing relevant matches: " + e.getMessage());
			return false;
		} finally {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(2, 2, this));
			eventDispatcher.triggerEvent(new CounterEvent(relevantOverlaps, this));

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//
				}
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
				logController.info("No overlaps between candidates and master buildings found.");
				return -1;
			}

			logController.info(overlaps + " overlap(s) found.");
			logController.info("Overlap of candidate buildings: min=" + minCandOverlap + "%, max=" + maxCandOverlap + "%");
			logController.info("Overlap of master buildings: min=" + minMasterOverlap + "%, max=" + maxMasterOverlap + "%");

			logController.info("Searching for 1:1 matches within the specified overlap thresholds.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.overlap.dialog.identifyMatch"), this));

			cstmt = conn.prepareCall("{CALL geodb_match.create_relevant_matches(?, ?)}");
			cstmt.setDouble(1, candOverlap); //delta1
			cstmt.setDouble(2, masterOverlap); //delta2
			cstmt.executeUpdate();

			result = stmt.executeQuery("select count(*) from match_overlap_relevant");
			if (result.next()) 
				relevantMatches = result.getInt(1);

			float percentage = (int)((float)relevantMatches/(overlaps > 0 ? overlaps : 1) * 10000) / 100f;				
			logController.info(relevantMatches + " relevant candidate match(es) found (" + Math.round(percentage*100)/100.0 + "%).");

			logController.info("Check the following tables for further information:");
			logController.info("MATCH_CAND_PROJECTED (2D projection of candidate buildings)");
			logController.info("MATCH_MASTER_PROJECTED (2D projection of master buildings)");
			logController.info("MATCH_OVERLAP_ALL (all overlaps between candidate and master buildings)");
			logController.info("MATCH_OVERLAP_RELEVANT (1:1 matches between candidate and master buildings)");

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
		Workspace workspace = plugin.getConfig().getWorkspace();
		if (!checkWorkspace(workspace))
			return false;

		ConfigImpl config = plugin.getConfig();
		Merging merging = config.getMerging();

		int masterLODGeometry = config.getMasterBuildings().getLodGeometry();
		int candLODGeometry = config.getCandidateBuildings().getLodGeometry();					
		String lineage = merging.getLineage();

		int gmlNameModeInt = 0;
		if (merging.isGmlNameModeIgnore()) 
			gmlNameModeInt = 1;
		else if (merging.isGmlNameModeReplace()) 
			gmlNameModeInt = 2;

		int deleteModeInt = 0;
		if (merging.isDeleteModeDelAll()) 
			deleteModeInt = 1;
		else if (merging.isDeleteModeRename()) 
			deleteModeInt = 2;

		logController.info("Starting merging process.");

		Connection conn = null;
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;

		try {
			conn = databaseController.getConnection();
			databaseController.gotoWorkspace(conn, workspace.getName(), workspace.getTimestamp());
			conn.setAutoCommit(true);

			eventDispatcher.triggerEvent(new StatusDialogTitle(Util.I18N.getString("match.merge.dialog.process"), this));

			stmt = conn.createStatement();
			int candidates = 0;
			result = stmt.executeQuery("select count(*) from match_overlap_relevant");
			if (result.next())
				candidates = result.getInt(1);

			if (candidates == 0) {
				logController.info("There are no candidates to be merged." );
				return true;
			}

			logController.info("Fetching LOD " + candLODGeometry + " geometries of the matched candidate buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.collectGeomCand"), this));				

			cstmt = conn.prepareCall("{CALL geodb_merge.collect_all_geometry(?)}");
			cstmt.setInt(1, candLODGeometry); //candidate LOD
			cstmt.executeUpdate();

			cstmt = conn.prepareCall("{CALL geodb_merge.remove_geometry_from_cand(?)}");
			cstmt.setInt(1, candLODGeometry); //candidate LOD
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(1, 6, this));
			logController.info("Moving appearances to master buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.moveApp"), this));				

			cstmt = conn.prepareCall("{CALL geodb_merge.create_and_put_container(?, ?, ?)}");
			cstmt.setInt(1, masterLODGeometry); //master LOD
			cstmt.setInt(2, gmlNameModeInt); //GmlNameMode
			cstmt.setString(3, " --/\\-- "); //delimiter
			cstmt.executeUpdate();

			cstmt = conn.prepareCall("{CALL geodb_merge.move_appearance()}");
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(2, 6, this));
			logController.info("Moving geometries to LOD " + masterLODGeometry + " of master buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.moveGeom"), this));				

			cstmt = conn.prepareCall("{CALL geodb_merge.move_geometry()}");
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(3, 6, this));
			logController.info("Removing geometries from candidate buildings.");
			eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.delGeom"), this));				

			cstmt = conn.prepareCall("{CALL geodb_merge.delete_head_of_merge_geometry()}");
			cstmt.executeUpdate();

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(4, 6, this));

			if (deleteModeInt == 1) {
				logController.info("Deleting matched candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.delCand"), this));				

				cstmt = conn.prepareCall("{CALL geodb_merge.delete_relevant_candidates()}");
				cstmt.executeUpdate();
			} else if (deleteModeInt == 2) {
				logController.info("Changing lineage of matched candidate buildings.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.changeCandLineage"), this));				

				cstmt = conn.prepareCall("{CALL geodb_merge.update_lineage(?)}");
				cstmt.setString(1, lineage); //lineage for renaming
				cstmt.executeUpdate();
			}

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(5, 6, this));

			int cityObjects = 0;
			result = stmt.executeQuery("select count(*) from merge_collect_geom");			
			if (result.next()) 
				cityObjects = result.getInt(1);

			if (cityObjects > 0) {
				logController.info("Cleaning matching results.");
				eventDispatcher.triggerEvent(new StatusDialogMessage(Util.I18N.getString("match.merge.dialog.clear"), this));				

				cstmt = conn.prepareCall("{CALL geodb_match.clear_matching_tables()}");
				cstmt.executeUpdate();				
			}

			logController.info(cityObjects + " city object(s) were affected by moving geometries and appearances.");
			return true;
		} catch (SQLException sqlEx) {
			logController.error("SQL error while processing merging: " + sqlEx.getMessage());
			return false;
		} finally {
			eventDispatcher.triggerEvent(new StatusDialogProgressBar(6, 6, this));			

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
		}
	}

	public boolean delete() {
		// checking workspace... this should be improved in future...
		Workspace workspace = plugin.getConfig().getWorkspace();
		if (!checkWorkspace(workspace))
			return false;

		String lineage = plugin.getConfig().getDeleteBuildingsByLineage().getLineage().trim();

		Connection conn = null;
		Statement stmt = null;
		ResultSet result = null;
		CallableStatement cstmt = null;

		logController.info("Deleting buildings with lineage '" + lineage + "'.");

		try {
			conn = databaseController.getConnection();
			databaseController.gotoWorkspace(conn, workspace.getName(), workspace.getTimestamp());
			conn.setAutoCommit(true);

			stmt = conn.createStatement();
			result = stmt.executeQuery("select count(*) from cityobject where class_id=26 and lineage='" + lineage+"'");
			if (!result.next()) 
				throw new SQLException("Could not select building tuples with lineage '" + lineage + '\'');

			int bldgCount = result.getInt(1);
			logController.info("Deleting " + bldgCount + " building(s).");

			if (bldgCount > 0) {
				cstmt = conn.prepareCall("{CALL geodb_delete_by_lineage.delete_buildings(?)}");
				cstmt.setString(1, lineage);
				cstmt.executeUpdate();
			}

			return true;
		} catch (SQLException sqlEx) {
			logController.error("SQL error while deleting buildings: " + sqlEx.getMessage());
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
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (isInterrupted.compareAndSet(false, true)) {
			shouldRun = false;
			logController.log(((InterruptEvent)e).getLogLevelType(), ((InterruptEvent)e).getLogMessage());
		}
	}

}

