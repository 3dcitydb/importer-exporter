-- MATCH.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--
-- Conversion:  Felix Kunde <fkunde@virtualcitysystems.de>
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
-- All functions are part of the geodb_pkg.schema and MATCHING-"Package" 
-- They start with the prefix "match_" to guarantee a better overview 
-- in the PGAdminIII-Tool.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author  | Conversion
-- 1.0.0     2013-02-22   PostGIS version    CNag      FKun
--

-------------------------------------------------------------------------------
-- Conversion-Report:
-- TEMPORARY TABLE match_tmp_building is created in the public-schema as geodb_pkg
-- is not defined as a temporary schema. Temp-tables have to be created after every
-- connection intialisation. collect_cand_building is the first FUNCTION to be called
-- by the Importer/Exporter and must create this temp-table.
--
-- Geometric functions of PostGIS can't use a tolarance. ST_IsValid is working too strict,
-- and to many buildings are deleted. Another solution is needed here.
-------------------------------------------------------------------------------

/*****************************************************************
* TABLEs for Matching
* 
* part of geodb_pkg.schema except from match_tmp_building
* temporary tables can't be created in a non-temporary schema like geodb_pkg
*
******************************************************************/

DROP TABLE IF EXISTS geodb_pkg.match_overlap_all;
CREATE TABLE geodb_pkg.match_overlap_all(
  id1 INTEGER,
  parent_id1 INTEGER,
  root_id1 INTEGER,
  area1 NUMERIC,
  lod1 NUMERIC,
  lineage VARCHAR(256),
  id2 INTEGER,
  parent_id2 INTEGER,
  root_id2 INTEGER,
  area2 NUMERIC,
  lod2 NUMERIC,
  intersection_geometry GEOMETRY,
  intersection_area NUMERIC,
  area1_cov_by_area2 NUMERIC,
  area2_cov_by_area1 NUMERIC
);

DROP TABLE IF EXISTS geodb_pkg.match_overlap_relevant;
CREATE TABLE geodb_pkg.match_overlap_relevant(
  id1 INTEGER,
  parent_id1 INTEGER,
  root_id1 INTEGER,
  area1 NUMERIC,
  lod1 NUMERIC,
  lineage VARCHAR(256),
  id2 INTEGER,
  parent_id2 INTEGER,
  root_id2 INTEGER,
  area2 NUMERIC,
  lod2 NUMERIC,
  intersection_geometry GEOMETRY,
  intersection_area NUMERIC,
  area1_cov_by_area2 NUMERIC,
  area2_cov_by_area1 NUMERIC
);

DROP TABLE IF EXISTS geodb_pkg.MATCH_MASTER_PROJECTED;
CREATE TABLE geodb_pkg.match_master_projected(
  id INTEGER,
  parent_id INTEGER,
  root_id INTEGER,
  geometry GEOMETRY
);

DROP TABLE IF EXISTS geodb_pkg.MATCH_CAND_PROJECTED;
CREATE TABLE geodb_pkg.match_cand_projected(
  id INTEGER,
  parent_id INTEGER,
  root_id INTEGER,
  geometry GEOMETRY
);

DROP TABLE IF EXISTS geodb_pkg.match_collect_geom;
CREATE TABLE geodb_pkg.match_collect_geom(
  id INTEGER,
  parent_id INTEGER,
  root_id INTEGER,
  geometry GEOMETRY
);


/*****************************************************************
* FUNCTIONs for Matching
* 
* part of geodb_pkg.schema
*
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.match_create_matching_table(
  lod_cand      INTEGER, 
  lineage cityobject.lineage%TYPE, 
  lod_master    INTEGER, 
  delta_cand    INTEGER, 
  delta_master  INTEGER,  
  aggregate_building NUMERIC DEFAULT 1)
RETURNS SETOF void AS
$$
DECLARE 
  log VARCHAR(4000);
BEGIN 
  -- gather candidate buildings
  PERFORM geodb_pkg.match_collect_cand_building(lod_cand, lineage);

  -- gather candidate geometry
  PERFORM geodb_pkg.match_collect_geometry(lod_cand);

  -- rectify candidate geometry 
  PERFORM geodb_pkg.match_rectify_geometry();

  -- aggregate candidate geometry   
  PERFORM geodb_pkg.match_aggregate_geometry('geodb_pkg.MATCH_CAND_PROJECTED', aggregate_building);   

  -- gather master buildings
  PERFORM geodb_pkg.match_collect_master_building(lod_master, lineage);

  -- gather master geometry
  PERFORM geodb_pkg.match_collect_geometry(lod_master);

  -- rectify master geometry 
  PERFORM geodb_pkg.match_rectify_geometry();

  -- aggregate master geometry
  PERFORM geodb_pkg.match_aggregate_geometry('geodb_pkg.MATCH_MASTER_PROJECTED', aggregate_building); 

  -- fill matching table
  PERFORM geodb_pkg.match_join_cand_master(lod_cand, lineage, lod_master);

  -- densify to 1:1 matches
  PERFORM geodb_pkg.match_create_relevant_matches(delta_cand, delta_master);

  COMMIT;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_collect_cand_building(lod INTEGER, lineage cityobject.lineage%TYPE)
RETURNS SETOF void AS
$$
BEGIN
  -- creates the temporary table match_tmp_building
  EXECUTE 'CREATE GLOBAL TEMPORARY TABLE match_tmp_building(
    id INTEGER,
    parent_id INTEGER,
    root_id INTEGER,
    geometry_id INTEGER
    ) ON COMMIT PRESERVE ROWS';

  -- retrieve all building tupels belonging to the specified lineage
  EXECUTE 'INSERT INTO match_tmp_building 
    SELECT b.id, b.building_parent_id parent_id, b.building_root_id root_id, b.lod'||lod||'_geometry_id geometry_id
      FROM building b, cityobject c WHERE c.id = b.id 
        AND c.lineage = $1' USING lineage;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_collect_master_building(lod INTEGER, lineage cityobject.lineage%TYPE)
RETURNS SETOF void AS
$$
BEGIN
  -- truncate tmp table
  EXECUTE 'TRUNCATE TABLE match_tmp_building';

  -- retrieve all building tupels not belonging to the specified lineage and
  -- whose mbr is interacting with the aggregated mbr of all candidate building footprint
  EXECUTE 'INSERT INTO match_tmp_building
    SELECT b.id, b.building_parent_id parent_id, b.building_root_id root_id, b.lod'||lod||'_geometry_id geometry_id
      FROM building b, cityobject c WHERE c.id = b.id 
        AND ST_Intersects(c.envelope, $1) = ''TRUE'' 
        AND (c.lineage != $2 OR c.lineage IS NULL)' 
          USING geodb_pkg.match_aggregate_mbr('geodb_pkg.MATCH_CAND_PROJECTED'), lineage;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_collect_geometry(lod INTEGER)
RETURNS SETOF void AS
$$
DECLARE
  srid  INTEGER;
BEGIN
  -- first, truncate tmp table
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_collect_geom';
  EXECUTE 'DROP INDEX IF EXISTS match_collect_id_idx';
  EXECUTE 'DROP INDEX IF EXISTS match_collect_root_id_idx';
  
  -- second, retrieve exterior shell surfaces FROM building
  EXECUTE 'INSERT INTO geodb_pkg.match_collect_geom
    SELECT bl.id, bl.parent_id, bl.root_id, ST_Force_2D(s.geometry)
      FROM match_tmp_building bl, surface_geometry s WHERE s.root_id = bl.geometry_id
        AND s.geometry IS NOT NULL';

  -- for lod > 1 we also have to check surfaces FROM the tables
  -- building_installation and thematic surface
  IF lod > 1 THEN
    -- retrieve surfaces FROM building installations referencing the identified
    -- building tupels
    EXECUTE 'INSERT INTO geodb_pkg.match_collect_geom
      SELECT bl.id, bl.parent_id, bl.root_id, ST_Force_2D(s.geometry)
        FROM match_tmp_building bl, building_installation i, surface_geometry s
          WHERE i.building_id = bl.id 
            AND i.is_external = 1
            AND s.root_id = i.lod'||lod||'_geometry_id
            AND s.geometry IS NOT NULL';

    -- retrieve surfaces FROM thematic surfaces referencing the identified
    -- building tupels
    EXECUTE 'INSERT INTO geodb_pkg.match_collect_geom 
	  SELECT bl.id, bl.parent_id, bl.root_id, ST_Force_2D(s.geometry)
        FROM match_tmp_building bl, thematic_surface t, surface_geometry s
          WHERE t.building_id = bl.id
            AND upper(t.type) NOT IN (''INTERIORWALLSURFACE'', ''CEILINGSURFACE'', ''FLOORSURFACE'')
            AND s.root_id = t.lod'||lod||'_multi_surface_id
            AND s.geometry IS NOT NULL';
  END IF;    

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'collect_geometry: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_rectify_geometry()
RETURNS SETOF void AS
$$
BEGIN
  -- first, remove invalid geometries 
  EXECUTE 'DELETE FROM geodb_pkg.match_collect_geom WHERE ST_IsValid(geometry) != ''TRUE'''; --USING tolerance; --no tolerance in PostGIS-function

  -- second, DELETE vertical surfaces
  EXECUTE 'DELETE FROM geodb_pkg.match_collect_geom WHERE ST_Area(geometry) <= 0.001';

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'rectify_geometry: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_aggregate_geometry(tab_name VARCHAR, aggregate_building INTEGER DEFAULT 1)
RETURNS SETOF void AS
$$
DECLARE
  log VARCHAR(4000);
  match_cand_projected_spx geodb_pkg.index_obj;
  match_master_projected_spx geodb_pkg.index_obj;
  match_collect_id_idx geodb_pkg.index_obj;
  match_collect_root_id_idx geodb_pkg.index_obj;
BEGIN
  match_cand_projected_spx := geodb_pkg.idx_construct_spatial_2d('match_cand_projected_spx', 'geodb_pkg.MATCH_CAND_PROJECTED', 'geometry');
  match_master_projected_spx := geodb_pkg.idx_construct_spatial_2d('match_master_projected_spx', 'geodb_pkg.MATCH_MASTER_PROJECTED', 'geometry');
  match_collect_id_idx := geodb_pkg.idx_construct_normal('match_collect_id_idx', 'geodb_pkg.match_collect_geom', 'id');
  match_collect_root_id_idx := geodb_pkg.idx_construct_normal('match_collect_root_id_idx', 'geodb_pkg.match_collect_geom', 'root_id');
  
  -- TRUNCATE TABLE
  EXECUTE 'TRUNCATE TABLE '||tab_name;

  -- drop spatial indexes   
  IF (match_cand_projected_spx).table_name = tab_name THEN
    log := geodb_pkg.idx_drop_index(match_cand_projected_spx);
  ELSE
    log := geodb_pkg.idx_drop_index(match_master_projected_spx);
  END IF;

  IF aggregate_building > 0 then    
    DECLARE
      root_id_cur CURSOR FOR
        SELECT DISTINCT root_id FROM match_tmp_building;

    BEGIN
      log := geodb_pkg.idx_create_index(match_collect_root_id_idx);

      FOR root_id_rec IN root_id_cur LOOP
        EXECUTE 'INSERT INTO '||tab_name||' (id, parent_id, root_id, geometry)
          VALUES ($1, null, $2, $3)' 
          USING root_id_rec.root_id, root_id_rec.root_id, 
            (geodb_pkg.match_aggregate_geometry_by_id(root_id_rec.root_id, 1));          
      END LOOP;
    END;
  ELSE
    DECLARE
      id_cur CURSOR FOR
        SELECT DISTINCT id, parent_id, root_id FROM match_tmp_building;

    BEGIN
      log := geodb_pkg.idx_create_index(match_collect_id_idx);

      FOR id_rec IN id_cur LOOP
        EXECUTE 'INSERT INTO '||tab_name||' (id, parent_id, root_id, geometry)
          VALUES ($1, $2, $3, $4)' 
          USING id_rec.id, id_rec.parent_id, id_rec.root_id, 
              (geodb_pkg.match_aggregate_geometry_by_id(id_rec.id, 0));          
      END LOOP;
    END;
  END IF;

  -- clean up aggregate table
  EXECUTE 'DELETE FROM '||tab_name||' WHERE geometry IS NULL';

  -- create spatial index
  IF match_cand_projected_spx.table_name = tab_name THEN
    match_cand_projected_spx.srid := geodb_pkg.match_get_2d_srid();
    log := geodb_pkg.idx_create_index(match_cand_projected_spx);
  ELSE
    match_master_projected_spx.srid := geodb_pkg.match_get_2d_srid();
    log := geodb_pkg.idx_create_index(match_master_projected_spx);
  END IF;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_join_cand_master(
  lod_cand INTEGER, 
  lineage cityobject.lineage%TYPE, 
  lod_master INTEGER)
RETURNS SETOF void AS
$$
DECLARE
  log VARCHAR(4000);
  match_overlap_all_spx geodb_pkg.index_obj;

BEGIN
  match_overlap_all_spx := geodb_pkg.idx_construct_spatial_2d('match_overlap_all_spx', 'geodb_pkg.match_overlap_all', 'intersection_geometry');

  -- clean environment
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_overlap_all';

  log := geodb_pkg.idx_drop_index(match_overlap_all_spx);

  EXECUTE 'INSERT INTO geodb_pkg.match_overlap_all 
    (id1, parent_id1, root_id1, area1, lod1, lineage,
      id2, parent_id2, root_id2, area2, lod2, intersection_geometry)
    SELECT c.id AS id1, c.parent_id AS parent_id1, c.root_id AS root_id1, ST_Area(c.geometry) AS area1, $1, $2,
      m.id AS id2, m.parent_id AS parent_id2, m.root_id AS root_id2, ST_Area(m.geometry) AS area2, $3, ST_Intersection(c.geometry, m.geometry)
        FROM geodb_pkg.MATCH_CAND_PROJECTED c, geodb_pkg.MATCH_MASTER_PROJECTED m' 
          USING lod_cand, lineage, lod_master;

  EXECUTE 'UPDATE geodb_pkg.match_overlap_all SET intersection_area = ST_Area(intersection_geometry)';
  EXECUTE 'DELETE FROM geodb_pkg.match_overlap_all WHERE intersection_area = 0';
  EXECUTE 'UPDATE geodb_pkg.match_overlap_all SET area1_cov_by_area2 = geodb_pkg.util_min(intersection_area / area1, 1.0), 
             area2_cov_by_area1 = geodb_pkg.util_min(intersection_area / area2, 1.0)'; 
  
  -- create spatial index on intersection geometry 
  match_overlap_all_spx.srid := geodb_pkg.match_get_2d_srid();
  log := geodb_pkg.idx_create_index(match_overlap_all_spx);
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_create_relevant_matches(delta_cand DOUBLE PRECISION, delta_master DOUBLE PRECISION)
RETURNS SETOF void AS
$$
DECLARE
  log VARCHAR(4000);
  match_result_spx geodb_pkg.index_obj; 

  ref_to_cand_cur CURSOR FOR
    SELECT id2, count(id1) AS cnt_cand FROM geodb_pkg.match_overlap_relevant GROUP BY id2;
  cand_to_ref_cur CURSOR FOR
    SELECT id1, count(id2) AS cnt_ref FROM geodb_pkg.match_overlap_relevant GROUP BY id1;
BEGIN
  match_result_spx := geodb_pkg.idx_construct_spatial_2d('match_overlap_relevant_spx', 'geodb_pkg.match_overlap_relevant', 'intersection_geometry');

  -- TRUNCATE TABLE
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_overlap_relevant';

  log := geodb_pkg.idx_drop_index(match_result_spx);

  -- retrieve all match tupels with more than a user-specified percentage of area coverage
  EXECUTE 'INSERT INTO geodb_pkg.match_overlap_relevant 
    SELECT * FROM geodb_pkg.match_overlap_all
      WHERE area1_cov_by_area2 >= $1 AND area2_cov_by_area1 >= $2' USING delta_cand, delta_master;

  -- enforce 1:1 matches between candidates and reference buildings
  FOR ref_to_cand_rec IN ref_to_cand_cur LOOP
    IF ref_to_cand_rec.cnt_cand > 1 THEN
      EXECUTE 'DELETE FROM geodb_pkg.match_overlap_relevant WHERE id2=$1' USING ref_to_cand_rec.id2;
    END IF;
  END LOOP;

  FOR cand_to_ref_rec IN cand_to_ref_cur LOOP
    IF cand_to_ref_rec.cnt_ref > 1 THEN
      EXECUTE 'DELETE FROM geodb_pkg.match_overlap_relevant WHERE id1=$1' USING cand_to_ref_rec.id1;
    END IF;
  END LOOP;

  -- create spatial index on intersection geometry
  match_result_spx.srid := geodb_pkg.match_get_2d_srid();
  log := geodb_pkg.idx_create_index(match_result_spx);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'create_relevant_matches: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_clear_matching_tables()
RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_overlap_all';
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_overlap_relevant';
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_master_projected';
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_cand_projected';
  EXECUTE 'TRUNCATE TABLE geodb_pkg.match_collect_geom';
  EXECUTE 'TRUNCATE TABLE match_tmp_building';
  EXECUTE 'TRUNCATE TABLE geodb_pkg.merge_collect_geom';
  EXECUTE 'TRUNCATE TABLE geodb_pkg.merge_container_ids';

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'clean_matching_tables: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_aggregate_mbr(table_name VARCHAR) RETURNS GEOMETRY AS
$$
DECLARE
  aggr_mbr GEOMETRY;
  srid INTEGER;
BEGIN
  EXECUTE 'SELECT srid FROM database_srs' INTO srid;
  EXECUTE 'SELECT ST_Union(geometry) FROM '||table_name||'' INTO aggr_mbr;

  PERFORM ST_SetSRID(aggr_mbr, srid);

  RETURN aggr_mbr;

  EXCEPTION
    WHEN OTHERS THEN 
      RETURN null;
END;
$$ 
LANGUAGE plpgsql;


/*
* create footprint for building by aggregating (USING boolean union) all identified
* polygons.
*/
CREATE OR REPLACE FUNCTION geodb_pkg.match_aggregate_geometry_by_id(id INTEGER, aggregate_building INTEGER DEFAULT 1)
RETURNS GEOMETRY AS
$$
DECLARE
  aggr_geom GEOMETRY;
  attr VARCHAR(10);
BEGIN
  IF aggregate_building > 0 THEN
    attr := 'root_id';
  ELSE
    attr := 'id';
  END IF;

  EXECUTE 'SELECT ST_Union(geometry) FROM geodb_pkg.match_collect_geom 
             WHERE '||attr||'=$1' INTO aggr_geom USING id;

  RETURN aggr_geom;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE '%: %', id, SQLERRM;
      RETURN null;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.match_get_2d_srid()
RETURNS INTEGER AS
$$
DECLARE
  srid INTEGER;
BEGIN
  IF geodb_pkg.util_is_db_coord_ref_sys_3d() = 1 THEN
    srid := null;
  ELSE
    EXECUTE 'SELECT srid FROM database_srs' INTO srid;
  END IF;

  RETURN srid;
END;
$$ 
LANGUAGE plpgsql;