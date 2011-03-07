-- GEODB_REPORT.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <kolbe@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--
-- Copyright:   (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universität Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--              (c) 2005-2006, lat/lon GmbH, Germany
--                             http://www.lat-lon.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 1.0       2006-04-03   release version                             LPlu
--                                                                    TKol
--                                                                    GGro
--                                                                    JSch
--                                                                    VStr
--                                                                    APot
--

CREATE OR REPLACE  PROCEDURE GEODB_REPORT  IS
  -- lokale Variablen
  ws VARCHAR2(30);
  cnt NUMBER;
  refreshDate DATE;
  reportDate DATE;
  pa_id PLANNING_ALTERNATIVE.ID%TYPE;
  pa_title PLANNING_ALTERNATIVE.TITLE%TYPE;

BEGIN
  DBMS_OUTPUT.ENABLE;
  DBMS_OUTPUT.PUT(CHR(10));
  SELECT SYSDATE INTO reportDate FROM DUAL;
  DBMS_OUTPUT.PUT('Database Report on 3D City Model - Report date: ');
  DBMS_OUTPUT.PUT_LINE(TO_CHAR(reportDate, 'DD.MM.YYYY HH24:MI:SS'));
  DBMS_OUTPUT.PUT_LINE('===================================================================');
  DBMS_OUTPUT.PUT(CHR(10));

  -- Determine current workspace
  ws := DBMS_WM.GetWorkspace;
  DBMS_OUTPUT.PUT('Current workspace: ' || ws);

  IF ws != 'LIVE' THEN
    -- Get associated planning alternative
    SELECT id,title INTO pa_id,pa_title FROM PLANNING_ALTERNATIVE
    WHERE workspace_name=ws;
    DBMS_OUTPUT.PUT_LINE(' (PlanningAlternative ID ' || pa_id ||': "' || pa_title || '")');

    -- Query date of last refresh
    SELECT createtime INTO refreshDate
	  FROM all_workspace_savepoints
 	  WHERE savepoint='refreshed' AND workspace=ws;
    DBMS_OUTPUT.PUT_LINE('Last refresh from LIVE workspace: ' || TO_CHAR(refreshDate, 'DD.MM.YYYY HH24:MI:SS'));
  END IF;
  DBMS_OUTPUT.NEW_LINE;
  DBMS_OUTPUT.PUT(CHR(10));

  SELECT count(*) INTO cnt FROM citymodel;
  DBMS_OUTPUT.PUT_LINE('#CITYMODEL:               ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM cityobject;
  DBMS_OUTPUT.PUT_LINE('#CITYOBJECT: 		  ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM cityobject_genericattrib;
  DBMS_OUTPUT.PUT_LINE('#CITYOBJECT_GENERICATTRIB:' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM cityobject_member;
  DBMS_OUTPUT.PUT_LINE('#CITYOBJECT_MEMBER:       ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM city_furniture;
  DBMS_OUTPUT.PUT_LINE('#CITY_FURNITURE:          ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM external_reference;
  DBMS_OUTPUT.PUT_LINE('#EXTERNAL_REFERENCE:      ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM generalization;
  DBMS_OUTPUT.PUT_LINE('#GENERALIZATION:          ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM generic_cityobject;
  DBMS_OUTPUT.PUT_LINE('#GENERIC_CITYOBJECT:      ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM implicit_geometry;
  DBMS_OUTPUT.PUT_LINE('#IMPLICIT_GEOMETRY:       ' || SUBSTR('        ' || cnt,-8));  
  SELECT count(*) INTO cnt FROM objectclass;
  DBMS_OUTPUT.PUT_LINE('#OBJECTCLASS:             ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM surface_geometry;
  DBMS_OUTPUT.PUT_LINE('#SURFACE_GEOMETRY:        ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM cityobjectgroup;
  DBMS_OUTPUT.PUT_LINE('#CITYOBJECTGROUP:         ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM group_to_cityobject;
  DBMS_OUTPUT.PUT_LINE('#GROUP_TO_CITYOBJECT:     ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM address;
  DBMS_OUTPUT.PUT_LINE('#ADDRESS:                 ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM address_to_building;
  DBMS_OUTPUT.PUT_LINE('#ADDRESS_TO_BUILDING:     ' || SUBSTR('        ' || cnt,-8));  
  SELECT count(*) INTO cnt FROM building;
  DBMS_OUTPUT.PUT_LINE('#BUILDING:                ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM building_furniture;
  DBMS_OUTPUT.PUT_LINE('#BUILDING_FURNITURE:      ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM building_installation;
  DBMS_OUTPUT.PUT_LINE('#BUILDING_INSTALLATION:   ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM opening;
  DBMS_OUTPUT.PUT_LINE('#OPENING:                 ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM opening_to_them_surface;
  DBMS_OUTPUT.PUT_LINE('#OPENING_TO_THEM_SURFACE: ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM room;
  DBMS_OUTPUT.PUT_LINE('#ROOM:                    ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM thematic_surface;
  DBMS_OUTPUT.PUT_LINE('#THEMATIC_SURFACE:        ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM appearance;
  DBMS_OUTPUT.PUT_LINE('#APPEARANCE:              ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM surface_data;
  DBMS_OUTPUT.PUT_LINE('#SURFACE_DATA:            ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM textureparam;
  DBMS_OUTPUT.PUT_LINE('#TEXTUREPARAM:            ' || SUBSTR('        ' || cnt,-8));  
  SELECT count(*) INTO cnt FROM appear_to_surface_data;
  DBMS_OUTPUT.PUT_LINE('#APPEAR_TO_SURFACE_DATA:  ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM breakline_relief;
  DBMS_OUTPUT.PUT_LINE('#BREAKLINE_RELIEF:        ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM masspoint_relief;
  DBMS_OUTPUT.PUT_LINE('#MASSPOINT_RELIEF:        ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM raster_relief;
  DBMS_OUTPUT.PUT_LINE('#RASTER_RELIEF:           ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM raster_relief_imp;
  DBMS_OUTPUT.PUT_LINE('#RASTER_RELIEF_IMP:       ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM relief;
  DBMS_OUTPUT.PUT_LINE('#RELIEF:                  ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM relief_component;
  DBMS_OUTPUT.PUT_LINE('#RELIEF_COMPONENT:        ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM relief_feature;
  DBMS_OUTPUT.PUT_LINE('#RELIEF_FEATURE:          ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM relief;
  DBMS_OUTPUT.PUT_LINE('#RELIEF_FEAT_TO_REL_COMP: ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM tin_relief;
  DBMS_OUTPUT.PUT_LINE('#TIN_RELIEF:              ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM orthophoto;
  DBMS_OUTPUT.PUT_LINE('#ORTHOPHOTO:              ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM orthophoto_imp;
  DBMS_OUTPUT.PUT_LINE('#ORTHOPHOTO_IMP:          ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM transportation_complex;
  DBMS_OUTPUT.PUT_LINE('#TRANSPORTATION_COMPLEX:  ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM traffic_area;
  DBMS_OUTPUT.PUT_LINE('#TRAFFIC_AREA:            ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM land_use;
  DBMS_OUTPUT.PUT_LINE('#LAND_USE:                ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM plant_cover;
  DBMS_OUTPUT.PUT_LINE('#PLANT_COVER:             ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM solitary_vegetat_object;
  DBMS_OUTPUT.PUT_LINE('#SOLITARY_VEGETAT_OBJECT: ' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM waterbody;
  DBMS_OUTPUT.PUT_LINE('#WATERBODY:               ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM waterboundary_surface;
  DBMS_OUTPUT.PUT_LINE('#WATERBOUNDARY_SURFACE:   ' || SUBSTR('        ' || cnt,-8));
  SELECT count(*) INTO cnt FROM waterbod_to_waterbnd_srf;
  DBMS_OUTPUT.PUT_LINE('#WATERBOD_TO_WATERBND_SRF:' || SUBSTR('        ' || cnt,-8));

  SELECT count(*) INTO cnt FROM cityobject;
  DBMS_OUTPUT.PUT_LINE('#CITYOBJECT: 		  ' || SUBSTR('        ' || cnt,-8));

END GeoDB_Report;
/
