-- ENABLE_VERSIONING2.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Zhihang Yao <zhihang.yao@tum.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--              Philipp Willkomm <pwillkomm@moss.de>
--              Gerhard K�nig <gerhard.koenig@tu-berlin.de>
--              Alexandra Lorenz <di.alex.lorenz@googlemail.com>
--
-- Copyright:   (c) 2012-2014  Chair of Geoinformatics,
--                             Technische Universit�t M�nchen, Germany
--                             http://www.gis.bv.tum.de
--
--              (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
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
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 3.0.0     2015-03-05   added support for Oracle Locator            ZYao
-- 3.0.0     2013-12-06   new version for 3DCityDB V3                 ZYao
--                                                                    TKol
--                                                                    CNag
--                                                                    PWil
-- 2.0.1     2008-06-28   log message included                        TKol
-- 2.0.0     2007-11-23   release version                             TKol
--                                                                    GKoe
--                                                                    CNag
--

SELECT 'EnableVersioning procedure is working, that takes a while.' as message from DUAL;

-- for Oracle locator, the RASTER_RELIEF and GRID_COVERAGE_RDT tables are not exist
BEGIN
  IF ('&DBVERSION'='S' or '&DBVERSION'='s') THEN
    DBMS_WM.EnableVersioning('ADDRESS,ADDRESS_TO_BRIDGE,ADDRESS_TO_BUILDING,APPEAR_TO_SURFACE_DATA,APPEARANCE,BREAKLINE_RELIEF,BRIDGE,BRIDGE_CONSTR_ELEMENT,BRIDGE_FURNITURE,BRIDGE_INSTALLATION,BRIDGE_OPEN_TO_THEM_SRF,BRIDGE_OPENING,BRIDGE_ROOM,BRIDGE_THEMATIC_SURFACE,BUILDING,BUILDING_FURNITURE,BUILDING_INSTALLATION,CITY_FURNITURE,CITYMODEL,CITYOBJECT,CITYOBJECT_GENERICATTRIB,CITYOBJECT_MEMBER,CITYOBJECTGROUP,EXTERNAL_REFERENCE,GENERALIZATION,GENERIC_CITYOBJECT,GROUP_TO_CITYOBJECT,IMPLICIT_GEOMETRY,LAND_USE,MASSPOINT_RELIEF,OPENING,OPENING_TO_THEM_SURFACE,PLANT_COVER,GRID_COVERAGE_RDT,RASTER_RELIEF,RELIEF_COMPONENT,RELIEF_FEAT_TO_REL_COMP,RELIEF_FEATURE,ROOM,SOLITARY_VEGETAT_OBJECT,SURFACE_DATA,SURFACE_GEOMETRY,TEX_IMAGE,TEXTUREPARAM,THEMATIC_SURFACE,TIN_RELIEF,TRAFFIC_AREA,TRANSPORTATION_COMPLEX,TUNNEL,TUNNEL_FURNITURE,TUNNEL_HOLLOW_SPACE,TUNNEL_INSTALLATION,TUNNEL_OPEN_TO_THEM_SRF,TUNNEL_OPENING,TUNNEL_THEMATIC_SURFACE,WATERBOD_TO_WATERBND_SRF,WATERBODY,WATERBOUNDARY_SURFACE','VIEW_WO_OVERWRITE'); 
  ELSE
  	DBMS_WM.EnableVersioning('ADDRESS,ADDRESS_TO_BRIDGE,ADDRESS_TO_BUILDING,APPEAR_TO_SURFACE_DATA,APPEARANCE,BREAKLINE_RELIEF,BRIDGE,BRIDGE_CONSTR_ELEMENT,BRIDGE_FURNITURE,BRIDGE_INSTALLATION,BRIDGE_OPEN_TO_THEM_SRF,BRIDGE_OPENING,BRIDGE_ROOM,BRIDGE_THEMATIC_SURFACE,BUILDING,BUILDING_FURNITURE,BUILDING_INSTALLATION,CITY_FURNITURE,CITYMODEL,CITYOBJECT,CITYOBJECT_GENERICATTRIB,CITYOBJECT_MEMBER,CITYOBJECTGROUP,EXTERNAL_REFERENCE,GENERALIZATION,GENERIC_CITYOBJECT,GROUP_TO_CITYOBJECT,IMPLICIT_GEOMETRY,LAND_USE,MASSPOINT_RELIEF,OPENING,OPENING_TO_THEM_SURFACE,PLANT_COVER,RELIEF_COMPONENT,RELIEF_FEAT_TO_REL_COMP,RELIEF_FEATURE,ROOM,SOLITARY_VEGETAT_OBJECT,SURFACE_DATA,SURFACE_GEOMETRY,TEX_IMAGE,TEXTUREPARAM,THEMATIC_SURFACE,TIN_RELIEF,TRAFFIC_AREA,TRANSPORTATION_COMPLEX,TUNNEL,TUNNEL_FURNITURE,TUNNEL_HOLLOW_SPACE,TUNNEL_INSTALLATION,TUNNEL_OPEN_TO_THEM_SRF,TUNNEL_OPENING,TUNNEL_THEMATIC_SURFACE,WATERBOD_TO_WATERBND_SRF,WATERBODY,WATERBOUNDARY_SURFACE','VIEW_WO_OVERWRITE');  
  END IF;
END;
/

