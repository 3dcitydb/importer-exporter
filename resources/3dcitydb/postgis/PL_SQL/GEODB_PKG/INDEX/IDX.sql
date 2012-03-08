-- IDX.sql
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
-- Creates package "geodb_idx" containing utility methods for creating/droping
-- spatial/normal indexes on versioned/unversioned tables.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 1.0.0     2012-01-27   release version    CNag	  FKun
--

/*****************************************************************
* TYPE INDEX_OBJ
* 
* global type to store information relevant to indexes
******************************************************************/

CREATE TYPE geodb_pkg.INDEX_OBJ AS (
   index_name 				VARCHAR(100),
   table_name 				VARCHAR(100),
   attribute_name 			VARCHAR(100),
   type       				NUMERIC(1),
   srid               		INTEGER,
   is_3d 					NUMERIC(1, 0)
); 

/******************************************************************
* constructors for INDEX_OBJ instances
* 
******************************************************************/

CREATE FUNCTION geodb_pkg.idx_construct_spatial_3d
	(index_name VARCHAR, table_name VARCHAR, attribute_name VARCHAR, srid INTEGER DEFAULT 0) RETURNS geodb_pkg.INDEX_OBJ AS $$
  DECLARE
	iObj geodb_pkg.INDEX_OBJ;
  BEGIN
	iObj.index_name := index_name;
	iObj.table_name := table_name;
	iObj.attribute_name := attribute_name;
	iObj.type := 1;
	iObj.srid := srid;
	iObj.is_3d := 1;
	
	RETURN iObj;
  END; 
$$
LANGUAGE 'plpgsql';

CREATE FUNCTION geodb_pkg.idx_construct_spatial_2d
    (index_name VARCHAR, table_name VARCHAR, attribute_name VARCHAR, srid INTEGER DEFAULT 0) RETURNS geodb_pkg.INDEX_OBJ AS $$
  DECLARE
	iObj geodb_pkg.INDEX_OBJ;
  BEGIN
	iObj.index_name := index_name;
	iObj.table_name := table_name;
	iObj.attribute_name := attribute_name;
	iObj.type := 1;
	iObj.srid := srid;
	iObj.is_3d := 0;
	
	RETURN iObj;
  END; 
$$
LANGUAGE 'plpgsql';

CREATE FUNCTION geodb_pkg.idx_construct_normal
    (index_name VARCHAR, table_name VARCHAR, attribute_name VARCHAR, srid INTEGER DEFAULT 0) RETURNS geodb_pkg.INDEX_OBJ AS $$
  DECLARE
	iObj geodb_pkg.INDEX_OBJ;
  BEGIN
	iObj.index_name := index_name;
	iObj.table_name := table_name;
	iObj.attribute_name := attribute_name;
	iObj.type := 0;
	iObj.srid := srid;
	iObj.is_3d := 0;
	
	RETURN iObj;
  END;
$$
LANGUAGE 'plpgsql';
     
/******************************************************************
* Index_Table that holds INDEX_OBJ instances
* 
******************************************************************/

CREATE TABLE geodb_pkg.INDEX_TABLE (
	ID_idx_obj			SERIAL NOT NULL,
	idx_obj				geodb_pkg.INDEX_OBJ
);

ALTER TABLE geodb_pkg.INDEX_TABLE
ADD CONSTRAINT idx_obj_pk PRIMARY KEY
(
ID_idx_obj
);

INSERT INTO geodb_pkg.index_table VALUES (1, geodb_pkg.idx_construct_spatial_3d('cityobject_spx', 'cityobject', 'envelope'));
INSERT INTO geodb_pkg.index_table VALUES (2, geodb_pkg.idx_construct_spatial_3d('surface_geom_spx', 'surface_geometry', 'geometry'));
INSERT INTO geodb_pkg.index_table VALUES (3, geodb_pkg.idx_construct_normal('cityobject_inx', 'cityobject', 'gmlid, gmlid_codespace'));
INSERT INTO geodb_pkg.index_table VALUES (4, geodb_pkg.idx_construct_normal('surface_geometry_inx', 'surface_geometry', 'gmlid, gmlid_codespace'));
INSERT INTO geodb_pkg.index_table VALUES (5, geodb_pkg.idx_construct_normal('appereance_inx', 'appearance', 'gmlid, gmlid_codespace'));
INSERT INTO geodb_pkg.index_table VALUES (6, geodb_pkg.idx_construct_normal('surface_data_inx', 'surface_data', 'gmlid, gmlid_codespace'));
 
/*****************************************************************
* index_status
* 
* @param idx index to retrieve status from
* @return VARCHAR string represntation of status, may include
*                  'DROPPED', 'VALID', 'FAILED', 'INVALID'
******************************************************************/
  
  CREATE FUNCTION geodb_pkg.idx_index_status(idx geodb_pkg.INDEX_OBJ) RETURNS VARCHAR AS $$
  DECLARE	
    index_name VARCHAR(100);
	is_valid BOOLEAN;
	status VARCHAR(20);
  BEGIN
 	index_name := (idx).index_name;
	
	EXECUTE 'SELECT indisvalid FROM pg_index WHERE indexrelid = $1::regclass' INTO is_valid USING index_name;
	
	IF is_valid = true THEN
		status := 'VALID';
	ELSE
		status := 'INVALID';
	END IF;
	   		
  RETURN status;
  
  EXCEPTION
    WHEN NO_DATA_FOUND OR SQLSTATE '42704' OR SQLSTATE '42P01' THEN
      RETURN 'DROPPED';
    WHEN OTHERS THEN
      RETURN 'FAILED';
	  
  END;
  $$
  LANGUAGE plpgsql;
  
/*****************************************************************
* index_status - There is no column_name in the pg_stat_user_indexes-view!
* 
* @param table_name table_name of index to retrieve status from
* @param column_name column_name of index to retrieve status from
* @return VARCHAR string represntation of status, may include
*                  'DROPPED', 'VALID', 'FAILED', 'INVALID'
******************************************************************/

  CREATE FUNCTION geodb_pkg.idx_index_status(table_name VARCHAR, column_name VARCHAR) RETURNS VARCHAR AS $$
  DECLARE
    internal_table_name ALIAS FOR $1;
	internal_column_name ALIAS FOR $2;
	internal_column_id INTEGER;
	is_valid BOOLEAN;
    status VARCHAR(20);
  BEGIN
    
--    IF geodb_util.versioning_table(table_name) = 'ON' THEN
--      internal_table_name := table_name || '_LT';
--    END IF;     

 /* 
  * the pg_index-table has one OID for identifying the table (indrelid)
  * and one for the attribute (indexrelid) the index belongs to 
  * indrelid=tablename::regclass works as shown before
  * as the column_name exists only in pg_attribute-table the more generel ::oid-routine has to be used
  * but ::oid requires an int-value, not the passed char-variable
  * thus the id is queried first an executed into the variable internal_column_id
  * relam = 783 is a GiST-index (_spx), 403 is a btree-index (_pk, _fxk, _inx) 
  */
  
  EXECUTE 'SELECT a.attrelid FROM pg_attribute a
	JOIN pg_class c ON c.relfilenode=a.attrelid
	JOIN pg_stat_user_indexes sui ON sui.indexrelname=c.relname 
	WHERE a.attname=$1 AND
	(c.relam = 783 OR c.relam = 403) AND
	sui.relname=$2'
	INTO internal_column_id USING internal_column_name, internal_table_name;
  
  EXECUTE 'SELECT indisvalid FROM pg_index WHERE indrelid=$1::regclass
	AND indexrelid=$2::oid' INTO is_valid USING internal_table_name, internal_column_id;
  
	IF is_valid = true THEN
		status := 'VALID';
	ELSE
		status := 'INVALID';
	END IF;
	
	IF is_valid IS NULL THEN
		RAISE EXCEPTION NO_DATA_FOUND;
	END IF;
    		
  RETURN status;
  
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      RETURN 'DROPPED';
    WHEN OTHERS THEN
      RETURN 'FAILED';
  END;
  $$
  LANGUAGE plpgsql;
    
/******************************************************************
* create_spatial_metadata
* 
* @param idx index to create metadata for
******************************************************************/
  
  CREATE FUNCTION geodb_pkg.idx_create_spatial_metadata(idx geodb_pkg.INDEX_OBJ/*, is_versioned BOOLEAN*/) RETURNS void AS $$
  DECLARE 
    srid database_srs.srid%TYPE;
	geom VARCHAR(30);
  BEGIN
	/*
    IF is_versioned THEN
      (idx).table_name := (idx).table_name || '_LT';
    END IF;    
    */
	
    PERFORM DropGeometryColumn((idx).table_name, (idx).attribute_name);
    
    IF (idx).srid = 0 THEN
      EXECUTE 'SELECT srid FROM database_srs' INTO srid;
    ELSE
      srid := (idx).srid;
    END IF;
	
	geom := 'GEOMETRY';
    
    IF (idx).is_3d = 0 THEN
      PERFORM AddGeometryColumn((idx).table_name, (idx).attribute_name, srid, geom, 2);
    ELSE
      PERFORM AddGeometryColumn((idx).table_name, (idx).attribute_name, srid, geom, 3);
    END IF;
  END;
  $$
  LANGUAGE plpgsql;
      
/*****************************************************************
* create_index
* 
* @param idx index to create
* @param is_versioned TRUE IF database table is version-enabled
* @return VARCHAR sql error code, 00000 for no errors
******************************************************************/

CREATE FUNCTION geodb_pkg.idx_create_index(idx geodb_pkg.INDEX_OBJ/*, is_versioned BOOLEAN*/, params VARCHAR DEFAULT '') RETURNS VARCHAR AS $$
  DECLARE
    create_ddl VARCHAR(1000);
    -- table_name VARCHAR(100);
    sql_err_code VARCHAR(20);
	SPATIAL CONSTANT NUMERIC(1) := 1;
  
  BEGIN    
	
	IF geodb_pkg.idx_index_status(idx) != 'VALID' THEN
      sql_err_code := geodb_pkg.idx_drop_index(idx/*, is_versioned*/);
            
      BEGIN
		
		/*
		table_name := idx.table_name;
      
		IF is_versioned THEN
          dbms_wm.BEGINDDL(idx.table_name);
          table_name := table_name || '_LTS';
        END IF;
		*/

        IF (idx).type = SPATIAL THEN
          PERFORM geodb_pkg.idx_create_spatial_metadata(idx/*, is_versioned*/);
          create_ddl := 'CREATE INDEX ' || (idx).index_name || ' ON ' || (idx).table_name || ' USING GIST (' || (idx).attribute_name || ')';
		ELSE
		  create_ddl := 'CREATE INDEX ' || (idx).index_name || ' ON ' || (idx).table_name || '(' || (idx).attribute_name || ')';
        END IF;

        IF params != '' THEN
          create_ddl := create_ddl || ' ' || params;
        END IF;

        EXECUTE create_ddl;
        
        /*
		IF is_versioned THEN
          dbms_wm.COMMITDDL(idx.table_name);
        END IF;*/
        		
        EXCEPTION
        WHEN OTHERS THEN
		   RAISE INFO 'failed to execute create_index';
           RETURN SQLSTATE;
        
		  /*
          IF is_versioned THEN
            dbms_wm.ROLLBACKDDL(idx.table_name);
          END IF;
		  */
      END;
	  
    END IF;
    
    RETURN '00000';
  END;
  $$
  LANGUAGE plpgsql;
  
/****************************************************************
* drop_index
* 
* @param idx index to drop
* @param is_versioned TRUE IF database table is version-enabled
* @return VARCHAR sql error code, 00000 for no errors
******************************************************************/

  CREATE FUNCTION geodb_pkg.idx_drop_index(idx geodb_pkg.INDEX_OBJ /*, is_versioned BOOLEAN*/) RETURNS VARCHAR AS $$
  DECLARE
    index_name VARCHAR(100);
  BEGIN
    IF geodb_pkg.idx_index_status(idx) != 'DROPPED' OR geodb_pkg.idx_index_status(idx) IS NULL THEN
      BEGIN
        
		/*
		index_name := idx.index_name;
        
        IF is_versioned THEN
          dbms_wm.BEGINDDL(idx.table_name);
          index_name := index_name || '_LTS';
        END IF;
		*/
        
        EXECUTE 'DROP INDEX ' || (idx).index_name;
        
		/*
        IF is_versioned THEN
          dbms_wm.COMMITDDL(idx.table_name);
        END IF;
		*/
		
      EXCEPTION
        WHEN OTHERS THEN
          RAISE INFO 'failed to execute drop_index!';
		  RETURN SQLSTATE;
          
		  /*
          IF is_versioned THEN
            dbms_wm.ROLLBACKDDL(idx.table_name);
          END IF;
          */
		  
      END;
    END IF;
    
    RETURN '00000';
  END;
  $$
  LANGUAGE plpgsql;

/*****************************************************************
* create_indexes
* private convience method for invoking create_index on indexes 
* of same index type
* 
* @param type type of index, e.g. SPATIAL or NORMAL
* @return ARRAY array of log message strings
******************************************************************/

CREATE FUNCTION geodb_pkg.idx_create_indexes(type SMALLINT) RETURNS text[] AS $$
  DECLARE
    log text[];
    sql_error_code VARCHAR(20);
	i INTEGER;
	j geodb_pkg.INDEX_OBJ;
	k INTEGER;
  BEGIN   
    SELECT max(ID_idx_obj) FROM geodb_pkg.index_table INTO k;
	
	FOR i in 1..k LOOP
          EXECUTE 'SELECT (idx_obj).index_name,
					  (idx_obj).table_name,
					  (idx_obj).attribute_name,
					  (idx_obj).type,
					  (idx_obj).srid,
					  (idx_obj).is_3d
			FROM geodb_pkg.index_table WHERE ID_idx_obj=' || i ||'' INTO j;
	  
	IF j.type = type THEN
        sql_error_code := geodb_pkg.idx_create_index(j/*, geodb_util.versioning_table(index_table.j.table_name) = 'ON'*/);
        log[i] := j.index_name || ':' || j.table_name || ':' || j.attribute_name || ':' || sql_error_code || ':' || geodb_pkg.idx_index_status(j);
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
* @param type type of index, e.g. SPATIAL or NORMAL
* @return ARRAY array of log message strings
******************************************************************/
 
CREATE FUNCTION geodb_pkg.idx_drop_indexes(type SMALLINT) RETURNS text[] AS $$
  DECLARE
    log text[];
    sql_error_code VARCHAR(20);
	i INTEGER;
	j geodb_pkg.INDEX_OBJ;
	k INTEGER;
  BEGIN    
    SELECT max(ID_idx_obj) FROM geodb_pkg.index_table INTO k;
	
	FOR i in 1..k LOOP
      EXECUTE 'SELECT (idx_obj).index_name,
					  (idx_obj).table_name,
					  (idx_obj).attribute_name,
					  (idx_obj).type,
					  (idx_obj).srid,
					  (idx_obj).is_3d
			FROM geodb_pkg.index_table WHERE ID_idx_obj=' || i ||'' INTO j;
	
	  IF j.type = type THEN
        sql_error_code := geodb_pkg.idx_drop_index(j/*, geodb_util.versioning_table(index_table.idx_obj(i).table_name) = 'ON'*/);
        log[i] := j.index_name || ':' || j.table_name || ':' || j.attribute_name || ':' || sql_error_code || ':' || geodb_pkg.idx_index_status(j);
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

CREATE FUNCTION geodb_pkg.idx_status_spatial_indexes() RETURNS text[] AS $$
  DECLARE
    log text[];
    sql_error_code VARCHAR(20);
	i INTEGER;
	j geodb_pkg.INDEX_OBJ;
	k INTEGER;
  BEGIN
    SELECT max(ID_idx_obj) FROM geodb_pkg.index_table INTO k;
	
	FOR i in 1..k LOOP
            EXECUTE 'SELECT (idx_obj).index_name,
					  (idx_obj).table_name,
					  (idx_obj).attribute_name,
					  (idx_obj).type,
					  (idx_obj).srid,
					  (idx_obj).is_3d
			FROM geodb_pkg.index_table WHERE ID_idx_obj=' || i ||'' INTO j;
	  IF j.type = 1 THEN
        sql_error_code := geodb_pkg.idx_index_status(j);
        log[i] := j.index_name || ':' || j.table_name || ':' || j.attribute_name || ':' || sql_error_code;
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

CREATE FUNCTION geodb_pkg.idx_status_normal_indexes() RETURNS text[] AS $$
  DECLARE
    log text[];
    sql_error_code VARCHAR(20);
	i INTEGER;
	j geodb_pkg.INDEX_OBJ;
	k INTEGER;
  BEGIN
	SELECT max(ID_idx_obj) FROM geodb_pkg.index_table INTO k;
	
	FOR i in 1..k LOOP
            EXECUTE 'SELECT (idx_obj).index_name,
					  (idx_obj).table_name,
					  (idx_obj).attribute_name,
					  (idx_obj).type,
					  (idx_obj).srid,
					  (idx_obj).is_3d
			FROM geodb_pkg.index_table WHERE ID_idx_obj=' || i ||'' INTO j;
	  
	  IF j.type = 0 THEN
        sql_error_code := geodb_pkg.idx_index_status(j);
        log[i] := j.index_name || ':' || j.table_name || ':' || j.attribute_name || ':' || sql_error_code;
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

CREATE FUNCTION geodb_pkg.idx_create_spatial_indexes() RETURNS text[] AS $$
  BEGIN
	RETURN geodb_pkg.idx_create_indexes('1');
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

CREATE FUNCTION geodb_pkg.idx_drop_spatial_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_drop_indexes('1');
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

CREATE FUNCTION geodb_pkg.idx_create_normal_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_create_indexes('0');
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

CREATE FUNCTION geodb_pkg.idx_drop_normal_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_drop_indexes('0');
  END;  
$$
LANGUAGE plpgsql;


/*
 *
 * NEW FUNCTIONS (by FKun)
 *
 */

/*****************************************************************
* switch_on_index
* 
* @param idx index to set to indisvalid = true
* @return VARCHAR sql error code, 00000 for no errors
******************************************************************/

CREATE FUNCTION geodb_pkg.idx_switch_on_index(idx geodb_pkg.INDEX_OBJ) RETURNS VARCHAR AS $$
  DECLARE
    index_name VARCHAR(100);
	create_status VARCHAR;
  BEGIN
    index_name := (idx).index_name;
	
	IF geodb_pkg.idx_index_status(idx) != 'VALID' THEN
		BEGIN
			IF geodb_pkg.idx_index_status(idx) = 'DROPPED' THEN
				RAISE INFO 'index had to be created because it was not found';
				SELECT geodb_pkg.idx_create_index(idx) INTO create_status;
				IF create_status != '00000' THEN
					PERFORM geodb_pkg.util_error_msg(create_status);
				END IF;
			END IF;
		UPDATE pg_index SET indisvalid = true WHERE indexrelid = index_name::regclass;
		
		EXCEPTION
			WHEN OTHERS THEN
				RAISE INFO 'failed to execute switch_on_index!';
				RETURN SQLSTATE;
				
		END;
	END IF;
		  
	RETURN '00000';
  END;
  $$
  LANGUAGE plpgsql;
  
/****************************************************************
* switch_off_index
* 
* @param idx index to set to indisvalid = false
* @return VARCHAR sql error code, 00000 for no errors
******************************************************************/

CREATE FUNCTION geodb_pkg.idx_switch_off_index(idx geodb_pkg.INDEX_OBJ) RETURNS VARCHAR AS $$
  DECLARE
    index_name VARCHAR(100);
  BEGIN
    index_name := (idx).index_name;
	
	IF geodb_pkg.idx_index_status(idx) != 'INVALID' THEN
		BEGIN
			IF geodb_pkg.idx_index_status(idx) = 'DROPPED' THEN
				RAISE EXCEPTION NO_DATA_FOUND; 
			END IF;
        
			UPDATE pg_index SET indisvalid = false WHERE indexrelid = index_name::regclass;
		
		    EXCEPTION
				WHEN NO_DATA_FOUND THEN
					RAISE INFO 'unable to switch off index, because it is dropped';
					RETURN SQLSTATE;
				WHEN OTHERS THEN
					RAISE INFO 'failed to execute switch_off_index!';
					RETURN SQLSTATE;  
		END;
  	END IF;
	   
    RETURN '00000';
  END;
  $$
  LANGUAGE plpgsql;
 
/*****************************************************************
* switch_on_indexes
* private convience method for invoking switch_on_index on indexes 
* of same index type
* 
* @param type type of index, e.g. SPATIAL or NORMAL
* @return ARRAY array of log message strings
******************************************************************/

CREATE FUNCTION geodb_pkg.idx_switch_on_indexes(type SMALLINT) RETURNS text[] AS $$
  DECLARE
    log text[];
    sql_error_code VARCHAR(20);
	i INTEGER;
	j geodb_pkg.INDEX_OBJ;
	k INTEGER;
  BEGIN   
    SELECT max(ID_idx_obj) FROM geodb_pkg.index_table INTO k;
	
	FOR i in 1..k LOOP
          EXECUTE 'SELECT (idx_obj).index_name,
					  (idx_obj).table_name,
					  (idx_obj).attribute_name,
					  (idx_obj).type,
					  (idx_obj).srid,
					  (idx_obj).is_3d
			FROM geodb_pkg.index_table WHERE ID_idx_obj=' || i ||'' INTO j;
	  
	IF j.type = type THEN
        sql_error_code := geodb_pkg.idx_switch_on_index(j);
        log[i] := j.index_name || ':' || j.table_name || ':' || j.attribute_name || ':' || sql_error_code || ':' || geodb_pkg.idx_index_status(j);
    END IF;
	END LOOP;     
    
    RETURN log;
  END;
$$
LANGUAGE plpgsql;
  
/*****************************************************************
* switch_off_indexes
* private convience method for invoking switch_off_index on indexes 
* of same index type
* 
* @param type type of index, e.g. SPATIAL or NORMAL
* @return ARRAY array of log message strings
******************************************************************/
 
CREATE FUNCTION geodb_pkg.idx_switch_off_indexes(type SMALLINT) RETURNS text[] AS $$
  DECLARE
    log text[];
    sql_error_code VARCHAR(20);
	i INTEGER;
	j geodb_pkg.INDEX_OBJ;
	k INTEGER;
  BEGIN    
    SELECT max(ID_idx_obj) FROM geodb_pkg.index_table INTO k;
	
	FOR i in 1..k LOOP
      EXECUTE 'SELECT (idx_obj).index_name,
					  (idx_obj).table_name,
					  (idx_obj).attribute_name,
					  (idx_obj).type,
					  (idx_obj).srid,
					  (idx_obj).is_3d
			FROM geodb_pkg.index_table WHERE ID_idx_obj=' || i ||'' INTO j;
	
	  IF j.type = type THEN
        sql_error_code := geodb_pkg.idx_switch_off_index(j);
        log[i] := j.index_name || ':' || j.table_name || ':' || j.attribute_name || ':' || sql_error_code || ':' || geodb_pkg.idx_index_status(j);
      END IF;
    END LOOP; 
    
    RETURN log;
  END;
$$
LANGUAGE plpgsql;

/*****************************************************************
* switch_on_spatial_indexes
* convience method for invoking switch_on_index on all normal 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
  
CREATE FUNCTION geodb_pkg.idx_switch_on_spatial_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_switch_on_indexes('1');
  END;  
$$
LANGUAGE plpgsql;
  
/*****************************************************************
* switch_off_spatial_indexes
* convience method for invoking switch_off_index on all normal 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
  
CREATE FUNCTION geodb_pkg.idx_switch_off_spatial_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_switch_off_indexes('1');
  END;  
$$
LANGUAGE plpgsql;

/*****************************************************************
* switch_on_normal_indexes
* convience method for invoking switch_on_index on all normal 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
  
CREATE FUNCTION geodb_pkg.idx_switch_on_normal_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_switch_on_indexes('0');
  END;  
$$
LANGUAGE plpgsql;
  
/*****************************************************************
* switch_off_normal_indexes
* convience method for invoking switch_off_index on all normal 
* indexes 
* 
* @return ARRAY array of log message strings
******************************************************************/
  
CREATE FUNCTION geodb_pkg.idx_switch_off_normal_indexes() RETURNS text[] AS $$
  BEGIN
    RETURN geodb_pkg.idx_switch_off_indexes('0');
  END;  
$$
LANGUAGE plpgsql;