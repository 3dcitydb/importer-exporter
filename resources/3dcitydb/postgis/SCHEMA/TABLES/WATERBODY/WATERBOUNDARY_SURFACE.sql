-- WATERBOUNDARY_SURFACE.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard K�nig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--              Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
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
-- 2.0.0     2012-05-21   PostGIS version    TKol     LFra	
--                                           GKoe     FKun
--                                           CNag
--                                           ASta
--
CREATE TABLE WATERBOUNDARY_SURFACE (
	ID                        SERIAL NOT NULL,
	NAME                      VARCHAR(1000),
	NAME_CODESPACE            VARCHAR(4000),
	DESCRIPTION               VARCHAR(4000),
	TYPE                      VARCHAR(256),
	WATER_LEVEL               VARCHAR(256),
	LOD2_SURFACE_ID           INTEGER,
	LOD3_SURFACE_ID           INTEGER,
	LOD4_SURFACE_ID           INTEGER
)
;

ALTER TABLE WATERBOUNDARY_SURFACE
ADD CONSTRAINT WATERBOUNDARY_SURFACE_PK PRIMARY KEY
(
ID
)
;