-- SURFACE_GEOMETRY.sql
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
CREATE TABLE SURFACE_GEOMETRY(
	ID 									SERIAL NOT NULL,
	GMLID 								VARCHAR(256),
	GMLID_CODESPACE 					VARCHAR(1000),
	PARENT_ID 							INTEGER,
	ROOT_ID 							INTEGER,
	IS_SOLID 							NUMERIC(1, 0),
	IS_COMPOSITE 						NUMERIC(1, 0),
	IS_TRIANGULATED 					NUMERIC(1, 0),
	IS_XLINK 							NUMERIC(1, 0),
	IS_REVERSE 							NUMERIC(1, 0)
)
;

SELECT AddGeometryColumn('surface_geometry', 'geometry', 3068, 'POLYGON', 3);

ALTER TABLE SURFACE_GEOMETRY
ADD CONSTRAINT SURFACE_GEOMETRY_PK PRIMARY KEY
(
ID
)
;