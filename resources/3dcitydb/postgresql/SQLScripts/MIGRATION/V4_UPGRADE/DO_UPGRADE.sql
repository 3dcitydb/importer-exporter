-- 3D City Database - The Open Source CityGML Database
-- http://www.3dcitydb.org/
-- 
-- Copyright 2013 - 2019
-- Chair of Geoinformatics
-- Technical University of Munich, Germany
-- https://www.gis.bgu.tum.de/
-- 
-- The 3D City Database is jointly developed with the following
-- cooperation partners:
-- 
-- virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
-- M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--     http://www.apache.org/licenses/LICENSE-2.0
--     
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

\pset footer off
SET client_min_messages TO WARNING;
\set ON_ERROR_STOP ON

--// upgrade CITYDB_PKG (additional schema with PL/pgSQL functions)
\echo
\echo 'Upgrading schema ''citydb_pkg'' ...'
DROP SCHEMA IF EXISTS citydb_pkg CASCADE;
CREATE SCHEMA citydb_pkg;

\i ../../CITYDB_PKG/UTIL/UTIL.sql
\i ../../CITYDB_PKG/CONSTRAINT/CONSTRAINT.sql
\i ../../CITYDB_PKG/INDEX/IDX.sql
\i ../../CITYDB_PKG/SRS/SRS.sql
\i ../../CITYDB_PKG/STATISTICS/STAT.sql