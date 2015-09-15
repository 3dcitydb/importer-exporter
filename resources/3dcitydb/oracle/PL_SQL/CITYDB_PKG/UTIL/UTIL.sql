-- UTIL.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2012-2015  Chair of Geoinformatics,
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
-- Creates package "citydb_util" containing utility methods for applications
-- and further subpackges. Therefore, "citydb_util" might be a dependency
-- for other packages and/or methods.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 2.1.0     2015-07-21   added string2id_array function              FKun
-- 2.0.0     2014-10-10   new version for 3DCityDB V3                 FKun
-- 1.2.0     2013-08-29   added change_db_srid procedure              FKun
-- 1.1.0     2011-07-28   update to 2.0.6                             CNag
-- 1.0.0     2008-09-10   release version                             CNag
--

--SET term OFF;
--SET serveroutput OFF;

/*****************************************************************
* TYPE STRARRAY
*
* global type for arrays of strings, e.g. used for log messages
* and reports
******************************************************************/
CREATE OR REPLACE TYPE STRARRAY IS TABLE OF VARCHAR2(32767);
/

/*****************************************************************
* TYPE ID_ARRAY
*
* global type for arrays of number values
******************************************************************/
CREATE OR REPLACE TYPE ID_ARRAY IS TABLE OF NUMBER;
/

/*****************************************************************
* TYPE DB_VERSION_OBJ and DB_VERSION_TABLE
* 
* global type for database version information
******************************************************************/
CREATE OR REPLACE TYPE DB_VERSION_OBJ AS OBJECT(
  VERSION VARCHAR2(10),
  MAJOR_VERSION NUMBER,
  MINOR_VERSION NUMBER,
  MINOR_REVISION NUMBER
);
/

CREATE OR REPLACE TYPE DB_VERSION_TABLE IS TABLE OF DB_VERSION_OBJ;
/

/*****************************************************************
* TYPE DB_INFO_OBJ and DB_INFO_TABLE
* 
* global type for database metadata
******************************************************************/
CREATE OR REPLACE TYPE DB_INFO_OBJ AS OBJECT(
  SCHEMA_SRID NUMBER,
  SCHEMA_GML_SRS_NAME VARCHAR2(1000),
  COORD_REF_SYS_NAME VARCHAR2(80),
  COORD_REF_SYS_KIND VARCHAR2(24),
  WKTEXT VARCHAR2(4000),
  VERSIONING VARCHAR2(100)
);
/

CREATE OR REPLACE TYPE DB_INFO_TABLE IS TABLE OF DB_INFO_OBJ;
/


/*****************************************************************
* PACKAGE citydb_util
* 
* utility methods for applications and subpackages
******************************************************************/
CREATE OR REPLACE PACKAGE citydb_util
AS
  FUNCTION citydb_version RETURN DB_VERSION_TABLE;
  FUNCTION versioning_table(table_name VARCHAR2, schema_name VARCHAR2 := USER) RETURN VARCHAR2;
  FUNCTION versioning_db(schema_name VARCHAR2 := USER) RETURN VARCHAR2;
  PROCEDURE db_info(schema_srid OUT DATABASE_SRS.SRID%TYPE, schema_gml_srs_name OUT DATABASE_SRS.GML_SRS_NAME%TYPE, versioning OUT VARCHAR2);
  FUNCTION db_metadata RETURN DB_INFO_TABLE;
  FUNCTION split(list VARCHAR2, delim VARCHAR2 := ',') RETURN STRARRAY;
  FUNCTION min(a NUMBER, b NUMBER) RETURN NUMBER;
  PROCEDURE update_schema_constraints(on_delete_param VARCHAR2 := 'CASCADE', schema_name VARCHAR2 := USER);
  PROCEDURE update_table_constraint(fkey_name VARCHAR2, table_name VARCHAR2, column_name VARCHAR2, ref_table VARCHAR2, ref_column VARCHAR2, on_delete_param VARCHAR2 := 'CASCADE', schema_name VARCHAR2 := USER);
  FUNCTION get_seq_values(seq_name VARCHAR2, seq_count NUMBER, schema_name VARCHAR2 := USER) RETURN ID_ARRAY;
  FUNCTION string2id_array(str VARCHAR2, delim VARCHAR2 := ',') RETURN ID_ARRAY;
  FUNCTION get_id_array_size(id_arr ID_ARRAY) RETURN NUMBER;
  FUNCTION objectclass_id_to_table_name(class_id NUMBER) RETURN VARCHAR2;
  FUNCTION construct_solid(geom_root_id NUMBER, schema_name VARCHAR2 := USER) RETURN SDO_GEOMETRY;
  FUNCTION to_2d(geom MDSYS.SDO_GEOMETRY, srid NUMBER) RETURN MDSYS.SDO_GEOMETRY;
  FUNCTION sdo2geojson3d(p_geometry in sdo_geometry, p_decimal_places in pls_integer default 2, p_compress_tags in pls_integer default 0, p_relative2mbr in pls_integer default 0) RETURN CLOB DETERMINISTIC;
END citydb_util;
/

CREATE OR REPLACE PACKAGE BODY citydb_util
AS
  type ref_cursor is ref cursor;
  
  /*****************************************************************
  * citydb_version
  *
  ******************************************************************/
  FUNCTION citydb_version RETURN DB_VERSION_TABLE
  IS
    version_ret DB_VERSION_TABLE;
    version_tmp DB_VERSION_OBJ;
  BEGIN
    version_ret := DB_VERSION_TABLE();
    version_ret.extend;

    version_tmp := DB_VERSION_OBJ('3.0.0', 3, 0, 0);

    version_ret(version_ret.count) := version_tmp;
    RETURN version_ret;
  END;

  /*****************************************************************
  * versioning_table
  *
  * @param table_name name of the unversioned table, i.e., omit
  *                   suffixes such as _LT
  * @param schema_name name of schema of target table
  * @return VARCHAR2 'ON' for version-enabled, 'OFF' otherwise
  ******************************************************************/
  FUNCTION versioning_table(
    table_name VARCHAR2, 
    schema_name VARCHAR2 := USER
    ) RETURN VARCHAR2
  IS
    status USER_TABLES.STATUS%TYPE;
  BEGIN
    EXECUTE IMMEDIATE 'SELECT status FROM all_tables WHERE owner=:1 AND table_name=:2' INTO status USING upper(schema_name), upper(table_name) || '_LT';
    RETURN 'ON';
  EXCEPTION
    WHEN others THEN
      RETURN 'OFF';
  END; 

  /*****************************************************************
  * versioning_db
  *
  * @param schema_name name of schema
  * @return VARCHAR2 'ON' for version-enabled, 'PARTLY' and 'OFF'
  ******************************************************************/
  FUNCTION versioning_db(schema_name VARCHAR2 := USER) RETURN VARCHAR2
  IS
    table_names STRARRAY;
    is_versioned BOOLEAN := FALSE;
    not_versioned BOOLEAN := FALSE;
  BEGIN
    table_names := split('ADDRESS,ADDRESS_TO_BRIDGE,ADDRESS_TO_BUILDING,APPEAR_TO_SURFACE_DATA,APPEARANCE,BREAKLINE_RELIEF,BRIDGE,BRIDGE_CONSTR_ELEMENT,BRIDGE_FURNITURE,BRIDGE_INSTALLATION,BRIDGE_OPEN_TO_THEM_SRF,BRIDGE_OPENING,BRIDGE_ROOM,BRIDGE_THEMATIC_SURFACE,BUILDING,BUILDING_FURNITURE,BUILDING_INSTALLATION,CITY_FURNITURE,CITYMODEL,CITYOBJECT,CITYOBJECT_GENERICATTRIB,CITYOBJECT_MEMBER,CITYOBJECTGROUP,EXTERNAL_REFERENCE,GENERALIZATION,GENERIC_CITYOBJECT,GROUP_TO_CITYOBJECT,IMPLICIT_GEOMETRY,LAND_USE,MASSPOINT_RELIEF,OPENING,OPENING_TO_THEM_SURFACE,PLANT_COVER,GRID_COVERAGE_RDT,RASTER_RELIEF,RELIEF_COMPONENT,RELIEF_FEAT_TO_REL_COMP,RELIEF_FEATURE,ROOM,SOLITARY_VEGETAT_OBJECT,SURFACE_DATA,SURFACE_GEOMETRY,TEX_IMAGE,TEXTUREPARAM,THEMATIC_SURFACE,TIN_RELIEF,TRAFFIC_AREA,TRANSPORTATION_COMPLEX,TUNNEL,TUNNEL_FURNITURE,TUNNEL_HOLLOW_SPACE,TUNNEL_INSTALLATION,TUNNEL_OPEN_TO_THEM_SRF,TUNNEL_OPENING,TUNNEL_THEMATIC_SURFACE,WATERBOD_TO_WATERBND_SRF,WATERBODY,WATERBOUNDARY_SURFACE');

    FOR i IN table_names.first .. table_names.last LOOP
      IF versioning_table(table_names(i), schema_name) = 'ON' THEN
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
  * db_info
  *
  * @param schema_srid database srid
  * @param schema_gml_srs_name database srs name
  * @param versioning database versioning
  * @param schema_name name of user schema
  ******************************************************************/
  PROCEDURE db_info(schema_srid OUT DATABASE_SRS.SRID%TYPE, schema_gml_srs_name OUT DATABASE_SRS.GML_SRS_NAME%TYPE, versioning OUT VARCHAR2) 
  IS
  BEGIN
    EXECUTE IMMEDIATE 'SELECT SRID, GML_SRS_NAME from DATABASE_SRS' INTO schema_srid, schema_gml_srs_name;
    versioning := versioning_db(USER);
  END;

  /*****************************************************************
  * db_metadata
  *
  ******************************************************************/
  FUNCTION db_metadata RETURN DB_INFO_TABLE 
  IS
    info_ret DB_INFO_TABLE;
    info_tmp DB_INFO_OBJ;
  BEGIN
    info_ret := DB_INFO_TABLE();
    info_ret.extend;

    info_tmp := DB_INFO_OBJ(0, NULL, NULL, 0, NULL, NULL);

    EXECUTE IMMEDIATE 'select SRID, GML_SRS_NAME from DATABASE_SRS' INTO info_tmp.schema_srid, info_tmp.schema_gml_srs_name;
    EXECUTE IMMEDIATE 'select COORD_REF_SYS_NAME, COORD_REF_SYS_KIND from SDO_COORD_REF_SYS where SRID=:1' INTO info_tmp.coord_ref_sys_name, info_tmp.coord_ref_sys_kind USING info_tmp.schema_srid;
    EXECUTE IMMEDIATE 'select nvl(WKTEXT3D, WKTEXT) from CS_SRS where SRID=:1' INTO info_tmp.wktext USING info_tmp.schema_srid;
    info_tmp.versioning := versioning_db;
    info_ret(info_ret.count) := info_tmp;
    RETURN info_ret;
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
    idx PLS_INTEGER;
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

  /******************************************************************
  * update_table_constraint
  *
  * Removes a constraint to add it again with parameters
  * ON UPDATE CASCADE ON DELETE CASCADE or RESTRICT
  *
  * @param fkey_name name of the foreign key that is updated 
  * @param table_name defines the table to which the constraint belongs to
  * @param column_name defines the column the constraint is relying on
  * @param ref_table name of referenced table
  * @param ref_column name of referencing column of referenced table
  * @param on_delete_param whether CASCADE or RESTRICT
  * @param schema_name name of schema of target constraints
  ******************************************************************/
  PROCEDURE update_table_constraint(
    fkey_name VARCHAR2,
    table_name VARCHAR2,
    column_name VARCHAR2,
    ref_table VARCHAR2,
    ref_column VARCHAR2,
    on_delete_param VARCHAR2 := 'CASCADE',
    schema_name VARCHAR2 := USER
    )
  IS
  BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE ' || upper(schema_name) || '.' || table_name || ' DROP CONSTRAINT ' || fkey_name;
    EXECUTE IMMEDIATE 'ALTER TABLE ' || upper(schema_name) || '.' || table_name || ' ADD CONSTRAINT ' || fkey_name || 
                         ' FOREIGN KEY (' || column_name || ') REFERENCES ' || upper(schema_name) || '.' || ref_table || '(' || ref_column || ')'
                         || on_delete_param;
    EXCEPTION
      WHEN OTHERS THEN
        dbms_output.put_line('Error on constraint ' || fkey_name || ': ' || SQLERRM);
  END;

  /******************************************************************
  * update_schema_constraints
  *
  * calls update_table_constraint for updating all the constraints
  * in the user schema
  *
  * @param on_delete_param whether CASCADE (default) or RESTRICT
  * @param schema_name name of schema of target constraints
  ******************************************************************/
  PROCEDURE update_schema_constraints(
    on_delete_param VARCHAR2 := 'CASCADE',
    schema_name VARCHAR2 := USER
    )
  IS
    delete_param VARCHAR2(30) := 'ON DELETE CASCADE';
    deferrable_param VARCHAR2(30);
  BEGIN
    IF on_delete_param <> 'CASCADE' THEN
      delete_param := '';
      dbms_output.put_line('Constraints are set to ON DELETE RESTRICT');
    ELSE
      dbms_output.put_line('Constraints are set to ON DELETE CASCADE');
    END IF;

    FOR rec IN (SELECT acc1.constraint_name AS fkey, acc1.table_name AS t, acc1.column_name AS c, 
                  ac2.table_name AS ref_t, acc2.column_name AS ref_c, acc1.owner AS schema
                FROM all_cons_columns acc1
                JOIN all_constraints ac1 ON acc1.owner = ac1.owner 
                  AND acc1.constraint_name = ac1.constraint_name
                JOIN all_constraints ac2 ON ac1.r_owner = ac2.owner 
                  AND ac1.r_constraint_name = ac2.constraint_name
                JOIN all_cons_columns acc2 ON ac2.owner = acc2.owner 
                  AND ac2.constraint_name = acc2.constraint_name 
                  AND acc2.position = acc1.position     
                WHERE acc1.owner = upper(schema_name) AND ac1.constraint_type = 'R') LOOP
      update_table_constraint(rec.fkey, rec.t, rec.c, rec.ref_t, rec.ref_c, delete_param, schema_name);
    END LOOP;
  END;

  /*****************************************************************
  * get_seq_values
  *
  * @param seq_name name of the sequence
  * @param count number of values to be queried from the sequence
  * @param schema_name name of schema of target sequence
  * @return ID_ARRAY array of sequence values
  ******************************************************************/
  FUNCTION get_seq_values(
    seq_name VARCHAR2, 
    seq_count NUMBER,
    schema_name VARCHAR2 := USER
    ) RETURN ID_ARRAY
  IS
    seq_tbl ID_ARRAY;
  BEGIN
    EXECUTE IMMEDIATE 'SELECT ' || upper(schema_name) || '.' || seq_name || '.nextval FROM dual CONNECT BY level <= :1' 
                         BULK COLLECT INTO seq_tbl USING seq_count;
    RETURN seq_tbl;
  END;

  /*****************************************************************
  * string2id_array
  *
  * converts a string into an ID_ARRAY object
  *      
  * @param str string to be splitted
  * @param delim delimiter used for splitting, defaults to ','
  * @return ID_ARRAY array of number containing split tokens
  ******************************************************************/
  FUNCTION string2id_array(str VARCHAR2, delim VARCHAR2 := ',') RETURN ID_ARRAY
  IS
    arr ID_ARRAY;
  BEGIN
    EXECUTE IMMEDIATE 'WITH split_str AS (
                         SELECT regexp_substr(:1, :2, 1, LEVEL) AS str_parts FROM dual 
                           CONNECT BY regexp_substr(:3, :4, 1, LEVEL) IS NOT NULL
                       )
                       SELECT to_number(replace(str_parts,''.'','','')) FROM split_str'
                       BULK COLLECT INTO arr USING str, '[^'||delim||']+', str, '[^'||delim||']+';
    RETURN arr;
  END;

  /*****************************************************************
  * get_id_array_size
  *
  * returns the size of a given ID_ARRAY object
  *      
  * @param id_arr passed ID_ARRAY object
  * @return size of ID_ARRAY object
  ******************************************************************/
  FUNCTION get_id_array_size(id_arr ID_ARRAY) RETURN NUMBER
  IS
    arr_count NUMBER := 0;
  BEGIN
    arr_count := id_arr.count;
    RETURN arr_count;
  END;


  /*****************************************************************
  * objectclass_id_to_table_name
  *
  * @param class_id objectclass_id identifier
  * @return VARCHAR2 name of table that stores objects referred 
  *                  to the given objectclass_id
  ******************************************************************/
  FUNCTION objectclass_id_to_table_name(class_id NUMBER) RETURN VARCHAR2
  IS
    table_name VARCHAR2(30) := '';
  BEGIN
    CASE 
      WHEN class_id = 4 THEN table_name := 'land_use';
      WHEN class_id = 5 THEN table_name := 'generic_cityobject';
      WHEN class_id = 7 THEN table_name := 'solitary_vegetat_object';
      WHEN class_id = 8 THEN table_name := 'plant_cover';
      WHEN class_id = 9 THEN table_name := 'waterbody';
      WHEN class_id = 11 OR 
           class_id = 12 OR 
           class_id = 13 THEN table_name := 'waterboundary_surface';
      WHEN class_id = 14 THEN table_name := 'relief_feature';
      WHEN class_id = 16 OR 
           class_id = 17 OR 
           class_id = 18 OR 
           class_id = 19 THEN table_name := 'relief_component';
      WHEN class_id = 21 THEN table_name := 'city_furniture';
      WHEN class_id = 23 THEN table_name := 'cityobjectgroup';
      WHEN class_id = 25 OR 
           class_id = 26 THEN table_name := 'building';
      WHEN class_id = 27 OR 
           class_id = 28 THEN table_name := 'building_installation';
      WHEN class_id = 30 OR 
           class_id = 31 OR 
           class_id = 32 OR 
           class_id = 33 OR 
           class_id = 34 OR 
           class_id = 35 OR
           class_id = 36 OR
           class_id = 60 OR
           class_id = 61 THEN table_name := 'thematic_surface';
      WHEN class_id = 38 OR 
           class_id = 39 THEN table_name := 'opening';
      WHEN class_id = 40 THEN table_name := 'building_furniture';
      WHEN class_id = 41 THEN table_name := 'room';
      WHEN class_id = 43 OR 
           class_id = 44 OR 
           class_id = 45 OR 
           class_id = 46 THEN table_name := 'transportation_complex';
      WHEN class_id = 47 OR 
           class_id = 48 THEN table_name := 'traffic_area';
      WHEN class_id = 57 THEN table_name := 'citymodel';
      WHEN class_id = 63 OR
           class_id = 64 THEN table_name := 'bridge';
      WHEN class_id = 65 OR
           class_id = 66 THEN table_name := 'bridge_installation';
      WHEN class_id = 68 OR 
           class_id = 69 OR 
           class_id = 70 OR 
           class_id = 71 OR 
           class_id = 72 OR
           class_id = 73 OR
           class_id = 74 OR
           class_id = 75 OR
           class_id = 76 THEN table_name := 'bridge_thematic_surface';
      WHEN class_id = 78 OR 
           class_id = 79 THEN table_name := 'bridge_opening';		 
      WHEN class_id = 80 THEN table_name := 'bridge_furniture';
      WHEN class_id = 81 THEN table_name := 'bridge_room';
      WHEN class_id = 82 THEN table_name := 'bridge_constr_element';
      WHEN class_id = 84 OR
           class_id = 85 THEN table_name := 'tunnel';
      WHEN class_id = 86 OR
           class_id = 87 THEN table_name := 'tunnel_installation';
      WHEN class_id = 88 OR 
           class_id = 89 OR 
           class_id = 90 OR 
           class_id = 91 OR 
           class_id = 92 OR
           class_id = 93 OR
           class_id = 94 OR
           class_id = 95 OR
           class_id = 96 THEN table_name := 'tunnel_thematic_surface';
      WHEN class_id = 99 OR 
           class_id = 100 THEN table_name := 'tunnel_opening';		 
      WHEN class_id = 101 THEN table_name := 'tunnel_furniture';
      WHEN class_id = 102 THEN table_name := 'tunnel_hollow_space';
    ELSE
      dbms_output.put_line('Table name unknown.');
      NULL;
    END CASE;

    RETURN table_name;
  END;
  

  /*****************************************************************
  * construct_solid
  *
  * @param geom_root_id identifier to group geometries of a solid
  * @param schema_name name of schema
  * @return SDO_GEOMETRY the constructed solid geometry
  ******************************************************************/
  FUNCTION construct_solid(
    geom_root_id NUMBER,
    schema_name VARCHAR2 := USER
    ) RETURN SDO_GEOMETRY
  IS
    column_srid NUMBER;
    geom_cur ref_cursor;
    solid_part SDO_GEOMETRY;
    solid_geom SDO_GEOMETRY;
    elem_count NUMBER := 1;
    solid_offset NUMBER := 0;
    solid_null_ex EXCEPTION;
    --solid_invalid_ex EXCEPTION;
  BEGIN
    SELECT srid INTO column_srid FROM user_sdo_geom_metadata WHERE table_name = 'SURFACE_GEOMETRY' AND column_name = 'SOLID_GEOMETRY';

    OPEN geom_cur FOR 'SELECT geometry FROM ' || schema_name || '.surface_geometry
                         WHERE (root_id = :1 OR parent_id = :2) 
                           AND geometry IS NOT NULL ORDER BY id' USING geom_root_id, geom_root_id;
    LOOP
      FETCH geom_cur INTO solid_part;
      EXIT WHEN geom_cur%NOTFOUND;

      IF solid_geom IS NULL THEN
        -- construct an empty solid
        solid_geom := mdsys.sdo_geometry(
                      3008, column_srid, null,
                      mdsys.sdo_elem_info_array (), mdsys.sdo_ordinate_array ()
                      );

        solid_geom.sdo_elem_info.extend(6);
        solid_geom.sdo_elem_info(1) := 1;
        solid_geom.sdo_elem_info(2) := 1007;
        solid_geom.sdo_elem_info(3) := 1;
        solid_geom.sdo_elem_info(4) := 1;
        solid_geom.sdo_elem_info(5) := 1006;
        solid_geom.sdo_elem_info(6) := 0;
      END IF;

      IF (solid_part.sdo_elem_info IS NOT NULL) THEN
        -- fill elem_info_array
        FOR i IN solid_part.sdo_elem_info.FIRST .. solid_part.sdo_elem_info.LAST LOOP
          solid_geom.sdo_elem_info.extend;

          -- set correct offset
          -- the following equation will always be 0 for the first position of one or more ELEM_INFO_ARRAYs
          IF (elem_count - (i + 2) / 3) = 0 THEN
            solid_geom.sdo_elem_info(solid_geom.sdo_elem_info.count) := solid_offset + solid_part.sdo_elem_info(i);
            elem_count := elem_count + 1;
          ELSE
            solid_geom.sdo_elem_info(solid_geom.sdo_elem_info.count) := solid_part.sdo_elem_info(i);
          END IF;
        END LOOP;

        -- fill ordinates_array
        IF (solid_part.sdo_ordinates IS NOT NULL) THEN
          FOR i IN solid_part.sdo_ordinates.FIRST .. solid_part.sdo_ordinates.LAST LOOP
            solid_geom.sdo_ordinates.extend;
            solid_geom.sdo_ordinates(solid_geom.sdo_ordinates.count) := solid_part.sdo_ordinates(i);
          END LOOP;
          -- update offset
          solid_offset := solid_geom.sdo_ordinates.count;
        END IF;

        -- update sdo_elem_info of solid and reset elem_count
        solid_geom.sdo_elem_info(6) := solid_geom.sdo_elem_info(6) + solid_part.sdo_elem_info.count / 3;
        elem_count := 1;
      END IF;
    END LOOP;
    CLOSE geom_cur;

    -- loop stops when last solid is complete but before the corresponding update is commited
    -- therefore it has to be done here
    IF solid_geom IS NOT NULL THEN
      --IF sdo_geom.validate_geometry(solid_geom, 0.001) = 'TRUE' THEN
        RETURN solid_geom; 
      /*ELSE
        RAISE solid_invalid_ex;
      END IF;*/
    ELSE
      RAISE solid_null_ex;
    END IF;

    EXCEPTION
      WHEN solid_null_ex THEN
        dbms_output.put_line('Empty solid. Propably no entries exist in the database for chosen ID');
        RETURN NULL;
      /*WHEN solid_invalid_ex THEN
        dbms_output.put_line('Constructed solid is not valid.');
        RETURN NULL;*/
      WHEN OTHERS THEN
        dbms_output.put_line('An error occured when executing function citydb_util.construct_solid. Error: ' || SQLERRM);
  END;


  /*
  * code taken from http://forums.oracle.com/forums/thread.jspa?messageID=960492&#960492
  */
  function to_2d (geom mdsys.sdo_geometry, srid number)
  return mdsys.sdo_geometry
  is
    geom_2d mdsys.sdo_geometry;
    dim_count integer; -- number of dimensions in layer
    gtype integer; -- geometry type (single digit)
    n_points integer; -- number of points in ordinates array
    n_ordinates integer; -- number of ordinates
    i integer;
    j integer;
    k integer;
    offset integer;
  begin
    -- If the input geometry is null, just return null
    if geom is null then
      return (null);
    end if;
    
    -- Get the number of dimensions from the gtype
    if length (geom.sdo_gtype) = 4 then
      dim_count := substr (geom.sdo_gtype, 1, 1);
      gtype := substr (geom.sdo_gtype, 4, 1);
    else
    -- Indicate failure
      raise_application_error (-20000, 'Unable to determine dimensionality from gtype');
    end if;
    
    if dim_count = 2 then
      -- Nothing to do, geometry is already 2D
      return (geom);
    end if;
  
    -- Construct and prepare the output geometry
    geom_2d := mdsys.sdo_geometry (
                2000+gtype, srid, geom.sdo_point,
                mdsys.sdo_elem_info_array (), mdsys.sdo_ordinate_array()
                );
  
    -- Process the point structure
    if geom_2d.sdo_point is not null then
      geom_2D.sdo_point.z := null;
    else
      -- It is not a point  
      -- Process the ordinates array
  
      -- Prepare the size of the output array
      n_points := geom.sdo_ordinates.count / dim_count;
      n_ordinates := n_points * 2;
      geom_2d.sdo_ordinates.extend(n_ordinates);
  
      -- Copy the ordinates array
      j := geom.sdo_ordinates.first; -- index into input elem_info array
      k := 1; -- index into output ordinate array
      for i in 1..n_points loop
        geom_2d.sdo_ordinates (k) := geom.sdo_ordinates (j); -- copy X
        geom_2d.sdo_ordinates (k+1) := geom.sdo_ordinates (j+1); -- copy Y
        j := j + dim_count;
        k := k + 2;
      end loop;
  
      -- Process the element info array
      
      -- Copy the input array into the output array
      geom_2d.sdo_elem_info := geom.sdo_elem_info;
      
      -- Adjust the offsets
      i := geom_2d.sdo_elem_info.first;
      while i < geom_2d.sdo_elem_info.last loop
        offset := geom_2d.sdo_elem_info(i);
        geom_2d.sdo_elem_info(i) := (offset-1)/dim_count*2+1;
        i := i + 3;
      end loop;
    end if;
  
    return geom_2d;
  exception
    when others then
      dbms_output.put_line('to_2d: ' || SQLERRM);
      return null;
  end;
  
  /*
  * code taken from https://gist.github.com/nathanvda/a61ab4b094c4c3429a39
  * Author: Simon Greener, Nathan Van der Auwera
  */
  FUNCTION sdo2geojson3d(p_geometry in sdo_geometry,
    p_decimal_places in pls_integer default 2,
    p_compress_tags in pls_integer default 0,
    p_relative2mbr in pls_integer default 0
    ) return clob deterministic

    /* Note: Does not support curved geometries.
    * If required, stroke geometry before calling function.
    * If Compressed apply bbox to coordinates.....
    * { "type": "Feature",
    * "bbox": [-180.0, -90.0, 180.0, 90.0],
    * "geometry": {
    * "type": "Polygon",
    * "coordinates": [[ [-180.0, 10.0], [20.0, 90.0], [180.0, -5.0], [-30.0, -90.0] ]]
    * }
    * ...
    * }
    */
     
    as
    v_relative boolean := case when p_relative2mbr<>0 then true else false end;
     
    v_result clob;
    v_type varchar2(50);
    v_compress_tags boolean := case when p_compress_tags<>0 then true else false end;
    v_feature_key varchar2(100) := case when v_compress_tags then 'F' else '"Feature"' end;
    v_bbox_tag varchar2(100) := case when v_compress_tags then 'b:' else '"bbox":' end;
    v_coord_tag varchar2(100) := case when v_compress_tags then 'c:' else '"coordinates":' end;
    v_geometry_tag varchar2(100) := case when v_compress_tags then 'g:' else '"Geometry":' end;
    v_type_tag varchar2(100) := case when v_compress_tags then 't:' else '"type":' end;
    v_temp_string varchar2(30000);
     
    v_precision pls_integer := nvl(p_decimal_places,2);
    v_i pls_integer;
    v_num_rings pls_integer;
    v_num_elements pls_integer;
    v_element_no pls_integer;
    v_vertices mdsys.vertex_set_type;
    v_element mdsys.sdo_geometry;
    v_ring mdsys.sdo_geometry;
    v_mbr mdsys.sdo_geometry;
    v_geometry mdsys.sdo_geometry := p_geometry;
     
    Function hasRectangles( p_elem_info in mdsys.sdo_elem_info_array )
    Return Pls_Integer
    Is
    v_rectangle_count number := 0;
    v_etype pls_integer;
    v_interpretation pls_integer;
    v_elements pls_integer;
    Begin
    If ( p_elem_info is null ) Then
    return 0;
    End If;
    v_elements := ( ( p_elem_info.COUNT / 3 ) - 1 );
    <<element_extraction>>
    for v_i IN 0 .. v_elements LOOP
    v_etype := p_elem_info(v_i * 3 + 2);
    v_interpretation := p_elem_info(v_i * 3 + 3);
    If ( v_etype in (1003,2003) AND v_interpretation = 3 ) Then
    v_rectangle_count := v_rectangle_count + 1;
    end If;
    end loop element_extraction;
    Return v_rectangle_Count;
    End hasRectangles;
     
    Function hasCircularArcs(p_elem_info in mdsys.sdo_elem_info_array)
    return boolean
    Is
    v_elements number;
    Begin
    v_elements := ( ( p_elem_info.COUNT / 3 ) - 1 );
    <<element_extraction>>
    for v_i IN 0 .. v_elements LOOP
    if ( ( /* etype */ p_elem_info(v_i * 3 + 2) = 2 AND
    /* interpretation*/ p_elem_info(v_i * 3 + 3) = 2 )
    OR
    ( /* etype */ p_elem_info(v_i * 3 + 2) in (1003,2003) AND
    /* interpretation*/ p_elem_info(v_i * 3 + 3) IN (2,4) ) ) then
    return true;
    end If;
    end loop element_extraction;
    return false;
    End hasCircularArcs;
     
    Function GetNumRings( p_geometry in mdsys.sdo_geometry,
    p_ring_type in integer default 0 /* 0 = ALL; 1 = OUTER; 2 = INNER */ )
    Return Number
    Is
    v_ring_count number := 0;
    v_ring_type number := p_ring_type;
    v_elements number;
    v_etype pls_integer;
    Begin
    If ( p_geometry is null ) Then
    return 0;
    End If;
    If ( p_geometry.sdo_elem_info is null ) Then
    return 0;
    End If;
    If ( v_ring_type not in (0,1,2) ) Then
    v_ring_type := 0;
    End If;
    v_elements := ( ( p_geometry.sdo_elem_info.COUNT / 3 ) - 1 );
    <<element_extraction>>
    for v_i IN 0 .. v_elements LOOP
    v_etype := p_geometry.sdo_elem_info(v_i * 3 + 2);
    If ( v_etype in (1003,1005,2003,2005) and 0 = v_ring_type )
    OR ( v_etype in (1003,1005) and 1 = v_ring_type )
    OR ( v_etype in (2003,2005) and 2 = v_ring_type ) Then
    v_ring_count := v_ring_count + 1;
    end If;
    end loop element_extraction;
    Return v_ring_count;
    End GetNumRings;
     
    PROCEDURE ADD_Coordinate( p_ordinates in out nocopy mdsys.sdo_ordinate_array,
    p_dim in number,
    p_x_coord in number,
    p_y_coord in number,
    p_z_coord in number,
    p_m_coord in number,
    p_measured in boolean := false,
    p_duplicates in boolean := false)
    IS
    Function Duplicate
    Return Boolean
    Is
    Begin
    Return case when p_ordinates is null or p_ordinates.count = 0
    then False
    Else case p_dim
    when 2
    then ( p_ordinates(p_ordinates.COUNT) = p_y_coord
    AND
    p_ordinates(p_ordinates.COUNT-1) = p_x_coord )
    when 3
    then ( p_ordinates(p_ordinates.COUNT) = case when p_measured then p_m_coord else p_z_coord end
    AND
    p_ordinates(p_ordinates.COUNT-1) = p_y_coord
    AND
    p_ordinates(p_ordinates.COUNT-2) = p_x_coord )
    when 4
    then ( p_ordinates(p_ordinates.COUNT) = p_m_coord
    AND
    p_ordinates(p_ordinates.COUNT-1) = p_z_coord
    AND
    p_ordinates(p_ordinates.COUNT-2) = p_y_coord
    AND
    p_ordinates(p_ordinates.COUNT-3) = p_x_coord )
    end
    End;
    End Duplicate;
     
    Begin
    If ( p_ordinates is null ) Then
    p_ordinates := new mdsys.sdo_ordinate_array(null);
    p_ordinates.DELETE;
    End If;
    If ( p_duplicates or Not Duplicate() ) Then
    IF ( p_dim >= 2 ) Then
    p_ordinates.extend(2);
    p_ordinates(p_ordinates.count-1) := p_x_coord;
    p_ordinates(p_ordinates.count ) := p_y_coord;
    END IF;
    IF ( p_dim >= 3 ) Then
    p_ordinates.extend(1);
    p_ordinates(p_ordinates.count) := case when p_dim = 3 And p_measured
    then p_m_coord
    else p_z_coord
    end;
    END IF;
    IF ( p_dim = 4 ) Then
    p_ordinates.extend(1);
    p_ordinates(p_ordinates.count) := p_m_coord;
    END IF;
    End If;
    END ADD_Coordinate;
     
    Function Rectangle2Polygon(p_geometry in mdsys.sdo_geometry)
    return mdsys.sdo_geometry
    As
    v_dims pls_integer;
    v_ordinates mdsys.sdo_ordinate_array := new mdsys.sdo_ordinate_array(null);
    v_vertices mdsys.vertex_set_type;
    v_etype pls_integer;
    v_start_coord mdsys.vertex_type;
    v_end_coord mdsys.vertex_type;
    Begin
    v_ordinates.DELETE;
    v_dims := p_geometry.get_dims();
    v_etype := p_geometry.sdo_elem_info(2);
    v_vertices := sdo_util.getVertices(p_geometry);
    v_start_coord := v_vertices(1);
    v_end_coord := v_vertices(2);
    -- First coordinate
    ADD_Coordinate( v_ordinates, v_dims, v_start_coord.x, v_start_coord.y, v_start_coord.z, v_start_coord.w );
    -- Second coordinate
    If ( v_etype = 1003 ) Then
    ADD_Coordinate(v_ordinates,v_dims,v_end_coord.x,v_start_coord.y,(v_start_coord.z + v_end_coord.z) /2, v_start_coord.w);
    Else
    ADD_Coordinate(v_ordinates,v_dims,v_start_coord.x,v_end_coord.y,(v_start_coord.z + v_end_coord.z) /2,
    (v_end_coord.w - v_start_coord.w) * ((v_end_coord.x - v_start_coord.x) /
    ((v_end_coord.x - v_start_coord.x) + (v_end_coord.y - v_start_coord.y)) ));
    End If;
    -- 3rd or middle coordinate
    ADD_Coordinate(v_ordinates,v_dims,v_end_coord.x,v_end_coord.y,v_end_coord.z,v_end_coord.w);
    -- 4th coordinate
    If ( v_etype = 1003 ) Then
    ADD_Coordinate(v_ordinates,v_dims,v_start_coord.x,v_end_coord.y,(v_start_coord.z + v_end_coord.z) /2,v_start_coord.w);
    Else
    Add_Coordinate(v_ordinates,v_dims,v_end_coord.x,v_start_coord.y,(v_start_coord.z + v_end_coord.z) /2,
    (v_end_coord.w - v_start_coord.w) * ((v_end_coord.x - v_start_coord.x) /
    ((v_end_coord.x - v_start_coord.x) + (v_end_coord.y - v_start_coord.y)) ));
    End If;
    -- Last coordinate
    ADD_Coordinate(v_ordinates,v_dims,v_start_coord.x,v_start_coord.y,v_start_coord.z,v_start_coord.w);
    return mdsys.sdo_geometry(p_geometry.sdo_gtype,p_geometry.sdo_srid,null,mdsys.sdo_elem_info_array(1,v_etype,1),v_ordinates);
    End Rectangle2Polygon;
     
     
    Function formatNumber(pos in number)
    return varchar2
    As
    v_temp number;
    v_result varchar2(100);
    Begin
    v_temp := round(pos, v_precision);
     
    if ((v_temp >=0) and (v_temp < 1)) then
    v_result := to_char(v_temp, '0.99');
    else
    v_result := to_char(v_temp);
    end if;
    return v_result;
    End formatNumber;
     
    Function formatCoord(p_x in number,
    p_y in number,
    p_z in number,
    p_relative in boolean)
    return varchar2
    As
    Begin
    return '[' ||
    case when p_relative
    then formatNumber(p_x - v_mbr.sdo_ordinates(1)) || ',' || formatNumber(p_y - v_mbr.sdo_ordinates(2))
    else formatNumber(p_x) || ',' || formatNumber(p_y) || ',' || formatNumber(p_z)
    end ||
    ']';
    End formatCoord;
     
    begin
    if ( p_geometry is null ) then
    return null;
    end if;
     
    -- Currently, we do not support compound objects
    -- 
    If ( p_geometry.get_gtype() not in (1,2,3,5,6,7) ) Then
    RETURN NULL;
    End If;
     
    DBMS_LOB.createtemporary (lob_loc => v_result, cache => TRUE);
     
    v_type := case when v_compress_tags
    then case p_geometry.get_gtype()
    when 1 then 'P'
    when 2 then 'LS'
    when 3 then 'PG'
    when 5 then 'MP'
    when 6 then 'MLS'
    when 7 then 'MPG'
    end
    else case p_geometry.get_gtype()
    when 1 then '"Point"'
    when 2 then '"LineString"'
    when 3 then '"Polygon"'
    when 5 then '"MultiPoint"'
    when 6 then '"MultiLineString"'
    when 7 then '"MultiPolygon"'
    end
    end;
     
    v_temp_string := '{';
     
    if ( p_geometry.get_gtype() = 1 ) then
    v_temp_string := v_temp_string || v_type_tag || v_type || ',' || v_coord_tag;
    if (p_geometry.SDO_POINT is not null ) then
    v_temp_string := v_temp_string || formatCoord(p_geometry.SDO_POINT.X, p_geometry.SDO_POINT.Y, p_geometry.SDO_POINT.Z, false);
    else
    v_temp_string := v_temp_string || formatCoord(p_geometry.sdo_ordinates(1), p_geometry.sdo_ordinates(2), p_geometry.sdo_ordinates(3), false);
    End If;
    v_temp_string := v_temp_string || '}';
     
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => 1,
    buffer => v_temp_string );
    return v_result;
    end If;
     
    if ( v_relative ) then
    v_mbr := SDO_GEOM.SDO_MBR(p_geometry);
    if ( v_mbr is not null ) then
    v_temp_string := v_temp_string ||
    v_type_tag || v_feature_key || ',' ||
    v_bbox_tag || '[' ||
    v_mbr.sdo_ordinates(1) || ',' ||
    v_mbr.sdo_ordinates(2) || ',' ||
    v_mbr.sdo_ordinates(3) || ',' ||
    v_mbr.sdo_ordinates(4) || ',' ||
    '],' || v_geometry_tag || '{';
    End If;
    End If;
    v_temp_string := v_temp_string || v_type_tag || v_type || ',' || v_coord_tag;
     
    -- Write header
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => 1,
    buffer => v_temp_string);
     
    If ( hasCircularArcs(p_geometry.sdo_elem_info) ) then
    return null;
    End If;
     
    v_num_elements := mdsys.sdo_util.GetNumElem(p_geometry);
    <<for_all_elements>>
    FOR v_element_no IN 1..v_num_elements LOOP
    v_element := mdsys.sdo_util.Extract(p_geometry,v_element_no); -- Extract element with all sub-elements
    If ( v_element.get_gtype() in (1,2,5) ) Then
    if (v_element_no = 1) Then
    v_temp_string := '[';
    elsif ( v_element.get_gtype() = 2 ) Then
    v_temp_string := '],[';
    End If;
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    v_vertices := mdsys.sdo_util.getVertices(v_element);
    v_temp_string := formatCoord(v_vertices(1).x,v_vertices(1).y,v_vertices(1).z,v_relative);
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    <<for_all_vertices>>
    for j in 2..v_vertices.count loop
    v_temp_string := ',' || formatCoord(v_vertices(j).x,v_vertices(j).y,v_vertices(j).z,v_relative);
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    end loop for_all_vertices;
    Else
    if (v_element_no = 1) Then
    v_temp_string := '[';
    else
    v_temp_string := '],[';
    End If;
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    v_num_rings := GetNumRings(v_element);
    <<for_all_rings>>
    FOR v_ring_no in 1..v_num_rings Loop
    v_ring := MDSYS.SDO_UTIL.EXTRACT(p_geometry,v_element_no,v_ring_no); -- Extract ring from element .. must do it this way, can't correctly extract from v_element.
    If (hasRectangles(v_ring.sdo_elem_info)>0) Then
    v_ring := Rectangle2Polygon(v_ring);
    End If;
    if ( v_ring_no > 1 ) Then
    v_temp_string := ',';
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    End If;
    v_vertices := mdsys.sdo_util.getVertices(v_ring);
    v_temp_string := '[' || formatCoord(v_vertices(1).x,v_vertices(1).y,v_vertices(1).z,v_relative);
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
     
    <<for_all_vertices>>
    for j in 2..v_vertices.count loop
    v_temp_string := ',' || formatCoord(v_vertices(j).x,v_vertices(j).y,v_vertices(j).z,v_relative);
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    end loop for_all_vertices;
    v_temp_string := ']'; -- Close Ring
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    End Loop for_all_rings;
    End If;
    END LOOP for_all_elements;
     
    -- Closing tag
    v_temp_string := ']}';
    if ( v_relative and p_geometry.get_gtype() <> 1 ) then
    v_temp_string := v_temp_string || '}';
    end if;
     
    DBMS_LOB.write(lob_loc => v_result,
    amount => LENGTH (v_temp_string),
    offset => DBMS_LOB.GETLENGTH(v_result)+1,
    buffer => v_temp_string );
    return v_result;
  End sdo2geojson3d; 
  
END citydb_util;
/