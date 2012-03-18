-- ORTHOPHOTO_IMP.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Prof. Dr. Lutz Pluemer <pluemer@ikg.uni-bonn.de>
--              Dr. Gerhard Groeger <groeger@ikg.uni-bonn.de>
--              Joerg Schmittwilken <schmittwilken@ikg.uni-bonn.de>
--              Viktor Stroh <stroh@ikg.uni-bonn.de>
--              Dr. Andreas Poth <poth@lat-lon.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--				Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007,      Institute for Geodesy and Geoinformation Science,
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
-- About:
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description      | Author | Conversion
-- 2.0       2011-12-09   release version    LPlu	  LFra
--                                           TKol	  FKun
--                                           GGro
--                                           JSch
--                                           VStr
--                                           APot
--

-- DROP TABLE "ORTHOPHOTO_IMP" CASCADE CONSTRAINT PURGE;

CREATE TABLE ORTHOPHOTO_IMP (
	ID 					SERIAL 			NOT NULL,
	ORTHOPHOTOPROPERTY  RASTER,
	FILENAME 			VARCHAR(4000)
)
;

SELECT AddGeometryColumn('orthophoto_imp', 'footprint', 3068, 'POLYGON', 3);

ALTER TABLE ORTHOPHOTO_IMP
ADD CONSTRAINT ORTHOPHOTO_IMP_PK PRIMARY KEY
(
ID
)
;
