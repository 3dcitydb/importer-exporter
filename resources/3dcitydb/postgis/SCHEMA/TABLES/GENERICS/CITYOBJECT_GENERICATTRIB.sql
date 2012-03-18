-- CITYOBJECT_GENERICATTRIB.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <kolbe@igg.tu-berlin.de>
--              Gerhard K�nig <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <nagel@igg.tu-berlin.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Conversion:  Laure Fraysse <Laure.fraysse@etumel.univmed.fr>
--				Felix Kunde <felix-kunde@gmx.de>
--
-- Copyright:   (c) 2007-2011  Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description     | Author | Conversion
-- 2.0.0     2011-12-09   PostGIS version    TKol	  LFra	
--                                           GKoe     FKun
--                                           CNag
--                                           ASta
--
CREATE TABLE CITYOBJECT_GENERICATTRIB (
	ID 									SERIAL NOT NULL,
	ATTRNAME 							VARCHAR(256) NOT NULL,
	DATATYPE 							NUMERIC(1),
	STRVAL 								VARCHAR(4000),
	INTVAL 								NUMERIC,
	REALVAL 							NUMERIC,
	URIVAL 								VARCHAR(4000),
	DATEVAL 							DATE
)
;

SELECT AddGeometryColumn('cityobject_genericattrib', 'geomval', 3068, 'GEOMETRY', 3);

ALTER TABLE CITYOBJECT_GENERICATTRIB
	ADD COLUMN BLOBVAL 					BYTEA,
	ADD COLUMN CITYOBJECT_ID 			INTEGER NOT NULL,
	ADD COLUMN SURFACE_GEOMETRY_ID 		INTEGER
;

ALTER TABLE CITYOBJECT_GENERICATTRIB
ADD CONSTRAINT CITYOBJ_GENERICATTRIB_PK PRIMARY KEY
(
ID
)
;