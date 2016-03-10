-- SEQUENCES.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Zhihang Yao <zhihang.yao@tum.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--              Philipp Willkomm <pwillkomm@moss.de>
--
-- Copyright:   (c) 2012-2016  Chair of Geoinformatics,
--                             Technische Universit�t M�nchen, Germany
--                             http://www.gis.bv.tum.de
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
-- 3.0.0     2015-03-05   added support for Oracle Locator            ZYao
-- 3.0.0     2013-12-06   new version for 3DCityDB V3                 ZYao
--                                                                    TKol
--                                                                    CNag
--                                                                    PWil
-- 2.0.0     2007-11-23   release version                             TKol
--                                                                    GKoe
--                                                                    CNag
--                                                                    ALor
CREATE SEQUENCE ADDRESS_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE APPEARANCE_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE CITYMODEL_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE CITYOBJECT_GENERICATT_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE CITYOBJECT_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE EXTERNAL_REF_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE IMPLICIT_GEOMETRY_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

BEGIN
  IF ('&DBVERSION'='S' or '&DBVERSION'='s') THEN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE GRID_COVERAGE_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 NOCACHE';  
    EXECUTE IMMEDIATE 'CREATE SEQUENCE GRID_COVERAGE_RDT_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 NOCACHE';
  END IF;
END;
/

CREATE SEQUENCE SURFACE_DATA_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE SURFACE_GEOMETRY_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;

CREATE SEQUENCE TEX_IMAGE_SEQ INCREMENT BY 1 START WITH 1 MINVALUE 1 CACHE 10000;
