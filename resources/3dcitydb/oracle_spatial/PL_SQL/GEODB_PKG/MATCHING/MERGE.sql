-- MERGE.sql
--
-- Authors:     Alexandra Stadler <stadler@igg.tu-berlin.de>
--              Claus Nagel <claus.nagel@tu-berlin.de>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
--                             Technische Universit�t Berlin, Germany
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
-- 1.1       2011-02-13   bugfixes, e.g. appearance mover             CNag
-- 1.0.0     2008-09-10   release version                             ASta
--

set term off;
set serveroutput off;
   
drop table merge_collect_geom;
create global temporary table merge_collect_geom
  (building_id number, 
    geometry_id number, 
    cityobject_id number
  ) on commit preserve rows;
   
drop table merge_container_ids;
create table merge_container_ids 
  (building_id number, 
    container_id number
  ) nologging;
   
set term on;
set serveroutput on;

CREATE OR REPLACE PACKAGE geodb_merge
AS
	procedure process_matches(lod_src number, lod_dst number, name_mode number, delimiter varchar2);
  
  procedure collect_all_geometry(lod number);
	procedure remove_geometry_from_cand(lod number);
	procedure create_and_put_container(lod number, name_mode number, delimiter varchar2);
	procedure move_appearance;
	procedure move_geometry;
	procedure delete_head_of_merge_geometry;
  procedure delete_relevant_candidates;
	procedure update_lineage(lineage varchar2);
END geodb_merge;
/
 
CREATE OR REPLACE PACKAGE BODY geodb_merge 
AS
  -- declaration of private indexes
  merge_geom_building_id_idx index_obj :=
    index_obj.construct_normal('merge_geom_building_id_idx', 'merge_collect_geom', 'building_id');
  merge_geom_geometry_id_idx index_obj :=
    index_obj.construct_normal('merge_geom_geometry_id_idx', 'merge_collect_geom', 'geometry_id');
  merge_cont_building_id_idx index_obj :=
    index_obj.construct_normal('merge_cont_building_id_idx', 'merge_container_ids', 'building_id');
  merge_cont_id_idx index_obj :=
    index_obj.construct_normal('merge_cont_id_idx', 'merge_container_ids', 'container_id');

  procedure process_matches(lod_src number, lod_dst number, name_mode number, delimiter varchar2)
  is
  begin
    -- find relevant matches
    collect_all_geometry(lod_src);
    remove_geometry_from_cand(lod_src);
    create_and_put_container(lod_dst, name_mode, delimiter);
    move_appearance();
    move_geometry();
    delete_head_of_merge_geometry();
    
    commit;
  exception
    when others then
      dbms_output.put_line('process_matches: ' || SQLERRM);
  end;

  procedure collect_all_geometry(lod number)
  is
    log varchar2(4000);
  begin
    execute immediate 'truncate table merge_collect_geom';
    log := geodb_idx.drop_index(merge_geom_building_id_idx, false);
    log := geodb_idx.drop_index(merge_geom_geometry_id_idx, false); 
  
    -- retrieve all building and building part geometry
    execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
      select b.building_root_id, b.lod'||to_char(lod)||'_geometry_id, b.id from building b, match_overlap_relevant m
      where b.building_root_id = m.id1 and b.lod'||to_char(lod)||'_geometry_id is not null';
  
    if lod >= 2 then
      -- retrieve relevant building installation geometry
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, i.lod'||to_char(lod)||'_geometry_id, i.id
          from match_overlap_relevant m, building_installation i, building b
          where i.building_id = b.id
                and b.building_root_id = m.id1
                and i.is_external = 1
                and i.lod'||to_char(lod)||'_geometry_id is not null';
  
      -- retrieve surfaces from relevant thematic surfaces
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select  b.building_root_id, t.lod'||to_char(lod)||'_multi_surface_id, t.id
          from match_overlap_relevant m, thematic_surface t, building b
          where t.building_id = b.id
                and b.building_root_id = m.id1
                and t.lod'||to_char(lod)||'_multi_surface_id is not null';
    end if;
  
    if lod >= 3 then
        -- monster join to retrieve all openings of all thematic surfaces beloning to all buildings and building parts
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, o.lod'||to_char(lod)||'_multi_surface_id, o.id
          from match_overlap_relevant m, thematic_surface t, building b, opening o, opening_to_them_surface ot
          where t.building_id = b.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id
                and o.id = ot.opening_id
                and o.lod'||to_char(lod)||'_multi_surface_id is not null';
    end if;
  
    if lod >= 4 then
      -- room
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, r.lod4_geometry_id, r.id
          from match_overlap_relevant m, room r, building b
          where r.building_id = b.id
                and b.building_root_id = m.id1
                and r.lod4_geometry_id is not null';
  
      -- building furniture (in rooms) --bei lod r in f ge�ndert
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, f.lod4_geometry_id, f.id
          from match_overlap_relevant m, room r, building b, building_furniture f
          where r.building_id = b.id
                and b.building_root_id = m.id1
                and f.room_id = r.id
                and f.lod4_geometry_id is not null';
  
      -- retrieve relevant internal (or external) building installation geometry (in rooms)
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, i.lod4_geometry_id, i.id
          from match_overlap_relevant m, building_installation i, building b, room r
          where r.building_id = b.id
                and i.room_id = r.id
                and b.building_root_id = m.id1
                and i.lod4_geometry_id is not null';
  
      -- retrieve surfaces from relevant thematic surfaces (in rooms)
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select  b.building_root_id, t.lod4_multi_surface_id, t.id
          from match_overlap_relevant m, thematic_surface t, building b, room r
          where r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and t.lod4_multi_surface_id is not null';
  
        -- monster join to retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, o.lod4_multi_surface_id, o.id
          from match_overlap_relevant m, thematic_surface t, building b, opening o, opening_to_them_surface ot, room r
          where r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id
                and o.id = ot.opening_id
                and o.lod4_multi_surface_id is not null';
  
      -- retrieve relevant internal building installation geometry
      execute immediate 'insert all /*+ append nologging */ into merge_collect_geom
        select b.building_root_id, i.lod4_geometry_id, i.id
          from match_overlap_relevant m, building_installation i, building b
          where i.building_id = b.id
                and b.building_root_id = m.id1
                and i.is_external = 0
                and i.lod4_geometry_id is not null';
    end if;
    
    log := geodb_idx.create_index(merge_geom_building_id_idx, false, 'nologging');
    log := geodb_idx.create_index(merge_geom_geometry_id_idx, false, 'nologging'); 
  exception
      when others then
        dbms_output.put_line('collect_all_geometry: ' || SQLERRM);
  end;
  
  procedure remove_geometry_from_cand(lod number)
  is
  begin
    -- retrieve all building and building part geometry
    execute immediate 'update building b
      set b.lod'||to_char(lod)||'_geometry_id = null
      where b.building_root_id in (select id1 from match_overlap_relevant)';
  
    if lod >= 2 then
      -- retrieve relevant building installation geometry
      execute immediate 'update building_installation i
        set i.lod'||to_char(lod)||'_geometry_id = null
        where i.building_id in (select b.id from building b, match_overlap_relevant m where b.building_root_id = m.id1)
          and i.is_external = 1';
  
      -- retrieve surfaces from relevant thematic surfaces
      execute immediate 'update thematic_surface t
      set t.lod'||to_char(lod)||'_multi_surface_id = null
      where t.building_id in (select b.id from building b, match_overlap_relevant m where b.building_root_id = m.id1)';
    end if;
  
    if lod >= 3 then
      -- monster join to retrieve all openings of all thematic surfaces beloning to all buildings and building parts
      execute immediate 'update opening o
        set o.lod'||to_char(lod)||'_multi_surface_id = null
        where o.id in
          (select ot.opening_id from match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot
          where ot.thematic_surface_id = t.id
        and t.building_id = b.id
        and b.building_root_id = m.id1)';
    end if;
  
    if lod >= 4 then
      -- room
      execute immediate 'update room r
      set r.lod4_geometry_id = null
      where r.building_id in
        (select b.id from match_overlap_relevant m, building b
        where b.building_root_id = m.id1)';
  
      -- building furniture (in rooms) --bei lod r in f ge�ndert
      execute immediate 'update building_furniture f
      set f.lod4_geometry_id = null
      where f.room_id in
            (select r.id from match_overlap_relevant m, room r, building b
            where r.building_id = b.id
              and b.building_root_id = m.id1)';
  
        -- retrieve relevant internal (or external) building installation geometry (in rooms)
      execute immediate 'update building_installation i
      set i.lod4_geometry_id = null
      where i. room_id in
        (select r.id from match_overlap_relevant m, building b, room r
        where r.building_id = b.id
                  and b.building_root_id = m.id1)';
  
      -- retrieve surfaces from relevant thematic surfaces (in rooms)
      execute immediate 'update thematic_surface t
      set t.lod4_multi_surface_id = null
      where t.room_id in
        (select r.id from match_overlap_relevant m, building b, room r
        where r.building_id = b.id
            and b.building_root_id = m.id1)';
  
      -- monster join to retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
      execute immediate 'update opening o
      set o.lod4_multi_surface_id = null
      where o.id in
        (select ot.opening_id from match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot, room r
          where r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id)';
  
      -- retrieve relevant internal building installation geometry
      execute immediate 'update building_installation i
      set i.lod4_geometry_id = null
      where i.is_external = 0
        and i.building_id in
          (select b.id from match_overlap_relevant m, building b
          where b.building_root_id = m.id1)';
      end if;
  exception
      when others then
        dbms_output.put_line('remove_geometry_from_cand: ' || SQLERRM);
  end;
  
  procedure create_and_put_container(lod number, name_mode number, delimiter varchar2)
  is
    seq_val number;
    old_geometry number;
    log varchar2(4000);
    
    cursor building_id_cur is
      select id1 from match_overlap_relevant;
  begin
    execute immediate 'truncate table merge_container_ids';
    log := geodb_idx.drop_index(merge_cont_building_id_idx, false);
    log := geodb_idx.drop_index(merge_cont_id_idx, false); 
    
    -- iterate through all affected buildings
    for building_id_rec in building_id_cur loop    
      -- create geometry id and register in container
      execute immediate 'select surface_geometry_seq.nextval from dual' into seq_val;
      execute immediate 'insert into merge_container_ids (building_id, container_id)
        values (:1, :2)'
        using building_id_rec.id1, seq_val;
        
      -- retrieve and delete old geometry
      begin
        execute immediate 'select b.lod'||to_char(lod)||'_geometry_id from building b
          where b.id = (select id2 from match_overlap_relevant where id1 = :1)'
          into old_geometry
          using building_id_rec.id1;
      exception
        when others then
          old_geometry := 0;
      end;
      
      -- create new multisurface as root element of new merge geometry
      execute immediate 'insert into surface_geometry (id, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry)
        values (:1, null, :2, 0, 0, 0, 0, 0, null)'
        using seq_val, seq_val;
        
      -- set building geometry to new multisurface and process name
      if name_mode=1 then
        -- ignore cand name
        execute immediate 'update building b
          set b.lod'||to_char(lod)||'_geometry_id = :1
          where b.id = (select id2 from match_overlap_relevant where id1 = :2)'
        using seq_val, building_id_rec.id1;
      elsif name_mode=2 then
        -- replace master name with cand name
        execute immediate 'update building b
          set b.lod'||to_char(lod)||'_geometry_id = :1,
          b.name = (select name from building where id = :2)
          where b.id = (select id2 from match_overlap_relevant where id1 = :3)'
        using seq_val, building_id_rec.id1, building_id_rec.id1;
      else
        -- append cand name to master
        execute immediate 'update building b
          set b.lod'||to_char(lod)||'_geometry_id = :1,
            b.name = concat(b.name, nullif(concat(:2, (select name from building where id = :3)), :4))
            where b.id = (select id2 from match_overlap_relevant where id1 = :4)'
        using seq_val, delimiter, building_id_rec.id1, delimiter, building_id_rec.id1;
      end if;
      
      -- delete old geometry
      if old_geometry > 0 then
        geodb_delete.delete_building(old_geometry);
      end if;    
    end loop;
    
    log := geodb_idx.create_index(merge_cont_building_id_idx, false, 'nologging');
    log := geodb_idx.create_index(merge_cont_id_idx, false, 'nologging'); 
  exception
    when others then
      dbms_output.put_line('create_and_put_container: ' || SQLERRM);
  end;
  
  procedure move_appearance
  is
    geom_hierachies number;  
    app_id number;
    app_is_global number;
    seq_val number;
    building_id number;
        
    cursor building_cur is
      select building_id, count(geometry_id) as cnt_hierarchies from merge_collect_geom 
        group by building_id;   
  begin
    -- iterate through all building matches
    for building_rec in building_cur loop
      declare
        cursor app_cur is
          select a.id, a.theme, a.description, a.cityobject_id, sd.id as sd_id, tp.surface_geometry_id as geometry_id, cg.geometry_id as hierarchy_id
            from merge_collect_geom cg, surface_geometry sg, textureparam tp, surface_data sd, appear_to_surface_data asd, appearance a 
            where cg.building_id=building_rec.building_id
            and sg.root_id=cg.geometry_id
            and tp.surface_geometry_id=sg.id
            and sd.id=tp.surface_data_id            
            and asd.surface_data_id=sd.id  
            and a.id=asd.appearance_id
            order by a.id;
      begin
        app_id := -1;
        app_is_global := 0;
        
        -- iterate through appearances which reference a geometry which will be merged 
        -- into the newly created gml:MultiSurface of the reference building
        for app_rec in app_cur loop
          if app_rec.id != app_id then
            app_id := app_rec.id;
            
            -- if the appearance is local we create a new appearance element for the
            -- reference building into which we transfer the surface data
            if app_rec.cityobject_id is not null then
              execute immediate 'select appearance_seq.nextval from dual' into seq_val;
              execute immediate 'select id2 from match_overlap_relevant where id1=:1' into building_id using building_rec.building_id;
              
              -- create new appearance for reference building
              execute immediate 'insert into appearance (id, name, name_codespace, description, theme, citymodel_id, cityobject_id)
                values (:1, null, null, :2, :3, null, :4)'
                using seq_val, app_rec.description, app_rec.theme, building_id;
            else
              app_is_global := 1;
            end if;
          end if;
          
          if app_is_global = 0 then
            -- move existing surface data to newly created appearance
            execute immediate 'update appear_to_surface_data
              set appearance_id=:1
              where appearance_id=:2 and surface_data_id=:3'
              using seq_val, app_rec.id, app_rec.sd_id;
          end if;
            
          -- if any surface data of the appearance references the root element of the merge geometry
          -- we need to apply further checks
          if app_rec.geometry_id = app_rec.hierarchy_id then
          
            -- if just one geometry hierarchy has to be merged we simply let the
            -- textureparam point to the new root geometry element created for the reference building
            if building_rec.cnt_hierarchies = 1 then
              -- let textureparam point to newly created root element
              execute immediate 'update textureparam t
                set t.surface_geometry_id=(select container_id from merge_container_ids where building_id=:1)
                where t.surface_geometry_id=:2'
                using building_rec.building_id, app_rec.hierarchy_id;
              
              -- copy gml:id to newly created root element - this is required
              -- for referencing the geometry from within the appearance
              execute immediate 'update surface_geometry s
                set (s.gmlid, s.gmlid_codespace)=(select gmlid, gmlid_codespace from surface_geometry where id=:1)
                where s.id=(select container_id from merge_container_ids where building_id=:2)'
                using app_rec.hierarchy_id, building_rec.building_id;
            
            -- if more than one geometry hierarchy is merged into a single geometry hierarchy
            -- for the reference building, things are a bit more complicated
            else
              declare
                counter number;
                gmlid surface_geometry.gmlid%type;
                gmlid_codespace surface_geometry.gmlid_codespace%type;
                
                cursor textureparam_cur is
                  select * from textureparam where surface_data_id=app_rec.sd_id;
                cursor surface_geometry_cur is
                  select * from surface_geometry where parent_id=app_rec.hierarchy_id;
              begin
                begin
                  execute immediate 'select gmlid, gmlid_codespace from surface_geometry where id=:1'
                    into gmlid, gmlid_codespace
                    using app_rec.hierarchy_id;
                exception
                  when others then
                    gmlid := 'ID_';
                    gmlid_codespace := '';
                end;
              
                -- first we need to iterate over all textureparam which point to the root of the geometry hierachy to be merged.
                -- second we identify all direct childs of this root element. for each of these childs we create a copy 
                -- of the original textureparam and let it point to the child.           
                for textureparam_rec in textureparam_cur loop
                  counter := 0;                
                  for surface_geometry_rec in surface_geometry_cur loop
                    counter := counter + 1;
                  
                    -- create a new textureparam and let it point to the child instead of the root
                    execute immediate 'insert into textureparam (surface_geometry_id, surface_data_id, is_texture_parametrization, world_to_texture, texture_coordinates)
                      values (:1, :2, :3, :4, :5)'
                      using surface_geometry_rec.id, 
                        app_rec.sd_id, 
                        textureparam_rec.is_texture_parametrization, 
                        textureparam_rec.world_to_texture,
                        textureparam_rec.texture_coordinates;
              
                    -- make sure the child geometry referenced by the textureparam has a gml:id value
                    execute immediate 'update surface_geometry
                      set gmlid=concat(:1, :2),
                        gmlid_codespace=:3
                      where id=:4 and gmlid is null'
                      using gmlid, '_' || to_char(counter), gmlid_codespace, surface_geometry_rec.id;                                      
                  end loop;
                end loop;
              end;
            end if;
          end if;
        end loop;
      end;
    end loop;
  exception
      when others then
        dbms_output.put_line('move_appearance: ' || SQLERRM);
  end;
  
  procedure move_geometry
  is
  begin
    -- update parent of immediate children of all collected geometries
    execute immediate 'update surface_geometry s
      set s.parent_id = (select c.container_id from merge_container_ids c, merge_collect_geom g
                        where s.parent_id = g.geometry_id and c.building_id = g.building_id)
      where s.parent_id in (select geometry_id from merge_collect_geom)';
  
    -- change nested solids into composite surfaces since we throw everything into a multisurface
    execute immediate 'update surface_geometry s
      set s.is_composite = 1,
      s.is_solid = 0
      where s.root_id in (select geometry_id from merge_collect_geom) and s.is_solid=1 and s.root_id<>s.id';
    
    -- update all root_ids
    execute immediate 'update surface_geometry s
      set s.root_id = (select c.container_id from merge_container_ids c, merge_collect_geom g
                      where s.root_id = g.geometry_id and c.building_id = g.building_id)
      where s.root_id in (select geometry_id from merge_collect_geom) and s.root_id<>s.id';
  exception
      when others then
        dbms_output.put_line('move_geometry: ' || SQLERRM);
  end;
  
  procedure update_lineage(lineage varchar2)
  is
  begin
    execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select b.id from building b, match_overlap_relevant m where b.building_root_id = m.id1)'
      using lineage;
  
      -- retrieve relevant building installation geometry
      execute immediate 'update cityobject c
        set c.lineage = :1
        where c.id in (select i.id from building_installation i, building b, match_overlap_relevant m
          where i.building_id = b.id
            and b.building_root_id = m.id1
            and i.is_external = 1)'
        using lineage;
  
      -- retrieve surfaces from relevant thematic surfaces
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select t.id from thematic_surface t, building b, match_overlap_relevant m
        where t.building_id = b.id
          and b.building_root_id = m.id1)'
      using lineage;
  
      -- monster join to retrieve all openings of all thematic surfaces beloning to all buildings and building parts
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select o.id from opening o, match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot
        where o.id = ot.opening_id
          and ot.thematic_surface_id = t.id
          and t.building_id = b.id
          and b.building_root_id = m.id1)'
      using lineage;
  
      -- room
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select r.id from room r, match_overlap_relevant m, building b
        where r.building_id = b.id
          and b.building_root_id = m.id1)'
      using lineage;
  
      -- building furniture (in rooms) --bei lod r in f ge�ndert
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select f.id from building_furniture f, match_overlap_relevant m, room r, building b
            where f.room_id = r.id
          and r.building_id = b.id
              and b.building_root_id = m.id1)'
      using lineage;
  
        -- retrieve relevant internal (or external) building installation geometry (in rooms)
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select i.id from building_installation i, match_overlap_relevant m, building b, room r
        where i.room_id = r.id
          and r.building_id = b.id
                  and b.building_root_id = m.id1)'
      using lineage;
  
      -- retrieve surfaces from relevant thematic surfaces (in rooms)
      execute immediate 'update cityobject c
      set c.lineage = :1
        where c.id in (select t.id from thematic_surface t, match_overlap_relevant m, building b, room r
                where t.room_id = r.id
                and r.building_id = b.id
                  and b.building_root_id = m.id1)'
      using lineage;
  
      -- monster join to retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select o.id from opening o, match_overlap_relevant m, thematic_surface t, building b, opening_to_them_surface ot, room r
          where o.id = ot.opening_id
          and r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id)'
      using lineage;
  
      -- retrieve relevant internal building installation geometry
      execute immediate 'update cityobject c
      set c.lineage = :1
      where c.id in (select i.id from building_installation i, match_overlap_relevant m, building b
        where i.building_id = b.id
          and b.building_root_id = m.id1
          and i.is_external = 0)'
      using lineage;
  exception
      when others then
        dbms_output.put_line('collect_all_geometry: ' || SQLERRM);
  end;
  
  procedure delete_head_of_merge_geometry
  is
    cursor geometry_cur is
      select geometry_id from merge_collect_geom;
  begin
    -- cleanly delete root of merged geometry hierarchy
    for geometry_rec in geometry_cur loop
      geodb_delete.delete_surface_geometry(geometry_rec.geometry_id);
    end loop;
  end;
  
  procedure delete_relevant_candidates
  is
    cursor candidate_cur is
      select id1 from match_overlap_relevant;
  begin
    for candidate_rec in candidate_cur loop
      geodb_delete.delete_building(candidate_rec.id1);
    end loop;
  exception
    when others then
      dbms_output.put_line('delete_candidates: ' || SQLERRM);
  end;

END geodb_merge;
/
 
