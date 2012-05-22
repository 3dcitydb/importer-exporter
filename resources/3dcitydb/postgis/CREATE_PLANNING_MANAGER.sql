-- CREATE_PLANNING_MANAGER.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <kolbe@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--
-- Conversion:	Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universität Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About:
--
-- Aufruf der Einzelskripte zum Erstellen der notwendigen Tabellen, Indizes,
-- Sequenzen und Prozeduren.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description     | Author | Conversion
-- 1.0       2012-05-21   PostGIS version   LPlu     FKun
--                                          TKol
--                                          GGro
--                                          JSch
--                                          VStr
--

--// create PLANNING_MANAGER schema
CREATE SCHEMA planning_manager;

-- database schema
\i PLANNING_MANAGER/CREATE_TABLES.sql
\i PLANNING_MANAGER/CREATE_CONSTRAINTS.sql
\i PLANNING_MANAGER/CREATE_SPATIAL_INDEX.sql

-- utility procedures
\i PLANNING_MANAGER/CREATE_UTIL_PROCEDURES.sql

-- procedure bodies with return values (Java)
\i PLANNING_MANAGER/CREATE_PLANNING_PROCEDUREBODYS.sql
\i PLANNING_MANAGER/CREATE_PLANNINGALTERNATIVE_PROCEDUREBODYS.sql
\i PLANNING_MANAGER/CREATE_CITYMODELASPECT_PROCEDUREBODYS.sql

-- procedures for console output (psql)
\i PLANNING_MANAGER/CREATE_PLANNING_PROCEDURES.sql
\i PLANNING_MANAGER/CREATE_PLANNINGALTERNATIVE_PROCEDURES.sql
\i PLANNING_MANAGER/CREATE_CITYMODELASPECT_PROCEDURES.sql

COMMIT;