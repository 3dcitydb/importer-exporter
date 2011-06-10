-- GENERIC_CITYOBJECT.sql
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
CREATE TABLE GENERIC_CITYOBJECT
(
ID NUMBER NOT NULL,
NAME VARCHAR2(1000),
NAME_CODESPACE VARCHAR2(4000),
DESCRIPTION VARCHAR2(4000),
CLASS VARCHAR2(256),
FUNCTION VARCHAR2(1000),
USAGE VARCHAR2(1000),
LOD0_TERRAIN_INTERSECTION MDSYS.SDO_GEOMETRY,
LOD1_TERRAIN_INTERSECTION MDSYS.SDO_GEOMETRY,
LOD2_TERRAIN_INTERSECTION MDSYS.SDO_GEOMETRY,
LOD3_TERRAIN_INTERSECTION MDSYS.SDO_GEOMETRY,
LOD4_TERRAIN_INTERSECTION MDSYS.SDO_GEOMETRY,
LOD0_GEOMETRY_ID NUMBER,
LOD1_GEOMETRY_ID NUMBER,
LOD2_GEOMETRY_ID NUMBER,
LOD3_GEOMETRY_ID NUMBER,
LOD4_GEOMETRY_ID NUMBER,
LOD0_IMPLICIT_REP_ID NUMBER,
LOD1_IMPLICIT_REP_ID NUMBER,
LOD2_IMPLICIT_REP_ID NUMBER,
LOD3_IMPLICIT_REP_ID NUMBER,
LOD4_IMPLICIT_REP_ID NUMBER,
LOD0_IMPLICIT_REF_POINT MDSYS.SDO_GEOMETRY,
LOD1_IMPLICIT_REF_POINT MDSYS.SDO_GEOMETRY,
LOD2_IMPLICIT_REF_POINT MDSYS.SDO_GEOMETRY,
LOD3_IMPLICIT_REF_POINT MDSYS.SDO_GEOMETRY,
LOD4_IMPLICIT_REF_POINT MDSYS.SDO_GEOMETRY,
LOD0_IMPLICIT_TRANSFORMATION VARCHAR2(1000),
LOD1_IMPLICIT_TRANSFORMATION VARCHAR2(1000),
LOD2_IMPLICIT_TRANSFORMATION VARCHAR2(1000),
LOD3_IMPLICIT_TRANSFORMATION VARCHAR2(1000),
LOD4_IMPLICIT_TRANSFORMATION VARCHAR2(1000)
)
;
ALTER TABLE GENERIC_CITYOBJECT
ADD CONSTRAINT GENERIC_CITYOBJECT_PK PRIMARY KEY
(
ID
)
 ENABLE
;