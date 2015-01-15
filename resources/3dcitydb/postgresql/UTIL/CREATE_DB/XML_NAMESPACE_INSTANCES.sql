-- XML_NAMESPACE_INSTANCES.sql
--
-- Authors:     Felix Kunde <fkunde@virtualcitysystems.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--
-- Copyright:   (c) 2012-2014  virtualcitySYSTEMS GmbH
--                             http://www.virtualcitysystems.de
--
-------------------------------------------------------------------------------
-- About:
--
--
-------------------------------------------------------------------------------
--
-- ChangeLog:
--
-- Version | Date       | Description                               | Author
-- 0.0.1     2014-02-10   test                                        ZYao
--
DELETE FROM XML_NAMESPACE;

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (0,'xs',NULL);

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (1,'gml','http://www.opengis.net/gml');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (2,'core','http://www.opengis.net/citygml/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (3,'app','http://www.opengis.net/citygml/appearance/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (4,'gen','http://www.opengis.net/citygml/generics/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (5,'grp','http://www.opengis.net/citygml/cityobjectgroup/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (6,'bldg','http://www.opengis.net/citygml/builing/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (7,'brid','http://www.opengis.net/citygml/bridge/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (8,'dem','http://www.opengis.net/citygml/relief/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (9,'frn','http://www.opengis.net/citygml/cityfurniture/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (10,'luse','http://www.opengis.net/citygml/landuse/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (11,'tran','http://www.opengis.net/citygml/transportation/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (12,'tun','http://www.opengis.net/citygml/tunnel/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (13,'veg','http://www.opengis.net/citygml/vegetation/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (14,'wtr','http://www.opengis.net/citygml/waterbody/2.0');

INSERT INTO XML_NAMESPACE ( ID , NAMESPACE_PREFIX , NAMESPACE_URI )
VALUES (15,'xAL','urn:oasis:names:tc:ciq:xsdschema:xAL:2.0');