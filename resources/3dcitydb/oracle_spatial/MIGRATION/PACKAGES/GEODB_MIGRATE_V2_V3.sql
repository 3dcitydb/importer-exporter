CREATE OR REPLACE PACKAGE geodb_migrate_v2_v3
AS
  PROCEDURE fillSurfaceGeometryTable;
  PROCEDURE fillCityObjectTable;
  PROCEDURE fillCityModelTable;
  PROCEDURE fillAddressTable;
  PROCEDURE fillBuildingTable;
  PROCEDURE fillAddressToBuildingTable;
  PROCEDURE fillAppearanceTable;
  PROCEDURE fillSurfaceDataTable;
  PROCEDURE fillAppearToSurfaceDataTable;
  PROCEDURE fillBreaklineReliefTable;
  PROCEDURE fillRoomTable;
  PROCEDURE fillBuildingFurnitureTable;  
  PROCEDURE fillBuildingInstallationTable;
  PROCEDURE fillImplicitGeometryTable;
  PROCEDURE fillCityFurnitureTable;
  PROCEDURE fillCityObjectGenAttrTable;
  PROCEDURE fillCityObjectMemberTable;
  PROCEDURE fillCityObjectGroupTable;
  PROCEDURE fillExternalReferenceTable;
  PROCEDURE fillGeneralizationTable;
  PROCEDURE fillGenericCityObjectTable;
  PROCEDURE fillGroupToCityObject;
  PROCEDURE fillLandUseTable;
  PROCEDURE fillMassPointReliefTable;
  PROCEDURE fillOpeningTable;
  PROCEDURE fillThematicSurfaceTable;
  PROCEDURE fillOpeningToThemSurfaceTable;
  PROCEDURE fillPlantCoverTable;
  PROCEDURE fillReliefComponentTable;
  PROCEDURE fillRasterReliefTable;
  PROCEDURE fillReliefFeatToRelCompTable;
  PROCEDURE fillReliefFeatureTable;
  PROCEDURE fillSolitaryVegetatObjectTable;
  PROCEDURE fillTextureParamTable;
  PROCEDURE fillTinReliefTable;
  PROCEDURE fillTrafficAreaTable;
  PROCEDURE fillTransportationComplex;
  PROCEDURE fillWaterBodyTable;
  PROCEDURE fillWaterBoundarySurfaceTable;
  PROCEDURE fillWaterbodToWaterbndSrfTable;
  PROCEDURE updateSurfaceGeoTableCityObj;
END geodb_migrate_v2_v3;
/

CREATE OR REPLACE 
PACKAGE BODY geodb_migrate_v2_v3
AS

  PROCEDURE fillSurfaceGeometryTable
  IS
    -- variables --
    CURSOR surface_geometry_v2 IS select * from surface_geometry_v2 order by id;
    has_xlink NUMBER(1,0) := 0;
    is_solid NUMBER(1,0) := 0;
    solid_coordinates SDO_GEOMETRY;
  BEGIN
    dbms_output.put_line('Surface_Geometry table is being copied...');
    for surface_geometry in surface_geometry_v2 loop
        --  if the parent has xlink = 1,
        -- then insert the geometry into the implicit geometry column
        IF (surface_geometry.PARENT_ID IS NOT NULL) THEN
          EXECUTE IMMEDIATE 'select IS_XLINK from surface_geometry_v2 where
                             ID = '||surface_geometry.PARENT_ID INTO has_xlink;
        END IF;
        EXECUTE IMMEDIATE 'select IS_SOLID from surface_geometry_v2 where
                             ID = '||surface_geometry.ID INTO is_solid;
        IF (has_xlink = 1) THEN
          insert into surface_geometry
          (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE,
          IS_TRIANGULATED, IS_XLINK, IS_REVERSE, IMPLICIT_GEOMETRY)
          values
          (surface_geometry.ID, surface_geometry.GMLID, surface_geometry.PARENT_ID,
          surface_geometry.ROOT_ID, surface_geometry.IS_SOLID, surface_geometry.IS_COMPOSITE,
          surface_geometry.IS_TRIANGULATED, surface_geometry.IS_XLINK,
          surface_geometry.IS_REVERSE, surface_geometry.GEOMETRY);
        ELSIF (is_solid = 1) THEN
	   EXECUTE IMMEDIATE 'SELECT SDO_AGGR_UNION(
                                    MDSYS.SDOAGGRTYPE(s.geometry, 1000)) 
                             from surface_geometry_v2 s where root_id = ' || 
                             surface_geometry.ROOT_ID || ' order by id' 
                             INTO solid_coordinates;
          insert into surface_geometry
          (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE,
          IS_TRIANGULATED, IS_XLINK, IS_REVERSE, SOLID_GEOMETRY)
          values
          (surface_geometry.ID, surface_geometry.GMLID, surface_geometry.PARENT_ID,
          surface_geometry.ROOT_ID, surface_geometry.IS_SOLID, surface_geometry.IS_COMPOSITE,
          surface_geometry.IS_TRIANGULATED, surface_geometry.IS_XLINK,
          surface_geometry.IS_REVERSE, solid_coordinates);
        ELSE
          insert into surface_geometry
          (ID, GMLID, PARENT_ID, ROOT_ID, IS_SOLID, IS_COMPOSITE,
          IS_TRIANGULATED, IS_XLINK, IS_REVERSE, GEOMETRY)
          values
          (surface_geometry.ID, surface_geometry.GMLID, surface_geometry.PARENT_ID,
          surface_geometry.ROOT_ID, surface_geometry.IS_SOLID, surface_geometry.IS_COMPOSITE,
          surface_geometry.IS_TRIANGULATED, surface_geometry.IS_XLINK,
          surface_geometry.IS_REVERSE, surface_geometry.GEOMETRY);
        END IF;
        has_xlink := 0;
        is_solid := 0;
      end loop;
      dbms_output.put_line('Surface_Geometry table copy is completed.');
  end;

  PROCEDURE fillCityObjectTable
  IS
    -- variables --
    CURSOR city_object_v2 IS select * from cityobject_v2 order by id;
  BEGIN
    dbms_output.put_line('CityObject table is being copied...');
    for city_object in city_object_v2 loop
        insert into cityobject
        (ID, OBJECTCLASS_ID, GMLID, ENVELOPE, CREATION_DATE,
        TERMINATION_DATE, LAST_MODIFICATION_DATE, UPDATING_PERSON,
        REASON_FOR_UPDATE, LINEAGE, XML_SOURCE)
        values
        (city_object.ID, city_object.CLASS_ID, city_object.GMLID,
        city_object.ENVELOPE, city_object.CREATION_DATE, city_object.TERMINATION_DATE,
        city_object.LAST_MODIFICATION_DATE, city_object.UPDATING_PERSON,
        city_object.REASON_FOR_UPDATE, city_object.LINEAGE, city_object.XML_SOURCE);
    end loop;
    dbms_output.put_line('CityObject table copy is completed.');
  end;

  PROCEDURE fillCityModelTable
  IS
  BEGIN
    dbms_output.put_line('CityModel table is being copied...');
    insert into citymodel
    (ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, ENVELOPE, CREATION_DATE,
    TERMINATION_DATE, LAST_MODIFICATION_DATE, UPDATING_PERSON, REASON_FOR_UPDATE,
    LINEAGE)
    select ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, ENVELOPE, CREATION_DATE,
    TERMINATION_DATE, LAST_MODIFICATION_DATE, UPDATING_PERSON, REASON_FOR_UPDATE,
    LINEAGE from citymodel_v2;
    dbms_output.put_line('CityModel table copy is completed.');
  end;

  PROCEDURE fillAddressTable
  IS
  BEGIN
    dbms_output.put_line('Address table is being copied...');
    insert into address select * from address_v2;
    dbms_output.put_line('Address table copy is completed.');
  end;

  PROCEDURE fillBuildingTable
  IS
    -- variables --
    CURSOR buildings_v2 IS select * from building_v2 order by id;
    isSolidLOD1 NUMBER(1);
    isSolidLOD2 NUMBER(1);
    isSolidLOD3 NUMBER(1);
    isSolidLOD4 NUMBER(1);
    lod1MultiSurfaceID NUMBER(10);
    lod2MultiSurfaceID NUMBER(10);
    lod3MultiSurfaceID NUMBER(10);
    lod4MultiSurfaceID NUMBER(10);
    lod1SolidID NUMBER(10);
    lod2SolidID NUMBER(10);
    lod3SolidID NUMBER(10);
    lod4SolidID NUMBER(10);
  BEGIN
    dbms_output.put_line('Building table is being copied...');
    for building in buildings_v2 loop
        -- Check if the lod1-lod4 geometry ids are solid and/or multi surface
        -- Update the cityobject_id entry in surface_geometry table
        IF building.lod1_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD1 from surface_geometry_v2 where id = building.lod1_geometry_id;
           IF isSolidLOD1 = 1 THEN
              lod1SolidID := building.lod1_geometry_id;
           ELSE
              lod1MultiSurfaceID := building.lod1_geometry_id;
           END IF;           
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building.ID||' 
                              where ID = ' || building.lod1_geometry_id;
        END IF;
        IF building.lod2_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD2 from surface_geometry_v2 where id = building.lod2_geometry_id;
           IF isSolidLOD2 = 1 THEN
              lod2SolidID := building.lod2_geometry_id;
           ELSE
              lod2MultiSurfaceID := building.lod2_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building.ID||' 
                              where ID = ' || building.lod2_geometry_id;
        END IF;
        IF building.lod3_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD3 from surface_geometry_v2 where id = building.lod3_geometry_id;
           IF isSolidLOD3 = 1 THEN
              lod3SolidID := building.lod3_geometry_id;
           ELSE
              lod3MultiSurfaceID := building.lod3_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building.ID||' 
                              where ID = ' || building.lod3_geometry_id;
        END IF;
        IF building.lod4_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD4 from surface_geometry_v2 where id = building.lod4_geometry_id;
           IF isSolidLOD4 = 1 THEN
              lod4SolidID := building.lod4_geometry_id;
           ELSE
              lod4MultiSurfaceID := building.lod4_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building.ID||' 
                              where ID = ' || building.lod4_geometry_id;
        END IF;           

        -- Fill the building table
        insert into building
        (ID, BUILDING_PARENT_ID, BUILDING_ROOT_ID, CLASS, FUNCTION, USAGE, YEAR_OF_CONSTRUCTION,
        YEAR_OF_DEMOLITION, ROOF_TYPE, MEASURED_HEIGHT, STOREYS_ABOVE_GROUND, STOREYS_BELOW_GROUND,
        STOREY_HEIGHTS_ABOVE_GROUND, STOREY_HEIGHTS_BELOW_GROUND, LOD1_TERRAIN_INTERSECTION,
        LOD2_TERRAIN_INTERSECTION, LOD3_TERRAIN_INTERSECTION, LOD4_TERRAIN_INTERSECTION,
        LOD2_MULTI_CURVE, LOD3_MULTI_CURVE, LOD4_MULTI_CURVE, LOD1_MULTI_SURFACE_ID, LOD2_MULTI_SURFACE_ID,
        LOD3_MULTI_SURFACE_ID, LOD4_MULTI_SURFACE_ID, LOD1_SOLID_ID, LOD2_SOLID_ID, LOD3_SOLID_ID,
        LOD4_SOLID_ID)
        values
        (building.ID, building.BUILDING_PARENT_ID, building.BUILDING_ROOT_ID,
        replace(building.CLASS,' ','--/\--'), replace(building.FUNCTION,' ','--/\--'), replace(building.USAGE,' ','--/\--'), building.YEAR_OF_CONSTRUCTION,
        building.YEAR_OF_DEMOLITION, building.ROOF_TYPE, building.MEASURED_HEIGHT, building.STOREYS_ABOVE_GROUND,
        building.STOREYS_BELOW_GROUND, building.STOREY_HEIGHTS_ABOVE_GROUND, building.STOREY_HEIGHTS_BELOW_GROUND,
        building.LOD1_TERRAIN_INTERSECTION, building.LOD2_TERRAIN_INTERSECTION, building.LOD3_TERRAIN_INTERSECTION,
        building.LOD4_TERRAIN_INTERSECTION, building.LOD2_MULTI_CURVE, building.LOD3_MULTI_CURVE,
        building.LOD4_MULTI_CURVE, lod1MultiSurfaceID, lod2MultiSurfaceID, lod3MultiSurfaceID, lod4MultiSurfaceID,
        lod1SolidID, lod2SolidID, lod3SolidID, lod4SolidID);

        -- Insert the name and the description of the building
        -- into the cityobject table
        update cityobject
        set name = building.name,
        name_codespace = building.name_codespace,
        description = building.description
        where id = building.id;

        -- Reset the variables
        isSolidLOD1 := NULL;
        isSolidLOD2 := NULL;
        isSolidLOD3 := NULL;
        isSolidLOD4 := NULL;
        lod1MultiSurfaceID := NULL;
        lod2MultiSurfaceID := NULL;
        lod3MultiSurfaceID := NULL;
        lod4MultiSurfaceID := NULL;
        lod1SolidID := NULL;
        lod2SolidID := NULL;
        lod3SolidID := NULL;
        lod4SolidID := NULL;
    end loop;
    dbms_output.put_line('Building table copy is completed.');
  end;

  PROCEDURE fillAddressToBuildingTable
  IS
  BEGIN
    dbms_output.put_line('Address_to_Building table is being copied...');
    insert into address_to_building select * from address_to_building_v2;
    dbms_output.put_line('Address_to_Building table copy is completed.');
  end;

  PROCEDURE fillAppearanceTable
  IS
  BEGIN
    dbms_output.put_line('Appearance table is being copied...');
    insert into appearance
    (ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, THEME,
    CITYMODEL_ID, CITYOBJECT_ID)
    select ID, GMLID, NAME, NAME_CODESPACE, DESCRIPTION, THEME,
    CITYMODEL_ID, CITYOBJECT_ID from appearance_v2;
    dbms_output.put_line('Appearance table copy is completed.');
  end;

  PROCEDURE fillSurfaceDataTable
  IS
    -- variables --
    CURSOR surface_data_v2 IS select * from surface_data_v2 order by id;
    classID NUMBER(10);
    texID NUMBER(10);
  BEGIN
    dbms_output.put_line('Surface_data table is being copied...');
    for surface_data in surface_data_v2 loop
        -- Check Type
        IF surface_data.type IS NOT NULL THEN
           select id into classID from OBJECTCLASS where classname = surface_data.type;
        END IF;

        -- Add the Tex into the Tex Table
        -- ORDIMAGE to BLOB conversion via ordsys.ordimage.getContent
        IF (surface_data.tex_image_uri IS NOT NULL
            OR surface_data.tex_image IS NOT NULL
            OR surface_data.tex_mime_type IS NOT NULL) THEN
           texID := TEX_IMAGE_SEQ.NEXTVAL;
           insert into tex_image
           (ID, TEX_IMAGE_URI, TEX_IMAGE_DATA, TEX_MIME_TYPE)
           values
           (texID,surface_data.TEX_IMAGE_URI,
           ordsys.ordimage.getContent(surface_data.TEX_IMAGE),surface_data.TEX_MIME_TYPE) ;
        END IF;

        -- Insert into with objectclass_id, tex id and without gmlid_codespace
        insert into surface_data
        (ID,GMLID,NAME,NAME_CODESPACE,DESCRIPTION,IS_FRONT,OBJECTCLASS_ID,
        X3D_SHININESS,X3D_TRANSPARENCY,X3D_AMBIENT_INTENSITY,X3D_SPECULAR_COLOR,
        X3D_DIFFUSE_COLOR,X3D_EMISSIVE_COLOR,X3D_IS_SMOOTH, TEX_IMAGE_ID,
        TEX_TEXTURE_TYPE,TEX_WRAP_MODE,TEX_BORDER_COLOR,GT_PREFER_WORLDFILE,
        GT_ORIENTATION,GT_REFERENCE_POINT)
        values
        (surface_data.ID,surface_data.GMLID,surface_data.NAME,
        surface_data.NAME_CODESPACE,surface_data.DESCRIPTION,surface_data.IS_FRONT,
        classID,surface_data.X3D_SHININESS,surface_data.X3D_TRANSPARENCY,
        surface_data.X3D_AMBIENT_INTENSITY,surface_data.X3D_SPECULAR_COLOR,
        surface_data.X3D_DIFFUSE_COLOR,surface_data.X3D_EMISSIVE_COLOR,
        surface_data.X3D_IS_SMOOTH,texID,surface_data.TEX_TEXTURE_TYPE,
        surface_data.TEX_WRAP_MODE,surface_data.TEX_BORDER_COLOR,
        surface_data.GT_PREFER_WORLDFILE, surface_data.GT_ORIENTATION,
        surface_data.GT_REFERENCE_POINT);
    end loop;
    dbms_output.put_line('Surface_data table copy is completed.');
  end;

  PROCEDURE fillAppearToSurfaceDataTable
  IS
  BEGIN
    dbms_output.put_line('Appear_to_Surface_Data table is being copied...');
    insert into appear_to_surface_data select * from appear_to_surface_data_v2;
    dbms_output.put_line('Appear_to_Surface_Data table copy is completed.');
  end;

  PROCEDURE fillBreaklineReliefTable
  IS
  BEGIN
    dbms_output.put_line('Breakline_Relief table is being copied...');
    insert into breakline_relief select * from breakline_relief_v2;
    dbms_output.put_line('Breakline_Relief table copy is completed.');
  end;

  PROCEDURE fillRoomTable
  IS
    -- variables --
    CURSOR rooms_v2 IS select * from room_v2 order by id;
    isSolidLOD4 NUMBER(1);
    lod4MultiSurfaceID NUMBER(10);
    lod4SolidID NUMBER(10);
  BEGIN
    dbms_output.put_line('Room table is being copied...');
    for room in rooms_v2 loop
        -- Check if the lod4 geometry id is solid and/or multi surface
        -- Update the cityobject_id entry in surface_geometry table
        IF room.lod4_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD4 from surface_geometry_v2
           where id = room.lod4_geometry_id;

           IF isSolidLOD4 = 1 THEN
              lod4SolidID := room.lod4_geometry_id;
           ELSE
              lod4MultiSurfaceID := room.lod4_geometry_id;
           END IF;
           
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||room.ID||' 
                              where ID = ' || room.lod4_geometry_id;
        END IF;

        -- Fill the room table
        insert into room
        (ID,CLASS,FUNCTION,USAGE,BUILDING_ID,LOD4_MULTI_SURFACE_ID,
        LOD4_SOLID_ID)
        values
        (room.ID,replace(room.CLASS,' ','--/\--'),replace(room.FUNCTION,' ','--/\--'),replace(room.USAGE,' ','--/\--'),room.BUILDING_ID,
        lod4MultiSurfaceID, lod4SolidID);

        -- Insert the name and the description of the room
        -- into the cityobject table
        update cityobject
        set name = room.name,
        name_codespace = room.name_codespace,
        description = room.description
        where id = room.id;

        -- Reset the variables
        isSolidLOD4 := NULL;
        lod4MultiSurfaceID := NULL;
        lod4SolidID := NULL;
    end loop;
    dbms_output.put_line('Room table copy is completed.');
  end;

  PROCEDURE fillBuildingFurnitureTable
  IS
    -- variables --
    CURSOR building_furniture_v2 IS select * from building_furniture_v2 order by id;
  BEGIN
    dbms_output.put_line('Building_Furniture table is being copied...');
    for building_furniture in building_furniture_v2 loop
    
        -- Update the cityobject_id entry in surface_geometry table
        IF building_furniture.LOD4_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building_furniture.ID||' 
                              where ID = ' || building_furniture.LOD4_GEOMETRY_ID;
        END IF;
    
        insert into building_furniture
        (ID,CLASS,FUNCTION,USAGE,ROOM_ID,LOD4_BREP_ID,LOD4_IMPLICIT_REP_ID,
        LOD4_IMPLICIT_REF_POINT,LOD4_IMPLICIT_TRANSFORMATION)
        values
        (building_furniture.ID,replace(building_furniture.CLASS,' ','--/\--'),replace(building_furniture.FUNCTION,' ','--/\--'),
        replace(building_furniture.USAGE,' ','--/\--'),building_furniture.ROOM_ID,
        building_furniture.LOD4_GEOMETRY_ID,building_furniture.LOD4_IMPLICIT_REP_ID,
        building_furniture.LOD4_IMPLICIT_REF_POINT,
        building_furniture.LOD4_IMPLICIT_TRANSFORMATION);

        -- Insert the name and the description of the building furniture
        -- into the cityobject table

        update cityobject
        set
        name = building_furniture.name,
        name_codespace = building_furniture.name_codespace,
        description = building_furniture.description
        where id = building_furniture.id;
    end loop;
    dbms_output.put_line('Building_Furniture table copy is completed.');
  end;

  PROCEDURE fillBuildingInstallationTable
  IS
    -- variables --
    CURSOR building_installation_v2 IS select * from building_installation_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('Building_Installation table is being copied...');
    for building_installation in building_installation_v2 loop
        -- Check the id of the is_external type
        IF building_installation.is_external IS NOT NULL THEN
          IF building_installation.is_external = 1 THEN
              select id into classID from OBJECTCLASS where classname = 'BuildingInstallation';
          ELSE
              select id into classID from OBJECTCLASS where classname = 'IntBuildingInstallation';
          END IF;
        END IF;
        
        -- Update the cityobject_id entry in surface_geometry table
        IF building_installation.LOD2_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building_installation.ID||' 
                              where ID = ' || building_installation.LOD2_GEOMETRY_ID;
        END IF;
        IF building_installation.LOD3_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building_installation.ID||' 
                              where ID = ' || building_installation.LOD3_GEOMETRY_ID;
        END IF;
        IF building_installation.LOD4_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||building_installation.ID||' 
                              where ID = ' || building_installation.LOD4_GEOMETRY_ID;
        END IF;              

        insert into building_installation
        (ID,OBJECTCLASS_ID,CLASS,FUNCTION,USAGE,BUILDING_ID,ROOM_ID,
        LOD2_BREP_ID,LOD3_BREP_ID,LOD4_BREP_ID)
        values
        (building_installation.ID,classID,replace(building_installation.CLASS,' ','--/\--'),
        replace(building_installation.FUNCTION,' ','--/\--'),replace(building_installation.USAGE,' ','--/\--'),
        building_installation.BUILDING_ID,building_installation.ROOM_ID,
        building_installation.LOD2_GEOMETRY_ID,building_installation.LOD3_GEOMETRY_ID,
        building_installation.LOD4_GEOMETRY_ID);

        -- Insert the name and the description of the building installation
        -- into the cityobject table
        update cityobject
        set
        name = building_installation.name,
        name_codespace = building_installation.name_codespace,
        description = building_installation.description
        where id = building_installation.id;
    end loop;
    dbms_output.put_line('Building_Installation table copy is completed.');
  end;

  PROCEDURE fillImplicitGeometryTable
  IS
    -- variables --
    CURSOR implicit_geometry_v2 IS select * from implicit_geometry_v2 order by id;
  BEGIN
    dbms_output.put_line('Implicit_Geometry table is being copied...');
    for implicit_geometry in implicit_geometry_v2 loop
        insert into implicit_geometry
        (ID,MIME_TYPE,REFERENCE_TO_LIBRARY,LIBRARY_OBJECT,RELATIVE_BREP_ID)
        values
        (implicit_geometry.ID,implicit_geometry.MIME_TYPE,
        implicit_geometry.REFERENCE_TO_LIBRARY,implicit_geometry.LIBRARY_OBJECT,
        implicit_geometry.RELATIVE_GEOMETRY_ID);
    end loop;
    dbms_output.put_line('Implicit_Geometry table copy is completed.');
  end;

  PROCEDURE fillCityFurnitureTable
  IS
    -- variables --
    CURSOR city_furniture_v2 IS select * from city_furniture_v2 order by id;
  BEGIN
    dbms_output.put_line('City_Furniture table is being copied...');
    for city_furniture in city_furniture_v2 loop        
        -- Update the cityobject_id entry in surface_geometry table
        IF city_furniture.LOD1_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||city_furniture.ID||' 
                              where ID = ' || city_furniture.LOD1_GEOMETRY_ID;
        END IF;
        IF city_furniture.LOD2_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||city_furniture.ID||' 
                              where ID = ' || city_furniture.LOD2_GEOMETRY_ID;
        END IF;
        IF city_furniture.LOD3_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||city_furniture.ID||' 
                              where ID = ' || city_furniture.LOD3_GEOMETRY_ID;
        END IF;
        IF city_furniture.LOD4_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||city_furniture.ID||' 
                              where ID = ' || city_furniture.LOD4_GEOMETRY_ID;
        END IF;
        
        insert into city_furniture
        (ID,CLASS,FUNCTION,LOD1_TERRAIN_INTERSECTION,LOD2_TERRAIN_INTERSECTION,
        LOD3_TERRAIN_INTERSECTION,LOD4_TERRAIN_INTERSECTION,LOD1_BREP_ID,
        LOD2_BREP_ID,LOD3_BREP_ID,LOD4_BREP_ID,LOD1_IMPLICIT_REP_ID,
        LOD2_IMPLICIT_REP_ID,LOD3_IMPLICIT_REP_ID,LOD4_IMPLICIT_REP_ID,
        LOD1_IMPLICIT_REF_POINT,LOD2_IMPLICIT_REF_POINT,LOD3_IMPLICIT_REF_POINT,
        LOD4_IMPLICIT_REF_POINT,LOD1_IMPLICIT_TRANSFORMATION,
        LOD2_IMPLICIT_TRANSFORMATION,LOD3_IMPLICIT_TRANSFORMATION,
        LOD4_IMPLICIT_TRANSFORMATION)
        values
        (city_furniture.ID,replace(city_furniture.CLASS,' ','--/\--'),replace(city_furniture.FUNCTION,' ','--/\--'),
        city_furniture.LOD1_TERRAIN_INTERSECTION,city_furniture.LOD2_TERRAIN_INTERSECTION,
        city_furniture.LOD3_TERRAIN_INTERSECTION,city_furniture.LOD4_TERRAIN_INTERSECTION,
        city_furniture.LOD1_GEOMETRY_ID,city_furniture.LOD2_GEOMETRY_ID,
        city_furniture.LOD3_GEOMETRY_ID,city_furniture.LOD4_GEOMETRY_ID,
        city_furniture.LOD1_IMPLICIT_REP_ID,city_furniture.LOD2_IMPLICIT_REP_ID,
        city_furniture.LOD3_IMPLICIT_REP_ID,city_furniture.LOD4_IMPLICIT_REP_ID,
        city_furniture.LOD1_IMPLICIT_REF_POINT,city_furniture.LOD2_IMPLICIT_REF_POINT,
        city_furniture.LOD3_IMPLICIT_REF_POINT,city_furniture.LOD4_IMPLICIT_REF_POINT,
        city_furniture.LOD1_IMPLICIT_TRANSFORMATION,
        city_furniture.LOD2_IMPLICIT_TRANSFORMATION,
        city_furniture.LOD3_IMPLICIT_TRANSFORMATION,
        city_furniture.LOD4_IMPLICIT_TRANSFORMATION);

        -- Insert the name and the description of the city furniture
        -- into the cityobject table
        update cityobject
        set
        name = city_furniture.name,
        name_codespace = city_furniture.name_codespace,
        description = city_furniture.description
        where id = city_furniture.id;
    end loop;
    dbms_output.put_line('City_Furniture table copy is completed.');
  end;

  PROCEDURE fillCityObjectGenAttrTable
  IS
    -- variables --
    CURSOR cityobject_genericattrib_v2 IS
           select * from cityobject_genericattrib_v2 order by id;
  BEGIN
    dbms_output.put_line('CityObject_GenericAttrib table is being copied...');
    for cityobject_genericattrib in cityobject_genericattrib_v2 loop
        insert into cityobject_genericattrib
        (ID,ROOT_GENATTRIB_ID,ATTRNAME,DATATYPE,STRVAL,INTVAL,REALVAL,URIVAL,
        DATEVAL,GEOMVAL,BLOBVAL,CITYOBJECT_ID,SURFACE_GEOMETRY_ID)
        values
        (cityobject_genericattrib.ID,cityobject_genericattrib.ID,
        cityobject_genericattrib.ATTRNAME,cityobject_genericattrib.DATATYPE,
        cityobject_genericattrib.STRVAL,cityobject_genericattrib.INTVAL,
        cityobject_genericattrib.REALVAL,cityobject_genericattrib.URIVAL,
        cityobject_genericattrib.DATEVAL,cityobject_genericattrib.GEOMVAL,
        cityobject_genericattrib.BLOBVAL,cityobject_genericattrib.CITYOBJECT_ID,
        cityobject_genericattrib.SURFACE_GEOMETRY_ID);
    end loop;
    dbms_output.put_line('CityObject_GenericAttrib table copy is completed.');
  end;

  PROCEDURE fillCityObjectMemberTable
  IS
  BEGIN
    dbms_output.put_line('CityObject_Member table is being copied...');
    insert into cityobject_member select * from cityobject_member_v2;
    dbms_output.put_line('CityObject_Member table copy is completed.');
  end;

  PROCEDURE fillCityObjectGroupTable
  IS
    -- variables --
    CURSOR cityobjectgroup_v2 IS select * from cityobjectgroup_v2 order by id;
  BEGIN
    dbms_output.put_line('CityObjectGroup table is being copied...');
    for cityobjectgroup in cityobjectgroup_v2 loop
        insert into cityobjectgroup
        (ID,CLASS,FUNCTION,USAGE,BREP_ID,OTHER_GEOM,PARENT_CITYOBJECT_ID)
        values
        (cityobjectgroup.ID,replace(cityobjectgroup.CLASS,' ','--/\--'),replace(cityobjectgroup.FUNCTION,' ','--/\--'),
        replace(cityobjectgroup.USAGE,' ','--/\--'),cityobjectgroup.SURFACE_GEOMETRY_ID,
        cityobjectgroup.GEOMETRY,cityobjectgroup.PARENT_CITYOBJECT_ID);

        -- Insert the name and the description of the city furniture
        -- into the cityobject table
        update cityobject
        set
        name = cityobjectgroup.name,
        name_codespace = cityobjectgroup.name_codespace,
        description = cityobjectgroup.description
        where id = cityobjectgroup.id;
    end loop;
    dbms_output.put_line('CityObjectGroup table copy is completed.');
  end;

  PROCEDURE fillExternalReferenceTable
  IS
  BEGIN
    dbms_output.put_line('External_Reference table is being copied...');
    insert into external_reference select * from external_reference_v2;
    dbms_output.put_line('External_Reference table copy is completed.');
  end;

  PROCEDURE fillGeneralizationTable
  IS
  BEGIN
    dbms_output.put_line('Generalization table is being copied...');
    insert into generalization select * from generalization_v2;
    dbms_output.put_line('Generalization table copy is completed.');
  end;

  PROCEDURE fillGenericCityObjectTable
  IS
    -- variables --
    CURSOR generic_cityobject_v2 IS select * from generic_cityobject_v2 order by id;
  BEGIN
    dbms_output.put_line('Generic_CityObject table is being copied...');
    -- Drop the invalid indexes
    EXECUTE IMMEDIATE 'DROP INDEX GEN_OBJECT_LOD3XGEOM_SPX';
    EXECUTE IMMEDIATE 'DROP INDEX GEN_OBJECT_LOD4REFPNT_SPX';
    EXECUTE IMMEDIATE 'DROP INDEX GEN_OBJECT_LOD4TERR_SPX';
    EXECUTE IMMEDIATE 'DROP INDEX GEN_OBJECT_LOD4XGEOM_SPX'; 
        
    for generic_cityobject in generic_cityobject_v2 loop
        -- Update the cityobject_id entry in surface_geometry table
        IF generic_cityobject.LOD0_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||generic_cityobject.ID||' 
                              where ID = ' || generic_cityobject.LOD0_GEOMETRY_ID;
        END IF;
        IF generic_cityobject.LOD1_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||generic_cityobject.ID||' 
                              where ID = ' || generic_cityobject.LOD1_GEOMETRY_ID;
        END IF;
        IF generic_cityobject.LOD2_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||generic_cityobject.ID||' 
                              where ID = ' || generic_cityobject.LOD2_GEOMETRY_ID;
        END IF;
        IF generic_cityobject.LOD3_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||generic_cityobject.ID||' 
                              where ID = ' || generic_cityobject.LOD3_GEOMETRY_ID;
        END IF;
        IF generic_cityobject.LOD4_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||generic_cityobject.ID||' 
                              where ID = ' || generic_cityobject.LOD4_GEOMETRY_ID;
        END IF;
    
        insert into generic_cityobject
        (ID,CLASS,FUNCTION,USAGE,LOD0_TERRAIN_INTERSECTION,
        LOD1_TERRAIN_INTERSECTION,LOD2_TERRAIN_INTERSECTION,LOD3_TERRAIN_INTERSECTION,
        LOD4_TERRAIN_INTERSECTION,LOD0_BREP_ID,LOD1_BREP_ID,LOD2_BREP_ID,
        LOD3_BREP_ID,LOD4_BREP_ID,LOD0_IMPLICIT_REP_ID,LOD1_IMPLICIT_REP_ID,
        LOD2_IMPLICIT_REP_ID,LOD3_IMPLICIT_REP_ID,LOD4_IMPLICIT_REP_ID,
        LOD0_IMPLICIT_REF_POINT,LOD1_IMPLICIT_REF_POINT,LOD2_IMPLICIT_REF_POINT,
        LOD3_IMPLICIT_REF_POINT,LOD4_IMPLICIT_REF_POINT,LOD0_IMPLICIT_TRANSFORMATION,
        LOD1_IMPLICIT_TRANSFORMATION,LOD2_IMPLICIT_TRANSFORMATION,
        LOD3_IMPLICIT_TRANSFORMATION,LOD4_IMPLICIT_TRANSFORMATION)
        values
        (generic_cityobject.ID,replace(generic_cityobject.CLASS,' ','--/\--'),replace(generic_cityobject.FUNCTION,' ','--/\--'),
        replace(generic_cityobject.USAGE,' ','--/\--'),generic_cityobject.LOD0_TERRAIN_INTERSECTION,
        generic_cityobject.LOD1_TERRAIN_INTERSECTION,
        generic_cityobject.LOD2_TERRAIN_INTERSECTION,
        generic_cityobject.LOD3_TERRAIN_INTERSECTION,
        generic_cityobject.LOD4_TERRAIN_INTERSECTION,
        generic_cityobject.LOD0_GEOMETRY_ID, generic_cityobject.LOD1_GEOMETRY_ID,
        generic_cityobject.LOD2_GEOMETRY_ID, generic_cityobject.LOD3_GEOMETRY_ID,
        generic_cityobject.LOD4_GEOMETRY_ID, generic_cityobject.LOD0_IMPLICIT_REP_ID,
        generic_cityobject.LOD1_IMPLICIT_REP_ID,generic_cityobject.LOD2_IMPLICIT_REP_ID,
        generic_cityobject.LOD3_IMPLICIT_REP_ID,generic_cityobject.LOD4_IMPLICIT_REP_ID,
        generic_cityobject.LOD0_IMPLICIT_REF_POINT,
        generic_cityobject.LOD1_IMPLICIT_REF_POINT,
        generic_cityobject.LOD2_IMPLICIT_REF_POINT,
        generic_cityobject.LOD3_IMPLICIT_REF_POINT,
        generic_cityobject.LOD4_IMPLICIT_REF_POINT,
        generic_cityobject.LOD0_IMPLICIT_TRANSFORMATION,
        generic_cityobject.LOD1_IMPLICIT_TRANSFORMATION,
        generic_cityobject.LOD2_IMPLICIT_TRANSFORMATION,
        generic_cityobject.LOD3_IMPLICIT_TRANSFORMATION,
        generic_cityobject.LOD4_IMPLICIT_TRANSFORMATION);

        -- Insert the name and the description of the generic city object
        -- into the cityobject table
        update cityobject
        set
        name = generic_cityobject.name,
        name_codespace = generic_cityobject.name_codespace,
        description = generic_cityobject.description
        where id = generic_cityobject.id;
    end loop;
    -- Recreate the dropped invalid indexes
    EXECUTE IMMEDIATE 'CREATE INDEX GEN_OBJECT_LOD3XGEOM_SPX ON GENERIC_CITYOBJECT (LOD3_OTHER_GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX';
    EXECUTE IMMEDIATE 'CREATE INDEX GEN_OBJECT_LOD4REFPNT_SPX ON GENERIC_CITYOBJECT (LOD4_IMPLICIT_REF_POINT) INDEXTYPE IS MDSYS.SPATIAL_INDEX';
    EXECUTE IMMEDIATE 'CREATE INDEX GEN_OBJECT_LOD4TERR_SPX ON GENERIC_CITYOBJECT (LOD4_TERRAIN_INTERSECTION) INDEXTYPE IS MDSYS.SPATIAL_INDEX';
    EXECUTE IMMEDIATE 'CREATE INDEX GEN_OBJECT_LOD4XGEOM_SPX ON GENERIC_CITYOBJECT (LOD4_OTHER_GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX';
    dbms_output.put_line('Generic_CityObject table copy is completed.');
  end;

  PROCEDURE fillGroupToCityObject
  IS
  BEGIN
    dbms_output.put_line('Group_To_CityObject table is being copied...');
    insert into group_to_cityobject select * from group_to_cityobject_v2;
    dbms_output.put_line('Group_To_CityObject table copy is completed.');
  end;

  PROCEDURE fillLandUseTable
  IS
    -- variables --
    CURSOR land_use_v2 IS select * from land_use_v2 order by id;
  BEGIN
    dbms_output.put_line('Land_Use table is being copied...');
    for land_use in land_use_v2 loop
        -- Update the cityobject_id entry in surface_geometry table
        IF land_use.LOD0_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||land_use.ID||' 
                              where ID = ' || land_use.LOD0_MULTI_SURFACE_ID;
        END IF;            
        IF land_use.LOD1_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||land_use.ID||' 
                              where ID = ' || land_use.LOD1_MULTI_SURFACE_ID;
        END IF;
        IF land_use.LOD2_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||land_use.ID||' 
                              where ID = ' || land_use.LOD2_MULTI_SURFACE_ID;
        END IF;
        IF land_use.LOD3_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||land_use.ID||' 
                              where ID = ' || land_use.LOD3_MULTI_SURFACE_ID;
        END IF;
        IF land_use.LOD4_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||land_use.ID||' 
                              where ID = ' || land_use.LOD4_MULTI_SURFACE_ID;
        END IF;
    
        insert into land_use
        (ID,CLASS,FUNCTION,USAGE,LOD0_MULTI_SURFACE_ID,LOD1_MULTI_SURFACE_ID,
        LOD2_MULTI_SURFACE_ID,LOD3_MULTI_SURFACE_ID,LOD4_MULTI_SURFACE_ID)
        values
        (land_use.ID,replace(land_use.CLASS,' ','--/\--'),replace(land_use.FUNCTION,' ','--/\--'),replace(land_use.USAGE,' ','--/\--'),
        land_use.LOD0_MULTI_SURFACE_ID,land_use.LOD1_MULTI_SURFACE_ID,
        land_use.LOD2_MULTI_SURFACE_ID,land_use.LOD3_MULTI_SURFACE_ID,
        land_use.LOD4_MULTI_SURFACE_ID);

        -- Insert the name and the description of the land use
        -- into the cityobject table
        update cityobject
        set
        name = land_use.name,
        name_codespace = land_use.name_codespace,
        description = land_use.description
        where id = land_use.id;
    end loop;
    dbms_output.put_line('Land_Use table copy is completed.');
  end;

  PROCEDURE fillMassPointReliefTable
  IS
  BEGIN
    dbms_output.put_line('MassPoint_Relief table is being copied...');
    insert into masspoint_relief select * from masspoint_relief_v2;
    dbms_output.put_line('MassPoint_Relief table copy is completed.');
  end;

  PROCEDURE fillOpeningTable
  IS
    -- variables --
    CURSOR opening_v2 IS select * from opening_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('Opening table is being copied...');
    for opening in opening_v2 loop
        -- Check Type
        IF opening.type IS NOT NULL THEN
           select id into classID from OBJECTCLASS where classname = opening.type;
        END IF;
        
        -- Update the cityobject_id entry in surface_geometry table
        IF opening.LOD3_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||opening.ID||' 
                              where ID = ' || opening.LOD3_MULTI_SURFACE_ID;
        END IF;
        IF opening.LOD4_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||opening.ID||' 
                              where ID = ' || opening.LOD4_MULTI_SURFACE_ID;
        END IF;

        insert into opening
        (ID,OBJECTCLASS_ID,ADDRESS_ID,LOD3_MULTI_SURFACE_ID,LOD4_MULTI_SURFACE_ID)
        values
        (opening.ID,classID,opening.ADDRESS_ID,
        opening.LOD3_MULTI_SURFACE_ID,opening.LOD4_MULTI_SURFACE_ID);

        -- Insert the name and the description of the opening
        -- into the cityobject table
        update cityobject
        set
        name = opening.name,
        name_codespace = opening.name_codespace,
        description = opening.description
        where id = opening.id;
    end loop;
    dbms_output.put_line('Opening table copy is completed.');
  end;

  PROCEDURE fillThematicSurfaceTable
  IS
    -- variables --
    CURSOR thematic_surface_v2 IS select * from thematic_surface_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('Thematic_Surface table is being copied...');
    for thematic_surface in thematic_surface_v2 loop
        -- Check Type
        IF thematic_surface.type IS NOT NULL THEN
           select id into classID from OBJECTCLASS where classname = thematic_surface.type;
        END IF;
        
        -- Update the cityobject_id entry in surface_geometry table
        IF thematic_surface.LOD2_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||thematic_surface.ID||' 
                              where ID = ' || thematic_surface.LOD2_MULTI_SURFACE_ID;
        END IF;
        IF thematic_surface.LOD3_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||thematic_surface.ID||' 
                              where ID = ' || thematic_surface.LOD3_MULTI_SURFACE_ID;
        END IF;
        IF thematic_surface.LOD4_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||thematic_surface.ID||' 
                              where ID = ' || thematic_surface.LOD4_MULTI_SURFACE_ID;
        END IF;

        insert into thematic_surface
        (ID,OBJECTCLASS_ID,BUILDING_ID,ROOM_ID,LOD2_MULTI_SURFACE_ID,
        LOD3_MULTI_SURFACE_ID,LOD4_MULTI_SURFACE_ID)
        values
        (thematic_surface.ID,classID,thematic_surface.BUILDING_ID,
        thematic_surface.ROOM_ID,thematic_surface.LOD2_MULTI_SURFACE_ID,
        thematic_surface.LOD3_MULTI_SURFACE_ID,
        thematic_surface.LOD4_MULTI_SURFACE_ID);

        -- Insert the name and the description of the thematic surface
        -- into the cityobject table
        update cityobject
        set
        name = thematic_surface.name,
        name_codespace = thematic_surface.name_codespace,
        description = thematic_surface.description
        where id = thematic_surface.id;
    end loop;
    dbms_output.put_line('Thematic_Surface table copy is completed.');
  end;

  PROCEDURE fillOpeningToThemSurfaceTable
  IS
  BEGIN
    dbms_output.put_line('Opening_To_Them_Surface table is being copied...');
    insert into opening_to_them_surface select * from opening_to_them_surface_v2;
    dbms_output.put_line('Opening_To_Them_Surface table copy is completed.');
  end;

  PROCEDURE fillPlantCoverTable
  IS
    -- variables --
    CURSOR plant_cover_v2 IS select * from plant_cover_v2 order by id;
    isSolidLOD1 NUMBER(1);
    isSolidLOD2 NUMBER(1);
    isSolidLOD3 NUMBER(1);
    isSolidLOD4 NUMBER(1);
    lod1MultiSurfaceID NUMBER(10);
    lod2MultiSurfaceID NUMBER(10);
    lod3MultiSurfaceID NUMBER(10);
    lod4MultiSurfaceID NUMBER(10);
    lod1SolidID NUMBER(10);
    lod2SolidID NUMBER(10);
    lod3SolidID NUMBER(10);
    lod4SolidID NUMBER(10);
  BEGIN
    dbms_output.put_line('Plant_Cover table is being copied...');
    for plant_cover in plant_cover_v2 loop
        -- Check if the lod1-lod4 geometry ids are solid and/or multi surface
        IF plant_cover.lod1_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD1 from surface_geometry_v2 where id = plant_cover.lod1_geometry_id;
           IF isSolidLOD1 = 1 THEN
              lod1SolidID := plant_cover.lod1_geometry_id;
           ELSE
              lod1MultiSurfaceID := plant_cover.lod1_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||plant_cover.ID||' 
                              where ID = ' || plant_cover.lod1_geometry_id;
        END IF;
        IF plant_cover.lod2_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD2 from surface_geometry_v2 where id = plant_cover.lod2_geometry_id;
           IF isSolidLOD2 = 1 THEN
              lod2SolidID := plant_cover.lod2_geometry_id;
           ELSE
              lod2MultiSurfaceID := plant_cover.lod2_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||plant_cover.ID||' 
                              where ID = ' || plant_cover.lod2_geometry_id;
        END IF;
        IF plant_cover.lod3_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD3 from surface_geometry_v2 where id = plant_cover.lod3_geometry_id;
           IF isSolidLOD3 = 1 THEN
              lod3SolidID := plant_cover.lod3_geometry_id;
           ELSE
              lod3MultiSurfaceID := plant_cover.lod3_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||plant_cover.ID||' 
                              where ID = ' || plant_cover.lod3_geometry_id;
        END IF;
        IF plant_cover.lod4_geometry_id IS NOT NULL THEN
           select is_solid into isSolidLOD4 from surface_geometry_v2 where id = plant_cover.lod4_geometry_id;
           IF isSolidLOD4 = 1 THEN
              lod4SolidID := plant_cover.lod4_geometry_id;
           ELSE
              lod4MultiSurfaceID := plant_cover.lod4_geometry_id;
           END IF;
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||plant_cover.ID||' 
                              where ID = ' || plant_cover.lod4_geometry_id;
        END IF;

        -- Fill the building table
        insert into plant_cover
        (ID,CLASS,FUNCTION,AVERAGE_HEIGHT,LOD1_MULTI_SURFACE_ID,
        LOD2_MULTI_SURFACE_ID,LOD3_MULTI_SURFACE_ID,LOD4_MULTI_SURFACE_ID,
        LOD1_MULTI_SOLID_ID, LOD2_MULTI_SOLID_ID, LOD3_MULTI_SOLID_ID,
        LOD4_MULTI_SOLID_ID)
        values
        (plant_cover.ID,replace(plant_cover.CLASS,' ','--/\--'),replace(plant_cover.FUNCTION,' ','--/\--'),
        plant_cover.AVERAGE_HEIGHT,lod1MultiSurfaceID,lod2MultiSurfaceID,
        lod3MultiSurfaceID,lod4MultiSurfaceID,lod1SolidID,lod2SolidID,
        lod3SolidID, lod4SolidID);

        -- Insert the name and the description of the plant cover
        -- into the cityobject table
        update cityobject
        set name = plant_cover.name,
        name_codespace = plant_cover.name_codespace,
        description = plant_cover.description
        where id = plant_cover.id;

        -- Reset the variables
        isSolidLOD1 := NULL;
        isSolidLOD2 := NULL;
        isSolidLOD3 := NULL;
        isSolidLOD4 := NULL;
        lod1MultiSurfaceID := NULL;
        lod2MultiSurfaceID := NULL;
        lod3MultiSurfaceID := NULL;
        lod4MultiSurfaceID := NULL;
        lod1SolidID := NULL;
        lod2SolidID := NULL;
        lod3SolidID := NULL;
        lod4SolidID := NULL;
    end loop;
    dbms_output.put_line('Plant_Cover table copy is completed.');
  end;

  PROCEDURE fillReliefComponentTable
  IS
    -- variables --
    CURSOR relief_component_v2 IS select * from relief_component_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('Relief_Component table is being copied...');
    for relief_component in relief_component_v2 loop
        -- Fetch the objectclass id
        IF relief_component.id IS NOT NULL THEN
           select OBJECTCLASS_ID into classID from CITYOBJECT
           where id = relief_component.id;
        END IF;

        insert into relief_component
        (ID,OBJECTCLASS_ID,LOD,EXTENT)
        values
        (relief_component.ID,classID,relief_component.LOD,
        relief_component.EXTENT);

        -- Insert the name and the description of the relief component
        -- into the cityobject table
        update cityobject
        set
        name = relief_component.name,
        name_codespace = relief_component.name_codespace,
        description = relief_component.description
        where id = relief_component.id;
    end loop;
    dbms_output.put_line('Relief_Component table copy is completed.');
  end;

  PROCEDURE fillRasterReliefTable
  IS
    -- variables --
    CURSOR raster_relief_v2 IS select * from raster_relief_v2 order by id;
    gridID NUMBER(10);
  BEGIN
    dbms_output.put_line('Raster_Relief table is being copied...');
    for raster_relief in raster_relief_v2 loop
        -- Add the Raster Property into the Grid Coverage Table
        IF (raster_relief.RASTERPROPERTY IS NOT NULL) THEN
           gridID := GRID_COVERAGE_SEQ.NEXTVAL;
           insert into grid_coverage
           (ID,RASTERPROPERTY)
           values
           (gridID,raster_relief.RASTERPROPERTY) ;
        END IF;

        -- Is the raster relief id the same as raster component id?
        insert into raster_relief
        (ID,COVERAGE_ID)
        values
        (raster_relief.ID,gridID);
    end loop;
    dbms_output.put_line('Raster_Relief table copy is completed.');
  end;

  PROCEDURE fillReliefFeatToRelCompTable
  IS
  BEGIN
    dbms_output.put_line('Relief_Feat_To_Rel_Comp table is being copied...');
    insert into relief_feat_to_rel_comp select * from relief_feat_to_rel_comp_v2;
    dbms_output.put_line('Relief_Feat_To_Rel_Comp table copy is completed.');
  end;

  PROCEDURE fillReliefFeatureTable
  IS
    -- variables --
    CURSOR relief_feature_v2 IS select * from relief_feature_v2 order by id;
  BEGIN
    dbms_output.put_line('Relief_Feature table is being copied...');
    for relief_feature in relief_feature_v2 loop

        insert into relief_feature
        (ID,LOD)
        values
        (relief_feature.ID,relief_feature.LOD);

        -- Insert the name and the description of the relief feature
        -- into the cityobject table
        update cityobject
        set
        name = relief_feature.name,
        name_codespace = relief_feature.name_codespace,
        description = relief_feature.description
        where id = relief_feature.id;
    end loop;
    dbms_output.put_line('Relief_Feature table copy is completed.');
  end;

  PROCEDURE fillSolitaryVegetatObjectTable
  IS
    -- variables --
    CURSOR solitary_vegetat_object_v2 IS select * from solitary_vegetat_object_v2 order by id;
  BEGIN
    dbms_output.put_line('Solitary_Vegetat_Object table is being copied...');
    for solitary_vegetat_object in solitary_vegetat_object_v2 loop
    
        -- Update the cityobject_id entry in surface_geometry table
        IF solitary_vegetat_object.LOD1_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||solitary_vegetat_object.ID||' 
                              where ID = ' || solitary_vegetat_object.LOD1_GEOMETRY_ID;
        END IF;
        IF solitary_vegetat_object.LOD2_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||solitary_vegetat_object.ID||' 
                              where ID = ' || solitary_vegetat_object.LOD2_GEOMETRY_ID;
        END IF;
        IF solitary_vegetat_object.LOD3_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||solitary_vegetat_object.ID||' 
                              where ID = ' || solitary_vegetat_object.LOD3_GEOMETRY_ID;
        END IF;
        IF solitary_vegetat_object.LOD4_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||solitary_vegetat_object.ID||' 
                              where ID = ' || solitary_vegetat_object.LOD4_GEOMETRY_ID;
        END IF;

        insert into solitary_vegetat_object
        (ID,CLASS,SPECIES,FUNCTION,HEIGHT,TRUNC_DIAMETER,CROWN_DIAMETER,
        LOD1_BREP_ID,LOD2_BREP_ID,LOD3_BREP_ID,LOD4_BREP_ID,
        LOD1_IMPLICIT_REP_ID,LOD2_IMPLICIT_REP_ID,LOD3_IMPLICIT_REP_ID,
        LOD4_IMPLICIT_REP_ID,LOD1_IMPLICIT_REF_POINT,LOD2_IMPLICIT_REF_POINT,
        LOD3_IMPLICIT_REF_POINT,LOD4_IMPLICIT_REF_POINT,
        LOD1_IMPLICIT_TRANSFORMATION,LOD2_IMPLICIT_TRANSFORMATION,
        LOD3_IMPLICIT_TRANSFORMATION,LOD4_IMPLICIT_TRANSFORMATION)
        values
        (solitary_vegetat_object.ID,replace(solitary_vegetat_object.CLASS,' ','--/\--'),
        solitary_vegetat_object.SPECIES,replace(solitary_vegetat_object.FUNCTION,' ','--/\--'),
        solitary_vegetat_object.HEIGHT,solitary_vegetat_object.TRUNC_DIAMETER,
        solitary_vegetat_object.CROWN_DIAMETER, solitary_vegetat_object.LOD1_GEOMETRY_ID,
        solitary_vegetat_object.LOD2_GEOMETRY_ID,
        solitary_vegetat_object.LOD3_GEOMETRY_ID,
        solitary_vegetat_object.LOD4_GEOMETRY_ID,
        solitary_vegetat_object.LOD1_IMPLICIT_REP_ID,
        solitary_vegetat_object.LOD2_IMPLICIT_REP_ID,
        solitary_vegetat_object.LOD3_IMPLICIT_REP_ID,
        solitary_vegetat_object.LOD4_IMPLICIT_REP_ID,
        solitary_vegetat_object.LOD1_IMPLICIT_REF_POINT,
        solitary_vegetat_object.LOD2_IMPLICIT_REF_POINT,
        solitary_vegetat_object.LOD3_IMPLICIT_REF_POINT,
        solitary_vegetat_object.LOD4_IMPLICIT_REF_POINT,
        solitary_vegetat_object.LOD1_IMPLICIT_TRANSFORMATION,
        solitary_vegetat_object.LOD2_IMPLICIT_TRANSFORMATION,
        solitary_vegetat_object.LOD3_IMPLICIT_TRANSFORMATION,
        solitary_vegetat_object.LOD4_IMPLICIT_TRANSFORMATION);

        -- Insert the name and the description of the solitary vegetation object
        -- into the cityobject table
        update cityobject
        set name = solitary_vegetat_object.name,
        name_codespace = solitary_vegetat_object.name_codespace,
        description = solitary_vegetat_object.description
        where id = solitary_vegetat_object.id;
    end loop;
    dbms_output.put_line('Solitary_Vegetat_Object table copy is completed.');
  end;

  PROCEDURE fillTextureParamTable
  IS
    -- variables --
    CURSOR textureparam_v2 IS select * from textureparam_v2 order by surface_geometry_id;
  BEGIN
    dbms_output.put_line('TextureParam table is being copied...');
    for textureparam in textureparam_v2 loop
        insert into textureparam
        (SURFACE_GEOMETRY_ID,IS_TEXTURE_PARAMETRIZATION,WORLD_TO_TEXTURE,
        TEXTURE_COORDINATES,SURFACE_DATA_ID)
        values
        (textureparam.SURFACE_GEOMETRY_ID,textureparam.IS_TEXTURE_PARAMETRIZATION,
        textureparam.WORLD_TO_TEXTURE,
        convertVarcharToSDOGeom(textureparam.TEXTURE_COORDINATES),
        textureparam.SURFACE_DATA_ID);
    end loop;
    dbms_output.put_line('TextureParam table copy is completed.');
  end;

  PROCEDURE fillTinReliefTable
  IS
    CURSOR tin_relief_v2 IS select * from tin_relief_v2 order by id;
  BEGIN
    dbms_output.put_line('Tin Relief table is being copied...');
    for tin_relief in tin_relief_v2 loop
        -- Update the cityobject_id entry in surface_geometry table
        IF tin_relief.SURFACE_GEOMETRY_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||tin_relief.ID||' 
                              where ID = ' || tin_relief.SURFACE_GEOMETRY_ID;
        END IF;
        
        insert into tin_relief
        (ID,MAX_LENGTH,STOP_LINES,BREAK_LINES,CONTROL_POINTS, 
        SURFACE_GEOMETRY_ID)
        values
        (tin_relief.ID,tin_relief.MAX_LENGTH,tin_relief.STOP_LINES, 
        tin_relief.BREAK_LINES,tin_relief.CONTROL_POINTS,
        tin_relief.SURFACE_GEOMETRY_ID);
    end loop;
    dbms_output.put_line('Tin Relief table copy is completed.');
  end;

  PROCEDURE fillTrafficAreaTable
  IS
    -- variables --
    CURSOR traffic_area_v2 IS select * from traffic_area_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('Traffic_Area table is being copied...');
    for traffic_area in traffic_area_v2 loop
        -- Fetch the objectclass id
        IF traffic_area.is_auxiliary IS NOT NULL THEN
          IF traffic_area.is_auxiliary = 1 THEN
              select id into classID from OBJECTCLASS where classname = 'AuxiliaryTrafficArea';
          ELSE
              select id into classID from OBJECTCLASS where classname = 'TrafficArea';
          END IF;
        END IF;
        
        -- Update the cityobject_id entry in surface_geometry table
        IF traffic_area.LOD2_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||traffic_area.ID||' 
                              where ID = ' || traffic_area.LOD2_MULTI_SURFACE_ID;
        END IF;
        IF traffic_area.LOD3_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||traffic_area.ID||' 
                              where ID = ' || traffic_area.LOD3_MULTI_SURFACE_ID;
        END IF;
        IF traffic_area.LOD4_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||traffic_area.ID||' 
                              where ID = ' || traffic_area.LOD4_MULTI_SURFACE_ID;
        END IF;

        insert into traffic_area
        (ID,OBJECTCLASS_ID,FUNCTION,USAGE,SURFACE_MATERIAL,LOD2_MULTI_SURFACE_ID,
        LOD3_MULTI_SURFACE_ID,LOD4_MULTI_SURFACE_ID,TRANSPORTATION_COMPLEX_ID)
        values
        (traffic_area.ID,classID,replace(traffic_area.FUNCTION,' ','--/\--'),replace(traffic_area.USAGE,' ','--/\--'),
        traffic_area.SURFACE_MATERIAL,traffic_area.LOD2_MULTI_SURFACE_ID,
        traffic_area.LOD3_MULTI_SURFACE_ID,traffic_area.LOD4_MULTI_SURFACE_ID,
        traffic_area.TRANSPORTATION_COMPLEX_ID);

        -- Insert the name and the description of the traffic area
        -- into the cityobject table
        update cityobject
        set
        name = traffic_area.name,
        name_codespace = traffic_area.name_codespace,
        description = traffic_area.description
        where id = traffic_area.id;
    end loop;
    dbms_output.put_line('Traffic_Area table copy is completed.');
  end;

  PROCEDURE fillTransportationComplex
  IS
    -- variables --
    CURSOR transportation_complex_v2 IS select * from transportation_complex_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('Transportation_Complex table is being copied...');
    for transportation_complex in transportation_complex_v2 loop
        -- Check Type
        IF transportation_complex.type IS NOT NULL THEN
           select id into classID from OBJECTCLASS where classname = transportation_complex.type;
        END IF;
        
        -- Update the cityobject_id entry in surface_geometry table
        IF transportation_complex.LOD1_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||transportation_complex.ID||' 
                              where ID = ' || transportation_complex.LOD1_MULTI_SURFACE_ID;
        END IF;
        IF transportation_complex.LOD2_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||transportation_complex.ID||' 
                              where ID = ' || transportation_complex.LOD2_MULTI_SURFACE_ID;
        END IF;
        IF transportation_complex.LOD3_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||transportation_complex.ID||' 
                              where ID = ' || transportation_complex.LOD3_MULTI_SURFACE_ID;
        END IF;
        IF transportation_complex.LOD4_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||transportation_complex.ID||' 
                              where ID = ' || transportation_complex.LOD4_MULTI_SURFACE_ID;
        END IF;

        insert into transportation_complex
        (ID,OBJECTCLASS_ID,FUNCTION,USAGE,LOD0_NETWORK,
        LOD1_MULTI_SURFACE_ID,LOD2_MULTI_SURFACE_ID,LOD3_MULTI_SURFACE_ID,
        LOD4_MULTI_SURFACE_ID)
        values
        (transportation_complex.ID,classID,replace(transportation_complex.FUNCTION,' ','--/\--'),
        replace(transportation_complex.USAGE,' ','--/\--'),transportation_complex.LOD0_NETWORK,
        transportation_complex.LOD1_MULTI_SURFACE_ID,
        transportation_complex.LOD2_MULTI_SURFACE_ID,
        transportation_complex.LOD3_MULTI_SURFACE_ID,
        transportation_complex.LOD4_MULTI_SURFACE_ID);

        -- Insert the name and the description of the transportation complex
        -- into the cityobject table
        update cityobject
        set
        name = transportation_complex.name,
        name_codespace = transportation_complex.name_codespace,
        description = transportation_complex.description
        where id = transportation_complex.id;
    end loop;
    dbms_output.put_line('Transportation_Complex table copy is completed.');
  end;

  PROCEDURE fillWaterBodyTable
  IS
    -- variables --
    CURSOR waterbody_v2 IS select * from waterbody_v2 order by id;
  BEGIN
    dbms_output.put_line('WaterBody table is being copied...');
    for waterbody in waterbody_v2 loop
        -- Update the cityobject_id entry in surface_geometry table
        IF waterbody.LOD0_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterbody.ID||' 
                              where ID = ' || waterbody.LOD0_MULTI_SURFACE_ID;
        END IF;     
        IF waterbody.LOD1_MULTI_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterbody.ID||' 
                              where ID = ' || waterbody.LOD1_MULTI_SURFACE_ID;
        END IF;
        IF waterbody.LOD1_SOLID_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterbody.ID||' 
                              where ID = ' || waterbody.LOD1_SOLID_ID;
        END IF;
        IF waterbody.LOD2_SOLID_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterbody.ID||' 
                              where ID = ' || waterbody.LOD2_SOLID_ID;
        END IF;
        IF waterbody.LOD3_SOLID_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterbody.ID||' 
                              where ID = ' || waterbody.LOD3_SOLID_ID;
        END IF;
        IF waterbody.LOD4_SOLID_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterbody.ID||' 
                              where ID = ' || waterbody.LOD4_SOLID_ID;
        END IF;
      
        insert into waterbody
        (ID,CLASS,FUNCTION,USAGE,LOD0_MULTI_CURVE,LOD1_MULTI_CURVE,
        LOD0_MULTI_SURFACE_ID,LOD1_MULTI_SURFACE_ID,LOD1_SOLID_ID,
        LOD2_SOLID_ID,LOD3_SOLID_ID,LOD4_SOLID_ID)
        values
        (waterbody.ID,replace(waterbody.CLASS,' ','--/\--'),replace(waterbody.FUNCTION,' ','--/\--'),replace(waterbody.USAGE,' ','--/\--'),
        waterbody.LOD0_MULTI_CURVE,waterbody.LOD1_MULTI_CURVE,
        waterbody.LOD0_MULTI_SURFACE_ID,waterbody.LOD1_MULTI_SURFACE_ID,
        waterbody.LOD1_SOLID_ID,waterbody.LOD2_SOLID_ID,
        waterbody.LOD3_SOLID_ID,waterbody.LOD4_SOLID_ID);

        -- Insert the name and the description of the waterbody
        -- into the cityobject table
        update cityobject
        set
        name = waterbody.name,
        name_codespace = waterbody.name_codespace,
        description = waterbody.description
        where id = waterbody.id;
    end loop;
    dbms_output.put_line('WaterBody table copy is completed.');
  end;

  PROCEDURE fillWaterBoundarySurfaceTable
  IS
    -- variables --
    CURSOR waterboundary_surface_v2 IS select * from waterboundary_surface_v2 order by id;
    classID NUMBER(10);
  BEGIN
    dbms_output.put_line('WaterBoundary_Surface table is being copied...');
    for waterboundary_surface in waterboundary_surface_v2 loop
        -- Check Type
        IF waterboundary_surface.type IS NOT NULL THEN
           select id into classID from OBJECTCLASS where classname = waterboundary_surface.type;
        END IF;
        
        -- Update the cityobject_id entry in surface_geometry table
        IF waterboundary_surface.LOD2_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterboundary_surface.ID||' 
                              where ID = ' || waterboundary_surface.LOD2_SURFACE_ID;
        END IF;
        IF waterboundary_surface.LOD3_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterboundary_surface.ID||' 
                              where ID = ' || waterboundary_surface.LOD3_SURFACE_ID;
        END IF;
        IF waterboundary_surface.LOD4_SURFACE_ID IS NOT NULL THEN         
           EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = '||waterboundary_surface.ID||' 
                              where ID = ' || waterboundary_surface.LOD4_SURFACE_ID;
        END IF;

        insert into waterboundary_surface
        (ID,OBJECTCLASS_ID,WATER_LEVEL,LOD2_SURFACE_ID,LOD3_SURFACE_ID,
        LOD4_SURFACE_ID)
        values
        (waterboundary_surface.ID,classID,waterboundary_surface.WATER_LEVEL,
        waterboundary_surface.LOD2_SURFACE_ID,waterboundary_surface.LOD3_SURFACE_ID,
        waterboundary_surface.LOD4_SURFACE_ID);

        -- Insert the name and the description of the water boundary surface
        -- into the cityobject table
        update cityobject
        set
        name = waterboundary_surface.name,
        name_codespace = waterboundary_surface.name_codespace,
        description = waterboundary_surface.description
        where id = waterboundary_surface.id;
    end loop;
    dbms_output.put_line('WaterBoundary_Surface table copy is completed.');
  end;

  PROCEDURE fillWaterbodToWaterbndSrfTable
  IS
  BEGIN
    dbms_output.put_line('Waterbod_To_Waterbnd_Srf table is being copied...');
    insert into waterbod_to_waterbnd_srf select * from waterbod_to_waterbnd_srf_v2;
    dbms_output.put_line('Waterbod_To_Waterbnd_Srf table copy is completed.');
  end;

  PROCEDURE updateSurfaceGeoTableCityObj
  IS
    -- variables --    
    CURSOR surface_geometry_v3 IS select * from surface_geometry order by id;
    CURSOR surface_geometry_xv3 IS select * from surface_geometry where parent_id in 
                                  (select s.id from 
                                  implicit_geometry i, surface_geometry s
                                  where i.relative_brep_id = s.id 
                                  and s.is_xlink = 0);
  BEGIN
    dbms_output.put_line('Surface_Geometry table is being updated...');
    for surface_geometry in surface_geometry_v3 loop        
        IF (surface_geometry.CITYOBJECT_ID IS NOT NULL) THEN
	    EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = :1 where PARENT_ID = :2'
                              USING surface_geometry.CITYOBJECT_ID, 
                              surface_geometry.ID;
        END IF;
	 IF (surface_geometry.IS_SOLID = 1) THEN
	    EXECUTE IMMEDIATE 'update surface_geometry set 
                              CITYOBJECT_ID = :1 where ROOT_ID = :2'
                              USING surface_geometry.CITYOBJECT_ID, 
                              surface_geometry.ID;
	 END IF;
    end loop;
    for surface_geometry_x in surface_geometry_xv3 loop        
        IF (surface_geometry_x.GEOMETRY IS NOT NULL) THEN
          EXECUTE IMMEDIATE 'update surface_geometry set IMPLICIT_GEOMETRY = :1, 
          GEOMETRY = null where ID = :2' 
          USING surface_geometry_x.GEOMETRY, surface_geometry_x.ID;
        END IF;
    end loop;
    dbms_output.put_line('Surface_Geometry table is updated.');
  end;

END geodb_migrate_v2_v3;
/