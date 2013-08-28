-- CREATE_DB.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Gerhard Koenig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--              Alexandra Stadler <stroh@igg.tu-berlin.de>
--
-- Conversion:	Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2012, Institute for Geodesy and Geoinformation Science,
--                             Technische Universitaet Berlin, Germany
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
-- Version | Date       | Description                               | Author | Conversion
-- 2.0.1     2008-06-28   versioning is enabled depending on var      TKol	   
-- 2.0.0     2012-05-21   PostGIS version                             TKol     FKun
--                                                                    GKoe
--                                                                    CNag
--                                                                    ASta
--

-- This script is called from CREATE_DB.bat

SET client_min_messages TO WARNING;

\prompt 'Please enter a valid SRID (e.g., 3068 for DHDN/Soldner Berlin): ' SRS_NO
\prompt 'Please enter the corresponding SRSName to be used in GML exports (e.g., urn:ogc:def:crs,crs:EPSG::3068,crs:EPSG::5783): ' GMLSRSNAME

\set SRSNO :SRS_NO
\set ON_ERROR_STOP ON
\echo

--// checks if the chosen SRID is provided by the spatial_ref_sys table
\i UTIL/CREATE_DB/CHECK_SRID.sql
SELECT check_srid(:SRS_NO);

\i SCHEMA/TABLES/METADATA/DATABASE_SRS.sql
INSERT INTO DATABASE_SRS(SRID,GML_SRS_NAME) VALUES (:SRS_NO,:'GMLSRSNAME');

--// create TABLES
\i SCHEMA/TABLES/METADATA/OBJECTCLASS.sql
\i SCHEMA/TABLES/CORE/CITYMODEL.sql
\i SCHEMA/TABLES/CORE/CITYOBJECT.sql
\i SCHEMA/TABLES/CORE/CITYOBJECT_MEMBER.sql
\i SCHEMA/TABLES/CORE/EXTERNAL_REFERENCE.sql
\i SCHEMA/TABLES/CORE/GENERALIZATION.sql
\i SCHEMA/TABLES/CORE/IMPLICIT_GEOMETRY.sql
\i SCHEMA/TABLES/GEOMETRY/SURFACE_GEOMETRY.sql
\i SCHEMA/TABLES/CITYFURNITURE/CITY_FURNITURE.sql
\i SCHEMA/TABLES/GENERICS/CITYOBJECT_GENERICATTRIB.sql
\i SCHEMA/TABLES/GENERICS/GENERIC_CITYOBJECT.sql
\i SCHEMA/TABLES/CITYOBJECTGROUP/CITYOBJECTGROUP.sql
\i SCHEMA/TABLES/CITYOBJECTGROUP/GROUP_TO_CITYOBJECT.sql
\i SCHEMA/TABLES/BUILDING/ADDRESS.sql
\i SCHEMA/TABLES/BUILDING/ADDRESS_TO_BUILDING.sql
\i SCHEMA/TABLES/BUILDING/BUILDING.sql
\i SCHEMA/TABLES/BUILDING/BUILDING_FURNITURE.sql
\i SCHEMA/TABLES/BUILDING/BUILDING_INSTALLATION.sql
\i SCHEMA/TABLES/BUILDING/OPENING.sql
\i SCHEMA/TABLES/BUILDING/OPENING_TO_THEM_SURFACE.sql
\i SCHEMA/TABLES/BUILDING/ROOM.sql
\i SCHEMA/TABLES/BUILDING/THEMATIC_SURFACE.sql
\i SCHEMA/TABLES/APPEARANCE/APPEARANCE.sql
\i SCHEMA/TABLES/APPEARANCE/SURFACE_DATA.sql
\i SCHEMA/TABLES/APPEARANCE/TEXTUREPARAM.sql
\i SCHEMA/TABLES/APPEARANCE/APPEAR_TO_SURFACE_DATA.sql
\i SCHEMA/TABLES/RELIEF/BREAKLINE_RELIEF.sql
\i SCHEMA/TABLES/RELIEF/MASSPOINT_RELIEF.sql
\i SCHEMA/TABLES/RELIEF/RASTER_RELIEF.sql
\i SCHEMA/TABLES/RELIEF/RELIEF.sql
\i SCHEMA/TABLES/RELIEF/RELIEF_COMPONENT.sql
\i SCHEMA/TABLES/RELIEF/RELIEF_FEAT_TO_REL_COMP.sql
\i SCHEMA/TABLES/RELIEF/RELIEF_FEATURE.sql
\i SCHEMA/TABLES/RELIEF/TIN_RELIEF.sql
\i SCHEMA/TABLES/ORTHOPHOTO/ORTHOPHOTO.sql;
\i SCHEMA/TABLES/TRANSPORTATION/TRANSPORTATION_COMPLEX.sql
\i SCHEMA/TABLES/TRANSPORTATION/TRAFFIC_AREA.sql
\i SCHEMA/TABLES/LANDUSE/LAND_USE.sql
\i SCHEMA/TABLES/VEGETATION/PLANT_COVER.sql
\i SCHEMA/TABLES/VEGETATION/SOLITARY_VEGETAT_OBJECT.sql
\i SCHEMA/TABLES/WATERBODY/WATERBODY.sql
\i SCHEMA/TABLES/WATERBODY/WATERBOD_TO_WATERBND_SRF.sql
\i SCHEMA/TABLES/WATERBODY/WATERBOUNDARY_SURFACE.sql

--// activate constraints
\i SCHEMA/CONSTRAINTS/CONSTRAINTS.sql

--// build INDEXES
\i SCHEMA/INDEXES/SIMPLE_INDEX.sql
\i SCHEMA/INDEXES/SPATIAL_INDEX.sql

--// fill table OBJECTCLASS
\i UTIL/CREATE_DB/OBJECTCLASS_INSTANCES.sql

--// create GEODB_PKG (additional schema with PL_pgSQL-Functions)
\i CREATE_GEODB_PKG.sql

\echo
\echo '3DCityDB creation complete!'