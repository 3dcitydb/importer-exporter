-- BUILDING.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--				Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
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
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description     | Author | Conversion
-- 2.0.0     2011-12-09   PostGIS version    TKol	  LFra	
--                                           GKoe     FKun
--                                           CNag
--                                           ASta
--
CREATE TABLE BUILDING
(
	ID 									SERIAL NOT NULL,
	NAME 								VARCHAR(1000),
	NAME_CODESPACE 						VARCHAR(4000),
	BUILDING_PARENT_ID 					INTEGER,
	BUILDING_ROOT_ID 					INTEGER,
	DESCRIPTION 						VARCHAR(4000),
	CLASS 								VARCHAR(256),
	FUNCTION 							VARCHAR(1000),
	USAGE 								VARCHAR(1000),
	YEAR_OF_CONSTRUCTION 				DATE,
	YEAR_OF_DEMOLITION 					DATE,
	ROOF_TYPE 							VARCHAR(256),
	MEASURED_HEIGHT 					DOUBLE PRECISION,
	STOREYS_ABOVE_GROUND 				NUMERIC(8),
	STOREYS_BELOW_GROUND 				NUMERIC(8),
	STOREY_HEIGHTS_ABOVE_GROUND 		VARCHAR(4000),
	STOREY_HEIGHTS_BELOW_GROUND 		VARCHAR(4000)
)
;

SELECT AddGeometryColumn('building', 'lod1_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT AddGeometryColumn('building', 'lod2_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT AddGeometryColumn('building', 'lod3_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT AddGeometryColumn('building', 'lod4_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT AddGeometryColumn('building', 'lod2_multi_curve', 3068, 'MULTICURVE', 3);
SELECT AddGeometryColumn('building', 'lod3_multi_curve', 3068, 'MULTICURVE', 3);
SELECT AddGeometryColumn('building', 'lod4_multi_curve', 3068, 'MULTICURVE', 3);

ALTER TABLE BUILDING
	ADD COLUMN LOD1_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD2_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD3_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD4_GEOMETRY_ID 		INTEGER;

ALTER TABLE BUILDING
ADD CONSTRAINT BUILDING_PK PRIMARY KEY
(
ID
)
;