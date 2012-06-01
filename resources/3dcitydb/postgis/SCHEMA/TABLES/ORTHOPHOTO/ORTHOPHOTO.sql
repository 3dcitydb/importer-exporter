-- ORTHOPHOTO.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--              Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2012  Institute for Geodesy and Geoinformation Science,
--                             Technische Universität Berlin, Germany
--                             http://www.igg.tu-berlin.de
--              (c) 2004-2006, Institute for Cartography and Geoinformation,
--                             Universität Bonn, Germany
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
-- raster2pgsql -f orthophotoproperty -s yourSRID -I -C -M -F -t 128x128 -l 2,4 yourRasterFiles.tif orthophoto > orthophoto.sql
-- (see PostGIS-Manual for explanation: http://www.postgis.org/documentation/manual-svn/using_raster.xml.html)
--
-- After the orthophoto.sql is generated values for columns LOD and DATUM should be
-- added to the INSERT-statement before execution. The parameter -F adds a column
-- which includes the filename and type-ending. The geometric extent is calculated
-- in the raster-columns view, if a srid was assigned to the raster. Pyramid-layer
-- are additional raster-files managed in the raster_overviews view. They are created
-- with the parameter -l level,level,...
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 2.0       2012-01-06   PostGIS version    LPlu     LFra
--                                           TKol	  FKun
--                                           GGro
--                                           JSch
--                                           VStr
--                                           APot
--

CREATE TABLE ORTHOPHOTO (
	ID                        SERIAL NOT NULL,
	LOD                       NUMERIC(1,0) NOT NULL,
	DATUM                     DATE,
	ORTHOPHOTOPROPERTY        RASTER
)
;

ALTER TABLE ORTHOPHOTO
ADD CONSTRAINT ORTHOPHOTO_PK PRIMARY KEY
(
ID
)
;