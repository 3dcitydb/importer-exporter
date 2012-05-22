-- TEXTUREPARAM.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--              Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
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
-- 2.0.0     2012-05-21   PostGIS version    TKol	  LFra	
--                                           GKoe     FKun
--                                           CNag
--                                           ASta
--
CREATE TABLE TEXTUREPARAM (
	SURFACE_GEOMETRY_ID           SERIAL NOT NULL,
	IS_TEXTURE_PARAMETRIZATION    NUMERIC(1, 0),
	WORLD_TO_TEXTURE              VARCHAR(1000),
	TEXTURE_COORDINATES           VARCHAR(4000),
	SURFACE_DATA_ID               INTEGER NOT NULL
)
;

ALTER TABLE TEXTUREPARAM
ADD CONSTRAINT TEXTUREPARAM_PK PRIMARY KEY
(
SURFACE_GEOMETRY_ID,
SURFACE_DATA_ID
)
;