-- PROCESS_MATCHES.sql
--
-- Authors:     Alexandra Stadler <stadler@igg.tu-berlin.de>
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
-- 1.0.0     2008-09-10   release version                             ASta
--

truncate table MATCH_RESULT_RELEVANT;
drop table MATCH_RESULT_RELEVANT;
  CREATE GLOBAL TEMPORARY TABLE MATCH_RESULT_RELEVANT 
   (	"ID1" NUMBER, 
	"PARENT_ID1" NUMBER, 
	"ROOT_ID1" NUMBER, 
	"LOD1" NUMBER, 
	"ID2" NUMBER, 
	"PARENT_ID2" NUMBER, 
	"ROOT_ID2" NUMBER, 
	"LOD2" NUMBER
   ) ON COMMIT PRESERVE ROWS ;
   
   drop table COLLECT_GEOM;
   CREATE TABLE COLLECT_GEOM
   (	"BUILDING_ID" NUMBER, 
	"GEOMETRY_ID" NUMBER, 
	"CITYOBJECT_ID" NUMBER
   );
   
   drop table CONTAINER_IDS;
   CREATE TABLE CONTAINER_IDS 
   (	"BUILDING_ID" NUMBER, 
	"CONTAINER_ID" NUMBER
   );
   
  CREATE OR REPLACE PACKAGE GEODB_PROCESS_MATCHES
AS
	-- the important public function
	procedure process_matches(delta1 number, delta2 number, lod_src number, lod_dst number, name_mode number, delimiter varchar2);

	-- private helper functions
	procedure create_relevant_matches(delta1 number, delta2 number);
	procedure collect_all_geometry(lod number);
	procedure remove_geometry_from_cand(lod number);
	procedure create_and_put_container(lod number, name_mode number, delimiter varchar2);
	procedure move_appearance;
	procedure move_geometry;
	procedure delete_multi_surfaces;
	procedure update_lineage(lineage varchar2);
	procedure cleanup;
END geodb_process_matches;
/
 

  CREATE OR REPLACE PACKAGE BODY GEODB_PROCESS_MATCHES 
AS

procedure create_relevant_matches(delta1 number, delta2 number)
is
begin
	-- truncate tmp table
	execute immediate 'truncate table match_result_relevant';

	-- retrieve all match tupels with more than a user-specified percentage of area coverage
	execute immediate 'insert all into match_result_relevant (id1, parent_id1, root_id1, lod1, id2, parent_id2, root_id2, lod2)
	  select id1, parent_id1, root_id1, lod1, id2, parent_id2, root_id2, lod2
	    from match_result
	    where area1_cov_by_area2 >= :1
	          and area2_cov_by_area1 >= :2' using delta1, delta2;
exception
    when others then
      dbms_output.put_line('create_relevant_matches: ' || SQLERRM);
end;



procedure collect_all_geometry(lod number)
is
begin
	execute immediate 'truncate table collect_geom';
	--execute immediate 'create table collect_geom (building_id number, geometry_id number, cityobject_id number)';

	-- retrieve all building and building part geometry
	execute immediate 'insert all into collect_geom
		select b.building_root_id, b.lod'||to_char(lod)||'_geometry_id, b.id from building b, match_result_relevant m
		where b.building_root_id = m.id1 and b.lod'||to_char(lod)||'_geometry_id is not null';

    if lod >= 2 then
    	-- retrieve relevant building installation geometry
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, i.lod'||to_char(lod)||'_geometry_id, i.id
          from match_result_relevant m, building_installation i, building b
          where i.building_id = b.id
                and b.building_root_id = m.id1
                and i.is_external = 1
                and i.lod'||to_char(lod)||'_geometry_id is not null';

	    -- retrieve surfaces from relevant thematic surfaces
	    execute immediate 'insert all /*+ append nologging */ into collect_geom
        select  b.building_root_id, t.lod'||to_char(lod)||'_multi_surface_id, t.id
          from match_result_relevant m, thematic_surface t, building b
          where t.building_id = b.id
                and b.building_root_id = m.id1
                and t.lod'||to_char(lod)||'_multi_surface_id is not null';
    end if;

    if lod >= 3 then
        -- monster join to retrieve all openings of all thematic surfaces beloning to all buildings and building parts
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, o.lod'||to_char(lod)||'_multi_surface_id, o.id
          from match_result_relevant m, thematic_surface t, building b, opening o, opening_to_them_surface ot
          where t.building_id = b.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id
                and o.id = ot.opening_id
                and o.lod'||to_char(lod)||'_multi_surface_id is not null';
    end if;

    if lod >= 4 then
        -- room
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, r.lod4_geometry_id, r.id
          from match_result_relevant m, room r, building b
          where r.building_id = b.id
                and b.building_root_id = m.id1
                and r.lod4_geometry_id is not null';

        -- building furniture (in rooms) --bei lod r in f geändert
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, f.lod4_geometry_id, f.id
          from match_result_relevant m, room r, building b, building_furniture f
          where r.building_id = b.id
                and b.building_root_id = m.id1
                and f.room_id = r.id
                and f.lod4_geometry_id is not null';

    	-- retrieve relevant internal (or external) building installation geometry (in rooms)
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, i.lod4_geometry_id, i.id
          from match_result_relevant m, building_installation i, building b, room r
          where r.building_id = b.id
                and i.room_id = r.id
                and b.building_root_id = m.id1
                and i.lod4_geometry_id is not null';

	    -- retrieve surfaces from relevant thematic surfaces (in rooms)
	    execute immediate 'insert all /*+ append nologging */ into collect_geom
        select  b.building_root_id, t.lod4_multi_surface_id, t.id
          from match_result_relevant m, thematic_surface t, building b, room r
          where r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and t.lod4_multi_surface_id is not null';

        -- monster join to retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, o.lod4_multi_surface_id, o.id
	        from match_result_relevant m, thematic_surface t, building b, opening o, opening_to_them_surface ot, room r
	        where r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id
                and o.id = ot.opening_id
                and o.lod4_multi_surface_id is not null';

    	-- retrieve relevant internal building installation geometry
     	execute immediate 'insert all /*+ append nologging */ into collect_geom
        select b.building_root_id, i.lod4_geometry_id, i.id
	        from match_result_relevant m, building_installation i, building b
	        where i.building_id = b.id
                and b.building_root_id = m.id1
                and i.is_external = 0
                and i.lod4_geometry_id is not null';
    end if;
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
		where b.building_root_id in (select id1 from match_result_relevant)';

    if lod >= 2 then
    	-- retrieve relevant building installation geometry
     	execute immediate 'update building_installation i
     		set i.lod'||to_char(lod)||'_geometry_id = null
     		where i.building_id in (select b.id from building b, match_result_relevant m where b.building_root_id = m.id1)
     			and i.is_external = 1';

	    -- retrieve surfaces from relevant thematic surfaces
	    execute immediate 'update thematic_surface t
			set t.lod'||to_char(lod)||'_multi_surface_id = null
			where t.building_id in (select b.id from building b, match_result_relevant m where b.building_root_id = m.id1)';
    end if;

    if lod >= 3 then
        -- monster join to retrieve all openings of all thematic surfaces beloning to all buildings and building parts
     	execute immediate 'update opening o
		set o.lod'||to_char(lod)||'_multi_surface_id = null
		where o.id in
			(select ot.opening_id from match_result_relevant m, thematic_surface t, building b, opening_to_them_surface ot
			where ot.thematic_surface_id = t.id
				and t.building_id = b.id
				and b.building_root_id = m.id1)';
    end if;

    if lod >= 4 then
        -- room
		execute immediate 'update room r
		set r.lod4_geometry_id = null
		where r.building_id in
			(select b.id from match_result_relevant m, building b
			where b.building_root_id = m.id1)';

        -- building furniture (in rooms) --bei lod r in f geändert
		execute immediate 'update building_furniture f
		set f.lod4_geometry_id = null
		where f.room_id in
	        (select r.id from match_result_relevant m, room r, building b
	        where r.building_id = b.id
	        	and b.building_root_id = m.id1)';

    	-- retrieve relevant internal (or external) building installation geometry (in rooms)
		execute immediate 'update building_installation i
		set i.lod4_geometry_id = null
		where i. room_id in
			(select r.id from match_result_relevant m, building b, room r
			where r.building_id = b.id
                and b.building_root_id = m.id1)';

	    -- retrieve surfaces from relevant thematic surfaces (in rooms)
          execute immediate 'update thematic_surface t
          set t.lod4_multi_surface_id = null
          where t.room_id in
          	(select r.id from match_result_relevant m, building b, room r
          	where r.building_id = b.id
                and b.building_root_id = m.id1)';

        -- monster join to retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
	    execute immediate 'update opening o
	    set o.lod4_multi_surface_id = null
	    where o.id in
	    	(select ot.opening_id from match_result_relevant m, thematic_surface t, building b, opening_to_them_surface ot, room r
	        where r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id)';

    	-- retrieve relevant internal building installation geometry
        execute immediate 'update building_installation i
        set i.lod4_geometry_id = null
        where i.is_external = 0
        	and i.building_id in
        		(select b.id from match_result_relevant m, building b
        		where b.building_root_id = m.id1)';
    end if;
exception
    when others then
      dbms_output.put_line('collect_all_geometry: ' || SQLERRM);
end;




procedure create_and_put_container(lod number, name_mode number, delimiter varchar2)
is
begin

	execute immediate 'truncate table container_ids';
	--execute immediate 'create table container_ids (building_id number, container_id number)';

	declare
		cursor building_id_cur is
	  		select unique (id1)
	  		from match_result_relevant;
	  		--select unique (building_id)
	  		--from collect_geom;

	begin
	    -- go through all affected buildings
	    for building_id_rec in building_id_cur loop
	        -- create geometry id
	    	execute immediate 'insert into container_ids (building_id, container_id)
	    		values (:1, surface_geometry_seq.nextval)'
	    		using building_id_rec.id1;
	    	-- create multisurface in table
	    	execute immediate 'insert into surface_geometry (id, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry)
	    		values (surface_geometry_seq.currval, null, surface_geometry_seq.currval, 0, 0, 0, 0, 0, null)';
			-- set building geometry to new multisurface and process name
			if name_mode=1 then
				-- ignore cand name
				execute immediate 'update building b
					set b.lod'||to_char(lod)||'_geometry_id = surface_geometry_seq.currval
					where b.id = (select id2 from match_result_relevant where id1 = :1)'
					using building_id_rec.id1;
			else
				if name_mode=2 then
				    -- replace master name with cand name
					execute immediate 'update building b
						set b.lod'||to_char(lod)||'_geometry_id = surface_geometry_seq.currval,
							b.name = (select name from building where id = :1)
						where b.id = (select id2 from match_result_relevant where id1 = :2)'
						using building_id_rec.id1, building_id_rec.id1;
				else
					-- append cand name to master
					execute immediate 'update building b
						set b.lod'||to_char(lod)||'_geometry_id = surface_geometry_seq.currval,
							b.name = concat(b.name, concat(:1, (select name from building where id = :2)))
						where b.id = (select id2 from match_result_relevant where id1 = :3)'
						using delimiter, building_id_rec.id1, building_id_rec.id1;
				end if;
			end if;
	    end loop;
    end;
exception
    when others then
      execute immediate 'select :1 as message from dual' using 'create_and_put_container: ' || SQLERRM;
      --dbms_output.put_line('create_and_put_container: ' || SQLERRM);
end;


procedure move_appearance
is
	texcoord_count number;
begin

	execute immediate 'select count(*)
				from textureparam tp, surface_geometry sg
				where tp.texture_coordinates is not null
					and sg.parent_id in (select geometry_id from collect_geom)
					and tp.surface_geometry_id = sg.parent_id' into texcoord_count;

	if texcoord_count>0 then
		execute immediate 'SELECT :1 as message from DUAL' using 'Warning: there are texcoords applied to multisurfaces that will be lost!';
	end if;

	execute immediate 'insert into textureparam tp (surface_geometry_id, is_texture_parametrization, world_to_texture, texture_coordinates, surface_data_id)
		values (select sg.id, tp.is_texture_parametrization, tp.world_to_texture, tp.texture_coordinates, tp.surface_data_id
				from textureparam tp, surface_geometry sg
				where tp.texture_coordinates is null
					and sg.parent_id in (select geometry_id from collect_geom)
					and tp.surface_geometry_id = sg.parent_id)';

	declare
		cursor theme_cur is
	  		select a.theme, cg.building_id
	  		from appearance a, collect_geom cg
			where a.cityobject_id = cg.cityobject_id
			group by a.theme, cg.building_id;

	begin
	    -- go through all themes
	    for theme_rec in theme_cur loop

	        -- 1: neue Appearances in Zielgebäude erzeugen
	    	execute immediate 'insert into appearance (id, name, name_codespace, description, theme, citymodel_id, cityobject_id)
	    		values (appearance_seq.nextval, null, null, null, :1, null, :2);'
	    		using theme_rec.theme, theme_rec.building_id;

			-- 2: Appear_to_surface_data auf neue appearances umhängen
			execute immediate 'update appear_to_surface_data asd
				set asd.appearance_id = appearance_seq.currval
				where asd.surface_data_id in
					(select tp.surface_data_id from textureparam tp, surface_geometry sg, collect_geom cg
					where tp.surface_geometry_id = sg.id
						and sg.root_id = cg.geometry_id
						and cg.building_id = :1)'
	    			using theme_rec.building_id;
		end loop;
    end;

exception
	when others then
      	dbms_output.put_line('create_and_put_container: ' || SQLERRM);
end;


procedure move_geometry
is
begin
	-- update parent of immediate children of all collected geometries
	execute immediate 'update surface_geometry s
	set s.parent_id = (select c.container_id from container_ids c, collect_geom g
					   where s.parent_id = g.geometry_id and c.building_id = g.building_id)
	where s.parent_id in (select geometry_id from collect_geom)';

	-- update all root_ids AND REMOVE SOLID FLAG SINCE WE THROW EVERYTHING INTO A MULTISURFACE
	execute immediate 'update surface_geometry s
	set s.root_id = (select c.container_id from container_ids c, collect_geom g
					   where s.root_id = g.geometry_id and c.building_id = g.building_id),
		s.is_solid = 0
	where s.root_id in (select geometry_id from collect_geom) and s.root_id<>s.id';
exception
    when others then
      dbms_output.put_line('move_geometry: ' || SQLERRM);
end;

procedure delete_multi_surfaces
is
begin
	execute immediate 'delete from surface_geometry
		where id in (select geometry_id from collect_geom)';
end;

procedure update_lineage(lineage varchar2)
is
begin
--	execute immediate 'update cityobject
--		set lineage = :1
--		where id in (select cityobject_id from collect_geom)'
--	using lineage;

	execute immediate 'update cityobject c
		set c.lineage = :1
		where c.id in (select b.id from building b, match_result_relevant m where b.building_root_id = m.id1)'
		using lineage;

--    if lod >= 2 then
    	-- retrieve relevant building installation geometry
     	execute immediate 'update cityobject c
     		set c.lineage = :1
     		where c.id in (select i.id from building_installation i, building b, match_result_relevant m
     			where i.building_id = b.id
     				and b.building_root_id = m.id1
     				and i.is_external = 1)'
     		using lineage;

	    -- retrieve surfaces from relevant thematic surfaces
	    execute immediate 'update cityobject c
			set c.lineage = :1
			where c.id in (select t.id from thematic_surface t, building b, match_result_relevant m
				where t.building_id = b.id
					and b.building_root_id = m.id1)'
			using lineage;
--    end if;

--    if lod >= 3 then
        -- monster join to retrieve all openings of all thematic surfaces beloning to all buildings and building parts
     	execute immediate 'update cityobject c
		set c.lineage = :1
		where c.id in (select o.id from opening o, match_result_relevant m, thematic_surface t, building b, opening_to_them_surface ot
			where o.id = ot.opening_id
				and ot.thematic_surface_id = t.id
				and t.building_id = b.id
				and b.building_root_id = m.id1)'
		using lineage;
--    end if;

--    if lod >= 4 then
        -- room
		execute immediate 'update cityobject c
		set c.lineage = :1
		where c.id in (select r.id from room r, match_result_relevant m, building b
			where r.building_id = b.id
				and b.building_root_id = m.id1)'
		using lineage;

        -- building furniture (in rooms) --bei lod r in f geändert
		execute immediate 'update cityobject c
		set c.lineage = :1
		where c.id in (select f.id from building_furniture f, match_result_relevant m, room r, building b
	        where f.room_id = r.id
				and r.building_id = b.id
	        	and b.building_root_id = m.id1)'
		using lineage;

    	-- retrieve relevant internal (or external) building installation geometry (in rooms)
		execute immediate 'update cityobject c
		set c.lineage = :1
		where c.id in (select i.id from building_installation i, match_result_relevant m, building b, room r
			where i.room_id = r.id
				and r.building_id = b.id
                and b.building_root_id = m.id1)'
		using lineage;

	    -- retrieve surfaces from relevant thematic surfaces (in rooms)
        execute immediate 'update cityobject c
		set c.lineage = :1
			where c.id in (select t.id from thematic_surface t, match_result_relevant m, building b, room r
          		where t.room_id = r.id
          		and r.building_id = b.id
                and b.building_root_id = m.id1)'
		using lineage;

        -- monster join to retrieve all openings of all thematic surfaces beloning to all rooms in all buildings and building parts
	    execute immediate 'update cityobject c
	    set c.lineage = :1
	    where c.id in (select o.id from opening o, match_result_relevant m, thematic_surface t, building b, opening_to_them_surface ot, room r
	        where o.id = ot.opening_id
	    		and r.building_id = b.id
                and t.room_id = r.id
                and b.building_root_id = m.id1
                and ot.thematic_surface_id = t.id)'
		using lineage;

    	-- retrieve relevant internal building installation geometry
        execute immediate 'update cityobject c
        set c.lineage = :1
        where c.id in (select i.id from building_installation i, match_result_relevant m, building b
        	where i.building_id = b.id
        		and b.building_root_id = m.id1
        		and i.is_external = 0)'
		using lineage;
--    end if;
exception
    when others then
      dbms_output.put_line('collect_all_geometry: ' || SQLERRM);
end;



procedure cleanup
is
begin

    -- cleanup
    execute immediate 'truncate table collect_geom';
    execute immediate 'truncate table container_ids';
    --execute immediate 'truncate table match_result_relevant';

exception
    when others then
      dbms_output.put_line('cleanup: ' || SQLERRM);
end;

procedure process_matches(delta1 number, delta2 number, lod_src number, lod_dst number, name_mode number, delimiter varchar2)
is
begin
    -- our algorithm
  	create_relevant_matches(delta1, delta2);
  	collect_all_geometry(lod_src);
	remove_geometry_from_cand(lod_src);
  	move_appearance();
  	create_and_put_container(lod_dst, name_mode, delimiter);
  	move_geometry();
  	delete_multi_surfaces();

    -- cleanup
    cleanup();

    commit;

exception
    when others then
      dbms_output.put_line('process_matches: ' || SQLERRM);
end;



END geodb_process_matches;
/
 
