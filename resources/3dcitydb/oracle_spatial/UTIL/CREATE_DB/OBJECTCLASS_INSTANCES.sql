-- OBJECTCLASS_INSTANCES.sql
--
-- Authors:     Prof. Dr. Thomas H. Kolbe <thomas.kolbe@tum.de>
--              Gerhard König <gerhard.koenig@tu-berlin.de>
--              Claus Nagel <cnagel@virtualcitysystems.de>
--              Alexandra Stadler <stadler@igg.tu-berlin.de>
--
-- Copyright:   (c) 2007-2008  Institute for Geodesy and Geoinformation Science,
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
-- Version | Date       | Description                               | Author
-- 2.0.0     2007-11-23   release version                             TKol
--                                                                    GKoe
--                                                                    CNag
--
DELETE FROM OBJECTCLASS;

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (0,'Undefined',NULL);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (1,'Object',NULL);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (2,'_AbstractFeature',1);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (3,'_CityObject',2);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (4,'LandUse',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (5,'GenericCityObject',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (6,'_VegetationObject',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (7,'SolitaryVegetationObject',6);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (8,'PlantCover',6);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (9,'WaterBody',3);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (10,'_WaterBoundarySurface',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (11,'WaterSurface',10);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (12,'WaterGroundSurface',10);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (13,'WaterClosureSurface',10);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (14,'ReliefFeature',3);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (15,'_ReliefComponent',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (16,'TINRelief',15);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (17,'MassPointRelief',15);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (18,'BreaklineRelief',15);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (19,'Raster',15);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (20,'Orthophoto',3);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (21,'CityFurniture',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (22,'_TransportationObject',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (23,'CityObjectGroup',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (24,'_AbstractBuilding',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (25,'BuildingPart',24);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (26,'Building',24);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (27,'BuildingInstallation',3);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (28,'IntBuildingInstallation',3);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (29,'_BoundarySurface',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (30,'CeilingSurface',29);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (31,'InteriorWallSurface',29);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (32,'FloorSurface',29);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (33,'RoofSurface',29);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (34,'WallSurface',29);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (35,'GroundSurface',29);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (36,'ClosureSurface',29);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (37,'_Opening',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (38,'Window',37);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (39,'Door',37);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (40,'BuildingFurniture',3);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (41,'Room',3);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (42,'_TransportationComplex',22);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (43,'Track',42);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (44,'Railway',42);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (45,'Road',42);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (46,'Square',42);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (47,'TrafficArea',22);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (48,'AuxiliaryTrafficArea',22);


INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (49,'FeatureCollection',2);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (50,'Appearance',2);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (51,'_SurfaceData',2);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (52,'_AbstractTexture',51);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (53,'X3DMaterial',51);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (54,'ParameterizedTexture',52);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (55,'GeoreferencedTexture',52);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (56,'TextureParametrization',1);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (57,'CityModel',49);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (58,'Address',2);

INSERT INTO OBJECTCLASS ( ID , CLASSNAME , SUPERCLASS_ID )
VALUES (59,'ImplicitGeometry',1);

COMMIT;
