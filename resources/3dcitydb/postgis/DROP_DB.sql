-- DROP_DB.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Zhihang Yao <zhihang.yao@tum.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--              Philipp Willkomm <pwillkomm@moss.de>
--              Gerhard K�nig <gerhard.koenig@tu-berlin.de>
--              Alexandra Lorenz <di.alex.lorenz@googlemail.com>
--
-- Copyright:   (c) 2012-2014  Chair of Geoinformatics,
--                             Technische Universit�t M�nchen, Germany
--                             http://www.gis.bv.tum.de
--
--              (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
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
-- 3.0.0     2014-01-08   new script for 3DCityDB V3                  FKun
-- 2.0.0     2011-12-11   PostGIS version                             FKun
--

SET client_min_messages TO WARNING;

--//DROP TABLES

DROP TABLE ADDRESS CASCADE;
DROP TABLE ADDRESS_TO_BRIDGE CASCADE;
DROP TABLE ADDRESS_TO_BUILDING CASCADE;
DROP TABLE APPEARANCE CASCADE;
DROP TABLE APPEAR_TO_SURFACE_DATA CASCADE;
DROP TABLE BREAKLINE_RELIEF CASCADE;
DROP TABLE BRIDGE CASCADE;
DROP TABLE BRIDGE_CONSTR_ELEMENT CASCADE;
DROP TABLE BRIDGE_FURNITURE CASCADE;
DROP TABLE BRIDGE_INSTALLATION CASCADE;
DROP TABLE BRIDGE_OPEN_TO_THEM_SRF CASCADE;
DROP TABLE BRIDGE_OPENING CASCADE;
DROP TABLE BRIDGE_ROOM CASCADE;
DROP TABLE BRIDGE_THEMATIC_SURFACE CASCADE;
DROP TABLE BUILDING CASCADE;
DROP TABLE BUILDING_FURNITURE CASCADE;
DROP TABLE BUILDING_INSTALLATION CASCADE;
DROP TABLE CITYMODEL CASCADE;
DROP TABLE CITYOBJECT CASCADE;
DROP TABLE CITYOBJECTGROUP CASCADE;
DROP TABLE CITYOBJECT_GENERICATTRIB CASCADE;
DROP TABLE CITYOBJECT_MEMBER CASCADE;
DROP TABLE CITY_FURNITURE CASCADE;
DROP TABLE DATABASE_SRS CASCADE;
DROP TABLE EXTERNAL_REFERENCE CASCADE;
DROP TABLE GENERALIZATION CASCADE;
DROP TABLE GENERIC_CITYOBJECT CASCADE;
DROP TABLE GROUP_TO_CITYOBJECT CASCADE;
DROP TABLE IMPLICIT_GEOMETRY CASCADE;
DROP TABLE LAND_USE CASCADE;
DROP TABLE MASSPOINT_RELIEF CASCADE;
DROP TABLE OBJECTCLASS CASCADE;
DROP TABLE OPENING CASCADE;
DROP TABLE OPENING_TO_THEM_SURFACE CASCADE;
DROP TABLE PLANT_COVER CASCADE;
DROP TABLE RASTER_RELIEF CASCADE;
DROP TABLE RASTER_RELIEF_GEORASTER CASCADE;
DROP TABLE RELIEF_COMPONENT CASCADE;
DROP TABLE RELIEF_FEATURE CASCADE;
DROP TABLE RELIEF_FEAT_TO_REL_COMP CASCADE;
DROP TABLE ROOM CASCADE;
DROP TABLE SOLITARY_VEGETAT_OBJECT CASCADE;
DROP TABLE SURFACE_DATA CASCADE;
DROP TABLE SURFACE_GEOMETRY CASCADE;
DROP TABLE TEX_IMAGE CASCADE;
DROP TABLE TEXTUREPARAM CASCADE;
DROP TABLE THEMATIC_SURFACE CASCADE;
DROP TABLE TIN_RELIEF CASCADE;
DROP TABLE TRAFFIC_AREA CASCADE;
DROP TABLE TRANSPORTATION_COMPLEX CASCADE;
DROP TABLE TUNNEL CASCADE;
DROP TABLE TUNNEL_FURNITURE CASCADE;
DROP TABLE TUNNEL_HOLLOW_SPACE CASCADE;
DROP TABLE TUNNEL_INSTALLATION CASCADE;
DROP TABLE TUNNEL_OPEN_TO_THEM_SRF CASCADE;
DROP TABLE TUNNEL_OPENING CASCADE;
DROP TABLE TUNNEL_THEMATIC_SURFACE CASCADE;
DROP TABLE WATERBODY CASCADE;
DROP TABLE WATERBOD_TO_WATERBND_SRF CASCADE;
DROP TABLE WATERBOUNDARY_SURFACE CASCADE;

--//DROP SEQUENCES
DROP SEQUENCE address_seq;
DROP SEQUENCE appearance_seq;
DROP SEQUENCE citymodel_seq;
DROP SEQUENCE cityobject_genericatt_seq;
DROP SEQUENCE cityobject_seq;
DROP SEQUENCE external_ref_seq;
DROP SEQUENCE implicit_geometry_seq;
DROP SEQUENCE raster_rel_georaster_seq;
DROP SEQUENCE surface_data_seq;
DROP SEQUENCE surface_geometry_seq;
DROP SEQUENCE tex_image_seq;

--//DROP SCHEMA
DROP SCHEMA GEODB_PKG CASCADE;

--//DROP POSTGIS EXTENSION
DROP EXTENSION postgis CASCADE;

\echo
\echo '3DCityDB schema successfully removed!'