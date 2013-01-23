-- VACUUM_SPATIAL_COLUMNS.sql
--
-- Authors:     Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2012, Institute for Geodesy and Geoinformation Science,
--                             Technische Universitaet Berlin, Germany
--                             http://www.igg.tu-berlin.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
-- forces PostgreSQL to collect table statistics on spatial columns
-- to use spatial indexes
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description     | Author
-- 1.0.0     2010-06-22   PostGIS version   FKun
--

VACUUM ANALYSE cityobject (envelope);
VACUUM ANALYSE surface_geometry (geometry);
VACUUM ANALYSE breakline_relief (ridge_or_valley_lines);
VACUUM ANALYSE breakline_relief (break_lines);
VACUUM ANALYSE masspoint_relief (relief_points);
VACUUM ANALYSE tin_relief (stop_lines);
VACUUM ANALYSE tin_relief (break_lines);
VACUUM ANALYSE tin_relief (control_points);
VACUUM ANALYSE generic_cityobject (lod0_terrain_intersection);
VACUUM ANALYSE generic_cityobject (lod1_terrain_intersection);
VACUUM ANALYSE generic_cityobject (lod2_terrain_intersection);
VACUUM ANALYSE generic_cityobject (lod3_terrain_intersection);
VACUUM ANALYSE generic_cityobject (lod4_terrain_intersection);
VACUUM ANALYSE generic_cityobject (lod1_implicit_ref_point);
VACUUM ANALYSE generic_cityobject (lod2_implicit_ref_point);
VACUUM ANALYSE generic_cityobject (lod3_implicit_ref_point);
VACUUM ANALYSE generic_cityobject (lod4_implicit_ref_point);
VACUUM ANALYSE building (lod1_terrain_intersection);
VACUUM ANALYSE building (lod2_terrain_intersection);
VACUUM ANALYSE building (lod3_terrain_intersection);
VACUUM ANALYSE building (lod4_terrain_intersection);
VACUUM ANALYSE building (lod2_multi_curve);
VACUUM ANALYSE building (lod3_multi_curve);
VACUUM ANALYSE building (lod4_multi_curve);
VACUUM ANALYSE building_furniture (lod4_implicit_ref_point);
VACUUM ANALYSE city_furniture (lod1_terrain_intersection);
VACUUM ANALYSE city_furniture (lod2_terrain_intersection);
VACUUM ANALYSE city_furniture (lod3_terrain_intersection);
VACUUM ANALYSE city_furniture (lod4_terrain_intersection);
VACUUM ANALYSE city_furniture (lod1_implicit_ref_point);
VACUUM ANALYSE city_furniture (lod2_implicit_ref_point);
VACUUM ANALYSE city_furniture (lod3_implicit_ref_point);
VACUUM ANALYSE city_furniture (lod4_implicit_ref_point);
VACUUM ANALYSE citymodel (envelope);
VACUUM ANALYSE cityobjectgroup (geometry);
VACUUM ANALYSE relief_component (extent);
VACUUM ANALYSE solitary_vegetat_object (lod1_implicit_ref_point);
VACUUM ANALYSE solitary_vegetat_object (lod2_implicit_ref_point);
VACUUM ANALYSE solitary_vegetat_object (lod3_implicit_ref_point);
VACUUM ANALYSE solitary_vegetat_object (lod4_implicit_ref_point);
VACUUM ANALYSE surface_data (gt_reference_point);
VACUUM ANALYSE transportation_complex (lod0_network);
VACUUM ANALYSE waterbody (lod0_multi_curve);
VACUUM ANALYSE waterbody (lod1_multi_curve);