-- UTIL.sql
--
-- Authors:     Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Conversion:	Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
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
-- Version | Date       | Description      | Author | Conversion 
-- 1.0.0     2008-09-10   release version    CNag
-- 1.1.0     2012-02-29   update to 2.0.6    CNag     FKun
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
* @param versioning database versioning
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_db_info(OUT srid DATABASE_SRS.SRID%TYPE, OUT srs DATABASE_SRS.GML_SRS_NAME%TYPE) RETURNS SETOF record AS $$
    SELECT srid, gml_srs_name FROM database_srs;
$$ 
LANGUAGE sql;

/******************************************************************
* db_metadata
*
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
* @RETURN VARCHAR corresponding PostgreSQL error message                 
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.util_error_msg(err_code VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    BEGIN
        RAISE EXCEPTION USING ERRCODE = err_code;
        RETURN err_code;
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
* changes the database-SRID, if wrong SRID was used for CREATE_DB
* it should only be executed on an empty database to avoid any errors
*
*******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.util_change_db_srid (db_srid INTEGER, db_gml_srs_name VARCHAR) RETURNS SETOF void AS $$
BEGIN
-- Drop spatial indexes
  DROP INDEX CITYOBJECT_SPX;
  DROP INDEX SURFACE_GEOM_SPX;
  DROP INDEX BREAKLINE_RID_SPX;
  DROP INDEX BREAKLINE_BREAK_SPX;
  DROP INDEX MASSPOINT_REL_SPX;
  DROP INDEX TIN_RELF_STOP_SPX;
  DROP INDEX TIN_RELF_BREAK_SPX;
  DROP INDEX TIN_RELF_CRTLPTS_SPX;
  DROP INDEX GENERICCITY_LOD0TERR_SPX;
  DROP INDEX GENERICCITY_LOD1TERR_SPX;
  DROP INDEX GENERICCITY_LOD2TERR_SPX;
  DROP INDEX GENERICCITY_LOD3TERR_SPX;
  DROP INDEX GENERICCITY_LOD4TERR_SPX;
  DROP INDEX GENERICCITY_LOD1REFPNT_SPX;
  DROP INDEX GENERICCITY_LOD2REFPNT_SPX;
  DROP INDEX GENERICCITY_LOD3REFPNT_SPX;
  DROP INDEX GENERICCITY_LOD4REFPNT_SPX;
  DROP INDEX BUILDING_LOD1TERR_SPX;
  DROP INDEX BUILDING_LOD2TERR_SPX;
  DROP INDEX BUILDING_LOD3TERR_SPX;
  DROP INDEX BUILDING_LOD4TERR_SPX;
  DROP INDEX BUILDING_LOD2MULTI_SPX;
  DROP INDEX BUILDING_LOD3MULTI_SPX;
  DROP INDEX BUILDING_LOD4MULTI_SPX;
  DROP INDEX BUILDING_FURN_LOD4REFPNT_SPX;
  DROP INDEX CITY_FURN_LOD1TERR_SPX;
  DROP INDEX CITY_FURN_LOD2TERR_SPX;
  DROP INDEX CITY_FURN_LOD3TERR_SPX;
  DROP INDEX CITY_FURN_LOD4TERR_SPX;
  DROP INDEX CITY_FURN_LOD1REFPNT_SPX;
  DROP INDEX CITY_FURN_LOD2REFPNT_SPX;
  DROP INDEX CITY_FURN_LOD3REFPNT_SPX;
  DROP INDEX CITY_FURN_LOD4REFPNT_SPX;
  DROP INDEX CITYMODEL_SPX;
  DROP INDEX CITYOBJECTGROUP_SPX;
  DROP INDEX RELIEF_COMPONENT_SPX;
  DROP INDEX SOL_VEGETAT_OBJ_LOD1REFPNT_SPX;
  DROP INDEX SOL_VEGETAT_OBJ_LOD2REFPNT_SPX;
  DROP INDEX SOL_VEGETAT_OBJ_LOD3REFPNT_SPX;
  DROP INDEX SOL_VEGETAT_OBJ_LOD4REFPNT_SPX;
  DROP INDEX SURFACE_DATA_SPX;
  DROP INDEX TRANSPORTATION_COMPLEX_SPX;
  DROP INDEX WATERBODY_LOD0MULTI_SPX;
  DROP INDEX WATERBODY_LOD1MULTI_SPX;
	
-- Drop geometry columns from tables and geometry_columns-view
  PERFORM DropGeometryColumn('cityobject', 'envelope');
  PERFORM DropGeometryColumn('surface_geometry', 'geometry');
  PERFORM DropGeometryColumn('breakline_relief', 'ridge_or_valley_lines');
  PERFORM DropGeometryColumn('breakline_relief', 'break_lines');
  PERFORM DropGeometryColumn('masspoint_relief', 'relief_points');
  PERFORM DropGeometryColumn('tin_relief', 'stop_lines');
  PERFORM DropGeometryColumn('tin_relief', 'break_lines');
  PERFORM DropGeometryColumn('tin_relief', 'control_points');
  PERFORM DropGeometryColumn('cityobject_genericattrib', 'geomval');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod0_terrain_intersection');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod1_terrain_intersection');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod2_terrain_intersection');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod3_terrain_intersection');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod4_terrain_intersection');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod0_implicit_ref_point');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod1_implicit_ref_point');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod2_implicit_ref_point');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod3_implicit_ref_point');
  PERFORM DropGeometryColumn('generic_cityobject', 'lod4_implicit_ref_point');
  PERFORM DropGeometryColumn('address', 'multi_point');
  PERFORM DropGeometryColumn('building', 'lod1_terrain_intersection');
  PERFORM DropGeometryColumn('building', 'lod2_terrain_intersection');
  PERFORM DropGeometryColumn('building', 'lod3_terrain_intersection');
  PERFORM DropGeometryColumn('building', 'lod4_terrain_intersection');
  PERFORM DropGeometryColumn('building', 'lod2_multi_curve');
  PERFORM DropGeometryColumn('building', 'lod3_multi_curve');
  PERFORM DropGeometryColumn('building', 'lod4_multi_curve');
  PERFORM DropGeometryColumn('building_furniture', 'lod4_implicit_ref_point');
  PERFORM DropGeometryColumn('city_furniture', 'lod1_terrain_intersection');
  PERFORM DropGeometryColumn('city_furniture', 'lod2_terrain_intersection');
  PERFORM DropGeometryColumn('city_furniture', 'lod3_terrain_intersection');
  PERFORM DropGeometryColumn('city_furniture', 'lod4_terrain_intersection');
  PERFORM DropGeometryColumn('city_furniture', 'lod1_implicit_ref_point');
  PERFORM DropGeometryColumn('city_furniture', 'lod2_implicit_ref_point');
  PERFORM DropGeometryColumn('city_furniture', 'lod3_implicit_ref_point');
  PERFORM DropGeometryColumn('city_furniture', 'lod4_implicit_ref_point');
  PERFORM DropGeometryColumn('citymodel', 'envelope');
  PERFORM DropGeometryColumn('cityobjectgroup', 'geometry');
  PERFORM DropGeometryColumn('relief_component', 'extent');
  PERFORM DropGeometryColumn('solitary_vegetat_object', 'lod1_implicit_ref_point');
  PERFORM DropGeometryColumn('solitary_vegetat_object', 'lod2_implicit_ref_point');
  PERFORM DropGeometryColumn('solitary_vegetat_object', 'lod3_implicit_ref_point');
  PERFORM DropGeometryColumn('solitary_vegetat_object', 'lod4_implicit_ref_point');
  PERFORM DropGeometryColumn('surface_data', 'gt_reference_point');
  PERFORM DropGeometryColumn('transportation_complex', 'lod0_network');
  PERFORM DropGeometryColumn('waterbody', 'lod0_multi_curve');
  PERFORM DropGeometryColumn('waterbody', 'lod1_multi_curve');

-- Update entry in DATABASE_SRS-table
  UPDATE DATABASE_SRS SET SRID=db_srid, GML_SRS_NAME=db_gml_srs_name;
	
-- Create geometry columns in associted tables and add them to geometry_columns-view again
  PERFORM AddGeometryColumn('cityobject', 'envelope', db_srid, 'POLYGON', 3);
  PERFORM AddGeometryColumn('surface_geometry', 'geometry', db_srid, 'POLYGON', 3);
  PERFORM AddGeometryColumn('breakline_relief', 'ridge_or_valley_lines', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('breakline_relief', 'break_lines', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('masspoint_relief', 'relief_points', db_srid, 'MULTIPOINT', 3);
  PERFORM AddGeometryColumn('tin_relief', 'stop_lines', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('tin_relief', 'break_lines', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('tin_relief', 'control_points', db_srid, 'MULTIPOINT', 3);
  PERFORM AddGeometryColumn('cityobject_genericattrib', 'geomval', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod0_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod1_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod2_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod3_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod4_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod0_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod1_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod2_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod3_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('generic_cityobject', 'lod4_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('address', 'multi_point', db_srid, 'MULTIPOINT', 3);
  PERFORM AddGeometryColumn('building', 'lod1_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('building', 'lod2_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('building', 'lod3_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('building', 'lod4_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('building', 'lod2_multi_curve', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('building', 'lod3_multi_curve', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('building', 'lod4_multi_curve', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('building_furniture', 'lod4_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod1_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod2_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod3_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod4_terrain_intersection', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod1_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod2_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod3_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('city_furniture', 'lod4_implicit_ref_point', db_srid, 'POINT', 3);	
  PERFORM AddGeometryColumn('citymodel', 'envelope', db_srid, 'POLYGON', 3);
  PERFORM AddGeometryColumn('cityobjectgroup', 'geometry', db_srid, 'POLYGON', 3);
  PERFORM AddGeometryColumn('relief_component', 'extent', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('solitary_vegetat_object', 'lod1_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('solitary_vegetat_object', 'lod2_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('solitary_vegetat_object', 'lod3_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('solitary_vegetat_object', 'lod4_implicit_ref_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('surface_data', 'gt_reference_point', db_srid, 'POINT', 3);
  PERFORM AddGeometryColumn('transportation_complex', 'lod0_network', db_srid, 'GEOMETRY', 3);
  PERFORM AddGeometryColumn('waterbody', 'lod0_multi_curve', db_srid, 'MULTICURVE', 3);
  PERFORM AddGeometryColumn('waterbody', 'lod1_multi_curve', db_srid, 'MULTICURVE', 3);

-- Create spatial indexes
  CREATE INDEX CITYOBJECT_SPX                 ON CITYOBJECT               USING GIST ( ENVELOPE gist_geometry_ops_nd );
  CREATE INDEX SURFACE_GEOM_SPX               ON SURFACE_GEOMETRY         USING GIST ( GEOMETRY gist_geometry_ops_nd );
  CREATE INDEX BREAKLINE_RID_SPX              ON BREAKLINE_RELIEF         USING GIST ( RIDGE_OR_VALLEY_LINES gist_geometry_ops_nd );
  CREATE INDEX BREAKLINE_BREAK_SPX            ON BREAKLINE_RELIEF         USING GIST ( BREAK_LINES gist_geometry_ops_nd );
  CREATE INDEX MASSPOINT_REL_SPX              ON MASSPOINT_RELIEF         USING GIST ( RELIEF_POINTS gist_geometry_ops_nd );
  CREATE INDEX TIN_RELF_STOP_SPX              ON TIN_RELIEF               USING GIST ( STOP_LINES gist_geometry_ops_nd );
  CREATE INDEX TIN_RELF_BREAK_SPX             ON TIN_RELIEF               USING GIST ( BREAK_LINES gist_geometry_ops_nd ); 
  CREATE INDEX TIN_RELF_CRTLPTS_SPX           ON TIN_RELIEF               USING GIST ( CONTROL_POINTS gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD0TERR_SPX       ON GENERIC_CITYOBJECT       USING GIST ( LOD0_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD1TERR_SPX       ON GENERIC_CITYOBJECT       USING GIST ( LOD1_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD2TERR_SPX       ON GENERIC_CITYOBJECT       USING GIST ( LOD2_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD3TERR_SPX       ON GENERIC_CITYOBJECT       USING GIST ( LOD3_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD4TERR_SPX       ON GENERIC_CITYOBJECT       USING GIST ( LOD4_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD1REFPNT_SPX     ON GENERIC_CITYOBJECT       USING GIST ( LOD1_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD2REFPNT_SPX     ON GENERIC_CITYOBJECT       USING GIST ( LOD2_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD3REFPNT_SPX     ON GENERIC_CITYOBJECT       USING GIST ( LOD3_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX GENERICCITY_LOD4REFPNT_SPX     ON GENERIC_CITYOBJECT       USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD1TERR_SPX          ON BUILDING                 USING GIST ( LOD1_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD2TERR_SPX          ON BUILDING                 USING GIST ( LOD2_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD3TERR_SPX          ON BUILDING                 USING GIST ( LOD3_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD4TERR_SPX          ON BUILDING                 USING GIST ( LOD4_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD2MULTI_SPX         ON BUILDING                 USING GIST ( LOD2_MULTI_CURVE gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD3MULTI_SPX         ON BUILDING                 USING GIST ( LOD3_MULTI_CURVE gist_geometry_ops_nd );
  CREATE INDEX BUILDING_LOD4MULTI_SPX         ON BUILDING                 USING GIST ( LOD4_MULTI_CURVE gist_geometry_ops_nd );
  CREATE INDEX BUILDING_FURN_LOD4REFPNT_SPX   ON BUILDING_FURNITURE       USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD1TERR_SPX         ON CITY_FURNITURE           USING GIST ( LOD1_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD2TERR_SPX         ON CITY_FURNITURE           USING GIST ( LOD2_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD3TERR_SPX         ON CITY_FURNITURE           USING GIST ( LOD3_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD4TERR_SPX         ON CITY_FURNITURE           USING GIST ( LOD4_TERRAIN_INTERSECTION gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD1REFPNT_SPX       ON CITY_FURNITURE           USING GIST ( LOD1_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD2REFPNT_SPX       ON CITY_FURNITURE           USING GIST ( LOD2_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD3REFPNT_SPX       ON CITY_FURNITURE           USING GIST ( LOD3_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX CITY_FURN_LOD4REFPNT_SPX       ON CITY_FURNITURE           USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX CITYMODEL_SPX                  ON CITYMODEL                USING GIST ( ENVELOPE gist_geometry_ops_nd );
  CREATE INDEX CITYOBJECTGROUP_SPX            ON CITYOBJECTGROUP          USING GIST ( GEOMETRY gist_geometry_ops_nd );
  CREATE INDEX RELIEF_COMPONENT_SPX           ON RELIEF_COMPONENT         USING GIST ( EXTENT );
  CREATE INDEX SOL_VEGETAT_OBJ_LOD1REFPNT_SPX ON SOLITARY_VEGETAT_OBJECT  USING GIST ( LOD1_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX SOL_VEGETAT_OBJ_LOD2REFPNT_SPX ON SOLITARY_VEGETAT_OBJECT  USING GIST ( LOD2_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX SOL_VEGETAT_OBJ_LOD3REFPNT_SPX ON SOLITARY_VEGETAT_OBJECT  USING GIST ( LOD3_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX SOL_VEGETAT_OBJ_LOD4REFPNT_SPX ON SOLITARY_VEGETAT_OBJECT  USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
  CREATE INDEX SURFACE_DATA_SPX               ON SURFACE_DATA             USING GIST ( GT_REFERENCE_POINT );
  CREATE INDEX TRANSPORTATION_COMPLEX_SPX     ON TRANSPORTATION_COMPLEX   USING GIST ( LOD0_NETWORK gist_geometry_ops_nd );
  CREATE INDEX WATERBODY_LOD0MULTI_SPX        ON WATERBODY                USING GIST ( LOD0_MULTI_CURVE gist_geometry_ops_nd );
  CREATE INDEX WATERBODY_LOD1MULTI_SPX        ON WATERBODY                USING GIST ( LOD1_MULTI_CURVE gist_geometry_ops_nd );
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
  action VARCHAR)
RETURNS SETOF void AS 
$$
BEGIN
  EXECUTE 'ALTER TABLE ' || table_name || ' DROP CONSTRAINT ' || fkey_name || 
	         ', ADD CONSTRAINT ' || fkey_name || ' FOREIGN KEY (' || column_name || ') ' ||
                  'REFERENCES ' || ref_table || '(' || ref_column || ') ' ||
                     'ON UPDATE CASCADE ON DELETE ' || action;
					 
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

CREATE OR REPLACE FUNCTION geodb_pkg.util_update_constraints(action VARCHAR DEFAULT 'CASCADE') RETURNS SETOF void AS $$
BEGIN
  IF action <> 'CASCADE' THEN
    action := 'RESTRICT';
	RAISE NOTICE 'Constraints are set to ON DELETE RESTRICT';
  END IF;
  
  PERFORM geodb_pkg.util_on_delete_action('ADDRESS_TO_BUILDING','ADDRESS_TO_BUILDING_FK','BUILDING_ID','BUILDING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('ADDRESS_TO_BUILDING','ADDRESS_TO_BUILDING_ADDRESS_FK','ADDRESS_ID','ADDRESS','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('APPEARANCE','APPEARANCE_CITYMODEL_FK','CITYMODEL_ID','CITYMODEL','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('APPEARANCE','APPEARANCE_CITYOBJECT_FK','CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('APPEAR_TO_SURFACE_DATA','APPEAR_TO_SURFACE_DATA_FK1','APPEARANCE_ID','APPEARANCE','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('APPEAR_TO_SURFACE_DATA','APPEAR_TO_SURFACE_DATA_FK','SURFACE_DATA_ID','SURFACE_DATA','ID',action);  
  PERFORM geodb_pkg.util_on_delete_action('APPEAR_TO_SURFACE_DATA','APPEAR_TO_SURFACE_DATA_FK','SURFACE_DATA_ID','SURFACE_DATA','ID',action);    
  PERFORM geodb_pkg.util_on_delete_action('BREAKLINE_RELIEF','BREAKLINE_RELIEF_FK','ID','RELIEF_COMPONENT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK3','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK1','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_SURFACE_GEOMETRY_FK2','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_BUILDING_FK','BUILDING_PARENT_ID','BUILDING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING','BUILDING_BUILDING_FK1','BUILDING_ROOT_ID','BUILDING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_FK1','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_FK2','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_FK','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_FURNITURE','BUILDING_FURNITURE_ROOM_FK','ROOM_ID','ROOM','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK3','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_ROOM_FK','ROOM_ID','ROOM','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK4','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK1','BUILDING_ID','BUILDING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('BUILDING_INSTALLATION','BUILDING_INSTALLATION_FK2','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT','CITYOBJECT_OBJECTCLASS_FK','CLASS_ID','OBJECTCLASS','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECTGROUP','CITYOBJECT_GROUP_FK','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECTGROUP','CITYOBJECTGROUP_CITYOBJECT_FK','PARENT_CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECTGROUP','CITYOBJECTGROUP_CITYOBJECT_FK1','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_GENERICATTRIB','CITYOBJECT_GENERICATTRIB_FK','CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_GENERICATTRIB','CITYOBJECT_GENERICATTRIB_FK1','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_MEMBER','CITYOBJECT_MEMBER_CITYMODEL_FK','CITYMODEL_ID','CITYMODEL','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITYOBJECT_MEMBER','CITYOBJECT_MEMBER_FK','CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK1','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK2','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK3','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK4','LOD1_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK5','LOD2_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK6','LOD3_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_FK7','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('CITY_FURNITURE','CITY_FURNITURE_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('EXTERNAL_REFERENCE','EXTERNAL_REFERENCE_FK','CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERALIZATION','GENERALIZATION_FK1','GENERALIZES_TO_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERALIZATION','GENERALIZATION_FK','CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK1','LOD1_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK2','LOD2_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK3','LOD3_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK4','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK5','LOD0_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK6','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK7','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK8','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK9','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GENERIC_CITYOBJECT','GENERIC_CITYOBJECT_FK10','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GROUP_TO_CITYOBJECT','GROUP_TO_CITYOBJECT_FK','CITYOBJECT_ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('GROUP_TO_CITYOBJECT','GROUP_TO_CITYOBJECT_FK1','CITYOBJECTGROUP_ID','CITYOBJECTGROUP','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('IMPLICIT_GEOMETRY','IMPLICIT_GEOMETRY_FK','RELATIVE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK','LOD0_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK1','LOD1_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK2','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK3','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('LAND_USE','LAND_USE_SURFACE_GEOMETRY_FK4','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('MASSPOINT_RELIEF','MASSPOINT_RELIEF_FK','ID','RELIEF_COMPONENT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OBJECTCLASS','OBJECTCLASS_OBJECTCLASS_FK','SUPERCLASS_ID','OBJECTCLASS','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_SURFACE_GEOMETRY_FK1','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_SURFACE_GEOMETRY_FK','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OPENING','OPENING_ADDRESS_FK','ADDRESS_ID','ADDRESS','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OPENING_TO_THEM_SURFACE','OPENING_TO_THEMATIC_SURFACE_FK','OPENING_ID','OPENING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('OPENING_TO_THEM_SURFACE','OPENING_TO_THEMATIC_SURFAC_FK1','THEMATIC_SURFACE_ID','THEMATIC_SURFACE','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK1','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK2','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_FK3','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('PLANT_COVER','PLANT_COVER_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('RASTER_RELIEF','RASTER_RELIEF_FK','RELIEF_ID','RELIEF','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_COMPONENT','RELIEF_COMPONENT_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_FEATURE','RELIEF_FEATURE_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_FEAT_TO_REL_COMP','RELIEF_FEAT_TO_REL_COMP_FK','RELIEF_COMPONENT_ID','RELIEF_COMPONENT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('RELIEF_FEAT_TO_REL_COMP','RELIEF_FEAT_TO_REL_COMP_FK1','RELIEF_FEATURE_ID','RELIEF_FEATURE','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('ROOM','ROOM_BUILDING_FK','BUILDING_ID','BUILDING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('ROOM','ROOM_SURFACE_GEOMETRY_FK','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('ROOM','ROOM_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK1','LOD1_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK2','LOD2_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK3','LOD3_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK4','LOD4_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK5','LOD1_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK6','LOD2_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK7','LOD3_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SOLITARY_VEGETAT_OBJECT','SOLITARY_VEGETAT_OBJECT_FK8','LOD4_IMPLICIT_REP_ID','IMPLICIT_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SURFACE_GEOMETRY','SURFACE_GEOMETRY_FK','PARENT_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('SURFACE_GEOMETRY','SURFACE_GEOMETRY_FK1','ROOT_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TEXTUREPARAM','TEXTUREPARAM_SURFACE_GEOM_FK','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TEXTUREPARAM','TEXTUREPARAM_SURFACE_DATA_FK','SURFACE_DATA_ID','SURFACE_DATA','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_ROOM_FK','ROOM_ID','ROOM','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_BUILDING_FK','BUILDING_ID','BUILDING','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_FK','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_FK1','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('THEMATIC_SURFACE','THEMATIC_SURFACE_FK2','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TIN_RELIEF','TIN_RELIEF_SURFACE_GEOMETRY_FK','SURFACE_GEOMETRY_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TIN_RELIEF','TIN_RELIEF_RELIEF_COMPONENT_FK','ID','RELIEF_COMPONENT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK1','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK2','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRAFFIC_AREA','TRAFFIC_AREA_FK3','TRANSPORTATION_COMPLEX_ID','TRANSPORTATION_COMPLEX','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK1','LOD1_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK2','LOD2_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK3','LOD3_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('TRANSPORTATION_COMPLEX','TRANSPORTATION_COMPLEX_FK4','LOD4_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_CITYOBJECT_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK','LOD1_SOLID_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK1','LOD2_SOLID_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK2','LOD3_SOLID_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK3','LOD4_SOLID_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK4','LOD0_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK5','LOD1_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBODY','WATERBODY_SURFACE_GEOMETRY_FK4','LOD0_MULTI_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOD_TO_WATERBND_SRF','WATERBOD_TO_WATERBND_FK','WATERBOUNDARY_SURFACE_ID','WATERBOUNDARY_SURFACE','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOD_TO_WATERBND_SRF','WATERBOD_TO_WATERBND_FK1','WATERBODY_ID','WATERBODY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SRF_CITYOBJ_FK','ID','CITYOBJECT','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SURFACE_FK','LOD2_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SURFACE_FK1','LOD3_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
  PERFORM geodb_pkg.util_on_delete_action('WATERBOUNDARY_SURFACE','WATERBOUNDARY_SURFACE_FK2','LOD4_SURFACE_ID','SURFACE_GEOMETRY','ID',action);
END;
$$
LANGUAGE plpgsql;