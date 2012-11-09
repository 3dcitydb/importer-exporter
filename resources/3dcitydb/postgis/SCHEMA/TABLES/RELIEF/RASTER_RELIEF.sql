-- RASTER_RELIEF.sql
--
-- Authors:     Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--              Gerhard Koenig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--              Felix Kunde <felix-kunde@gmx.de>
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
-- raster2pgsql -a -f rasterproperty -s yourSRID -I -M -F -t 128x128 yourRasterFiles.tif raster_relief > rastrelief.sql
-- (see PostGIS-Manual for explanation: http://www.postgis.org/documentation/manual-svn/using_raster.xml.html)
--
-- The geometric extent is calculated in the raster_columns view, if a srid was 
-- assigned to the raster. Pyramidlayers for raster files can be added with the 
-- operator -l 2,4,... in the raster2pgsql command. They will be handled as 
-- additional raster-files and managed in their own tables and in the the 
-- raster_overviews view as well. 
-- Attention: Tables for raster_overviews will not yet be created when appending 
-- a raster to a table. You have to leave out the -a operator and delete the
-- table RASTER_RELIEF first in order to execute the generated SQL file by raster2pgsql.
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
FILENAME          TEXT,
RASTERPROPERTY    RASTER
--RELIEF_ID         INTEGER NOT NULL
)
;

ALTER TABLE RASTER_RELIEF
ADD CONSTRAINT RASTER_RLF_PK PRIMARY KEY
(
ID
)
;