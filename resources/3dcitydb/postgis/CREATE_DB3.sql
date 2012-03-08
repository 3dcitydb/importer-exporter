-- CREATE_DB2.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stroh@igg.tu-berlin.de>
--
-- Conversion:	Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2011, Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description                               | Author | Conversion
-- 2.0.1     2008-06-28   versioning is enabled depending on var      TKol	   
-- 2.0.0     2011-12-11   release version                             TKol	   FKun
--                                                                    GKoe
--                                                                    CNag
--                                                                    ASta
--
--SET SERVEROUTPUT ON
--SET FEEDBACK ON
--SET VER OFF

--VARIABLE VERSIONBATCHFILE VARCHAR2(50);

-- This script is called from CREATE_DB.sql and it
-- is required that the three substitution variables
-- &SRSNO, &GMLSRSNAME, and &VERSIONING are set properly.

--// create database srs

\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/METADATA/DATABASE_SRS.sql

INSERT INTO DATABASE_SRS(SRID,GML_SRS_NAME) VALUES (81989002,'urn:ogc:def:crs,crs:EPSG:6.12:3068,crs:EPSG:6.12:5783');

--// create tables
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/METADATA/OBJECTCLASS.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CORE/CITYMODEL.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CORE/CITYOBJECT.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CORE/CITYOBJECT_MEMBER.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CORE/EXTERNAL_REFERENCE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CORE/GENERALIZATION.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CORE/IMPLICIT_GEOMETRY.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/GEOMETRY/SURFACE_GEOMETRY.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CITYFURNITURE/CITY_FURNITURE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/GENERICS/CITYOBJECT_GENERICATTRIB.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/GENERICS/GENERIC_CITYOBJECT.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CITYOBJECTGROUP/CITYOBJECTGROUP.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/CITYOBJECTGROUP/GROUP_TO_CITYOBJECT.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/ADDRESS.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/ADDRESS_TO_BUILDING.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/BUILDING.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/BUILDING_FURNITURE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/BUILDING_INSTALLATION.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/OPENING.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/OPENING_TO_THEM_SURFACE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/ROOM.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/BUILDING/THEMATIC_SURFACE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/APPEARANCE/APPEARANCE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/APPEARANCE/SURFACE_DATA.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/APPEARANCE/TEXTUREPARAM.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/APPEARANCE/APPEAR_TO_SURFACE_DATA.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/BREAKLINE_RELIEF.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/MASSPOINT_RELIEF.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/RASTER_RELIEF.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/RASTER_RELIEF_IMP.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/RELIEF.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/RELIEF_COMPONENT.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/RELIEF_FEAT_TO_REL_COMP.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/RELIEF_FEATURE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/RELIEF/TIN_RELIEF.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/ORTHOPHOTO/ORTHOPHOTO.sql;
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/ORTHOPHOTO/ORTHOPHOTO_IMP.sql;
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/TRANSPORTATION/TRANSPORTATION_COMPLEX.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/TRANSPORTATION/TRAFFIC_AREA.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/LANDUSE/LAND_USE.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/VEGETATION/PLANT_COVER.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/VEGETATION/SOLITARY_VEGETAT_OBJECT.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/WATERBODY/WATERBODY.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/WATERBODY/WATERBOD_TO_WATERBND_SRF.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/TABLES/WATERBODY/WATERBOUNDARY_SURFACE.sql

--// activate constraints
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/CONSTRAINTS/CONSTRAINTS.sql

--// BUILD INDEXES
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/INDEXES/SIMPLE_INDEX.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/SCHEMA/INDEXES/SPATIAL_INDEX.sql

\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/UTIL/CREATE_DB/OBJECTCLASS_INSTANCES.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/UTIL/CREATE_DB/IMPORT_PROCEDURES.sql
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/UTIL/CREATE_DB/DUMMY_IMPORT.sql

/*--// (possibly) activate versioning
BEGIN
  :VERSIONBATCHFILE := 'UTIL/CREATE_DB/DO_NOTHING.sql';
END;
/
BEGIN
  IF ('&VERSIONING'='yes' OR '&VERSIONING'='YES' OR '&VERSIONING'='y' OR '&VERSIONING'='Y') THEN
    :VERSIONBATCHFILE := 'ENABLE_VERSIONING.sql';
  END IF;
END;
/
-- Transfer the value from the bind variable to the substitution variable
column mc2 new_value VERSIONBATCHFILE2 print
select :VERSIONBATCHFILE mc2 from dual;
\i &VERSIONBATCHFILE2

--// CREATE TABLES & PROCEDURES OF THE PLANNINGMANAGER
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/PL_SQL/MOSAIC/MOSAIC.sql;
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/CREATE_PLANNING_MANAGER.sql

--// geodb packages
\i C:/Users/FxK/.eclipse/3dcity_fxk1/resources/3dcitydb/postgis/CREATE_GEODB_PKG.sql
*/
