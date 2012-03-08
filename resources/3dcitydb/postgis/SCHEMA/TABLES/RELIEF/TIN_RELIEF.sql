-- TIN_RELIEF.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--				Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
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
-- 2.0.0     2011-12-09   PostGIS version    TKol	  LFra	
--                                           GKoe     FKun
--                                           CNag
--                                           ASta

-- DROP TABLE "TIN_RELIEF" CASCADE CONSTRAINT PURGE;
                                                            
CREATE TABLE TIN_RELIEF (
	ID 					SERIAL NOT NULL,
	MAX_LENGTH 			DOUBLE PRECISION
)
;

SELECT public.AddGeometryColumn('tin_relief', 'stop_lines', 3068, 'MULTICURVE', 3);
SELECT public.AddGeometryColumn('tin_relief', 'break_lines', 3068, 'MULTICURVE', 3);
SELECT public.AddGeometryColumn('tin_relief', 'control_points', 3068, 'MULTIPOINT', 3);

ALTER TABLE TIN_RELIEF
	ADD COLUMN surface_geometry_id 		INTEGER
;

ALTER TABLE TIN_RELIEF
ADD CONSTRAINT TIN_RELIEF_PK PRIMARY KEY
(
ID
)
;