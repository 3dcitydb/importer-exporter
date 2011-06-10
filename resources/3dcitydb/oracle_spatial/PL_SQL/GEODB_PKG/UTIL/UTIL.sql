-- UTIL.sql
--
-- Authors:     Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Creates package "geodb_util" containing utility methods for applications
-- and further subpackges. Therefore, "geodb_util" might be a dependency
-- for other packages and/or methods.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.0.0     2008-09-10   release version                             CNag
--

/*****************************************************************
* TYPE STRARRAY
* 
* global type for arrays of strings, e.g. used for log messages
* and reports
******************************************************************/
set term off;
set serveroutput off;

CREATE OR REPLACE TYPE STRARRAY IS TABLE OF VARCHAR2(32767);
/

DROP TYPE DB_INFO_TABLE;
CREATE OR REPLACE TYPE DB_INFO_OBJ AS OBJECT(
  SRID NUMBER,
  GML_SRS_NAME VARCHAR2(1000),
  COORD_REF_SYS_NAME VARCHAR2(80),
  IS_COORD_REF_SYS_3D NUMBER,
  VERSIONING VARCHAR2(100)
);
/

CREATE OR REPLACE TYPE DB_INFO_TABLE IS TABLE OF DB_INFO_OBJ;
/

/*****************************************************************
* PACKAGE geodb_util
* 
* utility methods for applications and subpackages
******************************************************************/
CREATE OR REPLACE PACKAGE geodb_util
AS
  FUNCTION versioning_table(table_name VARCHAR2) RETURN VARCHAR2;
  FUNCTION versioning_db RETURN VARCHAR2;
  FUNCTION db_info RETURN DB_INFO_TABLE;
  FUNCTION error_msg(err_code VARCHAR2) RETURN VARCHAR2;
  FUNCTION split(list VARCHAR2, delim VARCHAR2 := ',') RETURN STRARRAY;
  FUNCTION min(a number, b number) return number;
  FUNCTION transform_or_null(geom MDSYS.SDO_GEOMETRY, srid number) RETURN MDSYS.SDO_GEOMETRY;
  FUNCTION is_coord_ref_sys_3d(srid NUMBER) RETURN NUMBER;
  FUNCTION is_db_coord_ref_sys_3d RETURN NUMBER;
END geodb_util;
/

CREATE OR REPLACE PACKAGE BODY geodb_util
AS
  
  /*****************************************************************
  * versioning_table
  *
  * @param table_name name of the unversioned table, i.e., omit
  *                   suffixes such as _LT
  * @return VARCHAR2 'ON' for version-enabled, 'OFF' otherwise
  ******************************************************************/
  FUNCTION versioning_table(table_name VARCHAR2) RETURN VARCHAR2
  IS
    status USER_TABLES.STATUS%TYPE;
  BEGIN
    execute immediate 'SELECT STATUS FROM USER_TABLES WHERE TABLE_NAME=:1' into status using table_name || '_LT';
    RETURN 'ON';
  EXCEPTION
    WHEN others THEN
      RETURN 'OFF';
  END; 

  /*****************************************************************
  * versioning_db
  *
  * @return VARCHAR2 'ON' for version-enabled, 'PARTLY' and 'OFF'
  ******************************************************************/
  FUNCTION versioning_db RETURN VARCHAR2
  IS
    table_names STRARRAY;
    is_versioned BOOLEAN := FALSE;
    not_versioned BOOLEAN := FALSE;
  BEGIN
    table_names := split('ADDRESS,ADDRESS_TO_BUILDING,APPEAR_TO_SURFACE_DATA,APPEARANCE,BREAKLINE_RELIEF,BUILDING,BUILDING_FURNITURE,BUILDING_INSTALLATION,CITY_FURNITURE,CITYMODEL,CITYOBJECT,CITYOBJECT_GENERICATTRIB,CITYOBJECT_MEMBER,CITYOBJECTGROUP,EXTERNAL_REFERENCE,GENERALIZATION,GENERIC_CITYOBJECT,GROUP_TO_CITYOBJECT,IMPLICIT_GEOMETRY,LAND_USE,MASSPOINT_RELIEF,OPENING,OPENING_TO_THEM_SURFACE,PLANT_COVER,RELIEF_COMPONENT,RELIEF_FEAT_TO_REL_COMP,RELIEF_FEATURE,ROOM,SOLITARY_VEGETAT_OBJECT,SURFACE_DATA,SURFACE_GEOMETRY,TEXTUREPARAM,THEMATIC_SURFACE,TIN_RELIEF,TRAFFIC_AREA,TRANSPORTATION_COMPLEX,WATERBOD_TO_WATERBND_SRF,WATERBODY,WATERBOUNDARY_SURFACE');
  
    FOR i IN table_names.first .. table_names.last LOOP
      IF versioning_table(table_names(i)) = 'ON' THEN
        is_versioned := TRUE;
      ELSE
        not_versioned := TRUE;
      END IF;
      
    END LOOP;

    IF is_versioned AND NOT not_versioned THEN
      RETURN 'ON';
    ELSIF is_versioned AND not_versioned THEN
      RETURN 'PARTLY';
    ELSE
      RETURN 'OFF';
    END IF;
  END;  
  
  /*****************************************************************
  * error_msg
  *
  * @param srid database srid
  * @param srs database srs name
  * @param versioning database versioning
  ******************************************************************/
  FUNCTION db_info RETURN DB_INFO_TABLE 
  IS
    info_ret DB_INFO_TABLE;
    info_tmp DB_INFO_OBJ;
  BEGIN
    info_ret := DB_INFO_TABLE();
    info_ret.extend;
  
    info_tmp := DB_INFO_OBJ(0, NULL, NULL, 0, NULL);

    execute immediate 'SELECT SRID, GML_SRS_NAME from DATABASE_SRS' into info_tmp.srid, info_tmp.gml_srs_name;   
    execute immediate 'SELECT COORD_REF_SYS_NAME from SDO_COORD_REF_SYS where SRID=:1' into info_tmp.coord_ref_sys_name using info_tmp.srid;
    info_tmp.is_coord_ref_sys_3d := is_coord_ref_sys_3d(info_tmp.srid);
    info_tmp.versioning := versioning_db;     
       
    info_ret(info_ret.count) := info_tmp;
    return info_ret;
  END;
  
  /*****************************************************************
  * error_msg
  *
  * @param err_code Oracle SQL error code, usually starting with '-',
  *                 e.g. '-06404'
  * @return VARCHAR2 corresponding Oracle SQL error message                 
  ******************************************************************/
  FUNCTION error_msg(err_code VARCHAR2) RETURN VARCHAR2
  IS
  BEGIN
    RETURN SQLERRM(err_code);
  END;
  
  /*****************************************************************
  * split
  *
  * @param list string to be splitted
  * @param delim delimiter used for splitting, defaults to ','
  * @return STRARRAY array of strings containing split tokens                 
  ******************************************************************/
  FUNCTION split(list VARCHAR2, delim VARCHAR2 := ',') RETURN STRARRAY
  IS
    results STRARRAY := STRARRAY();
    idx pls_integer;
    tmp_list VARCHAR2(32767) := list;
  BEGIN
    LOOP
      idx := instr(tmp_list,delim);
      IF idx > 0 THEN
        results.extend;
        results(results.count) := substr(tmp_list, 1, idx-1);
        tmp_list := substr(tmp_list, idx + length(delim));
      ELSE
        results.extend;
        results(results.count) := tmp_list;
        EXIT;
      END IF;
    END LOOP;
    
    RETURN results;
  END;
  
  /*****************************************************************
  * min
  *
  * @param a first number value
  * @param b second number value
  * @return NUMBER the smaller of the two input number values                
  ******************************************************************/
  FUNCTION min(a NUMBER, b NUMBER) RETURN NUMBER
  IS
  BEGIN
    IF a < b THEN
      RETURN a;
    ELSE
      RETURN b;
    END IF;
  END;
  
  /*****************************************************************
  * transform_or_null
  *
  * @param geom the geometry whose representation is to be transformed using another coordinate system 
  * @param srid the SRID of the coordinate system to be used for the transformation.
  * @return MDSYS.SDO_GEOMETRY the transformed geometry representation                
  ******************************************************************/
  FUNCTION transform_or_null(geom MDSYS.SDO_GEOMETRY, srid number) RETURN MDSYS.SDO_GEOMETRY
  IS
  BEGIN
    IF geom is not NULL THEN
      RETURN SDO_CS.TRANSFORM(geom, srid);
    ELSE
      RETURN NULL;
    END IF;
  END;  
  
  /*****************************************************************
  * is_coord_ref_sys_3d
  *
  * @param srid the SRID of the coordinate system to be checked
  * @return NUMBER the boolean result encoded as number: 0 = false, 1 = true                
  ******************************************************************/
  FUNCTION is_coord_ref_sys_3d(srid NUMBER) RETURN NUMBER
  IS
    is_3d number := 0;
  BEGIN
    execute immediate 'SELECT COUNT(*) from SDO_CRS_COMPOUND where SRID=:1' into is_3d using srid;
    if is_3d = 0 then
      execute immediate 'SELECT COUNT(*) from SDO_CRS_GEOGRAPHIC3D where SRID=:1' into is_3d using srid;
    end if;
    
    return is_3d;
  END;
  
  /*****************************************************************
  * is_db_coord_ref_sys_3d
  *
  * @return NUMBER the boolean result encoded as number: 0 = false, 1 = true                
  ******************************************************************/
  FUNCTION is_db_coord_ref_sys_3d RETURN NUMBER
  IS
    srid number;
  BEGIN
    execute immediate 'SELECT srid from DATABASE_SRS' into srid;
    return is_coord_ref_sys_3d(srid);
  END;
  
END geodb_util;
/