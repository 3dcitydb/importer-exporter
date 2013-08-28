-- CREATE_GEODB_PKG.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--
-- Conversion:	Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universitaet Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Creates schema "geodb_pkg.*
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 1.0.0     2012-05-21   release version    CNag     FKun
--

-------------------------------------------------------------------------------
-- Conversion-Report:
-- PACKAGES do not exist in PostgreSQL. 
-- Only within PostgreSQL Plus Advance Server from EnterpriseDB.
-- The use of schemas is proposed. Thus usage-rights may have to be set.
-------------------------------------------------------------------------------

--// create GEODB_PKG schema
CREATE SCHEMA geodb_pkg;

--// call PL/pgSQL-Scripts to add GEODB_PKG-Functions
\i PL_pgSQL/GEODB_PKG/UTIL/UTIL.sql
\i PL_pgSQL/GEODB_PKG/INDEX/IDX.sql
\i PL_pgSQL/GEODB_PKG/STATISTICS/STAT.sql
\i PL_pgSQL/GEODB_PKG/DELETE/DELETE.sql
\i PL_pgSQL/GEODB_PKG/DELETE/DELETE_BY_LINEAGE.sql
\i PL_pgSQL/GEODB_PKG/MATCHING/MATCH.sql
\i PL_pgSQL/GEODB_PKG/MATCHING/MERGE.sql