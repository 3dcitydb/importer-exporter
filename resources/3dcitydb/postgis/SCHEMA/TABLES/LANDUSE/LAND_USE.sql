-- LAND_USE.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Gerhard Koenig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universitaet Berlin, Germany
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
CREATE TABLE LAND_USE (
ID                        SERIAL NOT NULL,
NAME                      VARCHAR(1000),
NAME_CODESPACE            VARCHAR(4000),
DESCRIPTION               VARCHAR(4000),
CLASS                     VARCHAR(256),
FUNCTION                  VARCHAR(1000),
USAGE                     VARCHAR(1000),
LOD0_MULTI_SURFACE_ID     INTEGER,
LOD1_MULTI_SURFACE_ID     INTEGER,
LOD2_MULTI_SURFACE_ID     INTEGER,
LOD3_MULTI_SURFACE_ID     INTEGER,
LOD4_MULTI_SURFACE_ID     INTEGER
)
;

ALTER TABLE LAND_USE
ADD CONSTRAINT LAND_USE_PK PRIMARY KEY
(
ID
)
;