-- UPDATE_DB_TO_2_0_5.sql
--
-- Authors:     Javier Herreruela <javier.herreruela@tu-berlin.de>
--              Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Copyright:   (c) 2007-2011, Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.0.0     2011-05-16   release version                             JHer
--                                                                    CNag

SET SERVEROUTPUT ON

SELECT 'Starting DB upgrade...' as message from DUAL;

--// drop old versions of GEODB_PKG
@@SCRIPTS/DROP_GEODB_PKG_GENERIC.sql

--// contents of @@../../CREATE_GEODB_PKG.sql
SELECT 'Installing GEODB package...' as message from DUAL;
@@../../PL_SQL/GEODB_PKG/UTIL/UTIL.sql;
@@../../PL_SQL/GEODB_PKG/INDEX/IDX.sql;
@@../../PL_SQL/GEODB_PKG/STATISTICS/STAT.sql;
@@../../PL_SQL/GEODB_PKG/DELETE/DELETE.sql;
@@../../PL_SQL/GEODB_PKG/DELETE/DELETE_BY_LINEAGE.sql;
@@../../PL_SQL/GEODB_PKG/MATCHING/MATCH.sql;
@@../../PL_SQL/GEODB_PKG/MATCHING/MERGE.sql;
SELECT 'GEODB package installed.' as message from DUAL;

SELECT 'DB upgrade complete!' as message from DUAL;
