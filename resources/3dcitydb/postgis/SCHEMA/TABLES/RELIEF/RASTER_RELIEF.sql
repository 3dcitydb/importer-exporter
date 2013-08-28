-- RASTER_RELIEF.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
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
--              (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universitaet Bonn, Germany
--                             http://www.ikg.uni-bonn.de
--              (c) 2005-2006, lat/lon GmbH, Germany
--                             http://www.lat-lon.de
--
--              This skript is free software under the LGPL Version 2.1.
--              See the GNU Lesser General Public License at
--              http://www.gnu.org/copyleft/lgpl.html
--              for more details.
-------------------------------------------------------------------------------
-- About: Rasterdata is imported via C-Loader raster2pgsql (executed in command line)
-- e.g.
-- raster2pgsql -a -f rasterproperty -s yourSRID -I -M -t 128x128 yourRasterFiles.tif raster_relief > rastrelief.sql
-- (see PostGIS-Manual for explanation: http://www.postgis.org/documentation/manual-svn/using_raster.xml.html)
--
-- Before executing the generated sql file it has to be edited! 
-- The INSERT statements do not assign a value for the RELIEF_ID column, which
-- is necessary due to its constraint. Thus an additional entry in the RELIEF table
-- is needed.
--
-- Edited example:
-- INSERT INTO "relief" ("name", "type", "lodgroup") VALUES ('NameOfRelief', 'TypeOfRelief', 2);
-- SELECT nextval('RELIEF_ID_SEQ');
-- INSERT INTO "raster_relief" ("lod", "rasterproperty", "relief_id") 
--                VALUES (2,'01000001...', (select currval('RELIEF_ID_SEQ'))-1);
-- INSERT INTO "raster_relief" ... (multiple inserts when tiling the raster) 
-- 
-- The geometric extent is calculated in the raster_columns view, if a srid was 
-- assigned to the raster.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 2.0.0     2012-11-09   PostGIS version    LPlu     LFra
--                                           TKol     FKun
--                                           GGro
--                                           JSch
--                                           VStr
--                                           APot
--                                           GKoe
--                                           CNag
--                                           ASta

CREATE TABLE RASTER_RELIEF (
ID                SERIAL NOT NULL,
LOD               NUMERIC(1),
RASTERPROPERTY    RASTER NOT NULL,
RELIEF_ID         INTEGER NOT NULL
)
;

ALTER TABLE RASTER_RELIEF
ADD CONSTRAINT RASTER_RLF_PK PRIMARY KEY
(
ID
)
;