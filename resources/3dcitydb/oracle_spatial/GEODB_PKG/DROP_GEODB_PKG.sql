-- DROP_GEODB_PKG.sql
--
-- Authors:     Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Drops subpackages "geodb_*".
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.0.0     2008-09-10   release version                             CNag
--
SELECT 'Deleting packages ''geodb_util'', ''geodb_idx'', ''geodb_stat'', and corresponding types' as message from DUAL;
--// drop global types
DROP TYPE STRARRAY;
DROP TYPE INDEX_OBJ;

--// drop packages
DROP PACKAGE geodb_util;
DROP PACKAGE geodb_idx;
DROP PACKAGE geodb_stat;

SELECT 'Packages ''geodb_util'', ''geodb_idx'', and ''geodb_stat'' deleted' as message from DUAL;

SELECT 'Deleting matching tool packages ''geodb_match'', ''geodb_process_matches'', ''geodb_delete_by_lineage'', and corresponding types' as message from DUAL;
--// drop packages
DROP PACKAGE geodb_match;
DROP PACKAGE geodb_process_matches;
DROP PACKAGE geodb_delete_by_lineage;

--// drop tables
DROP TABLE match_result;
DROP TABLE match_master_aggr_geom;
DROP TABLE match_cand_aggr_geom;
DROP TABLE match_allocate_geom;
TRUNCATE TABLE match_tmp_building;
DROP TABLE match_tmp_building;

TRUNCATE TABLE MATCH_RESULT_RELEVANT;
DROP TABLE MATCH_RESULT_RELEVANT;
DROP TABLE COLLECT_GEOM;
DROP TABLE CONTAINER_IDS;
SELECT 'Packages ''geodb_match'', ''geodb_process_matches'', and ''geodb_delete_by_lineage'' deleted' as message from DUAL;