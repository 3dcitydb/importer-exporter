-- 3D City Database - The Open Source CityGML Database
-- http://www.3dcitydb.org/
-- 
-- Copyright 2013 - 2017
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

\set RO_USERNAME :ro_username
\set SCHEMA_NAME :schema_name

\echo
\echo 'Granting read-only priviliges on schema "':SCHEMA_NAME'" to user "':RO_USERNAME'" ...'

GRANT CONNECT, TEMP ON DATABASE :"DBNAME" TO :"RO_USERNAME";
GRANT USAGE ON SCHEMA :"SCHEMA_NAME" TO :"RO_USERNAME";
GRANT SELECT ON ALL TABLES IN SCHEMA :"SCHEMA_NAME" TO :"RO_USERNAME";
GRANT USAGE ON SCHEMA citydb_pkg TO :"RO_USERNAME";
GRANT SELECT ON ALL TABLES IN SCHEMA citydb_pkg TO :"RO_USERNAME";
GRANT USAGE ON SCHEMA public TO :"RO_USERNAME";
GRANT SELECT ON ALL TABLES IN SCHEMA public TO :"RO_USERNAME";

\echo
\echo 'Read-only priviliges successfully granted.'