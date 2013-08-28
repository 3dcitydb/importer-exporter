-- BUILDING.sql
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
CREATE TABLE BUILDING
(
ID                            SERIAL NOT NULL,
NAME                          VARCHAR(1000),
NAME_CODESPACE                VARCHAR(4000),
BUILDING_PARENT_ID            INTEGER,
BUILDING_ROOT_ID              INTEGER,
DESCRIPTION                   VARCHAR(4000),
CLASS                         VARCHAR(256),
FUNCTION                      VARCHAR(1000),
USAGE                         VARCHAR(1000),
YEAR_OF_CONSTRUCTION          DATE,
YEAR_OF_DEMOLITION            DATE,
ROOF_TYPE                     VARCHAR(256),
MEASURED_HEIGHT               DOUBLE PRECISION,
STOREYS_ABOVE_GROUND          NUMERIC(8),
STOREYS_BELOW_GROUND          NUMERIC(8),
STOREY_HEIGHTS_ABOVE_GROUND   VARCHAR(4000),
STOREY_HEIGHTS_BELOW_GROUND   VARCHAR(4000),
LOD1_TERRAIN_INTERSECTION     GEOMETRY(GeometryZ,:SRSNO),
LOD2_TERRAIN_INTERSECTION     GEOMETRY(GeometryZ,:SRSNO),
LOD3_TERRAIN_INTERSECTION     GEOMETRY(GeometryZ,:SRSNO),
LOD4_TERRAIN_INTERSECTION     GEOMETRY(GeometryZ,:SRSNO),
LOD2_MULTI_CURVE              GEOMETRY(MultiCurveZ,:SRSNO),
LOD3_MULTI_CURVE              GEOMETRY(MultiCurveZ,:SRSNO),
LOD4_MULTI_CURVE              GEOMETRY(MultiCurveZ,:SRSNO),
LOD1_GEOMETRY_ID              INTEGER,
LOD2_GEOMETRY_ID              INTEGER,
LOD3_GEOMETRY_ID              INTEGER,
LOD4_GEOMETRY_ID              INTEGER
);

ALTER TABLE BUILDING
ADD CONSTRAINT BUILDING_PK PRIMARY KEY
(
ID
)
;