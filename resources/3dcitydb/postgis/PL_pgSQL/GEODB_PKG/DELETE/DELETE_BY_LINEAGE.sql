-- DELETE_BY_LINEAGE.sql
--
-- Authors:     Claus Nagel <claus.nagel@tu-berlin.de>
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
-- 
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                          | Author  | Conversion
-- 1.3.0     2013-02-22   PostGIS Version                                  FKun	
-- 1.2.0     2012-02-22   minor changes                          CNag
-- 1.1.0     2011-02-11   moved to new DELETE functionality      CNag
-- 1.0.0     2008-09-10   release version                        ASta
--

CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_buildings(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  building_rec INTEGER;
BEGIN
  FOR building_rec IN EXECUTE 'WITH RECURSIVE complex_building(id, parent_id, level) AS (
      SELECT id, building_parent_id, 1 AS level FROM building WHERE building_parent_id IS NULL AND id IN
	    (SELECT b.id FROM building b, cityobject c WHERE b.id = c.id AND c.lineage = $1)
      UNION ALL
        SELECT b.id, b.building_parent_id, cb.level + 1 AS level FROM building b, complex_building cb WHERE b.building_parent_id = cb.id
    ) SELECT id FROM complex_building ORDER BY level DESC' USING lineage_value LOOP
    BEGIN
      PERFORM geodb_pkg.del_delete_building(building_rec);

    EXCEPTION
      WHEN OTHERS THEN
        RAISE NOTICE 'delete_buildings: deletion of building with ID % threw %', building_rec, SQLERRM;
    END;
  END LOOP;
    
  -- cleanup
  PERFORM geodb_pkg.del_cleanup_appearances(1);
  PERFORM geodb_pkg.del_cleanup_cityobjectgroups();
  PERFORM geodb_pkg.del_cleanup_citymodels();

  EXCEPTION
    WHEN OTHERS THEN
      RAISE NOTICE 'delete_buildings: %', SQLERRM;
END;
$$
LANGUAGE plpgsql;