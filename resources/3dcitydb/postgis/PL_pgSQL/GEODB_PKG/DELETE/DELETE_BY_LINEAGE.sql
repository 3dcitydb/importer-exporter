-- DELETE_BY_LINEAGE.sql
--
-- Authors:     Claus Nagel <cnagel@virtualcitysystems.de>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--              György Hudra <hudra@moss.de>
--
-- Copyright:   (c) 2013       Faculty of Civil, Geo and Environmental Engineering, 
--                             Chair of Geoinformatics,
--                             Technische Universität München, Germany
--                             http://www.gis.bv.tum.de/
--              (c) 2007-2013  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 3.0.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- 
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.3.0     2013-08-08   extended to all thematic classes            GHud
--                                                                    FKun
-- 1.2.0     2012-02-22   minor changes                               CNag
-- 1.1.0     2011-02-11   moved to new DELETE functionality           CNag
-- 1.0.0     2008-09-10   release version                             ASta
--

CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_buildings(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  building_id INTEGER;
BEGIN
  FOR building_id IN EXECUTE 'WITH RECURSIVE complex_building(id, parent_id, level) AS (
      SELECT id, building_parent_id, 1 AS level FROM building WHERE building_parent_id IS NULL AND id IN
	    (SELECT b.id FROM building b, cityobject c WHERE b.id = c.id AND c.lineage = $1)
      UNION ALL
        SELECT b.id, b.building_parent_id, cb.level + 1 AS level FROM building b, complex_building cb WHERE b.building_parent_id = cb.id
    ) SELECT id FROM complex_building ORDER BY level DESC' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_building(building_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_buildings: deletion of building with ID % threw %', building_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_implicitgeometries();
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels();

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_buildings: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_city_furnitures(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  city_furniture_id INTEGER;
BEGIN
  FOR city_furniture_id IN EXECUTE 'SELECT cf.id FROM city_furniture cf, cityobject co 
                                      WHERE cf.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_city_furniture(city_furniture_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_city_furnitures: deletion of city_furniture with ID % threw %', city_furniture_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_implicitgeometries;
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'city_furnitures: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_generic_cityobjects(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  generic_cityobject_id INTEGER;
BEGIN
  FOR generic_cityobject_id IN EXECUTE 'SELECT gco.id FROM generic_cityobject gco, cityobject co 
                                          WHERE gco.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_generic_cityobject(generic_cityobject_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_generic_cityobjects: deletion of generic_cityobject with ID % threw %', generic_cityobject_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_implicitgeometries;
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_generic_cityobjects: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_land_uses(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  land_use_id INTEGER;
BEGIN
  FOR land_use_id IN EXECUTE 'SELECT lu.id FROM land_use lu, cityobject co 
                                WHERE lu.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_land_use(land_use_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_land_uses: deletion of land_use with ID % threw %', land_use_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_land_uses: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_plant_covers(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  plant_cover_id INTEGER;
BEGIN
  FOR plant_cover_id IN EXECUTE 'SELECT pc.id FROM plant_cover pc, cityobject co 
                                      WHERE pc.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_plant_cover(plant_cover_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_plant_covers: deletion of plant_cover with ID % threw %', plant_cover_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_plant_covers: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_soltary_veg_objs(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  soltary_veg_obj_id INTEGER;
BEGIN
  FOR soltary_veg_obj_id IN EXECUTE 'SELECT svo.id FROM soltary_vegegetat_object svo, cityobject co 
                                      WHERE svo.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_soltary_veg_obj(soltary_veg_obj_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_soltary_veg_objs: deletion of soltary_vegetation_object with ID % threw %', soltary_veg_obj_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_implicitgeometries;
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_soltary_veg_objs: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_transport_complexes(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  transport_complex_id INTEGER;
BEGIN
  FOR transport_complex_id IN EXECUTE 'SELECT tc.id FROM transportation_complex tc, cityobject co 
                                         WHERE tc.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_transport_complex(transport_complex_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_transport_complexes: deletion of transportation_complexe with ID % threw %', transport_complex_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_transport_complexes: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_waterbodies(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  waterbody_id INTEGER;
BEGIN
  FOR waterbody_id IN EXECUTE 'SELECT wb.id FROM waterbody wb, cityobject co  
                                 WHERE wb.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_waterbody(waterbody_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_city_waterbodies: deletion of waterbody with ID % threw %', waterbody_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_waterbodies: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_cityobjectgroups(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  cityobjectgroup_id INTEGER;
BEGIN
  FOR cityobjectgroup_id IN EXECUTE 'SELECT cog.id FROM cityobjectgroup cog, cityobject co 
                                      WHERE cog.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_cityobjectgroup(cityobjectgroup_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_cityobjectgroups: deletion of cityobjectgroup with ID % threw %', cityobjectgroup_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_cityobjectgroups: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_relief_features(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  relief_feature_id INTEGER;
BEGIN
  FOR relief_feature_id IN EXECUTE 'SELECT rf.id FROM relief_feature rf, cityobject co 
                                      WHERE rf.id = co.id AND co.lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_relief_feature(relief_feature_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_relief_features: deletion of relief_feature with ID % threw %', relief_feature_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_relief_features: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_cityobjects(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  cityobject_id INTEGER;
BEGIN
  FOR cityobject_id IN EXECUTE 'SELECT id FROM cityobject WHERE lineage = $1' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_cityobject(cityobject_id);

      EXCEPTION
        WHEN OTHERS THEN
          RAISE NOTICE 'delete_cityobjects: deletion of cityobject with ID % threw %', cityobject_id, SQLERRM;
    END;
  END LOOP;

  -- cleanup
  PERFORM geodb_pkg.del_cleanup_implicitgeometries;
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_citymodels;

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_cityobjects: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;