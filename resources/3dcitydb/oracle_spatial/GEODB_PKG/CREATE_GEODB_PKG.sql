-- CREATE_GEODB_PKG.sql
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
-- Creates subpackages "geodb_*".
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.0.0     2008-09-10   release version                             CNag
--
SELECT 'Creating packages ''geodb_util'', ''geodb_idx'', ''geodb_stat'', and corresponding types' as message from DUAL;
@@UTIL.sql;
@@IDX.sql;
@@STAT.sql;
SELECT 'Packages ''geodb_util'', ''geodb_idx'', and ''geodb_stat'' created' as message from DUAL;

SELECT 'Creating matching tool packages ''geodb_match'', ''geodb_process_matches'', ''geodb_delete_by_lineage'', and corresponding types' as message from DUAL;
@@MATCH.sql;
@@PROCESS_MATCHES.sql;
@@DELETE_BY_LINEAGE.sql;
SELECT 'Packages ''geodb_match'', ''geodb_process_matches'', and ''geodb_delete_by_lineage'' created' as message from DUAL;

