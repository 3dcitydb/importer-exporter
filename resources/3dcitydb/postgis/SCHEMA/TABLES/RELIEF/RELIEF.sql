-- RELIEF.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--              Felix Kunde <fkunde@virtualcitysystems.de>
--
-- Copyright:   (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universitaet Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--              (c) 2005-2006, lat/lon GmbH, Germany
--                             http://www.lat-lon.de
--   			(c) 2012       Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description      | Author | Conversion
-- 1.0       2012-05-21   PostGIS version    LPlu     LFra
--                                           TKol     FKun
--                                           GGro
--                                           JSch
--                                           VStr
--                                           APot

CREATE TABLE RELIEF (
ID           SERIAL NOT NULL,
NAME         VARCHAR(256),
TYPE         VARCHAR(256),
LODGROUP     NUMERIC(1) NOT NULL
)
;

ALTER TABLE RELIEF
ADD CONSTRAINT RELIEF_PK PRIMARY KEY
(
ID
)
;