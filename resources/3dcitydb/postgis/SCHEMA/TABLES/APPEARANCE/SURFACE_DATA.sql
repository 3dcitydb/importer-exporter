-- SURFACE_DATA.sql
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
CREATE TABLE SURFACE_DATA (
	ID 									SERIAL NOT NULL,
	GMLID 								VARCHAR(256),
	GMLID_CODESPACE 					VARCHAR(1000),
	NAME 								VARCHAR(1000),
	NAME_CODESPACE 						VARCHAR(4000),
	DESCRIPTION 						VARCHAR(4000),
	IS_FRONT 							NUMERIC(1, 0),
	TYPE 								VARCHAR(30),
	X3D_SHININESS 						DOUBLE PRECISION,
	X3D_TRANSPARENCY 					DOUBLE PRECISION,
	X3D_AMBIENT_INTENSITY 				DOUBLE PRECISION,
	X3D_SPECULAR_COLOR					VARCHAR(256),
	X3D_DIFFUSE_COLOR 					VARCHAR(256),
	X3D_EMISSIVE_COLOR 					VARCHAR(256),
	X3D_IS_SMOOTH 						NUMERIC(1, 0),
	TEX_IMAGE_URI 						VARCHAR(4000),
	TEX_IMAGE 							BYTEA,
	TEX_MIME_TYPE 						VARCHAR(256),
	TEX_TEXTURE_TYPE 					VARCHAR(256),
	TEX_WRAP_MODE 						VARCHAR(256),
	TEX_BORDER_COLOR 					VARCHAR(256),
	GT_PREFER_WORLDFILE 				NUMERIC(1, 0),
	GT_ORIENTATION 						VARCHAR(256),
	GT_REFERENCE_POINT					GEOMETRY(Point,3068)
)
;

ALTER TABLE SURFACE_DATA
ADD CONSTRAINT SURFACE_DATA_PK PRIMARY KEY
(
ID
)
;