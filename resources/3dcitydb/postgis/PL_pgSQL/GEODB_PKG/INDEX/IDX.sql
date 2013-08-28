-- IDX.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--
-- Conversion:	Felix Kunde <fkunde@virtualcitysystems.de>
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
-- Creates utility methods for creating/droping spatial/normal indexes.
-- All functions are part of the geodb_pkg.schema and INDEX-"Package" 
-- They start with the prefix "idx_" to guarantee a better overview 
-- in the PGAdminIII-Tool.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 1.0.0     2013-02-22   PostGIS version    CNag	  FKun
--

/*****************************************************************
* TYPE INDEX_OBJ
* 
* global type to store information relevant to indexes
******************************************************************/
DROP TYPE IF EXISTS geodb_pkg.INDEX_OBJ CASCADE;
CREATE TYPE geodb_pkg.INDEX_OBJ AS (
    index_name          VARCHAR(100),
    table_name 			VARCHAR(100),
    attribute_name 		VARCHAR(100),
    type       			NUMERIC(1),
    srid               	INTEGER,
    is_3d 				NUMERIC(1, 0)
); 

/******************************************************************
* constructors for INDEX_OBJ instances
* 
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_construct_spatial_3d
    (ind_name VARCHAR, tab_name VARCHAR, att_name VARCHAR, crs INTEGER DEFAULT 0) RETURNS geodb_pkg.INDEX_OBJ AS $$
DECLARE
    idx geodb_pkg.INDEX_OBJ;
BEGIN
    idx.index_name := ind_name;
    idx.table_name := tab_name;
    idx.attribute_name := att_name;
    idx.type := 1;
    idx.srid := crs;
    idx.is_3d := 1;

    RETURN idx;
END; 
$$
LANGUAGE 'plpgsql' IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION geodb_pkg.idx_construct_spatial_2d
    (ind_name VARCHAR, tab_name VARCHAR, att_name VARCHAR, crs INTEGER DEFAULT 0) RETURNS geodb_pkg.INDEX_OBJ AS $$
DECLARE
    idx geodb_pkg.INDEX_OBJ;
BEGIN
    idx.index_name := ind_name;   
    idx.table_name := tab_name;
    idx.attribute_name := att_name;
    idx.type := 1;
    idx.srid := crs;
    idx.is_3d := 0;

    RETURN idx;
END; 
$$
LANGUAGE 'plpgsql' IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION geodb_pkg.idx_construct_normal
    (ind_name VARCHAR, tab_name VARCHAR, att_name VARCHAR, crs INTEGER DEFAULT 0) RETURNS geodb_pkg.INDEX_OBJ AS $$
DECLARE
    idx geodb_pkg.INDEX_OBJ;
BEGIN
    idx.index_name := ind_name;
    idx.table_name := tab_name;
    idx.attribute_name := att_name;
    idx.type := 0;
    idx.srid := crs;
    idx.is_3d := 0;

    RETURN idx;
END;
$$
LANGUAGE 'plpgsql' IMMUTABLE STRICT;

/******************************************************************
* INDEX_TABLE that holds INDEX_OBJ instances
* 
******************************************************************/
DROP TABLE IF EXISTS geodb_pkg.INDEX_TABLE;
CREATE TABLE geodb_pkg.INDEX_TABLE (
	ID			SERIAL NOT NULL,
	idx_obj		geodb_pkg.INDEX_OBJ
);

/******************************************************************
* Populate INDEX_TABLE with INDEX_OBJ instances
* 
******************************************************************/
INSERT INTO geodb_pkg.index_table VALUES (1, geodb_pkg.idx_construct_spatial_3d('cityobject_spx', 'cityobject', 'envelope'));
INSERT INTO geodb_pkg.index_table VALUES (2, geodb_pkg.idx_construct_spatial_3d('surface_geom_spx', 'surface_geometry', 'geometry'));
INSERT INTO geodb_pkg.index_table VALUES (3, geodb_pkg.idx_construct_normal('cityobject_inx', 'cityobject', 'gmlid, gmlid_codespace'));
INSERT INTO geodb_pkg.index_table VALUES (4, geodb_pkg.idx_construct_normal('surface_geometry_inx', 'surface_geometry', 'gmlid, gmlid_codespace'));
INSERT INTO geodb_pkg.index_table VALUES (5, geodb_pkg.idx_construct_normal('appearance_inx', 'appearance', 'gmlid, gmlid_codespace'));
INSERT INTO geodb_pkg.index_table VALUES (6, geodb_pkg.idx_construct_normal('surface_data_inx', 'surface_data', 'gmlid, gmlid_codespace'));
 
/*****************************************************************
* index_status
* 
* @param idx index to retrieve status from
* @return VARCHAR string represntation of status, may include
*                  'DROPPED', 'VALID', 'INVALID', 'FAILED'
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_index_status(idx geodb_pkg.INDEX_OBJ) RETURNS VARCHAR AS $$
DECLARE
    is_valid BOOLEAN;
    status VARCHAR(20);
BEGIN
    EXECUTE 'SELECT DISTINCT pgi.indisvalid FROM pg_index pgi
        JOIN pg_stat_user_indexes pgsui ON pgsui.relid=pgi.indrelid
        JOIN pg_attribute pga ON pga.attrelid=pgi.indexrelid
        WHERE pgsui.indexrelname=$1' INTO is_valid USING idx.index_name;

    IF is_valid is null THEN
        status := 'DROPPED';
    ELSIF is_valid = true THEN
        status := 'VALID';
    ELSE
        status := 'INVALID';
    END IF;
    
    RETURN status;

EXCEPTION
    WHEN OTHERS THEN
        RETURN 'FAILED';
END;
$$
LANGUAGE plpgsql;
  
/*****************************************************************
* index_status
* 
* @param table_name table_name of index to retrieve status from
* @param column_name column_name of index to retrieve status from
* @return VARCHAR string representation of status, may include
*                  'DROPPED', 'VALID', 'INVALID', 'FAILED'
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_index_status(table_name VARCHAR, column_name VARCHAR) RETURNS VARCHAR AS $$
DECLARE
    is_valid BOOLEAN;
    status VARCHAR(20);
BEGIN   
    EXECUTE 'SELECT DISTINCT pgi.indisvalid FROM pg_index pgi
        JOIN pg_stat_user_indexes pgsui ON pgsui.relid=pgi.indrelid
        JOIN pg_attribute pga ON pga.attrelid=pgi.indexrelid
        WHERE pgsui.relname=$1 AND pga.attname=$2' INTO is_valid USING lower(table_name), lower(column_name);

    IF is_valid is null THEN
        status := 'DROPPED';
    ELSIF is_valid = true THEN
        status := 'VALID';
    ELSE
        status := 'INVALID';
    END IF;

    RETURN status;

EXCEPTION
    WHEN OTHERS THEN
        RETURN 'FAILED';
END;
$$
LANGUAGE plpgsql;
    
/*****************************************************************
* create_index
* 
* @param idx index to create
* @param params additional parameters for the index to be created
* @return VARCHAR sql error code and message, 0 for no errors
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_create_index(idx geodb_pkg.INDEX_OBJ, params VARCHAR DEFAULT '') RETURNS VARCHAR AS $$
DECLARE
    create_ddl VARCHAR(1000);
    SPATIAL CONSTANT NUMERIC(1) := 1;
BEGIN
    IF geodb_pkg.idx_index_status(idx) <> 'VALID' THEN
        PERFORM geodb_pkg.idx_drop_index(idx);

        BEGIN
            IF idx.type = SPATIAL THEN
                create_ddl := 'CREATE INDEX ' || idx.index_name || ' ON ' || idx.table_name || ' USING GIST (' || idx.attribute_name || ' gist_geometry_ops_nd)';
            ELSE
                create_ddl := 'CREATE INDEX ' || idx.index_name || ' ON ' || idx.table_name || '(' || idx.attribute_name || ')';
            END IF;

            IF params <> '' THEN
                create_ddl := create_ddl || ' ' || params;
            END IF;

            EXECUTE create_ddl;

        EXCEPTION
            WHEN OTHERS THEN
                RETURN SQLSTATE || ' - ' || SQLERRM;
        END;
    END IF;

    RETURN '0';
END;
$$
LANGUAGE plpgsql;
  
/****************************************************************
* drop_index
* 
* @param idx index to drop
* @param is_versioned TRUE IF database table is version-enabled
* @return VARCHAR sql error code and message, 0 for no errors
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_drop_index(idx geodb_pkg.INDEX_OBJ) RETURNS VARCHAR AS $$
DECLARE
    index_name VARCHAR(100);
BEGIN
    IF geodb_pkg.idx_index_status(idx) <> 'DROPPED' THEN
        BEGIN
            EXECUTE 'DROP INDEX IF EXISTS ' || idx.index_name;

        EXCEPTION
            WHEN OTHERS THEN
                RETURN SQLSTATE || ' - ' || SQLERRM;
        END;
    END IF;

    RETURN '0';
END;
$$
LANGUAGE plpgsql;

/*****************************************************************
* create_indexes
* private convience method for invoking create_index on indexes 
* of same index type
* 
* @param type type of index, e.g. 1 for spatial, 0 for normal
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_create_indexes(type INTEGER) RETURNS text[] AS $$
DECLARE
    log text[] := '{}';
    sql_error_msg VARCHAR;
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM geodb_pkg.index_table LOOP
        IF (rec.idx_obj).type = type THEN
            sql_error_msg := geodb_pkg.idx_create_index(rec.idx_obj);
            log := array_append(log, geodb_pkg.idx_index_status(rec.idx_obj) || ':' || (rec.idx_obj).index_name || ':' || (rec.idx_obj).table_name || ':' || (rec.idx_obj).attribute_name || ':' || sql_error_msg);
        END IF;
    END LOOP;

    RETURN log;
  END;
$$
LANGUAGE plpgsql;
  
/*****************************************************************
* drop_indexes
* private convience method for invoking drop_index on indexes 
* of same index type
* 
* @param type type of index, e.g. 1 for spatial, 0 for normal
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_drop_indexes(type INTEGER) RETURNS text[] AS $$
DECLARE
    log text[] := '{}';
    sql_error_msg VARCHAR;
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM geodb_pkg.index_table LOOP
        IF (rec.idx_obj).type = type THEN
            sql_error_msg := geodb_pkg.idx_drop_index(rec.idx_obj);
            log := array_append(log, geodb_pkg.idx_index_status(rec.idx_obj) || ':' || (rec.idx_obj).index_name || ':' || (rec.idx_obj).table_name || ':' || (rec.idx_obj).attribute_name || ':' || sql_error_msg);
        END IF;
    END LOOP;

    RETURN log;
END;
$$
LANGUAGE plpgsql;
  
/******************************************************************
* status_spatial_indexes
* 
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_status_spatial_indexes() RETURNS text[] AS $$
DECLARE
    log text[] := '{}';
    status VARCHAR(20);
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM geodb_pkg.index_table LOOP
        IF (rec.idx_obj).type = 1 THEN
            status := geodb_pkg.idx_index_status(rec.idx_obj);
            log := array_append(log, status || ':' || (rec.idx_obj).index_name || ':' || (rec.idx_obj).table_name || ':' || (rec.idx_obj).attribute_name);
        END IF;
    END LOOP;   

    RETURN log;
END;
$$
LANGUAGE plpgsql;
  
/******************************************************************
* status_normal_indexes
* 
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_status_normal_indexes() RETURNS text[] AS $$
DECLARE
    log text[] := '{}';
    status VARCHAR(20);
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM geodb_pkg.index_table LOOP
        IF (rec.idx_obj).type = 0 THEN
            status := geodb_pkg.idx_index_status(rec.idx_obj);
            log := array_append(log, status || ':' || (rec.idx_obj).index_name || ':' || (rec.idx_obj).table_name || ':' || (rec.idx_obj).attribute_name);
        END IF;
    END LOOP;

    RETURN log;
END;
$$
LANGUAGE plpgsql;

/******************************************************************
* create_spatial_indexes
* convience method for invoking create_index on all spatial 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_create_spatial_indexes() RETURNS text[] AS $$
BEGIN
    RETURN geodb_pkg.idx_create_indexes(1);
END;
$$
LANGUAGE plpgsql;

/******************************************************************
* drop_spatial_indexes
* convience method for invoking drop_index on all spatial 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_drop_spatial_indexes() RETURNS text[] AS $$
BEGIN
    RETURN geodb_pkg.idx_drop_indexes(1);
END;
$$
LANGUAGE plpgsql;

/******************************************************************
* create_normal_indexes
* convience method for invoking create_index on all normal 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_create_normal_indexes() RETURNS text[] AS $$
BEGIN
    RETURN geodb_pkg.idx_create_indexes(0);
END;
$$
LANGUAGE plpgsql;

/******************************************************************
* drop_normal_indexes
* convience method for invoking drop_index on all normal 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_drop_normal_indexes() RETURNS text[] AS $$
BEGIN
    RETURN geodb_pkg.idx_drop_indexes(0);
END; 
$$
LANGUAGE plpgsql;

/*****************************************************************
* get_index
* convience method for getting an index object 
* given the table and column it indexes
* 
* @param table_name
* @param attribute_name
* @return INDEX_OBJ
******************************************************************/
CREATE OR REPLACE FUNCTION geodb_pkg.idx_get_index(tab_name VARCHAR, column_name VARCHAR) RETURNS geodb_pkg.INDEX_OBJ AS $$
DECLARE
    idx geodb_pkg.INDEX_OBJ;
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM geodb_pkg.index_table LOOP
        IF (rec.idx_obj).attribute_name = column_name AND (rec.idx_obj).table_name = tab_name THEN
            idx := rec.idx_obj;
        END IF;
    END LOOP;

    RETURN idx;
END;
$$
LANGUAGE plpgsql;