-- CREATE_DB2.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stroh@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008, Institute for Geodesy and Geoinformation Science,
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
-- 2.0.1     2008-06-28   versioning is enabled depending on var      TKol
-- 2.0.0     2007-11-23   release version                             TKol
--                                                                    GKoe
--                                                                    CNag
--                                                                    ASta
--
SET SERVEROUTPUT ON
SET FEEDBACK ON

VARIABLE VERSIONBATCHFILE VARCHAR2(30);

-- This script is called from CREATE_DB.sql and it
-- is required that the three substitution variables
-- &SRSNO, &GMLSRSNAME, and &VERSIONING are set properly.

--//CREATE TABLES
@@DATABASE_SRS.sql

INSERT INTO DATABASE_SRS(SRID,GML_SRS_NAME) VALUES (&SRSNO,'&GMLSRSNAME');
COMMIT;

@@CITYOBJECT/CITYMODEL.sql
@@CITYOBJECT/CITYOBJECT.sql
@@CITYOBJECT/CITYOBJECT_MEMBER.sql
@@CITYOBJECT/EXTERNAL_REFERENCE.sql
@@CITYOBJECT/GENERALIZATION.sql
@@CITYOBJECT/IMPLICIT_GEOMETRY.sql
@@CITYOBJECT/OBJECTCLASS.sql

@@GEOMETRY/SURFACE_GEOMETRY.sql

@@CITYFURNITURE/CITY_FURNITURE.sql

@@GENERICS/CITYOBJECT_GENERICATTRIB.sql
@@GENERICS/GENERIC_CITYOBJECT.sql

@@CITYOBJECTGROUP/CITYOBJECTGROUP.sql
@@CITYOBJECTGROUP/GROUP_TO_CITYOBJECT.sql

@@BUILDING/ADDRESS.sql
@@BUILDING/ADDRESS_TO_BUILDING.sql
@@BUILDING/BUILDING.sql
@@BUILDING/BUILDING_FURNITURE.sql
@@BUILDING/BUILDING_INSTALLATION.sql
@@BUILDING/OPENING.sql
@@BUILDING/OPENING_TO_THEM_SURFACE.sql
@@BUILDING/ROOM.sql
@@BUILDING/THEMATIC_SURFACE.sql

@@APPEARANCE/APPEARANCE.sql
@@APPEARANCE/SURFACE_DATA.sql
@@APPEARANCE/TEXTUREPARAM.sql
@@APPEARANCE/APPEAR_TO_SURFACE_DATA.sql

@@DTM/BREAKLINE_RELIEF.sql
@@DTM/MASSPOINT_RELIEF.sql
@@DTM/RASTER_RELIEF.sql
@@DTM/RASTER_RELIEF_IMP.sql
@@DTM/RASTER_RELIEF_IMP_RDT.sql
@@DTM/RASTER_RELIEF_RDT.sql
@@DTM/RELIEF.sql
@@DTM/RELIEF_COMPONENT.sql
@@DTM/RELIEF_FEAT_TO_REL_COMP.sql
@@DTM/RELIEF_FEATURE.sql
@@DTM/TIN_RELIEF.sql

@@ORTHOPHOTO/ORTHOPHOTO_RDT.sql;
@@ORTHOPHOTO/ORTHOPHOTO.sql;
@@ORTHOPHOTO/ORTHOPHOTO_RDT_IMP.sql;
@@ORTHOPHOTO/ORTHOPHOTO_IMP.sql;

@@TRANSPORTATION/TRANSPORTATION_COMPLEX.sql
@@TRANSPORTATION/TRAFFIC_AREA.sql

@@LANDUSE/LAND_USE.sql

@@VEGETATION/PLANT_COVER.sql
@@VEGETATION/SOLITARY_VEGETAT_OBJECT.sql

@@WATERBODY/WATERBODY.sql
@@WATERBODY/WATERBOD_TO_WATERBND_SRF.sql
@@WATERBODY/WATERBOUNDARY_SURFACE.sql

--// CREATE SEQUENCES
@@CITYOBJECT/CITYMODEL_SEQ.sql
@@CITYOBJECT/CITYOBJECT_SEQ.sql
@@CITYOBJECT/EXTERNAL_REF_SEQ.sql
@@CITYOBJECT/IMPLICIT_GEOMETRY_SEQ.sql

@@GEOMETRY/SURFACE_GEOMETRY_SEQ.sql

@@GENERICS/CITYOBJECT_GENERICATT_SEQ.sql

@@BUILDING/ADDRESS_SEQ.sql

@@APPEARANCE/APPEARANCE_SEQ.sql
@@APPEARANCE/SURFACE_DATA_SEQ.sql

@@DTM/DTM_SEQ.sql

@@ORTHOPHOTO/ORTHOPHOTO_SEQ.sql

--// Activate Constraints
@@ADD_CONSTRAINTS.sql

--// BUILD SIMPLE INDEXES
@@BUILD_SIMPLE_INDEX.sql

--// CREATE SPATIAL INDEX
@@SPATIAL_INDEX.sql

@@OBJECTCLASS_INSTANCES.sql
@@IMPORT_PROCEDURES.sql
@@DUMMY_IMPORT.sql

--// (possibly) activate versioning
BEGIN
  :VERSIONBATCHFILE := 'DO_NOTHING.sql';
END;
/
BEGIN
  IF ('&VERSIONING'='yes' OR '&VERSIONING'='YES' OR '&VERSIONING'='y' OR '&VERSIONING'='Y') THEN
    :VERSIONBATCHFILE := 'ENABLEVERSIONING.sql';
  END IF;
END;
/
-- Transfer the value from the bind variable to the substitution variable
column mc2 new_value VERSIONBATCHFILE2 print
select :VERSIONBATCHFILE mc2 from dual;
@@&VERSIONBATCHFILE2


--// DML TRIGGER FOR RASTER TABLES
@@TRIGGER.sql;

@@MOSAIC.sql;

--// CREATE TABLES & PROCEDURES OF THE PLANNINGMANAGER
@@CREATE_PLANNINGMANAGER.sql

--// Tools and utilities
@@GEODB_REPORT.sql
@@GEODB_PKG/CREATE_GEODB_PKG.sql

SHOW ERRORS;
COMMIT;

SELECT 'DB creation complete!' as message from DUAL;
