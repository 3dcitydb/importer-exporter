-- CITYOBJECT_GENERICATTRIB.sql
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
CREATE TABLE CITYOBJECT_GENERICATTRIB (
ID                      SERIAL NOT NULL,
ATTRNAME                VARCHAR(256) NOT NULL,
DATATYPE                NUMERIC(1),
STRVAL                  VARCHAR(4000),
INTVAL                  NUMERIC,
REALVAL                 NUMERIC,
URIVAL                  VARCHAR(4000),
DATEVAL                 DATE,
GEOMVAL                 GEOMETRY(GeometryZ,:SRSNO),
BLOBVAL                 BYTEA,
CITYOBJECT_ID           INTEGER NOT NULL,
SURFACE_GEOMETRY_ID     INTEGER
)
;

ALTER TABLE CITYOBJECT_GENERICATTRIB
ADD CONSTRAINT CITYOBJ_GENERICATTRIB_PK PRIMARY KEY
(
ID
)
;