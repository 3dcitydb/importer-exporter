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

CREATE OR REPLACE PACKAGE GEODB_DELETE_BY_LINEAGE
AS
	procedure delete_buildings(lineage varchar2);
END geodb_delete_by_lineage;
/
 

  CREATE OR REPLACE PACKAGE BODY GEODB_DELETE_BY_LINEAGE
AS

procedure delete_buildings(lineage varchar2)
is
begin

--// disable FK-constraints on SURFACE_GEOMETRY
execute immediate 'ALTER TABLE SURFACE_GEOMETRY
    DISABLE
    CONSTRAINT SURFACE_GEOMETRY_FK
    DISABLE
    CONSTRAINT SURFACE_GEOMETRY_FK1';

--// *********************************************************************
--// delete openings
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting openings...';

execute immediate 'ALTER TABLE OPENING
    DISABLE
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK
    DISABLE
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK1';

--// delete entries from OPENING_TO_THEM_SURFACE
execute immediate 'DELETE FROM OPENING_TO_THEM_SURFACE where opening_id in (select id from cityobject where lineage=:1)'
using lineage;

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD3_MULTI_SURFACE_ID FROM OPENING where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD4_MULTI_SURFACE_ID FROM OPENING where id in (select id from cityobject where lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD3_MULTI_SURFACE_ID FROM OPENING where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD4_MULTI_SURFACE_ID FROM OPENING where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from OPENING
execute immediate 'DELETE FROM OPENING where id in (select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE OPENING
    ENABLE
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK
    ENABLE
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK1';

--// *********************************************************************
--// delete addresses
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting addresses...';

execute immediate 'ALTER TABLE ADDRESS_TO_BUILDING
    DISABLE
    CONSTRAINT ADDRESS_TO_BUILDING_ADDRESS_FK
    DISABLE
    CONSTRAINT ADDRESS_TO_BUILDING_FK';

--// delete entries from ADDRESS
execute immediate 'DELETE FROM ADDRESS where id in
	(select address_id from address_to_building where building_id in
		(select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from ADDRESS_TO_BUILDING;
execute immediate 'DELETE FROM ADDRESS_TO_BUILDING where building_id in
		(select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE ADDRESS_TO_BUILDING
    ENABLE
    CONSTRAINT ADDRESS_TO_BUILDING_ADDRESS_FK
    ENABLE
    CONSTRAINT ADDRESS_TO_BUILDING_FK';

--// *********************************************************************
--// delete thematic surfaces
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting thematic surfaces...';

execute immediate 'ALTER TABLE THEMATIC_SURFACE
    DISABLE
    CONSTRAINT THEMATIC_SURFACE_FK
    DISABLE
    CONSTRAINT THEMATIC_SURFACE_FK1
    DISABLE
    CONSTRAINT THEMATIC_SURFACE_FK2';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD2_MULTI_SURFACE_ID FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD3_MULTI_SURFACE_ID FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD4_MULTI_SURFACE_ID FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD2_MULTI_SURFACE_ID FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD3_MULTI_SURFACE_ID FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD4_MULTI_SURFACE_ID FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from THEMATIC_SURFACE
execute immediate 'DELETE FROM THEMATIC_SURFACE where id in (select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE THEMATIC_SURFACE
    ENABLE
    CONSTRAINT THEMATIC_SURFACE_FK
    ENABLE
    CONSTRAINT THEMATIC_SURFACE_FK1
    ENABLE
    CONSTRAINT THEMATIC_SURFACE_FK2';

--// *********************************************************************
--// delete building installations
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting building installations...';

execute immediate 'ALTER TABLE BUILDING_INSTALLATION
    DISABLE
    CONSTRAINT BUILDING_INSTALLATION_FK2
    DISABLE
    CONSTRAINT BUILDING_INSTALLATION_FK3
    DISABLE
    CONSTRAINT BUILDING_INSTALLATION_FK4';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD2_GEOMETRY_ID FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD3_GEOMETRY_ID FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD2_GEOMETRY_ID FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD3_GEOMETRY_ID FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD4_GEOMETRY_ID FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from BUILDING_INSTALLATION
execute immediate 'DELETE FROM BUILDING_INSTALLATION where id in (select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE BUILDING_INSTALLATION
    ENABLE
    CONSTRAINT BUILDING_INSTALLATION_FK2
    ENABLE
    CONSTRAINT BUILDING_INSTALLATION_FK3
    ENABLE
    CONSTRAINT BUILDING_INSTALLATION_FK4';

--// *********************************************************************
--// delete building furniture
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting building furniture...';

execute immediate 'ALTER TABLE IMPLICIT_GEOMETRY
    DISABLE
    CONSTRAINT IMPLICIT_GEOMETRY_FK';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT RELATIVE_GEOMETRY_ID FROM IMPLICIT_GEOMETRY
					WHERE ID IN
						(SELECT LOD4_IMPLICIT_REP_ID FROM BUILDING_FURNITURE where id in (select id from cityobject where lineage=:1))))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT RELATIVE_GEOMETRY_ID FROM IMPLICIT_GEOMETRY
			WHERE ID IN
				(SELECT LOD4_IMPLICIT_REP_ID FROM BUILDING_FURNITURE where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'ALTER TABLE BUILDING_FURNITURE
    DISABLE
    CONSTRAINT BUILDING_FURNITURE_FK
    DISABLE
    CONSTRAINT BUILDING_FURNITURE_FK2';

--// delete entries from IMPLICIT_GEOMETRY
execute immediate 'DELETE FROM IMPLICIT_GEOMETRY
	WHERE ID IN
		(SELECT LOD4_IMPLICIT_REP_ID FROM BUILDING_FURNITURE where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM BUILDING_FURNITURE where id in (select id from cityobject where lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD4_GEOMETRY_ID FROM BUILDING_FURNITURE where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from BUILDING_FURNITURE
execute immediate 'DELETE FROM BUILDING_FURNITURE where id in (select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE BUILDING_FURNITURE
    ENABLE
    CONSTRAINT BUILDING_FURNITURE_FK
    ENABLE
    CONSTRAINT BUILDING_FURNITURE_FK2';

execute immediate 'ALTER TABLE IMPLICIT_GEOMETRY
    ENABLE
    CONSTRAINT IMPLICIT_GEOMETRY_FK';

--// *********************************************************************
--// delete rooms
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting rooms...';

execute immediate 'ALTER TABLE ROOM
    DISABLE
    CONSTRAINT ROOM_SURFACE_GEOMETRY_FK';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM ROOM where id in (select id from cityobject where lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD4_GEOMETRY_ID FROM ROOM where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from ROOM
execute immediate 'DELETE FROM ROOM where id in (select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE ROOM
    ENABLE
    CONSTRAINT ROOM_SURFACE_GEOMETRY_FK';

--// *********************************************************************
--// delete buildings
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting buildings...';

execute immediate 'ALTER TABLE BUILDING
    DISABLE
    CONSTRAINT BUILDING_BUILDING_FK
    DISABLE
    CONSTRAINT BUILDING_BUILDING_FK1
    DISABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK
    DISABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK1
    DISABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK2
    DISABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK3';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD1_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD2_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD3_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1)))'
using lineage;

execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD1_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD2_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD3_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1))'
using lineage;

execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT LOD4_GEOMETRY_ID FROM BUILDING where id in (select id from cityobject where lineage=:1))'
using lineage;

--// delete entries from BUILDING
execute immediate 'DELETE FROM BUILDING where id in (select id from cityobject where lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE BUILDING
    ENABLE
    CONSTRAINT BUILDING_BUILDING_FK
    ENABLE
    CONSTRAINT BUILDING_BUILDING_FK1
    ENABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK
    ENABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK1
    ENABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK2
    ENABLE
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK3';

--// *********************************************************************
--// delete external_references
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting external references...';

--// delete entries from EXTERNAL_REFERENCE
execute immediate 'DELETE FROM EXTERNAL_REFERENCE
       WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

--// *********************************************************************
--// delete generic attributes
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting generic attributes...';

execute immediate 'ALTER TABLE CITYOBJECT_GENERICATTRIB
    DISABLE
    CONSTRAINT CITYOBJECT_GENERICATTRIB_FK1';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECT_GENERICATTRIB
					WHERE CITYOBJECT_ID IN
						(SELECT ID FROM CITYOBJECT
							WHERE (CLASS_ID=24
							OR CLASS_ID=25
							OR CLASS_ID=26
							OR CLASS_ID=27
							OR CLASS_ID=28
							OR CLASS_ID=29
							OR CLASS_ID=30
							OR CLASS_ID=31
							OR CLASS_ID=32
							OR CLASS_ID=33
							OR CLASS_ID=34
							OR CLASS_ID=35
							OR CLASS_ID=36
							OR CLASS_ID=37
							OR CLASS_ID=38
							OR CLASS_ID=39
							OR CLASS_ID=40
							OR CLASS_ID=41
                 			OR CLASS_ID=58)
                 			and lineage=:1)))'
using lineage;

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECT_GENERICATTRIB
			WHERE CITYOBJECT_ID IN
				(SELECT ID FROM CITYOBJECT
					WHERE (CLASS_ID=24
					OR CLASS_ID=25
					OR CLASS_ID=26
					OR CLASS_ID=27
					OR CLASS_ID=28
					OR CLASS_ID=29
					OR CLASS_ID=30
					OR CLASS_ID=31
					OR CLASS_ID=32
					OR CLASS_ID=33
					OR CLASS_ID=34
					OR CLASS_ID=35
					OR CLASS_ID=36
					OR CLASS_ID=37
					OR CLASS_ID=38
					OR CLASS_ID=39
					OR CLASS_ID=40
					OR CLASS_ID=41
                 	OR CLASS_ID=58)
                 	and lineage=:1))'
using lineage;

--// delete entries from CITYOBJECT_GENERICATTRIB
execute immediate 'DELETE FROM CITYOBJECT_GENERICATTRIB
       WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

execute immediate 'ALTER TABLE CITYOBJECT_GENERICATTRIB
    ENABLE
    CONSTRAINT CITYOBJECT_GENERICATTRIB_FK1';

--// *********************************************************************
--// delete generalizations
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting generalization hierarchies...';

--// delete entries from GENERALIZATION
execute immediate 'DELETE FROM GENERALIZATION
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

execute immediate 'DELETE FROM GENERALIZATION
	WHERE GENERALIZES_TO_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

--// *********************************************************************
--// delete city object groups
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting city object groups...';

--// delete entries from GROUP_TO_CITYOBJECT
execute immediate 'DELETE FROM GROUP_TO_CITYOBJECT
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

execute immediate 'DELETE FROM GROUP_TO_CITYOBJECT
	WHERE CITYOBJECTGROUP_ID IN
		(SELECT ID FROM CITYOBJECTGROUP
			WHERE PARENT_CITYOBJECT_ID IN
				(SELECT ID FROM CITYOBJECT
			              WHERE (CLASS_ID=24
			              	 OR CLASS_ID=25
			                 OR CLASS_ID=26
			                 OR CLASS_ID=27
			                 OR CLASS_ID=28
			                 OR CLASS_ID=29
			                 OR CLASS_ID=30
			                 OR CLASS_ID=31
			                 OR CLASS_ID=32
			                 OR CLASS_ID=33
			                 OR CLASS_ID=34
			                 OR CLASS_ID=35
			                 OR CLASS_ID=36
			                 OR CLASS_ID=37
			                 OR CLASS_ID=38
			                 OR CLASS_ID=39
			                 OR CLASS_ID=40
			                 OR CLASS_ID=41
                 			 OR CLASS_ID=58)
							and lineage=:1))'
using lineage;

execute immediate 'ALTER TABLE CITYOBJECTGROUP
    DISABLE
    CONSTRAINT CITYOBJECT_GROUP_FK';

--// delete entries from TEXTUREPARAM
execute immediate 'DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY
			WHERE ROOT_ID IN
				(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECTGROUP
					WHERE (SELECT COUNT(*) FROM GROUP_TO_CITYOBJECT
						WHERE CITYOBJECTGROUP_ID = CITYOBJECTGROUP.ID) = 0))';

--// delete entries from SURFACE_GEOMETRY
execute immediate 'DELETE FROM SURFACE_GEOMETRY
	WHERE ROOT_ID IN
		(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECTGROUP
			WHERE (SELECT COUNT(*) FROM GROUP_TO_CITYOBJECT
				WHERE CITYOBJECTGROUP_ID = CITYOBJECTGROUP.ID) = 0)';

--// delete entries from CITYOBJECTGROUP
execute immediate 'DELETE FROM CITYOBJECTGROUP
	WHERE (SELECT COUNT(*) FROM GROUP_TO_CITYOBJECT
		WHERE CITYOBJECTGROUP_ID = CITYOBJECTGROUP.ID) = 0';

execute immediate 'ALTER TABLE CITYOBJECTGROUP
    ENABLE
    CONSTRAINT CITYOBJECT_GROUP_FK';

--// *********************************************************************
--// delete appearances
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting appearances...';

execute immediate 'DELETE APPEAR_TO_SURFACE_DATA
	WHERE SURFACE_DATA_ID IN
		(SELECT ID FROM SURFACE_DATA
			WHERE (SELECT COUNT(*) FROM TEXTUREPARAM
				WHERE SURFACE_DATA_ID = SURFACE_DATA.ID) = 0)';

--// delete entries from SURFACE_DATA
execute immediate 'DELETE SURFACE_DATA
	WHERE (SELECT COUNT(*) FROM TEXTUREPARAM
		WHERE SURFACE_DATA_ID = SURFACE_DATA.ID) = 0';

--// delete entries from APPEARANCE
execute immediate 'DELETE APPEARANCE
	WHERE (SELECT COUNT(*) FROM APPEAR_TO_SURFACE_DATA
		WHERE APPEARANCE_ID = APPEARANCE.ID) = 0';

execute immediate 'DELETE FROM APPEARANCE
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

--// *********************************************************************
--// delete city object members
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting city object members...';

--// delete entries from CITYOBJECT_MEMBER
execute immediate 'DELETE FROM CITYOBJECT_MEMBER
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE (CLASS_ID=24
              	 OR CLASS_ID=25
                 OR CLASS_ID=26
                 OR CLASS_ID=27
                 OR CLASS_ID=28
                 OR CLASS_ID=29
                 OR CLASS_ID=30
                 OR CLASS_ID=31
                 OR CLASS_ID=32
                 OR CLASS_ID=33
                 OR CLASS_ID=34
                 OR CLASS_ID=35
                 OR CLASS_ID=36
                 OR CLASS_ID=37
                 OR CLASS_ID=38
                 OR CLASS_ID=39
                 OR CLASS_ID=40
                 OR CLASS_ID=41
                 OR CLASS_ID=58)
                 and lineage=:1)'
using lineage;

--// *********************************************************************
--// delete city models
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting city models...';

--// delete entries from APPEARANCE
execute immediate 'DELETE FROM APPEARANCE
	WHERE CITYMODEL_ID IN
		(SELECT ID FROM CITYMODEL
			WHERE (SELECT COUNT(*) FROM CITYOBJECT_MEMBER
				WHERE CITYMODEL_ID = CITYMODEL.ID) = 0)';

--// delete entries from CITYMODEL
execute immediate 'DELETE FROM CITYMODEL
	WHERE (SELECT COUNT(*) FROM CITYOBJECT_MEMBER
		WHERE CITYMODEL_ID = CITYMODEL.ID) = 0';

--// *********************************************************************
--// delete city objects
--// *********************************************************************
execute immediate 'SELECT :1 as message from DUAL'
using 'Deleting city objects...';

--// delete entries from CITYOBJECT
execute immediate 'DELETE FROM CITYOBJECT
	WHERE (CLASS_ID=24
		OR CLASS_ID=25
		OR CLASS_ID=26
		OR CLASS_ID=27
		OR CLASS_ID=28
		OR CLASS_ID=29
		OR CLASS_ID=30
		OR CLASS_ID=31
		OR CLASS_ID=32
		OR CLASS_ID=33
		OR CLASS_ID=34
		OR CLASS_ID=35
		OR CLASS_ID=36
		OR CLASS_ID=37
		OR CLASS_ID=38
		OR CLASS_ID=39
		OR CLASS_ID=40
		OR CLASS_ID=41
		OR CLASS_ID=58)
		and lineage=:1'
using lineage;

--// enable FK-constraints on SURFACE_GEOMETRY
execute immediate 'ALTER TABLE SURFACE_GEOMETRY
    ENABLE
    CONSTRAINT SURFACE_GEOMETRY_FK
    ENABLE
    CONSTRAINT SURFACE_GEOMETRY_FK1';

execute immediate 'SELECT :1 as message from DUAL'
using 'Deletion of buildings complete!';

exception
	when others then
		dbms_output.put_line('delete_lineage: ' || SQLERRM);

end;


END geodb_delete_by_lineage;
/