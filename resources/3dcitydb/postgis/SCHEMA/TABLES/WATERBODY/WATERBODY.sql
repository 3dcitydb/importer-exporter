-- WATERBODY.sql
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
CREATE TABLE WATERBODY (
	ID 									SERIAL NOT NULL,
	NAME 								VARCHAR(1000),
	NAME_CODESPACE 						VARCHAR(4000),
	DESCRIPTION 						VARCHAR(4000),
	CLASS 								VARCHAR(256),
	FUNCTION 							VARCHAR(1000),
	USAGE 								VARCHAR(1000)
)
;

SELECT AddGeometryColumn('waterbody', 'lod0_multi_curve', 3068, 'MULTICURVE', 3);
SELECT AddGeometryColumn('waterbody', 'lod1_multi_curve', 3068, 'MULTICURVE', 3);

ALTER TABLE WATERBODY 
	ADD COLUMN LOD1_SOLID_ID 			INTEGER,
	ADD COLUMN LOD2_SOLID_ID			INTEGER,
	ADD COLUMN LOD3_SOLID_ID 			INTEGER,
	ADD COLUMN LOD4_SOLID_ID 			INTEGER,
	ADD COLUMN LOD0_MULTI_SURFACE_ID 	INTEGER,
	ADD COLUMN LOD1_MULTI_SURFACE_ID 	INTEGER
;

ALTER TABLE WATERBODY
ADD CONSTRAINT WATERBODY_PK PRIMARY KEY
(
ID
)
;