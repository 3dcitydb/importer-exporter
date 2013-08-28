-- SOLITARY_VEGETAT_OBJECT.sql
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
CREATE TABLE SOLITARY_VEGETAT_OBJECT (
ID                             SERIAL NOT NULL,
NAME                           VARCHAR(1000),
NAME_CODESPACE                 VARCHAR(4000),
DESCRIPTION                    VARCHAR(4000),
CLASS                          VARCHAR(256),
SPECIES                        VARCHAR(1000),
FUNCTION                       VARCHAR(1000),
HEIGHT                         DOUBLE PRECISION,
TRUNC_DIAMETER                 DOUBLE PRECISION,
CROWN_DIAMETER                 DOUBLE PRECISION,
LOD1_GEOMETRY_ID               INTEGER,
LOD2_GEOMETRY_ID               INTEGER,
LOD3_GEOMETRY_ID               INTEGER,
LOD4_GEOMETRY_ID               INTEGER,
LOD1_IMPLICIT_REP_ID           INTEGER,
LOD2_IMPLICIT_REP_ID           INTEGER,
LOD3_IMPLICIT_REP_ID           INTEGER,
LOD4_IMPLICIT_REP_ID           INTEGER,
LOD1_IMPLICIT_REF_POINT        GEOMETRY(PointZ,:SRSNO),
LOD2_IMPLICIT_REF_POINT        GEOMETRY(PointZ,:SRSNO),
LOD3_IMPLICIT_REF_POINT        GEOMETRY(PointZ,:SRSNO),
LOD4_IMPLICIT_REF_POINT        GEOMETRY(PointZ,:SRSNO),
LOD1_IMPLICIT_TRANSFORMATION   VARCHAR(1000),
LOD2_IMPLICIT_TRANSFORMATION   VARCHAR(1000),
LOD3_IMPLICIT_TRANSFORMATION   VARCHAR(1000),
LOD4_IMPLICIT_TRANSFORMATION   VARCHAR(1000)
)
;

ALTER TABLE SOLITARY_VEGETAT_OBJECT
ADD CONSTRAINT SOLITARY_VEG_OBJECT_PK PRIMARY KEY
(
ID
)
;