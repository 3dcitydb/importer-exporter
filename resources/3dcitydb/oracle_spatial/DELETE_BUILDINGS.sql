-- DELETE_BUILDINGS.sql
-- 
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008, Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This script is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About: This script deletes all building instances from the table 
-- BUILDING within the current workspace. This also affects all related 
-- tables within the database schema.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.0.0     2007-12-20   release version                             TKol
--                                                                    GKoe
--                                                                    CNag
--                                                                    ASta
--

--// disable FK-constraints on SURFACE_GEOMETRY
ALTER TABLE SURFACE_GEOMETRY
    DISABLE 
    CONSTRAINT SURFACE_GEOMETRY_FK 
    DISABLE 
    CONSTRAINT SURFACE_GEOMETRY_FK1;
    
--// *********************************************************************
--// delete openings
--// *********************************************************************
SELECT 'Deleting openings...' as message from DUAL;

ALTER TABLE OPENING
    DISABLE 
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK
    DISABLE 
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK1;

--// delete entries from OPENING_TO_THEM_SURFACE
DELETE FROM OPENING_TO_THEM_SURFACE;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD3_MULTI_SURFACE_ID FROM OPENING));
		
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD4_MULTI_SURFACE_ID FROM OPENING));

--// delete entries from SURFACE_GEOMETRY    
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD3_MULTI_SURFACE_ID FROM OPENING); 

DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD4_MULTI_SURFACE_ID FROM OPENING); 
	
--// delete entries from OPENING
DELETE FROM OPENING;

ALTER TABLE OPENING
    ENABLE 
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK
    ENABLE 
    CONSTRAINT OPENING_SURFACE_GEOMETRY_FK1;

--// *********************************************************************
--// delete addresses
--// *********************************************************************
SELECT 'Deleting addresses...' as message from DUAL;

--// delete entries from ADDRESS_TO_BUILDING;
DELETE FROM ADDRESS_TO_BUILDING;

--// delete entries from ADDRESS 
DELETE FROM ADDRESS;

--// *********************************************************************
--// delete thematic surfaces
--// *********************************************************************
SELECT 'Deleting thematic surfaces...' as message from DUAL;

ALTER TABLE THEMATIC_SURFACE
    DISABLE 
    CONSTRAINT THEMATIC_SURFACE_FK
    DISABLE 
    CONSTRAINT THEMATIC_SURFACE_FK1
    DISABLE 
    CONSTRAINT THEMATIC_SURFACE_FK2;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD2_MULTI_SURFACE_ID FROM THEMATIC_SURFACE));

DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD3_MULTI_SURFACE_ID FROM THEMATIC_SURFACE));
				
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD4_MULTI_SURFACE_ID FROM THEMATIC_SURFACE));				

--// delete entries from SURFACE_GEOMETRY    
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD2_MULTI_SURFACE_ID FROM THEMATIC_SURFACE); 

DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD3_MULTI_SURFACE_ID FROM THEMATIC_SURFACE);
		
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD4_MULTI_SURFACE_ID FROM THEMATIC_SURFACE);		

--// delete entries from THEMATIC_SURFACE
DELETE FROM THEMATIC_SURFACE;

ALTER TABLE THEMATIC_SURFACE
    ENABLE 
    CONSTRAINT THEMATIC_SURFACE_FK
    ENABLE 
    CONSTRAINT THEMATIC_SURFACE_FK1
    ENABLE 
    CONSTRAINT THEMATIC_SURFACE_FK2;

--// *********************************************************************
--// delete building installations
--// *********************************************************************
SELECT 'Deleting building installations...' as message from DUAL;

ALTER TABLE BUILDING_INSTALLATION
    DISABLE 
    CONSTRAINT BUILDING_INSTALLATION_FK2
    DISABLE 
    CONSTRAINT BUILDING_INSTALLATION_FK3
    DISABLE 
    CONSTRAINT BUILDING_INSTALLATION_FK4;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD2_GEOMETRY_ID FROM BUILDING_INSTALLATION));

DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD3_GEOMETRY_ID FROM BUILDING_INSTALLATION));
				
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM BUILDING_INSTALLATION));

--// delete entries from SURFACE_GEOMETRY    
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD2_GEOMETRY_ID FROM BUILDING_INSTALLATION); 

DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD3_GEOMETRY_ID FROM BUILDING_INSTALLATION);
		
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD4_GEOMETRY_ID FROM BUILDING_INSTALLATION);

--// delete entries from BUILDING_INSTALLATION
DELETE FROM BUILDING_INSTALLATION;

ALTER TABLE BUILDING_INSTALLATION
    ENABLE 
    CONSTRAINT BUILDING_INSTALLATION_FK2
    ENABLE 
    CONSTRAINT BUILDING_INSTALLATION_FK3
    ENABLE 
    CONSTRAINT BUILDING_INSTALLATION_FK4;

--// *********************************************************************
--// delete building furniture
--// *********************************************************************
SELECT 'Deleting building furniture...' as message from DUAL;

ALTER TABLE IMPLICIT_GEOMETRY
    DISABLE 
    CONSTRAINT IMPLICIT_GEOMETRY_FK;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT RELATIVE_GEOMETRY_ID FROM IMPLICIT_GEOMETRY
					WHERE ID IN
						(SELECT LOD4_IMPLICIT_REP_ID FROM BUILDING_FURNITURE)));

--// delete entries from SURFACE_GEOMETRY
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT RELATIVE_GEOMETRY_ID FROM IMPLICIT_GEOMETRY
			WHERE ID IN
				(SELECT LOD4_IMPLICIT_REP_ID FROM BUILDING_FURNITURE));
				
ALTER TABLE BUILDING_FURNITURE
    DISABLE 
    CONSTRAINT BUILDING_FURNITURE_FK
    DISABLE 
    CONSTRAINT BUILDING_FURNITURE_FK2;				

--// delete entries from IMPLICIT_GEOMETRY
DELETE FROM IMPLICIT_GEOMETRY
	WHERE ID IN
		(SELECT LOD4_IMPLICIT_REP_ID FROM BUILDING_FURNITURE);

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM BUILDING_FURNITURE));

--// delete entries from SURFACE_GEOMETRY
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD4_GEOMETRY_ID FROM BUILDING_FURNITURE); 

--// delete entries from BUILDING_FURNITURE
DELETE FROM BUILDING_FURNITURE;

ALTER TABLE BUILDING_FURNITURE
    ENABLE 
    CONSTRAINT BUILDING_FURNITURE_FK
    ENABLE 
    CONSTRAINT BUILDING_FURNITURE_FK2;	

ALTER TABLE IMPLICIT_GEOMETRY
    ENABLE 
    CONSTRAINT IMPLICIT_GEOMETRY_FK;

--// *********************************************************************
--// delete rooms
--// *********************************************************************
SELECT 'Deleting rooms...' as message from DUAL;

ALTER TABLE ROOM
    DISABLE 
    CONSTRAINT ROOM_SURFACE_GEOMETRY_FK;	

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM ROOM));

--// delete entries from SURFACE_GEOMETRY    
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD4_GEOMETRY_ID FROM ROOM);

--// delete entries from ROOM
DELETE FROM ROOM;

ALTER TABLE ROOM
    ENABLE 
    CONSTRAINT ROOM_SURFACE_GEOMETRY_FK;	

--// *********************************************************************
--// delete buildings
--// *********************************************************************
SELECT 'Deleting buildings...' as message from DUAL;

ALTER TABLE BUILDING
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
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK3;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD1_GEOMETRY_ID FROM BUILDING));

DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD2_GEOMETRY_ID FROM BUILDING));

DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD3_GEOMETRY_ID FROM BUILDING));

DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT LOD4_GEOMETRY_ID FROM BUILDING));

--// delete entries from SURFACE_GEOMETRY    
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD1_GEOMETRY_ID FROM BUILDING);

DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD2_GEOMETRY_ID FROM BUILDING);
		
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD3_GEOMETRY_ID FROM BUILDING);
		
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT LOD4_GEOMETRY_ID FROM BUILDING);

--// delete entries from BUILDING
DELETE FROM BUILDING;

ALTER TABLE BUILDING
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
    CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK3;

--// *********************************************************************
--// delete external_references
--// *********************************************************************
SELECT 'Deleting external references...' as message from DUAL;

--// delete entries from EXTERNAL_REFERENCE
DELETE FROM EXTERNAL_REFERENCE
       WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58);

--// *********************************************************************
--// delete generic attributes
--// *********************************************************************
SELECT 'Deleting generic attributes...' as message from DUAL;

ALTER TABLE CITYOBJECT_GENERICATTRIB
    DISABLE 
    CONSTRAINT CITYOBJECT_GENERICATTRIB_FK1;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECT_GENERICATTRIB
					WHERE CITYOBJECT_ID IN
						(SELECT ID FROM CITYOBJECT
							WHERE CLASS_ID=24
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
                 					OR CLASS_ID=58)));

--// delete entries from SURFACE_GEOMETRY
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECT_GENERICATTRIB
			WHERE CITYOBJECT_ID IN
				(SELECT ID FROM CITYOBJECT
					WHERE CLASS_ID=24
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
                 			OR CLASS_ID=58));

--// delete entries from CITYOBJECT_GENERICATTRIB
DELETE FROM CITYOBJECT_GENERICATTRIB
       WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58);

ALTER TABLE CITYOBJECT_GENERICATTRIB
    ENABLE 
    CONSTRAINT CITYOBJECT_GENERICATTRIB_FK1;

--// *********************************************************************
--// delete generalizations
--// *********************************************************************
SELECT 'Deleting generalization hierarchies...' as message from DUAL;

--// delete entries from GENERALIZATION
DELETE FROM GENERALIZATION
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58);	

DELETE FROM GENERALIZATION
	WHERE GENERALIZES_TO_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58);

--// *********************************************************************
--// delete city object groups
--// *********************************************************************
SELECT 'Deleting city object groups...' as message from DUAL;

--// delete entries from GROUP_TO_CITYOBJECT
DELETE FROM GROUP_TO_CITYOBJECT 
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58);                              
                
DELETE FROM GROUP_TO_CITYOBJECT
	WHERE CITYOBJECTGROUP_ID IN
		(SELECT ID FROM CITYOBJECTGROUP 
			WHERE PARENT_CITYOBJECT_ID IN
				(SELECT ID FROM CITYOBJECT
			              WHERE CLASS_ID=24
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
                 			 OR CLASS_ID=58));                
                
ALTER TABLE CITYOBJECTGROUP
    DISABLE 
    CONSTRAINT CITYOBJECT_GROUP_FK;

--// delete entries from TEXTUREPARAM
DELETE FROM TEXTUREPARAM
	WHERE SURFACE_GEOMETRY_ID IN
		(SELECT ID FROM SURFACE_GEOMETRY 
			WHERE ROOT_ID IN
				(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECTGROUP
					WHERE (SELECT COUNT(*) FROM GROUP_TO_CITYOBJECT
						WHERE CITYOBJECTGROUP_ID = CITYOBJECTGROUP.ID) = 0));

--// delete entries from SURFACE_GEOMETRY
DELETE FROM SURFACE_GEOMETRY 
	WHERE ROOT_ID IN 
		(SELECT SURFACE_GEOMETRY_ID FROM CITYOBJECTGROUP
			WHERE (SELECT COUNT(*) FROM GROUP_TO_CITYOBJECT
				WHERE CITYOBJECTGROUP_ID = CITYOBJECTGROUP.ID) = 0);

--// delete entries from CITYOBJECTGROUP	
DELETE FROM CITYOBJECTGROUP
	WHERE (SELECT COUNT(*) FROM GROUP_TO_CITYOBJECT
		WHERE CITYOBJECTGROUP_ID = CITYOBJECTGROUP.ID) = 0;

ALTER TABLE CITYOBJECTGROUP
    ENABLE 
    CONSTRAINT CITYOBJECT_GROUP_FK;

--// *********************************************************************
--// delete appearances
--// *********************************************************************
SELECT 'Deleting appearances...' as message from DUAL;

DELETE APPEAR_TO_SURFACE_DATA
	WHERE SURFACE_DATA_ID IN
		(SELECT ID FROM SURFACE_DATA
			WHERE (SELECT COUNT(*) FROM TEXTUREPARAM
				WHERE SURFACE_DATA_ID = SURFACE_DATA.ID) = 0);

--// delete entries from SURFACE_DATA				
DELETE SURFACE_DATA
	WHERE (SELECT COUNT(*) FROM TEXTUREPARAM
		WHERE SURFACE_DATA_ID = SURFACE_DATA.ID) = 0;

--// delete entries from APPEARANCE
DELETE APPEARANCE
	WHERE (SELECT COUNT(*) FROM APPEAR_TO_SURFACE_DATA
		WHERE APPEARANCE_ID = APPEARANCE.ID) = 0;

DELETE FROM APPEARANCE
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58);  

--// *********************************************************************
--// delete city object members
--// *********************************************************************
SELECT 'Deleting city object members...' as message from DUAL;

--// delete entries from CITYOBJECT_MEMBER
DELETE FROM CITYOBJECT_MEMBER
	WHERE CITYOBJECT_ID IN
             (SELECT ID FROM CITYOBJECT
              WHERE CLASS_ID=24
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
                 OR CLASS_ID=58); 

--// *********************************************************************
--// delete city models
--// *********************************************************************
SELECT 'Deleting city models...' as message from DUAL;

--// delete entries from APPEARANCE
DELETE FROM APPEARANCE
	WHERE CITYMODEL_ID IN
		(SELECT ID FROM CITYMODEL
			WHERE (SELECT COUNT(*) FROM CITYOBJECT_MEMBER
				WHERE CITYMODEL_ID = CITYMODEL.ID) = 0);

--// delete entries from CITYMODEL
DELETE FROM CITYMODEL
	WHERE (SELECT COUNT(*) FROM CITYOBJECT_MEMBER
		WHERE CITYMODEL_ID = CITYMODEL.ID) = 0; 

--// *********************************************************************
--// delete city objects
--// *********************************************************************
SELECT 'Deleting city objects...' as message from DUAL;

--// delete entries from CITYOBJECT
DELETE FROM CITYOBJECT
	WHERE CLASS_ID=24
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
                 OR CLASS_ID=58;

--// enable FK-constraints on SURFACE_GEOMETRY
ALTER TABLE SURFACE_GEOMETRY 
    ENABLE
    CONSTRAINT SURFACE_GEOMETRY_FK 
    ENABLE
    CONSTRAINT SURFACE_GEOMETRY_FK1;
    
SELECT 'Deletion of buildings complete!' as message from DUAL;    