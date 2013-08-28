-- STAT.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--
-- Conversion:	Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2013  Institute for Geodesy and Geoinformation Science,
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
-- 1.1       2013-02-22   PostGIS version   CNag     FKun
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
  report := array_append(report, (E'#CITYMODEL:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM cityobject_member;
  report := array_append(report, (E'#CITYOBJECT_MEMBER:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM cityobject;
  report := array_append(report, (E'#CITYOBJECT:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM generalization;
  report := array_append(report, (E'#GENERALIZATION:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM external_reference;
  report := array_append(report, (E'#EXTERNAL_REFERENCE:\t\t' || cnt));

  -- Geometry
  SELECT COUNT(*) INTO cnt FROM implicit_geometry;
  report := array_append(report, (E'#IMPLICIT_GEOMETRY:\t\t' || cnt));  
  SELECT COUNT(*) INTO cnt FROM surface_geometry;
  report := array_append(report, (E'#SURFACE_GEOMETRY:\t\t' || cnt));

  -- Building
  SELECT COUNT(*) INTO cnt FROM address;
  report := array_append(report, (E'#ADDRESS:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM address_to_building;
  report := array_append(report, (E'#ADDRESS_TO_BUILDING:\t\t' || cnt));  
  SELECT COUNT(*) INTO cnt FROM building;
  report := array_append(report, (E'#BUILDING:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM building_furniture;
  report := array_append(report, (E'#BUILDING_FURNITURE:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM building_installation;
  report := array_append(report, (E'#BUILDING_INSTALLATION:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM opening;
  report := array_append(report, (E'#OPENING:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM opening_to_them_surface;
  report := array_append(report, (E'#OPENING_TO_THEM_SURFACE:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM room;
  report := array_append(report, (E'#ROOM:\t\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM thematic_surface;
  report := array_append(report, (E'#THEMATIC_SURFACE:\t\t' || cnt));

  -- CityFurniture
  SELECT COUNT(*) INTO cnt FROM city_furniture;
  report := array_append(report, (E'#CITY_FURNITURE:\t\t' || cnt));

  -- CityObjectGroup
  SELECT COUNT(*) INTO cnt FROM cityobjectgroup;
  report := array_append(report, (E'#CITYOBJECTGROUP:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM group_to_cityobject;
  report := array_append(report, (E'#GROUP_TO_CITYOBJECT:\t\t' || cnt));

  -- LandUse
  SELECT COUNT(*) INTO cnt FROM land_use;
  report := array_append(report, (E'#LAND_USE:\t\t\t' || cnt));

  -- Relief
  SELECT COUNT(*) INTO cnt FROM relief_feature;
  report := array_append(report, (E'#RELIEF_FEATURE:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM relief_component;
  report := array_append(report, (E'#RELIEF_COMPONENT:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM relief_feat_to_rel_comp;
  report := array_append(report, (E'#RELIEF_FEAT_TO_REL_COMP:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM tin_relief;
  report := array_append(report, (E'#TIN_RELIEF:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM breakline_relief;
  report := array_append(report, (E'#BREAKLINE_RELIEF:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM masspoint_relief;
  report := array_append(report, (E'#MASSPOINT_RELIEF:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM raster_relief;
  report := array_append(report, (E'#RASTER_RELIEF:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM relief;
  report := array_append(report, (E'#RELIEF:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM orthophoto;
  report := array_append(report, (E'#ORTHOPHOTO:\t\t\t' || cnt));

  -- Transportation
  SELECT COUNT(*) INTO cnt FROM transportation_complex;
  report := array_append(report, (E'#TRANSPORTATION_COMPLEX:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM traffic_area;
  report := array_append(report, (E'#TRAFFIC_AREA:\t\t\t' || cnt));

  -- Vegetation
  SELECT COUNT(*) INTO cnt FROM plant_cover;
  report := array_append(report, (E'#PLANT_COVER:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM solitary_vegetat_object;
  report := array_append(report, (E'#SOLITARY_VEGETAT_OBJECT:\t' || cnt));

  -- WaterBody
  SELECT COUNT(*) INTO cnt FROM waterbody;
  report := array_append(report, (E'#WATERBODY:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM waterboundary_surface;
  report := array_append(report, (E'#WATERBOUNDARY_SURFACE:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM waterbod_to_waterbnd_srf;
  report := array_append(report, (E'#WATERBOD_TO_WATERBND_SRF:\t' || cnt));

  -- GenericCityObject
  SELECT COUNT(*) INTO cnt FROM generic_cityobject;
  report := array_append(report, (E'#GENERIC_CITYOBJECT:\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM cityobject_genericattrib;
  report := array_append(report, (E'#CITYOBJECT_GENERICATTRIB:\t' || cnt));

  -- Appearance
  SELECT COUNT(*) INTO cnt FROM appearance;
  report := array_append(report, (E'#APPEARANCE:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM surface_data;
  report := array_append(report, (E'#SURFACE_DATA:\t\t\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM appear_to_surface_data;
  report := array_append(report, (E'#APPEAR_TO_SURFACE_DATA:\t' || cnt));
  SELECT COUNT(*) INTO cnt FROM textureparam;
  report := array_append(report, (E'#TEXTUREPARAM:\t\t\t' || cnt));  

  RETURN report;
END; 
$$
LANGUAGE plpgsql;