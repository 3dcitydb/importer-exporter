-- DROP_GEODB_PKG.sql
--
-- Authors:     Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
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
SELECT 'Deleting packages ''geodb_util'', ''geodb_idx'', ''geodb_stat'', ''geodb_delete_by_lineage'', ''geodb_delete'' and corresponding types' as message from DUAL;
--// drop global types
DROP TYPE STRARRAY;
DROP TYPE INDEX_OBJ;

--// drop packages
DROP PACKAGE geodb_util;
DROP PACKAGE geodb_idx;
DROP PACKAGE geodb_stat;
DROP PACKAGE geodb_delete_by_lineage;
DROP PACKAGE geodb_delete;

SELECT 'Packages ''geodb_util'', ''geodb_idx'', ''geodb_stat'', ''geodb_delete_by_lineage'', and ''geodb_delete'' deleted' as message from DUAL;

SELECT 'Deleting matching tool packages ''geodb_match'', ''geodb_merge'', and corresponding types' as message from DUAL;
--// drop packages
DROP PACKAGE geodb_match;
DROP PACKAGE geodb_merge;

--// drop tables
DROP TABLE match_overlap_all;
DROP TABLE match_overlap_relevant;
DROP TABLE match_master_projected;
DROP TABLE match_cand_projected;
DROP TABLE match_collect_geom;
TRUNCATE TABLE match_tmp_building;
DROP TABLE match_tmp_building;
DROP TABLE merge_collect_geom;
TRUNCATE TABLE merge_container_ids;
DROP TABLE merge_container_ids;
SELECT 'Packages ''geodb_match'', and ''geodb_merge'' deleted' as message from DUAL;