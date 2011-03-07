-- CREATE_PLANNINGMANAGER.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Thomas H. Kolbe <kolbe@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
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
-- Version | Date       | Description                               | Author
-- 1.0       2006-04-03   release version                             LPlu
--                                                                    TKol
--                                                                    GGro
--                                                                    JSch
--                                                                    VStr
--

SELECT 'PlanningManager: Creating tables, sequences and stored procedures!' as message from DUAL;

COMMIT;

SET SERVEROUTPUT ON;

-- database schema
@PLANNINGMANAGER/CREATE_TABLES.sql;
@PLANNINGMANAGER/CREATE_CONSTRAINTS.sql;
@PLANNINGMANAGER/CREATE_SPATIAL_INDEX.sql;

-- utility procedures
@PLANNINGMANAGER/CREATE_UTIL_PROCEDURES;

-- procedure bodies with return values (Java)
@PLANNINGMANAGER/CREATE_PLANNING_PROCEDUREBODYS.sql;
@PLANNINGMANAGER/CREATE_PLANNINGALTERNATIVE_PROCEDUREBODYS.sql;
@PLANNINGMANAGER/CREATE_CITYMODELASPECT_PROCEDUREBODYS.sql;

-- procedures for console output (SQL*Plus)
@PLANNINGMANAGER/CREATE_PLANNING_PROCEDURES.sql;
@PLANNINGMANAGER/CREATE_PLANNINGALTERNATIVE_PROCEDURES.sql;
@PLANNINGMANAGER/CREATE_CITYMODELASPECT_PROCEDURES.sql;

COMMIT;

SHOW ERRORS;

SELECT 'PlanningManager: Finished!' as message from DUAL;
