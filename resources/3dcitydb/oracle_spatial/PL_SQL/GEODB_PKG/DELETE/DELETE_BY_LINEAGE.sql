-- DELETE_BY_LINEAGE.sql
--
-- Authors:     Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description                               | Author
-- 1.1.0     2011-02-11   moved to new DELETE functionality           CNag
-- 1.0.0     2008-09-10   release version                             ASta
--

CREATE OR REPLACE PACKAGE geodb_delete_by_lineage
AS
  procedure delete_buildings(lineage_value varchar2);
END geodb_delete_by_lineage;
/

CREATE OR REPLACE PACKAGE BODY GEODB_DELETE_BY_LINEAGE
AS 

  procedure delete_buildings(lineage_value varchar2)
  is
    cursor building_cur is
      select b.id from building b, cityobject c where b.id = c.id and c.lineage = lineage_value;
  begin    
    for building_rec in building_cur loop
      begin
        geodb_delete.delete_building(building_rec.id);
      exception
        when others then null;
      end;
    end loop;
  exception
    when others then
      dbms_output.put_line('delete_buildings: ' || SQLERRM);
  end;

END geodb_delete_by_lineage;
/