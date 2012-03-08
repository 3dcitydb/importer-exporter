-- SOLITARY_VEGETAT_OBJECT.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard K�nig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--				Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description     | Author | Conversion
-- 2.0.0     2011-12-09   PostGIS version    TKol	  LFra	
--                                           GKoe     FKun
--                                           CNag
--                                           ASta
--
CREATE TABLE SOLITARY_VEGETAT_OBJECT (
	ID 									SERIAL NOT NULL,
	NAME 								VARCHAR(1000),
	NAME_CODESPACE 						VARCHAR(4000),
	DESCRIPTION 						VARCHAR(4000),
	CLASS 								VARCHAR(256),
	SPECIES 							VARCHAR(1000),
	FUNCTION 							VARCHAR(1000),
	HEIGHT 								DOUBLE PRECISION,
	TRUNC_DIAMETER 						DOUBLE PRECISION,
	CROWN_DIAMETER 						DOUBLE PRECISION,
	LOD1_GEOMETRY_ID 					INTEGER,
	LOD2_GEOMETRY_ID 					INTEGER,
	LOD3_GEOMETRY_ID 					INTEGER,
	LOD4_GEOMETRY_ID 					INTEGER,
	LOD1_IMPLICIT_REP_ID 				INTEGER,
	LOD2_IMPLICIT_REP_ID 				INTEGER,
	LOD3_IMPLICIT_REP_ID 				INTEGER,
	LOD4_IMPLICIT_REP_ID 				INTEGER
)
;

SELECT public.AddGeometryColumn('solitary_vegetat_object', 'lod1_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('solitary_vegetat_object', 'lod2_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('solitary_vegetat_object', 'lod3_implicit_ref_point', 3068, 'POINT', 3);
SELECT public.AddGeometryColumn('solitary_vegetat_object', 'lod4_implicit_ref_point', 3068, 'POINT', 3);

ALTER TABLE SOLITARY_VEGETAT_OBJECT 
	ADD COLUMN LOD1_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD2_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD3_IMPLICIT_TRANSFORMATION 	VARCHAR(1000),
	ADD COLUMN LOD4_IMPLICIT_TRANSFORMATION 	VARCHAR(1000)
;

ALTER TABLE SOLITARY_VEGETAT_OBJECT
ADD CONSTRAINT SOLITARY_VEG_OBJECT_PK PRIMARY KEY
(
ID
)
;