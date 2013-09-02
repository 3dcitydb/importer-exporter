-- UTIL.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2013  Institute for Geodesy and Geoinformation Science,
--                             Technische Universitaet Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Creates package utility methods for applications.
-- All functions are part of the geodb_pkg.schema and UTIL-"Package" 
-- They start with the prefix "util_" to guarantee a better overview 
-- in the PGAdminIII-Tool.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                                | Author
-- 1.2.0     2013-08-29   minor changes to change_db_srid function     FKun
-- 1.1.0     2013-02-22   PostGIS version                              FKun
--                                                                     CNag
-- 1.0.0     2008-09-10   release version                              CNag
--

/*****************************************************************
* versioning_table
*
* @param table_name name of the unversioned table, i.e., omit
*                   suffixes such as _LT
* @RETURN VARCHAR 'ON' for version-enabled, 'OFF' otherwise
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_versioning_table(table_name VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN 'OFF';
END;
$$
LANGUAGE plpgsql;

/*****************************************************************
* versioning_db
*
* @RETURN VARCHAR 'ON' for version-enabled, 'PARTLY' and 'OFF'
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_versioning_db() RETURNS VARCHAR AS $$
BEGIN
    RETURN 'OFF';
END;
$$
LANGUAGE plpgsql;
 
/*****************************************************************
* db_info
*
* @param srid database srid
* @param srs database srs name
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_db_info(OUT srid DATABASE_SRS.SRID%TYPE, OUT srs DATABASE_SRS.GML_SRS_NAME%TYPE) RETURNS SETOF record AS $$
    SELECT srid, gml_srs_name FROM database_srs;
$$ 
LANGUAGE sql;

/******************************************************************
* db_metadata
*
* @RETURN TABLE with columns SRID, GML_SRS_NAME, COORD_REF_SYS_NAME, COORD_REF_SYS_KIND
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_db_metadata() 
RETURNS TABLE(srid INTEGER, gml_srs_name VARCHAR(1000), coord_ref_sys_name VARCHAR(2048), coord_ref_sys_kind VARCHAR(2048)) AS $$
BEGIN
    EXECUTE 'SELECT SRID, GML_SRS_NAME FROM DATABASE_SRS' INTO srid, gml_srs_name;
    EXECUTE 'SELECT srtext, srtext FROM spatial_ref_sys WHERE SRID=' || srid || '' INTO coord_ref_sys_name, coord_ref_sys_kind;
    coord_ref_sys_name := split_part(coord_ref_sys_name, '"', 2);
    coord_ref_sys_kind := split_part(coord_ref_sys_kind, '[', 1);
    RETURN NEXT;
END;
$$
LANGUAGE plpgsql;
  
/******************************************************************
* error_msg
*
* @param err_code PostgreSQL error code, e.g. '06404'
* @RETURN TEXT corresponding PostgreSQL error message                 
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_error_msg(err_code VARCHAR) RETURNS TEXT AS $$
BEGIN
    BEGIN
        RAISE EXCEPTION USING ERRCODE = err_code;
    EXCEPTION
        WHEN OTHERS THEN
            RETURN SQLERRM;
    END;
END;
$$
LANGUAGE plpgsql;
   
/******************************************************************
* min
*
* @param a first NUMERIC value
* @param b second NUMERIC value
* @RETURN NUMERIC the smaller of the two input NUMERIC values                
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_min(a NUMERIC, b NUMERIC) RETURNS NUMERIC AS $$
BEGIN
    IF a < b THEN
        RETURN a;
    ELSE
        RETURN b;
    END IF;
END;
$$
LANGUAGE plpgsql;
  
/******************************************************************
* transform_or_null
*
* @param geom the geometry whose representation is to be transformed using another coordinate system 
* @param srid the SRID of the coordinate system to be used for the transformation.
* @RETURN GEOMETRY the transformed geometry representation                
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_transform_or_null(geom GEOMETRY, srid INTEGER) RETURNS geometry AS $$
BEGIN
    IF geom IS NOT NULL THEN
        RETURN ST_Transform(geom, srid);
    ELSE
        RETURN NULL;
    END IF;
END;
$$
LANGUAGE plpgsql;

/******************************************************************
* is_coord_ref_sys_3d
*
* no 3D-Coord.-Reference-System defined in the spatial_ref_sys-table of PostGIS 2.0 by default
* refer to spatialreference.org for INSERT-statements of 3D-SRIDs
* they can be identified by the AXIS UP in the srtext
*
* @param srid the SRID of the coordinate system to be checked
* @RETURN NUMERIC the boolean result encoded as NUMERIC: 0 = false, 1 = true                
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_is_coord_ref_sys_3d(srid INTEGER) RETURNS INTEGER AS $$
DECLARE
    is_3d INTEGER := 0;
BEGIN
    EXECUTE 'SELECT count(*) FROM spatial_ref_sys WHERE auth_srid=$1 AND srtext LIKE ''%UP]%''' INTO is_3d USING srid;

    RETURN is_3d;
END;
$$
LANGUAGE plpgsql;
 
/******************************************************************
* is_db_coord_ref_sys_3d
*
* @RETURN NUMERIC the boolean result encoded as NUMERIC: 0 = false, 1 = true                
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_is_db_coord_ref_sys_3d() RETURNS INTEGER AS $$
DECLARE
    srid INTEGER;
BEGIN
    EXECUTE 'SELECT srid from DATABASE_SRS' INTO srid;
    RETURN geodb_pkg.util_is_coord_ref_sys_3d(srid);
END;
$$
LANGUAGE plpgsql;


/*******************************************************************
* change_db_srid
*
* @param db_srid the SRID of the coordinate system to be further used in the database
* @param db_gml_srs_name the GML_SRS_NAME of the coordinate system to be further used in the database
*******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_change_db_srid (db_srid INTEGER, db_gml_srs_name VARCHAR) RETURNS SETOF void AS $$
BEGIN
  -- update entry in DATABASE_SRS table first
  UPDATE DATABASE_SRS SET SRID=db_srid, GML_SRS_NAME=db_gml_srs_name;
  
  -- change srid of each spatially enabled table
  PERFORM geodb_pkg.util_change_column_srid('cityobject_spx', 'cityobject', 'envelope', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('surface_geom_spx', 'surface_geometry', 'geometry', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('breakline_rid_spx', 'breakline_relief', 'ridge_or_valley_lines', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('breakline_break_spx', 'breakline_relief', 'break_lines', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('masspoint_rel_spx', 'masspoint_relief', 'relief_points', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('tin_relf_stop_spx', 'tin_relief', 'stop_lines', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('tin_relf_break_spx', 'tin_relief', 'break_lines', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('tin_relf_crtlpts_spx', 'tin_relief', 'control_points', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid(NULL, 'cityobject_genericattrib', 'geomval', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod0terr_spx', 'generic_cityobject', 'lod0_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod1terr_spx', 'generic_cityobject', 'lod1_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod2terr_spx', 'generic_cityobject', 'lod2_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod3terr_spx', 'generic_cityobject', 'lod3_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod4terr_spx', 'generic_cityobject', 'lod4_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod0refpnt_spx', 'generic_cityobject', 'lod0_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod1refpnt_spx', 'generic_cityobject', 'lod1_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod2refpnt_spx', 'generic_cityobject', 'lod2_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod3refpnt_spx', 'generic_cityobject', 'lod3_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('genericcity_lod4refpnt_spx', 'generic_cityobject', 'lod4_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid(NULL, 'address', 'multi_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod1terr_spx', 'building', 'lod1_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod2terr_spx', 'building', 'lod2_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod3terr_spx', 'building', 'lod3_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod4terr_spx', 'building', 'lod4_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod2multi_spx', 'building', 'lod2_multi_curve', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod3multi_spx', 'building', 'lod3_multi_curve', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('building_lod4multi_spx', 'building', 'lod4_multi_curve', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('bldg_furn_lod4refpt_spx', 'building_furniture', 'lod4_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod1terr_spx', 'city_furniture', 'lod1_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod2terr_spx', 'city_furniture', 'lod2_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod3terr_spx', 'city_furniture', 'lod3_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod4terr_spx', 'city_furniture', 'lod4_terrain_intersection', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod1refpnt_spx', 'city_furniture', 'lod1_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod2refpnt_spx', 'city_furniture', 'lod2_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod3refpnt_spx', 'city_furniture', 'lod3_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('city_furn_lod4refpnt_spx', 'city_furniture', 'lod4_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('citymodel_spx', 'citymodel', 'envelope', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('cityobjectgroup_spx', 'cityobjectgroup', 'geometry', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('relief_component_spx', 'relief_component', 'extent', FALSE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('sol_veg_obj_lod1refpt_spx', 'solitary_vegetat_object', 'lod1_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('sol_veg_obj_lod2refpt_spx', 'solitary_vegetat_object', 'lod2_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('sol_veg_obj_lod3refpt_spx', 'solitary_vegetat_object', 'lod3_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('sol_veg_obj_lod4refpt_spx', 'solitary_vegetat_object', 'lod4_implicit_ref_point', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('surface_data_spx', 'surface_data', 'gt_reference_point', FALSE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('transportation_complex_spx', 'transportation_complex', 'lod0_network', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('waterbody_lod0multi_spx', 'waterbody', 'lod0_multi_curve', TRUE, db_srid);
  PERFORM geodb_pkg.util_change_column_srid('waterbody_lod1multi_spx', 'waterbody', 'lod1_multi_curve', TRUE, db_srid);
END;
$$ 
LANGUAGE plpgsql;

/*****************************************************************
* change_column_srid
*
* @param i_name name of the spatial index
* @param t_name name of the table
* @param c_name name of the column
* @param is_3d dimension of spatial index
* @param db_srid the SRID of the coordinate system to be further used in the database
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_change_column_srid(
  i_name VARCHAR,
  t_name VARCHAR, 
  c_name VARCHAR,
  is_3d BOOLEAN,
  db_srid INTEGER) 
RETURNS SETOF void AS 
$$
DECLARE
  geom_type VARCHAR(100);
  is_valid BOOLEAN;
BEGIN
  EXECUTE 'SELECT type FROM geometry_columns WHERE f_table_name = $1 AND f_geometry_column = $2'
             INTO geom_type USING t_name, c_name;

  IF i_name IS NOT NULL THEN 
    is_valid := geodb_pkg.idx_index_status(t_name, c_name) = 'VALID';

    -- drop spatial index if exists
    IF is_valid THEN
       EXECUTE 'DROP INDEX ' || i_name;
    END IF;

    -- update geometry SRID
    PERFORM UpdateGeometrySRID(t_name, c_name, db_srid);

    -- create spatial index again
    IF is_valid THEN
      IF is_3d THEN
        EXECUTE 'CREATE INDEX ' || i_name || ' ON ' || t_name || ' USING GIST ( ' || c_name || ' gist_geometry_ops_nd )';
      ELSE
        EXECUTE 'CREATE INDEX ' || i_name || ' ON ' || t_name || ' USING GIST ( ' || c_name || ' gist_geometry_ops_nd )';
      END IF;
    END IF;
  ELSE
    -- no spatial index defined for table, only update metadata and geometry SRID
    PERFORM UpdateGeometrySRID(t_name, c_name, db_srid);
  END IF;

END;
$$ 
LANGUAGE plpgsql;
  
/******************************************************************
* on_delete_action
*
* Removes a contraint to add it again with parameters
* ON UPDATE CASCADE ON DELETE CASCADE or RESTRICT
*
* @param table_name defines the table to which the constraint belongs to
* @param fkey_name name of the foreign key that is updated 
* @param column_name defines the column the constraint is relying on
* @param ref_table 
* @param ref_column
* @param action whether CASCADE (default) or RESTRICT            
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_on_delete_action(
  table_name VARCHAR, 
  fkey_name VARCHAR,
  column_name VARCHAR,
  ref_table VARCHAR,
  ref_column VARCHAR,
  on_delete_param VARCHAR)
RETURNS SETOF void AS 
$$
BEGIN
  EXECUTE 'ALTER TABLE ' || table_name || ' DROP CONSTRAINT ' || fkey_name || 
             ', ADD CONSTRAINT ' || fkey_name || ' FOREIGN KEY (' || column_name || ') ' ||
                  'REFERENCES ' || ref_table || '(' || ref_column || ') ' ||
                     'ON UPDATE CASCADE ON DELETE ' || on_delete_param;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'Error on constraint %: %', fkey_name, SQLERRM;
END;
$$
LANGUAGE plpgsql;


/******************************************************************
* update_constraints
*
* uses the FUNCTION on_delete_action for updating all the contraints
*
* @param action whether CASCADE (default) or RESTRICT           
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_update_constraints(param VARCHAR DEFAULT 'CASCADE') RETURNS SETOF void AS 
$$
DECLARE
  on_delete_param VARCHAR;
BEGIN
  on_delete_param := $1;
  
  IF on_delete_param <> 'CASCADE' THEN
    on_delete_param := 'RESTRICT';
    RAISE NOTICE 'Constraints are set to ON DELETE RESTRICT';
  END IF;

  PERFORM geodb_pkg.util_on_delete_action('ADDRESS_TO_BUILDING','ADDRESS_TO_BUILDING_FK','BUILDING_ID','BUILDING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('ADDRESS_TO_BUILDING','ADDRESS_TO_BUILDING_ADDRESS_FK','ADDRESS_ID','ADDRESS','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('APPEARANCE','APPEARANCE_CITYMODEL_FK','CITYMODEL_ID','CITYMODEL','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('APPEARANCE','APPEARANCE_CITYOBJECT_FK','CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('APPEAR_TO_SURFACE_DATA','APPEAR_TO_SURFACE_DATA_FK1','APPEARANCE_ID','APPEARANCE','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('APPEAR_TO_SURFACE_DATA','APPEAR_TO_SURFACE_DATA_FK','SURFACE_DATA_ID','SURFACE_DATA','ID',on_delete_param);  
  PERFORM geodb_pkg.util_on_delete_action('APPEAR_TO_SURFACE_DATA','APPEAR_TO_SURFACE_DATA_FK','SURFACE_DATA_ID','SURFACE_DATA','ID',on_delete_param);    
  PERFORM geodb_pkg.util_on_delete_action('BREAKLINE_RELIEF','BREAKLINE_RELIEF_FK','ID','RELIEF_COMPONENT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK3','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK1','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK2','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_BUILDING_FK','BUILDING_PARENT_ID','BUILDING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_BUILDING_FK1','BUILDING_ROOT_ID','BUILDING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_FK1','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_FK2','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_FK','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_ROOM_FK','ROOM_ID','ROOM','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK3','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_ROOM_FK','ROOM_ID','ROOM','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK4','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK1','BUILDING_ID','BUILDING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK2','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT','CITYOBJECT_OBJECTCLASS_FK','CLASS_ID','OBJECTCLASS','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECTGROUP','CITYOBJECT_GROUP_FK','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECTGROUP','CITYOBJECTGROUP_CITYOBJECT_FK','PARENT_CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECTGROUP','CITYOBJECTGROUP_CITYOBJECT_FK1','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_GENERICATTRIB','CITYOBJECT_GENERICATTRIB_FK','CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_GENERICATTRIB','CITYOBJECT_GENERICATTRIB_FK1','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_MEMBER','CITYOBJECT_MEMBER_CITYMODEL_FK','CITYMODEL_ID','CITYMODEL','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_MEMBER','CITYOBJECT_MEMBER_FK','CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK1','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK2','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK3','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK4','LOD1_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK5','LOD2_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK6','LOD3_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK7','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('EXTERNAL_REFERENCE','EXTERNAL_REFERENCE_FK','CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERALIZATION','GENERALIZATION_FK1','GENERALIZES_TO_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERALIZATION','GENERALIZATION_FK','CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK1','LOD1_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK2','LOD2_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK3','LOD3_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK4','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK5','LOD0_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK6','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK7','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK8','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK9','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK10','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GROUP_TO_CITYOBJECT','GROUP_TO_CITYOBJECT_FK','CITYOBJECT_ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('GROUP_TO_CITYOBJECT','GROUP_TO_CITYOBJECT_FK1','CITYOBJECTGROUP_ID','CITYOBJECTGROUP','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('IMPLICIT_GEOMETRY','IMPLICIT_GEOMETRY_FK','RELATIVE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK','LOD0_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK1','LOD1_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK2','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK3','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK4','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('MASSPOINT_RELIEF','MASSPOINT_RELIEF_FK','ID','RELIEF_COMPONENT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OBJECTCLASS','OBJECTCLASS_OBJECTCLASS_FK','SUPERCLASS_ID','OBJECTCLASS','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_SURFACE_GEOMETRY_FK1','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_SURFACE_GEOMETRY_FK','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_ADDRESS_FK','ADDRESS_ID','ADDRESS','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OPENING_TO_THEM_SURFACE','OPENING_TO_THEMATIC_SURFACE_FK','OPENING_ID','OPENING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('OPENING_TO_THEM_SURFACE','OPENING_TO_THEMATIC_SURFAC_FK1','THEMATIC_SURFACE_ID','THEMATIC_SURFACE','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK1','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK2','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK3','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('RASTER_RELIEF','RASTER_RELIEF_FK','RELIEF_ID','RELIEF','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_COMPONENT','RELIEF_COMPONENT_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_FEATURE','RELIEF_FEATURE_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_FEAT_TO_REL_COMP','RELIEF_FEAT_TO_REL_COMP_FK','RELIEF_COMPONENT_ID','RELIEF_COMPONENT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_FEAT_TO_REL_COMP','RELIEF_FEAT_TO_REL_COMP_FK1','RELIEF_FEATURE_ID','RELIEF_FEATURE','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('ROOM','ROOM_BUILDING_FK','BUILDING_ID','BUILDING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('ROOM','ROOM_SURFACE_GEOMETRY_FK','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('ROOM','ROOM_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK1','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK2','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK3','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK4','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK5','LOD1_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK6','LOD2_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK7','LOD3_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK8','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SURFACE_GEOMETRY','SURFACE_GEOMETRY_FK','PARENT_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('SURFACE_GEOMETRY','SURFACE_GEOMETRY_FK1','ROOT_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TEXTUREPARAM','TEXTUREPARAM_SURFACE_GEOM_FK','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TEXTUREPARAM','TEXTUREPARAM_SURFACE_DATA_FK','SURFACE_DATA_ID','SURFACE_DATA','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_ROOM_FK','ROOM_ID','ROOM','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_BUILDING_FK','BUILDING_ID','BUILDING','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_FK','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_FK1','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_FK2','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TIN_RELIEF','TIN_RELIEF_SURFACE_GEOMETRY_FK','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TIN_RELIEF','TIN_RELIEF_RELIEF_COMPONENT_FK','ID','RELIEF_COMPONENT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK1','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK2','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK3','TRANSPORTATION_COMPLEX_ID','TRANSPORTATION_COMPLEX','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK1','LOD1_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK2','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK3','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK4','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_CITYOBJECT_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK','LOD1_SOLID_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK1','LOD2_SOLID_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK2','LOD3_SOLID_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK3','LOD4_SOLID_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK4','LOD0_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK5','LOD1_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK4','LOD0_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOD_TO_WATERBND_SRF','WATERBOD_TO_WATERBND_FK','WATERBOUNDARY_SURFACE_ID','WATERBOUNDARY_SURFACE','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOD_TO_WATERBND_SRF','WATERBOD_TO_WATERBND_FK1','WATERBODY_ID','WATERBODY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SRF_CITYOBJ_FK','ID','CITYOBJECT','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SURFACE_FK','LOD2_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SURFACE_FK1','LOD3_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SURFACE_FK2','LOD4_SURFACE_ID','SURFACE_GEOMETRY','ID',on_delete_param);
END;
$$
LANGUAGE plpgsql;