-- STAT.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <kolbe@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--
-- Conversion:	Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--              (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universität Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--              (c) 2005-2006, lat/lon GmbH, Germany
--                             http://www.lat-lon.de--              
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- Creates package "geodb_stat" containing utility methods for creating
-- database statistics.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description     | Author | Conversion
-- 1.1       2012-02-26   release version   CNag	 FKun
-- 1.0       2006-04-03   release version   LPlu
--                                          TKol
--                                          GGro
--                                          JSch
--                                          VStr
--                                          APot
--

/******************************************************************
* versioning_status
*
******************************************************************/

CREATE FUNCTION geodb_pkg.stat_table_contents() RETURNS text[] AS $$
  DECLARE
    report text[];
    --ws VARCHAR(30);
    cnt NUMERIC;
    --refreshDate DATE;
    reportDate TIMESTAMP;
    --pa_id PLANNING_ALTERNATIVE.ID%TYPE;
    --pa_title PLANNING_ALTERNATIVE.TITLE%TYPE;
  
  BEGIN
    reportDate := NOW();  
    report[1] := ('Database Report on 3D City Model - Report date: ' || TO_CHAR(reportDate, 'DD.MM.YYYY HH24:MI:SS'));
    report[2] := '===================================================================';
  
    /*-- Determine current workspace
    ws := DBMS_WM.GetWorkspace;
    report := report || ('Current workspace: ' || ws);

	IF ws != 'LIVE' THEN
      -- Get associated planning alternative
      SELECT id,title INTO pa_id,pa_title FROM PLANNING_ALTERNATIVE
      WHERE workspace_name=ws;
      report := report || (' (PlanningAlternative ID ' || pa_id ||': "' || pa_title || '")');

      -- Query date of last refresh
      SELECT createtime INTO refreshDate
      FROM all_workspace_savepoints
      WHERE savepoint='refreshed' AND workspace=ws;
      report := report || ('Last refresh from LIVE workspace: ' || TO_CHAR(refreshDate, 'DD.MM.YYYY HH24:MI:SS'));
    END IF;
    */
	
	PERFORM array_append(report, '');
  
    SELECT COUNT(*) INTO cnt FROM citymodel;
    report := report || ('#CITYMODEL:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM cityobject_member;
    report := report || ('#CITYOBJECT_MEMBER:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM cityobject;
    report := report || ('#CITYOBJECT:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM generalization;
    report := report || ('#GENERALIZATION:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM external_reference;
    report := report || ('#EXTERNAL_REFERENCE:\t\t' || cnt);
  
    -- Geometry
    SELECT COUNT(*) INTO cnt FROM implicit_geometry;
    report := report || ('#IMPLICIT_GEOMETRY:\t\t' || cnt);  
    SELECT COUNT(*) INTO cnt FROM surface_geometry;
    report := report || ('#SURFACE_GEOMETRY:\t\t' || cnt);
  
    -- Building
    SELECT COUNT(*) INTO cnt FROM address;
    report := report || ('#ADDRESS:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM address_to_building;
    report := report || ('#ADDRESS_TO_BUILDING:\t\t' || cnt);  
    SELECT COUNT(*) INTO cnt FROM building;
    report := report || ('#BUILDING:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM building_furniture;
    report := report || ('#BUILDING_FURNITURE:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM building_installation;
    report := report || ('#BUILDING_INSTALLATION:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM opening;
    report := report || ('#OPENING:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM opening_to_them_surface;
    report := report || ('#OPENING_TO_THEM_SURFACE:\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM room;
    report := report || ('#ROOM:\t\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM thematic_surface;
    report := report || ('#THEMATIC_SURFACE:\t\t' || cnt);
  
    -- CityFurniture
    SELECT COUNT(*) INTO cnt FROM city_furniture;
    report := report || ('#CITY_FURNITURE:\t\t' || cnt);
  
    -- CityObjectGroup
    SELECT COUNT(*) INTO cnt FROM cityobjectgroup;
    report := report || ('#CITYOBJECTGROUP:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM group_to_cityobject;
    report := report || ('#GROUP_TO_CITYOBJECT:\t\t' || cnt);
  
    -- LandUse
    SELECT COUNT(*) INTO cnt FROM land_use;
    report := report || ('#LAND_USE:\t\t\t' || cnt);
  
    -- Relief
    SELECT COUNT(*) INTO cnt FROM relief_feature;
    report := report || ('#RELIEF_FEATURE:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM relief_component;
    report := report || ('#RELIEF_COMPONENT:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM relief_feat_to_rel_comp;
    report := report || ('#RELIEF_FEAT_TO_REL_COMP:\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM tin_relief;
    report := report || ('#TIN_RELIEF:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM breakline_relief;
    report := report || ('#BREAKLINE_RELIEF:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM masspoint_relief;
    report := report || ('#MASSPOINT_RELIEF:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM raster_relief;
    report := report || ('#RASTER_RELIEF:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM raster_relief_imp;
    report := report || ('#RASTER_RELIEF_IMP:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM relief;
    report := report || ('#RELIEF:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM orthophoto;
    report := report || ('#ORTHOPHOTO:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM orthophoto_imp;
    report := report || ('#ORTHOPHOTO_IMP:\t\t' || cnt);
  
    -- Transportation
    SELECT COUNT(*) INTO cnt FROM transportation_complex;
    report := report || ('#TRANSPORTATION_COMPLEX:\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM traffic_area;
    report := report || ('#TRAFFIC_AREA:\t\t\t' || cnt);
   
    -- Vegetation
    SELECT COUNT(*) INTO cnt FROM plant_cover;
    report := report || ('#PLANT_COVER:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM solitary_vegetat_object;
    report := report || ('#SOLITARY_VEGETAT_OBJECT:\t' || cnt);
  
    -- WaterBody
    SELECT COUNT(*) INTO cnt FROM waterbody;
    report := report || ('#WATERBODY:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM waterboundary_surface;
    report := report || ('#WATERBOUNDARY_SURFACE:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM waterbod_to_waterbnd_srf;
    report := report || ('#WATERBOD_TO_WATERBND_SRF:\t' || cnt);
    
    -- GenericCityObject
    SELECT COUNT(*) INTO cnt FROM generic_cityobject;
    report := report || ('#GENERIC_CITYOBJECT:\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM cityobject_genericattrib;
    report := report || ('#CITYOBJECT_GENERICATTRIB:\t' || cnt);
  
    -- Appearance
    SELECT COUNT(*) INTO cnt FROM appearance;
    report := report || ('#APPEARANCE:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM surface_data;
    report := report || ('#SURFACE_DATA:\t\t\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM appear_to_surface_data;
    report := report || ('#APPEAR_TO_SURFACE_DATA:\t' || cnt);
    SELECT COUNT(*) INTO cnt FROM textureparam;
    report := report || ('#TEXTUREPARAM:\t\t\t' || cnt);  
  
    RETURN report;
  END; 
$$
LANGUAGE plpgsql;