-- CREATE_RO_USER.sql
--
-- Authors:     Javier Herreruela <javier.herreruela@tu-berlin.de>
--              Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Copyright:   (c) 2007-2012, Institute for Geodesy and Geoinformation Science,
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
-- 2.0.6     2010-06-03   bugfix for execute object rights in 2.0.6   JHer
-- 2.0.3     2010-06-03   release version                             JHer
--                                                                    CNag
--

SET SERVEROUTPUT ON;
-- SET FEEDBACK ON

prompt
prompt
accept RO_USERNAME CHAR PROMPT 'Please enter a username for the read-only user: '
accept SCHEMA_OWNER CHAR PROMPT 'Please enter the owner of the schema to which this user will have read-only access: '
prompt
prompt

DECLARE
 v_schemaOwnerName ALL_USERS.USERNAME%TYPE := null;
 v_readOnlyName ALL_USERS.USERNAME%TYPE := null;
 v_role DBA_ROLES.ROLE%TYPE := null;
 
 RO_USER_ALREADY_EXISTS EXCEPTION;

BEGIN

  IF ('&RO_USERNAME' IS NULL) THEN 
    dbms_output.put_line('Invalid username!');
  END IF;

  IF ('&SCHEMA_OWNER' IS NULL) THEN
    dbms_output.put_line('Invalid schema owner!');
  END IF;

  BEGIN  
    SELECT USERNAME INTO v_schemaOwnerName FROM ALL_USERS WHERE USERNAME = UPPER('&SCHEMA_OWNER');

  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      dbms_output.put_line('Schema owner ' || '&SCHEMA_OWNER' || ' does not exist!');
      RAISE;
  END;

  BEGIN  
    SELECT USERNAME INTO v_readOnlyName FROM ALL_USERS WHERE USERNAME = UPPER('&RO_USERNAME');
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      NULL; -- do nothing, read-only user must not exist
    WHEN OTHERS THEN
      RAISE;
  END;

  IF (v_readOnlyName IS NOT NULL) THEN
    RAISE RO_USER_ALREADY_EXISTS;
  END IF;

  v_readOnlyName := '&RO_USERNAME';
  EXECUTE IMMEDIATE 'create user ' || v_readOnlyName || ' identified by berlin3d PASSWORD EXPIRE';

  BEGIN
    SELECT ROLE INTO v_role FROM DBA_ROLES WHERE ROLE = UPPER('&SCHEMA_OWNER') || '_READ_ONLY';
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      v_role := UPPER('&SCHEMA_OWNER') || '_READ_ONLY';
      EXECUTE IMMEDIATE 'create role ' || v_role;

      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.ADDRESS to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.ADDRESS_TO_BUILDING to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.APPEARANCE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.APPEAR_TO_SURFACE_DATA to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.BREAKLINE_RELIEF to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.BUILDING to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.BUILDING_FURNITURE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.BUILDING_INSTALLATION to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITYMODEL to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITYOBJECT to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITYOBJECTGROUP to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITYOBJECT_GENERICATTRIB to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITYOBJECT_MEMBER to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITY_FURNITURE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITY_MODEL_ASPECT to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.CITY_MODEL_ASPECT_COMPONENT to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.DATABASE_SRS to ' || v_role;
      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.DB_INFO_TABLE to ' || v_role;
      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.DB_INFO_OBJ to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.EXTERNAL_REFERENCE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.GENERALIZATION to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.GENERIC_CITYOBJECT to ' || v_role;
      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_IDX to ' || v_role;
      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_STAT to ' || v_role;
      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_UTIL to ' || v_role;
--      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_DELETE_BY_LINEAGE to ' || v_role;
--      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_MATCH to ' || v_role;
--      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_DELETE to ' || v_role;
--      EXECUTE IMMEDIATE 'grant execute on ' || UPPER('&SCHEMA_OWNER') || '.GEODB_MERGE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.GROUP_TO_CITYOBJECT to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.IMPLICIT_GEOMETRY to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.IMPORT_PROCEDURES to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.LAND_USE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.MASSPOINT_RELIEF to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.OBJECTCLASS to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.OPENING to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.OPENING_TO_THEM_SURFACE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.ORTHOPHOTO to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.ORTHOPHOTO_IMP to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.PLANNING to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.PLANNING_ALTERNATIVE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.PLANT_COVER to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.RASTER_RELIEF to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.RASTER_RELIEF_IMP to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.RELIEF to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.RELIEF_COMPONENT to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.RELIEF_FEATURE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.RELIEF_FEAT_TO_REL_COMP to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.ROOM to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.SOLITARY_VEGETAT_OBJECT to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.SURFACE_DATA to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.SURFACE_GEOMETRY to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.TEXTUREPARAM to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.THEMATIC_SURFACE to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.TIN_RELIEF to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.TRAFFIC_AREA to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.TRANSPORTATION_COMPLEX to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.WATERBODY to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.WATERBOD_TO_WATERBND_SRF to ' || v_role;
      EXECUTE IMMEDIATE 'grant select on ' || UPPER('&SCHEMA_OWNER') || '.WATERBOUNDARY_SURFACE to ' || v_role;

    WHEN OTHERS THEN
      RAISE;
  END;

  EXECUTE IMMEDIATE 'grant ' || v_role || ' to ' || v_readOnlyName;
  EXECUTE IMMEDIATE 'grant CONNECT to ' || v_readOnlyName;
  EXECUTE IMMEDIATE 'grant RESOURCE to ' || v_readOnlyName;

-- synonyms for tables
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MATCH_RESULT_RELEVANT for ' || v_schemaOwnerName || '.MATCH_RESULT_RELEVANT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MATCH_TMP_BUILDING for ' || v_schemaOwnerName || '.MATCH_TMP_BUILDING';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.PLANNING_ALTERNATIVE for ' || v_schemaOwnerName || '.PLANNING_ALTERNATIVE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.PLANNING for ' || v_schemaOwnerName || '.PLANNING';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITY_MODEL_ASPECT_COMPONENT for ' || v_schemaOwnerName || '.CITY_MODEL_ASPECT_COMPONENT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITY_MODEL_ASPECT for ' || v_schemaOwnerName || '.CITY_MODEL_ASPECT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.IMPORT_PROCEDURES for ' || v_schemaOwnerName || '.IMPORT_PROCEDURES';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.WATERBOUNDARY_SURFACE for ' || v_schemaOwnerName || '.WATERBOUNDARY_SURFACE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.WATERBOD_TO_WATERBND_SRF for ' || v_schemaOwnerName || '.WATERBOD_TO_WATERBND_SRF';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.WATERBODY for ' || v_schemaOwnerName || '.WATERBODY';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.SOLITARY_VEGETAT_OBJECT for ' || v_schemaOwnerName || '.SOLITARY_VEGETAT_OBJECT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.PLANT_COVER for ' || v_schemaOwnerName || '.PLANT_COVER';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.LAND_USE for ' || v_schemaOwnerName || '.LAND_USE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.TRAFFIC_AREA for ' || v_schemaOwnerName || '.TRAFFIC_AREA';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.TRANSPORTATION_COMPLEX for ' || v_schemaOwnerName || '.TRANSPORTATION_COMPLEX';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.ORTHOPHOTO_IMP for ' || v_schemaOwnerName || '.ORTHOPHOTO_IMP';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.ORTHOPHOTO for ' || v_schemaOwnerName || '.ORTHOPHOTO';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.TIN_RELIEF for ' || v_schemaOwnerName || '.TIN_RELIEF';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.RELIEF_FEATURE for ' || v_schemaOwnerName || '.RELIEF_FEATURE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.RELIEF_FEAT_TO_REL_COMP for ' || v_schemaOwnerName || '.RELIEF_FEAT_TO_REL_COMP';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.RELIEF_COMPONENT for ' || v_schemaOwnerName || '.RELIEF_COMPONENT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.RELIEF for ' || v_schemaOwnerName || '.RELIEF';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.BUILDING for ' || v_schemaOwnerName || '.BUILDING';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.ADDRESS_TO_BUILDING for ' || v_schemaOwnerName || '.ADDRESS_TO_BUILDING';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.ADDRESS for ' || v_schemaOwnerName || '.ADDRESS';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GROUP_TO_CITYOBJECT for ' || v_schemaOwnerName || '.GROUP_TO_CITYOBJECT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITYOBJECTGROUP for ' || v_schemaOwnerName || '.CITYOBJECTGROUP';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GENERIC_CITYOBJECT for ' || v_schemaOwnerName || '.GENERIC_CITYOBJECT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITYOBJECT_GENERICATTRIB for ' || v_schemaOwnerName || '.CITYOBJECT_GENERICATTRIB';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITY_FURNITURE for ' || v_schemaOwnerName || '.CITY_FURNITURE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.SURFACE_GEOMETRY for ' || v_schemaOwnerName || '.SURFACE_GEOMETRY';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.OBJECTCLASS for ' || v_schemaOwnerName || '.OBJECTCLASS';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.IMPLICIT_GEOMETRY for ' || v_schemaOwnerName || '.IMPLICIT_GEOMETRY';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GENERALIZATION for ' || v_schemaOwnerName || '.GENERALIZATION';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.EXTERNAL_REFERENCE for ' || v_schemaOwnerName || '.EXTERNAL_REFERENCE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITYOBJECT_MEMBER for ' || v_schemaOwnerName || '.CITYOBJECT_MEMBER';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITYOBJECT for ' || v_schemaOwnerName || '.CITYOBJECT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CITYMODEL for ' || v_schemaOwnerName || '.CITYMODEL';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.DATABASE_SRS for ' || v_schemaOwnerName || '.DATABASE_SRS';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.RASTER_RELIEF_IMP for ' || v_schemaOwnerName || '.RASTER_RELIEF_IMP';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.RASTER_RELIEF for ' || v_schemaOwnerName || '.RASTER_RELIEF';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MASSPOINT_RELIEF for ' || v_schemaOwnerName || '.MASSPOINT_RELIEF';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.BREAKLINE_RELIEF for ' || v_schemaOwnerName || '.BREAKLINE_RELIEF';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.APPEAR_TO_SURFACE_DATA for ' || v_schemaOwnerName || '.APPEAR_TO_SURFACE_DATA';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.TEXTUREPARAM for ' || v_schemaOwnerName || '.TEXTUREPARAM';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.SURFACE_DATA for ' || v_schemaOwnerName || '.SURFACE_DATA';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.APPEARANCE for ' || v_schemaOwnerName || '.APPEARANCE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.THEMATIC_SURFACE for ' || v_schemaOwnerName || '.THEMATIC_SURFACE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.ROOM for ' || v_schemaOwnerName || '.ROOM';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.OPENING_TO_THEM_SURFACE for ' || v_schemaOwnerName || '.OPENING_TO_THEM_SURFACE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.OPENING for ' || v_schemaOwnerName || '.OPENING';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.BUILDING_INSTALLATION for ' || v_schemaOwnerName || '.BUILDING_INSTALLATION';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.BUILDING_FURNITURE for ' || v_schemaOwnerName || '.BUILDING_FURNITURE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MATCH_RESULT for ' || v_schemaOwnerName || '.MATCH_RESULT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.CONTAINER_IDS for ' || v_schemaOwnerName || '.CONTAINER_IDS';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.COLLECT_GEOM for ' || v_schemaOwnerName || '.COLLECT_GEOM';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MATCH_ALLOCATE_GEOM for ' || v_schemaOwnerName || '.MATCH_ALLOCATE_GEOM';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MATCH_CAND_AGGR_GEOM for ' || v_schemaOwnerName || '.MATCH_CAND_AGGR_GEOM';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.MATCH_MASTER_AGGR_GEOM for ' || v_schemaOwnerName || '.MATCH_MASTER_AGGR_GEOM';

-- synonyms for PL/SQL packages
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_DELETE for ' || v_schemaOwnerName || '.GEODB_DELETE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_DELETE_BY_LINEAGE for ' || v_schemaOwnerName || '.GEODB_DELETE_BY_LINEAGE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_IDX for ' || v_schemaOwnerName || '.GEODB_IDX';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_MATCH for ' || v_schemaOwnerName || '.GEODB_MATCH';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_MERGE for ' || v_schemaOwnerName || '.GEODB_MERGE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_STAT for ' || v_schemaOwnerName || '.GEODB_STAT';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.GEODB_UTIL for ' || v_schemaOwnerName || '.GEODB_UTIL';

-- synonyms for user defined object types
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.INDEX_OBJ for ' || v_schemaOwnerName || '.INDEX_OBJ';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.STRARRAY for ' || v_schemaOwnerName || '.STRARRAY';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.DB_INFO_TABLE for ' || v_schemaOwnerName || '.DB_INFO_TABLE';
  EXECUTE IMMEDIATE 'create or replace synonym ' || v_readOnlyName || '.DB_INFO_OBJ for ' || v_schemaOwnerName || '.DB_INFO_OBJ';

  COMMIT;
  dbms_output.put_line(' ');
  dbms_output.put_line('create_ro_user.sql finished successfully');

EXCEPTION
  WHEN RO_USER_ALREADY_EXISTS THEN
    dbms_output.put_line(' ');
    dbms_output.put_line('User ' || '&RO_USERNAME' || ' already exists!');
    dbms_output.put_line('create_ro_user.sql finished with errors');
  WHEN OTHERS THEN
    dbms_output.put_line(' ');
    dbms_output.put_line('create_ro_user.sql finished with errors');
END;
/
