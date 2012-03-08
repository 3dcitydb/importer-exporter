-- CREATE_GEODB_PKG.sql
--
-- Authors:     Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Conversion:	Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
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
-- Version | Date       | Description      | Author | Conversion
-- 1.0.0     2012-01-27   release version    CNag	  FKun
--

/*
 * PACKAGES do not exist in PostgreSQL. Only within Postgres Plus Advance Server from EnterpriseDB.
 * The use of schemas is proposed. Thus usage-rights may have to be set.
 */

CREATE SCHEMA geodb_pkg;

CREATE PROCEDURAL LANGUAGE plpgsql;
ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;
SET search_path = public, pg_catalog;

\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/UTIL/UTIL.sql;
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/INDEX/IDX.sql;
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/STATISTICS/STAT.sql;
--\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/DELETE/DELETE.sql;
--\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/DELETE/DELETE_BY_LINEAGE;
--\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/MATCHING/MATCH.sql;
--\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/GEODB_PKG/MATCHING/MERGE.sql;