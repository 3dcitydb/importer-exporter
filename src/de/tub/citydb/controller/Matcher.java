package de.tub.citydb.controller;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.matching.Match;
import de.tub.citydb.config.project.matching.Merge;
import de.tub.citydb.config.project.matching.MatchingSettings;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.log.Logger;

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

	public boolean match() {
		Match matchConfig = config.getProject().getMatching().getMatch();
		eventDispatcher.addListener(EventType.Interrupt, this);

		int lodRef = matchConfig.getLodReference();
		int lodMer = matchConfig.getLodMerge();
		String lineage = matchConfig.getLineage();
		boolean showMergeTable = matchConfig.getShowTable();

		LOG.info("Using lineage '" + lineage + "' to identify merge buildings.");
		LOG.info("Using buildings having a different lineage as reference buildings.");
		LOG.info("Starting matching process.");

		Connection conn = null;
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;
		
		try {
			conn = dbPool.getConnection();
			conn.setAutoCommit(true);
			
			if (shouldRun) {
				LOG.info("[1/9] Identifying merge buildings.");
				cstmt = conn.prepareCall("{CALL geodb_match.allocate_cand_building(?, ?)}");
				cstmt.setInt(1, lodMer); //LOD candidate
				cstmt.setString(2, lineage); //lineage
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[2/9] Allocating geometry objects of the merge buildings.");
				cstmt = conn.prepareCall("{CALL geodb_match.allocate_geometry(?)}");
				cstmt.setInt(1, lodMer); //Cand LOD
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[3/9] Rectifying merge geometry objects.");
				cstmt = conn.prepareCall("{CALL geodb_match.rectify_geometry(?)}");
				cstmt.setFloat(1, 0.001f); //tolerance
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[4/9] Computing topological union of the merge geometry objects.");
				cstmt = conn.prepareCall("{CALL geodb_match.aggregate_geometry(?, ?, ?)}");
				cstmt.setString(1, "MATCH_CAND_AGGR_GEOM");
				cstmt.setFloat(2, 0.001f); //tolerance
				cstmt.setInt(3, 1); //aggregate building
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[5/9] Identifying reference buildings.");
				cstmt = conn.prepareCall("{CALL geodb_match.allocate_master_building(?, ?)}");
				cstmt.setInt(1, lodRef); //Master LOD
				cstmt.setString(2, lineage); //lineage
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[6/9] Allocating geometry objects of the reference buildings.");
				cstmt = conn.prepareCall("{CALL geodb_match.allocate_geometry(?)}");
				cstmt.setInt(1, lodRef); //Master LOD
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[7/9] Rectifying reference geometry objects.");
				cstmt = conn.prepareCall("{CALL geodb_match.rectify_geometry(?)}");
				cstmt.setFloat(1, 0.001f); //tolerance
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[8/9] Computing topological union of the reference geometry objects.");
				cstmt = conn.prepareCall("{CALL geodb_match.aggregate_geometry(?, ?, ?)}");
				cstmt.setString(1, "MATCH_MASTER_AGGR_GEOM"); 
				cstmt.setFloat(2, 0.001f); //tolerance
				cstmt.setInt(3, 1); //aggregate buildings
				cstmt.executeUpdate();
			}

			if (shouldRun) {
				LOG.info("[9/9] Populating matching table.");
				cstmt = conn.prepareCall("{CALL geodb_match.join_cand_master(?, ?, ?, ?)}"); 
				cstmt.setInt(1, lodMer); //LOD candidate
				cstmt.setString(2, lineage); //lineage
				cstmt.setInt(3, lodRef); //Master LOD
				cstmt.setFloat(4, 0.001f); //tolerance
				cstmt.executeUpdate();

				int matchings = 0;
				stmt = conn.createStatement();				
				result = stmt.executeQuery("select count(*) from match_result");
				if (result.next())
					matchings = result.getInt(1);
				
				if (matchings == 0) {
					LOG.info("No candidate matches found.");
					return true;
				}					
				
				if (showMergeTable) {
					MatchingSettings matchPref = config.getProject().getMatching().getMatchingSettings();

					int rows = -1;
					if (matchPref.isResultModeFix()) 
						rows = 100;
					else if (matchPref.isResultModeUser()) 
						rows = matchPref.getResultUser();

					printTable(rows, conn);
				}
				
				LOG.info(matchings + " candidate matches found.");
			}
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while processing matching: " + sqlEx.getMessage());
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

		return shouldRun;
	}

	public void printTable(int rows, final Connection conn) {
		Statement stmt = null;
		ResultSet result = null;
			
		try {
			stmt = conn.createStatement();
			result = stmt.executeQuery("select id1, id2, area1_cov_by_area2, area2_cov_by_area1 from match_result");
			
			boolean isInitialized = false;			
			int i = 0;
			while (result.next() && (rows == -1 || i<rows) ) {
				if (!isInitialized) {					
					if (rows > 0)
						LOG.info("Printing first " + rows + " rows of matching table...");
					else
						LOG.info("Printing contents of matching table...");

					LOG.write("ID of merge\tID of reference\tOverlap[%] of\tOverlap[%] of");
					LOG.write("building\tbuilding\tmerge building\treference building");
					LOG.write("------------------------------------------------------------------");

					isInitialized = true;
				}

				int id1 = result.getInt(1);
				int id2 = result.getInt(2);
				float area1 = result.getFloat(3);
				float area2 = result.getFloat(4);
				LOG.write(Integer.toString(id1)+"\t\t"+Integer.toString(id2)+"\t\t"+Float.toString(area1*100).substring(0,5)+"\t\t"+Float.toString(area2*100).substring(0,5));
				i++;
			}

			if (isInitialized) {
				LOG.write("------------------------------------------------------------------");
				LOG.write("ID of merge\tID of reference\tOverlap[%] of\tOverlap[%] of");
				LOG.write("building\tbuilding\tmerge building\treference building");
			}
						
			LOG.info("Printed " + i + " rows of matching table.");
			
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while printing matching table: " + sqlEx.getMessage());
		} finally {
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
		Merge matchMerging = config.getProject().getMatching().getMerge();
		MatchingSettings mp = config.getProject().getMatching().getMatchingSettings();

		float percentageA = matchMerging.getOverlapOfMerge();
		float percentageB = matchMerging.getOverlapOfReference();
		int lodRef2 = matchMerging.getLodReference();
		int lodMer2 = matchMerging.getLodMerge();
		String lineage = matchMerging.getLineage();
		String delimiter = config.getInternal().getGmlNameDelimiter();

		int gmlNameModeInt = 0;
		if (mp.isGmlNameModeIgnore()) 
			gmlNameModeInt = 1;
		else if (mp.isGmlNameModeReplace()) 
			gmlNameModeInt = 2;

		int deleteModeInt = 0;
		if (mp.isDeleteModeDelAll()) 
			deleteModeInt = 1;
		else if (mp.isDeleteModeRename()) 
			deleteModeInt = 2;

		LOG.info("Starting merging process.");
		
		Connection conn = null;
		CallableStatement cstmt = null;
		Statement stmt = null;
		ResultSet result = null;
		
		try {
			conn = dbPool.getConnection();
			conn.setAutoCommit(true);

			LOG.info("[1/7] Identifying matching buildings according to the specified overlap thresholds.");
			cstmt = conn.prepareCall("{CALL geodb_process_matches.create_relevant_matches(?, ?)}");
			cstmt.setFloat(1, percentageA); //delta1
			cstmt.setFloat(2, percentageB); //delta2
			cstmt.executeUpdate();

			LOG.info("[2/7] Allocating geometry objects of the matched merge buildings.");
			cstmt = conn.prepareCall("{CALL geodb_process_matches.collect_all_geometry(?)}");
			cstmt.setInt(1, lodMer2); //candidate LOD
			cstmt.executeUpdate();

			cstmt = conn.prepareCall("{CALL geodb_process_matches.remove_geometry_from_cand(?)}");
			cstmt.setInt(1, lodMer2); //candidate LOD
			cstmt.executeUpdate();

			LOG.info("[3/7] Moving appearance information to reference buildings.");
			cstmt = conn.prepareCall("{CALL geodb_process_matches.move_appearance()}");
			cstmt.executeUpdate();

			cstmt = conn.prepareCall("{CALL geodb_process_matches.create_and_put_container(?, ?, ?)}");
			cstmt.setInt(1, lodRef2); //master LOD
			cstmt.setInt(2, gmlNameModeInt); //GmlNameMode
			cstmt.setString(3, delimiter); //delimiter
			cstmt.executeUpdate();

			LOG.info("[4/7] Moving geometry objects to reference buildings.");
			cstmt = conn.prepareCall("{CALL geodb_process_matches.move_geometry()}");
			cstmt.executeUpdate();

			LOG.info("[5/7] Deleting of redundant geometry objects in merge buildings.");
			cstmt = conn.prepareCall("{CALL geodb_process_matches.delete_multi_surfaces()}");
			cstmt.executeUpdate();

			if (deleteModeInt == 1) {
				cstmt = conn.prepareCall("{CALL geodb_process_matches.update_lineage(?)}");
				cstmt.setString(1, "@@@"); //lineage für umbenennung
				cstmt.executeUpdate();

				cstmt = conn.prepareCall("{CALL geodb_delete_by_lineage.delete_buildings(?)}");
				cstmt.setString(1, "@@@"); //lineage als schlüssel für zu löschende objekte
				cstmt.executeUpdate();
				LOG.info("[6/7] Deleting matched merge buildings.");
			}
			else if (deleteModeInt == 2) {
				cstmt = conn.prepareCall("{CALL geodb_process_matches.update_lineage(?)}");
				cstmt.setString(1, lineage); //lineage für umbenennung
				cstmt.executeUpdate();
				LOG.info("[6/7] Changing lineage of matched merge buildings.");
			}
			else 
				LOG.info("[6/7] Keeping merge buildings in database.");

			stmt = conn.createStatement();
			result = stmt.executeQuery("select count(*), max(area1_cov_by_area2), max(area2_cov_by_area1) from match_result");
			if (!result.next()) 
				throw new SQLException("Could not query matching table.");
			int sumOverlaps = result.getInt(1);
			float max1 = result.getFloat(2)*100.f;
			float max2 = result.getFloat(3)*100.f;
			result = stmt.executeQuery("select count(*) from match_result_relevant");
			if (!result.next()) 
				throw new SQLException("Could not query relevant matches table.");
			int relevantOverlaps = result.getInt(1);
			float percentage = 100 * (float)relevantOverlaps/(sumOverlaps > 0 ? sumOverlaps : 1);
			result = stmt.executeQuery("select count(*) from collect_geom");
			if (!result.next()) 
				throw new SQLException("Could not query table of collected geometry objects.");
			int cityObjects = result.getInt(1);

			LOG.info("[7/7] Cleaning temporary information.");
			cstmt = conn.prepareCall("{CALL geodb_process_matches.cleanup()}");
			cstmt.executeUpdate();

			LOG.info(sumOverlaps + " candidate match(es) found (max delta1: " + max1 + "%, max delta2: " + max2 + "%).");
			LOG.info(relevantOverlaps + " relevant match(es) identified (" + Math.round(percentage*100)/100.0 + "%).");
			LOG.info(cityObjects + " CityObject(s) affected by moving geometry objects.");

		} catch (SQLException sqlEx) {
			LOG.error("SQL error while processing merging: " + sqlEx.getMessage());
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

		return true;
	}

	public boolean delete() {
		String lineage = config.getProject().getMatching().getMatchingDelete().getLineage();
		if (lineage != null)
			lineage = lineage.trim();

		Connection conn = null;
		Statement stmt = null;
		ResultSet result = null;
		CallableStatement cstmt = null;

		LOG.info("Deleting buildings with lineage '" + lineage + "'.");

		try {
			conn = dbPool.getConnection();
			conn.setAutoCommit(true);
			
			stmt = conn.createStatement();
			result = stmt.executeQuery("select count(*) from cityobject c, building b where b.id = c.id and b.id = b.building_root_id and c.lineage='" + lineage+"'");
			if (!result.next()) 
				throw new SQLException("Could not select building tuples with lineage '" + lineage + '\'');
			int bldgCount = result.getInt(1);

			cstmt = conn.prepareCall("{CALL geodb_delete_by_lineage.delete_buildings(?)}");
			cstmt.setString(1, lineage); //lineage als schlüssel für zu löschende objekte
			cstmt.executeUpdate();

			LOG.info(bldgCount + " building(s) deleted.");
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
		}

		return true;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (isInterrupted.compareAndSet(false, true)) {
			if (e.getEventType() == EventType.Interrupt) {
				shouldRun = false;

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);
			}
		}
	}

}

