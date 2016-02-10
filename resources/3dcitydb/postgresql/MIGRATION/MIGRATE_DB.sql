-- MIGRATE_DB.sql
--
-- Authors:     Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2012-2015  Chair of Geoinformatics,
--                             Technische Universit�t M�nchen, Germany
--                             http://www.gis.bv.tum.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Top-level migration script that starts the migration process for a 3DCityDB 
-- instance of v2.1.0 to v3.1.0 for PostgreSQL databases >= 9.3
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.1.0     2015-11-02   update for v3.1                             FKun
-- 1.0.0     2014-12-28   release version                             FKun
--

-- This script is called from MIGRATE_DB.bat
\set ON_ERROR_STOP ON
\pset footer off
SET client_min_messages TO WARNING;

SELECT srid FROM database_srs \gset

--// In the previous version binary data of textures could be stored multiple times
--// when referred to different entries in the surface_data tables (bad for texture atlases).
--// This can be avoided in the new version because of a separated table for texture files.
--// IMPORTANT: The user has to be sure, that no tex_image_uri is used for different textures!
\prompt 'No texture URI is used for multiple texture files (yes (y)/unknown (n)): ' tex_opt
\set texop :tex_opt

--// create TABLES and SEQUENCES new in v3.1
\echo
\echo 'Create tables and sequences of 3DCityDB instance that are new in v3.1 ...'
\i CREATE_DB_V3.sql

--// fill tables OBJECTCLASS
\i ./../UTIL/CREATE_DB/OBJECTCLASS_INSTANCES.sql

--// create CITYDB_PKG (additional schema with PL/pgSQL-Functions)
\echo
\echo 'Creating additional schema ''citydb_pkg'' ...'
DROP SCHEMA IF EXISTS citydb_pkg CASCADE;
CREATE SCHEMA citydb_pkg;

\i ./../PL_pgSQL/CITYDB_PKG/UTIL/UTIL.sql
\i ./../PL_pgSQL/CITYDB_PKG/INDEX/IDX.sql
\i ./../PL_pgSQL/CITYDB_PKG/SRS/SRS.sql
\i ./../PL_pgSQL/CITYDB_PKG/STATISTICS/STAT.sql
\i ./../PL_pgSQL/CITYDB_PKG/DELETE/DELETE.sql
\i ./../PL_pgSQL/CITYDB_PKG/DELETE/DELETE_BY_LINEAGE.sql

--// create FUNCTIONS necessary for migration process
\echo
\echo 'Creating helper functions for migration process in geodb_pkg schema ...'
\i FUNCTIONS.sql

--// migrate TABLES from old to new schema
\echo
\echo 'Migrating database schema of 3DCityDB instance from v2.x to v3.1 ...'
\i MIGRATE_DB_V2_V3.sql

--// adding CONSTRAINTS in new schema
\echo
\echo 'Defining primary keys and foreign keys on v3.1 tables ...'
\i CONSTRAINTS_V3.sql

--// creating INDEXES in new schema
\echo
\echo 'Creating indexes on v3.1 tables ...'
\i INDEXES_V3.sql

--// removing v2.x schema (if the user wants to)
--\echo
--\echo 'Removing database elements of 3DCityDB v2.x schema ...'
--\i DROP_DB_V2.sql

--// update search_path on database level
ALTER DATABASE :"DBNAME" SET search_path TO citydb,citydb_pkg,public;

\echo
\echo '3DCityDB migration complete!'