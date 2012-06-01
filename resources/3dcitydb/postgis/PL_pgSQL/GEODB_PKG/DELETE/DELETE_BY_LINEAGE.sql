-- DELETE_BY_LINEAGE.sql
--
-- Authors:     Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Conversion:  Felix Kunde <felix-kunde@gmx.de>
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
-- 
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                          | Author  | Conversion
-- 1.2.0     2012-05-30   PostGIS Version                                  FKun	
-- 1.2.0     2012-02-22   minor changes                          CNag
-- 1.1.0     2011-02-11   moved to new DELETE functionality      CNag
-- 1.0.0     2008-09-10   release version                        ASta
--

CREATE OR REPLACE FUNCTION geodb_pkg.del_by_lin_delete_buildings(lineage_value VARCHAR) RETURNS SETOF void AS
$$
DECLARE
  building_cur CURSOR FOR
    SELECT b.id FROM building b, cityobject c WHERE b.id = c.id AND c.lineage = lineage_value;
BEGIN    
  FOR building_rec IN building_cur LOOP
    BEGIN  
      PERFORM geodb_pkg.del_delete_building(building_rec.id);
      
    EXCEPTION
      WHEN OTHERS THEN
        RAISE NOTICE 'delete_buildings: deletion of building with ID % threw %', building_rec.id, SQLERRM;
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