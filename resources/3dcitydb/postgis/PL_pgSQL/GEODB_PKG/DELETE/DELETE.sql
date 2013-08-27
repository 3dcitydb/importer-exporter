-- DELETE.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--              György Hudra <hudra@moss.de>
--
-- Copyright:   (c) 2013       Faculty of Civil, Geo and Environmental Engineering, 
--                             Chair of Geoinformatics,
--                             Technische Universitaet München, Germany
--                             http://www.gis.bv.tum.de/
--              (c) 2007-2013  Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description                               | Author
-- 1.2.0     2013-08-08   extended to all thematic classes            FKun
--                                                                    GHud
-- 1.1.0     2012-02-22   some performance improvements               CNag
-- 1.0.0     2011-02-11   release version                             CNag
--

CREATE OR REPLACE FUNCTION geodb_pkg.del_is_not_referenced(
  table_name VARCHAR, 
  check_column VARCHAR, 
  check_id INTEGER, 
  not_column VARCHAR, 
  not_id INTEGER) 
RETURNS BOOLEAN AS
$$
DECLARE
  is_not_referenced BOOLEAN;
BEGIN
  EXECUTE 'SELECT EXISTS (SELECT * FROM ' || table_name || ' WHERE ' || check_column || '=$1 AND NOT ' || not_column || '=$2)'
             INTO is_not_referenced USING check_id, not_id;

  RETURN is_not_referenced;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from SURFACE_GEOMETRY
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_surface_geometry(pid INTEGER, clean_apps INTEGER DEFAULT 0) RETURNS SETOF void AS
$$
DECLARE
  textureparam_rec INTEGER;
  surface_geometry_rec INTEGER;
BEGIN
  FOR textureparam_rec IN EXECUTE 'SELECT surface_geometry_id FROM textureparam WHERE surface_geometry_id IN
             (WITH RECURSIVE geometry(id, parent_id, level) AS (
                SELECT sg.id, sg.parent_id, 1 AS level FROM surface_geometry sg WHERE sg.id=$1
              UNION ALL
                SELECT sg.id, sg.parent_id, g.level + 1 AS level FROM surface_geometry sg, geometry g WHERE sg.parent_id = g.id
              )
              SELECT id FROM geometry ORDER BY level DESC)' USING pid LOOP
    EXECUTE 'DELETE FROM textureparam WHERE surface_geometry_id = $1' USING textureparam_rec;
  END LOOP;

  FOR surface_geometry_rec IN EXECUTE 'WITH RECURSIVE geometry(id, parent_id, level) AS (
                SELECT sg.id, sg.parent_id, 1 AS level FROM surface_geometry sg WHERE sg.id=$1
              UNION ALL
                SELECT sg.id, sg.parent_id, g.level + 1 AS level FROM surface_geometry sg, geometry g WHERE sg.parent_id = g.id
              )
              SELECT id FROM geometry ORDER BY level DESC' USING pid LOOP 			  
    EXECUTE 'DELETE FROM surface_geometry WHERE id = $1' USING surface_geometry_rec;
  END LOOP;

  IF clean_apps <> 0 THEN
    PERFORM geodb_pkg.del_cleanup_appearances(0);
  END IF;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'intern_delete_surface_geometry (id: %): %', pid, SQLERRM;
END;
$$
LANGUAGE plpgsql;


/*
delete from IMPLICIT_GEOMETRY
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_implicit_geom(pid INTEGER) RETURNS SETOF void AS
$$
DECLARE
  implicit_geometry_rec INTEGER;
BEGIN
  EXECUTE 'SELECT relative_geometry_id FROM implicit_geometry WHERE id=$1' INTO implicit_geometry_rec USING pid;
  EXECUTE 'DELETE FROM implicit_geometry WHERE id=$1' USING pid;

  PERFORM geodb_pkg.del_post_delete_implicit_geom(implicit_geometry_rec);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_implicit_geom (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_implicit_geom(relative_geometry_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  IF relative_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(relative_geometry_id);
  END IF;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_implicit_geom (id: %): %', relative_geometry_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from CITY_OBJECT
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_cityobject(pid INTEGER) RETURNS SETOF void AS
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
    PERFORM geodb_pkg.del_delete_appearance(appearance_rec);
  END LOOP;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_cityobject (id: %): %', pid, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_intern_delete_cityobject(pid INTEGER) RETURNS SETOF void AS
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
delete from CITYMODEL
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_citymodel(cm_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  appearance_rec INTEGER;
BEGIN
  -- TODO
  -- delete contained cityobjects!
  EXECUTE 'DELETE FROM cityobject_member WHERE citymodel_id=$1' USING cm_id;

  FOR appearance_rec IN EXECUTE 'SELECT id FROM appearance WHERE cityobject_id=$1' USING cm_id LOOP
    PERFORM geodb_pkg.del_delete_appearance(appearance_rec);
  END LOOP;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_citymodel (id: %): %', cm_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_citymodel(citymodel_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_citymodel(citymodel_id);
  EXECUTE 'DELETE FROM citymodel WHERE id=$1' USING citymodel_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_citymodel (id: %): %', citymodel_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from APPEARANCE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_appearance(app_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  surface_data_rec INTEGER;
BEGIN
  -- delete surface data not being referenced by appearances any more
  FOR surface_data_rec IN EXECUTE 'SELECT s.id from surface_data s, appear_to_surface_data ats WHERE s.id=ats.surface_data_id AND ats.appearance_id=$1' 
      USING app_id LOOP
    IF geodb_pkg.del_is_not_referenced('appear_to_surface_data', 'surface_data_id', surface_data_rec, 'appearance_id', app_id) THEN
      PERFORM geodb_pkg.del_delete_surface_data(surface_data_rec);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM appear_to_surface_data WHERE appearance_id=$1' USING app_id;
  
  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_appearance (id: %): %', app_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_appearance(appearance_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_appearance(appearance_id);
  EXECUTE 'DELETE FROM appearance WHERE id=$1' USING appearance_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_appearance (id: %): %', appearance_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from SURFACE_DATA
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_surface_data(sd_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM appear_to_surface_data WHERE surface_data_id=$1' USING sd_id;
  EXECUTE 'DELETE FROM textureparam WHERE surface_data_id=$1' USING sd_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_surface_data (id: %): %', sd_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_surface_data(surface_data_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_surface_data(surface_data_id);
  EXECUTE 'DELETE FROM surface_data WHERE id=$1' USING surface_data_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_surface_data (id: %): %', surface_data_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from CITYOBJECTGROUP
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_cityobjectgroup(cog_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM group_to_cityobject WHERE cityobjectgroup_id=$1' USING cog_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_cityobjectgroup (id: %): %', cog_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_cityobjectgroup(cityobjectgroup_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  cityobjectgroup_rec cityobjectgroup%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM cityobjectgroup WHERE id=$1' INTO cityobjectgroup_rec USING cityobjectgroup_id;

  PERFORM geodb_pkg.del_pre_delete_cityobjectgroup(cityobjectgroup_id);
  EXECUTE 'DELETE FROM cityobjectgroup WHERE id=$1' USING cityobjectgroup_id;
  PERFORM geodb_pkg.del_post_delete_cityobjectgroup(cityobjectgroup_id, cityobjectgroup_rec.surface_geometry_id);      

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_cityobjectgroup (id: %): %', cityobjectgroup_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_cityobjectgroup(
  cityobjectgroup_id INTEGER,
  surface_geometry_id INTEGER)
RETURNS SETOF void AS
$$
BEGIN
  IF surface_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(surface_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(cityobjectgroup_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_cityobjectgroup (id: %): %', cityobjectgroup_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from THEMATIC_SURFACE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_thematic_surface(ts_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  opening_rec INTEGER;
BEGIN
  -- delete openings not being referenced by a thematic surface any more
  FOR opening_rec IN EXECUTE 'SELECT o.id FROM opening o, opening_to_them_surface otm WHERE o.id=otm.opening_id AND otm.thematic_surface_id=$1' 
      USING ts_id LOOP
    IF geodb_pkg.del_is_not_referenced('opening_to_them_surface', 'opening_id', opening_rec, 'thematic_surface_id', ts_id) THEN
      PERFORM geodb_pkg.del_delete_opening(opening_rec);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM opening_to_them_surface WHERE thematic_surface_id=$1' USING ts_id;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_thematic_surface (id: %): %', ts_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_thematic_surface(thematic_surface_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  thematic_surface_rec thematic_surface%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM thematic_surface WHERE id=$1' INTO thematic_surface_rec USING thematic_surface_id;
  
  PERFORM geodb_pkg.del_pre_delete_thematic_surface(thematic_surface_id);
  EXECUTE 'DELETE FROM thematic_surface WHERE id=$1' USING thematic_surface_id;
  PERFORM geodb_pkg.del_post_delete_thematic_surface(thematic_surface_id, thematic_surface_rec.lod2_multi_surface_id, thematic_surface_rec.lod3_multi_surface_id, thematic_surface_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_thematic_surface (id: %): %', thematic_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_thematic_surface(
  thematic_surface_id INTEGER,
  ts_lod2_multi_surface_id INTEGER,
  ts_lod3_multi_surface_id INTEGER, 
  ts_lod4_multi_surface_id INTEGER) 
RETURNS SETOF void AS
$$
BEGIN
  IF ts_lod2_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(ts_lod2_multi_surface_id);
  END IF;

  IF ts_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(ts_lod3_multi_surface_id);
  END IF;

  IF ts_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(ts_lod4_multi_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(thematic_surface_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_thematic_surface (id: %): %', thematic_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from OPENING
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_opening(o_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM opening_to_them_surface WHERE opening_id=$1' USING o_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_opening (id: %): %', o_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_opening(opening_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  opening_rec opening%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM opening WHERE id=$1' INTO opening_rec USING opening_id;

  PERFORM geodb_pkg.del_pre_delete_opening(opening_id);
  EXECUTE 'DELETE FROM opening WHERE id=$1' USING opening_id;
  PERFORM geodb_pkg.del_post_delete_opening(opening_id, opening_rec.address_id, opening_rec.lod3_multi_surface_id, opening_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_opening (id: %): %', opening_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_opening(
  opening_id INTEGER,
  opening_address_id INTEGER,
  opening_lod3_multi_surface_id INTEGER, 
  opening_lod4_multi_surface_id INTEGER)
RETURNS SETOF void AS
$$
DECLARE
  address_rec INTEGER;
BEGIN 
  IF opening_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(opening_lod3_multi_surface_id);
  END IF;

  IF opening_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(opening_lod4_multi_surface_id);
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
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_opening (id: %): %', opening_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from BUILDING_INSTALLATION
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_building_inst(building_installation_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  building_installation_rec building_installation%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_installation WHERE id=$1' INTO building_installation_rec USING building_installation_id;

  EXECUTE 'DELETE FROM building_installation WHERE id=$1' USING building_installation_id;
  PERFORM geodb_pkg.del_post_delete_building_inst(building_installation_id, building_installation_rec.lod2_geometry_id, building_installation_rec.lod3_geometry_id, building_installation_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_building_installation (id: %): %', building_installation_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building_inst(
  building_installation_id INTEGER,
  bi_lod2_geometry_id INTEGER,
  bi_lod3_geometry_id INTEGER,
  bi_lod4_geometry_id INTEGER)
RETURNS SETOF void AS
$$
BEGIN
  IF bi_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(bi_lod2_geometry_id);
  END IF;

  IF bi_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(bi_lod3_geometry_id);
  END IF;

  IF bi_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(bi_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(building_installation_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_building_inst (id: %): %', building_installation_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from ROOM
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_room(r_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  ts_id INTEGER;
  bi_id INTEGER;
  bf_id INTEGER;
BEGIN
  FOR ts_id IN EXECUTE 'SELECT ts.id FROM thematic_surface ts WHERE ts.room_id=$1' USING r_id LOOP
    PERFORM geodb_pkg.del_delete_thematic_surface(ts_id);
  END LOOP;

  FOR bi_id IN EXECUTE 'SELECT bi.id FROM building_installation bi WHERE bi.room_id=$1' USING r_id LOOP
    PERFORM geodb_pkg.del_delete_building_inst(bi_id);
  END LOOP;

  FOR bf_id IN EXECUTE 'SELECT bf.id FROM building_furniture bf WHERE bf.room_id=$1' USING r_id LOOP
    PERFORM geodb_pkg.del_delete_building_furniture(bf_id);
  END LOOP;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_room (id: %): %', r_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_room(room_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  room_rec room%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM room WHERE id=$1' INTO room_rec USING room_id;

  PERFORM geodb_pkg.del_pre_delete_room(room_id);
  EXECUTE 'DELETE FROM room WHERE id=$1' USING room_id;
  PERFORM geodb_pkg.del_post_delete_room(room_id, room_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_room (id: %): %', room_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_room(
  room_id INTEGER,
  room_lod4_geometry_id INTEGER)
RETURNS SETOF void AS
$$
BEGIN
  IF room_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(room_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(room_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_room (id: %): %', room_id, SQLERRM;
END; 
$$
LANGUAGE plpgsql;


/*
delete from BUILDING_FURNITURE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_building_furniture(building_furniture_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  building_furniture_rec building_furniture%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_furniture WHERE id=$1' INTO building_furniture_rec USING building_furniture_id;

  EXECUTE 'DELETE FROM building_furniture WHERE id=$1' USING building_furniture_id;
  PERFORM geodb_pkg.del_post_delete_building_furniture(building_furniture_id, building_furniture_rec.lod4_geometry_id, building_furniture_rec.lod4_implicit_rep_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_building_furniture (id: %): %', building_furniture_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building_furniture(
  building_furniture_id INTEGER, 
  bf_lod4_geometry_id INTEGER, 
  bf_lod4_implicit_rep_id INTEGER) 
RETURNS SETOF void AS
$$
BEGIN
  IF bf_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(bf_rec.lod4_geometry_id);
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
delete from BUILDING
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_building(b_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  bp_id INTEGER;
  ts_id INTEGER;
  bi_id INTEGER;
  room_id INTEGER;
  ad_id INTEGER;
BEGIN
  FOR bp_id IN EXECUTE 'SELECT id FROM building WHERE id != $1 AND building_parent_id=$1' USING b_id LOOP
    PERFORM geodb_pkg.del_delete_building(bp_id);
  END LOOP;

  FOR ts_id IN EXECUTE 'SELECT id FROM thematic_surface WHERE building_id=$1' USING b_id LOOP
    PERFORM geodb_pkg.del_delete_thematic_surface(ts_id);
  END LOOP;

  FOR bi_id IN EXECUTE 'SELECT id FROM building_installation bi WHERE building_id=$1' USING b_id LOOP
    PERFORM geodb_pkg.del_delete_building_inst(bi_id);
  END LOOP;

  FOR room_id IN EXECUTE 'SELECT id FROM room WHERE building_id=$1' USING b_id LOOP
    PERFORM geodb_pkg.del_delete_room(room_id);
  END LOOP;

  -- delete addresses being not referenced from buildings any more
  FOR ad_id IN EXECUTE 'SELECT address_id FROM address_to_building WHERE building_id=$1' USING b_id LOOP
    IF geodb_pkg.del_is_not_referenced('address_to_building', 'address_id', ad_id, 'building_id', b_id) THEN
      PERFORM geodb_pkg.del_delete_address(ad_id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM address_to_building WHERE building_id=$1' USING b_id;
  
  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_building (id: %): %', b_id, SQLERRM;

END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_building(building_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  building_rec building%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building WHERE id=$1' INTO building_rec USING building_id;

  PERFORM geodb_pkg.del_pre_delete_building(building_id);
  EXECUTE 'DELETE FROM building WHERE id=$1' USING building_id;
  PERFORM geodb_pkg.del_post_delete_building(building_id, building_rec.lod1_geometry_id, building_rec.lod2_geometry_id, building_rec.lod3_geometry_id, building_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_building (id: %): %', building_id, SQLERRM;

END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building(
  building_id INTEGER,
  b_lod1_geometry_id INTEGER,
  b_lod2_geometry_id INTEGER,
  b_lod3_geometry_id INTEGER,
  b_lod4_geometry_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF b_lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(b_lod1_geometry_id);
  END IF; 

  IF b_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(b_lod2_geometry_id);
  END IF;

  IF b_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(b_lod3_geometry_id);
  END IF;

  IF b_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(b_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(building_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_building (id: %): %', building_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from ADDRESS
*/
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


/*
delete from CITY_FURNITURE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_city_furniture(city_furniture_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  city_furniture_rec city_furniture%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM city_furniture WHERE id=$1' INTO city_furniture_rec USING city_furniture_id;

  EXECUTE 'DELETE FROM city_furniture WHERE id=$1' USING city_furniture_id;
  PERFORM geodb_pkg.del_post_delete_city_furniture(city_furniture_id, city_furniture_rec.lod1_geometry_id, city_furniture_rec.lod2_geometry_id, city_furniture_rec.lod3_geometry_id, city_furniture_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_city_furniture (id: %): %', city_furniture_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_city_furniture(
  city_furniture_id INTEGER,
  cf_lod1_geometry_id INTEGER,
  cf_lod2_geometry_id INTEGER,
  cf_lod3_geometry_id INTEGER,
  cf_lod4_geometry_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF cf_lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(cf_lod1_geometry_id);
  END IF; 

  IF cf_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(cf_lod2_geometry_id);
  END IF;

  IF cf_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(cf_lod3_geometry_id);
  END IF;

  IF cf_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(cf_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(city_furniture_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_city_furniture (id: %): %', city_furniture_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from GENERIC_CITYOBJECT
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_generic_cityobject(generic_cityobject_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  generic_cityobject_rec generic_cityobject%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM generic_cityobject WHERE id=$1' INTO generic_cityobject_rec USING generic_cityobject_id;

  EXECUTE 'DELETE FROM generic_cityobject WHERE id=$1' USING generic_cityobject_id;
  PERFORM geodb_pkg.del_post_delete_generic_cityobject(generic_cityobject_id, generic_cityobject_rec.lod0_geometry_id, generic_cityobject_rec.lod1_geometry_id, generic_cityobject_rec.lod2_geometry_id, generic_cityobject_rec.lod3_geometry_id, generic_cityobject_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_generic_cityobject (id: %): %', generic_cityobject_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_generic_cityobject(
  generic_cityobject_id INTEGER,
  gco_lod0_geometry_id INTEGER,
  gco_lod1_geometry_id INTEGER,
  gco_lod2_geometry_id INTEGER,
  gco_lod3_geometry_id INTEGER,
  gco_lod4_geometry_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF gco_lod0_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(gco_lod0_geometry_id);
  END IF;

  IF gco_lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(gco_lod1_geometry_id);
  END IF; 

  IF gco_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(gco_lod2_geometry_id);
  END IF;

  IF gco_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(gco_lod3_geometry_id);
  END IF;

  IF gco_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(gco_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(generic_cityobject_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_generic_cityobject (id: %): %', generic_cityobject_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from LAND_USE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_land_use(land_use_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  land_use_rec land_use%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM land_use WHERE id=$1' INTO land_use_rec USING land_use_id;

  EXECUTE 'DELETE FROM land_use WHERE id=$1' USING land_use_id;
  PERFORM geodb_pkg.del_post_delete_land_use(land_use_id, land_use_rec.lod0_multi_surface_id, land_use_rec.lod1_multi_surface_id, land_use_rec.lod2_multi_surface_id, land_use_rec.lod3_multi_surface_id, land_use_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_land_use (id: %): %', land_use_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_land_use(
  land_use_id INTEGER,
  lu_lod0_multi_surface_id INTEGER,
  lu_lod1_multi_surface_id INTEGER,
  lu_lod2_multi_surface_id INTEGER,
  lu_lod3_multi_surface_id INTEGER,
  lu_lod4_multi_surface_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF lu_lod0_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(lu_lod0_multi_surface_id);
  END IF;

  IF lu_lod1_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(lu_lod1_multi_surface_id);
  END IF;

  IF lu_lod2_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(lu_lod2_multi_surface_id);
  END IF;

  IF lu_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(lu_lod3_multi_surface_id);
  END IF;

  IF lu_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(lu_lod4_multi_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(land_use_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_land_use (id: %): %', land_use_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from PLANT_COVER
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_plant_cover(plant_cover_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  plant_cover_rec plant_cover%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM plant_cover WHERE id=$1' INTO plant_cover_rec USING plant_cover_id;

  EXECUTE 'DELETE FROM plant_cover WHERE id=$1' USING plant_cover_id;
  PERFORM geodb_pkg.del_post_delete_plant_cover(plant_cover_id, plant_cover_rec.lod1_geometry_id, plant_cover_rec.lod2_geometry_id, plant_cover_rec.lod3_geometry_id, plant_cover_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_plant_cover (id: %): %', plant_cover_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_plant_cover(
  plant_cover_id INTEGER,
  pc_lod1_geometry_id INTEGER,
  pc_lod2_geometry_id INTEGER,
  pc_lod3_geometry_id INTEGER,
  pc_lod4_geometry_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF pc_lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(pc_lod1_geometry_id);
  END IF; 

  IF pc_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(pc_lod2_geometry_id);
  END IF;

  IF pc_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(pc_lod3_geometry_id);
  END IF;

  IF pc_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(pc_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(plant_cover_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_plant_cover (id: %): %', plant_cover_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from SOLITARY_VEGETAT_OBJECT
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_solitary_veg_obj(solitary_veg_obj_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  solitary_veg_obj_rec solitary_vegetat_object%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM solitary_vegetat_object WHERE id=$1' INTO solitary_veg_obj_rec USING solitary_veg_obj_id;

  EXECUTE 'DELETE FROM solitary_vegetat_object WHERE id=$1' USING solitary_veg_obj_rec.id;
  PERFORM geodb_pkg.del_post_delete_solitary_veg_obj(solitary_veg_obj_id, solitary_veg_obj_rec.lod1_geometry_id, solitary_veg_obj_rec.lod2_geometry_id, solitary_veg_obj_rec.lod3_geometry_id, solitary_veg_obj_rec.lod4_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_solitary_veg_obj (id: %): %', solitary_veg_obj_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_solitary_veg_obj(
  solitary_veg_obj_id INTEGER,
  svo_lod1_geometry_id INTEGER,
  svo_lod2_geometry_id INTEGER,
  svo_lod3_geometry_id INTEGER,
  svo_lod4_geometry_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF svo_lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(svo_lod1_geometry_id);
  END IF; 

  IF svo_lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(svo_lod2_geometry_id);
  END IF;

  IF svo_lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(svo_lod3_geometry_id);
  END IF;

  IF svo_lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(svo_lod4_geometry_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(solitary_veg_obj_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_solitary_veg_obj (id: %): %', solitary_veg_obj_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from TRAFFIC_AREA
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_traffic_area(traffic_area_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  traffic_area_rec traffic_area%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM traffic_area WHERE id=$1' INTO traffic_area_rec USING traffic_area_id;

  EXECUTE 'DELETE FROM traffic_area WHERE id=$1' USING traffic_area_rec.id;
  PERFORM geodb_pkg.del_post_delete_traffic_area(traffic_area_id, traffic_area_rec.lod2_multi_surface_id, traffic_area_rec.lod3_multi_surface_id, traffic_area_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_traffic_area (id: %): %', traffic_area_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_traffic_area(
  traffic_area_id INTEGER,
  ta_lod2_multi_surface_id INTEGER,
  ta_lod3_multi_surface_id INTEGER,
  ta_lod4_multi_surface_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF ta_lod2_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(ta_lod2_multi_surface_id);
  END IF;

  IF ta_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(ta_lod3_multi_surface_id);
  END IF;

  IF ta_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(ta_lod4_multi_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(traffic_area_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_traffic_area (id: %): %', traffic_area_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from TRANSPORTATION_COMPLEX
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_transport_complex(tc_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  ta_id INTEGER;
BEGIN
  FOR ta_id IN EXECUTE 'SELECT ta.id FROM traffic_area ta WHERE ta.transportation_complex_id=$1' USING tc_id LOOP
    PERFORM geodb_pkg.del_delete_traffic_area(ta_id);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_transport_complex (id: %): %', tc_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_transport_complex(transport_complex_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  transport_complex_rec transportation_complex%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM transportation_complex WHERE id=$1' INTO transport_complex_rec USING transport_complex_id;

  PERFORM geodb_pkg.del_pre_delete_transport_complex(transport_complex_id);
  EXECUTE 'DELETE FROM transportation_complex WHERE id=$1' USING transport_complex_id;
  PERFORM geodb_pkg.del_post_delete_transport_complex(transport_complex_id, transport_complex_rec.lod1_multi_surface_id, transport_complex_rec.lod2_multi_surface_id, transport_complex_rec.lod3_multi_surface_id, transport_complex_rec.lod4_multi_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_transport_complex (id: %): %', transport_complex_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_transport_complex(
  transport_complex_id INTEGER,
  tc_lod1_multi_surface_id INTEGER,
  tc_lod2_multi_surface_id INTEGER,
  ta_lod3_multi_surface_id INTEGER,
  ta_lod4_multi_surface_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF tc_lod1_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(tc_lod1_multi_surface_id);
  END IF; 

  IF tc_lod2_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(tc_lod2_multi_surface_id);
  END IF;

  IF tc_lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(tc_lod3_multi_surface_id);
  END IF;

  IF tc_lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(tc_lod4_multi_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(transport_complex_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_transport_complex (id: %): %', transport_complex_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from WATERBODY
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_waterbody(wb_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  wbs_id INTEGER;
BEGIN
  -- delete water boundary surface being not referenced from waterbodies any more
  FOR wbs_id IN EXECUTE 'SELECT waterboundary_surface_id FROM waterbod_to_waterbnd_srf WHERE waterbody_id=$1' USING wb_id LOOP
    IF geodb_pkg.del_is_not_referenced('waterbod_to_waterbnd_srf', 'waterboundary_surface_id', wbs_id, 'waterbody_id', wb_id) THEN
      PERFORM geodb_pkg.del_delete_waterbnd_surface(wbs_id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM waterbod_to_waterbnd_srf WHERE waterbody_id=$1' USING wb_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_waterbody (id: %): %', wb_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_waterbody(waterbody_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  waterbody_rec waterbody%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM waterbody WHERE id=$1' INTO waterbody_rec USING waterbody_id;

  PERFORM geodb_pkg.del_pre_delete_waterbody(waterbody_id);
  EXECUTE 'DELETE FROM waterbody WHERE id=$1' USING waterbody_id;
  PERFORM geodb_pkg.del_post_delete_waterbody(waterbody_id, waterbody_rec.lod1_solid_id, waterbody_rec.lod2_solid_id, waterbody_rec.lod3_solid_id, waterbody_rec.lod4_solid_id, waterbody_rec.lod0_multi_surface_id, waterbody_rec.lod1_multi_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_waterbody (id: %): %', waterbody_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_waterbody(
  waterbody_id INTEGER,
  wb_lod1_solid_id INTEGER,
  wb_lod2_solid_id INTEGER,
  wb_lod3_solid_id INTEGER,
  wb_lod4_solid_id INTEGER,
  wb_lod0_multi_surface_id INTEGER,
  wb_lod2_multi_surface_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF wb_lod1_solid_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wb_lod1_solid_id);
  END IF; 

  IF wb_lod2_solid_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wb_lod2_solid_id);
  END IF;

  IF wb_lod3_solid_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wb_lod3_solid_id);
  END IF;

  IF wb_lod4_solid_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wb_lod4_solid_id);
  END IF;

  IF wb_lod0_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wb_lod0_multi_surface_id);
  END IF;

  IF wb_lod1_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wb_lod1_multi_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(waterbody_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_waterbody (id: %): %', waterbody_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_waterbnd_surface(wbs_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM waterbod_to_waterbnd_srf WHERE waterboundary_surface_id=$1' USING wbs_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_waterbnd_surface (id: %): %', wbs_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_waterbnd_surface(waterbnd_surface_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  waterbnd_surface_rec waterboundary_surface%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM waterboundary_surface WHERE id=$1' INTO waterbnd_surface_rec USING waterbnd_surface_id;

  PERFORM geodb_pkg.del_pre_delete_waterbnd_surface(waterbnd_surface_id);
  EXECUTE 'DELETE FROM waterboundary_surface WHERE id=$1' USING waterbnd_surface_id;
  PERFORM geodb_pkg.del_post_delete_waterbnd_surface(waterbnd_surface_id, waterbnd_surface_rec.lod2_surface_id, waterbnd_surface_rec.lod3_surface_id, waterbnd_surface_rec.lod4_surface_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_waterbnd_surface (id: %): %', waterbnd_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_waterbnd_surface(
  waterbnd_surface_id INTEGER,
  wbs_lod2_surface_id INTEGER,
  wbs_lod3_surface_id INTEGER,
  wbs_lod4_surface_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF wbs_lod2_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wbs_lod2_surface_id);
  END IF;

  IF wbs_lod3_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wbs_lod3_surface_id);
  END IF;

  IF wbs_lod4_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(wbs_lod4_surface_id);
  END IF;

  PERFORM geodb_pkg.del_intern_delete_cityobject(waterbnd_surface_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_waterbnd_surface (id: %): %', waterbnd_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from RELIEF_FEATURE
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_relief_feature(rf_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  rc_id INTEGER;
BEGIN
  -- delete relief component being not referenced from relief fetaures any more
  FOR rc_id IN EXECUTE 'SELECT relief_component_id FROM relief_feat_to_rel_comp WHERE relief_feature_id=$1' USING rf_id LOOP
    IF geodb_pkg.del_is_not_referenced('relief_feat_to_rel_comp', 'relief_component_id', rc_id, 'relief_feature_id', rf_id) THEN
      PERFORM geodb_pkg.del_delete_relief_component(rc_id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM relief_feat_to_rel_comp WHERE relief_feature_id=$1' USING rf_id;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
	WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_relief_feature (id: %): %', rf_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_relief_feature(relief_feature_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_relief_feature(relief_feature_id);
  EXECUTE 'DELETE FROM relief_feature WHERE id=$1' USING relief_feature_id;
  PERFORM geodb_pkg.del_post_delete_relief_feature(relief_feature_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_relief_feature (id: %): %', relief_feature_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_relief_feature(relief_feature_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_intern_delete_cityobject(relief_feature_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_relief_feature (id: %): %', relief_feature_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from RELIEF_COMPONENT
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_relief_component(rc_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM relief_feat_to_rel_comp WHERE relief_component_id=$1' USING rc_id;

  PERFORM geodb_pkg.del_delete_tin_relief(rc_id);
  PERFORM geodb_pkg.del_delete_masspoint_relief(rc_id);
  PERFORM geodb_pkg.del_delete_breakline_relief(rc_id);
  PERFORM geodb_pkg.del_delete_raster_relief(rc_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_relief_component (id: %): %', rc_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_relief_component(relief_component_id INTEGER) RETURNS SETOF void AS
$$
BEGIN 
  PERFORM geodb_pkg.del_pre_delete_relief_component(relief_component_id);
  EXECUTE 'DELETE FROM relief_component WHERE id=$1' USING relief_component_id;
  PERFORM geodb_pkg.del_post_delete_relief_component(relief_component_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_relief_component (id: %): %', relief_component_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_relief_component(relief_component_id INTEGER) RETURNS SETOF void AS
$$
BEGIN 
  PERFORM geodb_pkg.del_intern_delete_cityobject(relief_component_rec.id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_relief_component (id: %): %', relief_component_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from TIN_RELIEF
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_tin_relief(tin_relief_id INTEGER) RETURNS SETOF void AS
$$
DECLARE
  tin_relief_rec tin_relief%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM tin_relief WHERE id=$1' INTO tin_relief_rec USING tin_relief_id;

  EXECUTE 'DELETE FROM tin_relief WHERE id=$1' USING tin_relief_id;
  PERFORM geodb_pkg.del_post_delete_tin_relief(tin_relief_id, tin_relief_rec.surface_geometry_id);

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_tin_relief (id: %): %', tin_relief_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_tin_relief(
  tin_relief_id INTEGER,
  tin_surface_geometry_id INTEGER
) RETURNS SETOF void AS
$$
BEGIN
  IF tin_surface_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_delete_surface_geometry(tin_surface_geometry_id);
  END IF;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_tin_relief (id: %): %', tin_relief_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from MASSPOINT_RELIEF
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_masspoint_relief(masspoint_relief_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM masspoint_relief WHERE id=$1' USING masspoint_relief_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_masspoint_relief (id: %): %', masspoint_relief_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from BREAKLINE_RELIEF
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_breakline_relief(breakline_relief_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM breakline_relief WHERE id=$1' USING breakline_relief_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_breakline_relief (id: %): %', breakline_relief_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
delete from RASTER_RELIEF

*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_raster_relief(raster_relief_id INTEGER) RETURNS SETOF void AS
$$
BEGIN
  -- 
  -- !!! Not yet implemented !!!
  -- 
  EXECUTE 'DELETE FROM raster_relief WHERE id=$1' USING raster_relief_id;

  EXCEPTION
	WHEN OTHERS THEN
      RAISE NOTICE 'delete_raster_relief (id: %): %', raster_relief_id, SQLERRM;
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
    PERFORM geodb_pkg.del_delete_surface_data(sd_global_id);
  END LOOP;

  -- delete appearances which does not have surface data any more
  IF only_global=1 THEN
    FOR app_global_id IN SELECT a.id FROM appearance a LEFT OUTER JOIN appear_to_surface_data asd
        ON a.id=asd.appearance_id WHERE a.cityobject_id IS NULL AND asd.appearance_id IS NULL LOOP
      PERFORM geodb_pkg.del_delete_appearance(app_global_id);
    END LOOP;
  ELSE
    FOR app_id IN SELECT a.id FROM appearance a LEFT OUTER JOIN appear_to_surface_data asd 
        ON a.id=asd.appearance_id WHERE asd.appearance_id IS NULL LOOP
      PERFORM geodb_pkg.del_delete_appearance(app_id);
    END LOOP;
  END IF;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
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
      ON g.id=gtc.cityobjectgroup_id WHERE gtc.cityobject_id IS NULL LOOP
    PERFORM geodb_pkg.del_delete_cityobjectgroup(cityobjectgroup_rec);
  END LOOP;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
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
      ON c.id=cm.citymodel_id WHERE cm.cityobject_id IS NULL LOOP
    PERFORM geodb_pkg.del_delete_citymodel(citymodel_rec);
  END LOOP;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
	WHEN OTHERS THEN
      RAISE NOTICE 'cleanup_citymodel: %', SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_cleanup_implicitgeometries() RETURNS SETOF void AS
$$
DECLARE
  implicit_geometry_rec INTEGER;
BEGIN
  FOR implicit_geometry_rec IN SELECT ig.id FROM implicit_geometry ig
	LEFT JOIN BUILDING_FURNITURE bf ON bf.LOD4_IMPLICIT_REP_ID = ig.id
	LEFT JOIN CITY_FURNITURE cf1 ON cf1.LOD1_IMPLICIT_REP_ID = ig.id
	LEFT JOIN CITY_FURNITURE cf2 ON cf2.LOD2_IMPLICIT_REP_ID = ig.id
	LEFT JOIN CITY_FURNITURE cf3 ON cf3.LOD3_IMPLICIT_REP_ID = ig.id
	LEFT JOIN CITY_FURNITURE cf4 ON cf4.LOD4_IMPLICIT_REP_ID = ig.id
	LEFT JOIN GENERIC_CITYOBJECT gco0 ON gco0.LOD0_IMPLICIT_REP_ID = ig.id
	LEFT JOIN GENERIC_CITYOBJECT gco1 ON gco1.LOD1_IMPLICIT_REP_ID = ig.id
	LEFT JOIN GENERIC_CITYOBJECT gco2 ON gco2.LOD2_IMPLICIT_REP_ID = ig.id
	LEFT JOIN GENERIC_CITYOBJECT gco3 ON gco3.LOD3_IMPLICIT_REP_ID = ig.id
	LEFT JOIN GENERIC_CITYOBJECT gco4 ON gco4.LOD4_IMPLICIT_REP_ID = ig.id
	LEFT JOIN SOLITARY_VEGETAT_OBJECT svo1 ON svo1.LOD1_IMPLICIT_REP_ID = ig.id
	LEFT JOIN SOLITARY_VEGETAT_OBJECT svo2 ON svo2.LOD2_IMPLICIT_REP_ID = ig.id
	LEFT JOIN SOLITARY_VEGETAT_OBJECT svo3 ON svo3.LOD3_IMPLICIT_REP_ID = ig.id
	LEFT JOIN SOLITARY_VEGETAT_OBJECT svo4 ON svo4.LOD4_IMPLICIT_REP_ID = ig.id
	WHERE (bf.LOD4_IMPLICIT_REP_ID IS NULL) AND
		  (cf1.LOD1_IMPLICIT_REP_ID IS NULL) AND
		  (cf2.LOD2_IMPLICIT_REP_ID IS NULL) AND
		  (cf3.LOD3_IMPLICIT_REP_ID IS NULL) AND
		  (cf4.LOD4_IMPLICIT_REP_ID IS NULL) AND
		  (gco0.LOD0_IMPLICIT_REP_ID IS NULL) AND
		  (gco1.LOD1_IMPLICIT_REP_ID IS NULL) AND
		  (gco2.LOD2_IMPLICIT_REP_ID IS NULL) AND
		  (gco3.LOD3_IMPLICIT_REP_ID IS NULL) AND
		  (gco4.LOD4_IMPLICIT_REP_ID IS NULL) AND
		  (svo1.LOD1_IMPLICIT_REP_ID IS NULL) AND
		  (svo2.LOD2_IMPLICIT_REP_ID IS NULL) AND
		  (svo3.LOD3_IMPLICIT_REP_ID IS NULL) AND
		  (svo4.LOD4_IMPLICIT_REP_ID IS NULL) LOOP

    PERFORM geodb_pkg.del_delete_implicit_geom(implicit_geometry_rec);
  END LOOP;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;   
    WHEN OTHERS THEN
      RAISE NOTICE 'cleanup_implicitgeometries: %', SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


-- generic function to delete any cityobject  
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_cityobject(pid INTEGER) RETURNS SETOF void AS
$$
DECLARE
  objectclass_id INTEGER;
BEGIN
  EXECUTE 'SELECT class_id FROM cityobject WHERE id=$1' INTO objectclass_id USING pid;

  CASE
    WHEN objectclass_id = 4 THEN PERFORM geodb_pkg.del_delete_land_use(pid);
    WHEN objectclass_id = 5 THEN PERFORM geodb_pkg.del_delete_generic_cityobject(pid);
    WHEN objectclass_id = 7 THEN PERFORM geodb_pkg.del_delete_solitary_veg_obj(pid);
    WHEN objectclass_id = 8 THEN PERFORM geodb_pkg.del_delete_plant_cover(pid);
    WHEN objectclass_id = 9 THEN PERFORM geodb_pkg.del_delete_waterbody(pid);
    WHEN objectclass_id = 11 OR 
	     objectclass_id = 12 OR 
	     objectclass_id = 13 THEN PERFORM geodb_pkg.del_delete_waterbnd_surface(pid);
    WHEN objectclass_id = 14 THEN PERFORM geodb_pkg.del_delete_relief_feature(pid);
    WHEN objectclass_id = 16 OR 
	     objectclass_id = 17 OR 
	     objectclass_id = 18 OR 
	     objectclass_id = 19 THEN PERFORM geodb_pkg.del_delete_relief_component(pid);
    WHEN objectclass_id = 21 THEN PERFORM geodb_pkg.del_delete_city_furniture(pid);
    WHEN objectclass_id = 23 THEN PERFORM geodb_pkg.del_delete_cityobjectgroup(pid);
    WHEN objectclass_id = 25 OR 
	     objectclass_id = 26 THEN PERFORM geodb_pkg.del_delete_building(pid);
    WHEN objectclass_id = 27 OR 
	     objectclass_id = 28 THEN PERFORM geodb_pkg.del_delete_cityobjectgroup(pid);
    WHEN objectclass_id = 30 OR 
	     objectclass_id = 31 OR 
	     objectclass_id = 32 OR 
	     objectclass_id = 33 OR 
	     objectclass_id = 34 OR 
	     objectclass_id = 35 OR 
	     objectclass_id = 36 THEN PERFORM geodb_pkg.del_delete_thematic_surface(pid);
    WHEN objectclass_id = 38 OR 
	     objectclass_id = 39 THEN PERFORM geodb_pkg.del_delete_opening(pid);
    WHEN objectclass_id = 40 THEN PERFORM geodb_pkg.del_delete_building_furniture(pid);
    WHEN objectclass_id = 41 THEN PERFORM geodb_pkg.del_delete_room(pid);
    WHEN objectclass_id = 43 OR 
	     objectclass_id = 44 OR 
	     objectclass_id = 45 OR 
	     objectclass_id = 46 THEN PERFORM geodb_pkg.del_delete_transport_complex(pid);
    WHEN objectclass_id = 47 OR 
	     objectclass_id = 48 THEN PERFORM geodb_pkg.del_delete_traffic_area(pid);
    WHEN objectclass_id = 57 THEN PERFORM geodb_pkg.del_delete_city_model(pid);
    ELSE
      -- do nothing
  END CASE;

  EXCEPTION
    WHEN no_data_found THEN
      RETURN;
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_cityobject (id: %): %', pid, SQLERRM;

END; 
$$ 
LANGUAGE plpgsql;