-- UPDATE_DB_TO_2_1_0.sql
--
-- Authors:     Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2013, Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
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
-- 1.0.0     2013-08-28   release version                             Fkun
--

SET client_min_messages TO WARNING;

\echo 'Starting DB upgrade...'

--// drop old versions of GEODB_PKG
DROP SCHEMA GEODB_PKG CASCADE;

--// create GEODB_PKG (additional schema with PL/pgSQL functions)
\echo
\echo 'Upgrading GEODB_PKG schema...'
CREATE SCHEMA geodb_pkg;

--// call PL/pgSQL-Scripts to add GEODB_PKG functions
\i ../../PL_pgSQL/GEODB_PKG/UTIL/UTIL.sql
\i ../../PL_pgSQL/GEODB_PKG/INDEX/IDX.sql
\i ../../PL_pgSQL/GEODB_PKG/STATISTICS/STAT.sql
\i ../../PL_pgSQL/GEODB_PKG/DELETE/DELETE.sql
\i ../../PL_pgSQL/GEODB_PKG/DELETE/DELETE_BY_LINEAGE.sql
\i ../../PL_pgSQL/GEODB_PKG/MATCHING/MATCH.sql
\i ../../PL_pgSQL/GEODB_PKG/MATCHING/MERGE.sql

\echo
\echo 'Updating indexes...'
ALTER INDEX BUILDING_FURN_LOD4REFPNT_SPX RENAME TO BLDG_FURN_LOD4REFPT_SPX;
ALTER INDEX SOL_VEGETAT_OBJ_LOD1REFPNT_SPX RENAME TO SOL_VEG_OBJ_LOD1REFPT_SPX;
ALTER INDEX SOL_VEGETAT_OBJ_LOD2REFPNT_SPX RENAME TO SOL_VEG_OBJ_LOD2REFPT_SPX;
ALTER INDEX SOL_VEGETAT_OBJ_LOD3REFPNT_SPX RENAME TO SOL_VEG_OBJ_LOD3REFPT_SPX;
ALTER INDEX SOL_VEGETAT_OBJ_LOD4REFPNT_SPX RENAME TO SOL_VEG_OBJ_LOD4REFPT_SPX;
CREATE INDEX GENERICCITY_LOD0REFPNT_SPX ON GENERIC_CITYOBJECT USING GIST ( LOD0_IMPLICIT_REF_POINT gist_geometry_ops_nd );

\echo
\echo 'DB upgrade complete!'
