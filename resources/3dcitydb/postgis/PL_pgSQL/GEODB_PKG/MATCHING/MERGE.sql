-- MERGE.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
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
-- They start with the prefix "merge_" to guarantee a better overview 
-- in the PGAdminIII-Tool.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                     | Author | Conversion
-- 1.1       2012-07-12   PostGIS version                            FKun
-- 1.1       2011-02-13   bugfixes, e.g. appearance mover   CNag
-- 1.0.0     2008-09-10   release version                   ASta
--

-------------------------------------------------------------------------------
-- Conversion-Report:
-- TEMPORARY TABLE megre_collect_geom is created in the public-schema as geodb_pkg
-- is not defined as a temporary schema. Temp-tables have to be created after every
-- connection intialisation. collect_all_geometry is the first FUNCTION to be called
-- by the Importer/Exporter and must create this temp-table.
--
-- It's not allowed to use table aliases in an UPDATE-statement in PostgreSQL!
-------------------------------------------------------------------------------

/*****************************************************************
* TABLE for Merging
* 
* merge_container_ids is part of geodb_pkg.schema
* merge_collect_geom has to be added to the public.schema
* temporary tables can't be created in a non-temporary schema like geodb_pkg
*
******************************************************************/
 
DROP TABLE IF EXISTS geodb_pkg.merge_container_ids;
CREATE TABLE geodb_pkg.merge_container_ids (
  building_id INTEGER, 
  container_id INTEGER
);


/*****************************************************************
* FUNCTIONs for Merging
* 
* part of geodb_pkg.schema
*
******************************************************************/

CREATE OR REPLACE FUNCTION geodb_pkg.merge_process_matches(
  lod_src NUMERIC, 
  lod_dst NUMERIC, 
  name_mode NUMERIC, 
  delimiter VARCHAR)
RETURNS SETOF void AS
$$
BEGIN
  -- find relevant matches
  PERFORM geodb_pkg.merge_collect_all_geometry(lod_src);
  PERFORM geodb_pkg.merge_remove_geometry_from_cand(lod_src);
  PERFORM geodb_pkg.merge_create_and_put_container(lod_dst, name_mode, delimiter);
  PERFORM geodb_pkg.merge_move_appearance();
  PERFORM geodb_pkg.merge_move_geometry();
  PERFORM geodb_pkg.merge_delete_head_of_merge_geometry();
  COMMIT;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'process_matches: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_collect_all_geometry(lod NUMERIC)
RETURNS SETOF void AS
$$
DECLARE
  log VARCHAR(4000);
  merge_geom_building_id_idx geodb_pkg.index_obj;
  merge_geom_geometry_id_idx geodb_pkg.index_obj;
BEGIN
  -- creates the temporary table merge_collect_geom
  EXECUTE 'CREATE GLOBAL TEMPORARY TABLE merge_collect_geom(
    building_id INTEGER, 
    geometry_id INTEGER, 
    cityobject_id INTEGER
    ) ON COMMIT PRESERVE ROWS';

  merge_geom_building_id_idx := geodb_pkg.idx_construct_normal('merge_geom_building_id_idx', 'merge_collect_geom', 'building_id');
  merge_geom_geometry_id_idx := geodb_pkg.idx_construct_normal('merge_geom_geometry_id_idx', 'merge_collect_geom', 'geometry_id');

  EXECUTE 'TRUNCATE TABLE merge_collect_geom';

  log := geodb_pkg.idx_drop_index(merge_geom_building_id_idx);
  log := geodb_pkg.idx_drop_index(merge_geom_geometry_id_idx);

  -- retrieve all building and building part geometry
  EXECUTE 'INSERT INTO merge_collect_geom
    SELECT b.building_root_id, b.lod'||lod||'_geometry_id, b.id 
      FROM building b, geodb_pkg.match_overlap_relevant m
        WHERE b.building_root_id = m.id1 
          AND b.lod'||lod||'_geometry_id IS NOT NULL';

  IF lod >= 2 THEN
    -- retrieve relevant building installation geometry
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT b.building_root_id, i.lod'||lod||'_geometry_id, i.id
        FROM geodb_pkg.match_overlap_relevant m, building_installation i, building b
          WHERE i.building_id = b.id
            AND b.building_root_id = m.id1
            AND i.is_external = 1
            AND i.lod'||lod||'_geometry_id IS NOT NULL';

    -- retrieve surfaces from relevant thematic surfaces
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT  b.building_root_id, t.lod'||lod||'_multi_surface_id, t.id
        FROM geodb_pkg.match_overlap_relevant m, thematic_surface t, building b
          WHERE t.building_id = b.id
            AND b.building_root_id = m.id1
            AND t.lod'||lod||'_multi_surface_id IS NOT NULL';
    END IF;

  IF lod >= 3 THEN
    -- retrieve all openings of all thematic surfaces beloning to all buildings and building parts
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT b.building_root_id, o.lod'||lod||'_multi_surface_id, o.id
        FROM geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, opening o, opening_to_them_surface ot
          WHERE t.building_id = b.id
            AND b.building_root_id = m.id1
            AND ot.thematic_surface_id = t.id
            AND o.id = ot.opening_id
            AND o.lod'||lod||'_multi_surface_id IS NOT NULL';
  END IF;

  IF lod >= 4 THEN
    -- room
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT b.building_root_id, r.lod4_geometry_id, r.id
        FROM geodb_pkg.match_overlap_relevant m, room r, building b
          WHERE r.building_id = b.id
            AND b.building_root_id = m.id1
            AND r.lod4_geometry_id IS NOT NULL';

    -- building furniture (in rooms) --if lod r is changed to f
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT b.building_root_id, f.lod4_geometry_id, f.id
        FROM geodb_pkg.match_overlap_relevant m, room r, building b, building_furniture f
          WHERE r.building_id = b.id
            AND b.building_root_id = m.id1
            AND f.room_id = r.id
            AND f.lod4_geometry_id IS NOT NULL';

    -- retrieve relevant internal (or external) building installation geometry (in rooms)
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT b.building_root_id, i.lod4_geometry_id, i.id
        FROM geodb_pkg.match_overlap_relevant m, building_installation i, building b, room r
          WHERE r.building_id = b.id
            AND i.room_id = r.id
            AND b.building_root_id = m.id1
            AND i.lod4_geometry_id IS NOT NULL';

    -- retrieve surfaces from relevant thematic surfaces (in rooms)
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT  b.building_root_id, t.lod4_multi_surface_id, t.id
        FROM geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, room r
          WHERE r.building_id = b.id
            AND t.room_id = r.id
            AND b.building_root_id = m.id1
            AND t.lod4_multi_surface_id IS NOT NULL';

     -- retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
    EXECUTE 'INSERT INTO merge_collect_geom
      SELECT b.building_root_id, o.lod4_multi_surface_id, o.id
        FROM geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, opening o, opening_to_them_surface ot, room r
          WHERE r.building_id = b.id
            AND t.room_id = r.id
            AND b.building_root_id = m.id1
            AND ot.thematic_surface_id = t.id
            AND o.id = ot.opening_id
            AND o.lod4_multi_surface_id IS NOT NULL';

    -- retrieve relevant internal building installation geometry
    EXECUTE 'INSERT INTO merge_collect_geom 
      SELECT b.building_root_id, i.lod4_geometry_id, i.id
        FROM geodb_pkg.match_overlap_relevant m, building_installation i, building b
          WHERE i.building_id = b.id
            AND b.building_root_id = m.id1
            AND i.is_external = 0
            AND i.lod4_geometry_id IS NOT NULL';
  END IF;

  log := geodb_pkg.idx_create_index(merge_geom_building_id_idx);
  log := geodb_pkg.idx_create_index(merge_geom_geometry_id_idx); 

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'collect_all_geometry: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_remove_geometry_from_cand(lod NUMERIC)
RETURNS SETOF void AS
$$
BEGIN
  -- retrieve all building and building part geometry
  EXECUTE 'UPDATE building SET lod'||lod||'_geometry_id = null
    WHERE building_root_id IN 
      (SELECT id1 FROM geodb_pkg.match_overlap_relevant)';

  IF lod >= 2 THEN
    -- retrieve relevant building installation geometry
    EXECUTE 'UPDATE building_installation SET lod'||lod||'_geometry_id = null
      WHERE building_id IN 
        (SELECT b.id FROM building b, geodb_pkg.match_overlap_relevant m 
           WHERE b.building_root_id = m.id1)
        AND i.is_external = 1';

    -- retrieve surfaces from relevant thematic surfaces
    EXECUTE 'UPDATE thematic_surface SET lod'||lod||'_multi_surface_id = null
      WHERE building_id IN 
        (SELECT b.id FROM building b, geodb_pkg.match_overlap_relevant m 
           WHERE b.building_root_id = m.id1)';
  END IF;

  IF lod >= 3 THEN
    -- retrieve all openings of all thematic surfaces beloning to all buildings and building parts
    EXECUTE 'UPDATE opening SET lod'||lod||'_multi_surface_id = null
      WHERE id IN
        (SELECT ot.opening_id FROM geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot
           WHERE ot.thematic_surface_id = t.id
             AND t.building_id = b.id
             AND b.building_root_id = m.id1)';
  END IF;

  IF lod >= 4 THEN
    -- room
    EXECUTE 'UPDATE room SET lod4_geometry_id = null
      WHERE building_id IN
        (SELECT b.id FROM geodb_pkg.match_overlap_relevant m, building b
           WHERE b.building_root_id = m.id1)';

    -- building furniture (in rooms) --if lod r is changed to f
    EXECUTE 'UPDATE building_furniture SET lod4_geometry_id = null
      WHERE room_id IN
        (SELECT r.id FROM geodb_pkg.match_overlap_relevant m, room r, building b
           WHERE r.building_id = b.id
             AND b.building_root_id = m.id1)';

    -- retrieve relevant internal (or external) building installation geometry (in rooms)
    EXECUTE 'UPDATE building_installation SET lod4_geometry_id = null
      WHERE room_id IN
        (SELECT r.id FROM geodb_pkg.match_overlap_relevant m, building b, room r
           WHERE r.building_id = b.id
             AND b.building_root_id = m.id1)';

    -- retrieve surfaces from relevant thematic surfaces (in rooms)
    EXECUTE 'UPDATE thematic_surface SET lod4_multi_surface_id = null
      WHERE room_id IN
        (SELECT r.id FROM geodb_pkg.match_overlap_relevant m, building b, room r
           WHERE r.building_id = b.id
             AND b.building_root_id = m.id1)';

    -- retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
    EXECUTE 'UPDATE opening SET lod4_multi_surface_id = null
      WHERE id IN
        (SELECT ot.opening_id FROM geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot, room r
           WHERE r.building_id = b.id
             AND t.room_id = r.id
             AND b.building_root_id = m.id1
             AND ot.thematic_surface_id = t.id)';

    -- retrieve relevant internal building installation geometry
    EXECUTE 'UPDATE building_installation SET lod4_geometry_id = null
      WHERE is_external = 0 AND building_id IN
        (SELECT b.id FROM geodb_pkg.match_overlap_relevant m, building b
           WHERE b.building_root_id = m.id1)';
  END IF;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'remove_geometry_from_cand: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_create_and_put_container(
  lod NUMERIC, 
  name_mode NUMERIC, 
  delimiter VARCHAR)
RETURNS SETOF void AS
$$
DECLARE
  seq_val INTEGER;
  old_geometry NUMERIC;
  log VARCHAR(4000);
  merge_cont_building_id_idx geodb_pkg.index_obj;
  merge_cont_id_idx geodb_pkg.index_obj;

  building_id_cur CURSOR FOR
    SELECT id1 FROM geodb_pkg.match_overlap_relevant;
BEGIN
  merge_cont_building_id_idx := geodb_pkg.idx_construct_normal('merge_cont_building_id_idx', 'geodb_pkg.merge_container_ids', 'building_id');
  merge_cont_id_idx := geodb_pkg.idx_construct_normal('merge_cont_id_idx', 'geodb_pkg.merge_container_ids', 'container_id');

  EXECUTE 'TRUNCATE TABLE geodb_pkg.merge_container_ids';

  log := geodb_pkg.idx_drop_index(merge_cont_building_id_idx);
  log := geodb_pkg.idx_drop_index(merge_cont_id_idx); 

  -- iterate through all affected buildings
  FOR building_id_rec IN building_id_cur LOOP
    -- create geometry id and register in container
    EXECUTE 'SELECT nextval(''surface_geometry_id_seq'')' INTO seq_val;
    EXECUTE 'INSERT INTO geodb_pkg.merge_container_ids (building_id, container_id) 
      VALUES ($1, $2)' USING building_id_rec.id1, seq_val;

    -- retrieve and delete old geometry
    BEGIN
      EXECUTE 'SELECT b.lod'||lod||'_geometry_id FROM building b
        WHERE b.id = (SELECT id2 FROM geodb_pkg.match_overlap_relevant WHERE id1 = $1)'
          INTO old_geometry USING building_id_rec.id1;

      EXCEPTION
        WHEN OTHERS THEN
          old_geometry := 0;
    END;

    -- create new multisurface as root element of new merge geometry
    EXECUTE 'INSERT INTO surface_geometry (id, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry)
      VALUES ($1, null, $2, 0, 0, 0, 0, 0, null)' USING seq_val, seq_val;

    -- set building geometry to new multisurface and process name
    IF name_mode=1 THEN
      -- ignore cand name
      EXECUTE 'UPDATE building SET lod'||lod||'_geometry_id = $1
        WHERE id = (SELECT id2 FROM geodb_pkg.match_overlap_relevant WHERE id1 = $2)'
          USING seq_val, building_id_rec.id1;
    ELSIF name_mode=2 THEN
      -- replace master name with cand name
      EXECUTE 'UPDATE building SET lod'||lod||'_geometry_id = $1,
        name = (SELECT name FROM building WHERE id = $2)
          WHERE id = (SELECT id2 FROM geodb_pkg.match_overlap_relevant WHERE id1 = $3)'
            USING seq_val, building_id_rec.id1, building_id_rec.id1;
    ELSE
      -- append cand name to master
      EXECUTE 'UPDATE building SET lod'||lod||'_geometry_id = $1,
        name = concat(name, nullif(concat($2, (SELECT name FROM building WHERE id = $3)), $4))
          WHERE id = (SELECT id2 FROM geodb_pkg.match_overlap_relevant WHERE id1 = $5)'
            USING seq_val, delimiter, building_id_rec.id1, delimiter, building_id_rec.id1;
    END IF;

    -- delete old geometry
    IF old_geometry > 0 THEN
      PERFORM geodb_pkg.del_delete_surface_geometry(old_geometry);
    END IF;    
  END LOOP;

  log := geodb_pkg.idx_create_index(merge_cont_building_id_idx);
  log := geodb_pkg.idx_create_index(merge_cont_id_idx);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'create_and_put_container: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_move_appearance()
RETURNS SETOF void AS
$$
DECLARE
  geom_hierachies NUMERIC;  
  app_id INTEGER;
  seq_val NUMERIC;
  building_id INTEGER;

  building_cur CURSOR FOR
    SELECT mcg.building_id, count(mcg.geometry_id) AS cnt_hierarchies FROM merge_collect_geom mcg 
      GROUP BY mcg.building_id;   
BEGIN
  -- iterate through all building matches
  FOR building_rec IN building_cur LOOP
    DECLARE
      app_cur CURSOR FOR
        SELECT DISTINCT a.id, a.theme, a.description, sd.id AS sd_id
          FROM merge_collect_geom cg, surface_data sd, appear_to_surface_data asd, appearance a 
            WHERE a.cityobject_id=building_rec.building_id
              AND asd.appearance_id=a.id 
              AND sd.id=asd.surface_data_id
              AND (SELECT count(*) FROM textureparam t WHERE t.surface_data_id=sd.id) > 0
                ORDER BY a.id;

      geom_cur CURSOR FOR
        SELECT DISTINCT tp.surface_geometry_id AS geometry_id, tp.surface_data_id AS sd_id, cg.geometry_id AS hierarchy_id
          FROM merge_collect_geom cg, textureparam tp 
            WHERE cg.building_id=building_rec.building_id
              AND tp.surface_geometry_id=cg.geometry_id;
    BEGIN
      app_id := -1;

      -- step 1: iterate through local appearances referencing a geometry that will be merged 
      -- into the newly created gml:MultiSurface of the reference building
      FOR app_rec IN app_cur LOOP
        IF app_rec.id != app_id THEN
          app_id := app_rec.id;

          -- create a new appearance element for the reference building
          -- into which we are going to transfer the surface data
          EXECUTE 'SELECT nextval(''appearance_id_seq'')' INTO seq_val;
          EXECUTE 'SELECT id2 FROM geodb_pkg.match_overlap_relevant WHERE id1=$1' INTO building_id USING building_rec.building_id;

          EXECUTE 'INSERT INTO appearance (id, name, name_codespace, description, theme, citymodel_id, cityobject_id)
            VALUES ($1, null, null, $2, $3, null, $4)' USING seq_val, app_rec.description, app_rec.theme, building_id;
        END IF;

        -- move existing surface data into the newly created appearance            
        EXECUTE 'UPDATE appear_to_surface_data SET appearance_id=$1
          WHERE appearance_id=$2 AND surface_data_id=$3'
            USING seq_val, app_rec.id, app_rec.sd_id;
      END LOOP;

      -- step 2: if any surface data of the appearance references the root element of the geometry
      -- to be merged we need to apply further checks
      FOR geom_rec IN geom_cur LOOP          
        -- if just one geometry hierarchy has to be merged we simply let the
        -- textureparam point to the new root geometry element created for the reference building
        IF building_rec.cnt_hierarchies = 1 THEN
          -- let textureparam point to newly created root element
          EXECUTE 'UPDATE textureparam SET surface_geometry_id=
              (SELECT mci.container_id FROM geodb_pkg.merge_container_ids mci WHERE mci.building_id=$1)
            WHERE surface_geometry_id=$2'
              USING building_rec.building_id, geom_rec.hierarchy_id;

          -- copy gml:id to newly created root element - this is required
          -- for referencing the geometry from within the appearance
          EXECUTE 'UPDATE surface_geometry SET (gmlid, gmlid_codespace)=
              (SELECT s.gmlid, s.gmlid_codespace FROM surface_geometry s WHERE s.id=$1)
            WHERE id=(SELECT mci.container_id FROM geodb_pkg.merge_container_ids mci WHERE mci.building_id=$2)'
              USING geom_rec.hierarchy_id, building_rec.building_id;

          -- if more than one geometry hierarchy is merged into a single geometry hierarchy
          -- for the reference building, things are a bit more complicated
        ELSE
          DECLARE
            counter NUMERIC;
            gmlid surface_geometry.gmlid%TYPE;
            gmlid_codespace surface_geometry.gmlid_codespace%TYPE;

            textureparam_cur CURSOR FOR
              SELECT * FROM textureparam WHERE surface_data_id=geom_rec.sd_id
                AND surface_geometry_id=geom_rec.hierarchy_id;

            surface_geometry_cur CURSOR FOR
              SELECT * FROM surface_geometry WHERE parent_id=geom_rec.hierarchy_id;

          BEGIN
            BEGIN
              EXECUTE 'SELECT gmlid, gmlid_codespace FROM surface_geometry WHERE id=$1'
                INTO gmlid, gmlid_codespace USING geom_rec.hierarchy_id;

              EXCEPTION
                WHEN OTHERS THEN
                  gmlid := 'ID';
                  gmlid_codespace := '';
            END;

            -- first we need to iterate over all textureparam which point to the root of the geometry hierachy to be merged.
            -- second we identify all direct childs of this root element. for each of these childs we create a copy 
            -- of the original textureparam and let it point to the child.           
            FOR textureparam_rec IN textureparam_cur LOOP
              counter := 0;
              FOR surface_geometry_rec IN surface_geometry_cur LOOP
                counter := counter + 1;

                -- create a new textureparam and let it point to the child instead of the root
                EXECUTE 'INSERT INTO textureparam (surface_geometry_id, surface_data_id, is_texture_parametrization, world_to_texture, texture_coordinates)
                  VALUES ($1, $2, $3, $4, $5)' USING surface_geometry_rec.id, geom_rec.sd_id, 
                    textureparam_rec.is_texture_parametrization, textureparam_rec.world_to_texture, textureparam_rec.texture_coordinates;

                -- make sure the child geometry referenced by the textureparam has a gml:id value
                IF surface_geometry_rec.gmlid IS NULL THEN
                  EXECUTE 'UPDATE surface_geometry SET gmlid=concat($1, $2), gmlid_codespace=$3
                    WHERE id=$4 AND gmlid is null'
                      USING gmlid, '_' || counter::VARCHAR, gmlid_codespace, surface_geometry_rec.id;
                END IF;
              END LOOP;
            END LOOP;
          END;
        END IF;
      END LOOP;
    END;
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'move_appearance: %', SQLERRM;

END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_move_geometry()
RETURNS SETOF void AS
$$
BEGIN
  -- UPDATE parent of immediate children of all collected geometries
  EXECUTE 'UPDATE surface_geometry SET parent_id = 
    (SELECT c.container_id FROM geodb_pkg.merge_container_ids c, merge_collect_geom g
       WHERE parent_id = g.geometry_id AND c.building_id = g.building_id)
    WHERE parent_id IN (SELECT geometry_id FROM merge_collect_geom)';

  -- change nested solids into composite surfaces since we throw everything into a multisurface
  EXECUTE 'UPDATE surface_geometry SET is_composite = 1, is_solid = 0
    WHERE root_id IN (SELECT geometry_id FROM merge_collect_geom) AND is_solid=1 AND root_id != id';

  -- UPDATE all root_ids
  EXECUTE 'UPDATE surface_geometry SET root_id = 
    (SELECT c.container_id FROM geodb_pkg.merge_container_ids c, merge_collect_geom g
       WHERE root_id = g.geometry_id AND c.building_id = g.building_id)
    WHERE root_id IN (SELECT geometry_id FROM merge_collect_geom) AND root_id != id';

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'move_geometry: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_update_lineage(lineage VARCHAR)
RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'UPDATE cityobject SET lineage = $1 WHERE id IN 
    (SELECT b.id FROM building b, geodb_pkg.match_overlap_relevant m 
       WHERE b.building_root_id = m.id1)'
         USING lineage;

  -- retrieve relevant building installation geometry
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT i.id FROM building_installation i, building b, geodb_pkg.match_overlap_relevant m
         WHERE i.building_id = b.id 
           AND b.building_root_id = m.id1 
           AND i.is_external = 1)'
             USING lineage;

  -- retrieve surfaces from relevant thematic surfaces
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT t.id FROM thematic_surface t, building b, geodb_pkg.match_overlap_relevant m
         WHERE t.building_id = b.id
           AND b.building_root_id = m.id1)'
             USING lineage;

  -- retrieve all openings of all thematic surfaces beloning to all buildings and building parts
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT o.id FROM opening o, geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot
         WHERE o.id = ot.opening_id 
           AND ot.thematic_surface_id = t.id
           AND t.building_id = b.id
           AND b.building_root_id = m.id1)'
             USING lineage;

  -- room
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT r.id FROM room r, geodb_pkg.match_overlap_relevant m, building b
         WHERE r.building_id = b.id
           AND b.building_root_id = m.id1)'
             USING lineage;

  -- building furniture (in rooms) --if lod r is changed to f
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT f.id FROM building_furniture f, geodb_pkg.match_overlap_relevant m, room r, building b
         WHERE f.room_id = r.id
           AND r.building_id = b.id
           AND b.building_root_id = m.id1)'
             USING lineage;

  -- retrieve relevant internal (or external) building installation geometry (in rooms)
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT i.id FROM building_installation i, geodb_pkg.match_overlap_relevant m, building b, room r
         WHERE i.room_id = r.id
           AND r.building_id = b.id
           AND b.building_root_id = m.id1)'
             USING lineage;

  -- retrieve surfaces from relevant thematic surfaces (in rooms)
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT t.id FROM thematic_surface t, geodb_pkg.match_overlap_relevant m, building b, room r
         WHERE t.room_id = r.id
           AND r.building_id = b.id
           AND b.building_root_id = m.id1)'
             USING lineage;

  -- retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
      (SELECT o.id FROM opening o, geodb_pkg.match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot, room r
         WHERE o.id = ot.opening_id
           AND r.building_id = b.id
           AND t.room_id = r.id
           AND b.building_root_id = m.id1
           AND ot.thematic_surface_id = t.id)'
             USING lineage;

  -- retrieve relevant internal building installation geometry
  EXECUTE 'UPDATE cityobject SET lineage = $1
    WHERE id IN 
	  (SELECT i.id FROM building_installation i, geodb_pkg.match_overlap_relevant m, building b
         WHERE i.building_id = b.id
           AND b.building_root_id = m.id1
           AND i.is_external = 0)'
             USING lineage;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'collect_all_geometry: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_delete_head_of_merge_geometry()
RETURNS SETOF void AS
$$
DECLARE
  geometry_cur CURSOR FOR
    SELECT geometry_id FROM merge_collect_geom;
BEGIN
  -- cleanly delete root of merged geometry hierarchy
  FOR geometry_rec IN geometry_cur LOOP
    PERFORM geodb_pkg.del_delete_surface_geometry(geometry_rec.geometry_id);
  END LOOP;

  PERFORM geodb_pkg.del_cleanup_appearances(0);
END;
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.merge_delete_relevant_candidates()
RETURNS SETOF void AS
$$
DECLARE
  candidate_cur CURSOR FOR
    SELECT id1 FROM geodb_pkg.match_overlap_relevant;
BEGIN
  FOR candidate_rec IN candidate_cur LOOP
    PERFORM geodb_pkg.del_delete_building(candidate_rec.id1);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_candidates: %', SQLERRM;
END;
$$ 
LANGUAGE plpgsql;