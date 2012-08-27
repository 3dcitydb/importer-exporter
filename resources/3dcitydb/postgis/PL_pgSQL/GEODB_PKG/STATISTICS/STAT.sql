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
--                             Technische Universitaet Berlin, Germany
--                             http://www.igg.tu-berlin.de
--              (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universitaet Bonn, Germany
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
-- Creates method for creating database statistics.
-- Part of the geodb_pkg.schema and STATISTICS-"Package"
-- Therefore the function starts with "stat_"-Prefix
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description     | Author | Conversion
-- 1.1       2012-02-26   PostGIS version   CNag     FKun
-- 1.0       2006-04-03   release version   LPlu
--                                          TKol
--                                          GGro
--                                          JSch
--                                          VStr
--                                          APot
--

CREATE OR REPLACE FUNCTION geodb_pkg.stat_table_contents() RETURNS text[] AS $$
DECLARE
  report text[] = '{}';
  cnt NUMERIC;
  reportDate TIMESTAMP;
  
BEGIN
  reportDate := NOW();  
  report := array_append(report, 'Database Report on 3D City Model - Report date: ' || TO_CHAR(reportDate, 'DD.MM.YYYY HH24:MI:SS'));
  report := array_append(report, '===================================================================');

  PERFORM array_append(report, '');

  SELECT COUNT(*) INTO cnt FROM citymodel;
  report := array_append(report, ('#CITYMODEL:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM cityobject_member;
  report := array_append(report, ('#CITYOBJECT_MEMBER:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM cityobject;
  report := array_append(report, ('#CITYOBJECT:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM generalization;
  report := array_append(report, ('#GENERALIZATION:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM external_reference;
  report := array_append(report, ('#EXTERNAL_REFERENCE:\t\t' || cnt));

  -- Geometry
  SELECT COUNT(*) INTO cnt FROM implicit_geometry;
  report := array_append(report, ('#IMPLICIT_GEOMETRY:\t\t' || cnt));  
  SELECT COUNT(*) INTO cnt FROM surface_geometry;
  report := array_append(report, ('#SURFACE_GEOMETRY:\t\t' || cnt));

  -- Building
  SELECT COUNT(*) INTO cnt FROM address;
  report := array_append(report, ('#ADDRESS:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM address_to_building;
  report := array_append(report, ('#ADDRESS_TO_BUILDING:\t\t' || cnt));  
  SELECT COUNT(*) INTO cnt FROM building;
  report := array_append(report, ('#BUILDING:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM building_furniture;
  report := array_append(report, ('#BUILDING_FURNITURE:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM building_installation;
  report := array_append(report, ('#BUILDING_INSTALLATION:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM opening;
  report := array_append(report, ('#OPENING:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM opening_to_them_surface;
  report := array_append(report, ('#OPENING_TO_THEM_SURFACE:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM room;
  report := array_append(report, ('#ROOM:\t\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM thematic_surface;
  report := array_append(report, ('#THEMATIC_SURFACE:\t\t' || cnt));

  -- CityFurniture
  SELECT COUNT(*) INTO cnt FROM city_furniture;
  report := array_append(report, ('#CITY_FURNITURE:\t\t' || cnt));

  -- CityObjectGroup
  SELECT COUNT(*) INTO cnt FROM cityobjectgroup;
  report := array_append(report, ('#CITYOBJECTGROUP:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM group_to_cityobject;
  report := array_append(report, ('#GROUP_TO_CITYOBJECT:\t\t' || cnt));

  -- LandUse
  SELECT COUNT(*) INTO cnt FROM land_use;
  report := array_append(report, ('#LAND_USE:\t\t\t' || cnt));

  -- Relief
  SELECT COUNT(*) INTO cnt FROM relief_feature;
  report := array_append(report, ('#RELIEF_FEATURE:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM relief_component;
  report := array_append(report, ('#RELIEF_COMPONENT:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM relief_feat_to_rel_comp;
  report := array_append(report, ('#RELIEF_FEAT_TO_REL_COMP:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM tin_relief;
  report := array_append(report, ('#TIN_RELIEF:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM breakline_relief;
  report := array_append(report, ('#BREAKLINE_RELIEF:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM masspoint_relief;
  report := array_append(report, ('#MASSPOINT_RELIEF:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM raster_relief;
  report := array_append(report, ('#RASTER_RELIEF:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM relief;
  report := array_append(report, ('#RELIEF:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM orthophoto;
  report := array_append(report, ('#ORTHOPHOTO:\t\t\t' || cnt));

  -- Transportation
  SELECT COUNT(*) INTO cnt FROM transportation_complex;
  report := array_append(report, ('#TRANSPORTATION_COMPLEX:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM traffic_area;
  report := array_append(report, ('#TRAFFIC_AREA:\t\t\t' || cnt));

  -- Vegetation
  SELECT COUNT(*) INTO cnt FROM plant_cover;
  report := array_append(report, ('#PLANT_COVER:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM solitary_vegetat_object;
  report := array_append(report, ('#SOLITARY_VEGETAT_OBJECT:\t' || cnt));

  -- WaterBody
  SELECT COUNT(*) INTO cnt FROM waterbody;
  report := array_append(report, ('#WATERBODY:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM waterboundary_surface;
  report := array_append(report, ('#WATERBOUNDARY_SURFACE:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM waterbod_to_waterbnd_srf;
  report := array_append(report, ('#WATERBOD_TO_WATERBND_SRF:\t' || cnt));

  -- GenericCityObject
  SELECT COUNT(*) INTO cnt FROM generic_cityobject;
  report := array_append(report, ('#GENERIC_CITYOBJECT:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM cityobject_genericattrib;
  report := array_append(report, ('#CITYOBJECT_GENERICATTRIB:\t' || cnt));

  -- Appearance
  SELECT COUNT(*) INTO cnt FROM appearance;
  report := array_append(report, ('#APPEARANCE:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM surface_data;
  report := array_append(report, ('#SURFACE_DATA:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM appear_to_surface_data;
  report := array_append(report, ('#APPEAR_TO_SURFACE_DATA:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM textureparam;
  report := array_append(report, ('#TEXTUREPARAM:\t\t\t' || cnt));  

  RETURN report;
END; 
$$
LANGUAGE plpgsql;