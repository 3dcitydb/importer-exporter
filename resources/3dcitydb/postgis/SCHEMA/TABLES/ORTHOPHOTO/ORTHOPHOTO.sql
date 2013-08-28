-- ORTHOPHOTO.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
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
-- raster2pgsql -a -f orthophotoproperty -s yourSRID -I -M -t 128x128 yourRasterFiles.tif orthophoto > orthophoto.sql
-- (see PostGIS-Manual for explanation: http://www.postgis.org/documentation/manual-svn/using_raster.xml.html)
--
-- Before executing the generated sql file it has to be edited! 
-- The INSERT statements do not assign a value for the LOD column, which
-- is necessary due to its constraint.
--
-- Edited example:
-- INSERT INTO "orthophoto" ("lod", "orthophotoproperty") VALUES (2,'01000001...
-- INSERT INTO "orthophoto" ... (multiple inserts when tiling the raster)
--
-- The geometric extent is calculated in the raster_columns view, if a srid was 
-- assigned to the raster.
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 2.0       2012-01-06   PostGIS version    LPlu     LFra
--                                           TKol     FKun
--                                           GGro
--                                           JSch
--                                           VStr
--                                           APot
--

CREATE TABLE ORTHOPHOTO (
ID                    SERIAL NOT NULL,
LOD                   NUMERIC(1,0) NOT NULL,
DATUM                 DATE,
ORTHOPHOTOPROPERTY    RASTER
)
;

ALTER TABLE ORTHOPHOTO
ADD CONSTRAINT ORTHOPHOTO_PK PRIMARY KEY
(
ID
)
;