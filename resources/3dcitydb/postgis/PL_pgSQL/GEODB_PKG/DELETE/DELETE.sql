-- DELETE.sql
--
-- Authors:     Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Conversion:  Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2013  Institute for Geodesy and Geoinformation Science,
--                             Technische Universitaet Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 3.0.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- All functions are part of the geodb_pkg.schema and DELETE-"Package" 
-- They start with the prefix "del_" to guarantee a better overview 
-- in the PGAdminIII-Tool.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                     | Author  | Conversion
-- 1.1.0     2013-02-22   PostGIS Version                             FKun
-- 1.1.0     2012-02-22   some performance improvements     CNag
-- 1.0.0     2011-02-11   release version                   CNag
--

/*
internal helpers
*/

CREATE OR REPLACE FUNCTION geodb_pkg.del_is_not_referenced(
  table_name VARCHAR, 
  check_column VARCHAR, 
  check_id NUMERIC, 
  not_column VARCHAR, 
  not_id NUMERIC) 
RETURNS BOOLEAN AS
$$
DECLARE
  ref_cur refcursor;
  dummy NUMERIC;
  is_not_referenced BOOLEAN;
BEGIN
  OPEN ref_cur FOR EXECUTE 'SELECT 1 from ' || table_name || ' WHERE ' || check_column || '=' || check_id || ' and not ' || not_column || '=' || not_id;

  LOOP 
    FETCH ref_cur INTO dummy;
    IF NOT FOUND THEN
      is_not_referenced := true;
    ELSE
      is_not_referenced := false;
    END IF;
    EXIT;
  END LOOP;

  CLOSE ref_cur;

  RETURN is_not_referenced;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM SURFACE_GEOMETRY
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_intern_delete_surface_geometry(pid numeric) RETURNS SETOF void AS
$$
DECLARE
  textureparam_rec INTEGER;
  surface_geometry_rec INTEGER;
BEGIN
  FOR textureparam_rec IN EXECUTE 'SELECT FROM textureparam WHERE surface_geometry_id IN 
            (WITH RECURSIVE recursive_query(id, parent_id, level) 
              AS (
                SELECT id, parent_id, 1 AS level FROM surface_geometry WHERE id=$1
              UNION ALL
                SELECT sg.id, sg.parent_id, rq.level + 1 AS level FROM surface_geometry sg, recursive_query rq WHERE sg.parent_id = rq.id
              )
              SELECT id FROM recursive_query ORDER BY level DESC)' USING pid LOOP
    EXECUTE 'DELETE FROM textureparam WHERE id = $1' USING textureparam_rec;
  END LOOP;
  
  FOR surface_geometry_rec IN EXECUTE 'SELECT FROM surface_geometry WHERE id IN
            (WITH RECURSIVE recursive_query(id, parent_id, level) 
              AS (
                SELECT id, parent_id, 1 AS level FROM surface_geometry WHERE id=$1
              UNION ALL
                SELECT sg.id, sg.parent_id, rq.level + 1 AS level FROM surface_geometry sg, recursive_query rq WHERE sg.parent_id = rq.id
              )
              SELECT id FROM recursive_query ORDER BY level DESC)' USING pid LOOP 			  
    EXECUTE 'DELETE FROM surface_geometry WHERE id = $1' USING surface_geometry_rec;
  END LOOP;
	 
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'intern_delete_surface_geometry (id: %): %', pid, SQLERRM;
END; 
$$
LANGUAGE plpgsql;



/*
internal: DELETE FROM IMPLICIT_GEOMETRY
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_intern_delete_implicit_geom(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  implicit_geometry_rec INTEGER;
BEGIN
  EXECUTE 'SELECT relative_geometry_id FROM implicit_geometry WHERE id=$1' INTO implicit_geometry_rec USING pid;
  EXECUTE 'DELETE FROM implicit_geometry WHERE id=$1' USING pid;
  
  PERFORM geodb_pkg.del_post_delete_implicit_geom(implicit_geometry_rec);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'intern_delete_implicit_geom (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql; 


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_implicit_geom(relative_geometry_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  IF relative_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(relative_geometry_id);
  END IF;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_implicit_geom (id: %): %', relative_geometry_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM CITY_OBJECT
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_cityobject(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  appearance_rec INTEGER;   
BEGIN
  EXECUTE 'DELETE FROM cityobject_member WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM group_to_cityobject WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM generalization WHERE generalizes_to_id=$1' USING pid;
  EXECUTE 'DELETE FROM generalization WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM external_reference WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM cityobject_genericattrib WHERE cityobject_id=$1' USING pid;
  EXECUTE 'UPDATE cityobjectgroup SET parent_cityobject_id=null WHERE parent_cityobject_id=$1' USING pid;

  FOR appearance_rec IN EXECUTE 'SELECT id FROM appearance WHERE cityobject_id=$1' USING pid LOOP
    PERFORM geodb_pkg.del_delete_row_appearance(appearance_rec);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_cityobject (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_intern_delete_cityobject(pid NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_cityobject(pid);
  EXECUTE 'DELETE FROM cityobject WHERE id=$1' USING pid;
 
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'intern_delete_cityobject (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM CITYMODEL
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_citymodel(citymodel_row_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  appearance_rec INTEGER;
BEGIN
  -- TODO
  -- delete contained cityobjects!
  EXECUTE 'DELETE FROM cityobject_member WHERE citymodel_id=$1' USING citymodel_row_id;

  FOR appearance_rec IN EXECUTE 'SELECT id FROM appearance WHERE cityobject_id=$1' USING citymodel_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_appearance(appearance_rec);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_citymodel (id: %): %', citymodel_row_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_citymodel(citymodel_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_citymodel(citymodel_id);
  EXECUTE 'DELETE FROM citymodel WHERE id=$1' USING citymodel_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_citymodel (id: %): %', citymodel_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM APPEARANCE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_appearance(appearance_row_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  surface_data_rec INTEGER;
BEGIN
  -- delete surface data not being referenced by appearances any more
  FOR surface_data_rec IN EXECUTE 'SELECT s.id from surface_data s, appear_to_surface_data ats WHERE s.id=ats.surface_data_id and ats.appearance_id=$1' 
                        USING appearance_row_id LOOP
    IF geodb_pkg.del_is_not_referenced('appear_to_surface_data', 'surface_data_id', surface_data_rec, 'appearance_id', appearance_row_id) THEN
      PERFORM geodb_pkg.del_delete_row_surface_data(surface_data_rec);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM appear_to_surface_data WHERE appearance_id=$1' USING appearance_row_id;
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_appearance (id: %): %', appearance_row_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_appearance(appearance_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_appearance(appearance_id);
  EXECUTE 'DELETE FROM appearance WHERE id=$1' USING appearance_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_appearance (id: %): %', appearance_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM SURFACE_DATA
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_surface_data(surface_data_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM appear_to_surface_data WHERE surface_data_id=$1' USING surface_data_id;
  EXECUTE 'DELETE FROM textureparam WHERE surface_data_id=$1' USING surface_data_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_surface_data (id: %): %', surface_data_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_surface_data(surface_data_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_surface_data(surface_data_id);
  EXECUTE 'DELETE FROM surface_data WHERE id=$1' USING surface_data_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_surface_data (id: %): %', surface_data_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM CITYOBJECTGROUP
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_cityobjectgroup(cityobjectgroup_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM group_to_cityobject WHERE cityobjectgroup_id=$1' USING cityobjectgroup_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_cityobjectgroup (id: %): %', cityobjectgroup_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_cityobjectgroup(cityobjectgroup_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  cityobjectgroup_rec cityobjectgroup%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM cityobjectgroup WHERE id=$1' INTO cityobjectgroup_rec USING cityobjectgroup_id;

  PERFORM geodb_pkg.del_pre_delete_cityobjectgroup(cityobjectgroup_id);
  EXECUTE 'DELETE FROM cityobjectgroup WHERE id=$1' USING cityobjectgroup_id;
  PERFORM geodb_pkg.del_post_delete_cityobjectgroup(cityobjectgroup_id, cityobjectgroup_rec.surface_geometry_id);      

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_cityobjectgroup (id: %): %', cityobjectgroup_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_cityobjectgroup(
  cityobjectgroup_id NUMERIC,
  surface_geometry_id NUMERIC)
RETURNS SETOF void AS
$$
BEGIN
  IF surface_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(surface_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(cityobjectgroup_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_cityobjectgroup (id: %): %', cityobjectgroup_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM THEMATIC_SURFACE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_thematic_surface(thematic_surface_row_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  opening_rec INTEGER;
BEGIN
  -- delete openings not being referenced by a thematic surface any more
  FOR opening_rec IN EXECUTE 'SELECT o.id FROM opening o, opening_to_them_surface otm WHERE o.id=otm.opening_id AND otm.thematic_surface_id=$1' 
                        USING thematic_surface_row_id LOOP
    IF geodb_pkg.del_is_not_referenced('opening_to_them_surface', 'opening_id', opening_rec, 'thematic_surface_id', thematic_surface_row_id) THEN
      PERFORM geodb_pkg.del_delete_row_opening(opening_rec);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM opening_to_them_surface WHERE thematic_surface_id=$1' USING thematic_surface_row_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_thematic_surface (id: %): %', thematic_surface_row_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_thematic_surface(thematic_surface_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  thematic_surface_rec thematic_surface%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM thematic_surface WHERE id=$1' INTO thematic_surface_rec USING thematic_surface_id;
  
  PERFORM geodb_pkg.del_pre_delete_thematic_surface(thematic_surface_id);
  EXECUTE 'DELETE FROM thematic_surface WHERE id=$1' USING thematic_surface_id;
  PERFORM geodb_pkg.del_post_delete_thematic_surface(thematic_surface_id, thematic_surface_rec.lod2_multi_surface_id, thematic_surface_rec.lod3_multi_surface_id, thematic_surface_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_thematic_surface (id: %): %', thematic_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_thematic_surface(
  thematic_surface_id NUMERIC,
  ts_lod2_multi_surface_id NUMERIC,
  ts_lod3_multi_surface_id NUMERIC, 
  ts_lod4_multi_surface_id NUMERIC) 
RETURNS SETOF void AS
$$
BEGIN
  IF ts_lod2_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(ts_lod2_multi_surface_id);
  END IF;

  IF ts_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(ts_lod3_multi_surface_id);
  END IF;

  IF ts_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(ts_lod4_multi_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(thematic_surface_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_thematic_surface (id: %): %', thematic_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM OPENING
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_opening(opening_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM opening_to_them_surface WHERE opening_id=$1' USING opening_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_opening (id: %): %', opening_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_opening(opening_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  opening_rec opening%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM opening WHERE id=$1' INTO opening_rec USING opening_id;

  PERFORM geodb_pkg.del_pre_delete_opening(opening_id);
  EXECUTE 'DELETE FROM opening WHERE id=$1' USING opening_id;
  PERFORM geodb_pkg.del_post_delete_opening(opening_id, opening_rec.address_id, opening_rec.lod3_multi_surface_id, opening_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_opening (id: %): %', opening_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_opening(
  opening_id NUMERIC,
  opening_address_id NUMERIC,
  opening_lod3_multi_surface_id NUMERIC, 
  opening_lod4_multi_surface_id NUMERIC)
RETURNS SETOF void AS
$$
DECLARE
  address_rec INTEGER;
BEGIN 
  IF opening_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(opening_lod3_multi_surface_id);
  END IF;

  IF opening_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(opening_lod4_multi_surface_id);
  END IF;

  -- delete addresses not being referenced from buildings and openings any more
  FOR address_rec IN EXECUTE 'SELECT a.id FROM address a LEFT OUTER JOIN address_to_building ab ON a.id=ab.address_id WHERE a.id=$1 AND ab.address_id IS NULL' 
                        USING opening_address_id LOOP
    IF geodb_pkg.del_is_not_referenced('opening', 'address_id', address_rec, 'id', opening_id) THEN
      PERFORM geodb_pkg.del_delete_address(address_rec);
    END IF;   
  END LOOP;

  PERFORM geodb_pkg.del_intern_delete_cityobject(opening_id);
 
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_opening (id: %): %', opening_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM BUILDING_INSTALLATION
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_building_inst(building_installation_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_installation_rec building_installation%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_installation WHERE id=$1' INTO building_installation_rec USING building_installation_id;

  EXECUTE 'DELETE FROM building_installation WHERE id=$1' USING building_installation_id;
  PERFORM geodb_pkg.del_post_delete_building_inst(building_installation_id, building_installation_rec.lod2_geometry_id, building_installation_rec.lod3_geometry_id, building_installation_rec.lod4_geometry_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_building_installation (id: %): %', building_installation_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building_inst(
  building_installation_id NUMERIC,
  bi_lod2_geometry_id NUMERIC,
  bi_lod3_geometry_id NUMERIC,
  bi_lod4_geometry_id NUMERIC)
RETURNS SETOF void AS
$$
BEGIN
  IF bi_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(bi_lod2_geometry_id);
  END IF;

  IF bi_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(bi_lod3_geometry_id);
  END IF;

  IF bi_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(bi_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(building_installation_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_building_inst (id: %): %', building_installation_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM ROOM
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_room(room_row_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  ts_id INTEGER;
  bi_id INTEGER;
  bf_id INTEGER;
BEGIN
  FOR ts_id IN EXECUTE 'SELECT ts.id FROM thematic_surface ts WHERE ts.room_id=$1' USING room_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_thematic_surface(ts_id);
  END LOOP;

  FOR bi_id IN EXECUTE 'SELECT bi.id FROM building_installation bi WHERE bi.room_id=$1' USING room_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_building_inst(bi_id);
  END LOOP;
  
  FOR bf_id IN EXECUTE 'SELECT bf.id FROM building_furniture bf WHERE bf.room_id=$1' USING room_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_building_furniture(bf_id);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_room (id: %): %', room_row_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_room(room_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  room_rec room%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM room WHERE id=$1' INTO room_rec USING room_id;

  PERFORM geodb_pkg.del_pre_delete_room(room_id);
  EXECUTE 'DELETE FROM room WHERE id=$1' USING room_id;
  PERFORM geodb_pkg.del_post_delete_room(room_id, room_rec.lod4_geometry_id);
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_room (id: %): %', room_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_room(
  room_id NUMERIC,
  room_lod4_geometry_id NUMERIC)
RETURNS SETOF void AS
$$
BEGIN
  IF room_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(room_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(room_id);
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_room (id: %): %', room_id, SQLERRM;
END; 
$$
LANGUAGE plpgsql;


/*
internal: DELETE FROM BUILDING_FURNITURE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_building_furniture(building_furniture_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_furniture_rec building_furniture%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_furniture WHERE id=$1' INTO building_furniture_rec USING building_furniture_id;

  EXECUTE 'DELETE FROM building_furniture WHERE id=$1' USING building_furniture_id;
  PERFORM geodb_pkg.del_post_delete_building_furniture(building_furniture_id, building_furniture_rec.lod4_geometry_id, building_furniture_rec.lod4_implicit_rep_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_building_furniture (id: %): %', building_furniture_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building_furniture(
  building_furniture_id NUMERIC, 
  bf_lod4_geometry_id NUMERIC, 
  bf_lod4_implicit_rep_id NUMERIC) 
RETURNS SETOF void AS
$$
BEGIN
  IF bf_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(bf_rec.lod4_geometry_id);
  END IF;

  IF bf_lod4_implicit_rep_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_implicit_geom(bf_lod4_implicit_rep_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(building_furniture_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_building_furniture (id: %): %', building_furniture_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM BUILDING
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_building(building_row_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  bp_id INTEGER;
  ts_id INTEGER;
  bi_id INTEGER;
  room_id INTEGER;
  ad_id INTEGER;
BEGIN
  FOR bp_id IN EXECUTE 'SELECT id FROM building WHERE id != $1 AND building_parent_id=$1' USING building_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_building(bp_id);
  END LOOP;

  FOR ts_id IN EXECUTE 'SELECT id FROM thematic_surface WHERE building_id=$1' USING building_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_thematic_surface(ts_id);
  END LOOP;

  FOR bi_id IN EXECUTE 'SELECT id FROM building_installation bi WHERE building_id=$1' USING building_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_building_inst(bi_id);
  END LOOP;

  FOR room_id IN EXECUTE 'SELECT address_id FROM address_to_building WHERE building_id=$1' USING building_row_id LOOP
    PERFORM geodb_pkg.del_delete_row_room(room_id);
  END LOOP;
	
  -- delete addresses being not referenced from buildings any more
  FOR ad_id IN EXECUTE 'SELECT address_id FROM address_to_building WHERE building_id=$1' USING building_row_id LOOP
    IF geodb_pkg.del_is_not_referenced('address_to_building', 'address_id', ad_id, 'building_id', building_row_id) THEN
      PERFORM geodb_pkg.del_delete_address(ad_id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM address_to_building WHERE building_id=$1' USING building_row_id;
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_building (id: %): %', building_row_id, SQLERRM;

END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_building(building_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_rec building%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building WHERE id=$1' INTO building_rec USING building_id;

  PERFORM geodb_pkg.del_pre_delete_building(building_id);
  EXECUTE 'DELETE FROM building WHERE id=$1' USING building_id;
  PERFORM geodb_pkg.del_post_delete_building(building_id, building_rec.lod1_geometry_id, building_rec.lod2_geometry_id, building_rec.lod3_geometry_id, building_rec.lod4_geometry_id);
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_building (id: %): %', building_id, SQLERRM;

END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building(
  building_id NUMERIC,
  b_lod1_geometry_id NUMERIC,
  b_lod2_geometry_id NUMERIC,
  b_lod3_geometry_id NUMERIC,
  b_lod4_geometry_id NUMERIC
) RETURNS SETOF void AS
$$
BEGIN
  IF b_lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(b_lod1_geometry_id);
  END IF; 

  IF b_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(b_lod2_geometry_id);
  END IF;

  IF b_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(b_lod3_geometry_id);
  END IF;

  IF b_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(b_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(building_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_building (id: %): %', building_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
PUBLIC API FUNCTIONS
*/  
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_surface_geometry(pid NUMERIC, clean_apps INTEGER DEFAULT 0) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_intern_delete_surface_geometry(pid);

  IF clean_apps <> 0 THEN
    PERFORM geodb_pkg.del_cleanup_appearances(0);
  END IF;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_surface_geometry (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_implicit_geometry(pid NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_intern_delete_implicit_geom(pid);
  
  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_implicit_geometry (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_external_reference(pid NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM external_reference WHERE id=$1' USING pid;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_external_reference (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql; 


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_citymodel(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  citymodel_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM citymodel WHERE id=$1'
    INTO citymodel_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_citymodel(citymodel_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_citymodel (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_appearance(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  appearance_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM appearance WHERE id=$1'
    INTO appearance_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_appearance(appearance_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_appearance (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_surface_data(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  surface_data_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM surface_data WHERE id=$1'
    INTO surface_data_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_surface_data(surface_data_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_surface_data (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_cityobjectgroup(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  cityobjectgroup_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM cityobjectgroup WHERE id=$1'
    INTO cityobjectgroup_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_cityobjectgroup(cityobjectgroup_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_cityobjectgroup (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_thematic_surface(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  thematic_surface_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM thematic_surface WHERE id=$1'
    INTO thematic_surface_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_thematic_surface(thematic_surface_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_thematic_surface (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_opening(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  opening_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM opening WHERE id=$1'
    INTO opening_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_opening(opening_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_opening (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_address(pid NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM address_to_building WHERE address_id=$1' USING pid;
  EXECUTE 'UPDATE opening SET address_id=null WHERE address_id=$1' USING pid;
  EXECUTE 'DELETE FROM address WHERE id=$1' USING pid;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_address (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_building_installation(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_installation_rec INTEGER;
BEGIN
  EXECUTE 'SELECT id FROM building_installation WHERE id=$1'
    INTO building_installation_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_building_inst(building_installation_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_building_installation (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_room(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  room_rec INTEGER;    
BEGIN
  EXECUTE 'SELECT id FROM room WHERE id=$1'
    INTO room_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_room(room_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_room (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_building_furniture(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_furniture_rec INTEGER;    
BEGIN
  EXECUTE 'SELECT id FROM building_furniture WHERE id=$1'
    INTO building_furniture_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_building_furniture(building_furniture_rec);
  
  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_building_furniture (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_building(pid NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_rec INTEGER;    
BEGIN
  EXECUTE 'SELECT id FROM building WHERE id=$1'
    INTO building_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_building(building_rec);

  EXCEPTION
    WHEN no_data_found THEN
	  RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_building (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_cleanup_appearances(only_global INTEGER DEFAULT 1) RETURNS SETOF void AS
$$
DECLARE
  sd_global_id INTEGER;
  app_global_id INTEGER;
  app_id INTEGER;
BEGIN
  -- global appearances are not related to a cityobject.
  -- however, we assume that all surface geometries of a cityobject
  -- have been deleted at this stage. thus, we can check and delete
  -- surface data which does not have a valid texture parameterization
  -- any more.
  FOR sd_global_id IN SELECT s.id FROM surface_data s LEFT OUTER JOIN textureparam t 
               ON s.id=t.surface_data_id WHERE t.surface_data_id IS NULL LOOP
    PERFORM geodb_pkg.del_delete_row_surface_data(sd_global_id);
  END LOOP;

  -- delete appearances which does not have surface data any more
  IF only_global=1 THEN
    FOR app_global_id IN SELECT a.id FROM appearance a LEFT OUTER JOIN appear_to_surface_data asd
                           ON a.id=asd.appearance_id WHERE a.cityobject_id IS NULL and asd.appearance_id IS NULL LOOP
      PERFORM geodb_pkg.del_delete_row_appearance(app_global_id);
    END LOOP;
  ELSE
    FOR app_id IN SELECT a.id FROM appearance a LEFT OUTER JOIN appear_to_surface_data asd 
                    ON a.id=asd.appearance_id WHERE asd.appearance_id IS NULL LOOP
      PERFORM geodb_pkg.del_delete_row_appearance(app_id);
    END LOOP;
  END IF;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'cleanup_appearances: %', SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_cleanup_cityobjectgroups() RETURNS SETOF void AS
$$
DECLARE
  cityobjectgroup_rec INTEGER;
BEGIN
  FOR cityobjectgroup_rec IN SELECT g.id FROM cityobjectgroup g LEFT OUTER JOIN group_to_cityobject gtc
               ON g.id=gtc.cityobject_id WHERE gtc.cityobject_id IS NULL LOOP
    PERFORM geodb_pkg.del_delete_row_cityobjectgroup(cityobjectgroup_rec);
  END LOOP;
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'cleanup_cityobjectgroups: %', SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_cleanup_citymodels() RETURNS SETOF void AS
$$
DECLARE
  citymodel_rec INTEGER;
BEGIN
  FOR citymodel_rec IN SELECT c.id FROM citymodel c LEFT OUTER JOIN cityobject_member cm
               ON c.id=cm.citymodel_id WHERE cm.citymodel_id IS NULL LOOP
    PERFORM geodb_pkg.del_delete_row_citymodel(citymodel_rec);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'cleanup_citymodel: %', SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;