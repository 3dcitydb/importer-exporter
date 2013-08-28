-- APPEARANCE.sql
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
CREATE TABLE APPEARANCE (
ID                SERIAL NOT NULL,
GMLID             VARCHAR(256),
GMLID_CODESPACE   VARCHAR(1000),
NAME              VARCHAR(1000),
NAME_CODESPACE    VARCHAR(4000),
DESCRIPTION       VARCHAR(4000),
THEME             VARCHAR(256),
CITYMODEL_ID      INTEGER,
CITYOBJECT_ID     INTEGER
)
;

ALTER TABLE APPEARANCE
ADD CONSTRAINT APPEARANCE_PK PRIMARY KEY
(
ID
)
;