-- GENERIC_CITYOBJECT.sql
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
CREATE TABLE GENERIC_CITYOBJECT (
	ID 									SERIAL NOT NULL,
	NAME 								VARCHAR(1000),
	NAME_CODESPACE 						VARCHAR(4000),
	DESCRIPTION 						VARCHAR(4000),
	CLASS 								VARCHAR(256),
	FUNCTION 							VARCHAR(1000),
	USAGE 								VARCHAR(1000)
)
;

SELECT public.AddGeometryColumn('generic_cityobject', 'lod0_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod1_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod2_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod3_terrain_intersection', 3068, 'GEOMETRY', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod4_terrain_intersection', 3068, 'GEOMETRY', 3);

ALTER TABLE GENERIC_CITYOBJECT
	ADD COLUMN LOD0_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD1_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD2_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD3_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD4_GEOMETRY_ID 		INTEGER,
	ADD COLUMN LOD0_IMPLICIT_REP_ID 	INTEGER,
	ADD COLUMN LOD1_IMPLICIT_REP_ID 	INTEGER,
	ADD COLUMN LOD2_IMPLICIT_REP_ID 	INTEGER,
	ADD COLUMN LOD3_IMPLICIT_REP_ID 	INTEGER,
	ADD COLUMN LOD4_IMPLICIT_REP_ID 	INTEGER
;

SELECT public.AddGeometryColumn('generic_cityobject', 'lod0_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod1_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod2_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod3_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('generic_cityobject', 'lod4_implicit_ref_point', 3068, 'POINT', 3);

ALTER TABLE GENERIC_CITYOBJECT
	ADD COLUMN LOD0_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD1_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD2_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD3_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD4_IMPLICIT_TRANSFORMATION 	VARCHAR(1000)
;

ALTER TABLE GENERIC_CITYOBJECT
ADD CONSTRAINT GENERIC_CITYOBJECT_PK PRIMARY KEY
(
ID
)
;