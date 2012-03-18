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
-- DROPs package "geodb_util" containing utility methods for applications
-- and further subpackges. Therefore, "geodb_util" might be a dependency
-- for other packages and/or methods.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion 
-- 1.0.0     2008-09-10   release version    CNag
-- 1.1.0     2012-02-29   update to 2.0.6    CNag	  FKun
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
* no 3D-Coord.-Reference-System defined in the spatial_ref_sys-table of PostGIS 2.0 
*
* @param srid the SRID of the coordinate system to be checked
* @RETURN NUMERIC the boolean result encoded as NUMERIC: 0 = false, 1 = true                
******************************************************************/
/*
CREATE OR REPLACE FUNCTION geodb_pkg.util_is_coord_ref_sys_3d(srid INTEGER) RETURNS INTEGER AS $$
  DECLARE
    s ALIAS FOR $1;
	is_3d INTEGER := 0;
  BEGIN
	EXECUTE 'SELECT COUNT(*) from SDO_CRS_COMPOUND where SRID=$1' INTO is_3d USING s;
    IF is_3d = 0 THEN
      EXECUTE 'SELECT COUNT(*) from SDO_CRS_GEOGRAPHIC3D where SRID=$1' INTO is_3d USING s;
    END IF;
    
    RETURN is_3d;
  END;
$$
LANGUAGE plpgsql;
*/  
/******************************************************************
* is_db_coord_ref_sys_3d
*
* @RETURN NUMERIC the boolean result encoded as NUMERIC: 0 = false, 1 = true                
******************************************************************/
/*
CREATE OR REPLACE FUNCTION geodb_pkg.util_is_db_coord_ref_sys_3d() RETURNS INTEGER AS $$
  DECLARE
    srid INTEGER;
  BEGIN
    EXECUTE 'SELECT srid from DATABASE_SRS' INTO srid;
    RETURN util_is_coord_ref_sys_3d(srid);
  END;
$$
LANGUAGE plpgsql;
*/
/*******************************************************************
* to_2d - In PostGIS has a function called ST_Force_2d, which cuts the z-values from the geometry.
*
* PL/SQL-code taken from http://forums.oracle.com/forums/thread.jspa?messageID=960492&#960492
*
*******************************************************************/
/*
CREATE OR REPLACE FUNCTION geodb_pkg.util_to_2d (geom GEOMETRY, srid INTEGER) RETURNS geometry AS $$
  DECLARE
    geom_2d GEOMETRY;
    geom_poi VARCHAR;
    dim_count INTEGER; -- NUMERIC of dimensions in layer
    n_points INTEGER; -- NUMERIC of points in ordinates array
    n_ordinates INTEGER; -- NUMERIC of ordinates
    i INTEGER;
    j DOUBLE PRECISION;
    k DOUBLE PRECISION;
  BEGIN
    -- If the input geometry is null, just RETURN null
    IF geom IS NULL THEN 
		RETURN null;
    END IF;
    
	SELECT ST_Dimension(geom) INTO dim_count;

    IF dim_count = 2 THEN
      -- Nothing to do, geometry is already 2D
      RETURN geom;
    END IF;
  
    -- Construct and prepare the output geometry
    SELECT ST_GeomFromText((ST_AsText(geom)), srid) INTO geom_2d;

    -- Process the point structure
    IF ST_GeometryType(geom) = 'POINT' THEN
      PERFORM ST_Force_2d(geom);
    ELSE
      -- It is not a point  
      -- Process the ordinates array
  
      -- Prepare the size of the output array
      n_points := ST_NPoints(geom);
      FOR i IN 1..n_points LOOP
		EXECUTE 'SELECT ST_X(ST_PointN('|| geom || ', ' || i || '))' INTO j;
		EXECUTE 'SELECT ST_Y(ST_PointN('|| geom || ', ' || i || '))' INTO k;
		geom_poi := 'POINT(' || k || ' ' || j || ' 0)';
		EXECUTE 'SELECT ST_SetPoint(' || geom || ", " || i || ', ' || geom_poi ||')' INTO geom_2d;
	  END LOOP;
  
    END IF;
  
    RETURN geom_2d;
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE INFO 'failed to execute to_2d';
      RETURN null;
  END;
$$
LANGUAGE plpgsql;
*/

CREATE OR REPLACE FUNCTION geodb_pkg.util_update_db_srid (db_srid INTEGER, db_gml_srs_name VARCHAR) RETURNS setof void AS $$
BEGIN
	UPDATE DATABASE_SRS SET SRID=db_srid, GML_SRS_NAME=db_gml_srs_name;
	
	PERFORM updategeometrysrid('cityobject', 'envelope', db_srid);
	PERFORM updategeometrysrid('surface_geometry', 'geometry', db_srid);
	PERFORM updategeometrysrid('breakline_relief', 'ridge_or_valley_lines', db_srid);
	PERFORM updategeometrysrid('breakline_relief', 'break_lines', db_srid);
	PERFORM updategeometrysrid('masspoint_relief', 'relief_points', db_srid);
	PERFORM updategeometrysrid('orthophoto_imp', 'footprint', db_srid);
	PERFORM updategeometrysrid('tin_relief', 'stop_lines', db_srid);
	PERFORM updategeometrysrid('tin_relief', 'break_lines', db_srid);
	PERFORM updategeometrysrid('tin_relief', 'control_points', db_srid);
	PERFORM updategeometrysrid('raster_relief_imp', 'footprint', db_srid);
	PERFORM updategeometrysrid('cityobject_genericattrib', 'geomval', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod0_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod1_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod2_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod3_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod4_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod0_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod1_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod2_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod3_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('generic_cityobject', 'lod4_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('address', 'multi_point', db_srid);
	PERFORM updategeometrysrid('building', 'lod1_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('building', 'lod2_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('building', 'lod3_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('building', 'lod4_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('building', 'lod2_multi_curve', db_srid);
	PERFORM updategeometrysrid('building', 'lod3_multi_curve', db_srid);
	PERFORM updategeometrysrid('building', 'lod4_multi_curve', db_srid);
	PERFORM updategeometrysrid('building_furniture', 'lod4_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod1_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod2_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod3_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod4_terrain_intersection', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod1_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod2_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod3_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('city_furniture', 'lod4_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('citymodel', 'envelope', db_srid);
	PERFORM updategeometrysrid('cityobjectgroup', 'geometry', db_srid);
	PERFORM updategeometrysrid('relief_component', 'extent', db_srid);
	PERFORM updategeometrysrid('solitary_vegetat_object', 'lod1_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('solitary_vegetat_object', 'lod2_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('solitary_vegetat_object', 'lod3_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('solitary_vegetat_object', 'lod4_implicit_ref_point', db_srid);
	PERFORM updategeometrysrid('surface_data', 'gt_reference_point', db_srid);
	PERFORM updategeometrysrid('transportation_complex', 'lod0_network', db_srid);
	PERFORM updategeometrysrid('waterbody', 'lod0_multi_curve', db_srid);
	PERFORM updategeometrysrid('waterbody', 'lod1_multi_curve', db_srid);

END;
$$ 
LANGUAGE plpgsql;
	

CREATE OR REPLACE FUNCTION geodb_pkg.util_change_db_srid (db_srid INTEGER, db_gml_srs_name VARCHAR) RETURNS setof void AS $$
BEGIN
-- Drop spatial indexes
	DROP INDEX CITYOBJECT_SPX;
	DROP INDEX SURFACE_GEOM_SPX;
	DROP INDEX BREAKLINE_RID_SPX;
	DROP INDEX BREAKLINE_BREAK_SPX;
	DROP INDEX MASSPOINT_REL_SPX;
	DROP INDEX ORTHOPHOTO_IMP_SPX;
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
	PERFORM DropGeometryColumn('orthophoto_imp', 'footprint');
	PERFORM DropGeometryColumn('tin_relief', 'stop_lines');
	PERFORM DropGeometryColumn('tin_relief', 'break_lines');
	PERFORM DropGeometryColumn('tin_relief', 'control_points');
	PERFORM DropGeometryColumn('raster_relief_imp', 'footprint');
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
	PERFORM AddGeometryColumn('orthophoto_imp', 'footprint', db_srid, 'POLYGON', 3);
	PERFORM AddGeometryColumn('tin_relief', 'stop_lines', db_srid, 'MULTICURVE', 3);
	PERFORM AddGeometryColumn('tin_relief', 'break_lines', db_srid, 'MULTICURVE', 3);
	PERFORM AddGeometryColumn('tin_relief', 'control_points', db_srid, 'MULTIPOINT', 3);
	PERFORM AddGeometryColumn('raster_relief_imp', 'footprint', db_srid, 'POLYGON', 3);
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
	CREATE INDEX CITYOBJECT_SPX 				ON CITYOBJECT 					USING GIST ( ENVELOPE gist_geometry_ops_nd );
	CREATE INDEX SURFACE_GEOM_SPX 				ON SURFACE_GEOMETRY 			USING GIST ( GEOMETRY gist_geometry_ops_nd );
	CREATE INDEX BREAKLINE_RID_SPX 				ON BREAKLINE_RELIEF 			USING GIST ( RIDGE_OR_VALLEY_LINES gist_geometry_ops_nd );
	CREATE INDEX BREAKLINE_BREAK_SPX 			ON BREAKLINE_RELIEF 			USING GIST ( BREAK_LINES gist_geometry_ops_nd );
	CREATE INDEX MASSPOINT_REL_SPX 				ON MASSPOINT_RELIEF 			USING GIST ( RELIEF_POINTS gist_geometry_ops_nd );
	CREATE INDEX ORTHOPHOTO_IMP_SPX				ON ORTHOPHOTO_IMP 				USING GIST ( FOOTPRINT );
	CREATE INDEX TIN_RELF_STOP_SPX 				ON TIN_RELIEF 					USING GIST ( STOP_LINES gist_geometry_ops_nd );
	CREATE INDEX TIN_RELF_BREAK_SPX 			ON TIN_RELIEF 					USING GIST ( BREAK_LINES gist_geometry_ops_nd ); 
	CREATE INDEX TIN_RELF_CRTLPTS_SPX			ON TIN_RELIEF 					USING GIST ( CONTROL_POINTS gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD0TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD0_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD1TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD1_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD2TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD2_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD3TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD3_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD4TERR_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD4_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD1REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD1_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD2REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD2_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD3REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD3_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX GENERICCITY_LOD4REFPNT_SPX		ON GENERIC_CITYOBJECT 			USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD1TERR_SPX 			ON BUILDING			 			USING GIST ( LOD1_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD2TERR_SPX 			ON BUILDING			 			USING GIST ( LOD2_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD3TERR_SPX			ON BUILDING			 			USING GIST ( LOD3_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD4TERR_SPX			ON BUILDING			 			USING GIST ( LOD4_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD2MULTI_SPX			ON BUILDING			 			USING GIST ( LOD2_MULTI_CURVE gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD3MULTI_SPX			ON BUILDING			 			USING GIST ( LOD3_MULTI_CURVE gist_geometry_ops_nd );
	CREATE INDEX BUILDING_LOD4MULTI_SPX			ON BUILDING			 			USING GIST ( LOD4_MULTI_CURVE gist_geometry_ops_nd );
	CREATE INDEX BUILDING_FURN_LOD4REFPNT_SPX 	ON BUILDING_FURNITURE 			USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD1TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD1_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD2TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD2_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD3TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD3_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD4TERR_SPX			ON CITY_FURNITURE 				USING GIST ( LOD4_TERRAIN_INTERSECTION gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD1REFPNT_SPX		ON CITY_FURNITURE 				USING GIST ( LOD1_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD2REFPNT_SPX		ON CITY_FURNITURE 				USING GIST ( LOD2_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD3REFPNT_SPX		ON CITY_FURNITURE 				USING GIST ( LOD3_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX CITY_FURN_LOD4REFPNT_SPX 		ON CITY_FURNITURE 				USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX CITYMODEL_SPX	 				ON CITYMODEL 					USING GIST ( ENVELOPE gist_geometry_ops_nd );
	CREATE INDEX CITYOBJECTGROUP_SPX			ON CITYOBJECTGROUP 				USING GIST ( GEOMETRY gist_geometry_ops_nd );
	CREATE INDEX RELIEF_COMPONENT_SPX 			ON RELIEF_COMPONENT 			USING GIST ( EXTENT );
	CREATE INDEX SOL_VEGETAT_OBJ_LOD1REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD1_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX SOL_VEGETAT_OBJ_LOD2REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD2_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX SOL_VEGETAT_OBJ_LOD3REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD3_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX SOL_VEGETAT_OBJ_LOD4REFPNT_SPX	ON SOLITARY_VEGETAT_OBJECT 		USING GIST ( LOD4_IMPLICIT_REF_POINT gist_geometry_ops_nd );
	CREATE INDEX SURFACE_DATA_SPX 				ON SURFACE_DATA 				USING GIST ( GT_REFERENCE_POINT );
	CREATE INDEX TRANSPORTATION_COMPLEX_SPX		ON TRANSPORTATION_COMPLEX 		USING GIST ( LOD0_NETWORK gist_geometry_ops_nd );
	CREATE INDEX WATERBODY_LOD0MULTI_SPX		ON WATERBODY 					USING GIST ( LOD0_MULTI_CURVE gist_geometry_ops_nd );
	CREATE INDEX WATERBODY_LOD1MULTI_SPX		ON WATERBODY 					USING GIST ( LOD1_MULTI_CURVE gist_geometry_ops_nd );

END;
$$ 
LANGUAGE plpgsql;