-- TRAFFIC_AREA.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard K�nig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description                               | Author
-- 2.0.0     2007-11-23   release version                             TKol
--                                                                    GKoe
--                                                                    CNag
--                                                                    ASta
--
CREATE TABLE TRAFFIC_AREA
(
ID NUMBER NOT NULL,
IS_AUXILIARY NUMBER(1),
NAME VARCHAR2(1000),
NAME_CODESPACE VARCHAR2(4000),
DESCRIPTION VARCHAR2(4000),
FUNCTION VARCHAR2(1000),
USAGE VARCHAR2(1000),
SURFACE_MATERIAL VARCHAR2(256),
LOD2_MULTI_SURFACE_ID NUMBER,
LOD3_MULTI_SURFACE_ID NUMBER,
LOD4_MULTI_SURFACE_ID NUMBER,
TRANSPORTATION_COMPLEX_ID NUMBER NOT NULL
)
;
ALTER TABLE TRAFFIC_AREA
ADD CONSTRAINT TRAFFIC_AREA_PK PRIMARY KEY
(
ID
)
 ENABLE
;