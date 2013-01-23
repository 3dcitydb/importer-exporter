-- DELETE.sql
--
-- Authors:     Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
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
-- 1.1.0     2012-02-22   some performance improvements               CNag
-- 1.0.0     2011-02-11   release version                             CNag
--

CREATE OR REPLACE PACKAGE geodb_delete
AS
  procedure delete_surface_geometry(pid number, clean_apps int := 0);
  procedure delete_implicit_geometry(pid number);
  procedure delete_external_reference(pid number);
  procedure delete_citymodel(pid number);
  procedure delete_appearance(pid number);
  procedure delete_surface_data(pid number);
  procedure delete_cityobjectgroup(pid number);
  procedure delete_thematic_surface(pid number);
  procedure delete_opening(pid number);
  procedure delete_address(pid number);
  procedure delete_building_installation(pid number);
  procedure delete_room(pid number);
  procedure delete_building_furniture(pid number);
  procedure delete_building(pid number);
  procedure cleanup_appearances(only_global int :=1);
  procedure cleanup_cityobjectgroups;
  procedure cleanup_citymodels;
END geodb_delete;
/

CREATE OR REPLACE PACKAGE BODY geodb_delete
AS
  -- private procedures
  procedure intern_delete_surface_geometry(pid number);
  procedure intern_delete_implicit_geom(pid number);
  procedure intern_delete_cityobject(pid number);
  procedure delete_citymodel(citymodel_rec citymodel%rowtype);
  procedure delete_appearance(appearance_rec appearance%rowtype);
  procedure delete_surface_data(surface_data_rec surface_data%rowtype);
  procedure delete_cityobjectgroup(cityobjectgroup_rec cityobjectgroup%rowtype);
  procedure delete_thematic_surface(thematic_surface_rec thematic_surface%rowtype);
  procedure delete_opening(opening_rec opening%rowtype);
  procedure delete_building_installation(building_installation_rec building_installation%rowtype);
  procedure delete_room(room_rec room%rowtype);
  procedure delete_building_furniture(building_furniture_rec building_furniture%rowtype);
  procedure delete_building(building_rec building%rowtype);

  procedure post_delete_implicit_geom(implicit_geometry_rec implicit_geometry%rowtype);
  procedure pre_delete_cityobject(pid number);
  procedure pre_delete_citymodel(citymodel_rec citymodel%rowtype);
  procedure pre_delete_appearance(appearance_rec appearance%rowtype);
  procedure pre_delete_surface_data(surface_data_rec surface_data%rowtype);
  procedure pre_delete_cityobjectgroup(cityobjectgroup_rec cityobjectgroup%rowtype);
  procedure post_delete_cityobjectgroup(cityobjectgroup_rec cityobjectgroup%rowtype);
  procedure pre_delete_thematic_surface(thematic_surface_rec thematic_surface%rowtype);
  procedure post_delete_thematic_surface(thematic_surface_rec thematic_surface%rowtype);
  procedure pre_delete_opening(opening_rec opening%rowtype);
  procedure post_delete_opening(opening_rec opening%rowtype);
  procedure post_delete_building_inst(building_installation_rec building_installation%rowtype);
  procedure pre_delete_room(room_rec room%rowtype);
  procedure post_delete_room(room_rec room%rowtype);
  procedure post_delete_building_furniture(building_furniture_rec building_furniture%rowtype);
  procedure pre_delete_building(building_rec building%rowtype);
  procedure post_delete_building(building_rec building%rowtype);

  function is_not_referenced(table_name varchar2, check_column varchar2, check_id number, not_column varchar2, not_id number) return boolean;
  type ref_cursor is ref cursor;
  
  /*
    internal helpers
  */
  function is_not_referenced(table_name varchar2, check_column varchar2, check_id number, not_column varchar2, not_id number) return boolean
  is
    ref_cur ref_cursor;
    dummy number;
    is_not_referenced boolean;
  begin
    open ref_cur for 'select 1 from ' || table_name || ' where ' || check_column || '=:1 and not ' || not_column || '=:2' using check_id, not_id;
    loop 
      fetch ref_cur into dummy;
      is_not_referenced := ref_cur%notfound;
      exit;
    end loop;
    close ref_cur;
    
    return is_not_referenced;
  end;
  
  /*
    internal: delete from SURFACE_GEOMETRY
  */
  procedure intern_delete_surface_geometry(pid number)
  is
  begin
    execute immediate 'delete from textureparam where surface_geometry_id in (select id from (select id from surface_geometry start with id=:1 connect by prior id=parent_id order by level desc))' using pid;
    execute immediate 'delete from surface_geometry where id in (select id from (select id from surface_geometry start with id=:1 connect by prior id=parent_id order by level desc))' using pid; 
  exception
    when others then
      dbms_output.put_line('intern_delete_surface_geometry (id: ' || pid || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from IMPLICIT_GEOMETRY
  */
  procedure intern_delete_implicit_geom(pid number)
  is
    implicit_geometry_rec implicit_geometry%rowtype;
  begin
    execute immediate 'select * from implicit_geometry where id=:1'
      into implicit_geometry_rec
      using pid;
  
    execute immediate 'delete from implicit_geometry where id=:1' using pid;
    post_delete_implicit_geom(implicit_geometry_rec);
  exception
    when others then
      dbms_output.put_line('intern_delete_implicit_geom (id: ' || pid || '): ' || SQLERRM);
  end; 
  
  procedure post_delete_implicit_geom(implicit_geometry_rec implicit_geometry%rowtype)
  is
  begin
    if implicit_geometry_rec.relative_geometry_id is not null then
      intern_delete_surface_geometry(implicit_geometry_rec.relative_geometry_id);
    end if;
  exception
    when others then
      dbms_output.put_line('post_delete_implicit_geom (id: ' || implicit_geometry_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from CITY_OBJECT
  */
  procedure pre_delete_cityobject(pid number)
  is
    cursor appearance_cur is
      select * from appearance where cityobject_id=pid;      
  begin   
    execute immediate 'delete from cityobject_member where cityobject_id=:1' using pid;
    execute immediate 'delete from group_to_cityobject where cityobject_id=:1' using pid;
    execute immediate 'delete from generalization where generalizes_to_id=:1' using pid;
    execute immediate 'delete from generalization where cityobject_id=:1' using pid;
    execute immediate 'delete from external_reference where cityobject_id=:1' using pid;
    execute immediate 'delete from cityobject_genericattrib where cityobject_id=:1' using pid;
    execute immediate 'update cityobjectgroup set parent_cityobject_id=null where parent_cityobject_id=:1' using pid;
    
    for rec in appearance_cur loop
      delete_appearance(rec);
    end loop;
  exception
    when others then
      dbms_output.put_line('pre_delete_cityobject (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure intern_delete_cityobject(pid number)
  is
  begin
    pre_delete_cityobject(pid);
    execute immediate 'delete from cityobject where id=:1' using pid;
  exception
    when others then
      dbms_output.put_line('intern_delete_cityobject (id: ' || pid || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from CITYMODEL
  */
  procedure pre_delete_citymodel(citymodel_rec citymodel%rowtype)
  is
    cursor appearance_cur is
      select * from appearance where cityobject_id=citymodel_rec.id;
  begin
    -- TODO
    -- delete contained cityobjects!
    
    execute immediate 'delete from cityobject_member where citymodel_id=:1' using citymodel_rec.id;
    
    for rec in appearance_cur loop
      delete_appearance(rec);
    end loop;
  exception
    when others then
      dbms_output.put_line('pre_delete_citymodel (id: ' || citymodel_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_citymodel(citymodel_rec citymodel%rowtype)
  is
  begin
    pre_delete_citymodel(citymodel_rec);
    execute immediate 'delete from citymodel where id=:1' using citymodel_rec.id;
  exception
    when others then
      dbms_output.put_line('delete_citymodel (id: ' || citymodel_rec.id || '): ' || SQLERRM);
  end;

  /*
    internal: delete from APPEARANCE
  */
  procedure pre_delete_appearance(appearance_rec appearance%rowtype)
  is
    cursor surface_data_cur is
      select s.* from surface_data s, appear_to_surface_data ats
        where s.id=ats.surface_data_id and ats.appearance_id=appearance_rec.id;
  begin
    -- delete surface data not being referenced by appearances any more
    for rec in surface_data_cur loop
      if is_not_referenced('appear_to_surface_data', 'surface_data_id', rec.id, 'appearance_id', appearance_rec.id) then 
        delete_surface_data(rec);
      end if;
    end loop;
    
    execute immediate 'delete from appear_to_surface_data where appearance_id=:1' using appearance_rec.id;
  exception
    when others then
      dbms_output.put_line('pre_delete_appearance (id: ' || appearance_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_appearance(appearance_rec appearance%rowtype)
  is
  begin
    pre_delete_appearance(appearance_rec);
    execute immediate 'delete from appearance where id=:1' using appearance_rec.id;
  exception
    when others then
      dbms_output.put_line('delete_appearance (id: ' || appearance_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from SURFACE_DATA
  */
  procedure pre_delete_surface_data(surface_data_rec surface_data%rowtype)
  is
  begin
      execute immediate 'delete from appear_to_surface_data where surface_data_id=:1' using surface_data_rec.id;
      execute immediate 'delete from textureparam where surface_data_id=:1' using surface_data_rec.id;
  exception
    when others then
      dbms_output.put_line('pre_delete_surface_data (id: ' || surface_data_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_surface_data(surface_data_rec surface_data%rowtype)
  is
  begin
    pre_delete_surface_data(surface_data_rec);
    execute immediate 'delete from surface_data where id=:1' using surface_data_rec.id;
  exception
    when others then
      dbms_output.put_line('delete_surface_data (id: ' || surface_data_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from CITYOBJECTGROUP
  */
  procedure pre_delete_cityobjectgroup(cityobjectgroup_rec cityobjectgroup%rowtype)
  is
  begin
    execute immediate 'delete from group_to_cityobject where cityobjectgroup_id=:1' using cityobjectgroup_rec.id;
  exception
    when others then
      dbms_output.put_line('pre_delete_cityobjectgroup (id: ' || cityobjectgroup_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_cityobjectgroup(cityobjectgroup_rec cityobjectgroup%rowtype)
  is
  begin
    pre_delete_cityobjectgroup(cityobjectgroup_rec);
    execute immediate 'delete from cityobjectgroup where id=:1' using cityobjectgroup_rec.id;
    post_delete_cityobjectgroup(cityobjectgroup_rec);      
    exception
    when others then
      dbms_output.put_line('delete_cityobjectgroup (id: ' || cityobjectgroup_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_cityobjectgroup(cityobjectgroup_rec cityobjectgroup%rowtype)
  is
  begin
    if cityobjectgroup_rec.surface_geometry_id is not null then
      intern_delete_surface_geometry(cityobjectgroup_rec.surface_geometry_id);
    end if;  
    
    intern_delete_cityobject(cityobjectgroup_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_cityobjectgroup (id: ' || cityobjectgroup_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from THEMATIC_SURFACE
  */
  procedure pre_delete_thematic_surface(thematic_surface_rec thematic_surface%rowtype)
  is
    cursor opening_cur is
      select o.* from opening o, opening_to_them_surface otm 
        where o.id=otm.opening_id and otm.thematic_surface_id=thematic_surface_rec.id;
  begin
    -- delete openings not being referenced by a thematic surface any more
    for rec in opening_cur loop
      if is_not_referenced('opening_to_them_surface', 'opening_id', rec.id, 'thematic_surface_id', thematic_surface_rec.id) then 
        delete_opening(rec);
      end if;
    end loop;
  
    execute immediate 'delete from opening_to_them_surface where thematic_surface_id=:1' using thematic_surface_rec.id;
  exception
    when others then
      dbms_output.put_line('pre_delete_thematic_surface (id: ' || thematic_surface_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_thematic_surface(thematic_surface_rec thematic_surface%rowtype)
  is
  begin
    pre_delete_thematic_surface(thematic_surface_rec);
    execute immediate 'delete from thematic_surface where id=:1' using thematic_surface_rec.id;
    post_delete_thematic_surface(thematic_surface_rec);
  exception
    when others then
      dbms_output.put_line('delete_thematic_surface (id: ' || thematic_surface_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_thematic_surface(thematic_surface_rec thematic_surface%rowtype)
  is
  begin
    if thematic_surface_rec.lod2_multi_surface_id is not null then
      intern_delete_surface_geometry(thematic_surface_rec.lod2_multi_surface_id);
    end if;
    if thematic_surface_rec.lod3_multi_surface_id is not null then
      intern_delete_surface_geometry(thematic_surface_rec.lod3_multi_surface_id);
    end if;
    if thematic_surface_rec.lod4_multi_surface_id is not null then
      intern_delete_surface_geometry(thematic_surface_rec.lod4_multi_surface_id);
    end if;
    
    intern_delete_cityobject(thematic_surface_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_thematic_surface (id: ' || thematic_surface_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from OPENING
  */
  procedure pre_delete_opening(opening_rec opening%rowtype)
  is
  begin
    execute immediate 'delete from opening_to_them_surface where opening_id=:1' using opening_rec.id;
  exception
    when others then
      dbms_output.put_line('pre_delete_opening (id: ' || opening_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_opening(opening_rec opening%rowtype)
  is
  begin
    pre_delete_opening(opening_rec);
    execute immediate 'delete from opening where id=:1' using opening_rec.id;
    post_delete_opening(opening_rec);
  exception
    when others then
      dbms_output.put_line('delete_opening (id: ' || opening_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_opening(opening_rec opening%rowtype)
  is
    cursor address_cur is
      select a.id from address a left outer join address_to_building ab
        on a.id=ab.address_id where a.id=opening_rec.address_id and ab.address_id is null;
  begin
    if opening_rec.lod3_multi_surface_id is not null then
      intern_delete_surface_geometry(opening_rec.lod3_multi_surface_id);
    end if;
    if opening_rec.lod4_multi_surface_id is not null then
      intern_delete_surface_geometry(opening_rec.lod4_multi_surface_id);
    end if;
    
    -- delete addresses not being referenced from buildings and openings any more
    for rec in address_cur loop
      if is_not_referenced('opening', 'address_id', rec.id, 'id', opening_rec.id) then
        delete_address(rec.id);
      end if;   
    end loop;
    
    intern_delete_cityobject(opening_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_opening (id: ' || opening_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from BUILDING_INSTALLATION
  */
  procedure delete_building_installation(building_installation_rec building_installation%rowtype)
  is
  begin
    execute immediate 'delete from building_installation where id=:1' using building_installation_rec.id;
    post_delete_building_inst(building_installation_rec);
  exception
    when others then
      dbms_output.put_line('delete_building_installation (id: ' || building_installation_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_building_inst(building_installation_rec building_installation%rowtype)
  is
  begin
    if building_installation_rec.lod2_geometry_id is not null then
      intern_delete_surface_geometry(building_installation_rec.lod2_geometry_id);
    end if;
    if building_installation_rec.lod3_geometry_id is not null then
      intern_delete_surface_geometry(building_installation_rec.lod3_geometry_id);
    end if;
    if building_installation_rec.lod4_geometry_id is not null then
      intern_delete_surface_geometry(building_installation_rec.lod4_geometry_id);
    end if;
    
    intern_delete_cityobject(building_installation_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_building_inst (id: ' || building_installation_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from ROOM
  */
  procedure pre_delete_room(room_rec room%rowtype)
  is
    cursor thematic_surface_cur is
      select * from thematic_surface where room_id=room_rec.id;
      
    cursor building_installation_cur is
      select * from building_installation where room_id=room_rec.id;
      
    cursor building_furniture_cur is
      select * from building_furniture where room_id=room_rec.id;
  begin
    for rec in thematic_surface_cur loop
      delete_thematic_surface(rec);
    end loop;
    
    for rec in building_installation_cur loop
      delete_building_installation(rec);
    end loop;
      
    for rec in building_furniture_cur loop
      delete_building_furniture(rec);
    end loop;
   exception
    when others then
      dbms_output.put_line('pre_delete_room (id: ' || room_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_room(room_rec room%rowtype)
  is
  begin
    pre_delete_room(room_rec);
    execute immediate 'delete from room where id=:1' using room_rec.id;
    post_delete_room(room_rec);
  exception
    when others then
      dbms_output.put_line('delete_room (id: ' || room_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_room(room_rec room%rowtype)
  is
  begin
    if room_rec.lod4_geometry_id is not null then
      intern_delete_surface_geometry(room_rec.lod4_geometry_id);
    end if;
    
    intern_delete_cityobject(room_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_room (id: ' || room_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from BUILDING_FURNITURE
  */
  procedure delete_building_furniture(building_furniture_rec building_furniture%rowtype)
  is
  begin
    execute immediate 'delete from building_furniture where id=:1' using building_furniture_rec.id;
    post_delete_building_furniture(building_furniture_rec);
  exception
    when others then
      dbms_output.put_line('delete_building_furniture (id: ' || building_furniture_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_building_furniture(building_furniture_rec building_furniture%rowtype)
  is
  begin
    if building_furniture_rec.lod4_geometry_id is not null then
      intern_delete_surface_geometry(building_furniture_rec.lod4_geometry_id);
    end if;
    if building_furniture_rec.lod4_implicit_rep_id is not null then
      intern_delete_implicit_geom(building_furniture_rec.lod4_implicit_rep_id);
    end if;
    
    intern_delete_cityobject(building_furniture_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_building_furniture (id: ' || building_furniture_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    internal: delete from BUILDING
  */
  procedure pre_delete_building(building_rec building%rowtype)
  is    
    cursor building_part_cur is
      select * from building where id!=building_rec.id and building_parent_id=building_rec.id;
    
    cursor thematic_surface_cur is
      select * from thematic_surface where building_id=building_rec.id;
    
    cursor building_installation_cur is
      select * from building_installation where building_id=building_rec.id;
      
    cursor room_cur is
      select * from room where building_id=building_rec.id;
    
    cursor address_cur is
      select address_id from address_to_building where building_id=building_rec.id;
  begin
    for rec in building_part_cur loop
      delete_building(rec);
    end loop;
    
    for rec in thematic_surface_cur loop
      delete_thematic_surface(rec);
    end loop;
    
    for rec in building_installation_cur loop
      delete_building_installation(rec);
    end loop;
    
    for rec in room_cur loop
      delete_room(rec);
    end loop;
        
    -- delete addresses being not referenced from buildings any more
    for rec in address_cur loop
      if is_not_referenced('address_to_building', 'address_id', rec.address_id, 'building_id', building_rec.id) then 
        delete_address(rec.address_id);
      end if;
    end loop;
    
    execute immediate 'delete from address_to_building where building_id=:1' using building_rec.id;
  exception
    when others then
      dbms_output.put_line('pre_delete_building (id: ' || building_rec.id || '): ' || SQLERRM);
  end;
  
  procedure delete_building(building_rec building%rowtype)
  is
  begin
    pre_delete_building(building_rec);
    execute immediate 'delete from building where id=:1' using building_rec.id;
    post_delete_building(building_rec);
  exception
    when others then
      dbms_output.put_line('delete_building (id: ' || building_rec.id || '): ' || SQLERRM);
  end;
  
  procedure post_delete_building(building_rec building%rowtype)
  is
  begin
    if building_rec.lod1_geometry_id is not null then
      intern_delete_surface_geometry(building_rec.lod1_geometry_id);
    end if; 
    if building_rec.lod2_geometry_id is not null then
      intern_delete_surface_geometry(building_rec.lod2_geometry_id);
    end if;
    if building_rec.lod3_geometry_id is not null then
      intern_delete_surface_geometry(building_rec.lod3_geometry_id);
    end if;
    if building_rec.lod4_geometry_id is not null then
      intern_delete_surface_geometry(building_rec.lod4_geometry_id);
    end if;
    
    intern_delete_cityobject(building_rec.id);
  exception
    when others then
      dbms_output.put_line('post_delete_building (id: ' || building_rec.id || '): ' || SQLERRM);
  end;
  
  /*
    PUBLIC API PROCEDURES
  */  
  procedure delete_surface_geometry(pid number, clean_apps int := 0)
  is
  begin
    intern_delete_surface_geometry(pid);
    
    if clean_apps <> 0 then
      cleanup_appearances(0);
    end if;
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_surface_geometry (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_implicit_geometry(pid number)
  is
  begin
    intern_delete_implicit_geom(pid);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_implicit_geometry (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_external_reference(pid number)
  is
  begin
    execute immediate 'delete from external_reference where id=:1' using pid;
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_external_reference (id: ' || pid || '): ' || SQLERRM);
  end; 
  
  procedure delete_citymodel(pid number)
  is
    citymodel_rec citymodel%rowtype;
  begin
    execute immediate 'select * from citymodel where id=:1'
      into citymodel_rec
      using pid;
    
    delete_citymodel(citymodel_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_citymodel (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_appearance(pid number)
  is
    appearance_rec appearance%rowtype;
  begin
    execute immediate 'select * from appearance where id=:1'
      into appearance_rec
      using pid;
    
    delete_appearance(appearance_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_appearance (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_surface_data(pid number)
  is
    surface_data_rec surface_data%rowtype;
  begin
    execute immediate 'select * from surface_data where id=:1'
      into surface_data_rec
      using pid;
    
    delete_surface_data(surface_data_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_surface_data (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_cityobjectgroup(pid number)
  is
    cityobjectgroup_rec cityobjectgroup%rowtype;
  begin
    execute immediate 'select * from cityobjectgroup where id=:1'
      into cityobjectgroup_rec
      using pid;
    
    delete_cityobjectgroup(cityobjectgroup_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_cityobjectgroup (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_thematic_surface(pid number)
  is
    thematic_surface_rec thematic_surface%rowtype;
  begin
    execute immediate 'select * from thematic_surface where id=:1'
      into thematic_surface_rec
      using pid;
  
    delete_thematic_surface(thematic_surface_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_thematic_surface (id: ' || pid || '): ' || SQLERRM);
  end;

  procedure delete_opening(pid number)
  is
    opening_rec opening%rowtype;
  begin
    execute immediate 'select * from opening where id=:1'
      into opening_rec
      using pid;
    
    delete_opening(opening_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_opening (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_address(pid number)
  is
  begin
    execute immediate 'delete from address_to_building where address_id=:1' using pid;
    execute immediate 'update opening set address_id=null where address_id=:1' using pid;
    execute immediate 'delete from address where id=:1' using pid;
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_address (id: ' || pid || '): ' || SQLERRM);
  end;

  procedure delete_building_installation(pid number)
  is
    building_installation_rec building_installation%rowtype;
  begin
    execute immediate 'select * from building_installation where id=:1'
      into building_installation_rec
      using pid;
    
    delete_building_installation(building_installation_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_building_installation (id: ' || pid || '): ' || SQLERRM);
  end;

  procedure delete_room(pid number)
  is
    room_rec room%rowtype;    
  begin
    execute immediate 'select * from room where id=:1'
      into room_rec
      using pid;
    
    delete_room(room_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_room (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_building_furniture(pid number)
  is
    building_furniture_rec building_furniture%rowtype;    
  begin
    execute immediate 'select * from building_furniture where id=:1'
      into building_furniture_rec
      using pid;
    
    delete_building_furniture(building_furniture_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_building_furniture (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure delete_building(pid number)
  is
    building_rec building%rowtype;    
  begin
    execute immediate 'select * from building where id=:1'
      into building_rec
      using pid;
    
    delete_building(building_rec);
  exception
    when no_data_found then
      return;
    when others then
      dbms_output.put_line('delete_building (id: ' || pid || '): ' || SQLERRM);
  end;
  
  procedure cleanup_appearances(only_global int :=1)
  is
    cursor surface_data_global_cur is
      select s.* from surface_data s left outer join textureparam t
        on s.id=t.surface_data_id where t.surface_data_id is null;
    
    cursor appearance_cur is
      select a.* from appearance a left outer join appear_to_surface_data asd
        on a.id=asd.appearance_id where asd.appearance_id is null;
        
    cursor appearance_global_cur is
      select a.* from appearance a left outer join appear_to_surface_data asd
        on a.id=asd.appearance_id where a.cityobject_id is null and asd.appearance_id is null;
  begin
    -- global appearances are not related to a cityobject.
    -- however, we assume that all surface geometries of a cityobject
    -- have been deleted at this stage. thus, we can check and delete
    -- surface data which does not have a valid texture parameterization
    -- any more.
    for rec in surface_data_global_cur loop
      delete_surface_data(rec);
    end loop;
    
    -- delete appearances which does not have surface data any more
    if only_global=1 then
      for rec in appearance_global_cur loop
        delete_appearance(rec);
      end loop;
    else
      for rec in appearance_cur loop
        delete_appearance(rec);
      end loop;
    end if;
  exception
    when others then
      dbms_output.put_line('cleanup_appearances: ' || SQLERRM);
  end;

  procedure cleanup_cityobjectgroups
  is
    cursor group_cur is
      select g.* from cityobjectgroup g left outer join group_to_cityobject gtc
        on g.id=gtc.cityobject_id where gtc.cityobject_id is null;
  begin
    for rec in group_cur loop
      delete_cityobjectgroup(rec);
    end loop;
  exception
    when others then
      dbms_output.put_line('cleanup_cityobjectgroups: ' || SQLERRM);
  end;
  
  procedure cleanup_citymodels
  is
    cursor citymodel_cur is
      select c.* from citymodel c left outer join cityobject_member cm
        on c.id=cm.citymodel_id where cm.citymodel_id is null;
  begin
    for rec in citymodel_cur loop
      delete_citymodel(rec);
    end loop;
  exception
    when others then
      dbms_output.put_line('cleanup_citymodel: ' || SQLERRM);
  end;
  
END geodb_delete;
/