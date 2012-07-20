-- DELETE.sql
--
-- Authors:     Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Conversion:  Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
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
-- 1.1.0     2012-05-30   PostGIS Version                             FKun	
-- 1.1.0     2012-02-22   some performance improvements     CNag
-- 1.0.0     2011-02-11   release version                   CNag
--

-------------------------------------------------------------------------------
-- Conversion-Report:
-- Challenges in porting Oracle PL/SQL-scripts:
-- In PostgreSQL it is not possible to pass record-types as parameters for functions.
-- Mostly only the id-parameter of the ROWTYPE is needed, so parameters had been changed
-- to NUMERIC and the affected functions were renamed like this: ...delete_row_...
-- to avoid function-duplicity.
--
-- In the FUNCTION intern_delete_surface_geometry the structure used for hierarchical sql
-- was higly Oracle-specific. PostgreSQL uses a WITH query (often refered as CTEs -
-- Common Table Expressions) which is part of the ANSI SQL Standard.
-------------------------------------------------------------------------------

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
  OPEN ref_cur FOR EXECUTE 'SELECT 1 from ' || table_name || ' WHERE ' || check_column || '=$1 and not ' || not_column || '=$2' USING check_id, not_id;

  LOOP 
    FETCH ref_cur into dummy;
    IF NOT FOUND THEN
      is_not_referenced := false;
      EXIT;
    END IF;
  END LOOP;

  CLOSE ref_cur;

  RETURN is_not_referenced;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM SURFACE_GEOMETRY
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_intern_delete_surface_geometry(pid NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM textureparam WHERE surface_geometry_id IN (SELECT id FROM 
            (WITH RECURSIVE recursive_query(id, parent_id, level) 
              AS (
                SELECT id, parent_id, 1 AS level FROM surface_geometry WHERE id=$1
              UNION ALL
                SELECT sg.id, sg.parent_id, rq.level + 1 AS level FROM surface_geometry sg, recursive_query rq WHERE sg.parent_id = rq.id
              )
              SELECT id FROM recursive_query ORDER BY level DESC) AS cte)' USING pid; 
  
  EXECUTE 'DELETE FROM surface_geometry WHERE id IN (SELECT id FROM 
            (WITH RECURSIVE recursive_query(id, parent_id, level) 
              AS (
                SELECT id, parent_id, 1 AS level FROM surface_geometry WHERE id=$1
              UNION ALL
                SELECT sg.id, sg.parent_id, rq.level + 1 AS level FROM surface_geometry sg, recursive_query rq WHERE sg.parent_id = rq.id
              )
              SELECT id FROM recursive_query ORDER BY level DESC) AS cte)' USING pid; 			  
			 
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
  implicit_geometry_rec implicit_geometry%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM implicit_geometry WHERE id=$1' INTO implicit_geometry_rec USING pid;
  EXECUTE 'DELETE FROM implicit_geometry WHERE id=$1' USING pid;
  
  PERFORM geodb_pkg.del_post_delete_implicit_geom(implicit_geometry_rec.relative_geometry_id);

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
  appearance_cur CURSOR FOR
    SELECT * FROM appearance WHERE cityobject_id=pid;      
BEGIN   
  EXECUTE 'DELETE FROM cityobject_member WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM group_to_cityobject WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM generalization WHERE generalizes_to_id=$1' USING pid;
  EXECUTE 'DELETE FROM generalization WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM external_reference WHERE cityobject_id=$1' USING pid;
  EXECUTE 'DELETE FROM cityobject_genericattrib WHERE cityobject_id=$1' USING pid;
  EXECUTE 'UPDATE cityobjectgroup SET parent_cityobject_id=null WHERE parent_cityobject_id=$1' USING pid;

  FOR rec IN appearance_cur LOOP
    PERFORM geodb_pkg.del_delete_row_appearance(rec.id);
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
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_citymodel(citymodel_rec_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  appearance_cur CURSOR FOR
    SELECT * FROM appearance WHERE cityobject_id=citymodel_rec_id;
BEGIN
-- TODO
-- delete contained cityobjects!
  EXECUTE 'DELETE FROM cityobject_member WHERE citymodel_id=$1' USING citymodel_rec_id;

  FOR rec IN appearance_cur LOOP
    PERFORM geodb_pkg.del_delete_row_appearance(rec.id);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_citymodel (id: %): %', citymodel_rec_id, SQLERRM;
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
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_appearance(appearance_rec_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  surface_data_cur CURSOR FOR
    SELECT s.* from surface_data s, appear_to_surface_data ats
      WHERE s.id=ats.surface_data_id and ats.appearance_id=appearance_rec_id;
BEGIN
-- delete surface data not being referenced by appearances any more
  FOR rec IN surface_data_cur LOOP
    IF is_not_referenced('appear_to_surface_data', 'surface_data_id', rec.id, 'appearance_id', appearance_rec_id) THEN
      PERFORM geodb_pkg.del_delete_row_surface_data(rec.id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM appear_to_surface_data WHERE appearance_id=$1' USING appearance_rec_id;
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_appearance (id: %): %', appearance_rec_id, SQLERRM;
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
BEGIN
  PERFORM geodb_pkg.del_pre_delete_cityobjectgroup(cityobjectgroup_id);
  EXECUTE 'DELETE FROM cityobjectgroup WHERE id=$1' USING cityobjectgroup_id;
  PERFORM geodb_pkg.del_post_delete_cityobjectgroup(cityobjectgroup_id);      

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_cityobjectgroup (id: %): %', cityobjectgroup_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_cityobjectgroup(cityobjectgroup_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  cityobjectgroup_rec cityobjectgroup%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM cityobjectgroup WHERE id=$1' INTO cityobjectgroup_rec USING cityobjectgroup_id;
  
  IF cityobjectgroup_rec.surface_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(cityobjectgroup_rec.surface_geometry_id);
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
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_thematic_surface(thematic_surface_rec_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  opening_cur CURSOR FOR
    SELECT o.* FROM	opening o, opening_to_them_surface otm 
	  WHERE o.id=otm.opening_id AND otm.thematic_surface_id=thematic_surface_rec_id;
BEGIN
-- delete openings not being referenced by a thematic surface any more
  FOR rec IN opening_cur LOOP
    IF is_not_referenced('opening_to_them_surface', 'opening_id', rec.id, 'thematic_surface_id', thematic_surface_rec_id) THEN
      PERFORM geodb_pkg.del_delete_row_opening(rec.id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM opening_to_them_surface WHERE thematic_surface_id=$1' USING thematic_surface_rec_id;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_thematic_surface (id: %): %', thematic_surface_rec_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_thematic_surface(thematic_surface_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_thematic_surface(thematic_surface_id);
  EXECUTE 'DELETE FROM thematic_surface WHERE id=$1' USING thematic_surface_id;
  PERFORM geodb_pkg.del_post_delete_thematic_surface(thematic_surface_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_thematic_surface (id: %): %', thematic_surface_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_thematic_surface(thematic_surface_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  thematic_surface_rec thematic_surface%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM thematic_surface WHERE id=$1' INTO thematic_surface_rec USING thematic_surface_id;
  
  IF thematic_surface_rec.lod2_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(thematic_surface_rec.lod2_multi_surface_id);
  END IF;

  IF thematic_surface_rec.lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(thematic_surface_rec.lod3_multi_surface_id);
  END IF;

  IF thematic_surface_rec.lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(thematic_surface_rec.lod4_multi_surface_id);
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


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_opening(opening_rec_id NUMERIC, opening_rec_address_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_opening(opening_rec_id);
  EXECUTE 'DELETE FROM opening WHERE id=$1' USING opening_rec_id;
  PERFORM geodb_pkg.del_post_delete_opening(opening_rec_address_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_opening (id: %): %', opening_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_opening(opening_rec_address_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  opening_rec opening%ROWTYPE;
  address_cur CURSOR FOR
    SELECT a.id FROM address a LEFT OUTER JOIN address_to_building ab
      ON a.id=ab.address_id WHERE a.id=opening_rec_address_id AND ab.address_id IS NULL;
BEGIN
  EXECUTE 'SELECT * FROM opening WHERE id=$1' INTO opening_rec USING opening_rec_address_id;
  
  IF opening_rec.lod3_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(opening_rec.lod3_multi_surface_id);
  END IF;

  IF opening_rec.lod4_multi_surface_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(opening_rec.lod4_multi_surface_id);
  END IF;

-- delete addresses not being referenced from buildings and openings any more
  FOR rec IN address_cur LOOP
    IF is_not_referenced('opening', 'address_id', rec.id, 'id', opening_rec.id) THEN
      PERFORM geodb_pkg.del_delete_address(rec.id);
    END IF;   
  END LOOP;

  PERFORM geodb_pkg.del_intern_delete_cityobject(opening_rec.id);
 
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'post_delete_opening (id: %): %', opening_rec.id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


/*
internal: DELETE FROM BUILDING_INSTALLATION
*/
CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_building_inst(building_installation_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  EXECUTE 'DELETE FROM building_installation WHERE id=$1' USING building_installation_id;
  PERFORM geodb_pkg.del_post_delete_building_inst(building_installation_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_building_installation (id: %): %', building_installation_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building_inst(building_installation_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_installation_rec building_installation%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_installation WHERE id=$1' INTO building_installation_rec USING building_installation_id;

  IF building_installation_rec.lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_installation_rec.lod2_geometry_id);
  END IF;

  IF building_installation_rec.lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_installation_rec.lod3_geometry_id);
  END IF;

  IF building_installation_rec.lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_installation_rec.lod4_geometry_id);
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
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_room(room_rec_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  thematic_surface_cur CURSOR FOR
    SELECT * FROM thematic_surface WHERE room_id=room_rec_id;
  
  building_installation_cur CURSOR FOR
    SELECT * FROM building_installation WHERE room_id=room_rec_id;
  
  building_furniture_cur CURSOR FOR
    SELECT * FROM building_furniture WHERE room_id=room_rec_id;
BEGIN
  FOR rec IN thematic_surface_cur LOOP
    PERFORM geodb_pkg.del_delete_row_thematic_surface(rec.id);
  END LOOP;

  FOR rec IN building_installation_cur LOOP
    PERFORM geodb_pkg.del_delete_row_building_inst(rec.id);
  END LOOP;
  
  FOR rec IN building_furniture_cur LOOP
    PERFORM geodb_pkg.del_delete_row_building_furniture(rec.id);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_room (id: %): %', room_rec_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_room(room_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_room(room_id);
  EXECUTE 'DELETE FROM room WHERE id=$1' USING room_id;
  PERFORM geodb_pkg.del_post_delete_room(room_id);
  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_room (id: %): %', room_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_room(room_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  room_rec room%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM room WHERE id=$1' INTO room_rec USING room_id;

  IF room_rec.lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(room_rec.lod4_geometry_id);
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
BEGIN
  EXECUTE 'DELETE FROM building_furniture WHERE id=$1' USING building_furniture_id;
  PERFORM geodb_pkg.del_post_delete_building_furniture(building_furniture_id);

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_building_furniture (id: %): %', building_furniture_id, SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building_furniture(building_furniture_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_furniture_rec building_furniture%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_furniture WHERE id=$1' INTO building_furniture_rec USING building_furniture_id;

  IF building_furniture_rec.lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_furniture_rec.lod4_geometry_id);
  END IF;

  IF building_furniture_rec.lod4_implicit_rep_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_implicit_geom(building_furniture_rec.lod4_implicit_rep_id);
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
CREATE OR REPLACE FUNCTION geodb_pkg.del_pre_delete_building(building_rec_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE   
  building_part_cur CURSOR FOR
    SELECT * FROM building WHERE id!=building_rec_id AND building_parent_id=building_rec_id;

  thematic_surface_cur CURSOR FOR
    SELECT * FROM thematic_surface WHERE building_id=building_rec_id;

  building_installation_cur CURSOR FOR
    SELECT * FROM building_installation WHERE building_id=building_rec_id;
  
  room_cur CURSOR FOR
    SELECT * FROM room WHERE building_id=building_rec_id;

  address_cur CURSOR FOR
    SELECT address_id FROM address_to_building WHERE building_id=building_rec_id;
BEGIN
  FOR rec IN building_part_cur LOOP
    PERFORM geodb_pkg.del_delete_row_building(rec.id);
  END LOOP;

  FOR rec IN thematic_surface_cur LOOP
    PERFORM geodb_pkg.del_delete_row_thematic_surface(rec.id);
  END LOOP;

  FOR rec IN building_installation_cur LOOP
    PERFORM geodb_pkg.del_delete_row_building_inst(rec.id);
  END LOOP;

  FOR rec IN room_cur LOOP
    PERFORM geodb_pkg.del_delete_row_room(rec.id);
  END LOOP;
	
-- delete addresses being not referenced from buildings any more
  FOR rec IN address_cur LOOP
    IF is_not_referenced('address_to_building', 'address_id', rec.address_id, 'building_id', building_rec_id) THEN
      PERFORM geodb_pkg.del_delete_address(rec.address_id);
    END IF;
  END LOOP;

  EXECUTE 'DELETE FROM address_to_building WHERE building_id=$1' USING building_rec_id;

  
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'pre_delete_building (id: %): %', building_rec_id, SQLERRM;
	  
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_delete_row_building(building_id NUMERIC) RETURNS SETOF void AS
$$
BEGIN
  PERFORM geodb_pkg.del_pre_delete_building(building_id);
  EXECUTE 'DELETE FROM building WHERE id=$1' USING building_id;
  PERFORM geodb_pkg.del_post_delete_building(building_id);

  /*
  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_row_building (id: %): %', building_id, SQLERRM;
	  */
END; 
$$ 
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_post_delete_building(building_id NUMERIC) RETURNS SETOF void AS
$$
DECLARE
  building_rec building%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building WHERE id=$1' INTO building_rec USING building_id;

  IF building_rec.lod1_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_rec.lod1_geometry_id);
  END IF; 

  IF building_rec.lod2_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_rec.lod2_geometry_id);
  END IF;

  IF building_rec.lod3_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_rec.lod3_geometry_id);
  END IF;

  IF building_rec.lod4_geometry_id IS NOT NULL THEN
    PERFORM geodb_pkg.del_intern_delete_surface_geometry(building_rec.lod4_geometry_id);
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

  IF clean_apps != 0 THEN
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
  citymodel_rec citymodel%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM citymodel WHERE id=$1'
    INTO citymodel_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_citymodel(citymodel_rec.id);

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
  appearance_rec appearance%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM appearance WHERE id=$1'
    INTO appearance_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_appearance(appearance_rec.id);

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
  surface_data_rec surface_data%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM surface_data WHERE id=$1'
    INTO surface_data_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_surface_data(surface_data_rec.id);

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
  cityobjectgroup_rec cityobjectgroup%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM cityobjectgroup WHERE id=$1'
    INTO cityobjectgroup_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_cityobjectgroup(cityobjectgroup_rec.id);

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
  thematic_surface_rec thematic_surface%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM thematic_surface WHERE id=$1'
    INTO thematic_surface_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_thematic_surface(thematic_surface_rec.id);

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
  opening_rec opening%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM opening WHERE id=$1'
    INTO opening_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_opening(opening_rec.id, opening_rec.address_id);

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
  building_installation_rec building_installation%ROWTYPE;
BEGIN
  EXECUTE 'SELECT * FROM building_installation WHERE id=$1'
    INTO building_installation_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_building_inst(building_installation_rec.id);

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
  room_rec room%ROWTYPE;    
BEGIN
  EXECUTE 'SELECT * FROM room WHERE id=$1'
    INTO room_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_room(room_rec.id);

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
  building_furniture_rec building_furniture%ROWTYPE;    
BEGIN
  EXECUTE 'SELECT * FROM building_furniture WHERE id=$1'
    INTO building_furniture_rec USING pid;

  PERFORM geodb_pkg.del_delete_row_building_furniture(building_furniture_rec.id);
  
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
  building_rec_id NUMERIC;    
BEGIN
  EXECUTE 'SELECT * FROM building WHERE id=$1'
    INTO building_rec_id USING pid;

  PERFORM geodb_pkg.del_delete_row_building(building_rec_id);

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
  surface_data_global_cur CURSOR FOR
    SELECT s.* FROM surface_data s LEFT OUTER JOIN textureparam t
      ON s.id=t.surface_data_id WHERE t.surface_data_id IS NULL;

  appearance_cur CURSOR FOR
    SELECT a.* FROM appearance a LEFT OUTER JOIN appear_to_surface_data asd
      ON a.id=asd.appearance_id WHERE asd.appearance_id IS NULL;
	
  appearance_global_cur CURSOR FOR
    SELECT a.* FROM appearance a LEFT OUTER JOIN appear_to_surface_data asd
      ON a.id=asd.appearance_id WHERE a.cityobject_id IS NULL and asd.appearance_id IS NULL;
BEGIN
-- global appearances are not related to a cityobject.
-- however, we assume that all surface geometries of a cityobject
-- have been deleted at this stage. thus, we can check and delete
-- surface data which does not have a valid texture parameterization
-- any more.
  FOR rec IN surface_data_global_cur LOOP
    PERFORM geodb_pkg.del_delete_row_surface_data(rec.id);
  END LOOP;

-- delete appearances which does not have surface data any more
  IF only_global=1 THEN
    FOR rec IN appearance_global_cur LOOP
      PERFORM geodb_pkg.del_delete_row_appearance(rec.id);
    END LOOP;
  ELSE
    FOR rec IN appearance_cur LOOP
      PERFORM geodb_pkg.del_delete_row_appearance(rec.id);
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
  group_cur CURSOR FOR
    SELECT g.* FROM cityobjectgroup g LEFT OUTER JOIN group_to_cityobject gtc
      ON g.id=gtc.cityobject_id WHERE gtc.cityobject_id IS NULL;
BEGIN
  FOR rec IN group_cur LOOP
    PERFORM geodb_pkg.del_delete_row_cityobjectgroup(rec.id);
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
  citymodel_cur CURSOR FOR
    SELECT c.* FROM citymodel c LEFT OUTER JOIN cityobject_member cm
      ON c.id=cm.citymodel_id WHERE cm.citymodel_id IS NULL;
BEGIN
  FOR rec IN citymodel_cur LOOP
    PERFORM geodb_pkg.del_delete_row_citymodel(rec.id);
  END LOOP;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'cleanup_citymodel: %', SQLERRM;
END; 
$$ 
LANGUAGE plpgsql;
